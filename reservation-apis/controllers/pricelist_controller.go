package controllers

import (
	"context"
	"net/http"
	"reservation-apis/database"
	"reservation-apis/models"
	"time"

	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
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

	collection := database.MotelDB.Collection("prices")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	// Check if a record already exists with the same combination
	filter := bson.M{
		"motel_id":                 price.MotelID,
		"motel_chain_id":           price.MotelChainID,
		"motel_room_category_id":   price.MotelRoomCategoryID,
		"motel_room_category_name": price.MotelRoomCategoryName,
		"date":                     price.Date,
	}

	var existingPrice models.MotelRoomPrice
	err := collection.FindOne(ctx, filter).Decode(&existingPrice)
	if err == nil {
		// Record exists, return the existing record
		responseData := map[string]interface{}{
			"message": "Price record already exists",
			"data":    existingPrice,
		}
		response := models.NewApiResponse("200", responseData)
		c.JSON(http.StatusOK, response)
		return
	} else if err != mongo.ErrNoDocuments {
		// Some other error occurred during the search
		response := models.NewErrorResponse("500", "Error checking for existing price record")
		c.JSON(http.StatusInternalServerError, response)
		return
	}

	// No existing record found, proceed with insertion
	// Set timestamps
	now := time.Now()
	price.CreatedAt = now
	price.UpdatedAt = now

	_, err = collection.InsertOne(ctx, price)
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
