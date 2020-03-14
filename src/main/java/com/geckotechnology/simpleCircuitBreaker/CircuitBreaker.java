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
    
    void moveToClosedState(String details) {
    	breakerState = new BreakerClosedState(this);
    	registerEvent(details);
    }

    void moveToOpenState(String details) {
    	breakerState = new BreakerOpenState(this);
    	registerEvent(details);
    }

    void moveToHalfOpenState(String details) {
    	breakerState = new BreakerHalfOpenState(this);
    	registerEvent(details);
    }

    void moveToDisabledState(String details) {
    	breakerState = new BreakerDisabledState(this);
    	registerEvent(details);
    }

    void moveToForcedOpenState(String details) {
    	breakerState = new BreakerForcedOpenState(this);
    	registerEvent(details);
    }
    
    private void registerEvent(String details) {
    	breakerStateEventManager.registerEvent(
    			new CircuitBreakerStateChangeEvent(circuitBreakerConfig.getName(),
    			breakerState.getBreakerStateType(),
    			details));    	
    }
    
    boolean isSlowCall(long callDuration) {
    	if(circuitBreakerConfig.getSlowCallDurationThreshold() > 0 &&
    			callDuration >= circuitBreakerConfig.getSlowCallDurationThreshold())
    		return true;
    	return false;
    }
    
    /**
     * This method checks if any ration exceeds threshold. It will call first calculateRates() on the countStats object.
     * This method assumes minimum number of calls is checked before calling it
     * @param countStats the 3 counts
     * @return true if any stat ratio exceeds any threshold
     */
    boolean isExceedFailureOrSlowRateThreshold(CountStats countStats) {
		countStats.calculateRates();
		if(circuitBreakerConfig.getFailureRateThreshold() > 0 &&
				countStats.failureRate >= circuitBreakerConfig.getFailureRateThreshold())
	    	return true;
		if(circuitBreakerConfig.getSlowCallRateThreshold() > 0 && 
				countStats.slowCallRate >= circuitBreakerConfig.getSlowCallRateThreshold())
			return true;
        return false;
    }
}
