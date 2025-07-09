/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.Book;
import com.bookbuddy.bookbuddy.service.BookService;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author holiday
 */
@Controller
@RequestMapping("/books")
public class BookController {
    
    private final BookService bookService;
    
    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    
    /**
     * Handle book listing form submission
     * Updated to match the frontend form fields and provide better error handling
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
            // Get current user from session
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please log in to list a book.");
                return "redirect:/pages/login.html";
            }
            
            // Parse coordinates
            Double latitude = null;
            Double longitude = null;
            
            if (latitudeStr != null && !latitudeStr.trim().isEmpty()) {
                try {
                    latitude = Double.parseDouble(latitudeStr.trim());
                } catch (NumberFormatException e) {
                    // Invalid latitude format - ignore coordinates
                    latitude = null;
                }
            }
            
            if (longitudeStr != null && !longitudeStr.trim().isEmpty()) {
                try {
                    longitude = Double.parseDouble(longitudeStr.trim());
                } catch (NumberFormatException e) {
                    // Invalid longitude format - ignore coordinates
                    longitude = null;
                }
            }
            
            // List the book
            Book newBook = bookService.listBook(title, author, genre, isbn, condition, 
                                               description, pickupLocation, latitude, longitude, userId);
            
            // Success message
            redirectAttributes.addFlashAttribute("successMessage", 
                "Book '" + newBook.getTitle() + "' has been listed successfully!");
            
            return "redirect:/pages/list-book.html?success=true";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            
            // Keep form data for user convenience
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
            // Log the actual error for debugging
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
            // Get current user from session
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Please log in to list a book.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Parse coordinates
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
            
            // List the book
            Book newBook = bookService.listBook(title, author, genre, isbn, condition, 
                                               description, pickupLocation, latitude, longitude, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Book '" + newBook.getTitle() + "' has been listed successfully!");
            response.put("bookId", newBook.getId());
            response.put("book", newBook);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
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
     */
    @GetMapping("/api/all")
    @ResponseBody
    public List<Book> getAllBooks() {
        return bookService.findAllAvailableBooks();
    }
    
    /**
     * API: Search books
     */
    @GetMapping("/api/search")
    @ResponseBody
    public List<Book> searchBooks(@RequestParam(value = "q", required = false) String query) {
        return bookService.searchBooks(query);
    }
    
    /**
     * API: Find books nearby
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
     * API: Get user's books
     */
    @GetMapping("/api/my-books")
    @ResponseBody
    public ResponseEntity<?> getMyBooks(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        Map<String, Object> response = new HashMap<>();
        
        if (userId == null) {
            response.put("authenticated", false);
            response.put("message", "Please log in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        List<Book> myBooks = bookService.findBooksByOwner(userId);
        response.put("authenticated", true);
        response.put("books", myBooks);
        response.put("count", myBooks.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API: Update book availability
     */
    @PostMapping("/api/{id}/availability")
    @ResponseBody
    public ResponseEntity<?> updateBookAvailability(@PathVariable Long id,
                                                   @RequestParam("available") boolean available,
                                                   HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            Map<String, Object> response = new HashMap<>();
            
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Please log in");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Verify ownership
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Book not found");
                return ResponseEntity.notFound().build();
            }
            
            Book book = bookOpt.get();
            if (!book.getOwnerId().equals(userId)) {
                response.put("success", false);
                response.put("message", "You can only modify your own books");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Update availability
            if (available) {
                book = bookService.markAsAvailable(id);
            } else {
                book = bookService.markAsUnavailable(id);
            }
            
            response.put("success", true);
            response.put("message", "Book availability updated successfully");
            response.put("book", book);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update book availability");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}