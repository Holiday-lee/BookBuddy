/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.Chat;
import com.bookbuddy.bookbuddy.model.Message;
import com.bookbuddy.bookbuddy.model.Book;
import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.service.ChatService;
import com.bookbuddy.bookbuddy.service.UserService;
import com.bookbuddy.bookbuddy.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller for handling chat functionality
 * @author holiday
 */
@Controller
public class ChatController {
    
    private final ChatService chatService;
    private final UserService userService;
    private final BookService bookService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public ChatController(ChatService chatService, UserService userService, BookService bookService, 
                        SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.userService = userService;
        this.bookService = bookService;
        this.messagingTemplate = messagingTemplate;
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
    public void sendMessage(@Payload Map<String, Object> messageData, 
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
            
            // Send to specific chat topic
            messagingTemplate.convertAndSend("/topic/chat/" + chatId, response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            // Send error to user's personal topic
            messagingTemplate.convertAndSendToUser(
                headerAccessor.getSessionId(), 
                "/topic/errors", 
                error
            );
        }
    }
    
    /**
     * WebSocket endpoint for joining chat
     */
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload Map<String, Object> joinData, 
                                     SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long chatId = Long.parseLong(joinData.get("chatId").toString());
            Long userId = getCurrentUserId();
            
            // Verify user is part of the chat
            Optional<Chat> chatOpt = chatService.findById(chatId);
            if (chatOpt.isEmpty() || !chatOpt.get().involvesUser(userId)) {
                throw new IllegalArgumentException("You are not part of this chat");
            }
            
            // Add chat info to web socket session
            headerAccessor.getSessionAttributes().put("chatId", chatId);
            headerAccessor.getSessionAttributes().put("userId", userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("type", "JOIN");
            response.put("userId", userId);
            response.put("message", "User joined the chat");
            
            // Send to specific chat topic
            messagingTemplate.convertAndSend("/topic/chat/" + chatId, response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            // Send error to user's personal topic
            messagingTemplate.convertAndSendToUser(
                headerAccessor.getSessionId(), 
                "/topic/errors", 
                error
            );
        }
    }
    
    /**
     * WebSocket endpoint for typing indicator
     */
    @MessageMapping("/chat.typing")
    public void typing(@Payload Map<String, Object> typingData, 
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
            
            // Send to typing topic
            messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/typing", response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            // Send error to user's personal topic
            messagingTemplate.convertAndSendToUser(
                headerAccessor.getSessionId(), 
                "/topic/errors", 
                error
            );
        }
    }
    
    /**
     * WebSocket endpoint for stop typing indicator
     */
    @MessageMapping("/chat.stopTyping")
    public void stopTyping(@Payload Map<String, Object> typingData, 
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
            
            // Send to stop typing topic
            messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/stopTyping", response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            // Send error to user's personal topic
            messagingTemplate.convertAndSendToUser(
                headerAccessor.getSessionId(), 
                "/topic/errors", 
                error
            );
        }
    }
    
    /**
     * REST API: Get all chats for current user with enhanced information
     */
    @GetMapping("/api/chats")
    @ResponseBody
    public ResponseEntity<?> getAllChats() {
        try {
            System.out.println("=== getAllChats called ===");
            
            // Debug authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication: " + authentication);
            if (authentication != null) {
                System.out.println("Principal: " + authentication.getPrincipal());
                System.out.println("Name: " + authentication.getName());
                System.out.println("Authenticated: " + authentication.isAuthenticated());
            }
            
            Long currentUserId = getCurrentUserId();
            System.out.println("Getting chats for user: " + currentUserId);
            
            List<Chat> chats = chatService.findByUserId(currentUserId);
            System.out.println("Found " + chats.size() + " chats");
            
            List<Map<String, Object>> chatList = new ArrayList<>();
            
            for (Chat chat : chats) {
                try {
                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put("id", chat.getId());
                    chatData.put("requestId", chat.getRequestId());
                    chatData.put("bookId", chat.getBookId());
                    chatData.put("user1Id", chat.getUser1Id());
                    chatData.put("user2Id", chat.getUser2Id());
                    chatData.put("status", chat.getStatus());
                    chatData.put("createdAt", chat.getCreatedAt());
                    chatData.put("updatedAt", chat.getUpdatedAt());
                    
                    // Get book information
                    Optional<Book> bookOpt = bookService.findById(chat.getBookId());
                    if (bookOpt.isPresent()) {
                        Book book = bookOpt.get();
                        Map<String, Object> bookData = new HashMap<>();
                        bookData.put("id", book.getId());
                        bookData.put("title", book.getTitle());
                        bookData.put("author", book.getAuthor());
                        bookData.put("genre", book.getGenre());
                        bookData.put("condition", book.getCondition());
                        bookData.put("sharingType", book.getSharingType());
                        bookData.put("status", book.getStatus());
                        chatData.put("book", bookData);
                    } else {
                        System.err.println("Book not found for chat " + chat.getId() + ", bookId: " + chat.getBookId());
                        // Create a placeholder book data
                        Map<String, Object> bookData = new HashMap<>();
                        bookData.put("id", chat.getBookId());
                        bookData.put("title", "Unknown Book");
                        bookData.put("author", "Unknown Author");
                        chatData.put("book", bookData);
                    }
                    
                    // Get user information
                    Optional<User> user1Opt = userService.findById(chat.getUser1Id());
                    Optional<User> user2Opt = userService.findById(chat.getUser2Id());
                    
                    if (user1Opt.isPresent() && user2Opt.isPresent()) {
                        User user1 = user1Opt.get();
                        User user2 = user2Opt.get();
                        
                        Map<String, Object> user1Data = new HashMap<>();
                        user1Data.put("id", user1.getId());
                        user1Data.put("firstName", user1.getFirstName());
                        user1Data.put("lastName", user1.getLastName());
                        user1Data.put("email", user1.getEmail());
                        
                        Map<String, Object> user2Data = new HashMap<>();
                        user2Data.put("id", user2.getId());
                        user2Data.put("firstName", user2.getFirstName());
                        user2Data.put("lastName", user2.getLastName());
                        user2Data.put("email", user2.getEmail());
                        
                        chatData.put("user1", user1Data);
                        chatData.put("user2", user2Data);
                    } else {
                        System.err.println("User not found for chat " + chat.getId() + ", user1Id: " + chat.getUser1Id() + ", user2Id: " + chat.getUser2Id());
                        // Create placeholder user data
                        Map<String, Object> user1Data = new HashMap<>();
                        user1Data.put("id", chat.getUser1Id());
                        user1Data.put("firstName", "Unknown");
                        user1Data.put("lastName", "User");
                        user1Data.put("email", "unknown@example.com");
                        
                        Map<String, Object> user2Data = new HashMap<>();
                        user2Data.put("id", chat.getUser2Id());
                        user2Data.put("firstName", "Unknown");
                        user2Data.put("lastName", "User");
                        user2Data.put("email", "unknown@example.com");
                        
                        chatData.put("user1", user1Data);
                        chatData.put("user2", user2Data);
                    }
                    
                    // Get last message
                    Optional<Message> lastMessageOpt = chatService.getLastMessage(chat.getId());
                    if (lastMessageOpt.isPresent()) {
                        Message lastMessage = lastMessageOpt.get();
                        Map<String, Object> messageData = new HashMap<>();
                        messageData.put("id", lastMessage.getId());
                        messageData.put("content", lastMessage.getContent());
                        messageData.put("senderId", lastMessage.getSenderId());
                        messageData.put("messageType", lastMessage.getMessageType());
                        messageData.put("createdAt", lastMessage.getCreatedAt());
                        chatData.put("lastMessage", messageData);
                    } else {
                        System.out.println("No last message found for chat " + chat.getId());
                    }
                    
                    // Get unread count for this chat
                    long unreadCount = chatService.getUnreadMessageCount(chat.getId(), currentUserId);
                    chatData.put("unreadCount", unreadCount);
                    
                    chatList.add(chatData);
                } catch (Exception e) {
                    System.err.println("Error processing chat " + chat.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                    // Continue with other chats instead of failing completely
                }
            }
            
            System.out.println("Returning " + chatList.size() + " processed chats");
            return ResponseEntity.ok(chatList);
            
        } catch (Exception e) {
            System.err.println("Error in getAllChats: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * REST API: Send a message in a chat
     */
    @PostMapping("/api/chats/{chatId}/messages")
    @ResponseBody
    public ResponseEntity<?> sendChatMessage(@PathVariable Long chatId, @RequestBody Map<String, Object> messageData) {
        try {
            Long currentUserId = getCurrentUserId();
            String content = (String) messageData.get("content");
            
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message content cannot be empty"));
            }
            
            // Verify user is part of the chat
            Optional<Chat> chatOpt = chatService.findById(chatId);
            if (chatOpt.isEmpty() || !chatOpt.get().involvesUser(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You do not have access to this chat"));
            }
            
            Message message = chatService.sendMessage(chatId, currentUserId, content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", message.getId());
            response.put("chatId", message.getChatId());
            response.put("senderId", message.getSenderId());
            response.put("content", message.getContent());
            response.put("messageType", message.getMessageType());
            response.put("createdAt", message.getCreatedAt());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * REST API: Get chat messages by chat ID
     */
    @GetMapping("/api/chats/{chatId}/messages")
    @ResponseBody
    public ResponseEntity<?> getChatMessagesByChatId(@PathVariable Long chatId) {
        try {
            Long currentUserId = getCurrentUserId();
            
            // Verify user is part of the chat
            Optional<Chat> chatOpt = chatService.findById(chatId);
            if (chatOpt.isEmpty() || !chatOpt.get().involvesUser(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You do not have access to this chat"));
            }
            
            List<Message> messages = chatService.getChatMessages(chatId);
            
            List<Map<String, Object>> messageList = new ArrayList<>();
            for (Message message : messages) {
                Map<String, Object> messageData = new HashMap<>();
                messageData.put("id", message.getId());
                messageData.put("chatId", message.getChatId());
                messageData.put("senderId", message.getSenderId());
                messageData.put("content", message.getContent());
                messageData.put("messageType", message.getMessageType());
                messageData.put("createdAt", message.getCreatedAt());
                messageList.add(messageData);
            }
            
            return ResponseEntity.ok(messageList);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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
            List<Map<String, Object>> enhancedChats = chatService.getEnhancedActiveChatsForUser(userId);
            
            // Enhance with book and user information
            for (Map<String, Object> chat : enhancedChats) {
                Long chatId = (Long) chat.get("id");
                Long otherUserId = (Long) chat.get("otherUserId");
                
                // Get chat details to find book
                Optional<Chat> chatOpt = chatService.findById(chatId);
                if (chatOpt.isPresent()) {
                    Chat chatEntity = chatOpt.get();
                    
                    // Get book information
                    Optional<Book> bookOpt = bookService.findById(chatEntity.getBookId());
                    if (bookOpt.isPresent()) {
                        Book book = bookOpt.get();
                        chat.put("bookId", book.getId());
                        chat.put("bookTitle", book.getTitle());
                        chat.put("bookAuthor", book.getAuthor());
                    }
                    
                    // Get other user information
                    Optional<User> otherUserOpt = userService.findById(otherUserId);
                    if (otherUserOpt.isPresent()) {
                        User otherUser = otherUserOpt.get();
                        chat.put("otherUserName", otherUser.getFirstName() + " " + otherUser.getLastName());
                        chat.put("otherUserEmail", otherUser.getEmail());
                    }
                }
            }
            
            return ResponseEntity.ok(enhancedChats);
            
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
     * REST API: Mark messages as read for a chat
     */
    @PostMapping("/api/chat/{chatId}/mark-read")
    @ResponseBody
    public ResponseEntity<?> markMessagesAsRead(@PathVariable Long chatId) {
        try {
            Long userId = getCurrentUserId();
            chatService.markMessagesAsRead(chatId, userId);
            return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to mark messages as read"));
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

    /**
     * REST API: Get chat details by chat ID with enhanced information
     */
    @GetMapping("/api/chats/{chatId}/details")
    @ResponseBody
    public ResponseEntity<?> getChatDetailsByChatId(@PathVariable Long chatId) {
        try {
            Long currentUserId = getCurrentUserId();
            
            Optional<Chat> chatOpt = chatService.findById(chatId);
            if (chatOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Chat chat = chatOpt.get();
            if (!chat.involvesUser(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You do not have access to this chat"));
            }
            
            // Get book information
            Optional<Book> bookOpt = bookService.findById(chat.getBookId());
            if (bookOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Book not found"));
            }
            Book book = bookOpt.get();
            
            // Get user information
            Optional<User> user1Opt = userService.findById(chat.getUser1Id());
            Optional<User> user2Opt = userService.findById(chat.getUser2Id());
            
            if (user1Opt.isEmpty() || user2Opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "User information not found"));
            }
            
            User user1 = user1Opt.get();
            User user2 = user2Opt.get();
            
            // Determine which user is the other user (not current user)
            User otherUser = user1.getId().equals(currentUserId) ? user2 : user1;
            
            Map<String, Object> response = new HashMap<>();
            response.put("chat", Map.of(
                "id", chat.getId(),
                "requestId", chat.getRequestId(),
                "bookId", chat.getBookId(),
                "user1Id", chat.getUser1Id(),
                "user2Id", chat.getUser2Id(),
                "status", chat.getStatus(),
                "createdAt", chat.getCreatedAt(),
                "updatedAt", chat.getUpdatedAt()
            ));
            response.put("book", Map.of(
                "id", book.getId(),
                "title", book.getTitle(),
                "author", book.getAuthor(),
                "genre", book.getGenre(),
                "condition", book.getCondition(),
                "sharingType", book.getSharingType().getDisplayName(),
                "status", book.getStatus().getDisplayName()
            ));
            response.put("otherUser", Map.of(
                "id", otherUser.getId(),
                "firstName", otherUser.getFirstName(),
                "lastName", otherUser.getLastName(),
                "email", otherUser.getEmail()
            ));
            response.put("currentUserId", currentUserId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
} 