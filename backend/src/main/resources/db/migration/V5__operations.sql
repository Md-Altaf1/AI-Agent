CREATE TABLE notifications (
                               id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                               user_id         BIGINT UNSIGNED NOT NULL,
                               type            ENUM('NEW_JOB_MATCH','APPROVAL_REQUEST','SUBMISSION_CONFIRMED',
                       'DAILY_SUMMARY','INTERVIEW_REMINDER') NOT NULL,
                               subject         VARCHAR(255)    NOT NULL,
                               body            TEXT            NOT NULL,
                               delivery_status ENUM('PENDING','SENT','FAILED') NOT NULL DEFAULT 'PENDING',
                               attempt_count   TINYINT UNSIGNED NOT NULL DEFAULT 0,
                               last_error      VARCHAR(500)    NULL,
                               read_at         DATETIME(6)     NULL,
                               created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                               sent_at         DATETIME(6)     NULL,
                               PRIMARY KEY (id),
                               KEY idx_notifications_outbox (delivery_status, created_at),
                               KEY idx_notifications_user_read (user_id, read_at),
                               CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- DB-backed task queue for the Playwright worker.
CREATE TABLE automation_tasks (
                                  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                  application_id BIGINT UNSIGNED NOT NULL,
                                  task_type      ENUM('FILL_FORM','SUBMIT') NOT NULL,
                                  status         ENUM('QUEUED','RUNNING','SUCCEEDED','FAILED','CANCELLED') NOT NULL DEFAULT 'QUEUED',
                                  payload        JSON            NULL,
                                  attempt_count  TINYINT UNSIGNED NOT NULL DEFAULT 0,
                                  max_attempts   TINYINT UNSIGNED NOT NULL DEFAULT 3,
                                  run_after      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                  locked_by      VARCHAR(64)     NULL,
                                  locked_until   DATETIME(6)     NULL,
                                  last_error     VARCHAR(1000)   NULL,
                                  created_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                  updated_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                  PRIMARY KEY (id),
                                  KEY idx_automation_tasks_poll (status, run_after),
                                  KEY idx_automation_tasks_lock (locked_until),
                                  KEY idx_automation_tasks_app (application_id),
                                  CONSTRAINT fk_automation_tasks_app FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE automation_logs (
                                 id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                 run_id     CHAR(36)        NOT NULL,
                                 user_id    BIGINT UNSIGNED NULL,
                                 job_id     BIGINT UNSIGNED NULL,
                                 component  ENUM('DISCOVERY','FILTERING','MATCHING','AUTOMATION','SCHEDULER','NOTIFICATION') NOT NULL,
                                 level      ENUM('INFO','WARN','ERROR') NOT NULL,
                                 message    VARCHAR(1000)   NOT NULL,
                                 created_at DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                 PRIMARY KEY (id),
                                 KEY idx_automation_logs_run (run_id),
                                 KEY idx_automation_logs_user_time (user_id, created_at),
                                 KEY idx_automation_logs_job (job_id),
                                 CONSTRAINT fk_automation_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                                 CONSTRAINT fk_automation_logs_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE scheduled_tasks (
                                 id               SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                 task_name        VARCHAR(100)      NOT NULL,
                                 state            ENUM('RUNNING','PAUSED','STOPPED') NOT NULL DEFAULT 'STOPPED',
                                 cron_expression  VARCHAR(50)       NOT NULL DEFAULT '0 0 8 * * *',
                                 last_started_at  DATETIME(6)       NULL,
                                 last_finished_at DATETIME(6)       NULL,
                                 last_status      ENUM('SUCCESS','FAILED','SKIPPED') NULL,
                                 version          BIGINT UNSIGNED   NOT NULL DEFAULT 0,
                                 PRIMARY KEY (id),
                                 UNIQUE KEY uk_scheduled_tasks_name (task_name)
) ENGINE=InnoDB;

INSERT INTO scheduled_tasks (task_name, state)
VALUES ('DAILY_JOB_DISCOVERY', 'STOPPED'),
       ('DAILY_SUMMARY_EMAIL', 'STOPPED');