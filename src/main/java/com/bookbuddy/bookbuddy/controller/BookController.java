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
 *
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
                          @RequestParam("sharingType") String sharingTypeStr,
                          @RequestParam(value = "lendingDurationDays", required = false) String lendingDurationStr,
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
            
            // Parse sharing type
            Book.SharingType sharingType;
            try {
                sharingType = Book.SharingType.valueOf(sharingTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid sharing type selected.");
                return "redirect:/pages/list-book.html?error=Invalid sharing type";
            }
            
            // Parse lending duration
            Integer lendingDurationDays = null;
            if (sharingType == Book.SharingType.LEND) {
                if (lendingDurationStr == null || lendingDurationStr.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lending duration is required for lend books.");
                    return "redirect:/pages/list-book.html?error=Lending duration required";
                }
                try {
                    lendingDurationDays = Integer.parseInt(lendingDurationStr.trim());
                    if (lendingDurationDays <= 0) {
                        throw new NumberFormatException("Duration must be positive");
                    }
                } catch (NumberFormatException e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Invalid lending duration.");
                    return "redirect:/pages/list-book.html?error=Invalid lending duration";
                }
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
                                               description, pickupLocation, latitude, longitude, userId,
                                               sharingType, lendingDurationDays);
            
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
            redirectAttributes.addFlashAttribute("sharingType", sharingTypeStr);
            redirectAttributes.addFlashAttribute("lendingDurationDays", lendingDurationStr);
            
            return "redirect:/pages/list-book.html?error=" + 
                   java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            // Log the actual error for debugging
            System.err.println("Error listing book: " + e.getMessage());
            e.printStackTrace();
            
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to list book: " + e.getMessage());
            
            return "redirect:/pages/list-book.html?error=" + 
                   java.net.URLEncoder.encode("Failed to list book: " + e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
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
     * API: Get all books (for debugging - shows all books regardless of status)
     */
    @GetMapping("/api/debug/all")
    @ResponseBody
    public List<Book> getAllBooksDebug() {
        return bookService.findAllBooks();
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
            Book book = available ? bookService.markAsAvailable(id) : bookService.markAsUnavailable(id);
            return ResponseEntity.ok(book);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * API: Get available give away books
     */
    @GetMapping("/api/give-away")
    @ResponseBody
    public List<Book> getGiveAwayBooks() {
        return bookService.findAvailableGiveAwayBooks();
    }
    
    /**
     * API: Get available lend books
     */
    @GetMapping("/api/lend")
    @ResponseBody
    public List<Book> getLendBooks() {
        return bookService.findAvailableLendBooks();
    }
    
    /**
     * API: Get available trade books
     */
    @GetMapping("/api/trade")
    @ResponseBody
    public List<Book> getTradeBooks() {
        return bookService.findAvailableTradeBooks();
    }
    
    /**
     * API: Get tradeable books by owner
     */
    @GetMapping("/api/tradeable")
    @ResponseBody
    public ResponseEntity<?> getTradeableBooks(HttpSession session) {
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
        
        List<Book> books = bookService.findTradeableBooksByOwner(userId);
        return ResponseEntity.ok(books);
    }
}