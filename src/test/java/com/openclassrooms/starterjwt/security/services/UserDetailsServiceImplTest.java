package com.openclassrooms.starterjwt.security.services;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        // Arrange (construction du service avec le mock)
        userDetailsService = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // Arrange
        String email = "test@example.com";

        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("hashedPassword");
        user.setAdmin(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(result).isInstanceOf(UserDetailsImpl.class);
        UserDetailsImpl userDetails = (UserDetailsImpl) result;

        assertThat(userDetails.getId()).isEqualTo(user.getId());
        assertThat(userDetails.getUsername()).isEqualTo(user.getEmail());
        assertThat(userDetails.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userDetails.getLastName()).isEqualTo(user.getLastName());
        assertThat(userDetails.getPassword()).isEqualTo(user.getPassword());
        assertThat(userDetails.getAdmin()).isTrue();

        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserDoesNotExist() {
        // Arrange
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act + Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email)
        );

        assertThat(exception.getMessage())
                .isEqualTo("User Not Found with email: " + email);

        verify(userRepository).findByEmail(email);
    }
}
