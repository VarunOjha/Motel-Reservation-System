package controllers

import (
	"net/http"
	"reservation-apis/models"

	"github.com/gin-gonic/gin"
)

func Health(c *gin.Context) {
	responseData := map[string]interface{}{
		"status": "all correct",
	}
	response := models.NewApiResponse("200", responseData)
	c.JSON(http.StatusOK, response)
}
