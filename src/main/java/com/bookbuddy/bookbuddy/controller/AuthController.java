/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
}