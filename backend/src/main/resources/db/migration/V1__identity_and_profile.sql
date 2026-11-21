CREATE TABLE users (
                       id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                       email         VARCHAR(255)    NOT NULL,
                       password_hash VARCHAR(72)     NOT NULL,
                       role          ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
                       enabled       BOOLEAN         NOT NULL DEFAULT TRUE,
                       created_at    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                       updated_at    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE refresh_tokens (
                                id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                user_id     BIGINT UNSIGNED NOT NULL,
                                token_hash  CHAR(64)        NOT NULL,
                                expires_at  DATETIME(6)     NOT NULL,
                                revoked_at  DATETIME(6)     NULL,
                                created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                PRIMARY KEY (id),
                                UNIQUE KEY uk_refresh_tokens_hash (token_hash),
                                KEY idx_refresh_tokens_user (user_id),
                                CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE user_profiles (
                               user_id                BIGINT UNSIGNED NOT NULL,
                               full_name              VARCHAR(120)    NOT NULL,
                               phone                  VARCHAR(20)     NULL,
                               city                   VARCHAR(100)    NULL,
                               highest_education      VARCHAR(100)    NULL,
                               graduation_year        SMALLINT UNSIGNED NULL,
                               total_experience_years DECIMAL(3,1)    NOT NULL DEFAULT 0.0,
                               linkedin_url           VARCHAR(255)    NULL,
                               github_url             VARCHAR(255)    NULL,
                               updated_at             DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                               PRIMARY KEY (user_id),
                               CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               CONSTRAINT chk_profile_experience CHECK (total_experience_years BETWEEN 0 AND 60),
                               CONSTRAINT chk_profile_grad_year CHECK (graduation_year BETWEEN 1950 AND 2100)
) ENGINE=InnoDB;

CREATE TABLE user_skills (
                             id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                             user_id     BIGINT UNSIGNED NOT NULL,
                             skill_name  VARCHAR(100)    NOT NULL,
                             proficiency ENUM('BEGINNER','INTERMEDIATE','ADVANCED') NULL,
                             source      ENUM('SELF_DECLARED','RESUME_EXTRACTED') NOT NULL,
                             created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                             PRIMARY KEY (id),
                             UNIQUE KEY uk_user_skills_user_skill (user_id, skill_name),
                             CONSTRAINT fk_user_skills_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE user_preferences (
                                  user_id                       BIGINT UNSIGNED NOT NULL,
                                  target_titles                 JSON            NOT NULL DEFAULT (JSON_ARRAY()),
                                  preferred_locations           JSON            NOT NULL DEFAULT (JSON_ARRAY()),
                                  employment_types              JSON            NOT NULL DEFAULT (JSON_ARRAY()),
                                  excluded_title_keywords       JSON            NOT NULL DEFAULT (JSON_ARRAY()),
                                  max_required_experience_years DECIMAL(3,1)    NOT NULL DEFAULT 2.0,
                                  min_match_score               TINYINT UNSIGNED NOT NULL DEFAULT 60,
                                  max_jobs_per_search           SMALLINT UNSIGNED NOT NULL DEFAULT 50,
                                  external_ai_enabled           BOOLEAN         NOT NULL DEFAULT TRUE,
                                  updated_at                    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                  PRIMARY KEY (user_id),
                                  CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                  CONSTRAINT chk_prefs_min_score CHECK (min_match_score BETWEEN 0 AND 100),
                                  CONSTRAINT chk_prefs_max_exp CHECK (max_required_experience_years BETWEEN 0 AND 10)
) ENGINE=InnoDB;