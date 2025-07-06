/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.service;

import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author holiday
 */
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Register a new user
     */
    public User registerUser(String firstName, String lastName, String email, String password) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        
        // Validate input
        validateUserInput(firstName, lastName, email, password);
        
        // Create new user
        User user = new User();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email.toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(password));
        
        return userRepository.save(user);
    }
    
    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim());
    }
    
    /**
     * Find user by ID
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Authenticate user login
     */
    @Transactional(readOnly = true)
    public boolean authenticateUser(String email, String password) {
        Optional<User> userOpt = findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }
    
    /**
     * Update user profile
     */
    public User updateUser(Long userId, String firstName, String lastName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        if (firstName != null && !firstName.trim().isEmpty()) {
            user.setFirstName(firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            user.setLastName(lastName.trim());
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Change user password
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }
    
    /**
     * Get user count
     */
    @Transactional(readOnly = true)
    public long getUserCount() {
        return userRepository.count();
    }
    
    /**
     * Search users by name
     */
    @Transactional(readOnly = true)
    public List<User> searchUsersByName(String name) {
        return userRepository.findByFullNameContaining(name);
    }
    
    /**
     * Validate user input
     */
    private void validateUserInput(String firstName, String lastName, String email, String password) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }
    
    /**
     * Basic email validation
     */
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }
}
