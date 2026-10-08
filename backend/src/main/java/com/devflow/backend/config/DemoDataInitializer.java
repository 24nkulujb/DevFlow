package com.devflow.backend.config;

import com.devflow.backend.mapper.*;
import com.devflow.backend.model.*;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("demo")
public class DemoDataInitializer implements ApplicationRunner {

    private final UserMapper users;
    private final ProjectMapper projects;
    private final MemberMapper members;
    private final TaskMapper tasks;
    private final PasswordEncoder encoder;
    private final JdbcTemplate jdbc;

    public DemoDataInitializer(
        UserMapper users,
        ProjectMapper projects,
        MemberMapper members,
        TaskMapper tasks,
        PasswordEncoder encoder,
        JdbcTemplate jdbc
    ) {
        this.users = users;
        this.projects = projects;
        this.members = members;
        this.tasks = tasks;
        this.encoder = encoder;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM app_setting WHERE setting_key='demo-v1-initialized'",
                Long.class
            ) > 0
        ) return;
        seedUser("alice", "Alice");
        seedUser("bob", "Bob");
        seedUser("charlie", "Charlie");
        Long alice = users.findByUsername("alice").id(),
            bob = users.findByUsername("bob").id(),
            charlie = users.findByUsername("charlie").id();
        if (projects.countProjects() == 0) {
            Project p = new Project();
            p.setName("DevFlow · 团队协作平台");
            p.setDescription("从需求设计到上线，打造可演示的全栈作品集。");
            projects.insert(p);
        }
        // Adopt only legacy projects without any membership. Never restore removed members.
        for (Project p : projects.findAll()) {
            if (members.list(p.getId()).isEmpty()) {
                members.add(p.getId(), alice, "ADMIN");
                members.add(p.getId(), bob, "MEMBER");
                members.add(p.getId(), charlie, "MEMBER");
                seedTasks(p.getId(), alice, bob);
            }
        }
        String privateName = "Bob 的产品实验室";
        boolean exists = projects
            .findForUser(bob)
            .stream()
            .anyMatch(p -> privateName.equals(p.getName()));
        if (!exists) {
            Project p = new Project();
            p.setName(privateName);
            p.setDescription("仅 Bob 和 Charlie 可见，用来演示项目数据隔离。");
            projects.insert(p);
            members.add(p.getId(), bob, "ADMIN");
            members.add(p.getId(), charlie, "MEMBER");
            seedTasks(p.getId(), bob, charlie);
        }
        jdbc.update(
            "INSERT INTO app_setting(setting_key,setting_value) VALUES('demo-v1-initialized','true')"
        );
    }

    private void seedUser(String username, String displayName) {
        if (users.findByUsername(username) == null) users.insert(
            username,
            encoder.encode("DevFlow123!"),
            displayName
        );
    }

    private void seedTasks(Long project, Long creator, Long assignee) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        seedTask(
            project,
            creator,
            assignee,
            "设计项目成员权限",
            "明确管理员与普通成员边界，后端校验每个入口。",
            "IN_PROGRESS",
            "HIGH",
            today.plusDays(2)
        );
        seedTask(
            project,
            creator,
            null,
            "完善响应式看板",
            "让桌面与手机都能查看任务状态。",
            "TODO",
            "MEDIUM",
            today.plusDays(4)
        );
        seedTask(
            project,
            creator,
            assignee,
            "整理验收清单",
            "覆盖登录、越权访问、任务冲突与评论。",
            "TODO",
            "HIGH",
            today.minusDays(1)
        );
        Task done = seedTask(
            project,
            creator,
            creator,
            "打通项目创建闭环",
            "前端表单 → Java 校验 → MyBatis → MySQL。",
            "DONE",
            "LOW",
            today.minusDays(2)
        );
        tasks.addComment(done.getId(), creator, "接口与页面已联调通过，下一步完善权限。");
    }

    private Task seedTask(
        Long p,
        Long creator,
        Long assignee,
        String title,
        String description,
        String status,
        String priority,
        LocalDate due
    ) {
        Task t = new Task();
        t.setProjectId(p);
        t.setCreatorId(creator);
        t.setAssigneeId(assignee);
        t.setTitle(title);
        t.setDescription(description);
        t.setStatus(status);
        t.setPriority(priority);
        t.setDueDate(due);
        tasks.insert(t);
        return t;
    }
}
