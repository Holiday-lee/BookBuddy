/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles user authentication with Spring Security integration
 */
@Controller
public class AuthController {
    
    private final UserService userService;
    
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Handle user registration
     */
    @PostMapping("/register")
    public String registerUser(@RequestParam("firstName") String firstName, 
                             @RequestParam("lastName") String lastName,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             RedirectAttributes redirectAttributes) {
        
        try {
            userService.registerUser(firstName, lastName, email, password);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Registration successful! Please log in.");
            return "redirect:/pages/login.html";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("firstName", firstName);
            redirectAttributes.addFlashAttribute("lastName", lastName);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/pages/register.html";
        }
    }
    
    /**
     * Check if email exists (for AJAX validation)
     */
    @GetMapping("/api/check-email")
    @ResponseBody
    public boolean checkEmailExists(@RequestParam("email") String email) {
        return userService.emailExists(email);
    }
    
    /**
     * Get current user info for frontend (works with Spring Security)
     */
    @GetMapping("/api/current-user")
    @ResponseBody
    public Map<String, Object> getCurrentUser(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Debug session information
        System.out.println("=== Session Debug ===");
        System.out.println("Session ID: " + session.getId());
        System.out.println("Session Creation Time: " + new java.util.Date(session.getCreationTime()));
        System.out.println("Session Last Accessed Time: " + new java.util.Date(session.getLastAccessedTime()));
        System.out.println("Session Max Inactive Interval: " + session.getMaxInactiveInterval());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            
            String email = authentication.getName(); // Spring Security stores username (email)
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("authenticated", true);
                response.put("email", user.getEmail());
                response.put("firstName", user.getFirstName());
                response.put("lastName", user.getLastName());
                response.put("userId", user.getId());
                response.put("createdAt", user.getCreatedAt());
                System.out.println("User authenticated: " + user.getEmail());
            } else {
                response.put("authenticated", false);
                response.put("message", "User not found");
                System.out.println("User not found in database");
            }
        } else {
            response.put("authenticated", false);
            response.put("message", "Not logged in");
            System.out.println("Not authenticated or anonymous user");
            if (authentication != null) {
                System.out.println("Authentication principal: " + authentication.getPrincipal());
                System.out.println("Authentication name: " + authentication.getName());
                System.out.println("Authentication authenticated: " + authentication.isAuthenticated());
            }
        }
        
        System.out.println("=== End Session Debug ===");
        
        return response;
    }

    /**
     * Update user profile information
     */
    @PostMapping("/api/update-profile")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestParam("firstName") String firstName,
                                         @RequestParam("lastName") String lastName,
                                         HttpSession session) {
        
        try {
            // Get current user from Spring Security authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Please log in to update your profile.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Get user by email from authentication
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found. Please log in again.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = userOpt.get();
            
            // Update user information using the correct method signature
            User updatedUser = userService.updateUser(user.getId(), firstName.trim(), lastName.trim());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully!");
            response.put("firstName", updatedUser.getFirstName());
            response.put("lastName", updatedUser.getLastName());
            response.put("email", updatedUser.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update profile. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Change user password
     */
    @PostMapping("/api/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestParam("currentPassword") String currentPassword,
                                          @RequestParam("newPassword") String newPassword,
                                          HttpSession session) {
        
        try {
            // Get current user from Spring Security authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Please log in to change your password.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Get user by email from authentication
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found. Please log in again.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = userOpt.get();
            
            // Change password using service
            userService.changePassword(user.getId(), currentPassword, newPassword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password changed successfully!");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to change password. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Debug endpoint to check session information
     */
    @GetMapping("/api/debug-session")
    @ResponseBody
    public Map<String, Object> debugSession(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        response.put("sessionId", session.getId());
        response.put("sessionCreationTime", new java.util.Date(session.getCreationTime()));
        response.put("sessionLastAccessedTime", new java.util.Date(session.getLastAccessedTime()));
        response.put("sessionMaxInactiveInterval", session.getMaxInactiveInterval());
        
        if (authentication != null) {
            response.put("authenticationName", authentication.getName());
            response.put("authenticationAuthenticated", authentication.isAuthenticated());
            response.put("authenticationPrincipal", authentication.getPrincipal().toString());
        } else {
            response.put("authenticationName", "null");
            response.put("authenticationAuthenticated", false);
            response.put("authenticationPrincipal", "null");
        }
        
        return response;
    }
}