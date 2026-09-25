-- Job Vacancy Management module: the job_vacancies table, plus the FK
-- constraint that V1 intentionally left off applications.vacancy_id since
-- this table didn't exist yet at that point.

CREATE TABLE job_vacancies (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    description       TEXT         NOT NULL,
    requirements      TEXT         NOT NULL,
    category          VARCHAR(30)  NOT NULL,
    location          VARCHAR(255),
    deadline          DATE         NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    posted_by_user_id BIGINT       NOT NULL,
    client_company_id BIGINT       NOT NULL,
    published_at      DATETIME,
    created_at        DATETIME     NOT NULL,
    updated_at        DATETIME     NOT NULL,
    CONSTRAINT fk_job_vacancies_posted_by
        FOREIGN KEY (posted_by_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_job_vacancies_client_company
        FOREIGN KEY (client_company_id) REFERENCES client_companies (id) ON DELETE CASCADE,
    CONSTRAINT chk_job_vacancies_category
        CHECK (category IN ('IT', 'ENGINEERING', 'FINANCE', 'MARKETING', 'SALES',
                             'HUMAN_RESOURCES', 'CUSTOMER_SERVICE', 'DESIGN',
                             'OPERATIONS', 'OTHER')),
    CONSTRAINT chk_job_vacancies_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED'))
) ENGINE = InnoDB;

CREATE INDEX idx_job_vacancies_posted_by ON job_vacancies (posted_by_user_id);
CREATE INDEX idx_job_vacancies_client_company ON job_vacancies (client_company_id);
CREATE INDEX idx_job_vacancies_status ON job_vacancies (status);

ALTER TABLE applications
    ADD CONSTRAINT fk_applications_vacancy
        FOREIGN KEY (vacancy_id) REFERENCES job_vacancies (id) ON DELETE CASCADE;
