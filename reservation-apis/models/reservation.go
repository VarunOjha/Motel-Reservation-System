package models

import (
	"encoding/json"
	"strings"
	"time"
)

// CustomDate handles date-only strings and converts them to time.Time
type CustomDate struct {
	time.Time
}

// UnmarshalJSON handles both date strings and datetime strings
func (cd *CustomDate) UnmarshalJSON(data []byte) error {
	var dateStr string
	if err := json.Unmarshal(data, &dateStr); err != nil {
		return err
	}

	// Remove any extra quotes or whitespace
	dateStr = strings.Trim(dateStr, "\"")

	// Try different date formats
	formats := []string{
		"2006-01-02T15:04:05Z",     // RFC3339 format
		"2006-01-02T15:04:05.000Z", // RFC3339 with milliseconds
		"2006-01-02",               // Date only
		"2006-01-02 15:04:05",      // Date with time
	}

	for _, format := range formats {
		if t, err := time.Parse(format, dateStr); err == nil {
			cd.Time = t
			return nil
		}
	}

	// If all formats fail, try to parse as RFC3339
	t, err := time.Parse(time.RFC3339, dateStr)
	if err != nil {
		return err
	}
	cd.Time = t
	return nil
}

// MarshalJSON converts time.Time back to JSON
func (cd CustomDate) MarshalJSON() ([]byte, error) {
	return json.Marshal(cd.Time.Format(time.RFC3339))
}

type Reservation struct {
	MotelReservationId    string     `json:"motel_reservation_id" bson:"motel_reservation_id"`
	MotelID               string     `json:"motel_id" bson:"motel_id"`
	MotelChainID          string     `json:"motel_chain_id" bson:"motel_chain_id"`
	MotelRoomCategoryID   string     `json:"motel_room_category_id" bson:"motel_room_category_id"`
	MotelRoomCategoryName string     `json:"motel_room_category_name" bson:"motel_room_category_name"`
	TotalPrice            string     `json:"price" bson:"total_price"`
	Status                string     `json:"status" bson:"status"`
	CustomerName          string     `json:"name" bson:"customer_name"`
	CustomerEmail         string     `json:"email" bson:"customer_email"`
	CheckIn               CustomDate `json:"check_in" bson:"check_in"`
	CheckOut              CustomDate `json:"check_out" bson:"check_out"`
	CreatedAt             time.Time  `json:"created_at" bson:"created_at"`
	UpdatedAt             time.Time  `json:"updated_at" bson:"updated_at"`
	DeletedAt             time.Time  `json:"deleted_at" bson:"deleted_at"`
}
