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
    public Map<String, Object> getCurrentUser() {
        Map<String, Object> response = new HashMap<>();
        
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
            } else {
                response.put("authenticated", false);
                response.put("message", "User not found");
            }
        } else {
            response.put("authenticated", false);
            response.put("message", "Not logged in");
        }
        
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
}