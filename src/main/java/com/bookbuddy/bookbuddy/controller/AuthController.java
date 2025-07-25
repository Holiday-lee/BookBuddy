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
     * handle user registration
     */
    @PostMapping("/register")
    public String registerUser(@RequestParam("firstName") String firstName, 
                             @RequestParam("lastName") String lastName,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             RedirectAttributes redirectAttributes) {
        // RedirectAttributes is used to send data(like messages) across a redirect,so tht flash msg(success/error)can be shown on the next page
        
        try {
            userService.registerUser(firstName, lastName, email, password); //call service layer to handle the registration logic
            redirectAttributes.addFlashAttribute("successMessage", //FlashAttribute:showing messages once after a redirect
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
    @ResponseBody //tell spring tht I want Json as return not rendering a html page
    public boolean checkEmailExists(@RequestParam("email") String email){
        return userService.emailExists(email);
    }
    
    /**
     * Get current user info for frontend(works wit Spring Security)
     */
     @GetMapping("/api/current-user")//when someone sends a GET request to /api/current-user, call this method
    @ResponseBody //tell spring tht I want Json as return not rendering a html page
    public Map<String, Object> getCurrentUser(HttpSession session){ //HttpSession session:gives u access to the current user's session
        Map<String, Object> response = new HashMap<>();
        
        //get the current authentication object from Spring Security=>it's how Spring knows who is logged in.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        //there is an authentication object//user is authenticated & not anonymous 
        if (authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getPrincipal())){
            
            //In Spring Security, the "name" is often the username or email,depending on how login is set up.
            String email = authentication.getName(); //get the current user's email
            //search for a user in database by email. get back an Opt<User> in case the user isn't found
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isPresent()){ 
                User user = userOpt.get();
                //fill the response with the user's data
                response.put("authenticated", true);
                response.put("id", user.getId());
                response.put("email", user.getEmail());
                response.put("firstName", user.getFirstName());
                response.put("lastName", user.getLastName());
                response.put("userId", user.getId());
                response.put("createdAt", user.getCreatedAt());
            } else { //is user nt found(maybe data is corrupted or s1 deleted the user after login in)
                response.put("authenticated", false);
                response.put("message", "User not found");
            }
        } else { //if no1 is log in(the user is anonymous)
            response.put("authenticated", false);
            response.put("message", "Not logged in");
        }
        
        return response;//send the map back as a JSON object to front end
    }

    /**
     * Update user profile information
     */
    @PostMapping("/api/update-profile")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestParam("firstName") String firstName,
                                         @RequestParam("lastName") String lastName,
                                         HttpSession session){
        
        try {
            // Get current user from Spring Security authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Please log in to update your profile");
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
            response.put("message", "Your profile has been updated successfully!");
            response.put("firstName", updatedUser.getFirstName());
            response.put("lastName", updatedUser.getLastName());
            response.put("email", updatedUser.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e){
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