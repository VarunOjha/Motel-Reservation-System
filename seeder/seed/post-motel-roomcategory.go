package seed

import (
	"encoding/json"
	"fmt"
	"motels/motelclient"
	"os"
)

func AddRoomCategoryToMotel(apiBaseUrl string, motelIDs []motelclient.MotelIDs) []motelclient.MotelRoomCategory {
	roomCategorySeedJsonPath := "resources/motel-room-category.json"
	var motelRoomCategoryResult []motelclient.MotelRoomCategory

	roomCategoryJson, err := os.ReadFile(roomCategorySeedJsonPath)
	if err != nil {
		fmt.Printf("error reading file: %v\n", err)
		return nil
	}

	var motelRoomCategory []motelclient.RoomCategory
	err = json.Unmarshal(roomCategoryJson, &motelRoomCategory)
	if err != nil {
		fmt.Printf("error parsing JSON: %v\n", err)
		return nil
	}

	for _, motels := range motelIDs {
		motelChainId := motels.MotelChainID
		motelId := motels.MotelID
		postRoomCategoryApi := apiBaseUrl + "/motels/chains/" + motelChainId + "/motels/" + motelId + "/rooms/categories"
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

			var response map[string]interface{}
			if err := json.Unmarshal(bodyBytes, &response); err != nil {
				fmt.Printf("Error parsing response JSON for motel ID %s: %v\n", motelId, err)
				continue
			}

			if val, ok := response["motelRoomCategoryId"]; ok {
				if roomCategoryId, isStr := val.(string); isStr {
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
