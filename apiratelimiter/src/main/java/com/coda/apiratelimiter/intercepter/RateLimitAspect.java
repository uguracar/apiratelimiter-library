package com.coda.apiratelimiter.intercepter;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.coda.apiratelimiter.annotation.RateLimit;
import com.coda.apiratelimiter.exception.RateLimitedException;
import com.coda.apiratelimiter.model.RateLimitRequest;
import com.coda.apiratelimiter.service.RateLimiterService;
import com.coda.apiratelimiter.utils.KeyEvaluator;

@Aspect
@Component
public class RateLimitAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RateLimiterService rateLimiterService;

    public RateLimitAspect(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Before("@annotation(rateLimitAnnotation)")
    public void before(JoinPoint joinPoint, RateLimit rateLimitAnnotation) {
        RateLimitRequest rateLimitRequest = new RateLimitRequest();
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Object value = KeyEvaluator.evaluateExpression(signature.getParameterNames(), joinPoint.getArgs(), rateLimitAnnotation.key());
            rateLimitRequest.setPrefix(rateLimitAnnotation.prefix());
            rateLimitRequest.setKey(value);
            rateLimitRequest.setLimit(rateLimitAnnotation.limit());
            rateLimitRequest.setWindowSize(rateLimitAnnotation.windowSize());
            rateLimiterService.rateLimit(rateLimitRequest);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        if (e instanceof RateLimitedException) {
            throw (RateLimitedException) e;
        } else {
            logger.warn(e.getMessage(), e);
        }
    }
}