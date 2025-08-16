package routes

import (
	"reservation-apis/controllers"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	v1 := r.Group("/reservationApi/v1")
	{
		v1.GET("/priceList", controllers.GetPriceList)
		v1.POST("/priceList", controllers.PostPriceList)
		v1.GET("/availableMotels", controllers.GetAvailableMotels)
		v1.GET("/getAllReservations", controllers.GetAllReservations)
		v1.GET("/allMotels", controllers.GetAllMotels)
		v1.GET("/debugPrices", controllers.GetDebugMotelPrices)
		v1.GET("/ping", controllers.Ping)
		v1.GET("/health", controllers.Health)
		v1.GET("/reservation", controllers.GetReservation)
		v1.POST("/reservation", controllers.PostReservation)
		v1.GET("/allbookings", controllers.GetAllBookings)
	}
}
