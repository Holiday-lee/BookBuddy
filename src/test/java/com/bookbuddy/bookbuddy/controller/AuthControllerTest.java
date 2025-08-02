package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("hashedPassword123");
    }

    @Test
    void registerUser_Success() throws Exception {
        // Given
        when(userService.registerUser("Jane", "Smith", "jane@example.com", "password123"))
                .thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/register")
                .param("firstName", "Jane")
                .param("lastName", "Smith")
                .param("email", "jane@example.com")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pages/login.html"));

        verify(userService).registerUser("Jane", "Smith", "jane@example.com", "password123");
    }

    @Test
    void registerUser_InvalidInput() throws Exception {
        // Given
        when(userService.registerUser(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid input"));

        // When & Then
        mockMvc.perform(post("/register")
                .param("firstName", "")
                .param("lastName", "Smith")
                .param("email", "invalid-email")
                .param("password", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pages/register.html"));

        verify(userService).registerUser("", "Smith", "invalid-email", "123");
    }

    @Test
    void registerUser_EmailAlreadyExists() throws Exception {
        // Given
        when(userService.registerUser(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/register")
                .param("firstName", "Jane")
                .param("lastName", "Smith")
                .param("email", "john@example.com")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pages/register.html"));

        verify(userService).registerUser("Jane", "Smith", "john@example.com", "password123");
    }

    @Test
    void getCurrentUser_Authenticated() throws Exception {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("john@example.com");
            when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

            // When & Then
            mockMvc.perform(get("/api/current-user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authenticated").value(true))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.email").value("john@example.com"));

            verify(userService).findByEmail("john@example.com");
        }
    }

    @Test
    void getCurrentUser_NotAuthenticated() throws Exception {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            // When & Then
            mockMvc.perform(get("/api/current-user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authenticated").value(false));

            verify(userService, never()).findByEmail(anyString());
        }
    }

    @Test
    void getCurrentUser_AnonymousUser() throws Exception {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("anonymousUser");
            when(userService.findByEmail("anonymousUser")).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/current-user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authenticated").value(false));

            verify(userService).findByEmail("anonymousUser");
        }
    }

    @Test
    void getCurrentUser_UserNotFound() throws Exception {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("john@example.com");
            when(userService.findByEmail("john@example.com")).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/current-user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authenticated").value(false));

            verify(userService).findByEmail("john@example.com");
        }
    }

    @Test
    void changePassword_Success() throws Exception {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("john@example.com");
            when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
            doNothing().when(userService).changePassword(1L, "oldpassword", "newpassword123");

            // When & Then
            mockMvc.perform(post("/api/change-password")
                    .param("currentPassword", "oldpassword")
                    .param("newPassword", "newpassword123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Password changed successfully!"));

            verify(userService).changePassword(1L, "oldpassword", "newpassword123");
        }
    }

    @Test
    void changePassword_NotAuthenticated() throws Exception {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            // When & Then
            mockMvc.perform(post("/api/change-password")
                    .param("currentPassword", "oldpassword")
                    .param("newPassword", "newpassword123"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Please log in to change your password."));

            verify(userService, never()).changePassword(any(), anyString(), anyString());
        }
    }

    @Test
    void changePassword_InvalidCurrentPassword() throws Exception {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("john@example.com");
            when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
            doThrow(new IllegalArgumentException("Current password is incorrect"))
                    .when(userService).changePassword(1L, "wrongpassword", "newpassword123");

            // When & Then
            mockMvc.perform(post("/api/change-password")
                    .param("currentPassword", "wrongpassword")
                    .param("newPassword", "newpassword123"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Current password is incorrect"));

            verify(userService).changePassword(1L, "wrongpassword", "newpassword123");
        }
    }
} 