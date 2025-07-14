/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Message entity for BookBuddy application
 * Represents individual messages in chat conversations
 * @author holiday
 */
@Entity
@Table(name = "messages")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // The chat this message belongs to
    @Column(name = "chat_id", nullable = false)
    private Long chatId;
    
    // The user who sent the message
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    
    // The message content
    @Column(name = "content", nullable = false, length = 2000)
    private String content;
    
    // Message type (text, system message, etc.)
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType = MessageType.TEXT;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Enum for message types
    public enum MessageType {
        TEXT("Text"),
        SYSTEM("System Message"),
        EXCHANGE_COMPLETED("Exchange Completed"),
        EXCHANGE_CANCELLED("Exchange Cancelled");
        
        private final String displayName;
        
        MessageType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Default constructor
    public Message() {
        this.messageType = MessageType.TEXT;
    }
    
    // Constructor with required fields
    public Message(Long chatId, Long senderId, String content) {
        this();
        this.chatId = chatId;
        this.senderId = senderId;
        this.content = content;
    }
    
    // Constructor for system messages
    public Message(Long chatId, String content, MessageType messageType) {
        this();
        this.chatId = chatId;
        this.senderId = null; // System messages don't have a sender
        this.content = content;
        this.messageType = messageType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getChatId() {
        return chatId;
    }
    
    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
    
    public Long getSenderId() {
        return senderId;
    }
    
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Utility methods
    public boolean isSystemMessage() {
        return messageType == MessageType.SYSTEM || 
               messageType == MessageType.EXCHANGE_COMPLETED || 
               messageType == MessageType.EXCHANGE_CANCELLED;
    }
    
    public boolean isTextMessage() {
        return messageType == MessageType.TEXT;
    }
    
    public boolean isExchangeCompleted() {
        return messageType == MessageType.EXCHANGE_COMPLETED;
    }
    
    public boolean isExchangeCancelled() {
        return messageType == MessageType.EXCHANGE_CANCELLED;
    }
    
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", senderId=" + senderId +
                ", content='" + content + '\'' +
                ", messageType=" + messageType +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Message message = (Message) o;
        return id != null ? id.equals(message.id) : message.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 