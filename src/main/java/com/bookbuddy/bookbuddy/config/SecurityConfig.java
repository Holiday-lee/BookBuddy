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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 
 * Spring Boot Security configuration to control page access
 * Public users can access home and search functionality
 * Authenticated users get access to additional features
 */
@Configuration 
@EnableWebSecurity
public class SecurityConfig{

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
            .authorizeHttpRequests(auth->auth
                // Public access - available to everyone
                .requestMatchers("/", "/index.html", "/pages/login.html", "/pages/register.html").permitAll()
                
                // Authentication endpoints - public access
                .requestMatchers("/register", "/login", "/logout").permitAll()
                .requestMatchers("/api/check-email", "/api/current-user").permitAll()
                
                // Static resources - allow all
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico").permitAll()
                
                // Public search functionality 
                .requestMatchers("/pages/search-books.html", "/pages/user-profile.html").permitAll()
                .requestMatchers("/search/**", "/api/books/search/**", "/api/books/nearby").permitAll()
                .requestMatchers("/books/api/search", "/books/api/user/**").permitAll()
                
                // Protected pages - require authentication
                .requestMatchers("/pages/dashboard.html", "/pages/list-book.html", 
                               "/pages/my-received-requests.html", "/pages/my-sent-requests.html",
                               "/pages/chat.html", "/pages/swap-history.html", "/pages/profile.html",
                               "/pages/search-by-location.html", "/pages/my-books.html", 
                               "/pages/settings.html").authenticated()
                
                // Protected API endpoints
                .requestMatchers("/api/books/create", "/api/books/update/**", "/api/books/delete/**", "/books/api/*/delete").authenticated()
                .requestMatchers("/books/list", "/books/api/list", "/books/api/my-books", "/books/api/*/availability", "/books/api/*", "/books/api/*/delete", "/books/api/*/sharing-type", "/books/api/swappable").authenticated()
                .requestMatchers("/api/requests/**", "/api/chat/**", "/api/profile/**", "/api/user/**").authenticated()
                .requestMatchers("/requests/api/**").authenticated()
                .requestMatchers("/ws/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Custom form login configuration
            .formLogin(form->form
                .loginPage("/pages/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)  // Redirect to home after successful login
                .failureUrl("/pages/login.html?error=true")
                .permitAll()
            )
            
            // Logout configuration
            .logout(logout->logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // Session management
            .sessionManagement(session->session
                .maximumSessions(5)  // Allow up to 5 sessions per user
                .maxSessionsPreventsLogin(false)
            )
            
            // CORS configuration
            .cors(cors->cors.configurationSource(corsConfigurationSource()))
            
            // Disable CSRF for easier testing (re-enable in production)
            .csrf(csrf->csrf.disable());
            
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}