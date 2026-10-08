package com.devflow.backend.service;

import com.devflow.backend.mapper.ProjectMapper;
import com.devflow.backend.model.Project;
import com.devflow.backend.dto.CreateProjectRequest;
import org.springframework.stereotype.Service;

import java.beans.PropertyEditorSupport;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectMapper projectMapper;

    public ProjectService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public List<Project> listProjects() {
        return projectMapper.findAll();
    }

    public Project createProject(CreateProjectRequest request) {
        Project project = new Project();

        project.setName(request.name().strip());
        project.setDescription(
                request.description() == null
                ? "" : request.description().strip()
        );
        int rows = projectMapper.insert(project);

        if (rows != 1) {
            throw new IllegalStateException("项目创建失败");
        }

        return project;
    }

}
