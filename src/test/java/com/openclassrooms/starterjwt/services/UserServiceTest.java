package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.exception.UnauthorizedException;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findById_shouldReturnUser_whenExists() {
        // Arrange
        Long id = 1L;
        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // Act
        User result = userService.findById(id);

        // Assert
        assertThat(result).isEqualTo(user);
        verify(userRepository).findById(id);
    }

    @Test
    void findById_shouldThrowNotFoundException_whenUserDoesNotExist() {
        // Arrange
        Long id = 99L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> userService.findById(id));
        verify(userRepository).findById(id);
    }

    @Test
    void delete_shouldDeleteUser_whenUserExists() {
        // Arrange
        Long id = 1L;
        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // Act
        userService.delete(id);

        // Assert
        verify(userRepository).delete(user);
    }

    @Test
    void delete_shouldThrowNotFoundException_whenUserDoesNotExist() {
        // Arrange
        Long id = 99L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> userService.delete(id));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteIfOwner_shouldDelete_whenRequesterIsOwner() {
        // Arrange
        Long id = 1L;
        String ownerEmail = "owner@test.com";

        User user = new User();
        user.setId(id);
        user.setEmail(ownerEmail);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // Act
        userService.deleteIfOwner(id, ownerEmail);

        // Assert
        verify(userRepository).delete(user);
    }

    @Test
    void deleteIfOwner_shouldThrowUnauthorized_whenRequesterIsNotOwner() {
        // Arrange
        Long id = 1L;

        User user = new User();
        user.setId(id);
        user.setEmail("owner@test.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // Act + Assert
        assertThrows(UnauthorizedException.class, () -> userService.deleteIfOwner(id, "other@test.com"));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void existsByEmail_shouldReturnRepositoryResult() {
        // Arrange
        String email = "toto@test.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean result = userService.existsByEmail(email);

        // Assert
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void save_shouldDelegateToRepository() {
        // Arrange
        User user = new User();
        user.setEmail("toto@test.com");

        when(userRepository.save(user)).thenReturn(user);

        // Act
        User result = userService.save(user);

        // Assert
        assertThat(result).isEqualTo(user);
        verify(userRepository).save(user);
    }

    @Test
    void findByEmail_shouldReturnUser_whenExists() {
        // Arrange
        String email = "toto@test.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        User result = userService.findByEmail(email);

        // Assert
        assertThat(result).isEqualTo(user);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByEmail_shouldThrowNotFoundException_whenUserDoesNotExist() {
        // Arrange
        String email = "missing@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> userService.findByEmail(email));
        verify(userRepository).findByEmail(email);
    }
}
