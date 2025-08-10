package com.example.motels.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiResponse<T> {
    
    @JsonProperty("response")
    private ResponseWrapper<T> response;
    
    public ApiResponse() {}
    
    public ApiResponse(String httpCode, T data) {
        this.response = new ResponseWrapper<>(httpCode, data, "");
    }
    
    public ApiResponse(String httpCode, T data, String message) {
        this.response = new ResponseWrapper<>(httpCode, data, message);
    }
    
    public ResponseWrapper<T> getResponse() {
        return response;
    }
    
    public void setResponse(ResponseWrapper<T> response) {
        this.response = response;
    }
    
    public static class ResponseWrapper<T> {
        @JsonProperty("http_code")
        private String httpCode;
        
        @JsonProperty("data")
        private T data;
        
        @JsonProperty("message")
        private String message;
        
        public ResponseWrapper() {}
        
        public ResponseWrapper(String httpCode, T data, String message) {
            this.httpCode = httpCode;
            this.data = data;
            this.message = message != null ? message : "";
        }
        
        public String getHttpCode() {
            return httpCode;
        }
        
        public void setHttpCode(String httpCode) {
            this.httpCode = httpCode;
        }
        
        public T getData() {
            return data;
        }
        
        public void setData(T data) {
            this.data = data;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message != null ? message : "";
        }
    }
}
