package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.Book;
import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.service.BookService;
import com.bookbuddy.bookbuddy.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@WithMockUser(username = "john.doe@example.com")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private UserService userService;

    @MockBean
    private com.bookbuddy.bookbuddy.service.RequestService requestService;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setGenre("Fiction");
        testBook.setCondition("Good");
        testBook.setOwnerId(1L);
        testBook.setSharingType(Book.SharingType.SWAP);
        testBook.setStatus(Book.BookStatus.AVAILABLE);
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void listBook_Success() throws Exception {
        // Arrange
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(bookService.listBook(anyString(), anyString(), anyString(), anyString(), 
                                 anyString(), anyString(), anyString(), any(), any(), 
                                 any(), any(), any())).thenReturn(testBook);

        // Act & Assert
        mockMvc.perform(post("/books/api/list")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("genre", "Fiction")
                .param("condition", "Good")
                .param("sharingType", "SWAP")
                .param("latitude", "40.7128")
                .param("longitude", "-74.0060")
                .param("pickupLocation", "Test Location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Book 'Test Book' has been listed successfully!"))
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.book").exists());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void listBook_WithLendingDuration_Success() throws Exception {
        // Arrange
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(bookService.listBook(anyString(), anyString(), anyString(), anyString(), 
                                 anyString(), anyString(), anyString(), any(), any(), 
                                 any(), any(), any())).thenReturn(testBook);

        // Act & Assert
        mockMvc.perform(post("/books/api/list")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "Good")
                .param("sharingType", "LEND")
                .param("lendingDurationDays", "14")
                .param("latitude", "40.7128")
                .param("longitude", "-74.0060")
                .param("pickupLocation", "Test Location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void listBook_MissingRequiredFields_BadRequest() throws Exception {
        // Arrange
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert - Missing title
        mockMvc.perform(post("/books/api/list")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("author", "Test Author")
                .param("condition", "Good")
                .param("sharingType", "SWAP")
                .param("latitude", "40.7128")
                .param("longitude", "-74.0060"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void listBook_InvalidSharingType_BadRequest() throws Exception {
        // Arrange
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(post("/books/api/list")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "Good")
                .param("sharingType", "INVALID_TYPE")
                .param("latitude", "40.7128")
                .param("longitude", "-74.0060"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid sharing type selected."));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void listBook_LendWithoutDuration_BadRequest() throws Exception {
        // Arrange
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(post("/books/api/list")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "Good")
                .param("sharingType", "LEND")
                .param("latitude", "40.7128")
                .param("longitude", "-74.0060"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Lending duration is required for lend books."));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void listBook_InvalidLatitude_BadRequest() throws Exception {
        // Arrange
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(post("/books/api/list")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "Good")
                .param("sharingType", "SWAP")
                .param("latitude", "invalid_latitude")
                .param("longitude", "-74.0060"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid latitude format"));
    }

    @Test
    void listBook_NotAuthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/books/api/list")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "Good")
                .param("sharingType", "SWAP"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Please log in to list a book."));
    }

    @Test
    @WithMockUser(username = "nonexistent@example.com")
    void listBook_UserNotFound_Unauthorized() throws Exception {
        // Arrange
        when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/books/api/list")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "Good")
                .param("sharingType", "SWAP"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found. Please log in again."));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void listBook_ServiceThrowsException_InternalServerError() throws Exception {
        // Arrange
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(bookService.listBook(anyString(), anyString(), anyString(), anyString(), 
                                 anyString(), anyString(), anyString(), any(), any(), 
                                 any(), any(), any())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/books/api/list")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "Good")
                .param("sharingType", "SWAP")
                .param("latitude", "40.7128")
                .param("longitude", "-74.0060"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to list book. Please try again."));
    }
} 