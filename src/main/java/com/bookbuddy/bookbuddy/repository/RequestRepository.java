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
     * Find requests by book ID
     */
    List<Request> findByBookId(Long bookId);
    
    /**
     * Find requests by requester ID
     */
    List<Request> findByRequesterId(Long requesterId);
    
    /**
     * Find requests by owner ID
     */
    List<Request> findByOwnerId(Long ownerId);
    
    /**
     * Find pending requests by book ID
     */
    List<Request> findByBookIdAndStatus(Long bookId, Request.RequestStatus status);
    
    /**
     * Find requests by requester and book
     */
    Optional<Request> findByRequesterIdAndBookId(Long requesterId, Long bookId);
    
    /**
     * Find active requests by requester ID
     */
    @Query("SELECT r FROM Request r WHERE r.requesterId = :requesterId AND r.status IN ('PENDING', 'ACCEPTED')")
    List<Request> findActiveRequestsByRequesterId(@Param("requesterId") Long requesterId);
    
    /**
     * Find active requests by owner ID
     */
    @Query("SELECT r FROM Request r WHERE r.ownerId = :ownerId AND r.status IN ('PENDING', 'ACCEPTED')")
    List<Request> findActiveRequestsByOwnerId(@Param("ownerId") Long ownerId);
    
    /**
     * Find requests by type and status
     */
    List<Request> findByRequestTypeAndStatus(Request.RequestType requestType, Request.RequestStatus status);
    
    /**
     * Find trade requests by offered book ID
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
} 