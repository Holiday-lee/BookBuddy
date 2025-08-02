package com.bookbuddy.bookbuddy.service;

import com.bookbuddy.bookbuddy.model.Book;
import com.bookbuddy.bookbuddy.model.Request;
import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.repository.BookRepository;
import com.bookbuddy.bookbuddy.repository.RequestRepository;
import com.bookbuddy.bookbuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookService bookService;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private RequestService requestService;

    private User testUser1;
    private User testUser2;
    private Book testBook;
    private Request testRequest;

    @BeforeEach
    void setUp() {
        // Setup test users
        testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setFirstName("John");
        testUser1.setLastName("Doe");
        testUser1.setEmail("john@example.com");

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setFirstName("Jane");
        testUser2.setLastName("Smith");
        testUser2.setEmail("jane@example.com");

        // Setup test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setOwnerId(1L);
        testBook.setStatus(Book.BookStatus.AVAILABLE);
        testBook.setSharingType(Book.SharingType.GIVE_AWAY);

        // Setup test request
        testRequest = new Request();
        testRequest.setId(1L);
        testRequest.setRequesterId(2L);
        testRequest.setBookId(1L);
        testRequest.setOwnerId(1L);
        testRequest.setStatus(Request.RequestStatus.PENDING);
        testRequest.setRequestType(Request.RequestType.GIVE_AWAY);
        testRequest.setMessage("I'm interested in your book!");
        testRequest.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createGiveAwayRequest_Success() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(requestRepository.save(any(Request.class))).thenReturn(testRequest);
        when(bookService.markAsUnavailable(1L)).thenReturn(testBook);

        // When
        Request result = requestService.createGiveAwayRequest(1L, 2L, "I'm interested!");

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getBookId());
        assertEquals(2L, result.getRequesterId());
        assertEquals(1L, result.getOwnerId());
        assertEquals(Request.RequestStatus.PENDING, result.getStatus());
        assertEquals(Request.RequestType.GIVE_AWAY, result.getRequestType());
        assertEquals("I'm interested in your book!", result.getMessage());
        verify(requestRepository).save(any(Request.class));
        verify(bookService).markAsUnavailable(1L);
    }

    @Test
    void createGiveAwayRequest_BookNotFound() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> requestService.createGiveAwayRequest(1L, 2L, "Message"));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createGiveAwayRequest_BookNotAvailable() {
        // Given
        testBook.setStatus(Book.BookStatus.UNAVAILABLE);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> requestService.createGiveAwayRequest(1L, 2L, "Message"));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createGiveAwayRequest_BookNotGiveAway() {
        // Given
        testBook.setSharingType(Book.SharingType.LEND);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> requestService.createGiveAwayRequest(1L, 2L, "Message"));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createLendRequest_Success() {
        // Given
        testBook.setSharingType(Book.SharingType.LEND);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> {
            Request savedRequest = invocation.getArgument(0);
            savedRequest.setId(1L);
            return savedRequest;
        });
        when(bookService.markAsUnavailable(1L)).thenReturn(testBook);

        // When
        Request result = requestService.createLendRequest(1L, 2L, "I want to borrow this", 30);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getBookId());
        assertEquals(2L, result.getRequesterId());
        assertEquals(Request.RequestType.LEND, result.getRequestType());
        assertEquals(30, result.getRequestedDurationDays());
        verify(requestRepository).save(any(Request.class));
        verify(bookService).markAsUnavailable(1L);
    }

    @Test
    void createLendRequest_BookNotLend() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> requestService.createLendRequest(1L, 2L, "Message", 30));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createSwapRequest_Success() {
        // Given
        testBook.setSharingType(Book.SharingType.SWAP);
        Book offeredBook = new Book();
        offeredBook.setId(2L);
        offeredBook.setOwnerId(2L);
        offeredBook.setStatus(Book.BookStatus.AVAILABLE);
        offeredBook.setSharingType(Book.SharingType.SWAP);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(offeredBook));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> {
            Request savedRequest = invocation.getArgument(0);
            savedRequest.setId(1L);
            return savedRequest;
        });
        when(bookService.markAsUnavailable(1L)).thenReturn(testBook);
        when(bookService.markAsUnavailable(2L)).thenReturn(offeredBook);

        // When
        Request result = requestService.createSwapRequest(1L, 2L, 2L, "Let's swap!");

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getBookId());
        assertEquals(2L, result.getRequesterId());
        assertEquals(Request.RequestType.SWAP, result.getRequestType());
        assertEquals(2L, result.getOfferedBookId());
        verify(requestRepository).save(any(Request.class));
        verify(bookService).markAsUnavailable(1L);
        verify(bookService).markAsUnavailable(2L);
    }

    @Test
    void createSwapRequest_OfferedBookNotFound() {
        // Given
        testBook.setSharingType(Book.SharingType.SWAP);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> requestService.createSwapRequest(1L, 2L, 2L, "Message"));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void acceptRequest_Success() {
        // Given
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(requestRepository.save(any(Request.class))).thenReturn(testRequest);
        when(bookService.markAsExchangeInProgress(1L)).thenReturn(testBook);

        // When
        Request result = requestService.acceptRequest(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(Request.RequestStatus.ACCEPTED, result.getStatus());
        verify(requestRepository).save(any(Request.class));
        verify(bookService).markAsExchangeInProgress(1L);
    }

    @Test
    void acceptRequest_RequestNotFound() {
        // Given
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> requestService.acceptRequest(1L, 1L));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void acceptRequest_AlreadyProcessed() {
        // Given
        testRequest.setStatus(Request.RequestStatus.ACCEPTED);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> requestService.acceptRequest(1L, 1L));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void rejectRequest_Success() {
        // Given
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(requestRepository.save(any(Request.class))).thenReturn(testRequest);
        when(bookService.markAsAvailable(1L)).thenReturn(testBook);

        // When
        Request result = requestService.rejectRequest(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(Request.RequestStatus.REJECTED, result.getStatus());
        verify(requestRepository).save(any(Request.class));
        verify(bookService).markAsAvailable(1L);
    }

    @Test
    void rejectRequest_RequestNotFound() {
        // Given
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> requestService.rejectRequest(1L, 1L));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void rejectRequest_AlreadyProcessed() {
        // Given
        testRequest.setStatus(Request.RequestStatus.REJECTED);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> requestService.rejectRequest(1L, 1L));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void findById_Success() {
        // Given
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        // When
        Optional<Request> result = requestService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testRequest, result.get());
        verify(requestRepository).findById(1L);
    }

    @Test
    void findById_NotFound() {
        // Given
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<Request> result = requestService.findById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(requestRepository).findById(1L);
    }

    @Test
    void findRequestsByRequester_Success() {
        // Given
        List<Request> requests = Arrays.asList(testRequest);
        when(requestRepository.findByRequesterIdOrderByCreatedAtDesc(2L)).thenReturn(requests);

        // When
        List<Request> result = requestService.findRequestsByRequester(2L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRequest, result.get(0));
        verify(requestRepository).findByRequesterIdOrderByCreatedAtDesc(2L);
    }

    @Test
    void findRequestsByRequester_EmptyList() {
        // Given
        when(requestRepository.findByRequesterIdOrderByCreatedAtDesc(2L)).thenReturn(Arrays.asList());

        // When
        List<Request> result = requestService.findRequestsByRequester(2L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(requestRepository).findByRequesterIdOrderByCreatedAtDesc(2L);
    }

    @Test
    void findRequestsByOwner_Success() {
        // Given
        List<Request> requests = Arrays.asList(testRequest);
        when(requestRepository.findByOwnerIdOrderByCreatedAtDesc(1L)).thenReturn(requests);

        // When
        List<Request> result = requestService.findRequestsByOwner(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRequest, result.get(0));
        verify(requestRepository).findByOwnerIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void findRequestsByOwner_EmptyList() {
        // Given
        when(requestRepository.findByOwnerIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList());

        // When
        List<Request> result = requestService.findRequestsByOwner(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(requestRepository).findByOwnerIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void findPendingRequestsByBook_Success() {
        // Given
        List<Request> requests = Arrays.asList(testRequest);
        when(requestRepository.findByBookIdAndStatusOrderByCreatedAtDesc(1L, Request.RequestStatus.PENDING)).thenReturn(requests);

        // When
        List<Request> result = requestService.findPendingRequestsByBook(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRequest, result.get(0));
        verify(requestRepository).findByBookIdAndStatusOrderByCreatedAtDesc(1L, Request.RequestStatus.PENDING);
    }

    @Test
    void findPendingRequestsByBook_EmptyList() {
        // Given
        when(requestRepository.findByBookIdAndStatusOrderByCreatedAtDesc(1L, Request.RequestStatus.PENDING)).thenReturn(Arrays.asList());

        // When
        List<Request> result = requestService.findPendingRequestsByBook(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(requestRepository).findByBookIdAndStatusOrderByCreatedAtDesc(1L, Request.RequestStatus.PENDING);
    }

    @Test
    void countPendingRequestsByOwner_Success() {
        // Given
        when(requestRepository.countByOwnerIdAndStatus(1L, Request.RequestStatus.PENDING)).thenReturn(5L);

        // When
        long result = requestService.countPendingRequestsByOwner(1L);

        // Then
        assertEquals(5L, result);
        verify(requestRepository).countByOwnerIdAndStatus(1L, Request.RequestStatus.PENDING);
    }

    @Test
    void countUpdatedSentRequests_Success() {
        // Given
        when(requestRepository.countByRequesterIdAndStatusIn(2L, Arrays.asList(
            Request.RequestStatus.ACCEPTED, 
            Request.RequestStatus.REJECTED, 
            Request.RequestStatus.COMPLETED
        ))).thenReturn(3L);

        // When
        long result = requestService.countUpdatedSentRequests(2L);

        // Then
        assertEquals(3L, result);
        verify(requestRepository).countByRequesterIdAndStatusIn(2L, Arrays.asList(
            Request.RequestStatus.ACCEPTED, 
            Request.RequestStatus.REJECTED, 
            Request.RequestStatus.COMPLETED
        ));
    }
} 