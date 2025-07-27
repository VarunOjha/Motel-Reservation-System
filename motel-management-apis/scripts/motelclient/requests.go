package motelclient

import (
	"bytes"
	"fmt"
	"io"
	"net/http"
	"time"
)

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
	return bodyBytes
}

func Get(url string) []byte {
	resp, err := http.Get(url)
	if err != nil {
		fmt.Printf("failed to send GET request: %v\n", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		fmt.Printf("unexpected status code: %d\n", resp.StatusCode)
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		fmt.Printf("failed to read response body: %v\n", err)
	}
	fmt.Printf("Response for motel:\nStatus: %s\nBody:\n%s\n\n", resp.Status, string(body))
	return body
}
