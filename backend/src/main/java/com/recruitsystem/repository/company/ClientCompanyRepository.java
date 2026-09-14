package com.recruitsystem.repository.company;

import com.recruitsystem.entity.company.ClientCompany;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientCompanyRepository extends JpaRepository<ClientCompany, Long> {
}
