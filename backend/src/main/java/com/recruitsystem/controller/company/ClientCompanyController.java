package com.recruitsystem.controller.company;

import com.recruitsystem.dto.company.ClientCompanyResponse;
import com.recruitsystem.service.company.ClientCompanyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Public: the registration form needs this list before the user has a token.
@RestController
@RequestMapping("/api/client-companies")
@RequiredArgsConstructor
@Tag(name = "Client Companies")
@SecurityRequirements
public class ClientCompanyController {

    private final ClientCompanyService clientCompanyService;

    @GetMapping
    public ResponseEntity<List<ClientCompanyResponse>> listCompanies() {
        return ResponseEntity.ok(clientCompanyService.listAll());
    }
}
