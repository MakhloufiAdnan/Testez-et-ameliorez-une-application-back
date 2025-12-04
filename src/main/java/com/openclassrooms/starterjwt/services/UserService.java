package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.springframework.stereotype.Service;

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
        // vérifier l’existence d’un utilisateur + le supprimer ensuite
        User user = this.userRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        this.userRepository.delete(user);
    }
}
