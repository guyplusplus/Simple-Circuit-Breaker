package com.geckotechnology.simpleCircuitBreaker;

class BreakerDisabledState implements BreakerStateInterface {

	BreakerDisabledState(CircuitBreaker circuitBreaker) {
	}
	
	@Override
	public BreakerStateType getBreakerStateType() {
		return BreakerStateType.DISABLED;
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
