package controllers

import (
	"context"
	"net/http"
	"reservation-apis/database"
	"reservation-apis/models"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/bson"
)

func GetReservation(c *gin.Context) {
	// Get required query parameters
	motelID := c.Query("MotelID")
	motelChainID := c.Query("MotelChainID")

	// Validate required parameters
	if motelID == "" {
		response := models.NewErrorResponse("400", "MotelID is required")
		c.JSON(http.StatusBadRequest, response)
		return
	}
	if motelChainID == "" {
		response := models.NewErrorResponse("400", "MotelChainID is required")
		c.JSON(http.StatusBadRequest, response)
		return
	}

	// Connect to MongoDB
	reservationsCollection := database.MotelDB.Collection("reservations")
	ctx := context.TODO()

	// Create filter for MongoDB query
	filter := bson.M{
		"motel_id":       motelID,
		"motel_chain_id": motelChainID,
	}

	// Find reservations
	cursor, err := reservationsCollection.Find(ctx, filter)
	if err != nil {
		response := models.NewErrorResponse("500", "Failed to query reservations")
		c.JSON(http.StatusInternalServerError, response)
		return
	}
	defer cursor.Close(ctx)

	// Decode results
	var reservations []models.Reservation
	if err = cursor.All(ctx, &reservations); err != nil {
		response := models.NewErrorResponse("500", "Failed to decode reservations")
		c.JSON(http.StatusInternalServerError, response)
		return
	}

	// Return successful response
	response := models.NewApiResponse("200", reservations)
	c.JSON(http.StatusOK, response)
}

func PostReservation(c *gin.Context) {
	var res models.Reservation
	if err := c.ShouldBindJSON(&res); err != nil {
		response := models.NewErrorResponse("400", "Invalid input")
		c.JSON(http.StatusBadRequest, response)
		return
	}

	// Validate required fields are present and not empty
	if res.MotelID == "" {
		response := models.NewErrorResponse("400", "motel_id is required and cannot be empty")
		c.JSON(http.StatusBadRequest, response)
		return
	}
	if res.MotelChainID == "" {
		response := models.NewErrorResponse("400", "motel_chain_id is required and cannot be empty")
		c.JSON(http.StatusBadRequest, response)
		return
	}
	if res.MotelRoomCategoryID == "" {
		response := models.NewErrorResponse("400", "motel_room_category_id is required and cannot be empty")
		c.JSON(http.StatusBadRequest, response)
		return
	}

	// Check if the combination exists in prices table
	pricesCollection := database.MotelDB.Collection("prices")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	filter := map[string]interface{}{
		"motel_id":               res.MotelID,
		"motel_chain_id":         res.MotelChainID,
		"motel_room_category_id": res.MotelRoomCategoryID,
	}

	var existingPrice models.MotelRoomPrice
	err := pricesCollection.FindOne(ctx, filter).Decode(&existingPrice)
	if err != nil {
		response := models.NewErrorResponse("400", "Invalid combination: motel_id, motel_chain_id, and motel_room_category_id combination not found in available prices")
		c.JSON(http.StatusBadRequest, response)
		return
	}

	// Check if the record is active
	if strings.ToLower(existingPrice.Status) != "active" {
		response := models.NewErrorResponse("400", "Selected motel/room combination is not active")
		c.JSON(http.StatusBadRequest, response)
		return
	}

	// Check availability for the date range
	// Generate all dates between check-in and check-out (inclusive)
	current := res.CheckIn
	for current.Before(res.CheckOut) || current.Equal(res.CheckOut) {
		dateStr := current.Format("2006-01-02")

		// Check availability for each date
		availabilityFilter := map[string]interface{}{
			"motel_id":               res.MotelID,
			"motel_chain_id":         res.MotelChainID,
			"motel_room_category_id": res.MotelRoomCategoryID,
			"date":                   dateStr,
		}

		var priceForDate models.MotelRoomPrice
		err := pricesCollection.FindOne(ctx, availabilityFilter).Decode(&priceForDate)
		if err != nil {
			response := models.NewErrorResponse("400", "No pricing data available for date: "+dateStr)
			c.JSON(http.StatusBadRequest, response)
			return
		}

		// Check if the record is active
		if strings.ToLower(priceForDate.Status) != "active" {
			response := models.NewErrorResponse("400", "Motel/room combination is not active for date: "+dateStr)
			c.JSON(http.StatusBadRequest, response)
			return
		}

		// Check if available room count is 0
		if priceForDate.AvailableRoomCount == "0" {
			response := models.NewErrorResponse("400", "No rooms available for date: "+dateStr)
			c.JSON(http.StatusBadRequest, response)
			return
		}

		// Move to next date
		current = current.AddDate(0, 0, 1)
	}

	// Update room counts for all dates in the reservation range
	current = res.CheckIn
	for current.Before(res.CheckOut) || current.Equal(res.CheckOut) {
		dateStr := current.Format("2006-01-02")

		// Find the record for this date
		updateFilter := map[string]interface{}{
			"motel_id":               res.MotelID,
			"motel_chain_id":         res.MotelChainID,
			"motel_room_category_id": res.MotelRoomCategoryID,
			"date":                   dateStr,
		}

		var currentPrice models.MotelRoomPrice
		err := pricesCollection.FindOne(ctx, updateFilter).Decode(&currentPrice)
		if err != nil {
			response := models.NewErrorResponse("500", "Failed to update room counts for date: "+dateStr)
			c.JSON(http.StatusInternalServerError, response)
			return
		}

		// Convert string counts to integers for calculation
		availableCount, err := strconv.Atoi(currentPrice.AvailableRoomCount)
		if err != nil {
			response := models.NewErrorResponse("500", "Invalid available room count format for date: "+dateStr)
			c.JSON(http.StatusInternalServerError, response)
			return
		}

		bookedCount, err := strconv.Atoi(currentPrice.BookedRoomCount)
		if err != nil {
			response := models.NewErrorResponse("500", "Invalid booked room count format for date: "+dateStr)
			c.JSON(http.StatusInternalServerError, response)
			return
		}

		// Decrement available count and increment booked count
		newAvailableCount := availableCount - 1
		newBookedCount := bookedCount + 1

		// Update the record
		update := map[string]interface{}{
			"$set": map[string]interface{}{
				"available_room_count": strconv.Itoa(newAvailableCount),
				"booked_room_count":    strconv.Itoa(newBookedCount),
				"updated_at":           time.Now(),
			},
		}

		_, err = pricesCollection.UpdateOne(ctx, updateFilter, update)
		if err != nil {
			response := models.NewErrorResponse("500", "Failed to update room availability for date: "+dateStr)
			c.JSON(http.StatusInternalServerError, response)
			return
		}

		// Move to next date
		current = current.AddDate(0, 0, 1)
	}

	// Set timestamps
	now := time.Now()
	res.CreatedAt = now
	res.UpdatedAt = now

	// Save reservation
	reservationsCollection := database.MotelDB.Collection("reservations")
	_, err = reservationsCollection.InsertOne(ctx, res)
	if err != nil {
		response := models.NewErrorResponse("500", "Could not save reservation")
		c.JSON(http.StatusInternalServerError, response)
		return
	}

	responseData := map[string]interface{}{
		"message": "Reservation successful",
		"data":    res,
	}
	response := models.NewApiResponse("201", responseData)
	c.JSON(http.StatusCreated, response)
}
