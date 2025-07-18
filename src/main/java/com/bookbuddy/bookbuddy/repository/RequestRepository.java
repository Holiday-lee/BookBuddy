/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.repository;

import com.bookbuddy.bookbuddy.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Request entity
 * @author holiday
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    
    /**
     * Find requests by book ID, sorted by creation date (newest first)
     */
    List<Request> findByBookIdOrderByCreatedAtDesc(Long bookId);
    
    /**
     * Find requests by requester ID, sorted by creation date (newest first)
     */
    List<Request> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    
    /**
     * Find requests by owner ID, sorted by creation date (newest first)
     */
    List<Request> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    
    /**
     * Find pending requests by book ID, sorted by creation date (newest first)
     */
    List<Request> findByBookIdAndStatusOrderByCreatedAtDesc(Long bookId, Request.RequestStatus status);
    
    /**
     * Find requests by requester and book
     */
    Optional<Request> findByRequesterIdAndBookId(Long requesterId, Long bookId);
    
    /**
     * Find active requests by requester ID, sorted by creation date (newest first)
     */
    @Query("SELECT r FROM Request r WHERE r.requesterId = :requesterId AND r.status IN ('PENDING', 'ACCEPTED') ORDER BY r.createdAt DESC")
    List<Request> findActiveRequestsByRequesterId(@Param("requesterId") Long requesterId);
    
    /**
     * Find active requests by owner ID, sorted by creation date (newest first)
     */
    @Query("SELECT r FROM Request r WHERE r.ownerId = :ownerId AND r.status IN ('PENDING', 'ACCEPTED') ORDER BY r.createdAt DESC")
    List<Request> findActiveRequestsByOwnerId(@Param("ownerId") Long ownerId);
    
    /**
     * Find requests by type and status
     */
    List<Request> findByRequestTypeAndStatus(Request.RequestType requestType, Request.RequestStatus status);
    
    /**
     * Find swap requests by offered book ID
     */
    List<Request> findByOfferedBookId(Long offeredBookId);
    
    /**
     * Check if there's a pending request for a book by a specific user
     */
    boolean existsByBookIdAndRequesterIdAndStatus(Long bookId, Long requesterId, Request.RequestStatus status);
    
    /**
     * Count pending requests for a book
     */
    long countByBookIdAndStatus(Long bookId, Request.RequestStatus status);
    
    /**
     * Count requests by owner and status
     */
    long countByOwnerIdAndStatus(Long ownerId, Request.RequestStatus status);
    
    /**
     * Count requests by requester and status in
     */
    long countByRequesterIdAndStatusIn(Long requesterId, List<Request.RequestStatus> statuses);
} 