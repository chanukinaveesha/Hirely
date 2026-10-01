CREATE TABLE assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_assessments_created_by FOREIGN KEY (created_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_assessments_type CHECK (type IN ('APTITUDE', 'TECHNICAL'))
) ENGINE = InnoDB;

CREATE TABLE candidate_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    application_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    deadline DATETIME NOT NULL,
    submission_text TEXT,
    submitted_at DATETIME,
    score INT,
    feedback TEXT,
    evaluated_at DATETIME,
    assigned_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_candidate_assessments_unique UNIQUE (assessment_id, application_id),
    CONSTRAINT fk_ca_assessment FOREIGN KEY (assessment_id) REFERENCES assessments (id),
    CONSTRAINT fk_ca_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE,
    CONSTRAINT chk_ca_status CHECK (status IN ('ASSIGNED', 'SUBMITTED', 'NOT_SUBMITTED', 'EVALUATED')),
    CONSTRAINT chk_ca_score CHECK (score IS NULL OR score BETWEEN 0 AND 100)
) ENGINE = InnoDB;

CREATE INDEX idx_candidate_assessments_application ON candidate_assessments (application_id);
CREATE INDEX idx_candidate_assessments_assessment ON candidate_assessments (assessment_id);
