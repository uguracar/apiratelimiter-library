package com.coda.apiratelimiter.service;

import com.coda.apiratelimiter.model.RateLimitRequest;

public interface RateLimiterService {
	
	void rateLimit(RateLimitRequest request);
}
