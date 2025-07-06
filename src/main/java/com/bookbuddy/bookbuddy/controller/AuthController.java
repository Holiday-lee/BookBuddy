/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

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
            User newUser = userService.registerUser(firstName, lastName, email, password);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Registration successful! Welcome to BookBuddy, " + newUser.getFirstName() + "!");
            
            return "redirect:/pages/login.html";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("firstName", firstName);
            redirectAttributes.addFlashAttribute("lastName", lastName);
            redirectAttributes.addFlashAttribute("email", email);
            
            return "redirect:/pages/register.html";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Registration failed. Please try again.");
            
            return "redirect:/pages/register.html";
        }
    }
    
    /**
     * Handle user login
     */
    @PostMapping("/login")
    public String loginUser(@RequestParam("email") String email,
                           @RequestParam("password") String password,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        try {
            if (userService.authenticateUser(email, password)) {
                Optional<User> userOpt = userService.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    // Store user in session
                    session.setAttribute("loggedInUser", user);
                    session.setAttribute("userId", user.getId());
                    session.setAttribute("userEmail", user.getEmail());
                    session.setAttribute("userName", user.getFullName());
                    session.setAttribute("isAuthenticated", true);
                    
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Welcome back, " + user.getFirstName() + "!");
                    
                    return "redirect:/";
                }
            }
            
            return "redirect:/pages/login.html?error=true";
            
        } catch (Exception e) {
            return "redirect:/pages/login.html?error=true";
        }
    }
    
    /**
     * Handle logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        
        redirectAttributes.addFlashAttribute("successMessage", 
            "You have been logged out successfully.");
        
        return "redirect:/";
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
     * Get current user info (for navigation bar)
     */
    @GetMapping("/api/current-user")
    @ResponseBody
    public Object getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user != null) {
            return new Object() {
                public final Long id = user.getId();
                public final String firstName = user.getFirstName();
                public final String lastName = user.getLastName();
                public final String email = user.getEmail();
                public final String fullName = user.getFullName();
                public final boolean authenticated = true;
            };
        }
        
        return new Object() {
            public final boolean authenticated = false;
            public final String message = "Not logged in";
        };
    }
}