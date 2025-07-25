/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.repository;

import com.bookbuddy.bookbuddy.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
     * Find unread messages for a user in a specific chat
     */
    @Query("SELECT m FROM Message m WHERE m.chatId = :chatId AND m.senderId != :userId AND m.messageType = 'TEXT' AND m.isRead = false ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessagesByChatIdAndUserId(@Param("chatId") Long chatId, @Param("userId") Long userId);
    
    /**
     * Mark all messages in a chat as read for a specific user
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true WHERE m.chatId = :chatId AND m.senderId != :userId AND m.messageType = 'TEXT'")
    void markMessagesAsRead(@Param("chatId") Long chatId, @Param("userId") Long userId);
    
    /**
     * Find the latest message in a chat (returns only one result)
     */
    Message findFirstByChatIdOrderByCreatedAtDesc(Long chatId);
} 