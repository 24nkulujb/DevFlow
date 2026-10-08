package com.devflow.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.devflow.backend.dto.WorkspaceRequests.*;
import com.devflow.backend.mapper.*;
import com.devflow.backend.model.*;
import com.devflow.backend.service.*;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BackendApplicationTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserMapper users;

    @Autowired
    ProjectMapper projects;

    @Autowired
    MemberMapper members;

    @Autowired
    TaskMapper tasks;

    @Autowired
    ProjectService projectService;

    @Autowired
    TaskService taskService;

    @Autowired
    PasswordEncoder encoder;

    Long alice, bob, charlie, eve, projectId, privateId;

    @BeforeEach
    void fixtures() {
        String hash = encoder.encode("DevFlow123!");
        for (String name : new String[] { "alice", "bob", "charlie", "eve" })
            users.insert(name, hash, name);
        alice = users.findByUsername("alice").id();
        bob = users.findByUsername("bob").id();
        charlie = users.findByUsername("charlie").id();
        eve = users.findByUsername("eve").id();
        Project p = projectService.createProject(new ProjectInput("团队项目", "测试"), "alice");
        projectId = p.getId();
        members.add(projectId, bob, "MEMBER");
        members.add(projectId, charlie, "MEMBER");
        privateId = projectService.createProject(new ProjectInput("私有项目", ""), "bob").getId();
    }

    private String path() {
        return "/api/projects/" + projectId;
    }

    private String taskJson(String title, Long assignee) {
        return (
            "{\"title\":\"" +
            title +
            "\",\"description\":\"说明\",\"status\":\"TODO\",\"priority\":\"HIGH\",\"assigneeId\":" +
            assignee +
            "}"
        );
    }

    private Task createTask(Long assignee) {
        return taskService.create(
            projectId,
            new TaskInput("任务", "说明", "TODO", "HIGH", assignee, null, null),
            "alice"
        );
    }

    @Test
    void authenticationAndLogout() throws Exception {
        mvc.perform(get("/api/projects")).andExpect(status().isUnauthorized());
        mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .param("username", "alice")
                .param("password", "wrong")
        ).andExpect(status().isUnauthorized());
        var result = mvc
            .perform(
                post("/api/auth/login")
                    .with(csrf())
                    .param("username", "alice")
                    .param("password", "DevFlow123!")
            )
            .andExpect(status().isNoContent())
            .andReturn();
        var session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        mvc.perform(get("/api/auth/me").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("alice"))
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
        mvc.perform(post("/api/auth/logout").session(session).with(csrf())).andExpect(
            status().isNoContent()
        );
        assertTrue(session.isInvalid());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void csrfIsRequired() throws Exception {
        mvc.perform(
            post("/api/projects")
                .with(user("alice"))
                .contentType("application/json")
                .content("{\"name\":\"项目\"}")
        ).andExpect(status().isForbidden());
        mvc.perform(get("/api/auth/csrf"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void listsOnlyMemberProjects() throws Exception {
        mvc.perform(get("/api/projects").with(user("alice")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].role").value("ADMIN"));
        mvc.perform(get("/api/projects/" + privateId).with(user("alice"))).andExpect(
            status().isNotFound()
        );
        mvc.perform(get(path() + "/tasks").with(user("eve"))).andExpect(status().isNotFound());
        mvc.perform(get(path() + "/members").with(user("eve"))).andExpect(status().isNotFound());
    }

    @Test
    void creationAndValidation() throws Exception {
        mvc.perform(
            post("/api/projects")
                .with(user("bob"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"name\":\" 新项目 \"}")
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("新项目"))
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.memberCount").value(1));
        mvc.perform(
            post("/api/projects")
                .with(user("alice"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"name\":\"   \"}")
        )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    void projectEditingRequiresAdmin() throws Exception {
        mvc.perform(
            put(path())
                .with(user("bob"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"name\":\"改名\"}")
        ).andExpect(status().isForbidden());
        mvc.perform(
            put(path())
                .with(user("alice"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"name\":\"改名\"}")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("改名"));
    }

    @Test
    void memberManagementBoundaries() throws Exception {
        String body = "{\"userId\":" + eve + ",\"role\":\"MEMBER\"}";
        mvc.perform(
            post(path() + "/members")
                .with(user("bob"))
                .with(csrf())
                .contentType("application/json")
                .content(body)
        ).andExpect(status().isForbidden());
        mvc.perform(
            post(path() + "/members")
                .with(user("alice"))
                .with(csrf())
                .contentType("application/json")
                .content(body)
        ).andExpect(status().isCreated());
        mvc.perform(
            post(path() + "/members")
                .with(user("alice"))
                .with(csrf())
                .contentType("application/json")
                .content(body)
        ).andExpect(status().isConflict());
        mvc.perform(
            delete(path() + "/members/" + alice)
                .with(user("alice"))
                .with(csrf())
        ).andExpect(status().isBadRequest());
        mvc.perform(
            patch(path() + "/members/" + alice)
                .with(user("alice"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"role\":\"MEMBER\"}")
        ).andExpect(status().isConflict());
    }

    @Test
    void createsTasksAndRejectsForeignAssignee() throws Exception {
        mvc.perform(
            post(path() + "/tasks")
                .with(user("bob"))
                .with(csrf())
                .contentType("application/json")
                .content(taskJson("第一条任务", bob))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.creatorId").value(bob.intValue()))
            .andExpect(jsonPath("$.assigneeName").value("bob"))
            .andExpect(jsonPath("$.version").value(0));
        mvc.perform(
            post(path() + "/tasks")
                .with(user("alice"))
                .with(csrf())
                .contentType("application/json")
                .content(taskJson("非法指派", eve))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void taskPermissionsAndOptimisticLocking() throws Exception {
        Task t = createTask(bob);
        String url = path() + "/tasks/" + t.getId() + "/status";
        mvc.perform(
            patch(url)
                .with(user("charlie"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"status\":\"DONE\",\"version\":0}")
        ).andExpect(status().isForbidden());
        mvc.perform(
            patch(url)
                .with(user("bob"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"status\":\"IN_PROGRESS\",\"version\":0}")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value(1));
        mvc.perform(
            patch(url)
                .with(user("alice"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"status\":\"DONE\",\"version\":0}")
        ).andExpect(status().isConflict());
        assertEquals("IN_PROGRESS", tasks.find(projectId, t.getId()).getStatus());
    }

    @Test
    void foreignProjectCannotReadTaskOrComment() throws Exception {
        Task t = createTask(bob);
        mvc.perform(
            get("/api/projects/" + privateId + "/tasks/" + t.getId()).with(user("bob"))
        ).andExpect(status().isNotFound());
        mvc.perform(
            post("/api/projects/" + privateId + "/tasks/" + t.getId() + "/comments")
                .with(user("bob"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"content\":\"越权\"}")
        ).andExpect(status().isNotFound());
    }

    @Test
    void removalUnassignsOnlyOpenTasks() throws Exception {
        Task open = createTask(bob),
            done = createTask(bob);
        taskService.status(projectId, done.getId(), new StatusInput("DONE", 0), "alice");
        projectService.removeMember(projectId, bob, "alice");
        assertNull(tasks.find(projectId, open.getId()).getAssigneeId());
        assertEquals(1, tasks.find(projectId, open.getId()).getVersion());
        assertEquals(bob, tasks.find(projectId, done.getId()).getAssigneeId());
        mvc.perform(get(path()).with(user("bob"))).andExpect(status().isNotFound());
    }

    @Test
    void commentsAndValidation() throws Exception {
        Task t = createTask(bob);
        String url = path() + "/tasks/" + t.getId() + "/comments";
        mvc.perform(
            post(url)
                .with(user("charlie"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"content\":\" 评论内容 \"}")
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0].content").value("评论内容"))
            .andExpect(jsonPath("$[0].authorName").value("charlie"));
        mvc.perform(
            post(url)
                .with(user("charlie"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"content\":\" \"}")
        ).andExpect(status().isBadRequest());
        mvc.perform(get(url).with(user("bob")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void statisticsAndFilters() throws Exception {
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Shanghai")).minusDays(1);
        Task a = taskService.create(
            projectId,
            new TaskInput("逾期", "", "TODO", "HIGH", bob, yesterday, null),
            "alice"
        );
        Task b = taskService.create(
            projectId,
            new TaskInput("完成", "", "TODO", "LOW", alice, yesterday, null),
            "alice"
        );
        taskService.status(projectId, b.getId(), new StatusInput("DONE", 0), "alice");
        mvc.perform(get(path() + "/stats").with(user("bob")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(2))
            .andExpect(jsonPath("$.done").value(1))
            .andExpect(jsonPath("$.overdue").value(1))
            .andExpect(jsonPath("$.completionRate").value(50.0));
        mvc.perform(
            get(path() + "/tasks")
                .param("overdue", "true")
                .with(user("bob"))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(a.getId().intValue()));
        mvc.perform(get("/api/projects/" + privateId + "/stats").with(user("bob")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(0))
            .andExpect(jsonPath("$.completionRate").value(0.0));
    }

    @Test
    void historicalAssigneeSurvivesEditingButNotReopening() {
        Task done = createTask(bob);
        taskService.status(projectId, done.getId(), new StatusInput("DONE", 0), "alice");
        projectService.removeMember(projectId, bob, "alice");
        Task edited = taskService.update(
            projectId,
            done.getId(),
            new TaskInput("完成后的说明更新", "历史负责人仍可追溯", "DONE", "HIGH", bob, null, 1),
            "alice"
        );
        assertEquals(bob, edited.getAssigneeId());
        Task reopened = taskService.status(
            projectId,
            done.getId(),
            new StatusInput("TODO", edited.getVersion()),
            "alice"
        );
        assertNull(reopened.getAssigneeId());
    }

    @Test
    void taskEditingAndFilterValidation() throws Exception {
        Task task = createTask(bob);
        String body =
            "{\"title\":\"修改后的任务\",\"description\":\"新说明\",\"status\":\"IN_PROGRESS\",\"priority\":\"LOW\",\"assigneeId\":null,\"version\":0}";
        mvc.perform(
            put(path() + "/tasks/" + task.getId())
                .with(user("charlie"))
                .with(csrf())
                .contentType("application/json")
                .content(body)
        ).andExpect(status().isForbidden());
        mvc.perform(
            put(path() + "/tasks/" + task.getId())
                .with(user("bob"))
                .with(csrf())
                .contentType("application/json")
                .content(body)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value(1))
            .andExpect(jsonPath("$.title").value("修改后的任务"));
        mvc.perform(
            get(path() + "/tasks")
                .param("status", "IN_PROGRESS")
                .param("priority", "LOW")
                .param("q", "修改")
                .with(user("alice"))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(
            get(path() + "/tasks")
                .param("status", "UNKNOWN")
                .with(user("alice"))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void deadlineTodayIsNotOverdue() {
        taskService.create(
            projectId,
            new TaskInput(
                "今天截止",
                "",
                "TODO",
                "MEDIUM",
                null,
                LocalDate.now(ZoneId.of("Asia/Shanghai")),
                null
            ),
            "alice"
        );
        assertEquals(0, taskService.stats(projectId, "alice").overdue());
    }
}
