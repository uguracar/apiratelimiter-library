package com.coda.apiratelimiter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import com.coda.apiratelimiter.config.EmbeddedRedis;
import com.coda.apiratelimiter.model.RateLimitRequest;
import com.coda.apiratelimiter.model.RateLimitWindowSize;
import com.coda.apiratelimiter.service.RateLimiterServiceImpl;
import com.coda.apiratelimiter.service.SampleLimitedService;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;


@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class BasicRateLimiterHystrixTest extends ApiRateLimiterTestApplicationTests {
	
	@Autowired
    SampleLimitedService limitedService;

    @Autowired
    EmbeddedRedis embeddedRedis;
    
    @Autowired
    RateLimiterServiceImpl rateLimiterService;
        
    
    @BeforeEach
    public void setup() throws IOException, URISyntaxException, InterruptedException {
    	Hystrix.reset();
        warmUpCircuitBreaker();
        openCircuitBreakerAfterOneFailingRequest();
    }
    
    @Test
    public void should_open_circuit_when_redis_server_is_down() throws InterruptedException {
        RateLimitRequest request = new RateLimitRequest();
        request.setKey("#request.userId");
        request.setLimit(10);
        request.setPrefix("app:method");
        request.setWindowSize(RateLimitWindowSize.MINUTE);

        HystrixCircuitBreaker circuitBreaker = getCircuitBreaker();

        // demonstrates circuit is actually closed
        assertThat(circuitBreaker.isOpen()).isFalse();
        assertThat(circuitBreaker.allowRequest()).isTrue();

        // when redis is down
        embeddedRedis.stopRedis();
        rateLimiterService.rateLimit(request);

        // then circuit is open
        waitUntilCircuitBreakerOpens();
        assertThat(circuitBreaker.isOpen()).isTrue();
        assertThat(circuitBreaker.allowRequest()).isFalse();
    }
    

    private void warmUpCircuitBreaker() {
        RateLimitRequest request = new RateLimitRequest();
        request.setKey("#request.userId");
        request.setLimit(10);
        request.setPrefix("app:method");
        request.setWindowSize(RateLimitWindowSize.MINUTE);
        rateLimiterService.rateLimit(request);
    }
    
    public static HystrixCircuitBreaker getCircuitBreaker() {
        return HystrixCircuitBreaker.Factory.getInstance(getCommandKey());
    }

    private static HystrixCommandKey getCommandKey() {
    	return HystrixCommandKey.Factory.asKey(RateLimiterServiceImpl.HYSTRIX_COMMAND_KEY);
    }
    
    private void openCircuitBreakerAfterOneFailingRequest() {
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command."+RateLimiterServiceImpl.HYSTRIX_COMMAND_KEY+".circuitBreaker.requestVolumeThreshold", 1);
    }
    
    private void waitUntilCircuitBreakerOpens() throws InterruptedException {
        Thread.sleep(1000);
    }
}
