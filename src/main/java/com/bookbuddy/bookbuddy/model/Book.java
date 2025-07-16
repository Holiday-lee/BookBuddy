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
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Book entity for BookBuddy application
 * Represents books that users want to share (give away, lend, or swap)
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
    
    // New fields for sharing functionality
    @Enumerated(EnumType.STRING)
    @Column(name = "sharing_type", nullable = false)
    private SharingType sharingType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookStatus status = BookStatus.AVAILABLE;
    
    // For lending: maximum lending period in days
    @Column(name = "lending_duration_days")
    private Integer lendingDurationDays;
    
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
    
    // Enums for sharing types and status
    public enum SharingType {
        GIVE_AWAY("Give Away"),
        LEND("Lend for Specific Period"),
        SWAP("Swap");
        
        private final String displayName;
        
        SharingType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum BookStatus {
        AVAILABLE("Available"),
        UNAVAILABLE("Unavailable"),
        EXCHANGE_IN_PROGRESS("Exchange in Progress"),
        CURRENTLY_LENT_OUT("Currently Lent Out"),
        GIVEN_AWAY("Given Away"),
        SWAPPED("Swapped");
        
        private final String displayName;
        
        BookStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Default constructor
    public Book() {
        this.status = BookStatus.AVAILABLE;
    }
    
    // Constructor with required fields
    public Book(String title, String author, String condition, Long ownerId, SharingType sharingType) {
        this();
        this.title = title;
        this.author = author;
        this.condition = condition;
        this.ownerId = ownerId;
        this.sharingType = sharingType;
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
    
    public SharingType getSharingType() {
        return sharingType;
    }
    
    public void setSharingType(SharingType sharingType) {
        this.sharingType = sharingType;
    }
    
    public BookStatus getStatus() {
        return status;
    }
    
    public void setStatus(BookStatus status) {
        this.status = status;
    }
    
    public Integer getLendingDurationDays() {
        return lendingDurationDays;
    }
    
    public void setLendingDurationDays(Integer lendingDurationDays) {
        this.lendingDurationDays = lendingDurationDays;
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
    
    @JsonProperty("displayLocation")
    public String getDisplayLocation() {
        if (pickupLocation != null && !pickupLocation.trim().isEmpty()) {
            return pickupLocation;
        }
        if (hasLocation()) {
            // Show coordinates in a more readable format
            return String.format("📍 %.4f, %.4f", pickupLatitude, pickupLongitude);
        }
        return "📍 Location not specified";
    }
    
    /**
     * Get display location with optional distance information
     */
    public String getDisplayLocationWithDistance(Double userLat, Double userLng) {
        String baseLocation = getDisplayLocation();
        
        if (userLat != null && userLng != null && hasLocation()) {
            double distance = distanceTo(userLat, userLng);
            if (distance < Double.MAX_VALUE) {
                String distanceText = distance < 1 ? 
                    String.format("%.0f m", distance * 1000) : 
                    String.format("%.1f km", distance);
                return baseLocation + " (" + distanceText + " away)";
            }
        }
        
        return baseLocation;
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
        
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Check if book is available for requests
     */
    public boolean isAvailableForRequests() {
        return status == BookStatus.AVAILABLE;
    }
    
    /**
     * Check if book can be given away
     */
    public boolean canBeGivenAway() {
        return sharingType == SharingType.GIVE_AWAY && status == BookStatus.AVAILABLE;
    }
    
    /**
     * Check if book can be lent
     */
    public boolean canBeLent() {
        return sharingType == SharingType.LEND && status == BookStatus.AVAILABLE;
    }
    
    /**
     * Check if book can be swapped
     */
    public boolean canBeSwapped() {
        return sharingType == SharingType.SWAP && status == BookStatus.AVAILABLE;
    }
    
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", sharingType=" + sharingType +
                ", status=" + status +
                ", ownerId=" + ownerId +
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