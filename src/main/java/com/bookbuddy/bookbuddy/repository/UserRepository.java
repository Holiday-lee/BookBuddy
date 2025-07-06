/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.repository;

import com.bookbuddy.bookbuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 *
 * @author holiday
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email address
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by first name (case insensitive)
     */
    List<User> findByFirstNameIgnoreCase(String firstName);
    
    /**
     * Find users by last name (case insensitive)
     */
    List<User> findByLastNameIgnoreCase(String lastName);
    
    /**
     * Find users by full name search (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByFullNameContaining(@Param("name") String name);
    
    /**
     * Count total users
     */
    long count();
}
