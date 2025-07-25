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
 * it defines custom query methods
 */
@Repository
// JpaRepository<User, Long> :it inherits built-in CRUD operations like save(), findById(), delete(), 
// and more — all for User entities, using Long as the ID type.
public interface UserRepository extends JpaRepository<User, Long> { //Long=type of the user's ID
    
    /**
     * find & return a user by email address
     * auto generate SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email); //returns Optional<User> to handle nulls safely
    
    /**
     * Check if user exists by email
     * SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);

    
    /**
     * Find users by full name search (case insensitive)
     * findByFullNameContaining() is a custom JPQL query
     * it joins firstName and lastName with a space
     * then it does a case-insensitive partial match with the given input (:name)
     * For example: searching "john doe" will match users with full names like "John Doe" or "john DOe"
     */
    @Query("SELECT u FROM User u WHERE " +
            //CONCAT(u.firstName, ' ', u.lastName) => joins first and last name
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByFullNameContaining(@Param("name") String name);
    
}
