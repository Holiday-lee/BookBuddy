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

/**
 *
 * @author holiday
 * Handles user authentication tasks such as registration, login, logout, and session management. 
 * Communicates with UserService and returns web page redirects or JSON responses.
 * 
 * Stores/retrieves user data in HttpSession: loginUser(), logout(), getCurrentUser()		
 * 
 */

@Controller  // mark as Spring MVC Controller that handles web requests
public class AuthController {
    
    // Injects UserService, which contains business logic: registering/authenticating users
    private final UserService userService;
    
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    

     /** 
      * handle user registration
      */
    @PostMapping("/register") // will be triggered when a POST request is sent to /register.
    // gets user input from the form (firstName, lastName, email, password).
    public String registerUser(@RequestParam("firstName") String firstName, 
                             @RequestParam("lastName") String lastName,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             RedirectAttributes redirectAttributes) {
        
        try {
            User newUser = userService.registerUser(firstName, lastName, email, password); // try to register the user via userService.registerUser(bla bla bla)
            
            // if success, add a flash message like “Registration successful!”
            redirectAttributes.addFlashAttribute("successMessage", 
                "Registration successful! Welcome to BookBuddy, " + newUser.getFirstName() + "!");
            
            return "redirect:/pages/login.html";  // then redirects to the login page (/pages/login.html)
            
            // if fail, show an error message + prefilled values to the redirect.
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("firstName", firstName);
            redirectAttributes.addFlashAttribute("lastName", lastName);
            redirectAttributes.addFlashAttribute("email", email);
            
            return "redirect:/pages/register.html"; // redirects to the registration page (/pages/register.html)
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Registration failed. Please try again.");
            
            return "redirect:/pages/register.html";
        }
    }
    
    /**
    // handle user login
     */
    @PostMapping("/login") // triggered by POST to /login.
    public String loginUser(@RequestParam("email") String email,
                           @RequestParam("password") String password,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        try {
            if (userService.authenticateUser(email, password)) { // authenticates user credentials via userService.authenticateUser(...)
                Optional<User> userOpt = userService.findByEmail(email); // search user by email
                if (userOpt.isPresent()) {
                    User user = userOpt.get(); //if valid
                    
                    //store user info in the HTTP session (loggedInUser, userId, userEmail, userName, isAuthenticated)
                    session.setAttribute("loggedInUser", user);
                    session.setAttribute("userId", user.getId());
                    session.setAttribute("userEmail", user.getEmail());
                    session.setAttribute("userName", user.getFullName());
                    session.setAttribute("isAuthenticated", true);
                    
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Welcome back, " + user.getFirstName() + "!");
                    
                    return "redirect:/"; // then redirects to the home page (/)
                }
            }
            
            return "redirect:/pages/login.html?error=true"; // if invalid, redirects to login page with error flag (/pages/login.html?error=true)
            
        } catch (Exception e) {
            return "redirect:/pages/login.html?error=true"; // if encounter exception, also redirect to login page with error flag....
        }
    }
    
    /**
     * Handle logout
     */
    @GetMapping("/logout") // triggered by GET to /logout
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) { //invalidates the session(logs out the user)
        session.invalidate();
        
        redirectAttributes.addFlashAttribute("successMessage", 
            "You have been logged out successfully."); //add a success message
        
        return "redirect:/"; //redirect to the home page
    }
    
    /**
     * Check if email exists (for AJAX validation)
     */
    @GetMapping("/api/check-email") //AJAX endpoint for checking if an email is already registered (e.g. during sign-up form validation)
    @ResponseBody
    public boolean checkEmailExists(@RequestParam("email") String email) {
        return userService.emailExists(email); //return true or false as JSON
    }
    
    /**
     * Get current user info AJAX (for navigation bar)
     */
    @GetMapping("/api/current-user")
    @ResponseBody
    public Object getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        
        if (user != null) {
            return new Object() { //return a JSON object with the currently logged-in user's info.
                //if logged in: return user data(ID, name, email, etc.) and authenticated: true.
                public final Long id = user.getId();
                public final String firstName = user.getFirstName();
                public final String lastName = user.getLastName();
                public final String email = user.getEmail();
                public final String fullName = user.getFullName();
                public final boolean authenticated = true;
            };
        }
        
        //if not: return{ authenticated: false, message: "Not logged in" }
        return new Object() {
            public final boolean authenticated = false;
            public final String message = "Not logged in";
        };
    }
}