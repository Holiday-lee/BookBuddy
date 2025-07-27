/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.service;

import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
/**
 *
 * @author holiday
 * this classs is a helper tht Spring Security uses to load a user from database by email when s1 logs in
 * like when s1 tries to login, check the database for their email.If found,give Spring Security their details so it can check the password.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService{

    private final UserRepository userRepository;

    @Autowired//ask Spring to inject an instance of UserRepository
    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;//we need a userRepository to b injected here so it can fetch user data from the database
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {//Spring Securty sends email(s1 wants to login)
        User user = userRepository.findByEmail(email)//checks with database for that email
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(new ArrayList<>()) // No roles for now, just empty list
                .build();
    }
}
