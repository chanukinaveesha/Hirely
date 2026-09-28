package com.recruitsystem.service.company;

import com.recruitsystem.dto.company.ClientCompanyResponse;
import java.util.List;

public interface ClientCompanyService {

    List<ClientCompanyResponse> listAll();
}
