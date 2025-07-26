package scripts

import (
	"fmt"
)

func main() {
	api := "http://localhost:8080/api/motels"
	jsonPath := "resources/motel-chain.json"
	jsonKey := "motelChainId"

	ids, err := PostMotelChains(api, jsonPath, jsonKey)
	if err != nil {
		fmt.Printf("Failed to post motels: %v\n", err)
		return
	}

	fmt.Println("Extracted IDs:")
	for _, id := range ids {
		fmt.Println(id)
	}
}
