package com.coda.apiratelimiter;

import java.io.IOException;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.coda.apiratelimiter.model.SampleRequest;

public class BasicRateLimiterTest extends ApiRateLimiterTestApplicationTests {
	@LocalServerPort
	int randomServerPort;
	     
    @Before
    public void setup() throws IOException, URISyntaxException, InterruptedException {
    }
    
    @After
    public void tearDown() throws InterruptedException, IOException {

    }
    
    @Test
    public void should_return_rate_limit_exceeded_message_when_rate_limit_exceeded() throws JSONException {
        TestRestTemplate restTemplate = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();

        SampleRequest request = new SampleRequest();
        request.setAuthenticationId("20");
        request.setMessage("Hello coda");

        HttpEntity<SampleRequest> entity = new HttpEntity<SampleRequest>(request, headers);

        //when
        ResponseEntity<String> response = null;
        for (int i = 0; i < 19; i++) {
            response = restTemplate.exchange(
                    createURLWithPort("/sampleLimitedService"),
                    HttpMethod.POST, entity, String.class);
        }

        //then
        String expected = "{\"message\":\"Rate limit exceeded\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + randomServerPort + uri;
    }
}
