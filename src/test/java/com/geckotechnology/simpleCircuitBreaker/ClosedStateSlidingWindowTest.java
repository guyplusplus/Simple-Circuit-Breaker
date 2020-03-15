package com.geckotechnology.simpleCircuitBreaker;

import static org.junit.Assert.*;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class ClosedStateSlidingWindowTest {

	private static final int THREAD_COUNT = 8;
	private static final int WAIT_TIME_LONG = 4000;
	private static final int WAIT_TIME_SHORT = 1000;
	private static final int WAIT_TIME_VERY_SHORT = 100;
	private static final int LOAD_TEST_DURATION = 20000;
	private static final Random RANDOM = new Random();

	@Test
	public void test() {
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		config.setSlidingWindowSize(5);
		config.setFailureRateThreshold(0);
		config.setSlowCallDurationThreshold(100);
		config.setSlowCallRateThreshold(0);
		CircuitBreaker circuitBreaker = new CircuitBreaker(config);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches(circuitBreaker, 0, 0, 0));
		
		//bucket 0
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches(circuitBreaker, 1, 0, 0));

		//bucket 0
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(120);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches(circuitBreaker, 2, 0, 1));

		TestUtils.sleep(2000);
		
		//bucket 2
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(120);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches(circuitBreaker, 3, 1, 2));
		
		TestUtils.sleep(1000);

		//bucket 3
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(120);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches(circuitBreaker, 4, 2, 3));
		
		TestUtils.sleep(1000);

		//bucket 4
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(2);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches(circuitBreaker, 5, 3, 3));
		
		TestUtils.sleep(1000);

		//bucket 0
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(200);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);

		TestUtils.sleep(1000);

		//bucket 1
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(2);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches(circuitBreaker, 5, 4, 3));

		TestUtils.sleep(5000);

		//bucket 1
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(250);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches(circuitBreaker, 1, 1, 1));

		TestUtils.sleep(5000);

		//bucket 1
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(250);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches(circuitBreaker, 1, 1, 1));

		TestUtils.sleep(4000);

		//bucket 0
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(250);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches(circuitBreaker, 2, 2, 2));

		TestUtils.sleep(6000);

		//bucket 1
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(250);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches(circuitBreaker, 1, 1, 1));
	}
	
	@Test
	public void loadtestLongWait() {
		loadtest(WAIT_TIME_LONG);
	}

	@Test
	public void loadtestShortWait() {
		loadtest(WAIT_TIME_SHORT);
	}

	@Test
	public void loadtestVeryShortWait() {
		loadtest(WAIT_TIME_VERY_SHORT);
	}

	@Test
	public void loadtestNoWait() {
		loadtest(0);
	}

	private void loadtest(final long waitTime) {
		System.out.println("Starting loadtest. Per call waitTime(ms): " + waitTime + ". Estimated time(ms): " + LOAD_TEST_DURATION);
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		config.setSlidingWindowSize(3);
		config.setFailureRateThreshold(0);
		config.setSlowCallRateThreshold(0);
		final CircuitBreaker circuitBreaker = new CircuitBreaker(config);
		final AtomicInteger callCount = new AtomicInteger();
		final AtomicInteger loopCount = new AtomicInteger();
		if(waitTime == 0)
			loopCount.set(LOAD_TEST_DURATION * 500);
		else
			loopCount.set((int)(LOAD_TEST_DURATION / waitTime));
		Thread threads[] = new Thread[THREAD_COUNT];
		for(int t = 0; t<THREAD_COUNT; t++) {
			threads[t] = new Thread() {
				public void run() {
					for(int i = 0; i<loopCount.get(); i++) {
						assertTrue(circuitBreaker.isClosedForThisCall());
						long wait = 0;
						if(waitTime > 0)
							wait = RANDOM.nextInt((int)waitTime);
						assertTrue(TestUtils.validateAggregatedCountStatsMatches(circuitBreaker));
						callCount.incrementAndGet();
						TestUtils.sleep(wait);
						circuitBreaker.callSucceeded(wait);
					}
				}
			};
			threads[t].start();
		}
		for(int t = 0; t<THREAD_COUNT; t++) {
			try {
				threads[t].join();
			} catch (InterruptedException e) {
				fail("InterruptedException catched: " + e);
			}
		}
		assertEquals(callCount.get(), THREAD_COUNT * loopCount.get());
		System.out.println("Load test done");
	}

}
