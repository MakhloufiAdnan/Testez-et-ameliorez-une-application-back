package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {
    @Mock TeacherRepository teacherRepository;
    @InjectMocks TeacherService teacherService;

    @Test
    void findById_shouldThrowNotFound_whenMissing() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> teacherService.findById(1L));
    }
}
