# Simple Circuit Breaker

This library is a fully featured Simple Circuit Breaker for JAVA 7 and above. It is directly inspired by [Resilience4j](https://resilience4j.readme.io/docs/circuitbreaker) in term of functionality and parameters. Only TIME_BASED sliding window is implemented in the current release.

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
| failureRateThreshold | 50 | If set to 0, breaker will ignore failures |
| slowCallRateThreshold | 100 | If set to 0, breaker will ignore slow calls |
| slowCallDurationThreshold | 60000 [ms] | If set to 0, breaker will ignore slow calls |
| permittedNumberOfCallsInHalfOpenState | 10 | 0 to move from open to closed state directly, without any half-open state |
| slidingWindowSize | 100 [s] | 0 to set breaker in DISABLED state, -1 to set breaker in FORCED_OPEN state |
| minimumNumberOfCalls | 10 | |
| waitDurationInOpenState | 60000 [ms] | |
| maxDurationOpenInHalfOpenState | 120000 [ms] | If set to 0, the breaker in HALF_OPEN state will wait forever for the outcome (fail or success) of all the permittedNumberOfCallsInHalfOpenState calls |


## Sample Code
Pseudo-code should look like bellow. Actual simple code can be found in [DemoApp](https://github.com/guyplusplus/Simple-Circuit-Breaker/blob/master/src/test/java/com/geckotechnology/simpleCircuitBreaker/DemoApp.java).

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

To avoid this situation a new property called maxDurationOpenInHalfOpenState is introduced. In HALF_OPEN state, after permittedNumberOfCallsInHalfOpenState calls to `isClosedForThisCall()` (which returns true), all its subsequent calls (which returns false) means no business logic should be executed as the circuit is opened. If this open circuit situation lasts longer than maxDurationOpenInHalfOpenState ms, the breaker goes back automatically to the CLOSED state.

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
SVC1.name=ABC
SVC1.failureRateThreshold=20
SVC1.slidingWindowSize=150
```

## Overhead
Load test, included in the JUnit tests, shows an overhead less than 0.05ms per wrapped logic. It is actually very difficult to measure as this time is very short and sensitive to server load and other factors like GC.

The load test is based on 4 concurrent threads running with a CLOSED circuit breaker, with a wrapped logic around 6.5ms.

## Concurrency
The code has 3 synchronized methods, it has minimum impact to initial code performance. Actual business logic (`doSomething()` in the pseudo-code above) is not included in the synchronized code, so blocking time is minimum
  - `boolean isClosedForThisCall()` to check the state of the breaker for this current call
  - `void callFailed(long callDuration)` to inform the breaker that the call failed
  - `void callSucceeded(long callDuration)` to inform the breaker that the call succeeded

## Event Listeners
The library supports listening for breaker state events. Registration and event consumption is straight forward. [DemoApp](https://github.com/guyplusplus/Simple-Circuit-Breaker/blob/master/src/test/java/com/geckotechnology/simpleCircuitBreaker/DemoApp.java) contains an example.

Registered Event Listeners are notified by a background thread. 

```java
circuitBreaker.getBreakerStateEventManager().addBreakerStateEventListener(new BreakerStateEventListener() {
    @Override
    public void onCircuitBreakerStateChangeEvent(CircuitBreakerStateChangeEvent event) {
        logger.warning("CircuitBreaker state changed. " + event);
        ...
    }
});
```

Events contain information about the breaker state change as well as the reason with statistics. Here is content when events are logged to console.

```
config:{name:ABC, failureRateThreshold:75.1, slowCallRateThreshold:45.0, slowCallDurationThreshold:528, permittedNumberOfCallsInHalfOpenState:5, slidingWindowSize:30, minimumNumberOfCalls:20, waitDurationOpenedState:2000, maxDurationOpenInHalfOpenState:1100}
...
...
CircuitBreaker state changed. circuitBreakerName:ABC, newBreakerStateType:OPEN, creationTimestamp:1584250428589, details:"Threshold exceeded. countStats:{callCount:80, failureCallCount:26, slowCallDurationCount:36, failureRate:32.5, slowCallRate:45.0}"
...
...
CircuitBreaker state changed. circuitBreakerName:ABC, newBreakerStateType:HALF_OPEN, creationTimestamp:1584250430632, details:"WaitDurationInOpenState is over"
...
...
CircuitBreaker state changed. circuitBreakerName:ABC, newBreakerStateType:OPEN, creationTimestamp:1584250432231, details:"Reached permittedNumberOfCallsInHalfOpenState and threshold exceeded. countStats:{callCount:5, failureCallCount:1, slowCallDurationCount:4, failureRate:20.0, slowCallRate:80.0}"
...
...
CircuitBreaker state changed. circuitBreakerName:ABC, newBreakerStateType:HALF_OPEN, creationTimestamp:1584250434265, details:"WaitDurationInOpenState is over"
...
...
CircuitBreaker state changed. circuitBreakerName:ABC, newBreakerStateType:CLOSED, creationTimestamp:1584250435804, details:"Reached permittedNumberOfCallsInHalfOpenState and no threshold exceeded. countStats:{callCount:5, failureCallCount:3, slowCallDurationCount:2, failureRate:60.0, slowCallRate:40.0}"
...
...
```

Another example showing the behavior of maxDurationOpenInHalfOpenState.

```
...
...
CircuitBreaker state changed. circuitBreakerName:ABC, newBreakerStateType:CLOSED, creationTimestamp:1584250604694, details:"Reached permittedNumberOfCallsInHalfOpenState and no threshold exceeded. countStats:{callCount:5, failureCallCount:0, slowCallDurationCount:2, failureRate:0.0, slowCallRate:40.0}"
...
...
CircuitBreaker state changed. circuitBreakerName:ABC, newBreakerStateType:OPEN, creationTimestamp:1584250609427, details:"Threshold exceeded. countStats:{callCount:20, failureCallCount:3, slowCallDurationCount:10, failureRate:15.0, slowCallRate:50.0}"
...
...
CircuitBreaker state changed. circuitBreakerName:ABC, newBreakerStateType:HALF_OPEN, creationTimestamp:1584250611443, details:"WaitDurationInOpenState is over"
...
...
CircuitBreaker state changed. circuitBreakerName:ABC, newBreakerStateType:CLOSED, creationTimestamp:1584250613133, details:"MaxDurationOpenInHalfOpenState is over. countStats:{callCount:4, failureCallCount:0, slowCallDurationCount:2}"
...
...
```