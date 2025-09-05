package com.cvmento.domain.resume.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "resume-llm-vision-api",
                url = "${llm.api.url}",
                configuration = ResumeLlmVisionFeignConfig.class)
public interface ResumeLlmVisionFeignClient {

    @PostMapping("/responses")
    String analyzeVision(@RequestBody Map<String, Object> request);
}
