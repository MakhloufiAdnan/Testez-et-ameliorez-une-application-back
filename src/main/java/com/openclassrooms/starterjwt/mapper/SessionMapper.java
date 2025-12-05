package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.services.TeacherService;
import com.openclassrooms.starterjwt.services.UserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        imports = {Collections.class, Optional.class, Collectors.class}
)
public abstract class SessionMapper implements EntityMapper<SessionDto, Session> {

    @Autowired
    protected TeacherService teacherService;

    @Autowired
    protected UserService userService;

    @Mappings({
            @Mapping(source = "description", target = "description"),
            @Mapping(
                    target = "teacher",
                    expression = "java(sessionDto.getTeacher_id() != null ? teacherService.findById(sessionDto.getTeacher_id()) : null)"
            ),
            @Mapping(
                    target = "users",
                    expression = "java(Optional.ofNullable(sessionDto.getUsers())" +
                            ".orElseGet(Collections::emptyList)" +
                            ".stream()" +
                            ".map(userId -> userService.findById(userId))" +
                            ".collect(Collectors.toList()))"
            )
    })
    public abstract Session toEntity(SessionDto sessionDto);

    @Mappings({
            @Mapping(source = "description", target = "description"),
            @Mapping(source = "session.teacher.id", target = "teacher_id"),
            @Mapping(
                    target = "users",
                    expression = "java(Optional.ofNullable(session.getUsers())" +
                            ".orElseGet(Collections::emptyList)" +
                            ".stream()" +
                            ".map(u -> u.getId())" +
                            ".collect(Collectors.toList()))"
            )
    })
    public abstract SessionDto toDto(Session session);
}
