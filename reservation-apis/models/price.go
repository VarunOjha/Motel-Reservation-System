package models

import "time"

type MotelRoomPrice struct {
	MotelID               string    `json:"motel_id" bson:"motel_id"`
	MotelChainID          string    `json:"motel_chain_id" bson:"motel_chain_id"`
	MotelRoomCategoryID   string    `json:"motel_room_category_id" bson:"motel_room_category_id"`
	MotelRoomCategoryName string    `json:"room_type" bson:"motel_room_category_name"`
	AvailableRoomCount    string    `json:"room_number" bson:"available_room_count"`
	Date                  string    `json:"date" bson:"date"`
	Status                string    `json:"status" bson:"status"`
	Price                 float64   `json:"price" bson:"price"`
	BookedRoomCount       int       `json:"booked_room_count" bson:"booked_room_count"`
	Version               int       `json:"version" bson:"version"` // Version field for tracking document version
	CreatedAt             time.Time `json:"created_at" bson:"created_at"`
	UpdatedAt             time.Time `json:"updated_at" bson:"updated_at"`
	DeletedAt             time.Time `json:"deleted_at" bson:"deleted_at"`
}
