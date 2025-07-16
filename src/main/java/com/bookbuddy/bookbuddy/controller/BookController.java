/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.controller;

import com.bookbuddy.bookbuddy.model.Book;
import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.model.Request;
import com.bookbuddy.bookbuddy.service.BookService;
import com.bookbuddy.bookbuddy.service.UserService;
import com.bookbuddy.bookbuddy.service.RequestService;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 *
 * @author holiday
 */
@Controller
@RequestMapping("/books")
public class BookController {
    
    private final BookService bookService;
    private final UserService userService;
    private final RequestService requestService;
    
    @Autowired
    public BookController(BookService bookService, UserService userService, RequestService requestService) {
        this.bookService = bookService;
        this.userService = userService;
        this.requestService = requestService;
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
                                        @RequestParam("sharingType") String sharingTypeStr,
                                        @RequestParam(value = "lendingDurationDays", required = false) String lendingDurationStr,
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
            
            // Parse sharing type
            Book.SharingType sharingType;
            try {
                sharingType = Book.SharingType.valueOf(sharingTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid sharing type selected.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Parse lending duration
            Integer lendingDurationDays = null;
            if (sharingType == Book.SharingType.LEND) {
                if (lendingDurationStr == null || lendingDurationStr.trim().isEmpty()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Lending duration is required for lend books.");
                    return ResponseEntity.badRequest().body(response);
                }
                try {
                    lendingDurationDays = Integer.parseInt(lendingDurationStr.trim());
                    if (lendingDurationDays <= 0) {
                        throw new NumberFormatException("Duration must be positive");
                    }
                } catch (NumberFormatException e) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Invalid lending duration.");
                    return ResponseEntity.badRequest().body(response);
                }
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
                                               description, pickupLocation, latitude, longitude, userId,
                                               sharingType, lendingDurationDays);
            
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
    public ResponseEntity<?> searchBooks(@RequestParam(value = "q", required = false) String query,
                                       @RequestParam(value = "lat", required = false) Double latitude,
                                       @RequestParam(value = "lng", required = false) Double longitude,
                                       @RequestParam(value = "radius", required = false) Double radius,
                                       @RequestParam(value = "sharingType", required = false) String sharingTypeStr) {
        
        List<Book> books;
        
        // If location parameters are provided, do location-based search
        if (latitude != null && longitude != null && radius != null) {
            List<Book> nearbyBooks = bookService.findBooksNearby(latitude, longitude, radius);
            
            // If there's also a text query, filter the nearby books
            if (query != null && !query.trim().isEmpty()) {
                List<Book> textSearchResults = bookService.searchBooks(query);
                Set<Long> textSearchIds = textSearchResults.stream()
                    .map(Book::getId)
                    .collect(Collectors.toSet());
                
                books = nearbyBooks.stream()
                    .filter(book -> textSearchIds.contains(book.getId()))
                    .collect(Collectors.toList());
            } else {
                books = nearbyBooks;
            }
            
            // Filter by sharing type if specified
            if (sharingTypeStr != null && !sharingTypeStr.trim().isEmpty()) {
                try {
                    Book.SharingType sharingType = Book.SharingType.valueOf(sharingTypeStr.toUpperCase());
                    books = books.stream()
                        .filter(book -> book.getSharingType() == sharingType)
                        .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Invalid sharing type, return empty list
                    books = new ArrayList<>();
                }
            }
            
            // Create enhanced response with distance information and owner details
            List<Map<String, Object>> enhancedBooks = books.stream()
                .map(book -> {
                    Map<String, Object> bookMap = new HashMap<>();
                    bookMap.put("id", book.getId());
                    bookMap.put("title", book.getTitle());
                    bookMap.put("author", book.getAuthor());
                    bookMap.put("genre", book.getGenre());
                    bookMap.put("isbn", book.getIsbn());
                    bookMap.put("condition", book.getCondition());
                    bookMap.put("description", book.getDescription());
                    bookMap.put("sharingType", book.getSharingType());
                    bookMap.put("status", book.getStatus());
                    bookMap.put("lendingDurationDays", book.getLendingDurationDays());
                    bookMap.put("ownerId", book.getOwnerId());
                    bookMap.put("createdAt", book.getCreatedAt());
                    bookMap.put("updatedAt", book.getUpdatedAt());
                    bookMap.put("displayLocation", book.getDisplayLocationWithDistance(latitude, longitude));
                    
                    // Add owner information
                    Optional<User> ownerOpt = userService.findById(book.getOwnerId());
                    if (ownerOpt.isPresent()) {
                        User owner = ownerOpt.get();
                        Map<String, Object> ownerInfo = new HashMap<>();
                        ownerInfo.put("id", owner.getId());
                        ownerInfo.put("firstName", owner.getFirstName());
                        ownerInfo.put("lastName", owner.getLastName());
                        ownerInfo.put("fullName", owner.getFullName());
                        bookMap.put("owner", ownerInfo);
                    }
                    
                    return bookMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(enhancedBooks);
        }
        
        // Otherwise, do regular text search with sharing type filter
        books = bookService.searchBooks(query);
        
        // Filter by sharing type if specified
        if (sharingTypeStr != null && !sharingTypeStr.trim().isEmpty()) {
            try {
                Book.SharingType sharingType = Book.SharingType.valueOf(sharingTypeStr.toUpperCase());
                books = books.stream()
                    .filter(book -> book.getSharingType() == sharingType)
                    .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Invalid sharing type, return empty list
                books = new ArrayList<>();
            }
        }
        
        // Add owner information to each book
        List<Map<String, Object>> enhancedBooks = books.stream()
            .map(book -> {
                Map<String, Object> bookMap = new HashMap<>();
                bookMap.put("id", book.getId());
                bookMap.put("title", book.getTitle());
                bookMap.put("author", book.getAuthor());
                bookMap.put("genre", book.getGenre());
                bookMap.put("isbn", book.getIsbn());
                bookMap.put("condition", book.getCondition());
                bookMap.put("description", book.getDescription());
                bookMap.put("sharingType", book.getSharingType());
                bookMap.put("status", book.getStatus());
                bookMap.put("lendingDurationDays", book.getLendingDurationDays());
                bookMap.put("ownerId", book.getOwnerId());
                bookMap.put("createdAt", book.getCreatedAt());
                bookMap.put("updatedAt", book.getUpdatedAt());
                bookMap.put("displayLocation", book.getDisplayLocation());
                
                // Add owner information
                Optional<User> ownerOpt = userService.findById(book.getOwnerId());
                if (ownerOpt.isPresent()) {
                    User owner = ownerOpt.get();
                    Map<String, Object> ownerInfo = new HashMap<>();
                    ownerInfo.put("id", owner.getId());
                    ownerInfo.put("firstName", owner.getFirstName());
                    ownerInfo.put("lastName", owner.getLastName());
                    ownerInfo.put("fullName", owner.getFullName());
                    bookMap.put("owner", ownerInfo);
                }
                
                return bookMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(enhancedBooks);
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
        // Get current user from Spring Security authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        // Get user by email from authentication
        String email = authentication.getName();
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }
        
        Long userId = userOpt.get().getId();
        
        List<Book> books = bookService.findBooksByOwner(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("books", books);
        response.put("count", books.size());
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
        // Get current user from Spring Security authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        // Get user by email from authentication
        String email = authentication.getName();
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }
        
        Long userId = userOpt.get().getId();
        
        try {
            // Verify the book belongs to the current user
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Book not found"));
            }
            
            Book book = bookOpt.get();
            if (!book.getOwnerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You can only modify your own books"));
            }
            
            // Update availability
            Book updatedBook = available ? bookService.markAsAvailable(id) : bookService.markAsUnavailable(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", available ? "Book marked as available" : "Book marked as unavailable");
            response.put("book", updatedBook);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * API: Delete book (only by owner)
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteBook(@PathVariable Long id, HttpSession session) {
        // Get current user from Spring Security authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        // Get user by email from authentication
        String email = authentication.getName();
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }
        
        Long userId = userOpt.get().getId();
        
        try {
            // Verify the book belongs to the current user
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Book not found"));
            }
            
            Book book = bookOpt.get();
            if (!book.getOwnerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You can only delete your own books"));
            }
            
            // Check if book has pending requests
            List<Request> pendingRequests = requestService.findPendingRequestsByBook(id);
            if (!pendingRequests.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete book with pending requests. Please handle all requests first."));
            }
            
            // Delete the book
            bookService.deleteBook(id, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Book '" + book.getTitle() + "' has been deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * API: Get available swap books
     */
    @GetMapping("/api/swap")
    @ResponseBody
    public List<Book> getSwapBooks() {
        return bookService.findAvailableSwapBooks();
    }
    
    /**
     * API: Get swappable books by owner
     */
    @GetMapping("/api/swappable")
    @ResponseBody
    public ResponseEntity<?> getSwappableBooks(HttpSession session) {
        // Get current user from Spring Security authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        // Get user by email from authentication
        String email = authentication.getName();
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }
        
        Long userId = userOpt.get().getId();
        
        List<Book> books = bookService.findSwappableBooksByOwner(userId);
        return ResponseEntity.ok(books);
    }

    /**
     * API: Get books by a specific user (owner)
     */
    @GetMapping("/api/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getBooksByUser(@PathVariable Long userId) {
        try {
            // Get user information
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            User user = userOpt.get();
            List<Book> books = bookService.findAvailableBooksByOwner(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            response.put("books", books);
            response.put("totalBooks", books.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get user books");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}