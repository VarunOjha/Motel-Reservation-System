package main

import (
	"fmt"
	"motels/seed"
	"time"
)

func main() {
	fmt.Printf("\n Seeing data")

	fmt.Printf("\n Adding motel chains")
	time.Sleep(2 * time.Second)
	seed.AddMotelChains()

	fmt.Printf("\n Adding motels to motel chains")
	time.Sleep(2 * time.Second)
	motelMappings := seed.AddMotelsToMotelChains()

	time.Sleep(2 * time.Second)
	motelRoomCategoriesMapping := seed.AddRoomCategoryToMotel(motelMappings)
	fmt.Printf("\n Motel data seeding completed successfully!\n")

	time.Sleep(2 * time.Second)
	seed.AddRoomsToMotel(motelRoomCategoriesMapping)
	fmt.Printf("\n Rooms added\n")

}
