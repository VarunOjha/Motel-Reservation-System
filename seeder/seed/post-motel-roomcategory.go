package seed

import (
	"encoding/json"
	"fmt"
	"motels/motelclient"
	"os"
)

func AddRoomCategoryToMotel(motelIDs []motelclient.MotelIDs) []motelclient.MotelRoomCategory {
	baseUrl := "http://localhost:8080/api/motels/chains"
	roomCategorySeedJsonPath := "resources/motel-room-category.json"
	var motelRoomCategoryResult []motelclient.MotelRoomCategory

	roomCategoryJson, err := os.ReadFile(roomCategorySeedJsonPath)
	if err != nil {
		fmt.Printf("error reading file: %v\n", err)
	}

	var motelRoomCategory []motelclient.RoomCategory
	err = json.Unmarshal(roomCategoryJson, &motelRoomCategory)
	if err != nil {
		fmt.Printf("error parsing JSON: %v\n", err)
	}

	for _, motels := range motelIDs {
		motelChainId := motels.MotelChainID
		motelId := motels.MotelID
		postRoomCategoryApi := baseUrl + "/" + motelChainId + "/motels/" + motelId + "/rooms/categories"
		for _, roomCategory := range motelRoomCategory {
			jsonData, err := json.Marshal(roomCategory)
			if err != nil {
				fmt.Printf("Error marshalling room category: %v\n", err)
				continue
			}

			bodyBytes := motelclient.Post(postRoomCategoryApi, jsonData)
			if bodyBytes == nil {
				fmt.Printf("No response body for motel ID: %s\n", motelId)
				continue
			}

			fmt.Printf("Response for motel ID %s:\n%s\n", motelId, string(bodyBytes))

			var response map[string]interface{}
			if err := json.Unmarshal(bodyBytes, &response); err != nil {
				fmt.Printf("Error parsing response JSON for motel ID %s: %v\n", motelId, err)
				continue
			}

			fmt.Printf("motelRoomCategoryId Response")

			if val, ok := response["motelRoomCategoryId"]; ok {
				if roomCategoryId, isStr := val.(string); isStr {
					fmt.Printf("Room Category ID: %s for Motel ID: %s\n", roomCategoryId, motelId)
					var motelRoomCategoryResultItem motelclient.MotelRoomCategory
					motelRoomCategoryResultItem.MotelChainID = motelChainId
					motelRoomCategoryResultItem.MotelID = motelId
					motelRoomCategoryResultItem.MotelRoomCategoryID = roomCategoryId
					motelRoomCategoryResult = append(motelRoomCategoryResult, motelRoomCategoryResultItem)
				}
			}
		}
	}
	return motelRoomCategoryResult
}
