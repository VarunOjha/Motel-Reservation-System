package seed

import (
	"encoding/json"
	"fmt"
	"motels/motelclient"
)

func AddMotelsToMotelChains() {
	getMotelChainsApi := "http://localhost:8080/api/motels/chains"

	body := motelclient.Get(getMotelChainsApi)
	var motels []motelclient.MotelChainResponse
	if err := json.Unmarshal(body, &motels); err != nil {
		fmt.Errorf("failed to parse JSON response: %v", err)
	}

	var ids []string
	for _, motel := range motels {
		if motel.MotelChainId != "" {
			ids = append(ids, motel.MotelChainId)
		}
	}

	fmt.Println("Extracted Motel Chain IDs:")
	for _, id := range ids {
		fmt.Println(id)
	}
}
