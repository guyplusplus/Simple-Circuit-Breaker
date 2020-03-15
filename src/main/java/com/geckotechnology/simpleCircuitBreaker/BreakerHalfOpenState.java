package com.geckotechnology.simpleCircuitBreaker;

class BreakerHalfOpenState implements BreakerStateInterface {

	private final CircuitBreaker circuitBreaker;
	private CountStats countStats;
    private int permittedNumberOfCallsInHalfOpenStateSoFar = 0;
    private long lastOpenCallTimeLimit = 0;
	
	BreakerHalfOpenState(CircuitBreaker circuitBreaker) {
		this.circuitBreaker = circuitBreaker;
		countStats = new CountStats();
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
    		circuitBreaker.moveToClosedState("MaxDurationOpenInHalfOpenState is over. countStats:{" + countStats.toCountStatsString() + "}");
    		return circuitBreaker.isClosedForThisCall();
		}
		//situation normal, no more call allowed
		return false;
	}

	@Override
	public void callFailed(long callDuration) {
		countStats.failureCallCount++;
		callFailedOrSuccedded(callDuration);
	}

	@Override
	public void callSucceeded(long callDuration) {
		callFailedOrSuccedded(callDuration);
	}

    private void callFailedOrSuccedded(long callDuration) {
    	countStats.callCount++;
    	if(circuitBreaker.isSlowCall(callDuration))
    		countStats.slowCallDurationCount++;
    	if(countStats.callCount < circuitBreaker.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState())
    		return;
    	//callCount reaches permittedNumberOfCallsInHalfOpenState
    	//Time to see if any threshold is exceeded:
    	//  If yes, go to open state
    	//  If no, go to closed state
    	if(circuitBreaker.isExceedFailureOrSlowRateThreshold(countStats))
    		circuitBreaker.moveToOpenState("Reached permittedNumberOfCallsInHalfOpenState and threshold exceeded. countStats:{" +
    				countStats.toCountAndRatioStatsString() + "}");
    	else
    		circuitBreaker.moveToClosedState("Reached permittedNumberOfCallsInHalfOpenState and no threshold exceeded. countStats:{" +
    				countStats.toCountAndRatioStatsString() + "}");
    }
}
