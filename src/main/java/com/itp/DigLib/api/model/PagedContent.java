package com.itp.DigLib.api.model;

/**
 * Represents a paginated portion of book content along with metadata about the pagination.
 * This class encapsulates both the actual content for a specific page and information
 * about the overall pagination state.
 */
public class PagedContent {
    private final String content;      // The actual text content for this page
    private final int pageNumber;      // Current page number (0-based)
    private final int totalPages;      // Total number of pages available
    private final int pageSize;        // Number of characters in this page
    private final int totalSize;       // Total number of characters in the entire book
    private final String readingTime;       // Average reading time for the entire book
    
    public PagedContent(String content, int pageNumber, int totalPages, int pageSize, int totalSize) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.totalSize = totalSize;
        this.readingTime = calculateReadingTime();
    }

    private String calculateReadingTime() {
        int words = this.totalSize / 5; // Average word length is 5 characters
        int avgReadingTime = 183; // Average reading speed in words per minute
        int totaltMinutes = words / avgReadingTime;
        int hours = totaltMinutes / 60;
        int minutes = totaltMinutes % 60;
        return String.format("%d hours, %d minutes", hours, minutes);
    }
    
    // Getters
    public String getContent() { 
        return content; 
    }
    
    public int getPageNumber() { 
        return pageNumber; 
    }
    
    public int getTotalPages() { 
        return totalPages; 
    }
    
    public int getPageSize() { 
        return pageSize; 
    }
    
    public int getTotalSize() { 
        return totalSize; 
    }

    public String getReadingTime() {
        return readingTime;
    }
}