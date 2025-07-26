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

// MotelChainMinimal holds only the field we want from the API response
type MotelChainMinimal struct {
	MotelChainId string `json:"motelChainId"`
}
