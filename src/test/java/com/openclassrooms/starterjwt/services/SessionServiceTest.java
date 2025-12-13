package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeacherService teacherService;

    @InjectMocks
    private SessionService sessionService;

    private Session baseSession() {
        Session s = new Session();
        s.setName("Yoga");
        s.setDate(new Date());
        s.setDescription("Desc");
        s.setUsers(new ArrayList<>());
        return s;
    }

    private SessionDto baseDto(Long teacherId, List<Long> userIds) {
        SessionDto dto = new SessionDto();
        dto.setName("Yoga");
        dto.setDate(new Date());
        dto.setDescription("Desc");

        dto.setTeacher_id(teacherId);

        dto.setUsers(userIds);
        return dto;
    }

    @Test
    void create_shouldSaveSession_legacySignature() {
        // Arrange
        Session session = baseSession();
        when(sessionRepository.save(session)).thenReturn(session);

        // Act
        Session result = sessionService.create(session);

        // Assert
        assertThat(result).isEqualTo(session);
        verify(sessionRepository).save(session);
    }

    @Test
    void update_shouldSaveSession_legacySignature_whenExists() {
        // Arrange
        Long id = 1L;

        Session existing = new Session();
        existing.setId(id);
        existing.setName("Old");

        Session incoming = new Session();
        incoming.setName("New");
        incoming.setDate(new Date());
        incoming.setDescription("New desc");

        when(sessionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Session result = sessionService.update(id, incoming);

        // Assert
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("New");
        verify(sessionRepository).save(existing); // le service merge dans existing puis save(existing)
    }

    @Test
    void update_shouldThrowNotFound_legacySignature_whenMissing() {
        // Arrange
        Long id = 99L;
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> sessionService.update(id, new Session()));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void create_withDto_shouldSetTeacher_andSetUsersEmpty_whenDtoUsersNull() {

        // Arrange
        Long teacherId = 1L;

        Session session = baseSession();
        SessionDto dto = baseDto(teacherId, null);

        Teacher teacher = new Teacher();
        teacher.setId(teacherId);

        when(teacherService.findById(teacherId)).thenReturn(teacher);
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Session saved = sessionService.create(session, dto);

        // Assert
        assertThat(saved.getTeacher()).isEqualTo(teacher);
        assertThat(saved.getUsers()).isNotNull().isEmpty();
        verify(teacherService).findById(teacherId);
        verify(userRepository, never()).findById(any());
        verify(sessionRepository).save(session);
    }

    @Test
    void create_withDto_shouldResolveUsers_whenDtoUsersProvided() {
        // Arrange
        Long teacherId = 1L;
        Long userId = 10L;

        Session session = baseSession();
        SessionDto dto = baseDto(teacherId, List.of(userId));

        Teacher teacher = new Teacher();
        teacher.setId(teacherId);

        User user = new User();
        user.setId(userId);

        when(teacherService.findById(teacherId)).thenReturn(teacher);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Session saved = sessionService.create(session, dto);

        // Assert
        assertThat(saved.getTeacher()).isEqualTo(teacher);
        assertThat(saved.getUsers()).containsExactly(user);
        verify(userRepository).findById(userId);
        verify(sessionRepository).save(session);
    }

    @Test
    void create_withDto_shouldThrowNotFound_whenUserMissing() {
        // Arrange
        Long teacherId = 1L;
        Long missingUserId = 99L;

        Session session = baseSession();
        SessionDto dto = baseDto(teacherId, List.of(missingUserId));

        Teacher teacher = new Teacher();
        teacher.setId(teacherId);

        when(teacherService.findById(teacherId)).thenReturn(teacher);
        when(userRepository.findById(missingUserId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> sessionService.create(session, dto));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void update_withDto_shouldSave_andReplaceUsers_whenDtoUsersProvided() {
        // Arrange
        Long id = 1L;
        Long teacherId = 2L;
        Long userId = 10L;

        Session existing = baseSession();
        existing.setId(id);
        existing.setUsers(new ArrayList<>(List.of(new User()))); // ancien users

        Session incoming = baseSession();
        incoming.setName("Updated name");

        SessionDto dto = baseDto(teacherId, List.of(userId));

        Teacher teacher = new Teacher();
        teacher.setId(teacherId);

        User user = new User();
        user.setId(userId);

        when(sessionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(teacherService.findById(teacherId)).thenReturn(teacher);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Session updated = sessionService.update(id, incoming, dto);

        // Assert
        assertThat(updated.getId()).isEqualTo(id);
        assertThat(updated.getName()).isEqualTo("Updated name");
        assertThat(updated.getTeacher()).isEqualTo(teacher);
        assertThat(updated.getUsers()).containsExactly(user); // remplacés par ceux du dto
        verify(sessionRepository).save(existing);
    }

    @Test
    void update_withDto_shouldKeepExistingUsers_whenDtoUsersNull() {

        // Arrange
        Long id = 1L;
        Long teacherId = 2L;

        User existingUser = new User();
        existingUser.setId(77L);

        Session existing = baseSession();
        existing.setId(id);
        existing.setUsers(new ArrayList<>(List.of(existingUser)));

        Session incoming = baseSession();
        incoming.setName("Updated name");

        SessionDto dto = baseDto(teacherId, null); // users null -> doit conserver

        Teacher teacher = new Teacher();
        teacher.setId(teacherId);

        when(sessionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(teacherService.findById(teacherId)).thenReturn(teacher);
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Session updated = sessionService.update(id, incoming, dto);

        // Assert
        assertThat(updated.getUsers()).containsExactly(existingUser); // inchangé
        verify(userRepository, never()).findById(any());
        verify(sessionRepository).save(existing);
    }

    @Test
    void update_withDto_shouldThrowNotFound_whenSessionMissing() {

        // Arrange
        Long id = 999L;
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        Session incoming = baseSession();
        SessionDto dto = baseDto(1L, null);

        // Act + Assert
        assertThrows(NotFoundException.class, () -> sessionService.update(id, incoming, dto));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void participate_shouldThrowBadRequest_whenUserAlreadyParticipates() {
        // Arrange
        Long sessionId = 1L;
        Long userId = 10L;

        User user = new User();
        user.setId(userId);

        Session session = baseSession();
        session.setId(sessionId);
        session.setUsers(new ArrayList<>(List.of(user)));

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act + Assert
        assertThrows(BadRequestException.class, () -> sessionService.participate(sessionId, userId));
        verify(sessionRepository, never()).save(any());
    }
}
