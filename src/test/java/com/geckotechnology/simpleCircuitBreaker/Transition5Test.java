package com.geckotechnology.simpleCircuitBreaker;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class Transition5Test {
	
	private AtomicInteger eventCount = new AtomicInteger();
	private static final LinkedList<BreakerStateType> EXPECTED_STATES = new LinkedList<BreakerStateType>(Arrays.asList(
			BreakerStateType.OPEN,
			BreakerStateType.HALF_OPEN,
			BreakerStateType.CLOSED
			));

	/**
	 * Test transitions : closed -> open -> half-open -> closed where no tx during open (wait longer than aitDurationInOpenState + axDurationOpenInHalfOpenState) 
	 * Event type: call slow
	 */
	@Test
	public void test() {
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		config.setSlidingWindowSize(5);
		config.setFailureRateThreshold(0);
		config.setWaitDurationInOpenState(4000);
		config.setMinimumNumberOfCalls(3);
		config.setPermittedNumberOfCallsInHalfOpenState(2);
		config.setSlowCallDurationThreshold(10);
		config.setSlowCallRateThreshold(70);
		config.setMaxDurationOpenInHalfOpenState(5000);
		CircuitBreaker circuitBreaker = new CircuitBreaker(config);
		circuitBreaker.getBreakerStateEventManager().addBreakerStateEventListener(new BreakerStateEventListener() {
			@Override
			public void onCircuitBreakerStateChangeEvent(CircuitBreakerStateChangeEvent event) {
				eventCount.incrementAndGet();
				System.out.println("CircuitBreaker state changed. " + event);
			}
		});
		circuitBreaker.getBreakerStateEventManager().addBreakerStateEventListener(new BreakerStateEventListener() {
			@Override
			public void onCircuitBreakerStateChangeEvent(CircuitBreakerStateChangeEvent event) {
				eventCount.incrementAndGet();
				assertEquals(event.getNewBreakerStateType(), EXPECTED_STATES.pollFirst());
			}
		});
		
		//time 0: CLOSED Failed
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(1000);

		//time 1: CLOSED Failed
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(6000);
		
		//time 8: CLOSED Failed
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(1000);

		//time 9: CLOSED Failed
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(1000);

		//time 10: CLOSED Success
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(9);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(1000);

		//time 11: CLOSED Fail, trip as 3F2S (3 fail, 1 success, ratio 75% > 70%)
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		TestUtils.sleep(11000);

		//time 22: HALF-OPEN since no tx during OPEN stage
		assertTrue(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		circuitBreaker.callSucceeded(9);
		TestUtils.sleep(1000);

		//time 23: HALF-OPEN moving to CLOSED Failed, 2 calls where failure = 50% < 70%
		assertTrue(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(1000);
	
		//time 24: CLOSED
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		
		//the end. The right number of events was received
		TestUtils.sleep(1000);
		assertEquals(eventCount.get(), 2*3);
		assertEquals(EXPECTED_STATES.size(), 0);
	}
}
