package seed

import (
	"encoding/json"
	"fmt"
	"motels/motelclient"
	"os"
)

func AddMotelsToMotelChains() []motelclient.MotelIDs {
	getMotelChainsApi := "http://localhost:8080/api/motels/chains"
	motelSeedJsonPath := "resources/motel.json"

	var motelMappings []motelclient.MotelIDs

	jsonFile, err := os.ReadFile(motelSeedJsonPath)
	if err != nil {
		fmt.Printf("error reading file: %v\n", err)
		return nil
	}

	var motels []motelclient.Motel
	err = json.Unmarshal(jsonFile, &motels)
	if err != nil {
		fmt.Printf("error parsing JSON: %v\n", err)
	}

	body := motelclient.Get(getMotelChainsApi)
	var motelChains []motelclient.MotelChain
	if err := json.Unmarshal(body, &motelChains); err != nil {
		fmt.Printf("failed to parse JSON response: %v\n", err)
	}

	var motelChainIds []string
	for _, motelChains := range motelChains {
		if motelChains.MotelChainId != "" {
			// fmt.Printf("IDs: %s\n", ids)
			motelChainIds = append(motelChainIds, motelChains.MotelChainId)
		}
	}

	fmt.Println("Adding motels to motel chains")
	count := 0
	for _, motelChainId := range motelChainIds {
		postMotelApi := getMotelChainsApi + "/" + motelChainId + "/motels"

		motel := motels[count]
		count++
		jsonData, err := json.Marshal(motel)
		if err != nil {
			fmt.Printf("Error marshalling entry %d: %v\n", count, err)
			continue
		}

		bodyBytes := motelclient.Post(postMotelApi, jsonData)
		if bodyBytes == nil {
			fmt.Printf("No response body for entry %d\n", count)
			continue
		}

		var response map[string]interface{}
		if err := json.Unmarshal(bodyBytes, &response); err != nil {
			fmt.Printf("Error parsing response JSON for entry %d: %v\n", count, err)
			continue
		}

		if val, ok := response["motelId"]; ok {
			if motelId, isStr := val.(string); isStr {
				fmt.Printf("Motel ID: %s MotelChainId: %s\n", motelId, motelChainId) // Example usage of results
				var motelMapping motelclient.MotelIDs
				motelMapping.MotelID = motelId
				motelMapping.MotelChainID = motelChainId
				motelMappings = append(motelMappings, motelMapping)
			}
		}
	}
	return motelMappings
}
