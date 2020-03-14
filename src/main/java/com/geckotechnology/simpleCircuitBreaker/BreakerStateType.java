package com.geckotechnology.simpleCircuitBreaker;

public enum BreakerStateType {
	CLOSED,
	DISABLED,
	FORCED_OPEN,
	HALF_OPEN,
	OPEN;
}
