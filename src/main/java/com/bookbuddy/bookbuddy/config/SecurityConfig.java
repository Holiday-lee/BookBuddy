/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.bookbuddy.bookbuddy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


/**
 *
 * @author holiday
 * Spring Boot Security to control which pages can be accessed by public or need a login 
 * To allow static files
 * Unauthenticated users are automatically redirected to /login
 */

@Profile("dev") // ← ONLY loads this config if profile = prod
@Configuration //marks this class as a Spring config class
@EnableWebSecurity //enables Spring Security support for the application
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { //defines the rules for securing HTTP requests
        http
            .authorizeHttpRequests(auth -> auth
                // Public access - available to everyone (unauthenticated users)
                .requestMatchers("/", "/index.html").permitAll()
                .requestMatchers("/pages/login.html", "/pages/register.html").permitAll()
                
                // Pulic view for sign up and login endpoints
                .requestMatchers("/register", "/login", "/logout").permitAll()
                .requestMatchers("/api/check-email", "/api/current-user").permitAll()
                
                // Static resources - allow all
                .requestMatchers("/static/**").permitAll()
                .requestMatchers("/css/**").permitAll()
                .requestMatchers("/js/**").permitAll()
                .requestMatchers("/images/**").permitAll()
                .requestMatchers("/fonts/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                
                // Publci access for search functionality 
                .requestMatchers("/pages/search-books.html").permitAll()
                .requestMatchers("/search/**", "/api/books/search/**", "/api/books/nearby").permitAll()
                
                    
                // Authenticated user features that require a login for other HTML pages
                .requestMatchers("/pages/dashboard.html").authenticated()
                .requestMatchers("/pages/list-book.html").authenticated()
                .requestMatchers("/pages/my-received-requests.html").authenticated()
                .requestMatchers("/pages/my-sent-requests.html").authenticated()
                .requestMatchers("/pages/chat.html").authenticated()
                .requestMatchers("/pages/swap-history.html").authenticated()
                .requestMatchers("/pages/profile.html").authenticated()
                .requestMatchers("/pages/search-by-location.html").authenticated()
                .requestMatchers("/pages/my-books.html").authenticated()
                .requestMatchers("/pages/settings.html").authenticated()
                
                // API endpoints - require authentication
                .requestMatchers("/api/books/create", "/api/books/update/**", "/api/books/delete/**").authenticated()
                .requestMatchers("/api/requests/**").authenticated()
                .requestMatchers("/api/chat/**").authenticated()
                .requestMatchers("/api/profile/**").authenticated()
                .requestMatchers("/api/user/**").authenticated()
                
                // any request not matched above also requires login
                .anyRequest().authenticated()
            )

            // DISABLE Spring Security's default form login completely
            // as using own custom login page,handle login via AuthController, not through Spring's UI.
            .formLogin(form -> form.disable())
            
            // Configure logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")  // redirect to public homepage after logout
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID") //clear session and cookies
                .permitAll()
            )
            
            // session management
            .sessionManagement(session -> session
                .maximumSessions(1) //limits each user to 1 session at a time
                //if wanted to block the new login instead, set it to true
                .maxSessionsPreventsLogin(false) //if user login again from a new device/browser,ends old session,allows the new session
                    
            )
            // Disable CSRF for now (easier for testing)
            .csrf(csrf -> csrf.disable());
            
        return http.build();
    }
    
    // create a bean that hashes passwords using BCrypt for latter to use in UserService to encode, check passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}