package com.coda.apiratelimiter.service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.time.LocalTime;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import com.coda.apiratelimiter.exception.RateLimitedException;
import com.coda.apiratelimiter.model.RateLimitRequest;
import com.coda.apiratelimiter.model.RateLimitWindowSize;
import com.google.common.collect.Iterables;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;



@Service
public class RateLimiterServiceImpl implements RateLimiterService{
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterServiceImpl.class);
    private RedisTemplate<String, String> redisTemplate;
    
    
    public static final String HYSTRIX_COMMAND_KEY = "coda-xyz";
    private static final String DELIMITER = ":";
    private static final String KEY_PREFIX_SECOND = "coda:s:";
    private static final String KEY_PREFIX_MINUTE = "coda:m:";
    private static final String KEY_PREFIX_HOUR = "coda:h:";
    private static final double DELTA = 1.0;
    

    public RateLimiterServiceImpl(RedisTemplate<String, String> redisTemplate) {
    		this.redisTemplate = redisTemplate;
    }
    
	@Override
	@HystrixCommand(
            commandKey = HYSTRIX_COMMAND_KEY,
            ignoreExceptions = RateLimitedException.class,
            fallbackMethod = "rateLimitFallback",
            commandProperties = {@HystrixProperty(
                    name = "execution.isolation.thread.timeoutInMilliseconds",
                    value = "1000"
            ), @HystrixProperty(
                    name = "circuitBreaker.requestVolumeThreshold",
                    value = "5"
            ), @HystrixProperty(
                    name = "metrics.rollingStats.timeInMilliseconds",
                    value = "1000"
            )},
            threadPoolProperties = {@HystrixProperty(
                    name = "coreSize",
                    value = "20")})
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
		String key = retrieveKey(request);
        return redisTemplate.execute(new SessionCallback<Long>() {
            @Override
            public <K, V> Long execute(RedisOperations<K, V> operations) {
                String value = redisTemplate.opsForValue().get(key);
                return Optional.ofNullable(value)
                        .map(Long::parseLong)
                        .orElse(1L);
            }
        });
	}

	private Boolean incrementCount(RateLimitRequest request) {
		String key = retrieveKey(request);
        Integer timeout = retrieveTimeout(request);
        TimeUnit timeUnit = retrieveTimeoutUnit(request);
        
        return redisTemplate.execute(new SessionCallback<Boolean>() {
            @Override
            public <K, V> Boolean execute(RedisOperations<K, V> operations) {
                redisTemplate.multi();
                redisTemplate.opsForValue().increment(key, DELTA);
                redisTemplate.expire(key, timeout, timeUnit);
                List<Object> objectList = operations.exec();
                return (Boolean) Iterables.getLast(objectList);
            }
        });
	}
	
	private String retrieveKey(RateLimitRequest request) {
		LocalTime now = LocalTime.now();
        String finalKey = request.getPrefix() + DELIMITER + request.getKey();
        String key="";
        if (RateLimitWindowSize.HOUR.equals(request.getWindowSize())) {
            key = KEY_PREFIX_HOUR + finalKey + DELIMITER + now.getHour(); 		
        } else if (RateLimitWindowSize.SECOND.equals(request.getWindowSize())) {
            key = KEY_PREFIX_SECOND + finalKey + DELIMITER + now.getSecond();
        } else {
            key = KEY_PREFIX_MINUTE + finalKey + DELIMITER + now.getMinute();
        }
        
        return key;
	}

	private Integer retrieveTimeout(RateLimitRequest request) {
        if (RateLimitWindowSize.HOUR.equals(request.getWindowSize())) {
            return 59;   		
        } else if (RateLimitWindowSize.SECOND.equals(request.getWindowSize())) {
            return 999;
        } else {
            return 59;
        }
	}
	
	private TimeUnit retrieveTimeoutUnit(RateLimitRequest request) {
        if (RateLimitWindowSize.HOUR.equals(request.getWindowSize())) {
            return TimeUnit.MINUTES;   		
        } else if (RateLimitWindowSize.SECOND.equals(request.getWindowSize())) {
            return TimeUnit.MILLISECONDS;
        } else {
            return TimeUnit.SECONDS;
        }
	}

	private boolean rateLimitExceeded(Long limit, Long count) {
        return count >= limit;
    }
	
	public void rateLimitFallback(RateLimitRequest request) {
		logger.warn("Fallback method executed, Something went wrong with the Rate Limiter.");
	}
}
