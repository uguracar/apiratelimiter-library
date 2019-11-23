package com.coda.apiratelimiter.service;

import org.springframework.stereotype.Service;

import com.coda.apiratelimiter.annotation.RateLimit;
import com.coda.apiratelimiter.exception.RateLimitedException;
import com.coda.apiratelimiter.model.Message;
import com.coda.apiratelimiter.model.RateLimitWindowSize;
import com.coda.apiratelimiter.model.SampleRequest;

@Service
public class SampleLimitedService {
	
	@RateLimit(prefix = "sampleLimitedService:getMessage", key = "#request.authenticationId", windowSize = RateLimitWindowSize.MINUTE, limit = 2)
    public Message getMessage(SampleRequest request) throws RateLimitedException {
        return new Message(request.getMessage());
    }
}
