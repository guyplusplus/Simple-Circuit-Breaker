package com.geckotechnology.simpleCircuitBreaker;

import java.util.logging.Logger;

public class CircuitBreaker {

    private static final Logger logger = Logger.getLogger(CircuitBreaker.class.getName());
    private CircuitBreakerStateInterface breakerState;
    private final CircuitBreakerConfig circuitBreakerConfig;

    public CircuitBreaker(CircuitBreakerConfig aCircuitBreakerDefinition) {
    	circuitBreakerConfig = aCircuitBreakerDefinition.clone();
    	circuitBreakerConfig.logInfoConfigProperties();
    	if(circuitBreakerConfig.getSlidingWindowSize() > 0)
    		moveToClosedState();
    	else if(circuitBreakerConfig.getSlidingWindowSize() == 0)
    		moveToDisabledState();
    	else if(circuitBreakerConfig.getSlidingWindowSize() == -1)
    		moveToForcedOpenState();
    }
    
    public synchronized boolean isClosedForThisCall() {
    	return breakerState.isClosedForThisCall();
    }
    public synchronized void callFailed(long callDuration) {
    	breakerState.callFailed(callDuration);
    }
    public synchronized void callSucceeded(long callDuration) {
    	breakerState.callSucceeded(callDuration);
    }
    
    //------ Only Private and Default access methods bellow --------------------------
    
    CircuitBreakerConfig getCircuitBreakerConfig() {
    	return circuitBreakerConfig;
    }
    
    void moveToClosedState() {
    	breakerState = new BreakerClosedState(this);
    	logger.info("Breaker state changed to: CLOSED");
    }

    void moveToOpenState() {
    	breakerState = new BreakerOpenState(this);
    	logger.info("Breaker state changed to: OPEN");
    }

    void moveToHalfOpenState() {
    	breakerState = new BreakerHalfOpenState(this);
    	logger.info("Breaker state changed to: HALF_OPEN");
    }

    void moveToDisabledState() {
    	breakerState = new BreakerDisabledState(this);
    	logger.info("Breaker state changed to: DISABLED");
    }

    void moveToForcedOpenState() {
    	breakerState = new BreakerForcedOpenState(this);
    	logger.info("Breaker state changed to: FORCED_OPEN");
    }
    
    boolean isSlowCall(long callDuration) {
    	if(circuitBreakerConfig.getSlowCallDurationThreshold() > 0 &&
    			callDuration >= circuitBreakerConfig.getSlowCallDurationThreshold())
    		return true;
    	return false;
    }
    
    boolean isExceedFailureOrSlowRateThreshold(int callCount, int failureCallCount, int slowCallDurationCount) {
        //calculating various rates, only if it any rate rule can apply
		//number of calls meets the minimumNumberOfCalls rule
		if(circuitBreakerConfig.getFailureRateThreshold() > 0) {
			float failureRate = (float)failureCallCount * 100f / (float)callCount; 
	        if(failureRate >= circuitBreakerConfig.getFailureRateThreshold()) {
				logger.warning("High failureRate: " + failureRate + "%, failureCallCount: " + failureCallCount + ", callCount: " + callCount);
	    		return true;
			}
		}
		if(circuitBreakerConfig.getSlowCallRateThreshold() > 0) {
			float slowCallRate = (float)slowCallDurationCount * 100f / (float)callCount; 
			if(slowCallRate >= circuitBreakerConfig.getSlowCallRateThreshold()) {
	    		logger.warning("High slowCallRate: " + slowCallRate + "%, slowCallDurationCount: " + slowCallDurationCount + ", callCount: " + callCount);
	    		return true;
			}
        }
        return false;
    }

}
