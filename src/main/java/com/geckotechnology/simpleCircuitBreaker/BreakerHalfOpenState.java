package com.geckotechnology.simpleCircuitBreaker;

class BreakerHalfOpenState implements CircuitBreakerStateInterface {

	private final CircuitBreaker circuitBreaker;
    private int callCount = 0;
    private int failureCallCount = 0;
    private int slowCallDurationCount = 0;
    private int permittedNumberOfCallsInHalfOpenStateSoFar = 0;
	
	BreakerHalfOpenState(CircuitBreaker circuitBreaker) {
		this.circuitBreaker = circuitBreaker;
	}
	
	@Override
	public boolean isClosedForThisCall() {
		if(permittedNumberOfCallsInHalfOpenStateSoFar < circuitBreaker.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState()) {
			permittedNumberOfCallsInHalfOpenStateSoFar++;
			return true;
		}
		return false;
		//@TODO add logic so that half open state is not for ever
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
    	//time to see if any threshold is exceeded. If yes, go to open state. If no, go to closed state
    	if(circuitBreaker.isExceedFailureOrSlowRateThreshold(callCount, failureCallCount, slowCallDurationCount))
    		circuitBreaker.moveToOpenState();
    	else
    		circuitBreaker.moveToClosedState();
    }
}
