package com.devflow.backend.mapper;

import com.devflow.backend.model.Project;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectMapper {
    long countProjects();

    List<Project> findAll();
}
