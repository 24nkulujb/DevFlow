package com.devflow.backend.service;

import com.devflow.backend.dto.WorkspaceRequests.*;
import com.devflow.backend.mapper.*;
import com.devflow.backend.model.Project;
import com.devflow.backend.model.WorkspaceViews.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectService {

    private final ProjectMapper projects;
    private final MemberMapper members;
    private final UserMapper users;
    private final TaskMapper tasks;
    private final ProjectAccess access;

    public ProjectService(
        ProjectMapper projects,
        MemberMapper members,
        UserMapper users,
        TaskMapper tasks,
        ProjectAccess access
    ) {
        this.projects = projects;
        this.members = members;
        this.users = users;
        this.tasks = tasks;
        this.access = access;
    }

    public List<Project> listProjects(String username) {
        return projects.findForUser(access.userId(username));
    }

    public Project get(Long id, String username) {
        Long userId = access.userId(username);
        access.member(id, userId);
        return projects.findAccessible(id, userId);
    }

    @Transactional
    public Project createProject(ProjectInput input, String username) {
        Long userId = access.userId(username);
        Project p = new Project();
        p.setName(input.name().strip());
        p.setDescription(clean(input.description()));
        projects.insert(p);
        members.add(p.getId(), userId, "ADMIN");
        return projects.findAccessible(p.getId(), userId);
    }

    @Transactional
    public Project update(Long id, ProjectInput input, String username) {
        Long userId = access.userId(username);
        access.admin(access.lockMember(id, userId));
        Project p = new Project();
        p.setId(id);
        p.setName(input.name().strip());
        p.setDescription(clean(input.description()));
        projects.update(p);
        return projects.findAccessible(id, userId);
    }

    public List<Member> members(Long id, String username) {
        access.member(id, access.userId(username));
        return members.list(id);
    }

    public List<UserOption> candidates(Long id, String query, String username) {
        access.admin(access.member(id, access.userId(username)));
        if (query.length() > 100) throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "搜索内容过长"
        );
        return users.candidates(id, query.strip());
    }

    @Transactional
    public List<Member> addMember(Long id, MemberInput input, String username) {
        access.admin(access.lockMember(id, access.userId(username)));
        if (users.findById(input.userId()) == null) throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "用户不存在"
        );
        if (members.role(id, input.userId()) != null) throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "用户已经加入项目"
        );
        members.add(id, input.userId(), input.role());
        return members.list(id);
    }

    @Transactional
    public List<Member> changeRole(Long id, Long target, RoleInput input, String username) {
        access.admin(access.lockMember(id, access.userId(username)));
        String old = members.role(id, target);
        if (old == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "成员不存在");
        if (
            "ADMIN".equals(old) && "MEMBER".equals(input.role()) && members.countAdmins(id) <= 1
        ) throw new ResponseStatusException(HttpStatus.CONFLICT, "项目必须保留至少一名管理员");
        members.changeRole(id, target, input.role());
        return members.list(id);
    }

    @Transactional
    public void removeMember(Long id, Long target, String username) {
        Long actor = access.userId(username);
        access.admin(access.lockMember(id, actor));
        if (actor.equals(target)) throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "管理员不能移除自己"
        );
        String role = members.role(id, target);
        if (role == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "成员不存在");
        if ("ADMIN".equals(role)) throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "请先将该管理员降为普通成员"
        );
        tasks.unassignOpen(id, target);
        members.remove(id, target);
    }

    private String clean(String value) {
        return value == null ? "" : value.strip();
    }
}
