package com.cvmento.domain.resume.client;

import com.cvmento.domain.resume.dto.request.ResumeLlmRequest;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "resume-llm-api",
                url = "${llm.api.url}",
                configuration = ResumeLlmFeignConfig.class)

public interface ResumeLlmFeignClient {

    @PostMapping("/responses")
    String analyzeRaw(@RequestBody Map<String, Object> request);
}
