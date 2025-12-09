package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Arrange : cleans the user database for each test
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void findById_shouldReturnUser_whenExists() throws Exception {
        // Arrange
        User user = User.builder()
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("pwd")
                .admin(false)
                .build();
        User saved = userRepository.save(user);
        Long id = saved.getId();

        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.admin", is(false)));
    }

    @Test
    @WithMockUser(username = "any@example.com")
    void findById_shouldReturn404_whenUserDoesNotExist() throws Exception {
        // Arrange
        Long unknownId = 999L;

        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", unknownId))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_shouldReturn401_whenUserIsNotAuthenticated() throws Exception {
        // Arrange : no @WithMockUser

        // Act & Assert
        mockMvc.perform(get("/api/user/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void delete_shouldReturn200_whenAuthenticatedUserDeletesOwnAccount() throws Exception {
        // Arrange
        User user = User.builder()
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("pwd")
                .admin(false)
                .build();
        User saved = userRepository.save(user);
        Long id = saved.getId();

        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", id))
                .andExpect(status().isOk());

        // Assert (verification database)
        boolean exists = userRepository.findById(id).isPresent();
        org.assertj.core.api.Assertions.assertThat(exists).isFalse();
    }

    @Test
    @WithMockUser(username = "other.user@example.com")
    void delete_shouldReturn401_whenAuthenticatedUserIsDifferent() throws Exception {
        // Arrange
        User user = User.builder()
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("pwd")
                .admin(false)
                .build();
        User saved = userRepository.save(user);
        Long id = saved.getId();

        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", id))
                .andExpect(status().isUnauthorized());

        // Assert : The user has not been deleted
        boolean exists = userRepository.findById(id).isPresent();
        org.assertj.core.api.Assertions.assertThat(exists).isTrue();
    }

    @Test
    @WithMockUser(username = "someone@example.com")
    void delete_shouldReturn404_whenUserDoesNotExist() throws Exception {
        // Arrange
        Long unknownId = 999L;

        // Act & Assert
        mockMvc.perform(delete("/api/user/{id}", unknownId))
                .andExpect(status().isNotFound());
    }
}
