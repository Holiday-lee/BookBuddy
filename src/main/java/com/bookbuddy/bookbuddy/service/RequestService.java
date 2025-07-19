/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bookbuddy.bookbuddy.service;

import com.bookbuddy.bookbuddy.model.Book;
import com.bookbuddy.bookbuddy.model.Request;
import com.bookbuddy.bookbuddy.model.RequestWithBookInfo;
import com.bookbuddy.bookbuddy.repository.BookRepository;
import com.bookbuddy.bookbuddy.repository.RequestRepository;
import com.bookbuddy.bookbuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for handling book requests
 * @author holiday
 */
@Service
@Transactional
public class RequestService {
    
    private final RequestRepository requestRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final UserRepository userRepository;
    private final ChatService chatService;
    
    @Autowired
    public RequestService(RequestRepository requestRepository, 
                        BookRepository bookRepository,
                        BookService bookService,
                        UserRepository userRepository,
                        ChatService chatService) {
        this.requestRepository = requestRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.userRepository = userRepository;
        this.chatService = chatService;
    }
    
    /**
     * Create a give away request
     */
    public Request createGiveAwayRequest(Long bookId, Long requesterId, String message) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        if (book.getOwnerId().equals(requesterId)) {
            throw new IllegalArgumentException("You cannot request your own book");
        }
        
        if (!book.canBeGivenAway()) {
            throw new IllegalArgumentException("Book is not available for give away");
        }
        
        // Check if user already has a pending request for this book
        if (requestRepository.existsByBookIdAndRequesterIdAndStatus(bookId, requesterId, Request.RequestStatus.PENDING)) {
            throw new IllegalArgumentException("You already have a pending request for this book");
        }
        
        Request request = new Request(bookId, requesterId, book.getOwnerId(), Request.RequestType.GIVE_AWAY);
        request.setMessage(message);
        
        // Mark book as unavailable
        bookService.markAsUnavailable(bookId);
        
        return requestRepository.save(request);
    }
    
    /**
     * Create a lending request
     */
    public Request createLendRequest(Long bookId, Long requesterId, String message, Integer requestedDurationDays) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        if (book.getOwnerId().equals(requesterId)) {
            throw new IllegalArgumentException("You cannot request your own book");
        }
        
        if (!book.canBeLent()) {
            throw new IllegalArgumentException("Book is not available for lending");
        }
        
        if (requestedDurationDays == null || requestedDurationDays <= 0) {
            throw new IllegalArgumentException("Requested duration is required");
        }
        
        if (book.getLendingDurationDays() != null && requestedDurationDays > book.getLendingDurationDays()) {
            throw new IllegalArgumentException("Requested duration cannot exceed the book's maximum lending period");
        }
        
        // Check if user already has a pending request for this book
        if (requestRepository.existsByBookIdAndRequesterIdAndStatus(bookId, requesterId, Request.RequestStatus.PENDING)) {
            throw new IllegalArgumentException("You already have a pending request for this book");
        }
        
        Request request = new Request(bookId, requesterId, book.getOwnerId(), Request.RequestType.LEND);
        request.setMessage(message);
        request.setRequestedDurationDays(requestedDurationDays);
        
        // Mark book as unavailable
        bookService.markAsUnavailable(bookId);
        
        return requestRepository.save(request);
    }
    
    /**
     * Create a swap request
     */
    public Request createSwapRequest(Long bookId, Long requesterId, Long offeredBookId, String message) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        Book offeredBook = bookRepository.findById(offeredBookId)
                .orElseThrow(() -> new IllegalArgumentException("Offered book not found"));
        
        if (book.getOwnerId().equals(requesterId)) {
            throw new IllegalArgumentException("You cannot request your own book");
        }
        
        if (!offeredBook.getOwnerId().equals(requesterId)) {
            throw new IllegalArgumentException("You can only offer your own books for swap");
        }
        
        if (!book.canBeSwapped()) {
            throw new IllegalArgumentException("Book is not available for swapping");
        }
        
        if (!offeredBook.canBeSwapped()) {
            throw new IllegalArgumentException("Offered book is not available for swapping");
        }
        
        if (bookId.equals(offeredBookId)) {
            throw new IllegalArgumentException("Cannot swap a book for itself");
        }
        
        // Check if user already has a pending request for this book
        if (requestRepository.existsByBookIdAndRequesterIdAndStatus(bookId, requesterId, Request.RequestStatus.PENDING)) {
            throw new IllegalArgumentException("You already have a pending request for this book");
        }
        
        Request request = new Request(bookId, requesterId, book.getOwnerId(), Request.RequestType.SWAP);
        request.setMessage(message);
        request.setOfferedBookId(offeredBookId);
        
        // Mark both books as unavailable
        bookService.markAsUnavailable(bookId);
        bookService.markAsUnavailable(offeredBookId);
        
        return requestRepository.save(request);
    }
    
    /**
     * Accept a request
     */
    public Request acceptRequest(Long requestId, Long ownerId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (!request.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("You can only accept requests for your own books");
        }
        
        if (!request.isPending()) {
            throw new IllegalArgumentException("Request is not pending");
        }
        
        request.setStatus(Request.RequestStatus.ACCEPTED);
        
        // Update book status based on request type
        if (request.isGiveAwayRequest()) {
            bookService.markAsExchangeInProgress(request.getBookId());
        } else if (request.isLendRequest()) {
            bookService.markAsCurrentlyLentOut(request.getBookId());
        } else if (request.isSwapRequest()) {
            bookService.markAsExchangeInProgress(request.getBookId());
            if (request.getOfferedBookId() != null) {
                bookService.markAsExchangeInProgress(request.getOfferedBookId());
            }
        }

        // Automatically create a chat when a request is accepted (if one doesn't already exist)
        if (request.isGiveAwayRequest() || request.isLendRequest() || request.isSwapRequest()) {
            try {
                chatService.createChatForRequest(requestId);
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("Chat already exists")) {
                    // Chat already exists, which is fine - just continue
                    System.out.println("Chat already exists for request " + requestId + ", continuing...");
                } else {
                    // Re-throw other IllegalArgumentException
                    throw e;
                }
            }
        }
        
        return requestRepository.save(request);
    }
    
    /**
     * Reject a request
     */
    public Request rejectRequest(Long requestId, Long ownerId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (!request.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("You can only reject requests for your own books");
        }
        
        if (!request.isPending()) {
            throw new IllegalArgumentException("Request is not pending");
        }
        
        request.setStatus(Request.RequestStatus.REJECTED);
        
        // Mark book as available again
        bookService.markAsAvailable(request.getBookId());
        
        // For swap requests, also mark offered book as available
        if (request.isSwapRequest() && request.getOfferedBookId() != null) {
            bookService.markAsAvailable(request.getOfferedBookId());
        }
        
        return requestRepository.save(request);
    }
    
    /**
     * Complete a request (for give away and swap)
     */
    public Request completeRequest(Long requestId, Long userId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (!request.isAccepted()) {
            throw new IllegalArgumentException("Request is not accepted");
        }
        
        // Only the book owner can complete the request
        if (!request.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Only the book owner can complete the request");
        }
        
        request.setStatus(Request.RequestStatus.COMPLETED);
        
        // Update book status based on request type
        if (request.isGiveAwayRequest()) {
            bookService.markAsGivenAway(request.getBookId());
        } else if (request.isLendRequest()) {
            // For lending requests, mark book as available again (returned)
            bookService.markAsAvailable(request.getBookId());
        } else if (request.isSwapRequest()) {
            bookService.markAsSwapped(request.getBookId());
            if (request.getOfferedBookId() != null) {
                bookService.markAsSwapped(request.getOfferedBookId());
            }
        }
        
        return requestRepository.save(request);
    }
    
    /**
     * Return a lent book
     */
    public Request returnLentBook(Long requestId, Long ownerId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (!request.isLendRequest()) {
            throw new IllegalArgumentException("Request is not a lending request");
        }
        
        if (!request.isAccepted()) {
            throw new IllegalArgumentException("Request is not accepted");
        }
        
        if (!request.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the book owner can return the book");
        }
        
        request.setStatus(Request.RequestStatus.COMPLETED);
        
        // Mark book as available again
        bookService.markAsAvailable(request.getBookId());
        
        return requestRepository.save(request);
    }
    
    /**
     * Cancel a request (by requester)
     */
    public Request cancelRequest(Long requestId, Long requesterId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (!request.getRequesterId().equals(requesterId)) {
            throw new IllegalArgumentException("You can only cancel your own requests");
        }
        
        if (!request.isPending()) {
            throw new IllegalArgumentException("Request is not pending");
        }
        
        request.setStatus(Request.RequestStatus.CANCELLED);
        
        // Mark book as available again
        bookService.markAsAvailable(request.getBookId());
        
        // For swap requests, also mark offered book as available
        if (request.isSwapRequest() && request.getOfferedBookId() != null) {
            bookService.markAsAvailable(request.getOfferedBookId());
        }
        
        return requestRepository.save(request);
    }
    
    /**
     * Find request by ID
     */
    @Transactional(readOnly = true)
    public Optional<Request> findById(Long id) {
        return requestRepository.findById(id);
    }
    
    /**
     * Find requests by requester
     */
    @Transactional(readOnly = true)
    public List<Request> findRequestsByRequester(Long requesterId) {
        return requestRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId);
    }
    
    /**
     * Find requests by owner
     */
    @Transactional(readOnly = true)
    public List<Request> findRequestsByOwner(Long ownerId) {
        return requestRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
    }
    
    /**
     * Find active requests by requester
     */
    @Transactional(readOnly = true)
    public List<Request> findActiveRequestsByRequester(Long requesterId) {
        return requestRepository.findActiveRequestsByRequesterId(requesterId);
    }
    
    /**
     * Find active requests by owner
     */
    @Transactional(readOnly = true)
    public List<Request> findActiveRequestsByOwner(Long ownerId) {
        return requestRepository.findActiveRequestsByOwnerId(ownerId);
    }
    
    /**
     * Find pending requests by book
     */
    @Transactional(readOnly = true)
    public List<Request> findPendingRequestsByBook(Long bookId) {
        return requestRepository.findByBookIdAndStatusOrderByCreatedAtDesc(bookId, Request.RequestStatus.PENDING);
    }
    
    /**
     * Find requests by book
     */
    @Transactional(readOnly = true)
    public List<Request> findRequestsByBook(Long bookId) {
        return requestRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }
    
    /**
     * Find requests by requester with book information
     */
    @Transactional(readOnly = true)
    public List<RequestWithBookInfo> findRequestsByRequesterWithBookInfo(Long requesterId) {
        List<Request> requests = findRequestsByRequester(requesterId);
        return requests.stream()
                .map(this::enrichRequestWithBookInfo)
                .collect(Collectors.toList());
    }
    
    /**
     * Find requests by owner with book information
     */
    @Transactional(readOnly = true)
    public List<RequestWithBookInfo> findRequestsByOwnerWithBookInfo(Long ownerId) {
        List<Request> requests = findRequestsByOwner(ownerId);
        return requests.stream()
                .map(this::enrichRequestWithBookInfo)
                .collect(Collectors.toList());
    }
    
    /**
     * Enrich a request with book and user information
     */
    private RequestWithBookInfo enrichRequestWithBookInfo(Request request) {
        Book book = bookRepository.findById(request.getBookId()).orElse(null);
        
        String ownerName = userRepository.findById(request.getOwnerId())
                .map(user -> user.getFirstName() + " " + user.getLastName())
                .orElse("Unknown Owner");
        
        String requesterName = userRepository.findById(request.getRequesterId())
                .map(user -> user.getFirstName() + " " + user.getLastName())
                .orElse("Unknown Requester");
        
        return new RequestWithBookInfo(request, book, ownerName, requesterName);
    }
    
    /**
     * Count pending requests by owner (for received requests notifications)
     */
    @Transactional(readOnly = true)
    public long countPendingRequestsByOwner(Long ownerId) {
        return requestRepository.countByOwnerIdAndStatus(ownerId, Request.RequestStatus.PENDING);
    }
    
    /**
     * Count updated sent requests (for sent requests notifications)
     * This counts requests that have been updated (accepted, rejected, etc.)
     */
    @Transactional(readOnly = true)
    public long countUpdatedSentRequests(Long requesterId) {
        return requestRepository.countByRequesterIdAndStatusIn(
            requesterId, 
            List.of(Request.RequestStatus.ACCEPTED, Request.RequestStatus.REJECTED, Request.RequestStatus.COMPLETED)
        );
    }
} 