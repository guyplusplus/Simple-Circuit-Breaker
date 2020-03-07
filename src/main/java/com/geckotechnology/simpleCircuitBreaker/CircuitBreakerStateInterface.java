package com.geckotechnology.simpleCircuitBreaker;

interface CircuitBreakerStateInterface {
	boolean isClosedForThisCall();
    void callFailed(long callDuration);
    void callSucceeded(long callDuration);
}