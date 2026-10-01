package com.recruitsystem.dto.interview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PanelMemberSummaryResponse {

    private Long id;
    private String name;
    private String email;
}
