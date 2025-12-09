package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeacherRepository teacherRepository;

    @BeforeEach
    void setUp() {
        // Arrange cleans the teacher database for each test
        teacherRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void findAll_shouldReturnListOfTeachers() throws Exception {
        // Arrange
        Teacher teacher1 = Teacher.builder()
                .firstName("John")
                .lastName("Doe")
                .build();
        Teacher teacher2 = Teacher.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        teacherRepository.save(teacher1);
        teacherRepository.save(teacher2);

        // Act & Assert
        mockMvc.perform(get("/api/teacher"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[0].lastName", is("Doe")))
                .andExpect(jsonPath("$[1].firstName", is("Jane")))
                .andExpect(jsonPath("$[1].lastName", is("Smith")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void findById_shouldReturnTeacher_whenExists() throws Exception {
        // Arrange
        Teacher teacher = Teacher.builder()
                .firstName("John")
                .lastName("Doe")
                .build();
        Teacher saved = teacherRepository.save(teacher);
        Long id = saved.getId();

        // Act & Assert
        mockMvc.perform(get("/api/teacher/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void findById_shouldReturn404_whenTeacherDoesNotExist() throws Exception {
        // Arrange
        Long unknownId = 999L;

        // Act & Assert
        mockMvc.perform(get("/api/teacher/{id}", unknownId))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_shouldReturn401_whenUserIsNotAuthenticated() throws Exception {
        // Arrange : aucun @WithMockUser -> pas authentifié

        // Act & Assert
        mockMvc.perform(get("/api/teacher"))
                .andExpect(status().isUnauthorized());
    }
}
