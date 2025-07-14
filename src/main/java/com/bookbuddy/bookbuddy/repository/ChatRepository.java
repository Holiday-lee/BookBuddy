/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.repository;

import com.bookbuddy.bookbuddy.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Chat entity
 * @author holiday
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    /**
     * Find chats by book ID
     */
    List<Chat> findByBookId(Long bookId);
    
    /**
     * Find chats by request ID
     */
    Optional<Chat> findByRequestId(Long requestId);
    
    /**
     * Find active chats for a user
     */
    @Query("SELECT c FROM Chat c WHERE (c.user1Id = :userId OR c.user2Id = :userId) AND c.status = 'ACTIVE'")
    List<Chat> findActiveChatsByUserId(@Param("userId") Long userId);
    
    /**
     * Find all chats for a user (active and completed)
     */
    @Query("SELECT c FROM Chat c WHERE c.user1Id = :userId OR c.user2Id = :userId")
    List<Chat> findAllChatsByUserId(@Param("userId") Long userId);
    
    /**
     * Find chat between two users for a specific book
     */
    @Query("SELECT c FROM Chat c WHERE c.bookId = :bookId AND " +
           "((c.user1Id = :user1Id AND c.user2Id = :user2Id) OR " +
           "(c.user1Id = :user2Id AND c.user2Id = :user1Id))")
    Optional<Chat> findChatByBookAndUsers(@Param("bookId") Long bookId, 
                                         @Param("user1Id") Long user1Id, 
                                         @Param("user2Id") Long user2Id);
    
    /**
     * Find chats by status
     */
    List<Chat> findByStatus(Chat.ChatStatus status);
    
    /**
     * Check if a chat exists for a request
     */
    boolean existsByRequestId(Long requestId);
    
    /**
     * Find completed chats for a user
     */
    @Query("SELECT c FROM Chat c WHERE (c.user1Id = :userId OR c.user2Id = :userId) AND c.status = 'COMPLETED'")
    List<Chat> findCompletedChatsByUserId(@Param("userId") Long userId);
} 