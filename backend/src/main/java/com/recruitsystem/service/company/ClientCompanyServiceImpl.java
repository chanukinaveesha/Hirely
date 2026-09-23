package com.recruitsystem.service.company;

import com.recruitsystem.dto.company.ClientCompanyResponse;
import com.recruitsystem.repository.company.ClientCompanyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientCompanyServiceImpl implements ClientCompanyService {

    private final ClientCompanyRepository clientCompanyRepository;

    @Override
    public List<ClientCompanyResponse> listAll() {
        return clientCompanyRepository.findAll().stream()
                .map(company -> ClientCompanyResponse.builder()
                        .id(company.getId())
                        .companyName(company.getCompanyName())
                        .industry(company.getIndustry())
                        .build())
                .toList();
    }
}
