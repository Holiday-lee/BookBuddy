/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.Request;
import com.bookbuddy.bookbuddy.model.RequestWithBookInfo;
import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.service.RequestService;
import com.bookbuddy.bookbuddy.service.ChatService;
import com.bookbuddy.bookbuddy.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling book requests
 * @author holiday
 */
@Controller
@RequestMapping("/requests")
public class RequestController {
    
    private final RequestService requestService;
    private final ChatService chatService;
    private final UserService userService;
    
    @Autowired
    public RequestController(RequestService requestService, ChatService chatService, UserService userService) {
        this.requestService = requestService;
        this.chatService = chatService;
        this.userService = userService;
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
     * Create a give away request
     */
    @PostMapping("/api/give-away")
    @ResponseBody
    public ResponseEntity<?> createGiveAwayRequest(@RequestParam("bookId") Long bookId,
                                                 @RequestParam(value = "message", required = false) String message,
                                                 HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            Request request = requestService.createGiveAwayRequest(bookId, userId, message);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Create a lending request
     */
    @PostMapping("/api/lend")
    @ResponseBody
    public ResponseEntity<?> createLendRequest(@RequestParam("bookId") Long bookId,
                                             @RequestParam("requestedDurationDays") Integer requestedDurationDays,
                                             @RequestParam(value = "message", required = false) String message,
                                             HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            Request request = requestService.createLendRequest(bookId, userId, message, requestedDurationDays);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Create a swap request
     */
    @PostMapping("/api/swap")
    @ResponseBody
    public ResponseEntity<?> createSwapRequest(@RequestParam("bookId") Long bookId,
                                              @RequestParam("offeredBookId") Long offeredBookId,
                                              @RequestParam(value = "message", required = false) String message,
                                              HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            Request request = requestService.createSwapRequest(bookId, userId, offeredBookId, message);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Accept a request
     */
    @PostMapping("/api/{requestId}/accept")
    @ResponseBody
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId, HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            Request request = requestService.acceptRequest(requestId, userId);
            
            // Create chat for accepted request
            chatService.createChatForRequest(requestId);
            
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Reject a request
     */
    @PostMapping("/api/{requestId}/reject")
    @ResponseBody
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId, HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            Request request = requestService.rejectRequest(requestId, userId);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Complete a request (for give away and swap)
     */
    @PostMapping("/api/{requestId}/complete")
    @ResponseBody
    public ResponseEntity<?> completeRequest(@PathVariable Long requestId, HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            Request request = requestService.completeRequest(requestId, userId);
            
            // Complete the associated chat
            Optional<com.bookbuddy.bookbuddy.model.Chat> chatOpt = chatService.findByRequestId(requestId);
            if (chatOpt.isPresent()) {
                chatService.completeChat(chatOpt.get().getId(), userId);
            }
            
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Return a lent book
     */
    @PostMapping("/api/{requestId}/return")
    @ResponseBody
    public ResponseEntity<?> returnLentBook(@PathVariable Long requestId, HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            Request request = requestService.returnLentBook(requestId, userId);
            
            // Complete the associated chat
            Optional<com.bookbuddy.bookbuddy.model.Chat> chatOpt = chatService.findByRequestId(requestId);
            if (chatOpt.isPresent()) {
                chatService.completeChat(chatOpt.get().getId(), userId);
            }
            
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Cancel a request (by requester)
     */
    @PostMapping("/api/{requestId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelRequest(@PathVariable Long requestId, HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            Request request = requestService.cancelRequest(requestId, userId);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get request by ID
     */
    @GetMapping("/api/{requestId}")
    @ResponseBody
    public ResponseEntity<?> getRequest(@PathVariable Long requestId) {
        Optional<Request> requestOpt = requestService.findById(requestId);
        if (requestOpt.isPresent()) {
            return ResponseEntity.ok(requestOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get requests by requester
     */
    @GetMapping("/api/my-sent")
    @ResponseBody
    public ResponseEntity<?> getMySentRequests(HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            List<RequestWithBookInfo> requests = requestService.findRequestsByRequesterWithBookInfo(userId);
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get requests by owner
     */
    @GetMapping("/api/my-received")
    @ResponseBody
    public ResponseEntity<?> getMyReceivedRequests(HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            List<RequestWithBookInfo> requests = requestService.findRequestsByOwnerWithBookInfo(userId);
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get active sent requests
     */
    @GetMapping("/api/my-sent/active")
    @ResponseBody
    public ResponseEntity<?> getMyActiveSentRequests(HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            List<Request> requests = requestService.findActiveRequestsByRequester(userId);
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get active received requests
     */
    @GetMapping("/api/my-received/active")
    @ResponseBody
    public ResponseEntity<?> getMyActiveReceivedRequests(HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            List<Request> requests = requestService.findActiveRequestsByOwner(userId);
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get pending requests by book
     */
    @GetMapping("/api/book/{bookId}/pending")
    @ResponseBody
    public ResponseEntity<?> getPendingRequestsByBook(@PathVariable Long bookId) {
        List<Request> requests = requestService.findPendingRequestsByBook(bookId);
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get all requests by book
     */
    @GetMapping("/api/book/{bookId}")
    @ResponseBody
    public ResponseEntity<?> getRequestsByBook(@PathVariable Long bookId) {
        List<Request> requests = requestService.findRequestsByBook(bookId);
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get notification count for received requests (pending requests)
     */
    @GetMapping("/api/notifications/received-count")
    @ResponseBody
    public ResponseEntity<?> getReceivedRequestsNotificationCount(HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            long count = requestService.countPendingRequestsByOwner(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get notification count for sent requests (updates on sent requests)
     */
    @GetMapping("/api/notifications/sent-count")
    @ResponseBody
    public ResponseEntity<?> getSentRequestsNotificationCount(HttpSession session) {
        try {
            Long userId = getCurrentUserId();
            long count = requestService.countUpdatedSentRequests(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
} 