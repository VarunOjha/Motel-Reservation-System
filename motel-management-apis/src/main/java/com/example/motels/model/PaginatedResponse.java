package com.example.motels.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaginatedResponse<T> {
    
    @JsonProperty("content")
    private List<T> content;
    
    @JsonProperty("pagination")
    private PaginationInfo pagination;
    
    public PaginatedResponse() {}
    
    public PaginatedResponse(List<T> content, PaginationInfo pagination) {
        this.content = content;
        this.pagination = pagination;
    }
    
    public List<T> getContent() {
        return content;
    }
    
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public PaginationInfo getPagination() {
        return pagination;
    }
    
    public void setPagination(PaginationInfo pagination) {
        this.pagination = pagination;
    }
    
    public static class PaginationInfo {
        @JsonProperty("page")
        private int page;
        
        @JsonProperty("size")
        private int size;
        
        @JsonProperty("total_elements")
        private long totalElements;
        
        @JsonProperty("total_pages")
        private int totalPages;
        
        @JsonProperty("is_first")
        private boolean isFirst;
        
        @JsonProperty("is_last")
        private boolean isLast;
        
        public PaginationInfo() {}
        
        public PaginationInfo(int page, int size, long totalElements, int totalPages, boolean isFirst, boolean isLast) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.isFirst = isFirst;
            this.isLast = isLast;
        }
        
        // Getters and setters
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
        
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        
        public boolean isFirst() { return isFirst; }
        public void setFirst(boolean isFirst) { this.isFirst = isFirst; }
        
        public boolean isLast() { return isLast; }
        public void setLast(boolean isLast) { this.isLast = isLast; }
    }
}
