package com.geckotechnology.simpleCircuitBreaker;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

public class CircuitBreakerConfigTest {

	/**
	 * Directly copy key and values from README.md
	 */
	@Test
	public void testDefaults() {
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		assertEquals(config.getName(), "");
		assertTrue(config.getFailureRateThreshold() == 50f);
		assertTrue(config.getSlowCallRateThreshold() == 100f);
		assertEquals(config.getSlowCallDurationThreshold(), 60000);
		assertEquals(config.getPermittedNumberOfCallsInHalfOpenState(), 10);
		assertEquals(config.getSlidingWindowSize(), 100);
		assertEquals(config.getMinimumNumberOfCalls(), 10);
		assertEquals(config.getWaitDurationInOpenState(), 60000);
		assertEquals(config.getMaxDurationOpenInHalfOpenState(), 120000);
	}
	
	@Test
	public void testConfig() throws Exception {
		Properties props = new Properties();
		InputStream is = CircuitBreakerConfigTest.class.getResourceAsStream("/configTest.config");
		props.load(is);
		is.close();
		CircuitBreakerConfig config = new CircuitBreakerConfig(props);
		assertEquals(config.getName(), "TEST");
		assertTrue(config.getFailureRateThreshold() == 6f);
		assertTrue(config.getSlowCallRateThreshold() == 7f);
		assertEquals(config.getSlowCallDurationThreshold(), 4);
		assertEquals(config.getPermittedNumberOfCallsInHalfOpenState(), 3);
		assertEquals(config.getSlidingWindowSize(), 2);
		assertEquals(config.getMinimumNumberOfCalls(), 5);
		assertEquals(config.getWaitDurationInOpenState(), 8);
		assertEquals(config.getMaxDurationOpenInHalfOpenState(), 9);
	}
	
	@Test
	public void testConfigWithPrefix() throws Exception {
		Properties props = new Properties();
		InputStream is = CircuitBreakerConfigTest.class.getResourceAsStream("/configTest.config");
		props.load(is);
		is.close();
		CircuitBreakerConfig config = new CircuitBreakerConfig("PREFIX.", props);
		assertEquals(config.getName(), "TEST_PREFIX");
		assertTrue(config.getFailureRateThreshold() == 60f);
		assertTrue(config.getSlowCallRateThreshold() == 70f);
		assertEquals(config.getSlowCallDurationThreshold(), 40);
		assertEquals(config.getPermittedNumberOfCallsInHalfOpenState(), 30);
		assertEquals(config.getSlidingWindowSize(), 20);
		assertEquals(config.getMinimumNumberOfCalls(), 50);
		assertEquals(config.getWaitDurationInOpenState(), 80);
		assertEquals(config.getMaxDurationOpenInHalfOpenState(), 90);
	}

}
