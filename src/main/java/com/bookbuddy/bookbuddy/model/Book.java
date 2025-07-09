/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Book entity for BookBuddy application
 * Represents books that users want to swap
 * @author holiday
 */
@Entity
@Table(name = "books")
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    @Column(nullable = false, length = 200)
    private String title;
    
    @NotBlank(message = "Author is required")
    @Size(max = 100, message = "Author must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String author;
    
    @Size(max = 50, message = "Genre must be less than 50 characters")
    @Column(length = 50)
    private String genre;
    
    @Size(max = 20, message = "ISBN must be less than 20 characters")
    @Column(length = 20)
    private String isbn;
    
    @NotBlank(message = "Condition is required")
    @Size(max = 20, message = "Condition must be less than 20 characters")
    @Column(name = "book_condition", nullable = false, length = 20)
    private String condition;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    @Column(length = 1000)
    private String description;
    
    // Location where the book can be picked up
    @Column(name = "pickup_latitude")
    private Double pickupLatitude;
    
    @Column(name = "pickup_longitude")
    private Double pickupLongitude;
    
    @Size(max = 200, message = "Pickup location must be less than 200 characters")
    @Column(name = "pickup_location", length = 200)
    private String pickupLocation; // Human-readable address/description
    
    // Book availability status
    @Column(nullable = false)
    private Boolean available = true;
    
    // Owner of the book (foreign key to User)
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    // Optional: Image URL for the book
    @Size(max = 500, message = "Image URL must be less than 500 characters")
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Book() {
        this.available = true;
    }
    
    // Constructor with required fields
    public Book(String title, String author, String condition, Long ownerId) {
        this();
        this.title = title;
        this.author = author;
        this.condition = condition;
        this.ownerId = ownerId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getPickupLatitude() {
        return pickupLatitude;
    }
    
    public void setPickupLatitude(Double pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
    }
    
    public Double getPickupLongitude() {
        return pickupLongitude;
    }
    
    public void setPickupLongitude(Double pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }
    
    public String getPickupLocation() {
        return pickupLocation;
    }
    
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }
    
    public Boolean getAvailable() {
        return available;
    }
    
    public void setAvailable(Boolean available) {
        this.available = available;
    }
    
    public Long getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods
    public boolean hasLocation() {
        return pickupLatitude != null && pickupLongitude != null;
    }
    
    public String getDisplayLocation() {
        if (pickupLocation != null && !pickupLocation.trim().isEmpty()) {
            return pickupLocation;
        }
        if (hasLocation()) {
            return String.format("Lat: %.4f, Lng: %.4f", pickupLatitude, pickupLongitude);
        }
        return "Location not specified";
    }
    
    /**
     * Calculate distance to another location (in kilometers)
     * Using Haversine formula
     */
    public double distanceTo(double latitude, double longitude) {
        if (!hasLocation()) {
            return Double.MAX_VALUE;
        }
        
        final double R = 6371; // Radius of Earth in kilometers
        
        double lat1Rad = Math.toRadians(this.pickupLatitude);
        double lat2Rad = Math.toRadians(latitude);
        double deltaLatRad = Math.toRadians(latitude - this.pickupLatitude);
        double deltaLngRad = Math.toRadians(longitude - this.pickupLongitude);
        
        double a = Math.sin(deltaLatRad/2) * Math.sin(deltaLatRad/2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLngRad/2) * Math.sin(deltaLngRad/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        return R * c;
    }
    
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", genre='" + genre + '\'' +
                ", condition='" + condition + '\'' +
                ", available=" + available +
                ", ownerId=" + ownerId +
                ", hasLocation=" + hasLocation() +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Book book = (Book) o;
        return id != null ? id.equals(book.id) : book.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}