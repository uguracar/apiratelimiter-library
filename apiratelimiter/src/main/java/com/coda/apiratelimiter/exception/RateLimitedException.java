package com.coda.apiratelimiter.exception;

import com.coda.apiratelimiter.model.RateLimitRequest;

public class RateLimitedException extends RuntimeException {
	
	public RateLimitedException(RateLimitRequest request) {
        super(request.toString());
    }

}
