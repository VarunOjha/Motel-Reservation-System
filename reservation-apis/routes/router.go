package routes

import (
	"reservation-apis/controllers"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	v1 := r.Group("/api/v1")
	{
		v1.GET("/price-list", controllers.GetPriceList)
		v1.POST("/reservation", controllers.PostReservation)
	}
}
