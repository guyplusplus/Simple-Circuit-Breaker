package com.geckotechnology.simpleCircuitBreaker;

public class CircuitBreaker {

    private static final String INIT_REASON = "initial state";
    private BreakerStateInterface breakerState;
    private final CircuitBreakerConfig circuitBreakerConfig;
    private final BreakerStateEventManager breakerStateEventManager;

    public CircuitBreaker(CircuitBreakerConfig aCircuitBreakerDefinition) {
    	circuitBreakerConfig = aCircuitBreakerDefinition.clone();
    	breakerStateEventManager = new BreakerStateEventManager();
    	if(circuitBreakerConfig.getSlidingWindowSize() > 0)
    		moveToClosedState(INIT_REASON);
    	else if(circuitBreakerConfig.getSlidingWindowSize() == 0)
    		moveToDisabledState(INIT_REASON);
    	else if(circuitBreakerConfig.getSlidingWindowSize() == -1)
    		moveToForcedOpenState(INIT_REASON);
    }
    
    public BreakerStateEventManager getBreakerStateEventManager() {
    	return breakerStateEventManager;
    }
    
    public boolean isClosedForThisCall() {
    	boolean isClosedForThisCall;
    	synchronized(this) {
    		isClosedForThisCall = breakerState.isClosedForThisCall();
    	}
    	breakerStateEventManager.processEventQueue();
    	return isClosedForThisCall;
    }
    public void callFailed(long callDuration) {
    	synchronized(this) {
    		breakerState.callFailed(callDuration);
    	}
    	breakerStateEventManager.processEventQueue();
    }
    public void callSucceeded(long callDuration) {
    	synchronized(this) {
    		breakerState.callSucceeded(callDuration);
    	}
    	breakerStateEventManager.processEventQueue();
    }
    
    //------ Only Private and Default access methods bellow --------------------------
    
    CircuitBreakerConfig getCircuitBreakerConfig() {
    	return circuitBreakerConfig;
    }
    
    /**
     * Used for unit test. Method is not synchronized
     * @return current state object of the breaker
     */
    BreakerStateInterface getBreakerState() {
    	return breakerState;
    }
    
    void moveToClosedState(String cause) {
    	breakerState = new BreakerClosedState(this);
    	breakerStateEventManager.registerEvent(new CircuitBreakerStateChangeEvent(circuitBreakerConfig.getName(), breakerState.getBreakerStateType(), cause));
    }

    void moveToOpenState(String cause) {
    	breakerState = new BreakerOpenState(this);
    	breakerStateEventManager.registerEvent(new CircuitBreakerStateChangeEvent(circuitBreakerConfig.getName(), breakerState.getBreakerStateType(), cause));
    }

    void moveToHalfOpenState(String cause) {
    	breakerState = new BreakerHalfOpenState(this);
    	breakerStateEventManager.registerEvent(new CircuitBreakerStateChangeEvent(circuitBreakerConfig.getName(), breakerState.getBreakerStateType(), cause));
    }

    void moveToDisabledState(String cause) {
    	breakerState = new BreakerDisabledState(this);
    	breakerStateEventManager.registerEvent(new CircuitBreakerStateChangeEvent(circuitBreakerConfig.getName(), breakerState.getBreakerStateType(), cause));
    }

    void moveToForcedOpenState(String cause) {
    	breakerState = new BreakerForcedOpenState(this);
    	breakerStateEventManager.registerEvent(new CircuitBreakerStateChangeEvent(circuitBreakerConfig.getName(), breakerState.getBreakerStateType(), cause));
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
	    		return true;
			}
		}
		if(circuitBreakerConfig.getSlowCallRateThreshold() > 0) {
			float slowCallRate = (float)slowCallDurationCount * 100f / (float)callCount; 
			if(slowCallRate >= circuitBreakerConfig.getSlowCallRateThreshold()) {
	    		return true;
			}
        }
        return false;
    }
    
    String getExpressiveStatsAsReason(int callCount, int failureCallCount, int slowCallDurationCount) {
		StringBuilder sb = new StringBuilder("stats:{");
		sb.append("callCount:").append(callCount);
		sb.append(", ").append("failureCallCount:").append(failureCallCount);
		if(circuitBreakerConfig.getFailureRateThreshold() > 0) {
			float failureRate = (float)failureCallCount * 100f / (float)callCount;
			sb.append(" (failureRate:").append(failureRate);
			sb.append(", failureRateThreshold:").append(circuitBreakerConfig.getFailureRateThreshold()).append(')');
		}
		sb.append(", ").append("slowCallDurationCount:").append(slowCallDurationCount);
		if(circuitBreakerConfig.getSlowCallRateThreshold() > 0) {
			float slowCallRate = (float)slowCallDurationCount * 100f / (float)callCount; 
			sb.append(" (slowCallRate:").append(slowCallRate);
			sb.append(", slowCallRateThreshold:").append(circuitBreakerConfig.getSlowCallRateThreshold()).append(')');
		}
		sb.append("}");
		return sb.toString();

    }

}
