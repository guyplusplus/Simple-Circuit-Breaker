package com.geckotechnology.simpleCircuitBreaker;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;


/**
 * Simple Circuit Breaker for JAVA 7 and above
 * It is inspired by 2 libraries:
 *   (A) https://github.com/hemantksingh/circuit-breaker
 *   (B) https://resilience4j.readme.io/docs/circuitbreaker
 * 
 * The circuit breaker can break under 4 conditions:
 * - high number of failures or high rate of failures
 * - high number of slow transactions or high rate of slow transactions. To calculate rate, a minimum number of transaction calls is required
 * 
 * The logic is based on a fixed time windows such as (B) [no sliding time window such as (B)]
 * where statistics are gathered. When time windows expires, statistics are reset.
 * Metrics and thresholds replicate similar logic as in (B).
 * There is no half-opened state logic in this implementation.
 * 
 * To be back in closed state, either:
 * - wait for current time window to expire and have counter to reset, which could be long or short
 * - wait a specified amount of time, then the breaker statistics are reset
 * 
 * Once the circuit breaker is not used, terminate() should be invoked to stop its associated time task.
 * 
 * Code should look like this:
 * 
 * CircuitBreakerConfig config = new CircuitBreakerConfig();
 * config.set...;
 * CircuitBreaker circuitBreaker = new CircuitBreaker(config);
 * loop
 *   if(circuitBreaker.isClosed())
 *     doSomething();
 *     if success
 *       circuitBreaker.callSucceeded(doSomething duration);
 *     else
 *       circuitBreaker.callFailed(doSomething duration);
 * circuitBreaker.terminate();
 * 
 * The code has synchronized methods, 2 public and 1 private:
 *   - private synchronized void reset() to reset counters on a regular basis
 *   - public synchronized void callFailed(long callDuration)
 *   - public synchronized void callSucceeded(long callDuration)
 */
public class CircuitBreaker {

    private static final Logger logger = Logger.getLogger(CircuitBreaker.class.getName());
    private static final Timer timer = new Timer();
    
    private TimerTask timerTask;
    private final CircuitBreakerConfig circuitBreakerConfig;
    private int callCount = 0;
    private int failureCount = 0;
    private int slowCallDurationCount = 0;
    private float failureRate = -1;
    private float slowCallRate = -1;
    private boolean isClosed = true;

    
    public CircuitBreaker(CircuitBreakerConfig aCircuitBreakerDefinition) {
    	circuitBreakerConfig = aCircuitBreakerDefinition.clone();
    	circuitBreakerConfig.logInfoConfigProperties();
    	setResetCircuitPeriodically(circuitBreakerConfig.getResetInterval());
    }
    
    private TimerTask createResetTimerTask(final CircuitBreaker circuitBreaker) {
    	return new TimerTask() {
            @Override
            public void run() {
            	circuitBreaker.reset();
            }
        };
    }

    /**
     * Setup reset timer task every resetInterval. If resetInterval is 0, no task is created
     * @param delayBeforeNextExecution: how long to wait until tasks is calls
     */
    private void setResetCircuitPeriodically(long delayBeforeNextExecution) {
    	if(timerTask != null)
    		timerTask.cancel();
    	if(circuitBreakerConfig.getResetInterval() > 0) {
        	timerTask = createResetTimerTask(this);
    		timer.scheduleAtFixedRate(timerTask, delayBeforeNextExecution, circuitBreakerConfig.getResetInterval());
    	}
    	else
    		timerTask = null;
    }

    private synchronized void reset() {
        callCount = 0;
        failureCount = 0;
        slowCallDurationCount = 0;
        failureRate = -1;
        slowCallRate = -1;
        String msg = "Resetting breaker statistics. Now in closed state";
        if(isClosed)
            logger.fine(msg);
        else
        	logger.warning(msg);
        isClosed = true;
    }
    
    public boolean isClosed() {
//    	logger.fine("callCount: " + callCount);
//    	logger.fine("successCount: " + successCount);
//      logger.fine("failureCount: " + failureCount + ", failureCountThreshold: " + circuitBreakerDefinition.getFailureCountThreshold());
//      logger.fine("slowCallDurationCount: " + slowCallDurationCount + ", slowCallCountThreshold: " + circuitBreakerDefinition.getSlowCallCountThreshold());
        return isClosed;
    }

    public int getCallCount() {
    	return callCount;
    }
    
    public int getSlowCallDurationCount() {
    	return slowCallDurationCount;
    }

    public synchronized void callFailed(long callDuration) {
    	failureCount++;
    	callFailedOrSuccedded(callDuration);
    }
    
    public synchronized void callSucceeded(long callDuration) {
    	callFailedOrSuccedded(callDuration);
    }
    
    /**
     * Method called only by synchronized callFailed or callSucceeded 
     */
    private void callFailedOrSuccedded(long callDuration) {
    	callCount++;
    	if(circuitBreakerConfig.getSlowCallDurationThreshold() > 0 &&
    			callDuration >= circuitBreakerConfig.getSlowCallDurationThreshold())
    		slowCallDurationCount++;
    	//check if breaker active
    	if(circuitBreakerConfig.getResetInterval() == 0)
    		return;
    	//evaluate breaker needs to break and open circuit 
        //first, no point reevaluating status is breaker is opened already
        if(!isClosed)
        	return;
        //circuit is closed here
        //calculating various rates, only if it any rate rule can apply
        if(circuitBreakerConfig.getMinimumNumberOfCalls() > 0) {
        	if(callCount >= circuitBreakerConfig.getMinimumNumberOfCalls() ) {
        		//number of calls meets the minimumNumberOfCalls rule
        		if(circuitBreakerConfig.getFailureRateThreshold() > 0) {
        			failureRate = (float)failureCount * 100f / (float)callCount; 
        		}
        		if(circuitBreakerConfig.getSlowCallRateThreshold() > 0) {
        			slowCallRate = (float)slowCallDurationCount * 100f / (float)callCount; 
        		}
        	}
        }
        //checking various conditions if opening circuit is required
        if(circuitBreakerConfig.getFailureCountThreshold() > 0) {
        	if(failureCount >= circuitBreakerConfig.getFailureCountThreshold()) {
        		logger.warning("Opening circuit, high failureCount: " + failureCount);
        		isClosed = false;
        	}
        }
        if(circuitBreakerConfig.getSlowCallCountThreshold() > 0) {
        	if(slowCallDurationCount >= circuitBreakerConfig.getSlowCallCountThreshold()) {
        		logger.warning("Opening circuit, high slowCallDurationCount: " + slowCallDurationCount);
        		isClosed = false;
        	}
        }
        if(failureRate >= circuitBreakerConfig.getFailureRateThreshold()) {
			logger.warning("Opening circuit, high failureRate: " + failureRate + "%, failureCount: " + failureCount + ", callCount: " + callCount);
    		isClosed = false;
		}
		if(slowCallRate >= circuitBreakerConfig.getSlowCallRateThreshold()) {
    		logger.warning("Opening circuit, high slowCallRate: " + slowCallRate + "%, slowCallDurationCount: " + slowCallDurationCount + ", callCount: " + callCount);
    		isClosed = false;
		}
		if(!isClosed) {
			//it was in closed and changed to opened state
			if(circuitBreakerConfig.getWaitDurationOpenedState() > 0) {
				setResetCircuitPeriodically(circuitBreakerConfig.getWaitDurationOpenedState());
				logger.warning("Breaker reset will happen in " + circuitBreakerConfig.getWaitDurationOpenedState() + " ms (waitDurationOpenedState)");
			}
			else
				logger.warning("Breaker reset happens every " + circuitBreakerConfig.getResetInterval() + " ms (resetInterval)");
		}
    }
    
    /**
     * Must be called when the breaker is not used anymore. It stops the reset timer task
     */
    public void terminate() {
    	if(timerTask != null) {
	    	timerTask.cancel();
	    	timerTask = null;
    	}    	
    }
}