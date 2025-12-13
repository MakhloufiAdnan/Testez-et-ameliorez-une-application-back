package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SessionMapper extends EntityMapper<SessionDto, Session> {

    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "users", ignore = true)
    Session toEntity(SessionDto dto);

    @Mapping(source = "teacher.id", target = "teacher_id")
    @Mapping(
            target = "users",
            expression = "java(session.getUsers() == null ? java.util.List.of() : session.getUsers().stream().map(u -> u.getId()).toList())"
    )
    SessionDto toDto(Session session);
}
