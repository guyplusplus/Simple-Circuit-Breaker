package com.geckotechnology.simpleCircuitBreaker;

class BreakerDisabledState implements CircuitBreakerStateInterface {

	BreakerDisabledState(CircuitBreaker circuitBreaker) {
	}
	
	@Override
	public boolean isClosedForThisCall() {
		//always closed
		return true;
	}

	@Override
	public void callFailed(long callDuration) {
	}

	@Override
	public void callSucceeded(long callDuration) {
	}

}
