-- Phase 1 foundation schema: base User (joined-table inheritance), its five
-- role subtypes, ClientCompany, Application, and Notification.
-- Entities owned by later feature modules (Vacancy, Resume, Interview,
-- Assessment) are intentionally NOT created here.

CREATE TABLE client_companies (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name  VARCHAR(255)  NOT NULL,
    industry      VARCHAR(255),
    description   TEXT,
    address       VARCHAR(500),
    phone         VARCHAR(50),
    email         VARCHAR(255)
) ENGINE = InnoDB;

CREATE TABLE users (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_type      VARCHAR(31)  NOT NULL,
    name           VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    phone          VARCHAR(50),
    account_status VARCHAR(20)  NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_account_status
        CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
) ENGINE = InnoDB;

CREATE TABLE job_seekers (
    user_id BIGINT PRIMARY KEY,
    CONSTRAINT fk_job_seekers_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE recruiters (
    user_id           BIGINT PRIMARY KEY,
    client_company_id BIGINT,
    CONSTRAINT fk_recruiters_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_recruiters_client_company
        FOREIGN KEY (client_company_id) REFERENCES client_companies (id) ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE TABLE hr_executives (
    user_id           BIGINT PRIMARY KEY,
    client_company_id BIGINT,
    CONSTRAINT fk_hr_executives_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_hr_executives_client_company
        FOREIGN KEY (client_company_id) REFERENCES client_companies (id) ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE TABLE interview_panel_members (
    user_id BIGINT PRIMARY KEY,
    CONSTRAINT fk_interview_panel_members_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE system_administrators (
    user_id BIGINT PRIMARY KEY,
    CONSTRAINT fk_system_administrators_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE applications (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_seeker_id BIGINT       NOT NULL,
    -- Plain FK column, no constraint yet: the vacancies table is created by
    -- the vacancy module in a later migration.
    vacancy_id    BIGINT       NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    applied_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    CONSTRAINT fk_applications_job_seeker
        FOREIGN KEY (job_seeker_id) REFERENCES job_seekers (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_applications_status
        CHECK (status IN ('SUBMITTED', 'UNDER_REVIEW', 'SHORTLISTED', 'REJECTED',
                           'INTERVIEWING', 'ASSESSED', 'SELECTED', 'WITHDRAWN'))
) ENGINE = InnoDB;

CREATE INDEX idx_applications_job_seeker ON applications (job_seeker_id);
CREATE INDEX idx_applications_vacancy ON applications (vacancy_id);

CREATE TABLE notifications (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_user_id BIGINT       NOT NULL,
    type              VARCHAR(100) NOT NULL,
    message           TEXT         NOT NULL,
    is_read           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        DATETIME     NOT NULL,
    CONSTRAINT fk_notifications_recipient
        FOREIGN KEY (recipient_user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_notifications_recipient ON notifications (recipient_user_id);
