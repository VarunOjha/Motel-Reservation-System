package controllers

import (
	"context"
	"net/http"
	"reservation-apis/database"
	"reservation-apis/models"
	"time"

	"github.com/gin-gonic/gin"
)

func GetPriceList(c *gin.Context) {
	collection := database.MotelDB.Collection("prices")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	cursor, err := collection.Find(ctx, map[string]interface{}{})
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Cannot fetch prices"})
		return
	}
	defer cursor.Close(ctx)

	var prices []models.MotelRoomPrice
	if err := cursor.All(ctx, &prices); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Error parsing prices"})
		return
	}

	c.JSON(http.StatusOK, prices)
}

func PostReservation(c *gin.Context) {
	var res models.Reservation
	if err := c.ShouldBindJSON(&res); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid input"})
		return
	}

	collection := database.MotelDB.Collection("reservations")
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	_, err := collection.InsertOne(ctx, res)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Could not save reservation"})
		return
	}

	c.JSON(http.StatusCreated, gin.H{"message": "Reservation successful"})
}
