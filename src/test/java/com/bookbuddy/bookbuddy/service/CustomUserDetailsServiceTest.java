package com.bookbuddy.bookbuddy.service;

import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("hashedPassword123");
    }

    @Test
    void loadUserByUsername_Success() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername("john@example.com");

        // Then
        assertNotNull(result);
        assertEquals("john@example.com", result.getUsername());
        assertEquals("hashedPassword123", result.getPassword());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        assertTrue(result.isEnabled());
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> 
            customUserDetailsService.loadUserByUsername("nonexistent@example.com"));
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void loadUserByUsername_EmailCaseInsensitive() {
        // Given
        when(userRepository.findByEmail("JOHN@EXAMPLE.COM")).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername("JOHN@EXAMPLE.COM");

        // Then
        assertNotNull(result);
        assertEquals("john@example.com", result.getUsername());
        verify(userRepository).findByEmail("JOHN@EXAMPLE.COM");
    }

    @Test
    void loadUserByUsername_EmailWithSpaces() {
        // Given
        when(userRepository.findByEmail(" john@example.com ")).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(" john@example.com ");

        // Then
        assertNotNull(result);
        assertEquals("john@example.com", result.getUsername());
        verify(userRepository).findByEmail(" john@example.com ");
    }

    @Test
    void loadUserByUsername_EmptyEmail() {
        // Given
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> 
            customUserDetailsService.loadUserByUsername(""));
        verify(userRepository).findByEmail("");
    }

    @Test
    void loadUserByUsername_NullEmail() {
        // Given
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> 
            customUserDetailsService.loadUserByUsername(null));
        verify(userRepository).findByEmail(null);
    }

    @Test
    void loadUserByUsername_UserWithNullPassword() {
        // Given
        testUser.setPassword(null);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            customUserDetailsService.loadUserByUsername("john@example.com"));
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void loadUserByUsername_UserWithEmptyPassword() {
        // Given
        testUser.setPassword("");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername("john@example.com");

        // Then
        assertNotNull(result);
        assertEquals("john@example.com", result.getUsername());
        assertEquals("", result.getPassword());
        verify(userRepository).findByEmail("john@example.com");
    }
} 