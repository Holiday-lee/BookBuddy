/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.Chat;
import com.bookbuddy.bookbuddy.model.Message;
import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.service.ChatService;
import com.bookbuddy.bookbuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.bookbuddy.bookbuddy.model.Book;
import com.bookbuddy.bookbuddy.service.BookService;

/**
 * Controller for handling chat functionality
 * @author holiday
 */
@Controller
public class ChatController {
    
    private final ChatService chatService;
    private final UserService userService;
    private final BookService bookService;
    
    @Autowired
    public ChatController(ChatService chatService, UserService userService, BookService bookService) {
        this.chatService = chatService;
        this.userService = userService;
        this.bookService = bookService;
    }
    
    /**
     * Get current user ID from Spring Security authentication
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalArgumentException("Not authenticated");
        }
        
        String email = authentication.getName();
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        return userOpt.get().getId();
    }
    
    /**
     * WebSocket endpoint for sending messages
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/chat/{chatId}")
    public Map<String, Object> sendMessage(@Payload Map<String, Object> messageData, 
                                         SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long chatId = Long.parseLong(messageData.get("chatId").toString());
            Long senderId = getCurrentUserId();
            String content = messageData.get("content").toString();
            
            Message message = chatService.sendMessage(chatId, senderId, content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", message.getId());
            response.put("chatId", message.getChatId());
            response.put("senderId", message.getSenderId());
            response.put("content", message.getContent());
            response.put("messageType", message.getMessageType());
            response.put("createdAt", message.getCreatedAt());
            
            return response;
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }
    
    /**
     * WebSocket endpoint for joining chat
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/chat/{chatId}")
    public Map<String, Object> addUser(@Payload Map<String, Object> joinData, 
                                     SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long chatId = Long.parseLong(joinData.get("chatId").toString());
            Long userId = getCurrentUserId();
            
            // Verify user is part of the chat
            Optional<Chat> chatOpt = chatService.findById(chatId);
            if (chatOpt.isEmpty() || !chatOpt.get().involvesUser(userId)) {
                throw new IllegalArgumentException("You are not part of this chat");
            }
            
            // Add username to web socket session
            headerAccessor.getSessionAttributes().put("chatId", chatId);
            headerAccessor.getSessionAttributes().put("userId", userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("type", "JOIN");
            response.put("userId", userId);
            response.put("message", "User joined the chat");
            
            return response;
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }
    
    /**
     * WebSocket endpoint for typing indicator
     */
    @MessageMapping("/chat.typing")
    @SendTo("/topic/chat/{chatId}/typing")
    public Map<String, Object> typing(@Payload Map<String, Object> typingData, 
                                     SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long chatId = Long.parseLong(typingData.get("chatId").toString());
            Long userId = getCurrentUserId();
            
            // Verify user is part of the chat
            Optional<Chat> chatOpt = chatService.findById(chatId);
            if (chatOpt.isEmpty() || !chatOpt.get().involvesUser(userId)) {
                throw new IllegalArgumentException("You are not part of this chat");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("chatId", chatId);
            response.put("userId", userId);
            response.put("type", "TYPING");
            
            return response;
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }
    
    /**
     * WebSocket endpoint for stop typing indicator
     */
    @MessageMapping("/chat.stopTyping")
    @SendTo("/topic/chat/{chatId}/stopTyping")
    public Map<String, Object> stopTyping(@Payload Map<String, Object> typingData, 
                                         SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long chatId = Long.parseLong(typingData.get("chatId").toString());
            Long userId = getCurrentUserId();
            
            // Verify user is part of the chat
            Optional<Chat> chatOpt = chatService.findById(chatId);
            if (chatOpt.isEmpty() || !chatOpt.get().involvesUser(userId)) {
                throw new IllegalArgumentException("You are not part of this chat");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("chatId", chatId);
            response.put("userId", userId);
            response.put("type", "STOP_TYPING");
            
            return response;
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }
    
    /**
     * REST API: Get chat messages
     */
    @GetMapping("/api/chat/{chatId}/messages")
    @ResponseBody
    public ResponseEntity<?> getChatMessages(@PathVariable Long chatId) {
        try {
            Long userId = getCurrentUserId();
            
            // Verify user is part of the chat
            Optional<Chat> chatOpt = chatService.findById(chatId);
            if (chatOpt.isEmpty() || !chatOpt.get().involvesUser(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You are not part of this chat"));
            }
            
            List<Message> messages = chatService.getChatMessages(chatId);
            return ResponseEntity.ok(messages);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to load messages"));
        }
    }
    
    /**
     * REST API: Get user's active chats
     */
    @GetMapping("/api/chat/active")
    @ResponseBody
    public ResponseEntity<?> getActiveChats() {
        try {
            Long userId = getCurrentUserId();
            List<Chat> chats = chatService.findActiveChatsByUser(userId);
            return ResponseEntity.ok(chats);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to load chats"));
        }
    }
    
    /**
     * REST API: Get chat by request ID
     */
    @GetMapping("/api/chat/request/{requestId}")
    @ResponseBody
    public ResponseEntity<?> getChatByRequest(@PathVariable Long requestId) {
        try {
            Long userId = getCurrentUserId();
            
            Optional<Chat> chatOpt = chatService.findByRequestId(requestId);
            if (chatOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Chat chat = chatOpt.get();
            if (!chat.involvesUser(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You are not part of this chat"));
            }
            
            return ResponseEntity.ok(chat);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to load chat"));
        }
    }
    
    /**
     * REST API: Complete chat
     */
    @PostMapping("/api/chat/{chatId}/complete")
    @ResponseBody
    public ResponseEntity<?> completeChat(@PathVariable Long chatId) {
        try {
            Long userId = getCurrentUserId();
            Chat chat = chatService.completeChat(chatId, userId);
            return ResponseEntity.ok(chat);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to complete chat"));
        }
    }
    
    /**
     * REST API: Cancel chat
     */
    @PostMapping("/api/chat/{chatId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelChat(@PathVariable Long chatId) {
        try {
            Long userId = getCurrentUserId();
            Chat chat = chatService.cancelChat(chatId, userId);
            return ResponseEntity.ok(chat);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to cancel chat"));
        }
    }

    /**
     * REST API: Get chat by request ID with enhanced information
     */
    @GetMapping("/api/chat/request/{requestId}/details")
    @ResponseBody
    public ResponseEntity<?> getChatDetailsByRequest(@PathVariable Long requestId) {
        try {
            Long userId = getCurrentUserId();
            
            Optional<Chat> chatOpt = chatService.findByRequestId(requestId);
            if (chatOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Chat chat = chatOpt.get();
            if (!chat.involvesUser(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You are not part of this chat"));
            }
            
            // Get book information
            Optional<Book> bookOpt = bookService.findById(chat.getBookId());
            if (bookOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Book not found"));
            }
            
            Book book = bookOpt.get();
            
            // Get user information
            Optional<User> user1Opt = userService.findById(chat.getUser1Id());
            Optional<User> user2Opt = userService.findById(chat.getUser2Id());
            
            if (user1Opt.isEmpty() || user2Opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "User information not found"));
            }
            
            User user1 = user1Opt.get();
            User user2 = user2Opt.get();
            
            // Create enhanced response
            Map<String, Object> response = new HashMap<>();
            response.put("chat", chat);
            response.put("book", Map.of(
                "id", book.getId(),
                "title", book.getTitle(),
                "author", book.getAuthor(),
                "genre", book.getGenre(),
                "condition", book.getCondition(),
                "sharingType", book.getSharingType().getDisplayName()
            ));
            response.put("user1", Map.of(
                "id", user1.getId(),
                "firstName", user1.getFirstName(),
                "lastName", user1.getLastName(),
                "email", user1.getEmail()
            ));
            response.put("user2", Map.of(
                "id", user2.getId(),
                "firstName", user2.getFirstName(),
                "lastName", user2.getLastName(),
                "email", user2.getEmail()
            ));
            response.put("currentUserId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to load chat details"));
        }
    }
} 