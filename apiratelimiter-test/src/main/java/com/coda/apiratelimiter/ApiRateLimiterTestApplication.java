package com.coda.apiratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

@SpringBootApplication
@EnableHystrix
public class ApiRateLimiterTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiRateLimiterTestApplication.class, args);
	}

}
