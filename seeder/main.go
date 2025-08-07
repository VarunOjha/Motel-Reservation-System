package main

import (
	"fmt"
	"motels/seed"
	"os"
	"time"
)

func main() {
	apiBaseUrl := os.Getenv("MOTEL_API_BASE_URL")
	if apiBaseUrl == "" {
		apiBaseUrl = "http://localhost:8085/motelApi/v1" // default fallback
	}

	fmt.Printf("\n Seeding data")

	fmt.Printf("\n Adding motel chains")
	time.Sleep(2 * time.Second)
	seed.AddMotelChains(apiBaseUrl)

	fmt.Printf("\n Adding motels to motel chains")
	time.Sleep(2 * time.Second)
	motelMappings := seed.AddMotelsToMotelChains(apiBaseUrl)

	time.Sleep(2 * time.Second)
	motelRoomCategoriesMapping := seed.AddRoomCategoryToMotel(apiBaseUrl, motelMappings)
	fmt.Printf("\n Motel data seeding completed successfully!\n")

	time.Sleep(2 * time.Second)
	seed.AddRoomsToMotel(apiBaseUrl, motelRoomCategoriesMapping)
	fmt.Printf("\n Rooms added\n")
}
