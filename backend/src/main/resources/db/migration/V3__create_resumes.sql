-- CV/Resume Management module: one resume per job seeker, replaceable in place.

CREATE TABLE resumes (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_seeker_id     BIGINT       NOT NULL,
    stored_filename   VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_type         VARCHAR(10)  NOT NULL,
    file_size_bytes   BIGINT       NOT NULL,
    uploaded_at       DATETIME     NOT NULL,
    updated_at        DATETIME     NOT NULL,
    CONSTRAINT uk_resumes_job_seeker UNIQUE (job_seeker_id),
    CONSTRAINT fk_resumes_job_seeker
        FOREIGN KEY (job_seeker_id) REFERENCES job_seekers (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_resumes_file_type CHECK (file_type IN ('PDF', 'DOCX'))
) ENGINE = InnoDB;
