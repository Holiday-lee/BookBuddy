/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.bookbuddy.bookbuddy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public access - available to everyone (unauthenticated users)
                .requestMatchers("/", "/index.html").permitAll()
                .requestMatchers("/pages/login.html", "/pages/register.html").permitAll()
                
                // Registration and login endpoints - CRITICAL: These must be public
                .requestMatchers("/register", "/login", "/logout").permitAll()
                .requestMatchers("/api/check-email", "/api/current-user").permitAll()
                
                // Static resources - allow all
                .requestMatchers("/static/**").permitAll()
                .requestMatchers("/css/**").permitAll()
                .requestMatchers("/js/**").permitAll()
                .requestMatchers("/images/**").permitAll()
                .requestMatchers("/fonts/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                
                // Search functionality - PUBLIC as per your strategy
                .requestMatchers("/pages/search-books.html").permitAll()
                .requestMatchers("/search/**", "/api/books/search/**", "/api/books/nearby").permitAll()
                
                // Authenticated user features - require login (HTML pages)
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
                
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            // DISABLE Spring Security's default form login completely
            .formLogin(form -> form.disable())
            
            // Configure logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")  // Redirect to public homepage after logout
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            // Disable CSRF for now (easier for testing)
            .csrf(csrf -> csrf.disable());
            
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}