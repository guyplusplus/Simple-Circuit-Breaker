package com.geckotechnology.simpleCircuitBreaker;

public class TestUtils {

	public static void sleep(long ms) {
		if(ms <= 0)
			return;
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isCountStatsEqual(CountStats expectedCS, CountStats actualCS) {
		if(expectedCS.callCount != actualCS.callCount ||
				expectedCS.failureCallCount != actualCS.failureCallCount ||
				expectedCS.slowCallDurationCount != actualCS.slowCallDurationCount) {
    		System.out.println("expected callCount: " + expectedCS.callCount + ", failureCallCount: " + expectedCS.failureCallCount + ", slowCallDurationCount: " + expectedCS.slowCallDurationCount);
    		System.out.println("actual   callCount: " + actualCS.callCount + ", failureCallCount: " + actualCS.failureCallCount + ", slowCallDurationCount: " + actualCS.slowCallDurationCount);
			return false;
		}
		return true;
	}
	
	public static boolean validateAggregatedCountStatsMatches(CircuitBreaker circuitBreaker) {
		synchronized(circuitBreaker) {
			BreakerClosedState breakerClosedState = (BreakerClosedState)circuitBreaker.getBreakerState();
			CountStats agrregatedCountStats = breakerClosedState.calculateAggregatedCountStatsForUnitTest();
			return isCountStatsEqual(agrregatedCountStats, breakerClosedState.getCountStats());
		}
	}
	
	public static boolean validateAggregatedCountStatsMatches(CircuitBreaker circuitBreaker,
			int expectedCallCount, int expectedFailureCallCount, int expectedSlowCallDurationCount) {
		synchronized(circuitBreaker) {
			BreakerClosedState breakerClosedState = (BreakerClosedState)circuitBreaker.getBreakerState();
			//step 1: ensure that sum(all buckets) = current countStats
			CountStats agrregatedCountStats = breakerClosedState.calculateAggregatedCountStatsForUnitTest();
			boolean checkAggregated = isCountStatsEqual(agrregatedCountStats, breakerClosedState.getCountStats());
			if(!checkAggregated) {
				System.out.println("sum(all buckets) is different from current countStats");
				return false;
			}
			//step 2: all passed values match current countStats
			CountStats expectedCountStats = new CountStats();
			expectedCountStats.callCount = expectedCallCount;
			expectedCountStats.failureCallCount = expectedFailureCallCount;
			expectedCountStats.slowCallDurationCount = expectedSlowCallDurationCount;
			return isCountStatsEqual(expectedCountStats, breakerClosedState.getCountStats());
		}
	}
}
