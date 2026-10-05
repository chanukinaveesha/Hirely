-- Platform layer: profile pictures. One avatar per user (any role),
-- replaceable in place — mirrors the resumes table's one-per-owner pattern.

CREATE TABLE user_avatars (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    stored_filename   VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type      VARCHAR(50)  NOT NULL,
    file_size_bytes   BIGINT       NOT NULL,
    uploaded_at       DATETIME     NOT NULL,
    updated_at        DATETIME     NOT NULL,
    CONSTRAINT uk_user_avatars_user UNIQUE (user_id),
    CONSTRAINT fk_user_avatars_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;
