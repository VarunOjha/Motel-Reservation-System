package main

import (
	"fmt"
	"motels/seed"
	"time"
)

func main() {
	fmt.Printf("\n Seeing data")

	fmt.Printf("\n Adding motel chains")
	time.Sleep(5 * time.Second)
	seed.AddMotelChains()

	fmt.Printf("\n Adding motels to motel chains")
	time.Sleep(5 * time.Second)
	seed.AddMotelsToMotelChains()
	time.Sleep(5 * time.Second)
}
