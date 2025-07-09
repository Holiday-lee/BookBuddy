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
     * Find books by owner ID
     */
    List<Book> findByOwnerId(Long ownerId);
    
    /**
     * Find available books by owner ID
     */
    List<Book> findByOwnerIdAndAvailableTrue(Long ownerId);
    
    /**
     * Find all available books
     */
    List<Book> findByAvailableTrue();
    
    /**
     * Find books by title (case insensitive)
     */
    List<Book> findByTitleContainingIgnoreCaseAndAvailableTrue(String title);
    
    /**
     * Find books by author (case insensitive)
     */
    List<Book> findByAuthorContainingIgnoreCaseAndAvailableTrue(String author);
    
    /**
     * Find books by genre (case insensitive)
     */
    List<Book> findByGenreIgnoreCaseAndAvailableTrue(String genre);
    
    /**
     * Find books by condition
     */
    List<Book> findByConditionIgnoreCaseAndAvailableTrue(String condition);
    
    /**
     * Search books by title, author, or genre
     */
    @Query("SELECT b FROM Book b WHERE b.available = true AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Book> searchBooks(@Param("query") String query);
    
    /**
     * Find books within a certain distance (requires latitude/longitude)
     * This is a basic implementation - for production, consider using PostGIS or similar
     */
    @Query("SELECT b FROM Book b WHERE " +
           "b.pickupLatitude IS NOT NULL AND b.pickupLongitude IS NOT NULL " +
           "AND b.available = true " +
           "AND (6371 * acos(cos(radians(:lat)) * cos(radians(b.pickupLatitude)) * " +
           "cos(radians(b.pickupLongitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(b.pickupLatitude)))) <= :distance")
    List<Book> findBooksWithinDistance(@Param("lat") double latitude, 
                                     @Param("lng") double longitude, 
                                     @Param("distance") double distanceKm);
    
    /**
     * Find books that have location information
     */
    @Query("SELECT b FROM Book b WHERE b.pickupLatitude IS NOT NULL AND b.pickupLongitude IS NOT NULL AND b.available = true")
    List<Book> findBooksWithLocation();
    
    /**
     * Find books by multiple criteria
     */
    @Query("SELECT b FROM Book b WHERE b.available = true " +
           "AND (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) " +
           "AND (:genre IS NULL OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))) " +
           "AND (:condition IS NULL OR LOWER(b.condition) = LOWER(:condition))")
    List<Book> findBooksByCriteria(@Param("title") String title,
                                  @Param("author") String author,
                                  @Param("genre") String genre,
                                  @Param("condition") String condition);
    
    /**
     * Count available books
     */
    long countByAvailableTrue();
    
    /**
     * Count books by owner
     */
    long countByOwnerId(Long ownerId);
    
    /**
     * Find recent books (last 30 days)
     */
    @Query(value = "SELECT * FROM books WHERE available = true AND created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) ORDER BY created_at DESC", 
       nativeQuery = true)
    List<Book> findRecentBooks();
    
    /**
     * Find books by ISBN
     */
    List<Book> findByIsbnAndAvailableTrue(String isbn);
}
