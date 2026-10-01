-- Interview Management module.
-- interview_feedback is a composition child of interviews (ON DELETE CASCADE,
-- no independent existence). interview_panel_assignments is a plain
-- association join table between interviews and interview_panel_members,
-- who exist independently either way.

CREATE TABLE interviews (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id         BIGINT       NOT NULL,
    scheduled_at           DATETIME     NOT NULL,
    location               VARCHAR(500),
    status                 VARCHAR(30)  NOT NULL,
    candidate_preferred_at DATETIME,
    created_at             DATETIME     NOT NULL,
    updated_at             DATETIME     NOT NULL,
    CONSTRAINT fk_interviews_application
        FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE,
    CONSTRAINT chk_interviews_status
        CHECK (status IN ('PROPOSED', 'RESCHEDULE_REQUESTED', 'CONFIRMED', 'CANCELLED'))
) ENGINE = InnoDB;

CREATE INDEX idx_interviews_application ON interviews (application_id);

CREATE TABLE interview_panel_assignments (
    interview_id    BIGINT NOT NULL,
    panel_member_id BIGINT NOT NULL,
    PRIMARY KEY (interview_id, panel_member_id),
    CONSTRAINT fk_ipa_interview
        FOREIGN KEY (interview_id) REFERENCES interviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_ipa_panel_member
        FOREIGN KEY (panel_member_id) REFERENCES interview_panel_members (user_id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE interview_feedback (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_id    BIGINT   NOT NULL,
    panel_member_id BIGINT   NOT NULL,
    score           INT      NOT NULL,
    comments        TEXT,
    submitted_at    DATETIME NOT NULL,
    CONSTRAINT uk_interview_feedback_unique UNIQUE (interview_id, panel_member_id),
    CONSTRAINT fk_if_interview
        FOREIGN KEY (interview_id) REFERENCES interviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_if_panel_member
        FOREIGN KEY (panel_member_id) REFERENCES interview_panel_members (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_if_score CHECK (score BETWEEN 1 AND 10)
) ENGINE = InnoDB;

CREATE INDEX idx_interview_feedback_interview ON interview_feedback (interview_id);
