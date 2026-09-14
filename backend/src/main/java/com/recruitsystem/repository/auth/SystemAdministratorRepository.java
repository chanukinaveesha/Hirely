package com.recruitsystem.repository.auth;

import com.recruitsystem.entity.auth.SystemAdministrator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemAdministratorRepository extends JpaRepository<SystemAdministrator, Long> {
}
