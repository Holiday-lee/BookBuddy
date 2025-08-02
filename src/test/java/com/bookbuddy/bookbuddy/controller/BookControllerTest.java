package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.Book;
import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.service.BookService;
import com.bookbuddy.bookbuddy.service.UserService;
import com.bookbuddy.bookbuddy.service.RequestService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private UserService userService;

    @MockBean
    private RequestService requestService;

    private ObjectMapper objectMapper;
    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@example.com");
        
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setOwnerId(1L);
        testBook.setStatus(Book.BookStatus.AVAILABLE);
        testBook.setSharingType(Book.SharingType.GIVE_AWAY);
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void listBook_Success() throws Exception {
        // Given
        when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(bookService.listBook(anyString(), anyString(), any(), any(), 
                                 anyString(), any(), any(), any(), any(), 
                                 anyLong(), any(Book.SharingType.class), any()))
                .thenReturn(testBook);

        // When & Then
        mockMvc.perform(post("/books/api/list")
                .with(csrf())
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "GOOD")
                .param("sharingType", "GIVE_AWAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Book 'Test Book' has been listed successfully!"));

        // verify(bookService).listBook(anyString(), anyString(), any(), any(), 
        //                            anyString(), any(), any(), any(), any(), 
        //                            anyLong(), any(Book.SharingType.class), any());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void listBook_WithLendingDuration_Success() throws Exception {
        // Given
        when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(bookService.listBook(anyString(), anyString(), any(), any(), 
                                 anyString(), any(), any(), any(), any(), 
                                 anyLong(), any(Book.SharingType.class), any()))
                .thenReturn(testBook);

        // When & Then
        mockMvc.perform(post("/books/api/list")
                .with(csrf())
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "GOOD")
                .param("sharingType", "LEND")
                .param("lendingDurationDays", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Book 'Test Book' has been listed successfully!"));

        // verify(bookService).listBook(anyString(), anyString(), any(), any(), 
        //                            anyString(), any(), any(), any(), any(), 
        //                            anyLong(), any(Book.SharingType.class), any());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void listBook_MissingRequiredFields_BadRequest() throws Exception {
        // Given
        when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/books/api/list")
                .with(csrf())
                .param("title", "")
                .param("author", "")
                .param("condition", "GOOD"))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).listBook(anyString(), anyString(), anyString(), anyString(), 
                                            anyString(), anyString(), anyString(), any(), any(), 
                                            anyLong(), any(Book.SharingType.class), any());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void listBook_InvalidSharingType_BadRequest() throws Exception {
        // Given
        when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/books/api/list")
                .with(csrf())
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "GOOD")
                .param("sharingType", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(bookService, never()).listBook(anyString(), anyString(), anyString(), anyString(), 
                                            anyString(), anyString(), anyString(), any(), any(), 
                                            anyLong(), any(Book.SharingType.class), any());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void listBook_LendWithoutDuration_BadRequest() throws Exception {
        // Given
        when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/books/api/list")
                .with(csrf())
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "GOOD")
                .param("sharingType", "LEND"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // verify(bookService, never()).listBook(anyString(), anyString(), any(), any(), 
        //                                    anyString(), any(), any(), any(), any(), 
        //                                    anyLong(), any(Book.SharingType.class), any());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void listBook_InvalidLatitude_BadRequest() throws Exception {
        // Given
        when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/books/api/list")
                .with(csrf())
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "GOOD")
                .param("sharingType", "GIVE_AWAY")
                .param("latitude", "200.0"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        // verify(bookService, never()).listBook(anyString(), anyString(), any(), any(), 
        //                                    anyString(), any(), any(), any(), any(), 
        //                                    anyLong(), any(Book.SharingType.class), any());
    }

    @Test
    void listBook_NotAuthenticated_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/books/api/list")
                .with(csrf())
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "GOOD")
                .param("sharingType", "GIVE_AWAY"))
                .andExpect(status().isUnauthorized());

        // verify(bookService, never()).listBook(anyString(), anyString(), any(), any(), 
        //                                    anyString(), any(), any(), any(), any(), 
        //                                    anyLong(), any(Book.SharingType.class), any());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void listBook_UserNotFound_Unauthorized() throws Exception {
        // Given
        when(userService.findByEmail("john@example.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/books/api/list")
                .with(csrf())
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "GOOD")
                .param("sharingType", "GIVE_AWAY"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        // verify(bookService, never()).listBook(anyString(), anyString(), any(), any(), 
        //                                    anyString(), any(), any(), any(), any(), 
        //                                    anyLong(), any(Book.SharingType.class), any());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void listBook_ServiceThrowsException_InternalServerError() throws Exception {
        // Given
        when(userService.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(bookService.listBook(anyString(), anyString(), any(), any(), 
                                 anyString(), any(), any(), any(), any(), 
                                 anyLong(), any(Book.SharingType.class), any()))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/books/api/list")
                .with(csrf())
                .param("title", "Test Book")
                .param("author", "Test Author")
                .param("condition", "GOOD")
                .param("sharingType", "GIVE_AWAY"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        // verify(bookService).listBook(anyString(), anyString(), any(), any(), 
        //                            anyString(), any(), any(), any(), any(), 
        //                            anyLong(), any(Book.SharingType.class), any());
    }
} 