package com.bookbuddy.bookbuddy.model;

import java.time.LocalDateTime;

/**
 * DTO for Request with Book information
 * Used to display request details with book information
 */
public class RequestWithBookInfo {
    
    private Long id;
    private Long bookId;
    private Long requesterId;
    private Long ownerId;
    private Request.RequestType requestType;
    private Request.RequestStatus status;
    private String message;
    private Long offeredBookId;
    private Integer requestedDurationDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Book information
    private String bookTitle;
    private String bookAuthor;
    private String bookGenre;
    private String bookCondition;
    private Book.SharingType bookSharingType;
    private Book.BookStatus bookStatus;
    private String bookDescription;
    
    // Owner information
    private String ownerName;
    private String requesterName;
    
    public RequestWithBookInfo() {}
    
    public RequestWithBookInfo(Request request, Book book, String ownerName, String requesterName) {
        this.id = request.getId();
        this.bookId = request.getBookId();
        this.requesterId = request.getRequesterId();
        this.ownerId = request.getOwnerId();
        this.requestType = request.getRequestType();
        this.status = request.getStatus();
        this.message = request.getMessage();
        this.offeredBookId = request.getOfferedBookId();
        this.requestedDurationDays = request.getRequestedDurationDays();
        this.createdAt = request.getCreatedAt();
        this.updatedAt = request.getUpdatedAt();
        
        // Book information
        if (book != null) {
            this.bookTitle = book.getTitle();
            this.bookAuthor = book.getAuthor();
            this.bookGenre = book.getGenre();
            this.bookCondition = book.getCondition();
            this.bookSharingType = book.getSharingType();
            this.bookStatus = book.getStatus();
            this.bookDescription = book.getDescription();
        }
        
        this.ownerName = ownerName;
        this.requesterName = requesterName;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
    public Long getRequesterId() {
        return requesterId;
    }
    
    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }
    
    public Long getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
    
    public Request.RequestType getRequestType() {
        return requestType;
    }
    
    public void setRequestType(Request.RequestType requestType) {
        this.requestType = requestType;
    }
    
    public Request.RequestStatus getStatus() {
        return status;
    }
    
    public void setStatus(Request.RequestStatus status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Long getOfferedBookId() {
        return offeredBookId;
    }
    
    public void setOfferedBookId(Long offeredBookId) {
        this.offeredBookId = offeredBookId;
    }
    
    public Integer getRequestedDurationDays() {
        return requestedDurationDays;
    }
    
    public void setRequestedDurationDays(Integer requestedDurationDays) {
        this.requestedDurationDays = requestedDurationDays;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public String getBookAuthor() {
        return bookAuthor;
    }
    
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }
    
    public String getBookGenre() {
        return bookGenre;
    }
    
    public void setBookGenre(String bookGenre) {
        this.bookGenre = bookGenre;
    }
    
    public String getBookCondition() {
        return bookCondition;
    }
    
    public void setBookCondition(String bookCondition) {
        this.bookCondition = bookCondition;
    }
    
    public Book.SharingType getBookSharingType() {
        return bookSharingType;
    }
    
    public void setBookSharingType(Book.SharingType bookSharingType) {
        this.bookSharingType = bookSharingType;
    }
    
    public Book.BookStatus getBookStatus() {
        return bookStatus;
    }
    
    public void setBookStatus(Book.BookStatus bookStatus) {
        this.bookStatus = bookStatus;
    }
    
    public String getBookDescription() {
        return bookDescription;
    }
    
    public void setBookDescription(String bookDescription) {
        this.bookDescription = bookDescription;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public String getRequesterName() {
        return requesterName;
    }
    
    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }
} 