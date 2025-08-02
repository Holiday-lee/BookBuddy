package com.bookbuddy.bookbuddy.service;

import com.bookbuddy.bookbuddy.model.Book;
import com.bookbuddy.bookbuddy.model.Chat;
import com.bookbuddy.bookbuddy.model.Message;
import com.bookbuddy.bookbuddy.model.Request;
import com.bookbuddy.bookbuddy.model.User;
import com.bookbuddy.bookbuddy.repository.ChatRepository;
import com.bookbuddy.bookbuddy.repository.MessageRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    private User testUser1;
    private User testUser2;
    private Book testBook;
    private Request testRequest;
    private Chat testChat;
    private Message testMessage;

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

        // Setup test request
        testRequest = new Request();
        testRequest.setId(1L);
        testRequest.setRequesterId(2L);
        testRequest.setBookId(1L);
        testRequest.setOwnerId(1L);
        testRequest.setStatus(Request.RequestStatus.ACCEPTED);

        // Setup test chat
        testChat = new Chat(1L, 1L, 2L, 1L);
        testChat.setId(1L);
        testChat.setStatus(Chat.ChatStatus.ACTIVE);

        // Setup test message
        testMessage = new Message(1L, 2L, "Hello, I'm interested in your book!");
        testMessage.setId(1L);
        testMessage.setMessageType(Message.MessageType.TEXT);
        testMessage.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createChatForRequest_Success() {
        // Given
        testRequest.setRequestType(Request.RequestType.GIVE_AWAY);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(chatRepository.existsByRequestId(1L)).thenReturn(false);
        when(chatRepository.save(any(Chat.class))).thenReturn(testChat);
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // When
        Chat result = chatService.createChatForRequest(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getBookId());
        assertEquals(1L, result.getRequestId());
        assertEquals(2L, result.getUser1Id());
        assertEquals(1L, result.getUser2Id());
        assertEquals(Chat.ChatStatus.ACTIVE, result.getStatus());
        verify(chatRepository).save(any(Chat.class));
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void createChatForRequest_RequestNotFound() {
        // Given
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> chatService.createChatForRequest(1L));
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    void createChatForRequest_RequestNotAccepted() {
        // Given
        testRequest.setStatus(Request.RequestStatus.PENDING);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> chatService.createChatForRequest(1L));
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    void createChatForRequest_ChatAlreadyExists() {
        // Given
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(chatRepository.existsByRequestId(1L)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> chatService.createChatForRequest(1L));
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    void sendMessage_Success() {
        // Given
        Message expectedMessage = new Message(1L, 2L, "Hello!");
        expectedMessage.setId(1L);
        expectedMessage.setMessageType(Message.MessageType.TEXT);
        expectedMessage.setCreatedAt(LocalDateTime.now());
        
        when(chatRepository.findById(1L)).thenReturn(Optional.of(testChat));
        when(messageRepository.save(any(Message.class))).thenReturn(expectedMessage);

        // When
        Message result = chatService.sendMessage(1L, 2L, "Hello!");

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getChatId());
        assertEquals(2L, result.getSenderId());
        assertEquals("Hello!", result.getContent());
        assertEquals(Message.MessageType.TEXT, result.getMessageType());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void sendMessage_ChatNotFound() {
        // Given
        when(chatRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> chatService.sendMessage(1L, 2L, "Hello!"));
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void sendMessage_UserNotInChat() {
        // Given
        when(chatRepository.findById(1L)).thenReturn(Optional.of(testChat));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> chatService.sendMessage(1L, 3L, "Hello!"));
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void sendMessage_ChatNotActive() {
        // Given
        testChat.setStatus(Chat.ChatStatus.COMPLETED);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(testChat));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> chatService.sendMessage(1L, 2L, "Hello!"));
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void getChatMessages_Success() {
        // Given
        List<Message> messages = Arrays.asList(testMessage);
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(1L)).thenReturn(messages);

        // When
        List<Message> result = chatService.getChatMessages(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage, result.get(0));
        verify(messageRepository).findByChatIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void getChatMessages_EmptyList() {
        // Given
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(1L)).thenReturn(Arrays.asList());

        // When
        List<Message> result = chatService.getChatMessages(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(messageRepository).findByChatIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void findById_Success() {
        // Given
        when(chatRepository.findById(1L)).thenReturn(Optional.of(testChat));

        // When
        Optional<Chat> result = chatService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testChat, result.get());
        verify(chatRepository).findById(1L);
    }

    @Test
    void findById_NotFound() {
        // Given
        when(chatRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<Chat> result = chatService.findById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(chatRepository).findById(1L);
    }

    @Test
    void findByRequestId_Success() {
        // Given
        when(chatRepository.findByRequestId(1L)).thenReturn(Optional.of(testChat));

        // When
        Optional<Chat> result = chatService.findByRequestId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testChat, result.get());
        verify(chatRepository).findByRequestId(1L);
    }

    @Test
    void findByRequestId_NotFound() {
        // Given
        when(chatRepository.findByRequestId(1L)).thenReturn(Optional.empty());

        // When
        Optional<Chat> result = chatService.findByRequestId(1L);

        // Then
        assertFalse(result.isPresent());
        verify(chatRepository).findByRequestId(1L);
    }

    @Test
    void findActiveChatsByUser_Success() {
        // Given
        List<Chat> chats = Arrays.asList(testChat);
        when(chatRepository.findActiveChatsByUserId(1L)).thenReturn(chats);

        // When
        List<Chat> result = chatService.findActiveChatsByUser(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testChat, result.get(0));
        verify(chatRepository).findActiveChatsByUserId(1L);
    }

    @Test
    void findActiveChatsByUser_EmptyList() {
        // Given
        when(chatRepository.findActiveChatsByUserId(1L)).thenReturn(Arrays.asList());

        // When
        List<Chat> result = chatService.findActiveChatsByUser(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(chatRepository).findActiveChatsByUserId(1L);
    }

    @Test
    void findByUserId_Success() {
        // Given
        List<Chat> chats = Arrays.asList(testChat);
        when(chatRepository.findAllChatsByUserId(1L)).thenReturn(chats);

        // When
        List<Chat> result = chatService.findByUserId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testChat, result.get(0));
        verify(chatRepository).findAllChatsByUserId(1L);
    }

    @Test
    void findByUserId_EmptyList() {
        // Given
        when(chatRepository.findAllChatsByUserId(1L)).thenReturn(Arrays.asList());

        // When
        List<Chat> result = chatService.findByUserId(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(chatRepository).findAllChatsByUserId(1L);
    }

    @Test
    void getLastMessage_Success() {
        // Given
        when(messageRepository.findFirstByChatIdOrderByCreatedAtDesc(1L)).thenReturn(testMessage);

        // When
        Optional<Message> result = chatService.getLastMessage(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testMessage, result.get());
        verify(messageRepository).findFirstByChatIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getLastMessage_NotFound() {
        // Given
        when(messageRepository.findFirstByChatIdOrderByCreatedAtDesc(1L)).thenReturn(null);

        // When
        Optional<Message> result = chatService.getLastMessage(1L);

        // Then
        assertFalse(result.isPresent());
        verify(messageRepository).findFirstByChatIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void findCompletedChatsByUser_Success() {
        // Given
        List<Chat> chats = Arrays.asList(testChat);
        when(chatRepository.findCompletedChatsByUserId(1L)).thenReturn(chats);

        // When
        List<Chat> result = chatService.findCompletedChatsByUser(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testChat, result.get(0));
        verify(chatRepository).findCompletedChatsByUserId(1L);
    }

    @Test
    void getUnreadMessageCount_Success() {
        // Given
        List<Message> unreadMessages = Arrays.asList(testMessage, testMessage, testMessage);
        when(messageRepository.findUnreadMessagesByChatIdAndUserId(1L, 1L)).thenReturn(unreadMessages);

        // When
        long result = chatService.getUnreadMessageCount(1L, 1L);

        // Then
        assertEquals(3L, result);
        verify(messageRepository).findUnreadMessagesByChatIdAndUserId(1L, 1L);
    }

    @Test
    void markMessagesAsRead_Success() {
        // Given
        when(chatRepository.findById(1L)).thenReturn(Optional.of(testChat));
        doNothing().when(messageRepository).markMessagesAsRead(1L, 1L);

        // When
        chatService.markMessagesAsRead(1L, 1L);

        // Then
        verify(messageRepository).markMessagesAsRead(1L, 1L);
    }

    @Test
    void getTotalUnreadMessageCount_Success() {
        // Given
        List<Chat> userChats = Arrays.asList(testChat);
        List<Message> unreadMessages = Arrays.asList(testMessage, testMessage, testMessage, testMessage, testMessage);
        
        when(chatRepository.findActiveChatsByUserId(1L)).thenReturn(userChats);
        when(messageRepository.findUnreadMessagesByChatIdAndUserId(1L, 1L)).thenReturn(unreadMessages);

        // When
        long result = chatService.getTotalUnreadMessageCount(1L);

        // Then
        assertEquals(5L, result);
        verify(chatRepository).findActiveChatsByUserId(1L);
        verify(messageRepository).findUnreadMessagesByChatIdAndUserId(1L, 1L);
    }

    @Test
    void getLatestMessage_Success() {
        // Given
        when(messageRepository.findFirstByChatIdOrderByCreatedAtDesc(1L)).thenReturn(testMessage);

        // When
        Message result = chatService.getLatestMessage(1L);

        // Then
        assertNotNull(result);
        assertEquals(testMessage, result);
        verify(messageRepository).findFirstByChatIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getLatestMessage_NotFound() {
        // Given
        when(messageRepository.findFirstByChatIdOrderByCreatedAtDesc(1L)).thenReturn(null);

        // When
        Message result = chatService.getLatestMessage(1L);

        // Then
        assertNull(result);
        verify(messageRepository).findFirstByChatIdOrderByCreatedAtDesc(1L);
    }
} 