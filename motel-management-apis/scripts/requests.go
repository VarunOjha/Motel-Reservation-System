package scripts

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"time"
)

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

// PostMotelChains reads JSON file, sends POST requests to given apiEndpoint,
// extracts the values at jsonKey (like "motelChainId") from response, and returns them.
func PostMotelChains(apiEndpoint string, jsonPath string, jsonKey string) ([]string, error) {
	// Read JSON file
	jsonFile, err := os.ReadFile(jsonPath)
	if err != nil {
		return nil, fmt.Errorf("error reading file: %v", err)
	}

	var motels []MotelChain
	err = json.Unmarshal(jsonFile, &motels)
	if err != nil {
		return nil, fmt.Errorf("error parsing JSON: %v", err)
	}

	client := &http.Client{Timeout: 10 * time.Second}
	var results []string

	for i, motel := range motels {
		jsonData, err := json.Marshal(motel)
		if err != nil {
			fmt.Printf("Error marshalling entry %d: %v\n", i+1, err)
			continue
		}

		req, err := http.NewRequest("POST", apiEndpoint, bytes.NewBuffer(jsonData))
		if err != nil {
			fmt.Printf("Error creating request for entry %d: %v\n", i+1, err)
			continue
		}
		req.Header.Set("Content-Type", "application/json")

		resp, err := client.Do(req)
		if err != nil {
			fmt.Printf("HTTP error for entry %d: %v\n", i+1, err)
			continue
		}
		defer resp.Body.Close()

		bodyBytes, err := io.ReadAll(resp.Body)
		if err != nil {
			fmt.Printf("Error reading response body for entry %d: %v\n", i+1, err)
			continue
		}

		// Print for debug
		fmt.Printf("Response for motel %d [%s]:\nStatus: %s\nBody:\n%s\n\n",
			i+1, motel.MotelChainName, resp.Status, string(bodyBytes))

		var response map[string]interface{}
		if err := json.Unmarshal(bodyBytes, &response); err != nil {
			fmt.Printf("Error parsing response JSON for entry %d: %v\n", i+1, err)
			continue
		}

		if val, ok := response[jsonKey]; ok {
			if str, isStr := val.(string); isStr {
				results = append(results, str)
			}
		}
	}

	return results, nil
}
