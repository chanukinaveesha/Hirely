package com.recruitsystem.service.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.recruitsystem.dto.company.ClientCompanyResponse;
import com.recruitsystem.entity.company.ClientCompany;
import com.recruitsystem.repository.company.ClientCompanyRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientCompanyServiceImplTest {

    @Mock
    private ClientCompanyRepository clientCompanyRepository;

    @Test
    void listAll_mapsEntitiesToResponses() {
        ClientCompanyServiceImpl service = new ClientCompanyServiceImpl(clientCompanyRepository);
        when(clientCompanyRepository.findAll()).thenReturn(
                List.of(ClientCompany.builder().id(1L).companyName("Acme Corp").industry("Tech").build()));

        List<ClientCompanyResponse> responses = service.listAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCompanyName()).isEqualTo("Acme Corp");
        assertThat(responses.get(0).getIndustry()).isEqualTo("Tech");
    }
}
