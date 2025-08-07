package seed

import (
	"encoding/json"
	"fmt"
	"motels/motelclient"
	"strconv"
)

func AddRoomsToMotel(apiBaseUrl string, motelRoomCategoriesMapping []motelclient.MotelRoomCategory) {
	for _, motelRoomCategories := range motelRoomCategoriesMapping {
		motelChainId := motelRoomCategories.MotelChainID
		motelId := motelRoomCategories.MotelID
		motelRoomCategoriesId := motelRoomCategories.MotelRoomCategoryID

		postRoomCategoryApi := apiBaseUrl + "/motels/chains/" + motelChainId + "/motels/" + motelId + "/rooms"

		for i := 1; i <= 25; i++ { // Loop to run 24 times
			var room motelclient.Room
			room.MotelChainID = motelChainId
			room.MotelID = motelId
			room.MotelRoomCategoryID = motelRoomCategoriesId
			room.Floor = strconv.Itoa(i % 5)          // Calculate floor based on room number
			room.RoomNumber = strconv.Itoa(i*100 + i) // Assign room number
			room.Status = "Active"

			roomJson, err := json.Marshal(room)
			if err != nil {
				fmt.Println("Error marshaling room:", err)
				continue
			}

			bodyBytes := motelclient.Post(postRoomCategoryApi, roomJson)
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

			if val, ok := response["roomId"]; ok {
				if roomId, isStr := val.(string); isStr {
					fmt.Printf("Room Category ID: %s for Motel ID: %s\n", roomId, motelId)
				}
			}
		}
	}
}
