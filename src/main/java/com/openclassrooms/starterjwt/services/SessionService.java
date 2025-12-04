package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionService(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    public Session create(Session session) {
        return this.sessionRepository.save(session);
    }

    public List<Session> findAll() {
        return this.sessionRepository.findAll();
    }

    public Session getById(Long id) {
        return this.sessionRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    public Session update(Long id, Session session) {
        // Vérifie que la session existe avant de la mettre à jour
        if (!this.sessionRepository.existsById(id)) {
            throw new NotFoundException();
        }
        session.setId(id);
        return this.sessionRepository.save(session);
    }

    public void delete(Long id) {
        // rechercher une session et la supprimer ensuite
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
                                .filter(user -> !user.getId().equals(userId))
                                .toList()
                )
        );

        this.sessionRepository.save(session);
    }
}
