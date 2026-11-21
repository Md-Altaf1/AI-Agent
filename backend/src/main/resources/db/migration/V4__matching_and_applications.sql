CREATE TABLE job_matches (
                             id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                             resume_version_id BIGINT UNSIGNED NOT NULL,
                             job_id            BIGINT UNSIGNED NOT NULL,
                             content_hash      CHAR(64)        NOT NULL,
                             rule_eligible     BOOLEAN         NOT NULL,
                             rule_reasons      JSON            NULL,
                             match_score       TINYINT UNSIGNED NULL,
                             eligible          BOOLEAN         NULL,
                             recommendation    ENUM('APPLY','APPLY_WITH_CAUTION','SKIP') NULL,
                             matching_skills   JSON            NULL,
                             missing_skills    JSON            NULL,
                             ai_model          VARCHAR(100)    NULL,
                             prompt_version    VARCHAR(20)     NULL,
                             created_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                             PRIMARY KEY (id),
                             UNIQUE KEY uk_job_matches_pair (resume_version_id, job_id),
                             KEY idx_job_matches_job (job_id),
                             KEY idx_job_matches_score (match_score),
                             CONSTRAINT fk_job_matches_resume_version FOREIGN KEY (resume_version_id) REFERENCES resume_versions(id) ON DELETE CASCADE,
                             CONSTRAINT fk_job_matches_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
                             CONSTRAINT chk_job_matches_score CHECK (match_score IS NULL OR match_score BETWEEN 0 AND 100)
) ENGINE=InnoDB;

CREATE TABLE applications (
                              id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                              user_id           BIGINT UNSIGNED NOT NULL,
                              job_id            BIGINT UNSIGNED NOT NULL,
                              resume_version_id BIGINT UNSIGNED NULL,
                              status            ENUM('DISCOVERED','ELIGIBLE','REJECTED_BY_FILTER','READY_FOR_REVIEW',
                         'APPROVED','SUBMITTED','UNDER_REVIEW','SHORTLISTED',
                         'INTERVIEW_SCHEDULED','REJECTED','WITHDRAWN') NOT NULL DEFAULT 'DISCOVERED',
                              match_score       TINYINT UNSIGNED NULL,
                              applied_at        DATETIME(6)     NULL,
                              interview_at      DATETIME(6)     NULL,
                              hr_contact_name   VARCHAR(120)    NULL,
                              hr_contact_email  VARCHAR(255)    NULL,
                              notes             TEXT            NULL,
                              version           BIGINT UNSIGNED NOT NULL DEFAULT 0,
                              created_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                              updated_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                              PRIMARY KEY (id),
                              UNIQUE KEY uk_applications_user_job (user_id, job_id),
                              KEY idx_applications_job (job_id),
                              KEY idx_applications_resume_version (resume_version_id),
                              KEY idx_applications_user_status (user_id, status),
                              KEY idx_applications_status_updated (status, updated_at),
                              KEY idx_applications_interview (interview_at),
                              CONSTRAINT fk_applications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                              CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs(id),
                              CONSTRAINT fk_applications_resume_version FOREIGN KEY (resume_version_id) REFERENCES resume_versions(id),
                              CONSTRAINT chk_applications_match_score CHECK (match_score IS NULL OR match_score <= 100)
) ENGINE=InnoDB;

CREATE TABLE application_answers (
                                     id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                     application_id BIGINT UNSIGNED NOT NULL,
                                     field_key      VARCHAR(100)    NOT NULL,
                                     question_text  VARCHAR(500)    NOT NULL,
                                     answer_text    TEXT            NULL,
                                     answer_source  ENUM('PROFILE','RESUME','AI_DRAFT','USER_ENTERED') NOT NULL,
                                     is_approved    BOOLEAN         NOT NULL DEFAULT FALSE,
                                     created_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                     updated_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                     PRIMARY KEY (id),
                                     UNIQUE KEY uk_application_answers_field (application_id, field_key),
                                     CONSTRAINT fk_application_answers_app FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE application_events (
                                    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                    application_id BIGINT UNSIGNED NOT NULL,
                                    event_type     ENUM('CREATED','STATUS_CHANGED','ANSWER_EDITED','APPROVAL_GRANTED',
                      'SUBMISSION_ATTEMPTED','SUBMISSION_CONFIRMED','SUBMISSION_FAILED','NOTE_ADDED') NOT NULL,
                                    from_status    VARCHAR(30)     NULL,
                                    to_status      VARCHAR(30)     NULL,
                                    actor_type     ENUM('USER','SYSTEM','WORKER') NOT NULL,
                                    details        JSON            NULL,
                                    created_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                    PRIMARY KEY (id),
                                    KEY idx_application_events_app_time (application_id, created_at),
                                    CONSTRAINT fk_application_events_app FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Audit log is append-only: the database itself refuses UPDATE and DELETE.
CREATE TRIGGER trg_application_events_no_update
    BEFORE UPDATE ON application_events
    FOR EACH ROW
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'application_events is append-only';

CREATE TRIGGER trg_application_events_no_delete
    BEFORE DELETE ON application_events
    FOR EACH ROW
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'application_events is append-only';