package com.geckotechnology.simpleCircuitBreaker;

import static org.junit.Assert.*;

import org.junit.Test;

public class Transition3Test {
	
	private CircuitBreakerStateChangeEvent lastEvent;

	/**
	 * Test transitions : closed -> open -> half-open for ever with maxDurationOpenInHalfOpenState 0
	 * Event type: call slow
	 */
	@Test
	public void test1() {
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		config.setSlidingWindowSize(5);
		config.setFailureRateThreshold(0);
		config.setWaitDurationInOpenState(4000);
		config.setMinimumNumberOfCalls(3);
		config.setPermittedNumberOfCallsInHalfOpenState(2);
		config.setSlowCallDurationThreshold(10);
		config.setSlowCallRateThreshold(70);
		config.setMaxDurationOpenInHalfOpenState(0);
		CircuitBreaker circuitBreaker = new CircuitBreaker(config);
		circuitBreaker.getBreakerStateEventManager().addBreakerStateEventListener(new BreakerStateEventListener() {
			@Override
			public void onCircuitBreakerStateChangeEvent(CircuitBreakerStateChangeEvent event) {
				System.out.println("CircuitBreaker state changed. " + event);
				lastEvent = event;
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
		assertNull(lastEvent);
		TestUtils.sleep(1000);

		//time 11: CLOSED Fail, trip as 3F2S (3 fail, 1 success, ratio 75% > 70%)
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		assertEquals(lastEvent.getNewBreakerStateType(), BreakerStateType.OPEN);
		lastEvent = null;
		TestUtils.sleep(1000);
		
		//time 12: OPEN
		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		TestUtils.sleep(1000);
		
		//time 13: OPEN
		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		TestUtils.sleep(3200);
		
		//time 16: HALF-OPEN S but no more
		assertTrue(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		assertEquals(lastEvent.getNewBreakerStateType(), BreakerStateType.HALF_OPEN);
		lastEvent = null;
		circuitBreaker.callSucceeded(9);
		
		assertTrue(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		assertNull(lastEvent);
		//NO CALL

		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		//NO CALL

		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		//NO CALL
		TestUtils.sleep(3000);

		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		//NO CALL
		TestUtils.sleep(3000);

		assertFalse(circuitBreaker.isClosedForThisCall());
		
		//the end. Queue is empty
		assertEquals(circuitBreaker.getBreakerStateEventManager().getEventQueueLength(), 0);
	}
}
