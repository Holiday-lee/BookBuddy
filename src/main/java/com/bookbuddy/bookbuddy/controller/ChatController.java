/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.Chat;
import com.bookbuddy.bookbuddy.model.Message;
import com.bookbuddy.bookbuddy.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling chat operations
 * @author holiday
 */
@Controller
@RequestMapping("/chats")
public class ChatController {
    
    private final ChatService chatService;
    
    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    /**
     * Send a message in a chat
     */
    @PostMapping("/api/{chatId}/send")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@PathVariable Long chatId,
                                       @RequestParam("content") String content,
                                       HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
            Message message = chatService.sendMessage(chatId, userId, content);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get messages for a chat
     */
    @GetMapping("/api/{chatId}/messages")
    @ResponseBody
    public ResponseEntity<?> getChatMessages(@PathVariable Long chatId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
            List<Message> messages = chatService.getChatMessages(chatId);
            return ResponseEntity.ok(messages);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get recent messages for a chat
     */
    @GetMapping("/api/{chatId}/messages/recent")
    @ResponseBody
    public ResponseEntity<?> getRecentChatMessages(@PathVariable Long chatId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
            List<Message> messages = chatService.getRecentChatMessages(chatId);
            return ResponseEntity.ok(messages);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Complete a chat
     */
    @PostMapping("/api/{chatId}/complete")
    @ResponseBody
    public ResponseEntity<?> completeChat(@PathVariable Long chatId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
            Chat chat = chatService.completeChat(chatId, userId);
            return ResponseEntity.ok(chat);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Cancel a chat
     */
    @PostMapping("/api/{chatId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelChat(@PathVariable Long chatId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
            Chat chat = chatService.cancelChat(chatId, userId);
            return ResponseEntity.ok(chat);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get chat by ID
     */
    @GetMapping("/api/{chatId}")
    @ResponseBody
    public ResponseEntity<?> getChat(@PathVariable Long chatId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        Optional<Chat> chatOpt = chatService.findById(chatId);
        if (chatOpt.isPresent()) {
            Chat chat = chatOpt.get();
            if (chat.involvesUser(userId)) {
                return ResponseEntity.ok(chat);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Not authorized"));
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get chat by request ID
     */
    @GetMapping("/api/request/{requestId}")
    @ResponseBody
    public ResponseEntity<?> getChatByRequest(@PathVariable Long requestId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        Optional<Chat> chatOpt = chatService.findByRequestId(requestId);
        if (chatOpt.isPresent()) {
            Chat chat = chatOpt.get();
            if (chat.involvesUser(userId)) {
                return ResponseEntity.ok(chat);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Not authorized"));
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get active chats for current user
     */
    @GetMapping("/api/active")
    @ResponseBody
    public ResponseEntity<?> getActiveChats(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        List<Chat> chats = chatService.findActiveChatsByUser(userId);
        return ResponseEntity.ok(chats);
    }
    
    /**
     * Get all chats for current user
     */
    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<?> getAllChats(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        List<Chat> chats = chatService.findAllChatsByUser(userId);
        return ResponseEntity.ok(chats);
    }
    
    /**
     * Get completed chats for current user
     */
    @GetMapping("/api/completed")
    @ResponseBody
    public ResponseEntity<?> getCompletedChats(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        List<Chat> chats = chatService.findCompletedChatsByUser(userId);
        return ResponseEntity.ok(chats);
    }
    
    /**
     * Get unread message count for a chat
     */
    @GetMapping("/api/{chatId}/unread-count")
    @ResponseBody
    public ResponseEntity<?> getUnreadMessageCount(@PathVariable Long chatId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
            long count = chatService.getUnreadMessageCount(chatId, userId);
            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get latest message for a chat
     */
    @GetMapping("/api/{chatId}/latest-message")
    @ResponseBody
    public ResponseEntity<?> getLatestMessage(@PathVariable Long chatId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
            Message message = chatService.getLatestMessage(chatId);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 