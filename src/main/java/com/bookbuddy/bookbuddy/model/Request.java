/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Request entity for BookBuddy application
 * Represents requests for books (give away, lend, swap)
 * @author holiday
 */
@Entity
@Table(name = "requests")
public class Request {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // The book being requested
    @Column(name = "book_id", nullable = false)
    private Long bookId;
    
    // The user making the request
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;
    
    // The owner of the book
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    // Type of request
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestType requestType;
    
    // Status of the request
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
    
    // Optional message from requester
    @Column(name = "message", length = 1000)
    private String message;
    
    // For swap requests: the book being offered in exchange
    @Column(name = "offered_book_id")
    private Long offeredBookId;
    
    // For lending requests: requested duration (may differ from book's max duration)
    @Column(name = "requested_duration_days")
    private Integer requestedDurationDays;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enums for request types and status
    public enum RequestType {
        GIVE_AWAY("Give Away Request"),
        LEND("Lending Request"),
        SWAP("Swap Request");
        
        private final String displayName;
        
        RequestType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum RequestStatus {
        PENDING("Pending"),
        ACCEPTED("Accepted"),
        REJECTED("Rejected"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        RequestStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Default constructor
    public Request() {
        this.status = RequestStatus.PENDING;
    }
    
    // Constructor with required fields
    public Request(Long bookId, Long requesterId, Long ownerId, RequestType requestType) {
        this();
        this.bookId = bookId;
        this.requesterId = requesterId;
        this.ownerId = ownerId;
        this.requestType = requestType;
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
    
    public RequestType getRequestType() {
        return requestType;
    }
    
    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }
    
    public RequestStatus getStatus() {
        return status;
    }
    
    public void setStatus(RequestStatus status) {
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
    
    /**
     * Get book information for display purposes
     * This method should be used with a BookService to fetch the actual book details
     */
    public String getBookDisplayInfo() {
        return "Book ID: " + bookId;
    }
    
    // Utility methods
    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }
    
    public boolean isAccepted() {
        return status == RequestStatus.ACCEPTED;
    }
    
    public boolean isRejected() {
        return status == RequestStatus.REJECTED;
    }
    
    public boolean isCompleted() {
        return status == RequestStatus.COMPLETED;
    }
    
    public boolean isCancelled() {
        return status == RequestStatus.CANCELLED;
    }
    
    public boolean isGiveAwayRequest() {
        return requestType == RequestType.GIVE_AWAY;
    }
    
    public boolean isLendRequest() {
        return requestType == RequestType.LEND;
    }
    
    public boolean isSwapRequest() {
        return requestType == RequestType.SWAP;
    }
    
    @Override
    public String toString() {
        return "Request{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", requesterId=" + requesterId +
                ", ownerId=" + ownerId +
                ", requestType=" + requestType +
                ", status=" + status +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Request request = (Request) o;
        return id != null ? id.equals(request.id) : request.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 