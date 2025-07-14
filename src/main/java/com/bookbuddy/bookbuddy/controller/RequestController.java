/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.Request;
import com.bookbuddy.bookbuddy.service.RequestService;
import com.bookbuddy.bookbuddy.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    
    @Autowired
    public RequestController(RequestService requestService, ChatService chatService) {
        this.requestService = requestService;
        this.chatService = chatService;
    }
    
    /**
     * Create a give away request
     */
    @PostMapping("/api/give-away")
    @ResponseBody
    public ResponseEntity<?> createGiveAwayRequest(@RequestParam("bookId") Long bookId,
                                                 @RequestParam(value = "message", required = false) String message,
                                                 HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
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
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
            Request request = requestService.createLendRequest(bookId, userId, message, requestedDurationDays);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Create a trade request
     */
    @PostMapping("/api/trade")
    @ResponseBody
    public ResponseEntity<?> createTradeRequest(@RequestParam("bookId") Long bookId,
                                              @RequestParam("offeredBookId") Long offeredBookId,
                                              @RequestParam(value = "message", required = false) String message,
                                              HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
            Request request = requestService.createTradeRequest(bookId, userId, offeredBookId, message);
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
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
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
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
            Request request = requestService.rejectRequest(requestId, userId);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Complete a request (for give away and trade)
     */
    @PostMapping("/api/{requestId}/complete")
    @ResponseBody
    public ResponseEntity<?> completeRequest(@PathVariable Long requestId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
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
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
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
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
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
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        List<Request> requests = requestService.findRequestsByRequester(userId);
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get requests by owner
     */
    @GetMapping("/api/my-received")
    @ResponseBody
    public ResponseEntity<?> getMyReceivedRequests(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        List<Request> requests = requestService.findRequestsByOwner(userId);
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get active requests by requester
     */
    @GetMapping("/api/my-sent/active")
    @ResponseBody
    public ResponseEntity<?> getMyActiveSentRequests(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        List<Request> requests = requestService.findActiveRequestsByRequester(userId);
        return ResponseEntity.ok(requests);
    }
    
    /**
     * Get active requests by owner
     */
    @GetMapping("/api/my-received/active")
    @ResponseBody
    public ResponseEntity<?> getMyActiveReceivedRequests(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        List<Request> requests = requestService.findActiveRequestsByOwner(userId);
        return ResponseEntity.ok(requests);
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
} 