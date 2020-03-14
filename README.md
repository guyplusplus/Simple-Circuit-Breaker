# Simple Circuit Breaker

This library is a Simple Circuit Breaker for JAVA 7 and above. It is directly inspired by [Resilience4j](https://resilience4j.readme.io/docs/circuitbreaker) in term of functionality and parameters. Only TIME_BASED sliding window is implemented in the current release.

It supports the 5 states:
  - OPEN
  - CLOSED
  - HALF_OPEN
  - DISABLED: broker is always closed (via property slidingWindowSize set to 0)
  - FORCED_OPEN: broker is always opened (via property slidingWindowSize set to -1)

![State Machine](./state_machine.jpg)


The following Resilience4j configuration properties are supported.
maxDurationOpenInHalfOpenState is a new property described in the sample code paragraph.
name is a new property that is used for logging purpose. If specified, each line is prefixed by [name].

| Config property | Default Value | Special Values |
| ------------- | ------------- | --------|
| name | Empty string | |
| failureRateThreshold  | 50  | If set to 0, breaker will ignore failures |
| slowCallRateThreshold  | 100 | If set to 0, breaker will ignore slow calls |
| slowCallDurationThreshold  | 60000 [ms] | If set to 0, breaker will ignore slow calls |
| permittedNumberOfCallsInHalfOpenState  | 10 | 0 to move from open to closed state directly, without any half-open state |
| slidingWindowSize  | 100 [s] | 0 to set breaker in DISABLED state, -1 to set breaker in FORCED_OPEN state |
| minimumNumberOfCalls  | 10 | |
| waitDurationInOpenState  | 60000 [ms] | |
| maxDurationOpenInHalfOpenState | 120000 [ms] | If set to 0, the breaker in HALF_OPEN state will wait forever for the outcome (fail or success) of all the permittedNumberOfCallsInHalfOpenState calls |


## Sample Code
Pseudo-code should look like this:

```
CircuitBreakerConfig config = new CircuitBreakerConfig();
config.set...;
logger.info(config);
CircuitBreaker circuitBreaker = new CircuitBreaker(config);
loop
  if(circuitBreaker.isClosedForThisCall())
    doSomething();
    if success
      circuitBreaker.callSucceeded(doSomething duration);
    else
      circuitBreaker.callFailed(doSomething duration);
```

**Important**: `callSucceeded()` or `callFailed()` must always be invoked after `isClosedForThisCall()`. Otherwise breaker in HALF_OPEN state will never move to another state, waiting for the results of the permittedNumberOfCallsInHalfOpenState calls.

To avoid this situation a new property called maxDurationOpenInHalfOpenState is introduced. In HALF_OPEN state, after permittedNumberOfCallsInHalfOpenState calls (`isClosedForThisCall()` returns true), all its subsequent calls (`isClosedForThisCall()` returns false) should not be executed as the circuit is opened. If this situation lasts longer than maxDurationOpenInHalfOpenState ms, the breaker goes back automatically to the CLOSED state.

## Circuit Breaker Configuration using Properties
The circuit breaker can easily be configured using `java.util.Properties`, possibly adding prefix, for example:

```java
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
SVC1.slidingWindowSize=150
```

## Overhead
Load test, included in the JUnit tests, shows an overhead less than 0.05ms per wrapped logic.

The load test is based on 4 concurrent threads running with a CLOSED circuit breaker, with a wrapped logic around 6.5ms.

## Concurrency
The code has 3 methods with a synchronized portion, it has minimum impact to initial code performance. Actual business logic (`doSomething` in the pseudo-code above) is not included in the synchronized code, so blocking time is minimum
  - `boolean isClosedForThisCall()` to check the state of the breaker for this current call
  - `void callFailed(long callDuration)` to inform the breaker that the call failed
  - `void callSucceeded(long callDuration)` to inform the breaker that the call succeeded

Registered EventListeners are informed by the thread performing the business logic, but outside any synchronized code. 

## Event Listeners
The library supports simple event listener. Registration and consumption is straight forward.

```
circuitBreaker.getBreakerStateEventManager().addBreakerStateEventListener(new BreakerStateEventListener() {
	@Override
	public void onCircuitBreakerStateChangeEvent(CircuitBreakerStateChangeEvent event) {
		logger.warning("CircuitBreaker state changed. " + event);
		...
	}
});

```

## Log File Output
Log file contains information about the breaker state change as well as the reason and simple statistics. Here is simple content. Log monitoring can be used to capture events.

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
Mar. 07, 2020 6:56:29 PM com.geckotechnology.simpleCircuitBreaker.BreakerHalfOpenState callFailedOrSuccedded
INFO: callCount: 3, failureCallCount: 1, slowCallDurationCount: 1
Mar. 07, 2020 6:56:29 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker moveToClosedState
INFO: Breaker state changed to: CLOSED
...
...
```

Another example showing the behavior of maxDurationOpenInHalfOpenState.

```
...
...
Mar. 08, 2020 5:02:27 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker moveToClosedState
INFO: Breaker state changed to: CLOSED
...
...
Mar. 08, 2020 5:02:52 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker isExceedFailureOrSlowRateThreshold
WARNING: High slowCallRate: 45.299145%, slowCallDurationCount: 53, callCount: 117
Mar. 08, 2020 5:02:52 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker moveToOpenState
INFO: Breaker state changed to: OPEN
...
...
Mar. 08, 2020 5:02:54 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker moveToHalfOpenState
INFO: Breaker state changed to: HALF_OPEN
...
...
Mar. 08, 2020 5:02:55 PM com.geckotechnology.simpleCircuitBreaker.BreakerHalfOpenState isClosedForThisCall
WARNING: maxDurationOpenInHalfOpenState is reached. CallCount: 4, failureCallCount: 1, slowCallDurationCount: 1
Mar. 08, 2020 5:02:55 PM com.geckotechnology.simpleCircuitBreaker.CircuitBreaker moveToClosedState
INFO: Breaker state changed to: CLOSED
...
...
```

