package com.devflow.backend.controller;

import com.devflow.backend.dto.WorkspaceRequests.*;
import com.devflow.backend.model.Task;
import com.devflow.backend.model.WorkspaceViews.*;
import com.devflow.backend.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping("/tasks")
    public List<Task> list(
        @PathVariable Long projectId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long assigneeId,
        @RequestParam(required = false) String priority,
        @RequestParam(defaultValue = "") String q,
        @RequestParam(defaultValue = "false") boolean overdue,
        Authentication auth
    ) {
        return service.list(projectId, status, assigneeId, priority, q, overdue, auth.getName());
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> create(
        @PathVariable Long projectId,
        @Valid @RequestBody TaskInput input,
        Authentication auth
    ) {
        return ResponseEntity.status(201).body(service.create(projectId, input, auth.getName()));
    }

    @GetMapping("/tasks/{id}")
    public Task get(@PathVariable Long projectId, @PathVariable Long id, Authentication auth) {
        return service.get(projectId, id, auth.getName());
    }

    @PutMapping("/tasks/{id}")
    public Task edit(
        @PathVariable Long projectId,
        @PathVariable Long id,
        @Valid @RequestBody TaskInput input,
        Authentication auth
    ) {
        return service.update(projectId, id, input, auth.getName());
    }

    @PatchMapping("/tasks/{id}/status")
    public Task status(
        @PathVariable Long projectId,
        @PathVariable Long id,
        @Valid @RequestBody StatusInput input,
        Authentication auth
    ) {
        return service.status(projectId, id, input, auth.getName());
    }

    @GetMapping("/stats")
    public Stats stats(@PathVariable Long projectId, Authentication auth) {
        return service.stats(projectId, auth.getName());
    }

    @GetMapping("/tasks/{id}/comments")
    public List<Comment> comments(
        @PathVariable Long projectId,
        @PathVariable Long id,
        Authentication auth
    ) {
        return service.comments(projectId, id, auth.getName());
    }

    @PostMapping("/tasks/{id}/comments")
    public ResponseEntity<List<Comment>> comment(
        @PathVariable Long projectId,
        @PathVariable Long id,
        @Valid @RequestBody CommentInput input,
        Authentication auth
    ) {
        return ResponseEntity.status(201).body(
            service.comment(projectId, id, input, auth.getName())
        );
    }
}
