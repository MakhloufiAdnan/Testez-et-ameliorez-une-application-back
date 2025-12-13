package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldThrowBadRequest_whenEmailAlreadyTaken() {
        // Arrange : email déjà pris
        SignupRequest req = new SignupRequest();
        req.setEmail("taken@test.com");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setPassword("secret123");

        when(userService.existsByEmail(req.getEmail())).thenReturn(true);

        // Act + Assert
        assertThrows(BadRequestException.class, () -> authService.register(req));

        // On ne doit PAS encoder / sauver
        verify(passwordEncoder, never()).encode(any());
        verify(userService, never()).save(any());
    }

    @Test
    void register_shouldEncodePassword_andSaveUser_whenEmailIsFree() {
        // Arrange : email libre
        SignupRequest req = new SignupRequest();
        req.setEmail("new@test.com");
        req.setFirstName("Jane");
        req.setLastName("Smith");
        req.setPassword("secret123");

        when(userService.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("hashed_pwd");

        // Act
        authService.register(req);

        // Assert : on a bien sauvegardé un User avec password hashé
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(req.getEmail());
        assertThat(saved.getFirstName()).isEqualTo(req.getFirstName());
        assertThat(saved.getLastName()).isEqualTo(req.getLastName());
        assertThat(saved.getPassword()).isEqualTo("hashed_pwd");
        assertThat(saved.isAdmin()).isFalse();

        verify(passwordEncoder).encode(req.getPassword());
    }
}
