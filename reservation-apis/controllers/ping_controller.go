package controllers

import (
	"context"
	"net/http"
	"reservation-apis/database"
	"reservation-apis/models"
	"time"

	"github.com/gin-gonic/gin"
)

func Ping(c *gin.Context) {
	// Test MongoDB connection
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	// Try to ping the database
	err := database.MotelDB.Client().Ping(ctx, nil)
	if err != nil {
		responseData := map[string]interface{}{
			"message":  "pong",
			"database": "connection failed",
			"error":    err.Error(),
		}
		response := models.NewApiResponse("500", responseData)
		c.JSON(http.StatusInternalServerError, response)
		return
	}

	responseData := map[string]interface{}{
		"message":  "pong",
		"database": "working fine",
	}
	response := models.NewApiResponse("200", responseData)
	c.JSON(http.StatusOK, response)
}
