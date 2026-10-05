-- Platform layer: lightweight posts feed. Recruiters/HR promote one of
-- their own published vacancies; job seekers showcase their profile.
-- Image is optional either way.

CREATE TABLE posts (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id               BIGINT       NOT NULL,
    type                    VARCHAR(20)  NOT NULL,
    body                    TEXT         NOT NULL,
    vacancy_id              BIGINT,
    image_stored_filename   VARCHAR(255),
    image_original_filename VARCHAR(255),
    image_content_type      VARCHAR(50),
    created_at              DATETIME     NOT NULL,
    CONSTRAINT fk_posts_author
        FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_posts_vacancy
        FOREIGN KEY (vacancy_id) REFERENCES job_vacancies (id) ON DELETE CASCADE,
    CONSTRAINT chk_posts_type
        CHECK (type IN ('VACANCY_PROMO', 'PROFILE_SHOWCASE'))
) ENGINE = InnoDB;

CREATE INDEX idx_posts_author ON posts (author_id);
CREATE INDEX idx_posts_created_at ON posts (created_at);
