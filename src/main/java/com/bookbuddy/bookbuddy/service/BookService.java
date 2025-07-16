/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.service;

import com.bookbuddy.bookbuddy.model.Book;
import com.bookbuddy.bookbuddy.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 *
 * @author holiday
 */
@Service
@Transactional
public class BookService {
    
    private final BookRepository bookRepository;
    
    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    /**
     * List a new book
     */
    public Book listBook(String title, String author, String genre, String isbn, 
                        String condition, String description, String pickupLocation,
                        Double pickupLatitude, Double pickupLongitude, Long ownerId,
                        Book.SharingType sharingType, Integer lendingDurationDays) {
        
        // Validate required fields
        validateBookInput(title, author, condition, ownerId, sharingType);
        
        // Create new book
        Book book = new Book();
        book.setTitle(title.trim());
        book.setAuthor(author.trim());
        book.setGenre(genre != null ? genre.trim() : null);
        book.setIsbn(isbn != null ? isbn.trim() : null);
        book.setCondition(condition.trim());
        book.setDescription(description != null ? description.trim() : null);
        book.setPickupLocation(pickupLocation != null ? pickupLocation.trim() : null);
        book.setPickupLatitude(pickupLatitude);
        book.setPickupLongitude(pickupLongitude);
        book.setOwnerId(ownerId);
        book.setSharingType(sharingType);
        book.setStatus(Book.BookStatus.AVAILABLE);
        
        // Set lending duration for lend books
        if (sharingType == Book.SharingType.LEND) {
            if (lendingDurationDays == null || lendingDurationDays <= 0) {
                throw new IllegalArgumentException("Lending duration is required for lend books");
            }
            book.setLendingDurationDays(lendingDurationDays);
        }
        
        return bookRepository.save(book);
    }
    
    /**
     * Find book by ID
     */
    @Transactional(readOnly = true)
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }
    
    /**
     * Find all available books
     */
    @Transactional(readOnly = true)
    public List<Book> findAllAvailableBooks() {
        return bookRepository.findByStatus(Book.BookStatus.AVAILABLE);
    }
    
    /**
     * Find all books (for debugging)
     */
    @Transactional(readOnly = true)
    public List<Book> findAllBooks() {
        return bookRepository.findAll();
    }
    
    /**
     * Find books by owner
     */
    @Transactional(readOnly = true)
    public List<Book> findBooksByOwner(Long ownerId) {
        return bookRepository.findByOwnerId(ownerId);
    }
    
    /**
     * Find available books by owner
     */
    @Transactional(readOnly = true)
    public List<Book> findAvailableBooksByOwner(Long ownerId) {
        return bookRepository.findByOwnerIdAndStatus(ownerId, Book.BookStatus.AVAILABLE);
    }
    
    /**
     * Search books by query (title, author, or genre)
     */
    @Transactional(readOnly = true)
    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAllAvailableBooks();
        }
        return bookRepository.searchBooks(query.trim());
    }
    
    /**
     * Find books within distance from user location
     */
    @Transactional(readOnly = true)
    public List<Book> findBooksNearby(double latitude, double longitude, double radiusKm) {
        List<Book> booksWithinRadius = bookRepository.findBooksWithinDistance(latitude, longitude, radiusKm);
        
        // Sort by distance (closest first)
        return booksWithinRadius.stream()
                .sorted(Comparator.comparingDouble(book -> book.distanceTo(latitude, longitude)))
                .collect(Collectors.toList());
    }
    
    /**
     * Advanced search with multiple criteria
     */
    @Transactional(readOnly = true)
    public List<Book> findBooksByCriteria(String title, String author, String genre, String condition, Book.SharingType sharingType) {
        return bookRepository.findBooksByCriteria(
            isNullOrEmpty(title) ? null : title,
            isNullOrEmpty(author) ? null : author,
            isNullOrEmpty(genre) ? null : genre,
            isNullOrEmpty(condition) ? null : condition,
            sharingType
        );
    }
    
    /**
     * Update book details
     */
    public Book updateBook(Long bookId, String title, String author, String genre, 
                          String isbn, String condition, String description, 
                          String pickupLocation) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));
        
        // Update fields if provided
        if (title != null && !title.trim().isEmpty()) {
            book.setTitle(title.trim());
        }
        if (author != null && !author.trim().isEmpty()) {
            book.setAuthor(author.trim());
        }
        if (genre != null) {
            book.setGenre(genre.trim().isEmpty() ? null : genre.trim());
        }
        if (isbn != null) {
            book.setIsbn(isbn.trim().isEmpty() ? null : isbn.trim());
        }
        if (condition != null && !condition.trim().isEmpty()) {
            book.setCondition(condition.trim());
        }
        if (description != null) {
            book.setDescription(description.trim().isEmpty() ? null : description.trim());
        }
        if (pickupLocation != null) {
            book.setPickupLocation(pickupLocation.trim().isEmpty() ? null : pickupLocation.trim());
        }
        
        return bookRepository.save(book);
    }
    
    /**
     * Update book location
     */
    public Book updateBookLocation(Long bookId, String pickupLocation, 
                                  Double pickupLatitude, Double pickupLongitude) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));
        
        book.setPickupLocation(pickupLocation);
        book.setPickupLatitude(pickupLatitude);
        book.setPickupLongitude(pickupLongitude);
        
        return bookRepository.save(book);
    }
    
    /**
     * Mark book as unavailable (when request is made)
     */
    public Book markAsUnavailable(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));
        
        book.setStatus(Book.BookStatus.UNAVAILABLE);
        return bookRepository.save(book);
    }
    
    /**
     * Mark book as available again
     */
    public Book markAsAvailable(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));
        
        book.setStatus(Book.BookStatus.AVAILABLE);
        return bookRepository.save(book);
    }
    
    /**
     * Mark book as exchange in progress
     */
    public Book markAsExchangeInProgress(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));
        
        book.setStatus(Book.BookStatus.EXCHANGE_IN_PROGRESS);
        return bookRepository.save(book);
    }
    
    /**
     * Mark book as currently lent out
     */
    public Book markAsCurrentlyLentOut(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));
        
        book.setStatus(Book.BookStatus.CURRENTLY_LENT_OUT);
        return bookRepository.save(book);
    }
    
    /**
     * Mark book as given away
     */
    public Book markAsGivenAway(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));
        
        book.setStatus(Book.BookStatus.GIVEN_AWAY);
        return bookRepository.save(book);
    }
    
    /**
     * Mark book as swapped
     */
    public Book markAsSwapped(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));
        
        book.setStatus(Book.BookStatus.SWAPPED);
        return bookRepository.save(book);
    }
    
    /**
     * Delete book (only by owner)
     */
    public void deleteBook(Long bookId, Long ownerId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));
        
        if (!book.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("You can only delete your own books");
        }
        
        bookRepository.delete(book);
    }
    
    /**
     * Get recent books
     */
    @Transactional(readOnly = true)
    public List<Book> getRecentBooks() {
        return bookRepository.findRecentBooks();
    }
    
    /**
     * Get book statistics
     */
    @Transactional(readOnly = true)
    public BookStats getBookStats() {
        long totalBooks = bookRepository.countByStatus(Book.BookStatus.AVAILABLE);
        List<Book> booksWithLocation = bookRepository.findBooksWithLocation();
        
        return new BookStats(totalBooks, booksWithLocation.size());
    }
    
    /**
     * Find books available for give away
     */
    @Transactional(readOnly = true)
    public List<Book> findAvailableGiveAwayBooks() {
        return bookRepository.findAvailableGiveAwayBooks();
    }
    
    /**
     * Find books available for lending
     */
    @Transactional(readOnly = true)
    public List<Book> findAvailableLendBooks() {
        return bookRepository.findAvailableLendBooks();
    }
    
    /**
     * Find books available for swapping
     */
    @Transactional(readOnly = true)
    public List<Book> findAvailableSwapBooks() {
        return bookRepository.findAvailableSwapBooks();
    }
    
    /**
     * Find swappable books by owner (for swap requests)
     */
    @Transactional(readOnly = true)
    public List<Book> findSwappableBooksByOwner(Long ownerId) {
        return bookRepository.findSwappableBooksByOwner(ownerId);
    }
    
    /**
     * Validate book input
     */
    private void validateBookInput(String title, String author, String condition, Long ownerId, Book.SharingType sharingType) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author is required");
        }
        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Condition is required");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID is required");
        }
        if (sharingType == null) {
            throw new IllegalArgumentException("Sharing type is required");
        }
    }
    
    /**
     * Helper method to check if string is null or empty
     */
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    public static class BookStats {
        private final long totalBooks;
        private final long booksWithLocation;
        
        public BookStats(long totalBooks, long booksWithLocation) {
            this.totalBooks = totalBooks;
            this.booksWithLocation = booksWithLocation;
        }
        
        public long getTotalBooks() { return totalBooks; }
        public long getBooksWithLocation() { return booksWithLocation; }
    }
}
