/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.repository;

import com.bookbuddy.bookbuddy.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author holiday
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    /**
     *find books by owner ID
     */
    List<Book> findByOwnerId(Long ownerId);
    
    /**
     * find available books by owner ID
     */
    List<Book> findByOwnerIdAndStatus(Long ownerId, Book.BookStatus status);
    
    /**
     * find all available books
     */
    List<Book> findByStatus(Book.BookStatus status);
    
    /**
     * search books by title, author, or genre
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Book> searchBooks(@Param("query") String query);
    
    /**
     * find books within a certain distance (requires latitude/longitude)
     * 
     */
    @Query("SELECT b FROM Book b WHERE " +
           "b.pickupLatitude IS NOT NULL AND b.pickupLongitude IS NOT NULL " +
           "AND b.status = 'AVAILABLE' " +
           "AND (6371 * acos(cos(radians(:lat)) * cos(radians(b.pickupLatitude)) * " +
           "cos(radians(b.pickupLongitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(b.pickupLatitude)))) <= :distance")
    List<Book> findBooksWithinDistance(@Param("lat") double latitude, 
                                     @Param("lng") double longitude, 
                                     @Param("distance") double distanceKm);
    
    /**
     * find books that have location information
     */
    @Query("SELECT b FROM Book b WHERE b.pickupLatitude IS NOT NULL AND b.pickupLongitude IS NOT NULL AND b.status = 'AVAILABLE'")
    List<Book> findBooksWithLocation();
    
    /**
     * find books by multiple criteria
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' " +
           "AND (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) " +
           "AND (:genre IS NULL OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))) " +
           "AND (:condition IS NULL OR LOWER(b.condition) = LOWER(:condition)) " +
           "AND (:sharingType IS NULL OR b.sharingType = :sharingType)")
    List<Book> findBooksByCriteria(@Param("title") String title,
                                  @Param("author") String author,
                                  @Param("genre") String genre,
                                  @Param("condition") String condition,
                                  @Param("sharingType") Book.SharingType sharingType);
    
    /**
     * count available books
     */
    long countByStatus(Book.BookStatus status);
    
    /**
     * find recent books (last 30 days)
     */
    @Query(value = "SELECT * FROM books WHERE status = 'AVAILABLE' AND created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) ORDER BY created_at DESC", 
       nativeQuery = true)
    List<Book> findRecentBooks();
    
    /**
     * find books available for give away
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND b.sharingType = 'GIVE_AWAY'")
    List<Book> findAvailableGiveAwayBooks();
    
    /**
     * find books available for lending
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND b.sharingType = 'LEND'")
    List<Book> findAvailableLendBooks();
    
    /**
     * find books available for swapping
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND b.sharingType = 'SWAP'")
    List<Book> findAvailableSwapBooks();
    
    /**
     * find books that can be swapped (for swap requests)
     * Returns books with SWAP sharing type that can be offered for swapping
     */
    @Query("SELECT b FROM Book b WHERE b.ownerId = :ownerId AND b.status = 'AVAILABLE' AND b.sharingType = 'SWAP'")
    List<Book> findSwappableBooksByOwner(@Param("ownerId") Long ownerId);
}
