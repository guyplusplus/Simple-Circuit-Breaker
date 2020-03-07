package com.geckotechnology.simpleCircuitBreaker;

class BreakerClosedState implements CircuitBreakerStateInterface {

	private final CircuitBreaker circuitBreaker;
	private final int slidingWindowSize;
    private int callCount = 0;
    private int failureCallCount = 0;
    private int slowCallDurationCount = 0;
    private int callCountBuckets[];
    private int failureCallCountBuckets[];
    private int slowCallDurationCountBuckets[];
    private int lastCallTimestampInSec = 0;
	
	BreakerClosedState(CircuitBreaker circuitBreaker) {
		this.circuitBreaker = circuitBreaker;
		slidingWindowSize = circuitBreaker.getCircuitBreakerConfig().getSlidingWindowSize();
		clearAllBuckets();
	}
	
	private void clearAllBuckets() {
		callCountBuckets = new int[slidingWindowSize];
		failureCallCountBuckets = new int[slidingWindowSize];
		slowCallDurationCountBuckets = new int[slidingWindowSize];
	    callCount = 0;
	    failureCallCount = 0;
	    slowCallDurationCount = 0;
    	lastCallTimestampInSec = (int)(System.currentTimeMillis() / 1000);
	}
	
	@Override
	public boolean isClosedForThisCall() {
		return true;
	}

	@Override
	public void callFailed(long callDuration) {
		failureCallCount++;
		callFailedOrSuccedded(callDuration, true);
	}

	@Override
	public void callSucceeded(long callDuration) {
		callFailedOrSuccedded(callDuration, false);
	}

    private void callFailedOrSuccedded(long callDuration, boolean isFailureCall) {
    	callCount++;
    	boolean isSlowCall = false;
    	if(circuitBreaker.isSlowCall(callDuration)) {
    		slowCallDurationCount++;
    		isSlowCall = true;
    	}
    	long now = System.currentTimeMillis();
    	int nowInSec = (int)(now / 1000);
    	if(lastCallTimestampInSec == nowInSec) {
    		//just add to last bucket
    		addToLastCallTimestampInSecBucket(isFailureCall, isSlowCall);
    	}
    	else {
    		if((nowInSec - lastCallTimestampInSec) >= slidingWindowSize) {
    			//clear all buckets
    			clearAllBuckets();
    		}
    		else {
    			for(int timeInSec = lastCallTimestampInSec + 1; timeInSec <= nowInSec; timeInSec++)
    				clearBucket(timeInSec);
    			lastCallTimestampInSec = nowInSec;
    		}
			addToLastCallTimestampInSecBucket(isFailureCall, isSlowCall);
    	}
    	//check now if we need to move to open state
    	if(callCount >= circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls()) {
    		if(circuitBreaker.isExceedFailureOrSlowRateThreshold(callCount, failureCallCount, slowCallDurationCount)) {
    			circuitBreaker.moveToOpenState();
    		}
    	}
    	System.out.println("callCount, failureCallCount, slowCallDurationCount=" + callCount + "," + failureCallCount + "," + slowCallDurationCount);
    }
    
    private void addToLastCallTimestampInSecBucket(boolean isFailureCall, boolean isSlowCall) {
    	int lastCallTimestampBucket = lastCallTimestampInSec % slidingWindowSize;
    	callCountBuckets[lastCallTimestampBucket]++;
    	if(isFailureCall)
    		failureCallCountBuckets[lastCallTimestampBucket]++;
    	if(isSlowCall)
    		slowCallDurationCountBuckets[lastCallTimestampBucket]++;	
    }
    
    private void clearBucket(int timeInSec) {
    	int bucket = timeInSec % slidingWindowSize;
    	callCount -= callCountBuckets[bucket];
		callCountBuckets[bucket] = 0;
		failureCallCount -= failureCallCountBuckets[bucket];
		failureCallCountBuckets[bucket] = 0;
		slowCallDurationCount -= slowCallDurationCountBuckets[bucket];
		slowCallDurationCountBuckets[bucket] = 0;
    }
}
