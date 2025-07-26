package seed

import (
	"fmt"
	"motels/motelclient"
)

func AddMotelsToMotelChains() {
	getMotelChainsApi := "http://localhost:8080/api/motels/chains"
	// jsonPath := "resources/motel-chain.json"
	// jsonKey := "motelChainId"

	ids, err := motelclient.GetMotelChainIds(getMotelChainsApi)
	if err != nil {
		fmt.Printf("Failed to get motels chain: %v\n", err)
		return
	}

	fmt.Println("Extracted Motel Chain IDs:")
	for _, id := range ids {
		fmt.Println(id)
	}
}
