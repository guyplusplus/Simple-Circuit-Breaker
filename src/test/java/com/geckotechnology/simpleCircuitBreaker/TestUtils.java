package com.geckotechnology.simpleCircuitBreaker;

public class TestUtils {

	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
