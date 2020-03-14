package com.geckotechnology.simpleCircuitBreaker;

interface BreakerStateInterface {
	boolean isClosedForThisCall();
    void callFailed(long callDuration);
    void callSucceeded(long callDuration);
    BreakerStateType getBreakerStateType();
}