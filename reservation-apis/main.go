package main

import (
	"log"
	"os"
	"reservation-apis/database"
	"reservation-apis/routes"

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
)

func main() {
	godotenv.Load()
	database.ConnectMongo()

	r := gin.Default()
	routes.RegisterRoutes(r)

	port := os.Getenv("PORT")
	if port == "" {
		port = "8081"
	}

	log.Println("Starting server on port:", port)
	r.Run(":" + port)
}
