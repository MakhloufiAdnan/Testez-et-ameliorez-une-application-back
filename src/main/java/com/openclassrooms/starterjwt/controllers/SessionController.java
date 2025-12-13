package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.services.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final SessionMapper sessionMapper;
    private final SessionService sessionService;

    public SessionController(SessionService sessionService, SessionMapper sessionMapper) {
        this.sessionMapper = sessionMapper;
        this.sessionService = sessionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDto> findById(@PathVariable Long id) {
        Session session = this.sessionService.getById(id);
        return ResponseEntity.ok(this.sessionMapper.toDto(session));
    }

    @GetMapping
    public ResponseEntity<List<SessionDto>> findAll() {
        List<Session> sessions = this.sessionService.findAll();
        return ResponseEntity.ok(this.sessionMapper.toDto(sessions));
    }

    @PostMapping
    public ResponseEntity<SessionDto> create(@Valid @RequestBody SessionDto dto) {
        Session session = this.sessionMapper.toEntity(dto);
        Session saved = this.sessionService.create(session, dto);
        return ResponseEntity.ok(this.sessionMapper.toDto(saved));
    }

    @PutMapping("{id}")
    public ResponseEntity<SessionDto> update(@PathVariable Long id,
                                             @Valid @RequestBody SessionDto dto) {
        Session incoming = this.sessionMapper.toEntity(dto);
        Session updated = this.sessionService.update(id, incoming, dto);
        return ResponseEntity.ok(this.sessionMapper.toDto(updated));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.sessionService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("{id}/participate/{userId}")
    public ResponseEntity<Void> participate(@PathVariable Long id, @PathVariable Long userId) {
        this.sessionService.participate(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}/participate/{userId}")
    public ResponseEntity<Void> noLongerParticipate(@PathVariable Long id, @PathVariable Long userId) {
        this.sessionService.noLongerParticipate(id, userId);
        return ResponseEntity.ok().build();
    }
}
