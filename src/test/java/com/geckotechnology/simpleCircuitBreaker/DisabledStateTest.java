package com.geckotechnology.simpleCircuitBreaker;

import static org.junit.Assert.*;

import org.junit.Test;

public class DisabledStateTest {
	
	private static final int LOOP_COUNT = 100;
	private static final long SLEEP_TIME = 200;
	private static final int THREAD_COUNT = 5;

	@Test
	public void test() {
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		config.setSlidingWindowSize(0);
		final CircuitBreaker circuitBreaker = new CircuitBreaker(config);
		Thread threads[] = new Thread[THREAD_COUNT];
		for(int t = 0; t<THREAD_COUNT; t++) {
			threads[t] = new Thread() {
				public void run() {
					for(int i = 0; i<LOOP_COUNT; i++) {
						assertTrue(circuitBreaker.getBreakerState() instanceof BreakerDisabledState);
						if(circuitBreaker.isClosedForThisCall()) {
							assertTrue(circuitBreaker.getBreakerState() instanceof BreakerDisabledState);
							TestUtils.sleep(SLEEP_TIME);
							circuitBreaker.callSucceeded(SLEEP_TIME);
						}
						else
							fail("circuitBreaker should always be closed");
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
