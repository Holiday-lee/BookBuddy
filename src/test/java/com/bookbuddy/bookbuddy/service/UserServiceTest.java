package com.bookbuddy.bookbuddy.service;

import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("hashedPassword123");
    }

    @Test
    void registerUser_Success() {
        // Arrange
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";
        String password = "password123";

        when(userRepository.existsByEmail(email.toLowerCase().trim())).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.registerUser(firstName, lastName, email, password);

        // Assert
        assertNotNull(result);
        assertEquals(firstName, result.getFirstName());
        assertEquals(lastName, result.getLastName());
        assertEquals(email.toLowerCase().trim(), result.getEmail());
        assertEquals("hashedPassword123", result.getPassword());

        verify(userRepository).existsByEmail(email.toLowerCase().trim());
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        // Arrange
        String email = "existing@example.com";
        when(userRepository.existsByEmail(email.toLowerCase().trim())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.registerUser("John", "Doe", email, "password123")
        );

        assertEquals("Email already exists: " + email, exception.getMessage());
        verify(userRepository).existsByEmail(email.toLowerCase().trim());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_InvalidInput_ThrowsException() {
        // Test null firstName
        assertThrows(IllegalArgumentException.class,
            () -> userService.registerUser(null, "Doe", "test@example.com", "password123"));

        // Test empty firstName
        assertThrows(IllegalArgumentException.class,
            () -> userService.registerUser("", "Doe", "test@example.com", "password123"));

        // Test invalid email
        assertThrows(IllegalArgumentException.class,
            () -> userService.registerUser("John", "Doe", "invalid-email", "password123"));

        // Test short password
        assertThrows(IllegalArgumentException.class,
            () -> userService.registerUser("John", "Doe", "test@example.com", "123"));
    }

    @Test
    void findByEmail_Success() {
        // Arrange
        String email = "john.doe@example.com";
        when(userRepository.findByEmail(email.toLowerCase().trim())).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByEmail(email.toLowerCase().trim());
    }

    @Test
    void findByEmail_UserNotFound_ReturnsEmpty() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email.toLowerCase().trim())).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail(email.toLowerCase().trim());
    }

    @Test
    void findById_Success() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findById(userId);
    }

    @Test
    void updateUser_Success() {
        // Arrange
        Long userId = 1L;
        String newFirstName = "Jane";
        String newLastName = "Smith";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(userId, newFirstName, newLastName);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.updateUser(userId, "Jane", "Smith")
        );

        assertEquals("User not found with the id" + userId, exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_Success() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.changePassword(userId, oldPassword, newPassword);

        // Assert
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, testUser.getPassword());
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_WrongOldPassword_ThrowsException() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.changePassword(userId, oldPassword, newPassword)
        );

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, testUser.getPassword());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void emailExists_Success() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.existsByEmail(email.toLowerCase().trim())).thenReturn(true);

        // Act
        boolean result = userService.emailExists(email);

        // Assert
        assertTrue(result);
        verify(userRepository).existsByEmail(email.toLowerCase().trim());
    }
} 