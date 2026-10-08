package com.devflow.backend.model;

import java.time.LocalDateTime;

/** Public response records never contain password hashes. */
public final class WorkspaceViews {

    private WorkspaceViews() {}

    public record Member(Long id, String username, String displayName, String role) {}

    public record UserOption(Long id, String username, String displayName) {}

    public record Comment(
        Long id,
        Long taskId,
        Long authorId,
        String authorName,
        String content,
        LocalDateTime createdAt
    ) {}

    public record Stats(long total, long todo, long inProgress, long done, long overdue) {
        public double getCompletionRate() {
            return total == 0 ? 0 : Math.round((done * 1000.0) / total) / 10.0;
        }
    }
}
