package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.exception.UnauthorizedException;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(Long id) {
        return this.userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    public void delete(Long id) {
        User user = findById(id);
        this.userRepository.delete(user);
    }

    public void deleteIfOwner(Long id, String requesterEmail) {
        User user = findById(id);

        if (!Objects.equals(requesterEmail, user.getEmail())) {
            throw new UnauthorizedException();
        }

        this.userRepository.delete(user);
    }

    public boolean existsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public User save(User user) {
        return this.userRepository.save(user);
    }

    public User findByEmail(String email) {
        return this.userRepository.findByEmail(email)
                .orElseThrow(NotFoundException::new);
    }
}
