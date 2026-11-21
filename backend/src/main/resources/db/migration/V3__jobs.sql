CREATE TABLE job_sources (
                             id           SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
                             code         VARCHAR(50)       NOT NULL,
                             display_name VARCHAR(100)      NOT NULL,
                             source_type  ENUM('ATS_API','MANUAL_IMPORT','FEED','CAREER_PAGE') NOT NULL,
                             is_enabled   BOOLEAN           NOT NULL DEFAULT TRUE,
                             created_at   DATETIME(6)       NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                             PRIMARY KEY (id),
                             UNIQUE KEY uk_job_sources_code (code)
) ENGINE=InnoDB;

CREATE TABLE jobs (
                      id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                      source_id            SMALLINT UNSIGNED NOT NULL,
                      external_id          VARCHAR(255)    NULL,
                      title                VARCHAR(255)    NOT NULL,
                      company_name         VARCHAR(255)    NOT NULL,
                      location_text        VARCHAR(255)    NOT NULL,
                      location_city        VARCHAR(100)    NULL,
                      employment_type      ENUM('FULL_TIME','PART_TIME','CONTRACT','INTERNSHIP','UNKNOWN') NOT NULL DEFAULT 'UNKNOWN',
                      work_mode            ENUM('ONSITE','HYBRID','REMOTE','UNKNOWN') NOT NULL DEFAULT 'UNKNOWN',
                      min_experience_years DECIMAL(3,1)    NULL,
                      max_experience_years DECIMAL(3,1)    NULL,
                      salary_min           INT UNSIGNED    NULL,
                      salary_max           INT UNSIGNED    NULL,
                      salary_currency      CHAR(3)         NULL,
                      description          MEDIUMTEXT      NOT NULL,
                      application_url      VARCHAR(2048)   NOT NULL,
                      posted_at            DATETIME(6)     NULL,
                      dedupe_key           CHAR(64)        NOT NULL,
                      content_hash         CHAR(64)        NOT NULL,
                      is_active            BOOLEAN         NOT NULL DEFAULT TRUE,
                      discovered_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                      last_seen_at         DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                      created_at           DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                      updated_at           DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                      PRIMARY KEY (id),
                      UNIQUE KEY uk_jobs_dedupe (dedupe_key),
                      UNIQUE KEY uk_jobs_source_external (source_id, external_id),
                      KEY idx_jobs_source (source_id),
                      KEY idx_jobs_active_posted (is_active, posted_at),
                      KEY idx_jobs_location_city (location_city),
                      KEY idx_jobs_discovered (discovered_at),
                      CONSTRAINT fk_jobs_source FOREIGN KEY (source_id) REFERENCES job_sources(id),
                      CONSTRAINT chk_jobs_experience_range CHECK (
                          min_experience_years IS NULL OR max_experience_years IS NULL
                              OR min_experience_years <= max_experience_years)
) ENGINE=InnoDB;

CREATE TABLE job_skills (
                            job_id      BIGINT UNSIGNED NOT NULL,
                            skill_name  VARCHAR(100)    NOT NULL,
                            is_required BOOLEAN         NOT NULL DEFAULT TRUE,
                            PRIMARY KEY (job_id, skill_name),
                            KEY idx_job_skills_skill (skill_name),
                            CONSTRAINT fk_job_skills_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO job_sources (code, display_name, source_type)
VALUES ('MANUAL_IMPORT', 'Manual URL import', 'MANUAL_IMPORT');