package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Arrange : cleans database for each test
        sessionRepository.deleteAll();
        teacherRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Teacher createTeacher(String firstName, String lastName) {
        Teacher teacher = Teacher.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build();
        return teacherRepository.save(teacher);
    }

    private User createUser(String email) {
        User user = User.builder()
                .email(email)
                .firstName("First")
                .lastName("Last")
                .password("pwd")
                .admin(false)
                .build();
        return userRepository.save(user);
    }

    private Date nowAsDate() {
        return Date.from(LocalDateTime.now()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void findAll_shouldReturnListOfSessions() throws Exception {
        // Arrange
        Teacher teacher1 = createTeacher("John", "Doe");
        Teacher teacher2 = createTeacher("Jane", "Smith");

        Session s1 = Session.builder()
                .name("Morning Yoga")
                .description("Nice morning session")
                .date(nowAsDate())
                .teacher(teacher1)
                .users(new ArrayList<>())
                .build();

        Session s2 = Session.builder()
                .name("Evening Yoga")
                .description("Relax after work")
                .date(nowAsDate())
                .teacher(teacher2)
                .users(new ArrayList<>())
                .build();

        sessionRepository.save(s1);
        sessionRepository.save(s2);

        // Act & Assert
        mockMvc.perform(get("/api/session"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Morning Yoga")))
                .andExpect(jsonPath("$[1].name", is("Evening Yoga")));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void findById_shouldReturnSession_whenExists() throws Exception {
        // Arrange
        Teacher teacher = createTeacher("John", "Doe");

        Session session = Session.builder()
                .name("Morning Yoga")
                .description("Nice morning session")
                .date(nowAsDate())
                .teacher(teacher)
                .users(new ArrayList<>())
                .build();

        Session saved = sessionRepository.save(session);
        Long id = saved.getId();

        // Act & Assert
        mockMvc.perform(get("/api/session/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.name", is("Morning Yoga")))
                .andExpect(jsonPath("$.description", is("Nice morning session")))
                .andExpect(jsonPath("$.teacher_id", is(teacher.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void findById_shouldReturn404_whenSessionDoesNotExist() throws Exception {
        // Arrange
        Long unknownId = 999L;

        // Act & Assert
        mockMvc.perform(get("/api/session/{id}", unknownId))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_shouldReturn401_whenNotAuthenticated() throws Exception {
        // Arrange : no @WithMockUser

        // Act & Assert
        mockMvc.perform(get("/api/session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void create_shouldCreateSession_whenDataIsValid() throws Exception {
        // Arrange
        Teacher teacher = createTeacher("John", "Doe");

        String jsonBody = """
                {
                  "name": "New Yoga Session",
                  "description": "Relax and stretch",
                  "date": "2025-01-01T10:00:00.000+00:00",
                  "teacher_id": %d
                }
                """.formatted(teacher.getId());

        // Act & Assert
        mockMvc.perform(post("/api/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("New Yoga Session")))
                .andExpect(jsonPath("$.description", is("Relax and stretch")))
                .andExpect(jsonPath("$.teacher_id", is(teacher.getId().intValue())));

        // Assert (base)
        assertThat(sessionRepository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void update_shouldUpdateSession_whenSessionExists() throws Exception {
        // Arrange
        Teacher teacher = createTeacher("John", "Doe");

        Session session = Session.builder()
                .name("Old name")
                .description("Old desc")
                .date(nowAsDate())
                .teacher(teacher)
                .users(new ArrayList<>())
                .build();

        Session saved = sessionRepository.save(session);
        Long id = saved.getId();

        String jsonBody = """
                {
                  "id": %d,
                  "name": "Updated name",
                  "description": "Updated desc",
                  "date": "2025-01-02T10:00:00.000+00:00",
                  "teacher_id": %d
                }
                """.formatted(id, teacher.getId());

        // Act & Assert
        mockMvc.perform(put("/api/session/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.name", is("Updated name")))
                .andExpect(jsonPath("$.description", is("Updated desc")));

        // Assert (base)
        Session updated = sessionRepository.findById(id).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated name");
        assertThat(updated.getDescription()).isEqualTo("Updated desc");
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void delete_shouldDeleteSession_whenSessionExists() throws Exception {
        // Arrange
        Teacher teacher = createTeacher("John", "Doe");

        Session session = Session.builder()
                .name("To delete")
                .description("To delete desc")
                .date(nowAsDate())
                .teacher(teacher)
                .users(new ArrayList<>())
                .build();

        Session saved = sessionRepository.save(session);
        Long id = saved.getId();

        // Act & Assert
        mockMvc.perform(delete("/api/session/{id}", id))
                .andExpect(status().isOk());

        // Assert (base)
        boolean exists = sessionRepository.findById(id).isPresent();
        assertThat(exists).isFalse();
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void participate_shouldAddUser_whenNotAlreadyParticipating() throws Exception {
        // Arrange
        Teacher teacher = createTeacher("John", "Doe");
        User user = createUser("user@example.com");

        Session session = Session.builder()
                .name("Participation session")
                .description("Join us")
                .date(nowAsDate())
                .teacher(teacher)
                .users(new ArrayList<>())
                .build();

        Session saved = sessionRepository.save(session);
        Long sessionId = saved.getId();
        Long userId = user.getId();

        // Act & Assert
        mockMvc.perform(post("/api/session/{id}/participate/{userId}", sessionId, userId))
                .andExpect(status().isOk());

        // Assert (base)
        Session updated = sessionRepository.findById(sessionId).orElseThrow();
        assertThat(updated.getUsers())
                .extracting(User::getId)
                .containsExactly(userId);
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void noLongerParticipate_shouldRemoveUser_whenParticipating() throws Exception {
        // Arrange
        Teacher teacher = createTeacher("John", "Doe");
        User user = createUser("user@example.com");

        Session session = Session.builder()
                .name("Participation session")
                .description("Join us")
                .date(nowAsDate())
                .teacher(teacher)
                .users(new ArrayList<>())
                .build();

        session.getUsers().add(user);
        Session saved = sessionRepository.save(session);
        Long sessionId = saved.getId();
        Long userId = user.getId();

        // Act & Assert
        mockMvc.perform(delete("/api/session/{id}/participate/{userId}", sessionId, userId))
                .andExpect(status().isOk());

        // Assert (base)
        Session updated = sessionRepository.findById(sessionId).orElseThrow();
        assertThat(updated.getUsers()).isEmpty();
    }
}
