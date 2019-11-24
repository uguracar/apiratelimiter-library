# apiratelimiter-library

API Rate Limiter

# 1. Requirements

JDK 1.8 or above is required.
Redis Server is required (redis:3.0.7-alpine docker image is used).

# 2. How to use it?

```java
@RateLimit(prefix = "app:sampleMethod", key = "#userID", windowSize = MINUTE, limit = 20)
    public void sampleRateLimitedMethod(SampleRequest request) {
        System.out.println("sample rate limited method executed!");
    }
```

# 3. Options

* ```prefix``` - the prefix of identifier to limit against
* ```key``` - the identifier to limit against (userID, IP etc.)
* ```windowSize``` - the size of a window (SECOND, MINUTE or HOUR).
* ```limit``` - maximum number of requests in the given window size.


