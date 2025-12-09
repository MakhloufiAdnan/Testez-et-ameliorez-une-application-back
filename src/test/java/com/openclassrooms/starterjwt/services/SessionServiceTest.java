package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SessionService sessionService;

    @Test
    void create_shouldSaveSession() {
        // Arrange
        Session session = new Session();
        when(sessionRepository.save(session)).thenReturn(session);

        // Act
        Session result = sessionService.create(session);

        // Assert
        assertThat(result).isEqualTo(session);
        verify(sessionRepository).save(session);
    }

    @Test
    void findAll_shouldReturnAllSessions() {
        // Arrange
        Session s1 = new Session();
        s1.setId(1L);
        Session s2 = new Session();
        s2.setId(2L);

        when(sessionRepository.findAll()).thenReturn(List.of(s1, s2));

        // Act
        List<Session> result = sessionService.findAll();

        // Assert
        assertThat(result)
                .hasSize(2)
                .containsExactly(s1, s2);
        verify(sessionRepository).findAll();
    }

    @Test
    void getById_shouldReturnSession_whenExists() {
        // Arrange
        Long id = 1L;
        Session session = new Session();
        session.setId(id);

        when(sessionRepository.findById(id)).thenReturn(Optional.of(session));

        // Act
        Session result = sessionService.getById(id);

        // Assert
        assertThat(result).isEqualTo(session);
        verify(sessionRepository).findById(id);
    }

    @Test
    void getById_shouldThrowNotFoundException_whenSessionDoesNotExist() {
        // Arrange
        Long id = 99L;
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> sessionService.getById(id));
        verify(sessionRepository).findById(id);
    }

    @Test
    void update_shouldSaveSession_whenSessionExists() {
        // Arrange
        Long id = 1L;
        Session session = new Session();
        session.setName("Old name");

        when(sessionRepository.existsById(id)).thenReturn(true);
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Session result = sessionService.update(id, session);

        // Assert
        assertThat(result.getId()).isEqualTo(id);
        verify(sessionRepository).existsById(id);
        verify(sessionRepository).save(session);
    }

    @Test
    void update_shouldThrowNotFoundException_whenSessionDoesNotExist() {
        // Arrange
        Long id = 99L;
        Session session = new Session();

        when(sessionRepository.existsById(id)).thenReturn(false);

        // Act + Assert
        assertThrows(NotFoundException.class, () -> sessionService.update(id, session));
        verify(sessionRepository).existsById(id);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteSession_whenExists() {
        // Arrange
        Long id = 1L;
        Session session = new Session();
        session.setId(id);

        when(sessionRepository.findById(id)).thenReturn(Optional.of(session));

        // Act
        sessionService.delete(id);

        // Assert
        verify(sessionRepository).findById(id);
        verify(sessionRepository).delete(session);
    }

    @Test
    void delete_shouldThrowNotFoundException_whenSessionDoesNotExist() {
        // Arrange
        Long id = 99L;
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> sessionService.delete(id));
        verify(sessionRepository).findById(id);
        verify(sessionRepository, never()).delete(any());
    }

    @Test
    void participate_shouldAddUser_whenNotAlreadyParticipating() {
        // Arrange
        Long sessionId = 1L;
        Long userId = 10L;

        User user = new User();
        user.setId(userId);

        Session session = new Session();
        session.setId(sessionId);
        session.setUsers(new ArrayList<>());

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        sessionService.participate(sessionId, userId);

        // Assert
        assertThat(session.getUsers()).containsExactly(user);
        verify(sessionRepository).save(session);
    }

    @Test
    void participate_shouldThrowBadRequest_whenUserAlreadyParticipates() {
        // Arrange
        Long sessionId = 1L;
        Long userId = 10L;

        User user = new User();
        user.setId(userId);

        Session session = new Session();
        session.setId(sessionId);
        session.setUsers(new ArrayList<>(List.of(user)));

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act + Assert
        assertThrows(BadRequestException.class, () -> sessionService.participate(sessionId, userId));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void participate_shouldThrowNotFound_whenSessionDoesNotExist() {
        // Arrange
        Long sessionId = 1L;
        Long userId = 10L;

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> sessionService.participate(sessionId, userId));
        verify(userRepository, never()).findById(any());
    }

    @Test
    void participate_shouldThrowNotFound_whenUserDoesNotExist() {
        // Arrange
        Long sessionId = 1L;
        Long userId = 10L;

        Session session = new Session();
        session.setId(sessionId);
        session.setUsers(new ArrayList<>());

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> sessionService.participate(sessionId, userId));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void noLongerParticipate_shouldRemoveUser_whenParticipating() {
        // Arrange
        Long sessionId = 1L;
        Long userId = 10L;

        User user = new User();
        user.setId(userId);

        Session session = new Session();
        session.setId(sessionId);
        session.setUsers(new ArrayList<>(List.of(user)));

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // Act
        sessionService.noLongerParticipate(sessionId, userId);

        // Assert
        assertThat(session.getUsers()).isEmpty();
        verify(sessionRepository).save(session);
    }

    @Test
    void noLongerParticipate_shouldThrowBadRequest_whenUserIsNotParticipating() {
        // Arrange
        Long sessionId = 1L;
        Long userId = 10L;

        Session session = new Session();
        session.setId(sessionId);
        session.setUsers(new ArrayList<>());

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // Act + Assert
        assertThrows(BadRequestException.class, () -> sessionService.noLongerParticipate(sessionId, userId));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void noLongerParticipate_shouldThrowNotFound_whenSessionDoesNotExist() {
        // Arrange
        Long sessionId = 1L;
        Long userId = 10L;

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> sessionService.noLongerParticipate(sessionId, userId));
        verify(sessionRepository, never()).save(any());
    }
}
