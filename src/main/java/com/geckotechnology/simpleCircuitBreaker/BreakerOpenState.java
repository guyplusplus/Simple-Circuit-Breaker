package com.geckotechnology.simpleCircuitBreaker;

class BreakerOpenState implements BreakerStateInterface {

	private final CircuitBreaker circuitBreaker;
	private long openStateEndTimestamp;
	
	BreakerOpenState(CircuitBreaker circuitBreaker) {
		this.circuitBreaker = circuitBreaker;
		openStateEndTimestamp = System.currentTimeMillis() + circuitBreaker.getCircuitBreakerConfig().getWaitDurationInOpenState();
	}
	
	@Override
	public BreakerStateType getBreakerStateType() {
		return BreakerStateType.OPEN;
	}
	
	@Override
	public boolean isClosedForThisCall() {
		//check if need to move to half-open
		if(System.currentTimeMillis() >= openStateEndTimestamp) {
			if(circuitBreaker.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState() == 0)
				circuitBreaker.moveToClosedState();
			else	
				circuitBreaker.moveToHalfOpenState();
			return circuitBreaker.isClosedForThisCall();
		}
		//no need. Remain in open state
		return false;
	}

	@Override
	public void callFailed(long callDuration) {
	}

	@Override
	public void callSucceeded(long callDuration) {
	}

}
