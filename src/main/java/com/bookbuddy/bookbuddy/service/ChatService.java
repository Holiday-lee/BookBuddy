/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.service;

import com.bookbuddy.bookbuddy.model.Chat;
import com.bookbuddy.bookbuddy.model.Message;
import com.bookbuddy.bookbuddy.model.Request;
import com.bookbuddy.bookbuddy.repository.ChatRepository;
import com.bookbuddy.bookbuddy.repository.MessageRepository;
import com.bookbuddy.bookbuddy.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for handling chat conversations
 * @author holiday
 */
@Service
@Transactional
public class ChatService {
    
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final RequestRepository requestRepository;
    
    @Autowired
    public ChatService(ChatRepository chatRepository, 
                      MessageRepository messageRepository,
                      RequestRepository requestRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.requestRepository = requestRepository;
    }
    
    /**
     * Create a chat for an accepted request
     */
    public Chat createChatForRequest(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (!request.isAccepted()) {
            throw new IllegalArgumentException("Cannot create chat for non-accepted request");
        }
        
        // Check if chat already exists for this request
        if (chatRepository.existsByRequestId(requestId)) {
            throw new IllegalArgumentException("Chat already exists for this request");
        }
        
        Chat chat = new Chat(request.getBookId(), requestId, request.getRequesterId(), request.getOwnerId());
        chat = chatRepository.save(chat);
        
        // Add initial system message
        String systemMessage = getInitialSystemMessage(request);
        Message message = new Message(chat.getId(), systemMessage, Message.MessageType.SYSTEM);
        messageRepository.save(message);
        
        return chat;
    }
    
    /**
     * Send a message in a chat
     */
    public Message sendMessage(Long chatId, Long senderId, String content) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
        
        if (!chat.isActive()) {
            throw new IllegalArgumentException("Chat is not active");
        }
        
        if (!chat.involvesUser(senderId)) {
            throw new IllegalArgumentException("You are not part of this chat");
        }
        
        Message message = new Message(chatId, senderId, content);
        return messageRepository.save(message);
    }
    
    /**
     * Get messages for a chat
     */
    @Transactional(readOnly = true)
    public List<Message> getChatMessages(Long chatId) {
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
    }
    
    /**
     * Get recent messages for a chat (for performance)
     */
    @Transactional(readOnly = true)
    public List<Message> getRecentChatMessages(Long chatId) {
        return messageRepository.findTop50ByChatIdOrderByCreatedAtDesc(chatId);
    }
    
    /**
     * Complete a chat (when exchange is completed)
     */
    public Chat completeChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
        
        if (!chat.involvesUser(userId)) {
            throw new IllegalArgumentException("You are not part of this chat");
        }
        
        if (!chat.isActive()) {
            throw new IllegalArgumentException("Chat is not active");
        }
        
        chat.setStatus(Chat.ChatStatus.COMPLETED);
        chat = chatRepository.save(chat);
        
        // Add completion message
        Message message = new Message(chatId, "Exchange completed successfully!", Message.MessageType.EXCHANGE_COMPLETED);
        messageRepository.save(message);
        
        return chat;
    }
    
    /**
     * Cancel a chat (when exchange is cancelled)
     */
    public Chat cancelChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));
        
        if (!chat.involvesUser(userId)) {
            throw new IllegalArgumentException("You are not part of this chat");
        }
        
        if (!chat.isActive()) {
            throw new IllegalArgumentException("Chat is not active");
        }
        
        chat.setStatus(Chat.ChatStatus.CANCELLED);
        chat = chatRepository.save(chat);
        
        // Add cancellation message
        Message message = new Message(chatId, "Exchange was cancelled.", Message.MessageType.EXCHANGE_CANCELLED);
        messageRepository.save(message);
        
        return chat;
    }
    
    /**
     * Find chat by ID
     */
    @Transactional(readOnly = true)
    public Optional<Chat> findById(Long id) {
        return chatRepository.findById(id);
    }
    
    /**
     * Find chat by request ID
     */
    @Transactional(readOnly = true)
    public Optional<Chat> findByRequestId(Long requestId) {
        return chatRepository.findByRequestId(requestId);
    }
    
    /**
     * Find active chats for a user
     */
    @Transactional(readOnly = true)
    public List<Chat> findActiveChatsByUser(Long userId) {
        return chatRepository.findActiveChatsByUserId(userId);
    }
    
    /**
     * Find all chats for a user
     */
    @Transactional(readOnly = true)
    public List<Chat> findAllChatsByUser(Long userId) {
        return chatRepository.findAllChatsByUserId(userId);
    }
    
    /**
     * Find chat between two users for a specific book
     */
    @Transactional(readOnly = true)
    public Optional<Chat> findChatByBookAndUsers(Long bookId, Long user1Id, Long user2Id) {
        return chatRepository.findChatByBookAndUsers(bookId, user1Id, user2Id);
    }
    
    /**
     * Find completed chats for a user
     */
    @Transactional(readOnly = true)
    public List<Chat> findCompletedChatsByUser(Long userId) {
        return chatRepository.findCompletedChatsByUserId(userId);
    }
    
    /**
     * Get unread message count for a user in a chat
     */
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(Long chatId, Long userId) {
        return messageRepository.findUnreadMessagesByChatIdAndUserId(chatId, userId).size();
    }
    
    /**
     * Get the latest message in a chat
     */
    @Transactional(readOnly = true)
    public Message getLatestMessage(Long chatId) {
        return messageRepository.findTopByChatIdOrderByCreatedAtDesc(chatId);
    }
    
    /**
     * Generate initial system message based on request type
     */
    private String getInitialSystemMessage(Request request) {
        switch (request.getRequestType()) {
            case GIVE_AWAY:
                return "Give away request accepted! You can now arrange the pickup details.";
            case LEND:
                return "Lending request accepted! You can now arrange the pickup and return details.";
            case SWAP:
                return "Swap request accepted! You can now arrange the book exchange details.";
            default:
                return "Request accepted! You can now arrange the details.";
        }
    }
} 