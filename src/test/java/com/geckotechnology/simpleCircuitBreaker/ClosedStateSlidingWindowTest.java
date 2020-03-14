package com.geckotechnology.simpleCircuitBreaker;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClosedStateSlidingWindowTest {

	@Test
	public void test() {
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		config.setSlidingWindowSize(5);
		config.setFailureRateThreshold(0);
		config.setSlowCallDurationThreshold(100);
		config.setSlowCallRateThreshold(0);
		CircuitBreaker circuitBreaker = new CircuitBreaker(config);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches((BreakerClosedState)circuitBreaker.getBreakerState(), 0, 0, 0));
		
		//bucket 0
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(10);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches((BreakerClosedState)circuitBreaker.getBreakerState(), 1, 0, 0));

		//bucket 0
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callSucceeded(120);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches((BreakerClosedState)circuitBreaker.getBreakerState(), 2, 0, 1));

		TestUtils.sleep(2000);
		
		//bucket 2
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(120);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches((BreakerClosedState)circuitBreaker.getBreakerState(), 3, 1, 2));
		
		TestUtils.sleep(1000);

		//bucket 3
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(120);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches((BreakerClosedState)circuitBreaker.getBreakerState(), 4, 2, 3));
		
		TestUtils.sleep(1000);

		//bucket 4
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(2);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches((BreakerClosedState)circuitBreaker.getBreakerState(), 5, 3, 3));
		
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
		assertTrue(TestUtils.validateAggregatedCountStatsMatches((BreakerClosedState)circuitBreaker.getBreakerState(), 5, 4, 3));

		TestUtils.sleep(5000);

		//bucket 1
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(250);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches((BreakerClosedState)circuitBreaker.getBreakerState(), 1, 1, 1));

		TestUtils.sleep(5000);

		//bucket 1
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(250);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches((BreakerClosedState)circuitBreaker.getBreakerState(), 1, 1, 1));

		TestUtils.sleep(4000);

		//bucket 0
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(250);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches((BreakerClosedState)circuitBreaker.getBreakerState(), 2, 2, 2));

		TestUtils.sleep(6000);

		//bucket 1
		assertTrue(circuitBreaker.isClosedForThisCall());
		circuitBreaker.callFailed(250);
		assertEquals(circuitBreaker.getBreakerState().getBreakerStateType(), BreakerStateType.CLOSED);
		assertTrue(TestUtils.validateAggregatedCountStatsMatches((BreakerClosedState)circuitBreaker.getBreakerState(), 1, 1, 1));
	}
}
