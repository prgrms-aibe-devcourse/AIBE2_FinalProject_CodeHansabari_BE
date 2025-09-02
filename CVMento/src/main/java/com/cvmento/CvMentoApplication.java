package com.cvmento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CvMentoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CvMentoApplication.class, args);
	}

}
