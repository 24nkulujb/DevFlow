CREATE TABLE project_member (
 project_id BIGINT NOT NULL,
 user_id BIGINT NOT NULL,
 role VARCHAR(10) NOT NULL DEFAULT 'MEMBER',
 joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (project_id,user_id),
 CONSTRAINT fk_member_project FOREIGN KEY (project_id) REFERENCES project(id),
 CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES app_user(id),
 CONSTRAINT ck_member_role CHECK (role IN ('ADMIN','MEMBER'))
) ENGINE=InnoDB;
CREATE TABLE task (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 project_id BIGINT NOT NULL,
 title VARCHAR(100) NOT NULL,
 description VARCHAR(2000) NOT NULL DEFAULT '',
 status VARCHAR(20) NOT NULL DEFAULT 'TODO',
 priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
 creator_id BIGINT NOT NULL,
 assignee_id BIGINT NULL,
 due_date DATE NULL,
 version INT NOT NULL DEFAULT 0,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project(id),
 CONSTRAINT fk_task_creator FOREIGN KEY (creator_id) REFERENCES app_user(id),
 CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES app_user(id),
 CONSTRAINT ck_task_status CHECK (status IN ('TODO','IN_PROGRESS','DONE')),
 CONSTRAINT ck_task_priority CHECK (priority IN ('LOW','MEDIUM','HIGH')),
 INDEX idx_task_project_status (project_id,status),
 INDEX idx_task_assignee (assignee_id),
 INDEX idx_task_due (project_id,due_date)
) ENGINE=InnoDB;
CREATE TABLE task_comment (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 task_id BIGINT NOT NULL,
 author_id BIGINT NOT NULL,
 content VARCHAR(500) NOT NULL,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_comment_task FOREIGN KEY (task_id) REFERENCES task(id),
 CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES app_user(id),
 INDEX idx_comment_task_time (task_id,created_at)
) ENGINE=InnoDB;
