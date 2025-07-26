package motelclient

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"
)

// PostMotelChains reads JSON file, sends POST requests to given apiEndpoint,
// extracts the values at jsonKey (like "motelChainId") from response, and returns them.

func Post(apiEndpoint string, jsonData []byte) []byte {

	client := &http.Client{Timeout: 10 * time.Second}

	req, err := http.NewRequest("POST", apiEndpoint, bytes.NewBuffer(jsonData))
	if err != nil {
		fmt.Printf("Error creating request for entry: %v\n", err)
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)
	if err != nil {
		fmt.Printf("HTTP error for entry: %v\n", err)
	}
	defer resp.Body.Close()

	bodyBytes, err := io.ReadAll(resp.Body)
	if err != nil {
		fmt.Printf("Error reading response body for entry: %v\n", err)
	}

	// Print for debug
	fmt.Printf("Response for motel:\nStatus: %s\nBody:\n%s\n\n", resp.Status, string(bodyBytes))
	return bodyBytes
}

func GetMotelChainIds(apiURL string) ([]string, error) {
	resp, err := http.Get(apiURL)
	if err != nil {
		return nil, fmt.Errorf("failed to send GET request: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("unexpected status code: %d", resp.StatusCode)
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response body: %v", err)
	}

	var motels []MotelChainMinimal
	if err := json.Unmarshal(body, &motels); err != nil {
		return nil, fmt.Errorf("failed to parse JSON response: %v", err)
	}

	var ids []string
	for _, motel := range motels {
		if motel.MotelChainId != "" {
			ids = append(ids, motel.MotelChainId)
		}
	}

	return ids, nil
}
