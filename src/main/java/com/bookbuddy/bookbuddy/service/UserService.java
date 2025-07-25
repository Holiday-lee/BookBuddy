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
import org.springframework.transaction.annotation.Transactional; // for readONLY

import java.util.List;
import java.util.Optional;

@Service //marks it as a Spring service (i.e., contains business logic)
@Transactional //ensures that all DB operations in each method are atomic (rollback on failure)
public class UserService {
    
    private final UserRepository userRepository; //Interface to interact with the database(like save, findByEmail, etc.).
    private final PasswordEncoder passwordEncoder; //use to hash passwords and check password validity securely.
    
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * REgister a new user
     */
    public User registerUser(String firstName, String lastName, String email, String password){
        // Check if user already exists by checking the email is taken
        if (userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        
        // Validate input
        validateUserInput(firstName, lastName, email, password);
        
        // Create new user
        User user = new User();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email.toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(password)); //hashes the password with PasswordEncoder
        
        return userRepository.save(user);
    }
    
    /**
     * Find user by email: use for login
     */
    @Transactional(readOnly = true) // add the (readOnly = true) for Read-only (queries) 
    public Optional<User> findByEmail(String email) { //return Optional<User>, allowing safe handling of nulls
        return userRepository.findByEmail(email.toLowerCase().trim());
    }
    
    /**
     * find user by ID: use for data retrieval 
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) { //return Optional<User>, allowing safe handling of nulls.
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
     * Update user profile: the user’s first and last name
     */
    public User updateUser(Long userId, String firstName, String lastName){
        
        // This variable holds an Optional object that may or may not contain a User
        // userOptional might contain a User, or it might be empty if no user is found
        Optional<User> userOptional = userRepository.findById(userId);
        User user;
        if (userOptional.isPresent()){
            user = userOptional.get();
        } else {
            throw new IllegalArgumentException("User not found with the id" + userId);
        }
        
        // User user = userRepository.findById(userId)
        //        .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        if (firstName != null && !firstName.trim().isEmpty()){ //!firstName.trim().isEmpty())=> after trim(),removing any space at the biginning & end of the string,first name also not empty(avoid string=spaces only)
            user.setFirstName(firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()){
            user.setLastName(lastName.trim());
        }
        
        return userRepository.save(user); //save the updated user
    }
    
    /**
     * Change user password
     */
    public void changePassword(Long userId, String oldPassword, String newPassword){
        User user = userRepository.findById(userId) //find the user by ID
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        // verify old password if matches with the stored hash
        if (!passwordEncoder.matches(oldPassword, user.getPassword())){
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // validate new password length
        if (newPassword == null || newPassword.length()<6){
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword)); // hashes 
        userRepository.save(user); // and saves the new password
    }
    
    /**
     * check if email exists(use in the AJAX call in the controller)
     */
    @Transactional(readOnly = true)//to optimise for read-only queries.
    public boolean emailExists(String email){
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }
    
    /**
     * Get user count: return the total number of users in the system
     */
    @Transactional(readOnly = true)//to optimise for read-only queries.
    public long getUserCount(){
        return userRepository.count();
    }
    
    /**
     * Search users by name:search users whose full name contains the given substring
     * depend on the custom query method: List<User> findByFullNameContaining(String name)
     * in UserRepository, probably using Spring Data JPA
     */
    @Transactional(readOnly = true)
    public List<User> searchUsersByName(String name){
        return userRepository.findByFullNameContaining(name);
    }
    
    /**
     * Validate user input, check for required fields, valid email format,password length
     */
    private void validateUserInput(String firstName, String lastName, String email, String password){
        if (firstName == null || firstName.trim().isEmpty()){
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()){
            throw new IllegalArgumentException("Last name is required");
        }
        if (email == null || email.trim().isEmpty()){
            throw new IllegalArgumentException("Email is required");
        }
        if (!isValidEmail(email)){
            throw new IllegalArgumentException("Invalid email format");
        }
        if (password == null || password.length()<6){
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }
    
    /**
     * email validation, makes sure contains @ . and >5
     */
    private boolean isValidEmail(String email){
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }
}