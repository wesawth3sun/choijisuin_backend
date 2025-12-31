package com.finance.app_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "com.finance.infra.entity")
@EnableJpaRepositories(basePackages = "com.finance.infra.repository")
@SpringBootApplication(scanBasePackages = "com.finance") // 모든 모듈의 공통 패키지 경로
public class AppApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppApiApplication.class, args);
	}

}
