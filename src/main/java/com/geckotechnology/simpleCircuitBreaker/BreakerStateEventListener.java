package com.geckotechnology.simpleCircuitBreaker;

public interface BreakerStateEventListener {
	public void onCircuitBreakerStateChangeEvent(CircuitBreakerStateChangeEvent e);
}
