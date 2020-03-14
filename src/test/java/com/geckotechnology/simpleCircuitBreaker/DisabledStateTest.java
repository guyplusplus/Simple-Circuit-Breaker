package com.geckotechnology.simpleCircuitBreaker;

import static org.junit.Assert.*;

import org.junit.Test;

public class DisabledStateTest {
	
	private static final int LOOP_COUNT = 100;
	private static final long SLEEP_TIME = 200;
	private static final int THREAD_COUNT = 5;

	@Test
	public void test() {
		System.out.println("Starting DisabledStateTest. Shall take " + (LOOP_COUNT * SLEEP_TIME) + " ms");
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		config.setSlidingWindowSize(0);
		final CircuitBreaker circuitBreaker = new CircuitBreaker(config);
		Thread threads[] = new Thread[THREAD_COUNT];
		for(int t = 0; t<THREAD_COUNT; t++) {
			threads[t] = new Thread() {
				public void run() {
					for(int i = 0; i<LOOP_COUNT; i++) {
						assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.DISABLED);
						assertTrue(circuitBreaker.isClosedForThisCall());
						assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.DISABLED);
						TestUtils.sleep(SLEEP_TIME);
						circuitBreaker.callSucceeded(SLEEP_TIME);
						assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.DISABLED);
					}					
				}
			};
			threads[t].start();
		}
		for(int t = 0; t<THREAD_COUNT; t++)
			try {
				threads[t].join();
			} catch (InterruptedException e) {
				fail("Exception captured " + e);
			}
	}
}
