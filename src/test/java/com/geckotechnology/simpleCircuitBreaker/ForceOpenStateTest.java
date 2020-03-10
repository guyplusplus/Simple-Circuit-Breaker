package com.geckotechnology.simpleCircuitBreaker;

import static org.junit.Assert.*;

import org.junit.Test;

public class ForceOpenStateTest {
	
	private static final int LOOP_COUNT = 100;
	private static final long SLEEP_TIME = 200;
	private static final int THREAD_COUNT = 5;

	@Test
	public void test() {
		System.out.println("Starting ForceOpenStateTest. Shall take " + (LOOP_COUNT * SLEEP_TIME) + " ms");
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		config.setSlidingWindowSize(-1);
		final CircuitBreaker circuitBreaker = new CircuitBreaker(config);
		Thread threads[] = new Thread[THREAD_COUNT];
		for(int t = 0; t<THREAD_COUNT; t++) {
			threads[t] = new Thread() {
				public void run() {
					for(int i = 0; i<LOOP_COUNT; i++) {
						assertTrue(circuitBreaker.getBreakerState() instanceof BreakerForcedOpenState);
						assertFalse(circuitBreaker.isClosedForThisCall());
						assertTrue(circuitBreaker.getBreakerState() instanceof BreakerForcedOpenState);
						TestUtils.sleep(SLEEP_TIME);
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
