package com.geckotechnology.simpleCircuitBreaker;

import java.util.Properties;

public class CircuitBreakerConfig {
	
	private static final String name_DEFAULT = "";
	private static final float FAILURE_RATE_THRESHOLD_DEFAULT = 50;
	private static final float SLOW_CALL_RATE_THRESHOLD_DEFAULT = 100;
	private static final long SLOW_CALL_DURATION_THRESHOLD_DEFAULT = 60000;
	private static final int PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE_DEFAULT = 10;
	private static final int SLIDING_WINDOW_SIZE_DEFAULT = 100;
	private static final int MINIMUM_NUMBER_OF_CALLS_DEFAULT = 10;
	private static final long WAIT_DURATION_IN_OPEN_STATE_DEFAULT = 60000;
	private static final long MAX_DURATION_OPE_IN_HALF_OPEN_STATE_DEFAULT = 120000;
	
    private String name = name_DEFAULT;
    private float failureRateThreshold = FAILURE_RATE_THRESHOLD_DEFAULT;
	private float slowCallRateThreshold = SLOW_CALL_RATE_THRESHOLD_DEFAULT;
	private long slowCallDurationThreshold = SLOW_CALL_DURATION_THRESHOLD_DEFAULT;
	private int permittedNumberOfCallsInHalfOpenState = PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE_DEFAULT;
	private int slidingWindowSize = SLIDING_WINDOW_SIZE_DEFAULT;
	private int minimumNumberOfCalls = MINIMUM_NUMBER_OF_CALLS_DEFAULT;
	private long waitDurationInOpenState = 	WAIT_DURATION_IN_OPEN_STATE_DEFAULT;
	private long maxDurationOpenInHalfOpenState = MAX_DURATION_OPE_IN_HALF_OPEN_STATE_DEFAULT;

	/**
	 * Default constructor with default values
	 */
	public CircuitBreakerConfig() {
	}
	
	/**
	 * Constructor where prefix is empty
	 */
	public CircuitBreakerConfig(Properties props) {
		this("", props);
	}
	
	/**
	 * Constructor where props contains some key/value pairs to override default values. Prefix can be provided
	 * so that one properties object can contain multiple breaker configurations. So for example if prefix is "breaker1.",
	 * property "breaker1.resetInterval" will be checked.
	 * @param prefix prefix for each properties. Null or "" for no prefix
	 * @param props
	 */
	public CircuitBreakerConfig(String prefix, Properties props) {
		if(prefix == null)
			prefix = "";
		String value = null;
		value = props.getProperty(prefix + "name");
		if(value != null)
			setName(value);
		value = props.getProperty(prefix + "failureRateThreshold");
		if(value != null)
			setFailureRateThreshold(Float.parseFloat(value));
		value = props.getProperty(prefix + "slowCallRateThreshold");
		if(value != null)
			setSlowCallRateThreshold(Float.parseFloat(value));
		value = props.getProperty(prefix + "slowCallDurationThreshold");
		if(value != null)
			setSlowCallDurationThreshold(Long.parseLong(value));
		value = props.getProperty(prefix + "permittedNumberOfCallsInHalfOpenState");
		if(value != null)
			setPermittedNumberOfCallsInHalfOpenState(Integer.parseInt(value));
		value = props.getProperty(prefix + "slidingWindowSize");
		if(value != null)
			setSlidingWindowSize(Integer.parseInt(value));
		value = props.getProperty(prefix + "minimumNumberOfCalls");
		if(value != null)
			setMinimumNumberOfCalls(Integer.parseInt(value));
		value = props.getProperty(prefix + "waitDurationInOpenState");
		if(value != null)
			setWaitDurationInOpenState(Long.parseLong(value));
		value = props.getProperty(prefix + "maxDurationOpenInHalfOpenState");
		if(value != null)
			setMaxDurationOpenInHalfOpenState(Long.parseLong(value));
	}
		
	public CircuitBreakerConfig clone() {
		CircuitBreakerConfig clone = new CircuitBreakerConfig();
		clone.setName(name);
		clone.slidingWindowSize = slidingWindowSize;
		clone.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
		clone.slowCallDurationThreshold = slowCallDurationThreshold;
		clone.minimumNumberOfCalls = minimumNumberOfCalls;
		clone.failureRateThreshold = failureRateThreshold;
		clone.slowCallRateThreshold = slowCallRateThreshold;
		clone.waitDurationInOpenState = waitDurationInOpenState;
		clone.maxDurationOpenInHalfOpenState = maxDurationOpenInHalfOpenState;
		return clone;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("name:").append(name);
		sb.append(", ").append("failureRateThreshold:").append(failureRateThreshold);
		sb.append(", ").append("slowCallRateThreshold:").append(slowCallRateThreshold);
		sb.append(", ").append("slowCallDurationThreshold:").append(slowCallDurationThreshold);
		sb.append(", ").append("permittedNumberOfCallsInHalfOpenState:").append(permittedNumberOfCallsInHalfOpenState);
		sb.append(", ").append("slidingWindowSize:").append(slidingWindowSize);
		sb.append(", ").append("minimumNumberOfCalls:").append(minimumNumberOfCalls);
		sb.append(", ").append("waitDurationInOpenState:").append(waitDurationInOpenState);
		sb.append(", ").append("maxDurationOpenInHalfOpenState:").append(maxDurationOpenInHalfOpenState);
		return sb.toString();
	}
	
	public int getSlidingWindowSize() {
		return slidingWindowSize;
	}
	
	public void setSlidingWindowSize(int slidingWindowSize) {
		if(slidingWindowSize < 0 && slidingWindowSize != -1)
			throw new IllegalArgumentException("slidingWindow must be positive or 0 (closed state) or -1 (open state)");
		this.slidingWindowSize = slidingWindowSize;
	}
	
	public long getSlowCallDurationThreshold() {
		return slowCallDurationThreshold;
	}

	public void setSlowCallDurationThreshold(long slowCallDurationThreshold) {
		if(slowCallDurationThreshold < 0)
			throw new IllegalArgumentException("slowCallDurationThreshold must be positive or null");
		this.slowCallDurationThreshold = slowCallDurationThreshold;
	}

	public int getMinimumNumberOfCalls() {
		return minimumNumberOfCalls;
	}

	public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
		if(minimumNumberOfCalls <= 0)
			throw new IllegalArgumentException("minimumNumberOfCalls must be positive");
		this.minimumNumberOfCalls = minimumNumberOfCalls;
	}

	public float getFailureRateThreshold() {
		return failureRateThreshold;
	}

	public void setFailureRateThreshold(float failureRateThreshold) {
		if(failureRateThreshold < 0 || failureRateThreshold > 100)
			throw new IllegalArgumentException("setFailureRateThreshold must be between 0 and 100 (included)");
		this.failureRateThreshold = failureRateThreshold;
	}

	public float getSlowCallRateThreshold() {
		return slowCallRateThreshold;
	}

	public void setSlowCallRateThreshold(float slowCallRateThreshold) {
		if(slowCallRateThreshold < 0 || slowCallRateThreshold > 100)
			throw new IllegalArgumentException("slowCallRateThreshold must be between 0 and 100 (included)");
		this.slowCallRateThreshold = slowCallRateThreshold;
	}

	public long getWaitDurationInOpenState() {
		return waitDurationInOpenState;
	}

	public void setWaitDurationInOpenState(long waitDurationInOpenState) {
		if(waitDurationInOpenState <= 0)
			throw new IllegalArgumentException("waitDurationOpenedState must be positive");
		this.waitDurationInOpenState = waitDurationInOpenState;
	}

	public int getPermittedNumberOfCallsInHalfOpenState() {
		return permittedNumberOfCallsInHalfOpenState;
	}

	public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
		if(permittedNumberOfCallsInHalfOpenState < 0)
			throw new IllegalArgumentException("permittedNumberOfCallsInHalfOpenState must be positive or null");
		this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
	}

	public long getMaxDurationOpenInHalfOpenState() {
		return maxDurationOpenInHalfOpenState;
	}

	public void setMaxDurationOpenInHalfOpenState(long maxDurationOpenInHalfOpenState) {
		if(maxDurationOpenInHalfOpenState < 0)
			throw new IllegalArgumentException("maxDurationOpenInHalfOpenState must be positive or null");
		this.maxDurationOpenInHalfOpenState = maxDurationOpenInHalfOpenState;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
