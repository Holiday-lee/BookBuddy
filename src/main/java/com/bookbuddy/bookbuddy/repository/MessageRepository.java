/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.repository;

import com.bookbuddy.bookbuddy.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Message entity
 * @author holiday
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Find messages by chat ID, ordered by creation time
     */
    @Query("SELECT m FROM Message m WHERE m.chatId = :chatId ORDER BY m.createdAt ASC")
    List<Message> findByChatIdOrderByCreatedAtAsc(@Param("chatId") Long chatId);
    
    /**
     * Find messages by chat ID with limit
     */
    @Query("SELECT m FROM Message m WHERE m.chatId = :chatId ORDER BY m.createdAt DESC")
    List<Message> findTop50ByChatIdOrderByCreatedAtDesc(@Param("chatId") Long chatId);
    
    /**
     * Find messages by sender ID
     */
    List<Message> findBySenderId(Long senderId);
    
    /**
     * Find system messages by chat ID
     */
    @Query("SELECT m FROM Message m WHERE m.chatId = :chatId AND m.messageType IN ('SYSTEM', 'EXCHANGE_COMPLETED', 'EXCHANGE_CANCELLED') ORDER BY m.createdAt ASC")
    List<Message> findSystemMessagesByChatId(@Param("chatId") Long chatId);
    
    /**
     * Count messages in a chat
     */
    long countByChatId(Long chatId);
    
    /**
     * Find unread messages for a user in a specific chat
     */
    @Query("SELECT m FROM Message m WHERE m.chatId = :chatId AND m.senderId != :userId AND m.messageType = 'TEXT' ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessagesByChatIdAndUserId(@Param("chatId") Long chatId, @Param("userId") Long userId);
    
    /**
     * Find the latest message in a chat
     */
    @Query("SELECT m FROM Message m WHERE m.chatId = :chatId ORDER BY m.createdAt DESC")
    Message findTopByChatIdOrderByCreatedAtDesc(@Param("chatId") Long chatId);
} 