# Simple Circuit Breaker

Simple Circuit Breaker for JAVA 7 and above
It is inspired by 2 libraries:
- (A) [Circuit Breaker](https://github.com/hemantksingh/circuit-breaker) by Hemant Kumar
- (B) [Resilience4j](https://resilience4j.readme.io/docs/circuitbreaker)

The circuit breaker can break under 4 conditions:
- high number of failures or high rate of failures
- high number of slow transactions or high rate of slow transactions. To calculate rate, a minimum number of transaction calls is required

The logic is based on a fixed time windows such as (B) [no sliding time window such as (B)]
where statistics are gathered. When time windows expires, statistics are reset.
Metrics and thresholds replicate similar logic as in (B).
There is no half-opened state logic in this implementation.

To be back in closed state, either:
- wait for current time window to expire and have counter to reset, which could be long or short
- wait a specified amount of time, then the breaker statistics are reset

Once the circuit breaker is not used, terminate() should be invoked to stop its associated time task.

## Sample Code
Code should look like this:

```
CircuitBreakerConfig config = new CircuitBreakerConfig();
config.set...;
CircuitBreaker circuitBreaker = new CircuitBreaker(config);
loop
  if(circuitBreaker.isClosed())
    doSomething();
    if success
      circuitBreaker.callSucceeded(doSomething duration);
    else
      circuitBreaker.callFailed(doSomething duration);
circuitBreaker.terminate();
```

## Concurrency
The code has synchronized methods, 2 public and 1 private, with has minimum impact to initial code performance:
  - private synchronized void reset() to reset counters on a regular basis
  - public synchronized void callFailed(long callDuration)
  - public synchronized void callSucceeded(long callDuration)
