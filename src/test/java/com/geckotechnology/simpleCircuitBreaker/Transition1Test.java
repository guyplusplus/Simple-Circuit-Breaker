package com.geckotechnology.simpleCircuitBreaker;

import static org.junit.Assert.*;

import org.junit.Test;

public class Transition1Test {
	
	private CircuitBreakerStateChangeEvent lastEvent;

	/**
	 * Test transitions : closed -> open -> half-open -> closed -> open -> half-open -> open -> half-open -> closed
	 * Event type: call failure
	 */
	@Test
	public void test1() {
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		config.setSlidingWindowSize(5);
		config.setFailureRateThreshold(70);
		config.setWaitDurationInOpenState(4000);
		config.setMinimumNumberOfCalls(3);
		config.setPermittedNumberOfCallsInHalfOpenState(2);
		config.setSlowCallDurationThreshold(1);
		config.setSlowCallRateThreshold(0);
		config.setMaxDurationOpenInHalfOpenState(5000);
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
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(1000);

		//time 1: CLOSED Failed
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(6000);
		
		//time 8: CLOSED Failed
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(1000);

		//time 9: CLOSED Failed
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(1000);

		//time 10: CLOSED Success
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertNull(lastEvent);
		TestUtils.sleep(1000);

		//time 11: CLOSED Fail, trip as 3F2S (3 fail, 1 success, ratio 75% > 70%)
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		assertEquals(lastEvent.getNewBreakerStateType(), BreakerStateType.OPEN);
		lastEvent = null;
		TestUtils.sleep(1000);

		//time 12: OPEN
		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		TestUtils.sleep(1000);
	
		//time 13: OPEN
		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		TestUtils.sleep(1000);
	
		//time 14: OPEN
		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		assertNull(lastEvent);
		TestUtils.sleep(1200);
	
		//time 15: HALF-OPEN as 4s+ is over Success
		assertTrue(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		assertEquals(lastEvent.getNewBreakerStateType(), BreakerStateType.HALF_OPEN);
		lastEvent = null;
		circuitBreaker.callSucceeded(10);
		TestUtils.sleep(1000);

		//time 16: HALF-OPEN moving to CLOSED Failed, 2 calls where failure = 50% < 70%
		assertTrue(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		assertNull(lastEvent);
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertEquals(lastEvent.getNewBreakerStateType(), BreakerStateType.CLOSED);
		lastEvent = null;
		TestUtils.sleep(1000);
	
		//time 17: CLOSED FSSFFFF, 71% failure
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);

		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);

		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);

		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);

		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertNull(lastEvent);

		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertNull(lastEvent);

		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		assertEquals(lastEvent.getNewBreakerStateType(), BreakerStateType.OPEN);
		lastEvent = null;

		//time 18: OPEN
		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		TestUtils.sleep(4200);
		
		//time 22: HALF-OPEN FF
		assertTrue(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		assertEquals(lastEvent.getNewBreakerStateType(), BreakerStateType.HALF_OPEN);
		lastEvent = null;
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		TestUtils.sleep(1000);

		assertTrue(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		assertNull(lastEvent);
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		assertEquals(lastEvent.getNewBreakerStateType(), BreakerStateType.OPEN);
		lastEvent = null;
		TestUtils.sleep(1000);
		
		//time 24: OPEN
		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		TestUtils.sleep(1000);
		
		//time 25: OPEN
		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		TestUtils.sleep(3200);
		
		//time 28: HALF-OPEN S but no more
		assertTrue(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		assertEquals(lastEvent.getNewBreakerStateType(), BreakerStateType.HALF_OPEN);
		lastEvent = null;
		circuitBreaker.callSucceeded(10);
		
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
		TestUtils.sleep(1000);

		//time 29
		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		//NO CALL
		TestUtils.sleep(3000);

		//time 32
		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		assertNull(lastEvent);
		//NO CALL
		TestUtils.sleep(1200);
		
		//time: 33 back to CLOSED
		assertTrue(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertEquals(lastEvent.getNewBreakerStateType(), BreakerStateType.CLOSED);
		lastEvent = null;
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		
		//the end. Queue is empty
		assertEquals(circuitBreaker.getBreakerStateEventManager().getEventQueueLength(), 0);
	}
}
