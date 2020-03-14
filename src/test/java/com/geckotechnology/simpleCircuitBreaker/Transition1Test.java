package com.geckotechnology.simpleCircuitBreaker;

import static org.junit.Assert.*;

import org.junit.Test;

public class Transition1Test {
	
	private CircuitBreakerStateChangeEvent lastEvent;

	@Test
	public void test1() {
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		config.setSlidingWindowSize(5);
		config.setFailureRateThreshold(70);
		config.setWaitDurationInOpenState(4000);
		config.setMinimumNumberOfCalls(3);
		config.setPermittedNumberOfCallsInHalfOpenState(2);
		config.setSlowCallDurationThreshold(100);
		config.setSlowCallRateThreshold(0);
		CircuitBreaker circuitBreaker = new CircuitBreaker(config);
		circuitBreaker.getBreakerStateEventManager().addBreakerStateEventListener(new BreakerStateEventListener() {
			@Override
			public void onCircuitBreakerStateChangeEvent(CircuitBreakerStateChangeEvent event) {
				System.out.println("CircuitBreaker state changed. " + event);
				lastEvent = event;
			}
		});
		
		//time 0: Failed
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(1000);

		//time 1: Failed
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(6000);
		
		//time 8: Failed
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(1000);

		//time 9: Failed
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		TestUtils.sleep(1000);

		//time 10: Success
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertNull(lastEvent);
		TestUtils.sleep(1000);

		//time 11: Fail, trip as 3F2S (3 fail, 1 success, ratio 75% > 70%)
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(10);
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
		TestUtils.sleep(1000);
	
		//time 14: OPEN
		assertFalse(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.OPEN);
		assertNull(lastEvent);
		TestUtils.sleep(1000);
	
		//time 15: HALF-OPEN
		assertTrue(circuitBreaker.isClosedForThisCall());
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.HALF_OPEN);
		assertEquals(lastEvent.getNewBreakerStateType(), BreakerStateType.HALF_OPEN);
		lastEvent = null;
		TestUtils.sleep(1000);
	}

}
