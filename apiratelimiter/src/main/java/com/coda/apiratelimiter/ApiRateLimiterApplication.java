package com.coda.apiratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

@SpringBootApplication
@EnableHystrix
public class ApiRateLimiterApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiRateLimiterApplication.class, args);
	}
}
