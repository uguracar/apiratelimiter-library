package com.coda.apiratelimiter.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@ComponentScan(basePackages = {"com.coda.apiratelimiter"})
public class RedisConfiguration {

    private RedisTemplate<String, String> redisTemplate;

    public RedisConfiguration(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}