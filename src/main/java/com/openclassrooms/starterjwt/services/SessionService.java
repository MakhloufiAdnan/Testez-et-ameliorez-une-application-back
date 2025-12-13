package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final TeacherService teacherService;

    public SessionService(SessionRepository sessionRepository,
                          UserRepository userRepository,
                          TeacherService teacherService) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.teacherService = teacherService;
    }

    public Session create(Session session) {
        return this.sessionRepository.save(session);
    }

    public Session update(Long id, Session incoming) {
        Session existing = this.sessionRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        existing.setName(incoming.getName());
        existing.setDate(incoming.getDate());
        existing.setDescription(incoming.getDescription());

        if (incoming.getTeacher() != null) {
            existing.setTeacher(incoming.getTeacher());
        }
        if (incoming.getUsers() != null) {
            existing.setUsers(incoming.getUsers());
        }

        return this.sessionRepository.save(existing);
    }

    public Session create(Session session, SessionDto dto) {
        applyRelations(session, dto, null);
        return this.sessionRepository.save(session);
    }

    public Session update(Long id, Session incoming, SessionDto dto) {
        Session existing = this.sessionRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        existing.setName(incoming.getName());
        existing.setDate(incoming.getDate());
        existing.setDescription(incoming.getDescription());

        applyRelations(existing, dto, existing);

        return this.sessionRepository.save(existing);
    }

    public List<Session> findAll() {
        return this.sessionRepository.findAll();
    }

    public Session getById(Long id) {
        return this.sessionRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    public void delete(Long id) {
        Session session = this.sessionRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        this.sessionRepository.delete(session);
    }

    public void participate(Long id, Long userId) {
        Session session = this.sessionRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        User user = this.userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);

        boolean alreadyParticipate = session.getUsers().stream()
                .anyMatch(o -> o.getId().equals(userId));
        if (alreadyParticipate) {
            throw new BadRequestException();
        }

        session.getUsers().add(user);
        this.sessionRepository.save(session);
    }

    public void noLongerParticipate(Long id, Long userId) {
        Session session = this.sessionRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        boolean alreadyParticipate = session.getUsers().stream()
                .anyMatch(o -> o.getId().equals(userId));
        if (!alreadyParticipate) {
            throw new BadRequestException();
        }

        session.setUsers(
                new ArrayList<>(
                        session.getUsers().stream()
                                .filter(u -> !u.getId().equals(userId))
                                .toList()
                )
        );

        this.sessionRepository.save(session);
    }

    private void applyRelations(Session target, SessionDto dto, Session existing) {
        Teacher teacher = teacherService.findById(dto.getTeacher_id());
        target.setTeacher(teacher);

        if (dto.getUsers() == null) {
            if (existing == null) {
                target.setUsers(new ArrayList<>());
            }
            return;
        }

        List<Long> userIds = dto.getUsers() == null ? Collections.emptyList() : dto.getUsers();

        List<User> users = userIds.stream()
                .map(uid -> userRepository.findById(uid).orElseThrow(NotFoundException::new))
                .toList();

        target.setUsers(new ArrayList<>(users));
    }
}
