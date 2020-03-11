package com.geckotechnology.simpleCircuitBreaker;

class BreakerClosedState implements BreakerStateInterface {

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
		callFailedOrSuccedded(callDuration, true);
	}

	@Override
	public void callSucceeded(long callDuration) {
		callFailedOrSuccedded(callDuration, false);
	}

    private void callFailedOrSuccedded(long callDuration, boolean isFailureCall) {
    	boolean isSlowCall = circuitBreaker.isSlowCall(callDuration);
    	int nowInSec = (int)(System.currentTimeMillis() / 1000);
    	if(lastCallTimestampInSec == nowInSec) {
    		//just add to current bucket, we are still in the same second
    		addToLastCallTimestampInSecBucket(isFailureCall, isSlowCall);
    	}
    	else {
    		//compared to lastCallTimestampInSec, we moved next second or more
    		if((nowInSec - lastCallTimestampInSec) >= slidingWindowSize) {
    			//compared to lastCallTimestampInSec, there is more than slidingWindowSize difference
    			//then clear all buckets
    			clearAllBuckets();
    		}
    		else {
    			//only few buckets need to be cleared
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
    }
    
    private void addToLastCallTimestampInSecBucket(boolean isFailureCall, boolean isSlowCall) {
    	callCount++;
    	if(isFailureCall)
    		failureCallCount++;
    	if(isSlowCall)
    		slowCallDurationCount++;
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
    
    
    /**
     * Method used during unit test to validate array and sum are correctly aligned.
     * Return true if all ok, false otherwise
     * Method is not synchronized
     */
    boolean testCheckSumForUnitTest() {
        int checkCallCount = 0;
        int checkFailureCallCount = 0;
        int checkSlowCallDurationCount = 0;
    	for(int i = 0; i<slidingWindowSize; i++) {
    		checkCallCount += callCountBuckets[i];
    		checkFailureCallCount += failureCallCountBuckets[i];
    		checkSlowCallDurationCount += slowCallDurationCountBuckets[i];
    	}
    	if(checkCallCount != callCount ||
    			checkFailureCallCount != failureCallCount ||
    			checkSlowCallDurationCount != slowCallDurationCount) {
    		System.out.println("actual     callCount: " + callCount + ", failureCallCount: " + failureCallCount + ", slowCallDurationCount: " + slowCallDurationCount);
    		System.out.println("calculated callCount: " + checkCallCount + ", failureCallCount: " + checkFailureCallCount + ", slowCallDurationCount: " + checkSlowCallDurationCount);
    		return false;
    	}
    	return true;
    }
   /**
     * Method used during unit test to validate array and sum are correctly aligned.
     * Return true if all ok, false otherwise
     * Method is not synchronized
     */
    boolean testCheckSumForUnitTest(int expectedCallCount, int expectedFailureCallCount, int expectedSlowCallDurationCount) {
    	boolean test1 = testCheckSumForUnitTest();
    	if(!test1)
    		return false;
    	if(callCount != expectedCallCount ||
    			failureCallCount != expectedFailureCallCount ||
    			slowCallDurationCount != expectedSlowCallDurationCount) {
    		System.out.println("actual   callCount: " + callCount + ", failureCallCount: " + failureCallCount + ", slowCallDurationCount: " + slowCallDurationCount);
    		System.out.println("expected callCount: " + expectedCallCount + ", failureCallCount: " + expectedFailureCallCount + ", slowCallDurationCount: " + expectedSlowCallDurationCount);
    		return false;
    	}
    	return true;
    }
}
