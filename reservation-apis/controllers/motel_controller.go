package controllers

import (
	"context"
	"net/http"
	"reservation-apis/database"
	"reservation-apis/models"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
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
