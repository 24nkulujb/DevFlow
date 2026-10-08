package com.devflow.backend.service;

import com.devflow.backend.mapper.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectAccess {

    private final MemberMapper members;
    private final ProjectMapper projects;
    private final UserMapper users;

    public ProjectAccess(MemberMapper members, ProjectMapper projects, UserMapper users) {
        this.members = members;
        this.projects = projects;
        this.users = users;
    }

    public Long userId(String username) {
        var user = users.findByUsername(username);
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录已失效");
        return user.id();
    }

    public String member(Long projectId, Long userId) {
        String role = members.role(projectId, userId);
        if (role == null) throw new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "项目不存在或你无权访问"
        );
        return role;
    }

    /** Serialize writes per project so membership checks and mutations are atomic. */
    public String lockMember(Long projectId, Long userId) {
        if (projects.lockProject(projectId) == null) throw new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "项目不存在或你无权访问"
        );
        return member(projectId, userId);
    }

    public void admin(String role) {
        if (!"ADMIN".equals(role)) throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "仅项目管理员可执行此操作"
        );
    }
}
