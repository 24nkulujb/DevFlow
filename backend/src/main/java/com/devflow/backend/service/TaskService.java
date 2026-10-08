package com.devflow.backend.service;

import com.devflow.backend.dto.WorkspaceRequests.*;
import com.devflow.backend.mapper.*;
import com.devflow.backend.model.Task;
import com.devflow.backend.model.WorkspaceViews.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskService {

    private final TaskMapper tasks;
    private final MemberMapper members;
    private final ProjectAccess access;

    public TaskService(TaskMapper tasks, MemberMapper members, ProjectAccess access) {
        this.tasks = tasks;
        this.members = members;
        this.access = access;
    }

    public List<Task> list(
        Long projectId,
        String status,
        Long assigneeId,
        String priority,
        String query,
        boolean overdue,
        String username
    ) {
        access.member(projectId, access.userId(username));
        if (
            status != null &&
            !status.isBlank() &&
            !Set.of("TODO", "IN_PROGRESS", "DONE").contains(status)
        ) bad("无效的状态筛选");
        if (
            priority != null &&
            !priority.isBlank() &&
            !Set.of("LOW", "MEDIUM", "HIGH").contains(priority)
        ) bad("无效的优先级筛选");
        if (query.length() > 100) bad("搜索内容过长");
        return tasks.list(projectId, status, assigneeId, priority, query.strip(), overdue, today());
    }

    public Task get(Long projectId, Long id, String username) {
        access.member(projectId, access.userId(username));
        return existing(projectId, id);
    }

    @Transactional
    public Task create(Long projectId, TaskInput input, String username) {
        Long actor = access.userId(username);
        access.lockMember(projectId, actor);
        assignee(projectId, input.assigneeId());
        Task t = new Task();
        t.setProjectId(projectId);
        t.setCreatorId(actor);
        apply(t, input);
        t.setStatus("TODO");
        tasks.insert(t);
        return existing(projectId, t.getId());
    }

    @Transactional
    public Task update(Long projectId, Long id, TaskInput input, String username) {
        Long actor = access.userId(username);
        String role = access.lockMember(projectId, actor);
        Task t = existing(projectId, id);
        editable(t, actor, role);
        if (input.version() == null) bad("缺少任务版本号，请刷新后重试");
        boolean keepHistoricalAssignee =
            "DONE".equals(t.getStatus()) &&
            "DONE".equals(input.status()) &&
            input.assigneeId() != null &&
            input.assigneeId().equals(t.getAssigneeId());
        if (!keepHistoricalAssignee) assignee(projectId, input.assigneeId());
        apply(t, input);
        t.setVersion(input.version());
        save(t);
        return existing(projectId, id);
    }

    @Transactional
    public Task status(Long projectId, Long id, StatusInput input, String username) {
        Long actor = access.userId(username);
        String role = access.lockMember(projectId, actor);
        Task t = existing(projectId, id);
        editable(t, actor, role);
        t.setStatus(input.status());
        t.setVersion(input.version());
        if (
            !"DONE".equals(input.status()) &&
            t.getAssigneeId() != null &&
            members.role(projectId, t.getAssigneeId()) == null
        ) t.setAssigneeId(null);
        save(t);
        return existing(projectId, id);
    }

    public Stats stats(Long projectId, String username) {
        access.member(projectId, access.userId(username));
        return tasks.stats(projectId, today());
    }

    public List<Comment> comments(Long projectId, Long taskId, String username) {
        access.member(projectId, access.userId(username));
        existing(projectId, taskId);
        return tasks.comments(taskId);
    }

    @Transactional
    public List<Comment> comment(Long projectId, Long taskId, CommentInput input, String username) {
        Long actor = access.userId(username);
        access.lockMember(projectId, actor);
        existing(projectId, taskId);
        tasks.addComment(taskId, actor, input.content().strip());
        return tasks.comments(taskId);
    }

    private Task existing(Long projectId, Long id) {
        Task t = tasks.find(projectId, id);
        if (t == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "任务不存在");
        return t;
    }

    private void editable(Task t, Long actor, String role) {
        if (
            !"ADMIN".equals(role) &&
            !actor.equals(t.getCreatorId()) &&
            !actor.equals(t.getAssigneeId())
        ) throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "仅管理员、创建者或负责人可修改任务"
        );
    }

    private void assignee(Long projectId, Long id) {
        if (id != null && members.role(projectId, id) == null) bad("负责人必须是项目成员");
    }

    private void apply(Task t, TaskInput input) {
        t.setTitle(input.title().strip());
        t.setDescription(input.description() == null ? "" : input.description().strip());
        t.setStatus(input.status());
        t.setPriority(input.priority());
        t.setAssigneeId(input.assigneeId());
        t.setDueDate(input.dueDate());
    }

    private void save(Task t) {
        if (tasks.update(t) != 1) throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "任务已被其他人修改，请刷新后重试"
        );
    }

    private void bad(String message) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private LocalDate today() {
        return LocalDate.now(ZoneId.of("Asia/Shanghai"));
    }
}
