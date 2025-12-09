package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Arrange : cleans the user database for each test
        userRepository.deleteAll();
    }

    @Test
    void login_shouldReturnJwt_whenCredentialsAreValid() throws Exception {
        // Arrange
        String email = "john.doe@example.com";
        String rawPassword = "password123";

        User user = User.builder()
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .password(passwordEncoder.encode(rawPassword))
                .admin(false)
                .build();
        userRepository.save(user);

        String jsonBody = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, rawPassword);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.id").value(user.getId().intValue()))
                .andExpect(jsonPath("$.username").value(email))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    void login_shouldReturn401_whenCredentialsAreInvalid() throws Exception {
        // Arrange
        String email = "john.doe@example.com";
        String rawPassword = "password123";

        User user = User.builder()
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .password(passwordEncoder.encode(rawPassword))
                .admin(false)
                .build();
        userRepository.save(user);

        String jsonBody = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, "wrongPassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_shouldCreateUser_whenEmailNotTaken() throws Exception {
        // Arrange
        String email = "new.user@example.com";

        String jsonBody = """
                {
                  "email": "%s",
                  "firstName": "New",
                  "lastName": "User",
                  "password": "strongPass123"
                }
                """.formatted(email);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Assert (base)
        boolean exists = userRepository.existsByEmail(email);
        assertThat(exists).isTrue();
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailAlreadyTaken() throws Exception {
        // Arrange
        String email = "existing.user@example.com";

        User existing = User.builder()
                .email(email)
                .firstName("Existing")
                .lastName("User")
                .password(passwordEncoder.encode("password"))
                .admin(false)
                .build();
        userRepository.save(existing);

        String jsonBody = """
                {
                  "email": "%s",
                  "firstName": "New",
                  "lastName": "User",
                  "password": "strongPass123"
                }
                """.formatted(email);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Error: Email is already taken!"));
    }
}
