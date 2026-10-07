package com.devflow.backend.service;

import com.devflow.backend.mapper.ProjectMapper;
import com.devflow.backend.model.Project;
import org.springframework.stereotype.Service;

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



}
