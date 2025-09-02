package com.cvmento.domain.resume.client;

import com.cvmento.domain.coverLetter.dto.request.LlmRequest; // Reusing LlmRequest from coverLetter
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "resume-llm-api",
                url = "${llm.api.url}",
                configuration = ResumeLlmFeignConfig.class)

public interface ResumeLlmFeignClient {

    @PostMapping("/responses")
    String analyzeRaw(@RequestBody LlmRequest request);
}
