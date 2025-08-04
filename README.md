## Circuit Breaker Pattern with Resilience4J
The Circuit Breaker pattern is essential in microservices architecture to prevent cascading failures. 
When a service fails repeatedly due to network issues, unavailability, or other reasons, 
the Circuit Breaker opens to prevent additional requests from hitting the failing service. 
This approach helps maintain the system's overall health and provides fallback mechanisms for graceful degradation.

### Successful Response 

```bash
curl http://localhost:8080/movies/3

{"id":"1","title":"The Matrix","director":"Lana Wachowski, Lilly Wachowski","rating":8.7}
```
```bash
curl http://localhost:8080/movies/2

{"id":"2","title":"Inception","director":"Christopher Nolan","rating":8.8}
```

### Error Response for Movie Id 3 & 4
```bash
curl http://localhost:8080/movies/3

{"timestamp":"2025-07-24T00:39:31.781+00:00","status":500,"error":"Internal Server Error","path":"/movies/3"}
```

```bash
curl http://localhost:8080/movies/4

{"timestamp":"2025-07-24T00:40:18.259+00:00","status":500,"error":"Internal Server Error","path":"/movies/4"}
```

Different circuit breaker instances are defined inside the application.yml. To mimic different circuit breaker scenarios use:

a. count-based-circuit-breaker: countBasedCircuitBreaker circuit breaker instance will be triggered

### Test script
```bash
    for i in {1..5}; do
      curl --location "http://localhost:8080/movies/4"
      sleep 1
    done
```

### Configuration
```yaml
resilience4j.circuitbreaker:
  instances:
    countBasedCircuitBreaker:   # count-based-circuit-breaker
      registerHealthIndicator: true
      #Configures the type of the sliding window which is used to record the outcome of calls when the CircuitBreaker is closed.
      #Sliding window can either be count-based or time-based.      
      slidingWindowType: COUNT_BASED # 
      slidingWindowSize: 5 # Defines the size of the sliding window used for calculating the failure rate. It can be count-based or time-based.
      # Specifies the minimum number of calls needed before the Circuit Breaker begins calculating the failure rate.
      # The Circuit Breaker will not open until this number is reached.
      minimumNumberOfCalls: 5
      permittedNumberOfCallsInHalfOpenState: 3 # Number of test calls allowed in the half-open state to determine if the Circuit Breaker should close again.
      automaticTransitionFromOpenToHalfOpenEnabled: true
      waitDurationInOpenState: 2000 # Duration the Circuit Breaker stays in the open state before transitioning to the half-open state.
      # When the failure rate is equal or greater than the threshold the CircuitBreaker transitions to open and starts short-circuiting calls
      failureRateThreshold: 60
      eventConsumerBufferSize: 10 
```

### Actuator Response [http://localhost:8080/actuator/circuitbreakers]
```json
{
  "status": "UP",
  "components": {
    "circuitBreakers": {
      "status": "UP",
      "details": {
        "countBasedCircuitBreaker": {
          "status": "CIRCUIT_OPEN",
          "details": {
            "failureRate": "100.0%",
            "failureRateThreshold": "60.0%",
            "slowCallRate": "0.0%",
            "slowCallRateThreshold": "100.0%",
            "bufferedCalls": 3,
            "slowCalls": 0,
            "slowFailedCalls": 0,
            "failedCalls": 3,
            "notPermittedCalls": 1,
            "state": "OPEN"
          }
        }

```

b. time-based-circuit-breaker: timeBasedCircuitBreaker circuit breaker instance will be triggered.

### Test script
```bash
    for i in {1..100}; do
      curl --location "http://localhost:8080/movies/3?circuitBreakerType=time-based-circuit-breaker&requestId=$i"
      sleep 1
    done
```
### Configuration  
```yaml
resilience4j.circuitbreaker:
  configs:
    default:
      registerHealthIndicator: true
  instances:
    timeBasedCircuitBreaker:
      #registerHealthIndicator: true
      slidingWindowType: TIME_BASED
      slidingWindowSize: 60
      minimumNumberOfCalls: 5
      permittedNumberOfCallsInHalfOpenState: 3
      automaticTransitionFromOpenToHalfOpenEnabled: true
      waitDurationInOpenState: 5000
      failureRateThreshold: 60
```

### Actuator Response [http://localhost:8080/actuator/circuitbreakers]
```json
  "timeBasedCircuitBreaker": {
      "failureRate": "-1.0%",
      "slowCallRate": "-1.0%",
      "failureRateThreshold": "60.0%",
      "slowCallRateThreshold": "100.0%",
      "bufferedCalls": 0,
      "failedCalls": 0,
      "slowCalls": 0,
      "slowFailedCalls": 0,
      "notPermittedCalls": 0,
      "state": "HALF_OPEN"
    }
```

c. circuit-breaker-on-exception: circuitBreakerOnException circuit breaker instance will be triggered.


### Test script
```bash
    for i in {1..100}; do
      curl --location "http://localhost:8080/movies/3?circuitBreakerType=circuit-breaker-on-exception&requestId=$i"
      sleep 1
    done
```


### Configuration
```yaml
    circuitBreakerOnException:
      #registerHealthIndicator: true
      slidingWindowType: COUNT_BASED
      slidingWindowSize: 5
      minimumNumberOfCalls: 5
      permittedNumberOfCallsInHalfOpenState: 3
      automaticTransitionFromOpenToHalfOpenEnabled: true
      waitDurationInOpenState: 5000
      failureRateThreshold: 60
      recordExceptions:
        - org.springframework.web.client.HttpClientErrorException
      ignoreExceptions:
        - com.example.resilience4j.exception.MovieNotFoundException
```

### Actuator Response [http://localhost:8080/actuator/circuitbreakerevents/circuitBreakerOnException]

```json
   {
      "circuitBreakerName": "circuitBreakerOnException",
      "type": "FAILURE_RATE_EXCEEDED",
      "creationTime": "2025-07-23T22:03:34.662563-05:00[America/Chicago]",
      "errorMessage": null,
      "durationInMs": null,
      "stateTransition": null
    },
    {
      "circuitBreakerName": "circuitBreakerOnException",
      "type": "STATE_TRANSITION",
      "creationTime": "2025-07-23T22:03:34.662762-05:00[America/Chicago]",
      "errorMessage": null,
      "durationInMs": null,
      "stateTransition": "CLOSED_TO_OPEN"
    }

```

```json
{
  "circuitBreakers": {
    "circuitBreakerForSlowCalls": {
      "failureRate": "-1.0%",
      "slowCallRate": "-1.0%",
      "failureRateThreshold": "50.0%",
      "slowCallRateThreshold": "60.0%",
      "bufferedCalls": 0,
      "failedCalls": 0,
      "slowCalls": 0,
      "slowFailedCalls": 0,
      "notPermittedCalls": 0,
      "state": "CLOSED"
    },
    "circuitBreakerOnException": {
      "failureRate": "-1.0%",
      "slowCallRate": "-1.0%",
      "failureRateThreshold": "60.0%",
      "slowCallRateThreshold": "100.0%",
      "bufferedCalls": 0,
      "failedCalls": 0,
      "slowCalls": 0,
      "slowFailedCalls": 0,
      "notPermittedCalls": 0,
      "state": "HALF_OPEN"
    }


```

d. circuit-breaker-with-record-failure-predicate: circuitBreakerWithRecordFailurePredicate circuit breaker instance will be triggered.

e. circuit-breaker-with-ignore-exception-predicate: circuitBreakerWithIgnoreExceptionPredicate circuit breaker instance will be triggered.

f. circuit-breaker-for-slow-calls: circuitBreakerForSlowCalls circuit breaker instance will be triggered.

g. circuit-breaker-with-fallback: countBasedCircuitBreaker circuit breaker instance will be triggered and fallback method logic will be executed in this case.

h. custom-circuit-breaker: customCircuitBreaker circuit breaker instance defined in CircuitBreakerConfiguration class will be triggered.