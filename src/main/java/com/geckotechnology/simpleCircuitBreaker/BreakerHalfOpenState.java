package com.geckotechnology.simpleCircuitBreaker;

class BreakerHalfOpenState implements BreakerStateInterface {

	private final CircuitBreaker circuitBreaker;
    private int callCount = 0;
    private int failureCallCount = 0;
    private int slowCallDurationCount = 0;
    private int permittedNumberOfCallsInHalfOpenStateSoFar = 0;
    private long lastOpenCallTimeLimit = 0;
	
	BreakerHalfOpenState(CircuitBreaker circuitBreaker) {
		this.circuitBreaker = circuitBreaker;
	}
	
	@Override
	public BreakerStateType getBreakerStateType() {
		return BreakerStateType.HALF_OPEN;
	}
	
	@Override
	public boolean isClosedForThisCall() {
		//ensure permittedNumberOfCallsInHalfOpenState calls are executed, by returning true
		if(permittedNumberOfCallsInHalfOpenStateSoFar < circuitBreaker.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState()) {
			permittedNumberOfCallsInHalfOpenStateSoFar++;
			return true;
		}
		//no more calls allowed
		//if maxDurationOpenInHalfOpenState is 0, this situation can last forever. Return false 
		if(circuitBreaker.getCircuitBreakerConfig().getMaxDurationOpenInHalfOpenState() == 0)
			return false;
		//There is a time  limit. Check if it is set. If not, it is the first call. Return false
		if(lastOpenCallTimeLimit == 0) {
			lastOpenCallTimeLimit = System.currentTimeMillis() + circuitBreaker.getCircuitBreakerConfig().getMaxDurationOpenInHalfOpenState();
			return false;
		}
		if(System.currentTimeMillis() >= lastOpenCallTimeLimit) {
			//we are beyond maxDurationOpenInHalfOpenState. Need to go back to CLOSED state
    		circuitBreaker.moveToClosedState("maxDurationOpenInHalfOpenState is reached. " + 
    				circuitBreaker.getExpressiveStatsAsReason(callCount, failureCallCount, slowCallDurationCount));
    		return circuitBreaker.isClosedForThisCall();
		}
		//situation normal, no more call allowed
		return false;
	}

	@Override
	public void callFailed(long callDuration) {
		failureCallCount++;
		callFailedOrSuccedded(callDuration);
	}

	@Override
	public void callSucceeded(long callDuration) {
		callFailedOrSuccedded(callDuration);
	}

    private void callFailedOrSuccedded(long callDuration) {
    	callCount++;
    	if(circuitBreaker.isSlowCall(callDuration))
    		slowCallDurationCount++;
    	if(callCount < circuitBreaker.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState())
    		return;
    	//callCount reaches permittedNumberOfCallsInHalfOpenState
    	//Time to see if any threshold is exceeded:
    	//  If yes, go to open state
    	//  If no, go to closed state
    	if(circuitBreaker.isExceedFailureOrSlowRateThreshold(callCount, failureCallCount, slowCallDurationCount))
    		circuitBreaker.moveToOpenState("Threshold exceeded. " +
					circuitBreaker.getExpressiveStatsAsReason(callCount, failureCallCount, slowCallDurationCount));
    	else {
    		circuitBreaker.moveToClosedState("Reached permittedNumberOfCallsInHalfOpenState and no threshold exceeded. " +
    				circuitBreaker.getExpressiveStatsAsReason(callCount, failureCallCount, slowCallDurationCount));
    	}
    }
}
