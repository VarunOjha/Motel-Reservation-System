package controllers

import (
	"context"
	"net/http"
	"reservation-apis/database"
	"reservation-apis/models"
	"time"

	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/bson"
)

func GetPriceList(c *gin.Context) {
	collection := database.MotelDB.Collection("prices")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	// Build query filter based on query parameters
	filter := map[string]interface{}{}
	hasValidParam := false

	// Check for motel_id query parameter
	if motelID := c.Query("motel_id"); motelID != "" {
		filter["motel_id"] = motelID
		hasValidParam = true
	}

	// Check for motel_chain_id query parameter
	if motelChainID := c.Query("motel_chain_id"); motelChainID != "" {
		filter["motel_chain_id"] = motelChainID
		hasValidParam = true
	}

	// Return error if no valid query parameters are provided
	if !hasValidParam {
		response := models.NewErrorResponse("400", "Required query parameter missing. Please provide either 'motel_id' or 'motel_chain_id'")
		c.JSON(http.StatusBadRequest, response)
		return
	}

	cursor, err := collection.Find(ctx, filter)
	if err != nil {
		response := models.NewErrorResponse("500", "Cannot fetch prices")
		c.JSON(http.StatusInternalServerError, response)
		return
	}
	defer cursor.Close(ctx)

	var prices []models.MotelRoomPrice
	if err := cursor.All(ctx, &prices); err != nil {
		response := models.NewErrorResponse("500", "Error parsing prices")
		c.JSON(http.StatusInternalServerError, response)
		return
	}

	response := models.NewApiResponse("200", prices)
	c.JSON(http.StatusOK, response)
}

func PostPriceList(c *gin.Context) {
	var price models.MotelRoomPrice
	if err := c.ShouldBindJSON(&price); err != nil {
		response := models.NewErrorResponse("400", "Invalid input")
		c.JSON(http.StatusBadRequest, response)
		return
	}

	// Set timestamps
	now := time.Now()
	price.CreatedAt = now
	price.UpdatedAt = now

	collection := database.MotelDB.Collection("prices")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	_, err := collection.InsertOne(ctx, price)
	if err != nil {
		response := models.NewErrorResponse("500", "Could not save price")
		c.JSON(http.StatusInternalServerError, response)
		return
	}

	responseData := map[string]interface{}{
		"message": "Price saved successfully",
		"data":    price,
	}
	response := models.NewApiResponse("201", responseData)
	c.JSON(http.StatusCreated, response)
}

func GetAvailableMotels(c *gin.Context) {
	collection := database.MotelDB.Collection("prices")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	// Build query filter based on query parameters
	filter := bson.M{}

	// Check for date range parameters
	if fromDate := c.Query("from_date"); fromDate != "" {
		if filter["date"] == nil {
			filter["date"] = bson.M{}
		}
		filter["date"].(bson.M)["$gte"] = fromDate
	}

	if toDate := c.Query("to_date"); toDate != "" {
		if filter["date"] == nil {
			filter["date"] = bson.M{}
		}
		filter["date"].(bson.M)["$lte"] = toDate
	}

	cursor, err := collection.Find(ctx, filter)
	if err != nil {
		response := models.NewErrorResponse("500", "Cannot fetch available motels")
		c.JSON(http.StatusInternalServerError, response)
		return
	}
	defer cursor.Close(ctx)

	var prices []models.MotelRoomPrice
	if err := cursor.All(ctx, &prices); err != nil {
		response := models.NewErrorResponse("500", "Error parsing available motels data")
		c.JSON(http.StatusInternalServerError, response)
		return
	}

	response := models.NewApiResponse("200", prices)
	c.JSON(http.StatusOK, response)
}
