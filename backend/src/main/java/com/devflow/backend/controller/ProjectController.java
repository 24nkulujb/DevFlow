package com.devflow.backend.controller;

import com.devflow.backend.dto.CreateProjectRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.devflow.backend.model.Project;
import com.devflow.backend.service.ProjectService;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<Project> listProjects() {
        return projectService.listProjects();
    }

    @PostMapping
    public ResponseEntity<Project> createProject(
            @Valid @RequestBody CreateProjectRequest request
    ) {
       Project project = projectService.createProject(request);

       return ResponseEntity
               .status(HttpStatus.CREATED)
               .body(project);
    }

}
