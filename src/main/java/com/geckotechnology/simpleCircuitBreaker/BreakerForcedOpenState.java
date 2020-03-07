package com.geckotechnology.simpleCircuitBreaker;

class BreakerForcedOpenState implements CircuitBreakerStateInterface {

	BreakerForcedOpenState(CircuitBreaker circuitBreaker) {
	}
	
	@Override
	public boolean isClosedForThisCall() {
		//always opened
		return false;
	}

	@Override
	public void callFailed(long callDuration) {
	}

	@Override
	public void callSucceeded(long callDuration) {
	}

}
