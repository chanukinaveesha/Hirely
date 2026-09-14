package com.recruitsystem.repository.application;

import com.recruitsystem.entity.application.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
