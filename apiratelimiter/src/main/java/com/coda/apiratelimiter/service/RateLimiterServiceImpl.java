package com.coda.apiratelimiter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.coda.apiratelimiter.exception.RateLimitedException;
import com.coda.apiratelimiter.model.RateLimitRequest;

@Service
public class RateLimiterServiceImpl implements RateLimiterService{
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterServiceImpl.class);

    private RedisTemplate<String, String> redisTemplate;

    public RateLimiterServiceImpl(RedisTemplate<String, String> redisTemplate) {
    		this.redisTemplate = redisTemplate;
    }

	@Override
	public void rateLimit(RateLimitRequest request) {
            Long count = retrieveCount(request);
            if (rateLimitExceeded(request.getLimit(), count)) {
                logger.warn("The rate limit has been exceeded for key: " + request.getKey());
                throw new RateLimitedException(request);
            } else {
                incrementCount(request);
            }
	}
	
	private Long retrieveCount(RateLimitRequest request) {
		//Retrieve count from Redis
		return 1L; 
	}

	private void incrementCount(RateLimitRequest request) {
		//Increment counter for specific key from Redis
	}

	private boolean rateLimitExceeded(Long limit, Long count) {
        return count >= limit;
    }

}
