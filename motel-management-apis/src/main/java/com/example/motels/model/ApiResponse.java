package com.example.motels.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiResponse<T> {
    
    @JsonProperty("response")
    private ResponseWrapper<T> response;
    
    public ApiResponse() {}
    
    public ApiResponse(String httpCode, T data) {
        this.response = new ResponseWrapper<>(httpCode, data);
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
        
        public ResponseWrapper() {}
        
        public ResponseWrapper(String httpCode, T data) {
            this.httpCode = httpCode;
            this.data = data;
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
    }
}
