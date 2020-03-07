package com.geckotechnology.simpleCircuitBreaker;

import java.util.Properties;
import java.util.logging.Logger;

public class CircuitBreakerConfig {
	
    private static final Logger logger = Logger.getLogger(CircuitBreakerConfig.class.getName());
    
	private float failureRateThreshold = 50;
	private float slowCallRateThreshold = 100;
	private long slowCallDurationThreshold = 60000;
	private int permittedNumberOfCallsInHalfOpenState = 10;
	private int slidingWindowSize = 100;
	private int minimumNumberOfCalls = 10;
	private long waitDurationOpenedState = 60000;
	
	/**
	 * Default constructor with default values
	 */
	public CircuitBreakerConfig() {
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
		value = props.getProperty(prefix + "waitDurationOpenedState");
		if(value != null)
			setWaitDurationOpenedState(Long.parseLong(value));
	}
		
	public CircuitBreakerConfig clone() {
		CircuitBreakerConfig clone = new CircuitBreakerConfig();
		clone.slidingWindowSize = slidingWindowSize;
		clone.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
		clone.slowCallDurationThreshold = slowCallDurationThreshold;
		clone.minimumNumberOfCalls = minimumNumberOfCalls;
		clone.failureRateThreshold = failureRateThreshold;
		clone.slowCallRateThreshold = slowCallRateThreshold;
		clone.waitDurationOpenedState = waitDurationOpenedState;
		return clone;
	}
	
	public void logInfoConfigProperties() {
		logger.info("CircuitBreakerConfig:");
		logger.info("\tfailureRateThreshold: " + failureRateThreshold);
		logger.info("\tslowCallRateThreshold: " + slowCallRateThreshold);
		logger.info("\tslowCallDurationThreshold: " + slowCallDurationThreshold);
		logger.info("\tpermittedNumberOfCallsInHalfOpenState: " + permittedNumberOfCallsInHalfOpenState);
		logger.info("\tslidingWindowSize: " + slidingWindowSize);
		logger.info("\tminimumNumberOfCalls: " + minimumNumberOfCalls);
		logger.info("\twaitDurationOpenedState: " + waitDurationOpenedState);
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

	public long getWaitDurationOpenedState() {
		return waitDurationOpenedState;
	}

	public void setWaitDurationOpenedState(long waitDurationOpenedState) {
		if(waitDurationOpenedState <= 0)
			throw new IllegalArgumentException("waitDurationOpenedState must be positive");
		this.waitDurationOpenedState = waitDurationOpenedState;
	}

	public int getPermittedNumberOfCallsInHalfOpenState() {
		return permittedNumberOfCallsInHalfOpenState;
	}

	public void setPermittedNumberOfCallsInHalfOpenState(int permittedNumberOfCallsInHalfOpenState) {
		if(permittedNumberOfCallsInHalfOpenState < 0)
			throw new IllegalArgumentException("permittedNumberOfCallsInHalfOpenState must be positive or null");
		this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
	}

}
