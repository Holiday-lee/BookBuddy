/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.Book;
import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.service.BookService;
import com.bookbuddy.bookbuddy.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Controller for handling book-related operations
 * Manages book listing, searching, and other book operations
 * @author holiday
 */
@Controller
@RequestMapping("/books")
public class BookController {
    
    private final BookService bookService;
    private final UserService userService;
    
    @Autowired
    public BookController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }
    
    /**
     * Handle book listing form submission
     * Updated to match the frontend form fields and provide better error handling
     * Uses Spring Security authentication instead of session attributes
     */
    @PostMapping("/list")
    public String listBook(@RequestParam("title") String title,
                          @RequestParam("author") String author,
                          @RequestParam(value = "genre", required = false) String genre,
                          @RequestParam(value = "isbn", required = false) String isbn,
                          @RequestParam("condition") String condition,
                          @RequestParam(value = "description", required = false) String description,
                          @RequestParam(value = "pickupLocation", required = false) String pickupLocation,
                          @RequestParam(value = "latitude", required = false) String latitudeStr,
                          @RequestParam(value = "longitude", required = false) String longitudeStr,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        
        try {
            // Get current user from Spring Security authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please log in to list a book.");
                return "redirect:/pages/login.html";
            }
            
            // Get user by email from authentication
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found. Please log in again.");
                return "redirect:/pages/login.html";
            }
            
            Long userId = userOpt.get().getId();
            
            // Parse coordinates - handle latitude and longitude parsing
            Double latitude = null;
            Double longitude = null;
            
            if (latitudeStr != null && !latitudeStr.trim().isEmpty()) {
                try {
                    latitude = Double.parseDouble(latitudeStr.trim());
                } catch (NumberFormatException e) {
                    // Invalid latitude format - ignore coordinates but don't fail
                    latitude = null;
                }
            }
            
            if (longitudeStr != null && !longitudeStr.trim().isEmpty()) {
                try {
                    longitude = Double.parseDouble(longitudeStr.trim());
                } catch (NumberFormatException e) {
                    // Invalid longitude format - ignore coordinates but don't fail
                    longitude = null;
                }
            }
            
            // List the book using the service layer
            Book newBook = bookService.listBook(title, author, genre, isbn, condition, 
                                               description, pickupLocation, latitude, longitude, userId);
            
            // Success message for user feedback
            redirectAttributes.addFlashAttribute("successMessage", 
                "Book '" + newBook.getTitle() + "' has been listed successfully!");
            
            return "redirect:/pages/list-book.html?success=true";
            
        } catch (IllegalArgumentException e) {
            // Handle validation errors from service layer
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            
            // Keep form data for user convenience (so they don't have to re-enter everything)
            redirectAttributes.addFlashAttribute("title", title);
            redirectAttributes.addFlashAttribute("author", author);
            redirectAttributes.addFlashAttribute("genre", genre);
            redirectAttributes.addFlashAttribute("isbn", isbn);
            redirectAttributes.addFlashAttribute("condition", condition);
            redirectAttributes.addFlashAttribute("description", description);
            redirectAttributes.addFlashAttribute("pickupLocation", pickupLocation);
            
            return "redirect:/pages/list-book.html?error=" + 
                   java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            // Log the actual error for debugging purposes
            System.err.println("Error listing book: " + e.getMessage());
            e.printStackTrace();
            
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to list book. Please try again.");
            
            return "redirect:/pages/list-book.html?error=" + 
                   java.net.URLEncoder.encode("Failed to list book. Please try again.", java.nio.charset.StandardCharsets.UTF_8);
        }
    }
    
    /**
     * API endpoint for AJAX form submission (alternative approach)
     * Returns JSON response instead of redirecting
     */
    @PostMapping("/api/list")
    @ResponseBody
    public ResponseEntity<?> listBookAPI(@RequestParam("title") String title,
                                        @RequestParam("author") String author,
                                        @RequestParam(value = "genre", required = false) String genre,
                                        @RequestParam(value = "isbn", required = false) String isbn,
                                        @RequestParam("condition") String condition,
                                        @RequestParam(value = "description", required = false) String description,
                                        @RequestParam(value = "pickupLocation", required = false) String pickupLocation,
                                        @RequestParam(value = "latitude", required = false) String latitudeStr,
                                        @RequestParam(value = "longitude", required = false) String longitudeStr,
                                        HttpSession session) {
        
        try {
            // Get current user from Spring Security authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Please log in to list a book.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Get user by email from authentication
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found. Please log in again.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long userId = userOpt.get().getId();
            
            // Parse coordinates with proper error handling
            Double latitude = null;
            Double longitude = null;
            
            if (latitudeStr != null && !latitudeStr.trim().isEmpty()) {
                try {
                    latitude = Double.parseDouble(latitudeStr.trim());
                } catch (NumberFormatException e) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Invalid latitude format");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            if (longitudeStr != null && !longitudeStr.trim().isEmpty()) {
                try {
                    longitude = Double.parseDouble(longitudeStr.trim());
                } catch (NumberFormatException e) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Invalid longitude format");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // List the book using the service layer
            Book newBook = bookService.listBook(title, author, genre, isbn, condition, 
                                               description, pickupLocation, latitude, longitude, userId);
            
            // Return success response with book details
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Book '" + newBook.getTitle() + "' has been listed successfully!");
            response.put("bookId", newBook.getId());
            response.put("book", newBook);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
                
        } catch (Exception e) {
            // Log the actual error for debugging
            System.err.println("Error listing book: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to list book. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // ... (keep all your existing API methods from the original controller)
    
    /**
     * API: Get all available books
     * Returns list of all books that are currently available for swap
     */
    @GetMapping("/api/all")
    @ResponseBody
    public List<Book> getAllBooks() {
        return bookService.findAllAvailableBooks();
    }
    
    /**
     * API: Search books
     * Searches books by title, author, or genre
     * @param query Search query string
     * @return List of matching books
     */
    @GetMapping("/api/search")
    @ResponseBody
    public List<Book> searchBooks(@RequestParam(value = "q", required = false) String query) {
        return bookService.searchBooks(query);
    }
    
    /**
     * API: Find books nearby
     * Finds books within specified radius of given coordinates
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param radius Search radius in kilometers
     * @return List of books within radius
     */
    @GetMapping("/api/nearby")
    @ResponseBody
    public List<Book> findNearbyBooks(@RequestParam("lat") double latitude,
                                     @RequestParam("lng") double longitude,
                                     @RequestParam(value = "radius", defaultValue = "5") double radius) {
        return bookService.findBooksNearby(latitude, longitude, radius);
    }
    
    /**
     * API: Get book details
     * Returns detailed information about a specific book
     * @param id Book ID
     * @return Book details or 404 if not found
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getBookDetails(@PathVariable Long id) {
        Optional<Book> bookOpt = bookService.findById(id);
        if (bookOpt.isPresent()) {
            return ResponseEntity.ok(bookOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * API: Get current user's books
     * Returns all books owned by the currently logged-in user
     * @return List of user's books
     */
    @GetMapping("/api/my-books")
    @ResponseBody
    public ResponseEntity<?> getMyBooks() {
        try {
            // Get current user from Spring Security authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Please log in to view your books.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Get user by email from authentication
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found. Please log in again.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long userId = userOpt.get().getId();
            
            // Get user's books
            List<Book> userBooks = bookService.getBooksByOwner(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("books", userBooks);
            response.put("count", userBooks.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to load your books.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * API: Delete a book (only by owner)
     * Allows users to delete their own books
     * @param id Book ID
     * @return Success/error response
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            // Get current user from Spring Security authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Please log in to delete books.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Get user by email from authentication
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found. Please log in again.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long userId = userOpt.get().getId();
            
            // Delete the book (service will check ownership)
            bookService.deleteBook(id, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Book deleted successfully.");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete book.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * API: Update book availability
     * Allows users to mark their books as available/unavailable
     * @param id Book ID
     * @param available New availability status
     * @param session HTTP session
     * @return Success/error response
     */
    @PostMapping("/api/{id}/availability")
    @ResponseBody
    public ResponseEntity<?> updateBookAvailability(@PathVariable Long id,
                                                   @RequestParam("available") boolean available,
                                                   HttpSession session) {
        try {
            // Get current user from Spring Security authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Please log in to update book availability.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Get user by email from authentication
            String email = authentication.getName();
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found. Please log in again.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long userId = userOpt.get().getId();
            
            // Verify ownership before updating
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Book not found.");
                return ResponseEntity.notFound().build();
            }
            
            Book book = bookOpt.get();
            if (!book.getOwnerId().equals(userId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "You can only modify your own books.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Update book availability
            Book updatedBook;
            if (available) {
                updatedBook = bookService.markAsAvailable(id);
            } else {
                updatedBook = bookService.markAsUnavailable(id);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Book availability updated successfully.");
            response.put("book", updatedBook);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update book availability.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}