package com.devflow.backend.controller;

import com.devflow.backend.dto.WorkspaceRequests.*;
import com.devflow.backend.model.Project;
import com.devflow.backend.model.WorkspaceViews.*;
import com.devflow.backend.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<Project> list(Authentication auth) {
        return service.listProjects(auth.getName());
    }

    @PostMapping
    public ResponseEntity<Project> create(
        @Valid @RequestBody ProjectInput input,
        Authentication auth
    ) {
        return ResponseEntity.status(201).body(service.createProject(input, auth.getName()));
    }

    @GetMapping("/{id}")
    public Project get(@PathVariable Long id, Authentication auth) {
        return service.get(id, auth.getName());
    }

    @PutMapping("/{id}")
    public Project update(
        @PathVariable Long id,
        @Valid @RequestBody ProjectInput input,
        Authentication auth
    ) {
        return service.update(id, input, auth.getName());
    }

    @GetMapping("/{id}/members")
    public List<Member> members(@PathVariable Long id, Authentication auth) {
        return service.members(id, auth.getName());
    }

    @GetMapping("/{id}/users")
    public List<UserOption> candidates(
        @PathVariable Long id,
        @RequestParam(defaultValue = "") String q,
        Authentication auth
    ) {
        return service.candidates(id, q, auth.getName());
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<List<Member>> add(
        @PathVariable Long id,
        @Valid @RequestBody MemberInput input,
        Authentication auth
    ) {
        return ResponseEntity.status(201).body(service.addMember(id, input, auth.getName()));
    }

    @PatchMapping("/{id}/members/{userId}")
    public List<Member> role(
        @PathVariable Long id,
        @PathVariable Long userId,
        @Valid @RequestBody RoleInput input,
        Authentication auth
    ) {
        return service.changeRole(id, userId, input, auth.getName());
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> remove(
        @PathVariable Long id,
        @PathVariable Long userId,
        Authentication auth
    ) {
        service.removeMember(id, userId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
