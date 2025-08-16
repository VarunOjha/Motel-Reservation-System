package seed

import (
	"encoding/json"
	"fmt"
	"motels/motelclient"
	"os"
)

func AddMotelChains() {
	apiEndpoint := "http://a85a9798b265f437f9c79edf50d2f68d-1226124390.us-west-2.elb.amazonaws.com/motelApi/v1/motels/chains"
	jsonPath := "resources/motel-chain.json"

	// Read JSON file
	jsonFile, err := os.ReadFile(jsonPath)
	if err != nil {
		fmt.Printf("error reading file: %v\n", err)
		return
	}

	var motels []motelclient.MotelChain
	err = json.Unmarshal(jsonFile, &motels)
	if err != nil {
		fmt.Printf("error parsing JSON: %v\n", err)
	}
	var results []string

	for i, motel := range motels {
		jsonData, err := json.Marshal(motel)
		if err != nil {
			fmt.Printf("Error marshalling entry %d: %v\n", i+1, err)
			continue
		}

		bodyBytes := motelclient.Post(apiEndpoint, jsonData)
		if bodyBytes == nil {
			fmt.Printf("No response body for entry %d\n", i+1)
			continue
		}

		var response map[string]interface{}
		if err := json.Unmarshal(bodyBytes, &response); err != nil {
			fmt.Printf("Error parsing response JSON for entry %d: %v\n", i+1, err)
			continue
		}

		if val, ok := response["motelChainId"]; ok {
			if str, isStr := val.(string); isStr {
				results = append(results, str)
			}
		}
	}

	fmt.Println("Extracted IDs:")
	for _, id := range results {
		fmt.Println(id)
	}
}
