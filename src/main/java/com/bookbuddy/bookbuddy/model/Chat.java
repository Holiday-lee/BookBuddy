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
 * Chat entity for BookBuddy application
 * Represents chat conversations between users for book exchanges
 * @author holiday
 */
@Entity
@Table(name = "chats")
public class Chat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // The book involved in the exchange
    @Column(name = "book_id", nullable = false)
    private Long bookId;
    
    // The request that initiated this chat
    @Column(name = "request_id", nullable = false)
    private Long requestId;
    
    // The two users in the chat
    @Column(name = "user1_id", nullable = false)
    private Long user1Id;
    
    @Column(name = "user2_id", nullable = false)
    private Long user2Id;
    
    // Status of the chat
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChatStatus status = ChatStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enum for chat status
    public enum ChatStatus {
        ACTIVE("Active"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        ChatStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Default constructor
    public Chat() {
        this.status = ChatStatus.ACTIVE;
    }
    
    // Constructor with required fields
    public Chat(Long bookId, Long requestId, Long user1Id, Long user2Id) {
        this();
        this.bookId = bookId;
        this.requestId = requestId;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
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
    
    public Long getRequestId() {
        return requestId;
    }
    
    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
    
    public Long getUser1Id() {
        return user1Id;
    }
    
    public void setUser1Id(Long user1Id) {
        this.user1Id = user1Id;
    }
    
    public Long getUser2Id() {
        return user2Id;
    }
    
    public void setUser2Id(Long user2Id) {
        this.user2Id = user2Id;
    }
    
    public ChatStatus getStatus() {
        return status;
    }
    
    public void setStatus(ChatStatus status) {
        this.status = status;
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
    
    // Utility methods
    public boolean isActive() {
        return status == ChatStatus.ACTIVE;
    }
    
    public boolean isCompleted() {
        return status == ChatStatus.COMPLETED;
    }
    
    public boolean isCancelled() {
        return status == ChatStatus.CANCELLED;
    }
    
    /**
     * Check if a user is part of this chat
     */
    public boolean involvesUser(Long userId) {
        return user1Id.equals(userId) || user2Id.equals(userId);
    }
    
    /**
     * Get the other user in the chat
     */
    public Long getOtherUserId(Long currentUserId) {
        if (user1Id.equals(currentUserId)) {
            return user2Id;
        } else if (user2Id.equals(currentUserId)) {
            return user1Id;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", requestId=" + requestId +
                ", user1Id=" + user1Id +
                ", user2Id=" + user2Id +
                ", status=" + status +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Chat chat = (Chat) o;
        return id != null ? id.equals(chat.id) : chat.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 