package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private TeacherService teacherService;

    @Test
    void findAll_shouldReturnTeacherList() {
        // Arrange
        Teacher t1 = new Teacher();
        t1.setId(1L);
        Teacher t2 = new Teacher();
        t2.setId(2L);

        when(teacherRepository.findAll()).thenReturn(List.of(t1, t2));

        // Act
        List<Teacher> result = teacherService.findAll();

        // Assert
        assertThat(result)
                .hasSize(2)
                .containsExactly(t1, t2);
        verify(teacherRepository).findAll();
    }

    @Test
    void findById_shouldReturnTeacher_whenExists() {
        // Arrange
        Long id = 1L;
        Teacher teacher = new Teacher();
        teacher.setId(id);

        when(teacherRepository.findById(id)).thenReturn(Optional.of(teacher));

        // Act
        Teacher result = teacherService.findById(id);

        // Assert
        assertThat(result).isEqualTo(teacher);
        verify(teacherRepository).findById(id);
    }

    @Test
    void findById_shouldThrowNotFoundException_whenTeacherDoesNotExist() {
        // Arrange
        Long id = 42L;
        when(teacherRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> teacherService.findById(id));
        verify(teacherRepository).findById(id);
    }
}
