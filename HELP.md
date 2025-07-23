

### What is a Circuit Breaker?
The idea of circuit breakers is to prevent calls to a remote service if we know that the call is likely to fail or time out. We do this so that we don’t unnecessarily waste critical resources both in our service and in the remote service. Backing off like this also gives the remote service some time to recover.

How do we know that a call is likely to fail? By keeping track of the results of the previous requests made to the remote service. If, say, 8 out of the previous 10 calls resulted in a failure or a timeout, the next call will likely also fail.

A circuit breaker keeps track of the responses by wrapping the call to the remote service. During normal operation, when the remote service is responding successfully, we say that the circuit breaker is in a “closed” state. When in the closed state, a circuit breaker passes the request through to the remote service normally.

When a remote service returns an error or times out, the circuit breaker increments an internal counter. If the count of errors exceeds a configured threshold, the circuit breaker switches to an “open” state. When in the open state, a circuit breaker immediately returns an error to the caller without even attempting the remote call.

After some configured time, the circuit breaker switches from open to a “half-open” state. In this state, it lets a few requests pass through to the remote service to check if it’s still unavailable or slow. If the error rate or slow call rate is above the configured threshold, it switches back to the open state. If the error rate or slow call rate is below the configured threshold, however, it switches to the closed state to resume normal operation.


### Curl 

#### Successful Response

```bash
curl http://localhost:8080/movies/1
```   
--- 
```bash
curl http://localhost:8080/movies/2
```   

#### Error Response
```bash
curl http://localhost:8080/movies/3
```   

a. simple-retry: simpleRetry retry instance will be triggered
#### Retry Type - simple-retry 
```bash
curl http://localhost:8080/movies/3?retryType=simple-retry
```

Based on below configuration , request will be retried 3 times after every 5 seconds. 
```yaml
resilience4j.retry:
  instances:
    simpleRetry:
      maxAttempts: 3
      waitDuration: 5s
```

b. retry-on-exception: retryOnException retry instance will be triggered.

#### Retry Type - retry-on-exception
```bash
curl http://localhost:8080/movies/3?retryType=retry-on-exception
```

Based on below configuration , request will be retried 4 times after every 3 seconds. 
```yaml
resilience4j.retry:
  instances:
    retryOnException:
      maxAttempts: 4
      waitDuration: 3s
      retryExceptions:
        - org.springframework.web.client.HttpClientErrorException
      ignoreExceptions:
        - com.example.resilience4j.exception.MovieNotFoundException
```

But when `http://localhost:8080/movies/4?retryType=retry-on-exception` is called , 
MovieNotFoundException exception will be thrown and there will be no retry as ignoreExceptions has ```MovieNotFoundException```.

c. retry-on-exception-predicate: retryBasedOnExceptionPredicate retry instance will be triggered.

#### Retry Type - retry-on-exception-predicate
```bash
curl http://localhost:8080/movies/4?retryType=retry-on-exception-predicate
```

Based on below configuration , request will be retried 3 times after every 4 seconds.
```yaml
resilience4j.retry:
  instances:
    retryBasedOnExceptionPredicate:
      maxAttempts: 3
      waitDuration: 4s
      retryExceptionPredicate: com.example.resilience4j.predicate.ExceptionPredicate
```

d. retry-on-conditional-predicate: retryBasedOnConditionalPredicate retry instance will be triggered.

#### Retry Type - retry-on-conditional-predicate
```bash
curl http://localhost:8080/movies/4?retryType=retry-on-conditional-predicate
```

Based on below configuration , request will be retried 2 times after every 5 seconds.
```yaml
resilience4j.retry:
  instances:
    retryBasedOnConditionalPredicate:
      maxAttempts: 2
      waitDuration: 5s
      resultPredicate: com.example.resilience4j.predicate.ConditionPredicate
```

e. retry-using-exponential-backoff: retryUsingExponentialBackoff retry instance will be triggered.

#### Retry Type - retry-using-exponential-backoff
```bash
curl http://localhost:8080/movies/4?retryType=retry-using-exponential-backoff
```

Based on below configuration , request will be retried 4 times.In this method, the wait time increases exponentially between attempts because of the multiplier. For example, if we specified an initial wait time of 1s and a multiplier of 2, the retries would be 
done after 1s, 2s, 4s, 8s, 16s, and so on. This method is a recommended approach when the client is a background job or a daemon.

```yaml
resilience4j.retry:
  instances:
    retryUsingExponentialBackoff:
      maxAttempts: 4
      waitDuration: 2s
      enableExponentialBackoff: true
      exponentialBackoffMultiplier: 2
```

f. retry-using-randomized-wait: retryUsingRandomizedWait retry instance will be triggered.

#### Retry Type - retry-using-randomized-wait
```bash
curl http://localhost:8080/movies/4?retryType=retry-using-randomized-wait
```

Based on below configuration , request will be retried 4 times. The randomizedWaitFactor determines the 
range over which the random value will be spread with regard to the specifiied waitDuration. 
So for the value of 0.5 above, the wait times generated will be between 1000ms (2000 - 2000 * 0.5) and 3000ms (2000 + 2000 * 0.5).

```yaml
resilience4j.retry:
  instances:
    retryUsingRandomizedWait:
      maxAttempts: 4
      waitDuration: 2s
      enableRandomizedWait: true
      randomizedWaitFactor: 0.7
```

g. retry-with-fallback: simpleRetry retry instance will be triggered and fallback method logic will be executed in this case.

#### Retry Type - retry-with-fallback
```bash
curl http://localhost:8080/movies/4?retryType=retry-with-fallback
```

Based on below configuration , request will be retried 4 times. The randomizedWaitFactor determines the
range over which the random value will be spread with regard to the specifiied waitDuration.
So for the value of 0.5 above, the wait times generated will be between 1000ms (2000 - 2000 * 0.5) and 3000ms (2000 + 2000 * 0.5).

Based on below configuration , request will be retried 3 times after every 5 seconds.
```yaml
resilience4j.retry:
  instances:
    simpleRetry:
      maxAttempts: 3
      waitDuration: 5s
```
After  the all the attempts below fallback method will be called. 
```java
    private Movie getMovieDetailsFallbackMethod(String movieId, MovieNotFoundException movieNotFoundException) {
    log.info("Fallback method called.");
    log.info("Original exception message: {}", movieNotFoundException.getMessage());
    return new Movie("Default", "N/A", "N/A", 0.0);
}
```


h. retry-with-custom-config: customRetryConfig retry instance defined in RetryConfiguration class will be triggered.

#### Retry Type - retry-with-custom-config
```bash
curl http://localhost:8080/movies/4?retryType=retry-with-custom-config
```

```java
@Configuration
@AllArgsConstructor
public class RetryConfiguration {

    private final RetryRegistry retryRegistry;

    @Bean
    public Retry retryWithCustomConfig() {
        RetryConfig customConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                .retryExceptions(HttpClientErrorException.class, HttpServerErrorException.class)
                .ignoreExceptions(MovieNotFoundException.class)
                .build();

        return retryRegistry.retry("customRetryConfig", customConfig);
    }
}
```
and above Custom configuration is close to what we have in below for `retryOnException`
```yaml
resilience4j.retry:
  instances:
    retryOnException:
      maxAttempts: 4
      waitDuration: 3s
      retryExceptions:
        - org.springframework.web.client.HttpClientErrorException
      ignoreExceptions:
        - com.example.resilience4j.exception.MovieNotFoundException
```


i. retry-with-event-details: retryWithEventDetails retry instance will be triggered.

#### Retry Type - retry-with-custom-config
```bash
curl http://localhost:8080/movies/4?retryType=retry-with-event-details
```