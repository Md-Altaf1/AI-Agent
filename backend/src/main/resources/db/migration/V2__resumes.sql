CREATE TABLE resumes (
                         id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                         user_id     BIGINT UNSIGNED NOT NULL,
                         label       VARCHAR(100)    NOT NULL,
                         created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                         updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_resumes_user_label (user_id, label),
                         CONSTRAINT fk_resumes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE resume_versions (
                                 id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                 resume_id         BIGINT UNSIGNED NOT NULL,
                                 version_no        INT UNSIGNED    NOT NULL,
                                 original_filename VARCHAR(255)    NOT NULL,
                                 content_type      VARCHAR(100)    NOT NULL,
                                 file_size_bytes   INT UNSIGNED    NOT NULL,
                                 sha256            CHAR(64)        NOT NULL,
                                 storage_key       CHAR(36)        NOT NULL,
                                 extracted_text    MEDIUMTEXT      NULL,
                                 parsed_profile    JSON            NULL,
                                 parse_status      ENUM('PENDING','PARSED','FAILED') NOT NULL DEFAULT 'PENDING',
                                 created_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                 PRIMARY KEY (id),
                                 UNIQUE KEY uk_resume_versions_no (resume_id, version_no),
                                 UNIQUE KEY uk_resume_versions_hash (resume_id, sha256),
                                 UNIQUE KEY uk_resume_versions_storage (storage_key),
                                 CONSTRAINT fk_resume_versions_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
                                 CONSTRAINT chk_resume_file_size CHECK (file_size_bytes > 0 AND file_size_bytes <= 5242880)
) ENGINE=InnoDB;

ALTER TABLE user_preferences
    ADD COLUMN preferred_resume_id BIGINT UNSIGNED NULL,
  ADD CONSTRAINT fk_user_preferences_resume
    FOREIGN KEY (preferred_resume_id) REFERENCES resumes(id) ON DELETE SET NULL;