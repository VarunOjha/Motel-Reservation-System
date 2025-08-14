package controllers

import (
	"context"
	"net/http"
	"reservation-apis/database"
	"reservation-apis/models"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo/options"
)

func GetAllMotels(c *gin.Context) {
	collection := database.MotelDB.Collection("prices")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	// Get pagination parameters from query params
	pageStr := c.DefaultQuery("page", "1")
	limitStr := c.DefaultQuery("limit", "10")

	// Convert to integers
	page, err := strconv.Atoi(pageStr)
	if err != nil || page < 1 {
		response := models.NewErrorResponse("400", "Invalid page number. Page must be a positive integer")
		c.JSON(http.StatusBadRequest, response)
		return
	}

	limit, err := strconv.Atoi(limitStr)
	if err != nil || limit < 1 || limit > 100 {
		response := models.NewErrorResponse("400", "Invalid limit. Limit must be between 1 and 100")
		c.JSON(http.StatusBadRequest, response)
		return
	}

	// Calculate skip value for pagination
	skip := (page - 1) * limit

	// Get total count for pagination info
	totalCount, err := collection.CountDocuments(ctx, map[string]interface{}{})
	if err != nil {
		response := models.NewErrorResponse("500", "Cannot count motels")
		c.JSON(http.StatusInternalServerError, response)
		return
	}

	// Set up find options with pagination
	findOptions := options.Find()
	findOptions.SetSkip(int64(skip))
	findOptions.SetLimit(int64(limit))

	cursor, err := collection.Find(ctx, map[string]interface{}{}, findOptions)
	if err != nil {
		response := models.NewErrorResponse("500", "Cannot fetch motels")
		c.JSON(http.StatusInternalServerError, response)
		return
	}
	defer cursor.Close(ctx)

	var prices []models.MotelRoomPrice
	if err := cursor.All(ctx, &prices); err != nil {
		response := models.NewErrorResponse("500", "Error parsing motels data")
		c.JSON(http.StatusInternalServerError, response)
		return
	}

	// Calculate pagination info
	totalPages := (totalCount + int64(limit) - 1) / int64(limit) // Ceiling division
	hasNext := page < int(totalPages)
	hasPrev := page > 1

	// Create paginated response
	paginatedResponse := map[string]interface{}{
		"data": prices,
		"pagination": map[string]interface{}{
			"current_page": page,
			"per_page":     limit,
			"total_items":  totalCount,
			"total_pages":  totalPages,
			"has_next":     hasNext,
			"has_previous": hasPrev,
		},
	}

	response := models.NewApiResponse("200", paginatedResponse)
	c.JSON(http.StatusOK, response)
}

func GetAvailableMotels(c *gin.Context) {
	collection := database.MotelDB.Collection("prices")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	// Build match stage for aggregation pipeline
	matchStage := bson.M{"$match": bson.M{}}

	// Check for date range parameters
	if fromDate := c.Query("from_date"); fromDate != "" {
		if matchStage["$match"].(bson.M)["date"] == nil {
			matchStage["$match"].(bson.M)["date"] = bson.M{}
		}
		matchStage["$match"].(bson.M)["date"].(bson.M)["$gte"] = fromDate
	}

	if toDate := c.Query("to_date"); toDate != "" {
		if matchStage["$match"].(bson.M)["date"] == nil {
			matchStage["$match"].(bson.M)["date"] = bson.M{}
		}
		matchStage["$match"].(bson.M)["date"].(bson.M)["$lte"] = toDate
	}

	// Add status filter to only include active records
	matchStage["$match"].(bson.M)["status"] = "Active"

	// MongoDB aggregation pipeline to group by motel_id, motel_chain_id, and date
	pipeline := []bson.M{
		matchStage,
		{
			"$addFields": bson.M{
				"available_room_int": bson.M{
					"$toInt": bson.M{
						"$ifNull": []interface{}{"$available_room_count", "0"},
					},
				},
				"booked_room_int": bson.M{
					"$toInt": bson.M{
						"$ifNull": []interface{}{"$booked_room_count", "0"},
					},
				},
				"price_double": bson.M{
					"$toDouble": bson.M{
						"$ifNull": []interface{}{"$price", "0"},
					},
				},
			},
		},
		{
			"$group": bson.M{
				"_id": bson.M{
					"motel_id":       "$motel_id",
					"motel_chain_id": "$motel_chain_id",
					"date":           "$date",
				},
				"motel_name":       bson.M{"$first": "$motel_name"},
				"motel_chain_name": bson.M{"$first": "$motel_chain_name"},
				"latitude":         bson.M{"$first": "$latitude"},
				"longitude":        bson.M{"$first": "$longitude"},
				"total_available_rooms": bson.M{
					"$sum": "$available_room_int",
				},
				"total_booked_rooms": bson.M{
					"$sum": "$booked_room_int",
				},
				"room_categories": bson.M{
					"$push": bson.M{
						"category_id":          "$motel_room_category_id",
						"category_name":        "$motel_room_category_name",
						"available_room_count": "$available_room_count",
						"booked_room_count":    "$booked_room_count",
						"price":                "$price",
					},
				},
				"min_price": bson.M{"$min": "$price_double"},
				"max_price": bson.M{"$max": "$price_double"},
			},
		},
		{
			"$project": bson.M{
				"_id":                   0,
				"motel_id":              "$_id.motel_id",
				"motel_chain_id":        "$_id.motel_chain_id",
				"date":                  "$_id.date",
				"motel_name":            1,
				"motel_chain_name":      1,
				"latitude":              1,
				"longitude":             1,
				"total_available_rooms": 1,
				"total_booked_rooms":    1,
				"room_categories":       1,
				"price_range": bson.M{
					"min_price": "$min_price",
					"max_price": "$max_price",
				},
			},
		},
		{
			"$sort": bson.M{
				"motel_id": 1,
				"date":     1,
			},
		},
	}

	cursor, err := collection.Aggregate(ctx, pipeline)
	if err != nil {
		response := models.NewErrorResponse("500", "Cannot fetch available motels: "+err.Error())
		c.JSON(http.StatusInternalServerError, response)
		return
	}
	defer cursor.Close(ctx)

	var aggregatedResults []map[string]interface{}
	if err := cursor.All(ctx, &aggregatedResults); err != nil {
		response := models.NewErrorResponse("500", "Error parsing available motels data: "+err.Error())
		c.JSON(http.StatusInternalServerError, response)
		return
	}

	// Check if no data was found
	if len(aggregatedResults) == 0 {
		// Try a simple find to check if there's any data at all
		var count int64
		count, err = collection.CountDocuments(ctx, bson.M{})
		if err != nil {
			response := models.NewErrorResponse("500", "Error checking data existence")
			c.JSON(http.StatusInternalServerError, response)
			return
		}

		if count == 0 {
			response := models.NewApiResponse("200", []interface{}{})
			c.JSON(http.StatusOK, response)
			return
		}

		// If there is data but aggregation returned nothing, return with message
		responseData := map[string]interface{}{
			"message": "No active motels found matching the criteria",
			"data":    []interface{}{},
		}
		response := models.NewApiResponse("200", responseData)
		c.JSON(http.StatusOK, response)
		return
	}

	response := models.NewApiResponse("200", aggregatedResults)
	c.JSON(http.StatusOK, response)
}

// Debug function to see raw data in the collection
func GetDebugMotelPrices(c *gin.Context) {
	collection := database.MotelDB.Collection("prices")
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	// Simple find to see what's in the collection
	cursor, err := collection.Find(ctx, bson.M{})
	if err != nil {
		response := models.NewErrorResponse("500", "Cannot fetch prices: "+err.Error())
		c.JSON(http.StatusInternalServerError, response)
		return
	}
	defer cursor.Close(ctx)

	var results []map[string]interface{}
	if err := cursor.All(ctx, &results); err != nil {
		response := models.NewErrorResponse("500", "Error parsing prices data: "+err.Error())
		c.JSON(http.StatusInternalServerError, response)
		return
	}

	response := models.NewApiResponse("200", results)
	c.JSON(http.StatusOK, response)
}
