package com.bookbuddy.bookbuddy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

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
                
                // Static resources - allow all
                .requestMatchers("/static/**").permitAll()
                .requestMatchers("/css/**").permitAll()
                .requestMatchers("/js/**").permitAll()
                .requestMatchers("/images/**").permitAll()
                .requestMatchers("/fonts/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                
                // Authenticated user features - require login (HTML pages)
                .requestMatchers("/pages/dashboard.html").authenticated()
                .requestMatchers("/pages/list-book.html").authenticated()
                .requestMatchers("/pages/search-books.html").authenticated()
                .requestMatchers("/pages/search-by-location.html").authenticated()
                .requestMatchers("/pages/my-received-requests.html").authenticated()
                .requestMatchers("/pages/my-sent-requests.html").authenticated()
                .requestMatchers("/pages/chat.html").authenticated()
                .requestMatchers("/pages/swap-history.html").authenticated()
                .requestMatchers("/pages/profile.html").authenticated()
                
                // API endpoints - require authentication
                .requestMatchers("/api/books/**").authenticated()
                .requestMatchers("/api/requests/**").authenticated()
                .requestMatchers("/api/chat/**").authenticated()
                .requestMatchers("/api/profile/**").authenticated()
                .requestMatchers("/api/user/**").authenticated()
                
                // API endpoints for authenticated users
                .requestMatchers("/api/books/**").authenticated()
                .requestMatchers("/api/requests/**").authenticated()
                .requestMatchers("/api/chat/**").authenticated()
                .requestMatchers("/api/profile/**").authenticated()
                
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/pages/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)  // Redirect to homepage after login (with nav bar)
                .failureUrl("/pages/login.html?error=true")
                .permitAll()
            )
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
            );
            
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}