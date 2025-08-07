package motelclient

type Address struct {
	AddressLine1 string `json:"addressLine1"`
	AddressLine2 string `json:"addressLine2"`
	Landmark     string `json:"landmark"`
	AddressName  string `json:"addressName"`
	Status       string `json:"status"`
}

type ContactInfo struct {
	PhoneNumber        string `json:"phoneNumber"`
	Email              string `json:"email"`
	ContactName        string `json:"contactName"`
	ContactPosition    string `json:"contactPosition"`
	ContactType        string `json:"contactType"`
	ContactDescription string `json:"contactDescription"`
	Status             string `json:"status"`
}

type MotelChain struct {
	MotelChainId   string      `json:"motelChainId"`
	MotelChainName string      `json:"motelChainName"`
	DisplayName    string      `json:"displayName"`
	State          string      `json:"state"`
	Pincode        string      `json:"pincode"`
	Status         string      `json:"status"`
	Address        Address     `json:"address"`
	ContactInfo    ContactInfo `json:"contactInfo"`
}

// MotelChainResponse holds only the field we want from the API response
type MotelChainResponse struct {
	MotelChainId string `json:"motelChainId"`
}

type Motel struct {
	MotelChainId   string      `json:"motelChainId"`
	MotelId        string      `json:"motelId"`
	MotelName      string      `json:"motelName"`
	MotelChainName string      `json:"motelChainName"`
	DisplayName    string      `json:"displayName"`
	State          string      `json:"state"`
	Pincode        string      `json:"pincode"`
	Status         string      `json:"status"`
	Address        Address     `json:"address"`
	ContactInfo    ContactInfo `json:"contactInfo"`
}

type MotelIDs struct {
	MotelID      string
	MotelChainID string
}

type RoomCategory struct {
	RoomCategoryName string `json:"roomCategoryName"`
	DisplayName      string `json:"displayName"`
	Name             string `json:"name"`
	Description      string `json:"description"` // Note: matches your key spelling
	Status           string `json:"status"`
}

type MotelRoomCategory struct {
	MotelID             string
	MotelChainID        string
	MotelRoomCategoryID string
}

type Room struct {
	MotelID             string `json:"motelId"`
	MotelChainID        string `json:"motelChainId"`
	MotelRoomCategoryID string `json:"motelRoomCategoryId"`
	Floor               string `json:"floor"`
	RoomNumber          string `json:"roomNumber"`
	Status              string `json:"status"`
}
