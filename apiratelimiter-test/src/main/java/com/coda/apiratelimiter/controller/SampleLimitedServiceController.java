package com.coda.apiratelimiter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.coda.apiratelimiter.exception.RateLimitedException;
import com.coda.apiratelimiter.model.Message;
import com.coda.apiratelimiter.model.SampleRequest;
import com.coda.apiratelimiter.service.SampleLimitedService;

@RestController
public class SampleLimitedServiceController {
	
	@Autowired
    SampleLimitedService limitedService;
	
	@PostMapping("/sampleLimitedService")
    public Message greeting(@RequestBody SampleRequest sampleRequest) {
        try {
            Message responseMessage = limitedService.getMessage(sampleRequest);
            return responseMessage;
        } catch (Exception e) {
            if (e instanceof RateLimitedException) {
                return new Message("Rate limit exceeded");
            } else {
                throw e;
            }
        }
    }

}
