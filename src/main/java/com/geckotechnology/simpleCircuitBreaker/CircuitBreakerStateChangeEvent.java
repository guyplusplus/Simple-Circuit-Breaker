package com.geckotechnology.simpleCircuitBreaker;

public class CircuitBreakerStateChangeEvent {
	
	private String circuitBreakerName;
	private long creationTimestamp = System.currentTimeMillis();
	private BreakerStateType newBreakerStateType;
	private String details;
	
	CircuitBreakerStateChangeEvent(String circuitBreakerName, BreakerStateType newBreakerStateType, String details) {
		this.circuitBreakerName = circuitBreakerName;
		this.newBreakerStateType = newBreakerStateType;
		this.details = details;
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
	
	public String getDetails() {
		return details;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("circuitBreakerName:").append(circuitBreakerName);
		sb.append(", ").append("newBreakerStateType:").append(newBreakerStateType);
		sb.append(", ").append("creationTimestamp:").append(creationTimestamp);
		sb.append(", ").append("details:\"").append(details).append("\"");
		return sb.toString();
	}
}
