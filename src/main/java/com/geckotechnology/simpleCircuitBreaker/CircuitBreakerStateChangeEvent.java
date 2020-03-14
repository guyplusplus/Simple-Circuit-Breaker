package com.geckotechnology.simpleCircuitBreaker;

public class CircuitBreakerStateChangeEvent {
	
	private String circuitBreakerName;
	private long creationTimestamp = System.currentTimeMillis();
	private BreakerStateType newBreakerStateType;
	private String cause;
	
	CircuitBreakerStateChangeEvent(String circuitBreakerName, BreakerStateType newBreakerStateType, String cause) {
		this.circuitBreakerName = circuitBreakerName;
		this.newBreakerStateType = newBreakerStateType;
		this.cause = cause;
	}
	
	public String getCircuitBreakerName() {
		return circuitBreakerName;
	}
	
	public long getCreationTime() {
		return creationTimestamp;
	}
	
	public BreakerStateType getNewBreakerStateType() {
		return newBreakerStateType;
	}
	
	public String getCause() {
		return cause;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("CircuitBreakerStateChangeEvent:{");
		sb.append("circuitBreakerName:").append(circuitBreakerName);
		sb.append(", ").append("newBreakerStateType:").append(newBreakerStateType);
		sb.append(", ").append("cause:\"").append(cause).append("\"");
		sb.append("}");
		return sb.toString();
	}
}
