package com.cvmento;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(properties = {
		"spring.cloud.openfeign.enabled=false",
		"spring.main.allow-bean-definition-overriding=true"
})
@ActiveProfiles("test")
class CvMentoApplicationTests {

	@Test
	void contextLoads() {
	}

	// Feign 클라이언트를 비활성화하는 테스트 설정
	@Configuration
	static class TestConfiguration {
	}
}