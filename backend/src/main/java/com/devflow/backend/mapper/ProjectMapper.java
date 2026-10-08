package com.devflow.backend.mapper;

import com.devflow.backend.model.Project;
import java.util.List;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ProjectMapper {
    long countProjects();
    List<Project> findAll();
    List<Project> findForUser(@Param("userId") Long userId);
    Project findAccessible(@Param("id") Long id, @Param("userId") Long userId);
    Long lockProject(@Param("id") Long id);
    int insert(Project project);
    int update(Project project);
}
