package models

// ApiResponse represents the standard API response structure
type ApiResponse struct {
	Response ResponseData `json:"response"`
}

// ResponseData contains the HTTP code and data
type ResponseData struct {
	HTTPCode string      `json:"http_code"`
	Data     interface{} `json:"data"`
}

// NewApiResponse creates a new API response with the given HTTP code and data
func NewApiResponse(httpCode string, data interface{}) ApiResponse {
	return ApiResponse{
		Response: ResponseData{
			HTTPCode: httpCode,
			Data:     data,
		},
	}
}

// NewErrorResponse creates a new API response for errors
func NewErrorResponse(httpCode string, errorMessage string) ApiResponse {
	return ApiResponse{
		Response: ResponseData{
			HTTPCode: httpCode,
			Data:     map[string]string{"error": errorMessage},
		},
	}
}
