package com.familyos.familyos.authentication.service;

import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void findOrCreateUserReturnsExistingUser() {
        User existing = user("user@example.com", "Test User", "google");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existing));

        User result = userService.findOrCreateUser("user@example.com", "Ignored", "github");

        assertSame(existing, result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void findOrCreateUserCreatesNewUser() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            return user;
        });

        User result = userService.findOrCreateUser("user@example.com", "Test User", "google");

        assertEquals("user@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserThrowsWhenMissing() {
        User user = user("user@example.com", "Test User", "google");
        user.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        when(userRepository.existsById(user.getId())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(user));
    }

    @Test
    void findByEmailDelegatesToRepository() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        userService.findByEmail("user@example.com");

        verify(userRepository).findByEmail("user@example.com");
    }

    private User user(String email, String name, String provider) {
        User user = new User(email, name, provider);
        user.setId(UUID.randomUUID());
        return user;
    }
}
