package models

import "time"

type Reservation struct {
	MotelReservationId    string    `json:"motel_reservation_id" bson:"motel_reservation_id"`
	MotelID               string    `json:"motel_id" bson:"motel_id"`
	MotelChainID          string    `json:"motel_chain_id" bson:"motel_chain_id"`
	MotelRoomCategoryID   string    `json:"motel_room_category_id" bson:"motel_room_category_id"`
	MotelRoomCategoryName string    `json:"motel_room_category_name" bson:"motel_room_category_name"`
	TotalPrice            float64   `json:"price" bson:"total_price"`
	Status                string    `json:"status" bson:"status"`
	CustomerName          string    `json:"name" bson:"customer_name"`
	CustomerEmail         string    `json:"email" bson:"customer_email"`
	CheckIn               time.Time `json:"check_in" bson:"check_in"`
	CheckOut              time.Time `json:"check_out" bson:"check_out"`
	CreatedAt             time.Time `json:"created_at" bson:"created_at"`
	UpdatedAt             time.Time `json:"updated_at" bson:"updated_at"`
	DeletedAt             time.Time `json:"deleted_at" bson:"deleted_at"`
}
