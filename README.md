# Simple Circuit Breaker

This library is a Simple Circuit Breaker for JAVA 7 and above. It is directly inspired by [Resilience4j](https://resilience4j.readme.io/docs/circuitbreaker) in term of functionality and parameters. Only TIME_BASED sliding window is implemented in the current release.

It supports the 5 states:
  - OPEN
  - CLOSED
  - HALF-OPEN
  - DISABLED: broker is always closed (via property slidingWindowSize)
  - FORCED_OPEN: broker is always opened (via property slidingWindowSize)

![State Machine](./state_machine.jpg)


The following configuration properties are supported

| Config property | Default Value | Special values |
| ------------- | ------------- | --------|
| failureRateThreshold  | 50  | If set to 0, breaker will not open due to failures |
| slowCallRateThreshold  | 100 | If set to 0, breaker will not open due to slow calls |
| slowCallDurationThreshold  | 60000 [ms] | If set to 0, breaker will not open due to slow calls |
| permittedNumberOfCallsInHalfOpenState  | 10 | 0 to move from open to closed state directly, without any half-open state |
| slidingWindowSize  | 100 [s] | 0 to set breaker in DISABLED state, -1 to set breaker in FORCED_OPEN state |
| minimumNumberOfCalls  | 10 | |
| waitDurationInOpenState  | 60000 [ms] | |


## Sample Code
Pseudo-code should look like this:

```
CircuitBreakerConfig config = new CircuitBreakerConfig();
config.set...;
CircuitBreaker circuitBreaker = new CircuitBreaker(config);
loop
  if(circuitBreaker.isClosedForThisCall())
    doSomething();
    if success
      circuitBreaker.callSucceeded(doSomething duration);
    else
      circuitBreaker.callFailed(doSomething duration);
```

## Circuit Breaker Configuration using Properties
The circuit breaker can easily be configured using `java.util.Properties`, possibly adding prefix, for example:

```
Properties properties = new Properties();
FileInputStream fis = new FileInputStream("my-breaker.config");
props.load(fis);
fis.close();
CircuitBreakerConfig config = new CircuitBreakerConfig("SVC1.", properties);
CircuitBreaker circuitBreaker = new CircuitBreaker(config);
```

Where the file `my-breaker.config` contains values to override the default values:

```
SVC1.failureRateThreshold=20
SVC1.slidingWindowSize=100
```

## Log File
Log file contains information about the breaker state change as well as reason. Here is simple content. Log monitoring can be used to capture events.

```
Mar. 07, 2020 6:56:21 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreakerConfig logInfoConfigProperties
INFO: CircuitBreakerConfig:
Mar. 07, 2020 6:56:21 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreakerConfig logInfoConfigProperties
INFO: 	failureRateThreshold: 55.1
Mar. 07, 2020 6:56:21 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreakerConfig logInfoConfigProperties
INFO: 	slowCallRateThreshold: 40.0
Mar. 07, 2020 6:56:21 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreakerConfig logInfoConfigProperties
INFO: 	slowCallDurationThreshold: 600
Mar. 07, 2020 6:56:21 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreakerConfig logInfoConfigProperties
INFO: 	permittedNumberOfCallsInHalfOpenState: 3
Mar. 07, 2020 6:56:21 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreakerConfig logInfoConfigProperties
INFO: 	slidingWindowSize: 30
Mar. 07, 2020 6:56:21 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreakerConfig logInfoConfigProperties
INFO: 	minimumNumberOfCalls: 6
Mar. 07, 2020 6:56:21 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreakerConfig logInfoConfigProperties
INFO: 	waitDurationOpenedState: 2000
Mar. 07, 2020 6:56:21 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker moveToClosedState
INFO: Breaker state changed to: CLOSED
...
...
Mar. 07, 2020 6:56:22 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker isExceedFailureOrSlowRateThreshold
WARNING: High slowCallRate: 66.666664%, slowCallDurationCount: 4, callCount: 6
Mar. 07, 2020 6:56:22 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker moveToOpenState
INFO: Breaker state changed to: OPEN
...
...
Mar. 07, 2020 6:56:24 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker moveToHalfOpenState
INFO: Breaker state changed to: HALF_OPEN
...
...
Mar. 07, 2020 6:56:25 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker isExceedFailureOrSlowRateThreshold
WARNING: High slowCallRate: 66.666664%, slowCallDurationCount: 2, callCount: 3
Mar. 07, 2020 6:56:25 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker moveToOpenState
INFO: Breaker state changed to: OPEN
...
...
Mar. 07, 2020 6:56:27 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker moveToHalfOpenState
INFO: Breaker state changed to: HALF_OPEN
...
...
Mar. 07, 2020 6:56:29 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker moveToClosedState
INFO: Breaker state changed to: CLOSED
...
...
```

## Concurrency
The code has 3 synchronized methods, so it has minimum impact to initial code performance
  - `boolean isClosedForThisCall()` to check the state of the breaker for this current call
  - `void callFailed(long callDuration)` to inform the breaker that the call failed
  - `void callSucceeded(long callDuration)` to inform the breaker that the call succeeded
