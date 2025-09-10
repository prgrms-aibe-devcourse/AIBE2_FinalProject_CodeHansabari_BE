package com.cvmento.domain.resume.dto.response;

import java.util.List;

public record CareerResponse(
        String startDate,
        String endDate,
        String companyName,
        String companyDescription,
        String departmentPosition,
        String mainTasks,
        List<CareerTechStackResponse> techStacks
) {
}