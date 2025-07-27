package com.bookbuddy.bookbuddy.service;

import com.bookbuddy.bookbuddy.model.Book;
import com.bookbuddy.bookbuddy.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookServiceIntegrationTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void listBook_Success() {
        // Arrange
        String title = "Test Book";
        String author = "Test Author";
        String genre = "Fiction";
        String isbn = "1234567890";
        String condition = "Good";
        String description = "A test book";
        String pickupLocation = "Test Location";
        Double pickupLatitude = 40.7128;
        Double pickupLongitude = -74.0060;
        Long ownerId = 1L;
        Book.SharingType sharingType = Book.SharingType.SWAP;
        Integer lendingDurationDays = null;

        // Act
        Book result = bookService.listBook(title, author, genre, isbn, condition, description,
                                         pickupLocation, pickupLatitude, pickupLongitude, ownerId,
                                         sharingType, lendingDurationDays);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(author, result.getAuthor());
        assertEquals(genre, result.getGenre());
        assertEquals(isbn, result.getIsbn());
        assertEquals(condition, result.getCondition());
        assertEquals(description, result.getDescription());
        assertEquals(pickupLocation, result.getPickupLocation());
        assertEquals(pickupLatitude, result.getPickupLatitude());
        assertEquals(pickupLongitude, result.getPickupLongitude());
        assertEquals(ownerId, result.getOwnerId());
        assertEquals(sharingType, result.getSharingType());
        assertEquals(Book.BookStatus.AVAILABLE, result.getStatus());

        // Verify it was saved to database
        assertTrue(bookRepository.findById(result.getId()).isPresent());
    }

    @Test
    void listBook_WithLendingDuration_Success() {
        // Arrange
        String title = "Lend Book";
        String author = "Lend Author";
        String genre = "Non-Fiction";
        String isbn = "9876543210";
        String condition = "Excellent";
        String description = "A book for lending";
        String pickupLocation = "Library";
        Double pickupLatitude = 40.7589;
        Double pickupLongitude = -73.9851;
        Long ownerId = 2L;
        Book.SharingType sharingType = Book.SharingType.LEND;
        Integer lendingDurationDays = 30;

        // Act
        Book result = bookService.listBook(title, author, genre, isbn, condition, description,
                                         pickupLocation, pickupLatitude, pickupLongitude, ownerId,
                                         sharingType, lendingDurationDays);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(sharingType, result.getSharingType());
        assertEquals(lendingDurationDays, result.getLendingDurationDays());
        assertEquals(Book.BookStatus.AVAILABLE, result.getStatus());
    }

    @Test
    void listBook_MissingRequiredFields_ThrowsException() {
        // Test missing title
        assertThrows(IllegalArgumentException.class, () ->
            bookService.listBook(null, "Author", "Genre", "ISBN", "Condition", "Description",
                               "Location", 40.0, -74.0, 1L, Book.SharingType.SWAP, null));

        // Test missing author
        assertThrows(IllegalArgumentException.class, () ->
            bookService.listBook("Title", null, "Genre", "ISBN", "Condition", "Description",
                               "Location", 40.0, -74.0, 1L, Book.SharingType.SWAP, null));

        // Test missing condition
        assertThrows(IllegalArgumentException.class, () ->
            bookService.listBook("Title", "Author", "Genre", "ISBN", null, "Description",
                               "Location", 40.0, -74.0, 1L, Book.SharingType.SWAP, null));
    }

    @Test
    void listBook_LendWithoutDuration_ThrowsException() {
        // Arrange
        String title = "Lend Book";
        String author = "Lend Author";
        String condition = "Good";
        Long ownerId = 1L;
        Book.SharingType sharingType = Book.SharingType.LEND;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            bookService.listBook(title, author, "Genre", "ISBN", condition, "Description",
                               "Location", 40.0, -74.0, ownerId, sharingType, null));
    }

    @Test
    void listBook_InvalidSharingType_ThrowsException() {
        // This test would require a custom enum value, but we can test the validation
        // by ensuring the service properly validates the sharing type
        String title = "Test Book";
        String author = "Test Author";
        String condition = "Good";
        Long ownerId = 1L;

        // Test with null sharing type
        assertThrows(IllegalArgumentException.class, () ->
            bookService.listBook(title, author, "Genre", "ISBN", condition, "Description",
                               "Location", 40.0, -74.0, ownerId, null, null));
    }
} 