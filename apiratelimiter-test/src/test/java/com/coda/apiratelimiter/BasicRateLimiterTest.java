package com.coda.apiratelimiter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalTime;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.coda.apiratelimiter.model.SampleRequest;
import com.coda.apiratelimiter.service.SampleLimitedService;
import com.netflix.hystrix.Hystrix;

public class BasicRateLimiterTest extends ApiRateLimiterTestApplicationTests {
	
	@Autowired
    SampleLimitedService limitedService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;
    
    
	@LocalServerPort
	int randomServerPort;
	     
    @BeforeEach
    public void setup() throws IOException, URISyntaxException, InterruptedException {
    	Hystrix.reset();
    }
    
    @After
    public void tearDown() throws InterruptedException, IOException {
    }
    
    @Test
    public void should_return_one_when_first_call_of_rate_limiter_and_window_type_is_minute() {
        SampleRequest request = new SampleRequest();
        request.setAuthenticationId("user1");
        LocalTime now = LocalTime.now();

        Integer result = limitedService.rateLimitedMethod(request);
        String value = redisTemplate.opsForValue().get("coda:m:app:method:user1:" + now.getMinute());

        assertThat(value).isEqualTo("1");
        assertThat(result).isEqualTo(1);
    }
    
    
    @Test
    public void should_return_rate_limit_exceeded_message_when_rate_limit_exceeded() throws JSONException {
        TestRestTemplate restTemplate = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();

        SampleRequest request = new SampleRequest();
        request.setAuthenticationId("user1");
        request.setMessage("Hello coda");

        HttpEntity<SampleRequest> entity = new HttpEntity<SampleRequest>(request, headers);

        ResponseEntity<String> response = null;
        for (int i = 0; i < 15; i++) {
            response = restTemplate.exchange(
                    createURLWithPort("/sampleLimitedService"),
                    HttpMethod.POST, entity, String.class);
        }

        String expected = "{\"message\":\"Rate limit exceeded\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }
    

    private String createURLWithPort(String uri) {
        return "http://localhost:" + randomServerPort + uri;
    }
}
