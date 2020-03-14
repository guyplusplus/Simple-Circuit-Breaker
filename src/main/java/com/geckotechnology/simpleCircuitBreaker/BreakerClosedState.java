package com.geckotechnology.simpleCircuitBreaker;

class BreakerClosedState implements BreakerStateInterface {

	private final CircuitBreaker circuitBreaker;
	private final int slidingWindowSize;
	private CountStats countStats;
    private int callCountBuckets[];
    private int failureCallCountBuckets[];
    private int slowCallDurationCountBuckets[];
    private int lastCallTimestampInSec = 0;
	
	BreakerClosedState(CircuitBreaker circuitBreaker) {
		this.circuitBreaker = circuitBreaker;
		countStats = new CountStats();
		slidingWindowSize = circuitBreaker.getCircuitBreakerConfig().getSlidingWindowSize();
		clearAllBuckets();
	}
	
	@Override
	public BreakerStateType getBreakerStateType() {
		return BreakerStateType.CLOSED;
	}
	
	private void clearAllBuckets() {
		callCountBuckets = new int[slidingWindowSize];
		failureCallCountBuckets = new int[slidingWindowSize];
		slowCallDurationCountBuckets = new int[slidingWindowSize];
		countStats = new CountStats();
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
    	if(countStats.callCount >= circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls()) {
    		if(circuitBreaker.isExceedFailureOrSlowRateThreshold(countStats)) {
    			circuitBreaker.moveToOpenState("Threshold exceeded. countStats:{" + countStats.toCountAndRatioStatsString() + "}");
    		}
    	}
    }
    
    private void addToLastCallTimestampInSecBucket(boolean isFailureCall, boolean isSlowCall) {
    	countStats.callCount++;
    	if(isFailureCall)
    		countStats.failureCallCount++;
    	if(isSlowCall)
    		countStats.slowCallDurationCount++;
    	int lastCallTimestampBucket = lastCallTimestampInSec % slidingWindowSize;
    	callCountBuckets[lastCallTimestampBucket]++;
    	if(isFailureCall)
    		failureCallCountBuckets[lastCallTimestampBucket]++;
    	if(isSlowCall)
    		slowCallDurationCountBuckets[lastCallTimestampBucket]++;	
    }
    
    private void clearBucket(int timeInSec) {
    	int bucket = timeInSec % slidingWindowSize;
    	countStats.callCount -= callCountBuckets[bucket];
		callCountBuckets[bucket] = 0;
		countStats.failureCallCount -= failureCallCountBuckets[bucket];
		failureCallCountBuckets[bucket] = 0;
		countStats.slowCallDurationCount -= slowCallDurationCountBuckets[bucket];
		slowCallDurationCountBuckets[bucket] = 0;
    }
    
    CountStats getCountStats() {
    	return countStats;
    }
    
    CountStats calculateAggregatedCountStatsForUnitTest() {
    	CountStats aggregatedCountStats = new CountStats();
    	for(int i = 0; i<slidingWindowSize; i++) {
    		aggregatedCountStats.callCount += callCountBuckets[i];
    		aggregatedCountStats.failureCallCount += failureCallCountBuckets[i];
    		aggregatedCountStats.slowCallDurationCount += slowCallDurationCountBuckets[i];
    	}   	
    	return aggregatedCountStats;
    }
}
