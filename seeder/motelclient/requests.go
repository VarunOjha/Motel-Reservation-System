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
		return nil
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)
	if err != nil {
		fmt.Printf("HTTP error for entry: %v\n", err)
		return nil
	}
	defer resp.Body.Close()

	// Check HTTP status code
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		fmt.Printf("HTTP error: %d %s for endpoint: %s\n", resp.StatusCode, resp.Status, apiEndpoint)
		return nil
	}

	bodyBytes, err := io.ReadAll(resp.Body)
	if err != nil {
		fmt.Printf("Error reading response body for entry: %v\n", err)
		return nil
	}

	return bodyBytes
}

func Get(url string) []byte {
	resp, err := http.Get(url)
	if err != nil {
		fmt.Printf("failed to send GET request: %v\n", err)
		return nil
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		fmt.Printf("unexpected status code: %d\n", resp.StatusCode)
		return nil
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		fmt.Printf("failed to read GET response body: %v\n", err)
		return nil
	}
	return body
}
