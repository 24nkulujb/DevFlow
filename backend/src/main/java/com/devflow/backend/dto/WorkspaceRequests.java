package com.devflow.backend.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** Request DTOs only accept fields the client is allowed to change. */
public final class WorkspaceRequests {

    private WorkspaceRequests() {}

    public record ProjectInput(
        @NotBlank(message = "项目名称不能为空")
        @Size(max = 50, message = "项目名称最多50个字符")
        String name,
        @Size(max = 500, message = "项目描述最多500个字符") String description
    ) {}

    public record MemberInput(
        @NotNull Long userId,
        @NotBlank
        @Pattern(regexp = "ADMIN|MEMBER", message = "角色必须为 ADMIN 或 MEMBER")
        String role
    ) {}

    public record RoleInput(
        @NotBlank
        @Pattern(regexp = "ADMIN|MEMBER", message = "角色必须为 ADMIN 或 MEMBER")
        String role
    ) {}

    public record TaskInput(
        @NotBlank(message = "任务标题不能为空")
        @Size(max = 100, message = "任务标题最多100个字符")
        String title,
        @Size(max = 2000, message = "任务描述最多2000个字符") String description,
        @NotBlank
        @Pattern(regexp = "TODO|IN_PROGRESS|DONE", message = "无效的任务状态")
        String status,
        @NotBlank @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "无效的优先级") String priority,
        Long assigneeId,
        LocalDate dueDate,
        @Min(0) Integer version
    ) {}

    public record StatusInput(
        @NotBlank @Pattern(regexp = "TODO|IN_PROGRESS|DONE") String status,
        @NotNull @Min(0) Integer version
    ) {}

    public record CommentInput(
        @NotBlank(message = "评论不能为空")
        @Size(max = 500, message = "评论最多500个字符")
        String content
    ) {}
}
