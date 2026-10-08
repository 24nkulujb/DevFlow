package com.devflow.backend.mapper;

import com.devflow.backend.model.Task;
import com.devflow.backend.model.WorkspaceViews.*;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TaskMapper {
    List<Task> list(
        @Param("projectId") Long projectId,
        @Param("status") String status,
        @Param("assigneeId") Long assigneeId,
        @Param("priority") String priority,
        @Param("query") String query,
        @Param("overdue") boolean overdue,
        @Param("today") LocalDate today
    );
    Task find(@Param("projectId") Long projectId, @Param("id") Long id);
    int insert(Task task);
    int update(Task task);
    int unassignOpen(@Param("projectId") Long projectId, @Param("userId") Long userId);
    Stats stats(@Param("projectId") Long projectId, @Param("today") LocalDate today);
    List<Comment> comments(@Param("taskId") Long taskId);
    int addComment(
        @Param("taskId") Long taskId,
        @Param("authorId") Long authorId,
        @Param("content") String content
    );
}
