package com.geckotechnology.simpleCircuitBreaker;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class OverheadTest {

	private static final int ARRAY_SIZE = 5017;
	private static final int LOOP_ONE_CALL = 500000;
	private static final int LOOP_TEST = 5000;
	private static final int LOOP_TEST_WARMUP = 5000;
	private static final int THREAD_COUNT = 4;
		
	@Before
	public void warmUp() {
		System.out.println("Starting warmUp");
		int array[] = createArray();
		for(int i = 0; i<LOOP_TEST_WARMUP; i++)
			oneCall(array);
	}
	
	@Test
	public void overHeadTestSingleThread() {
		System.out.println("Starting OverheadTest.overHeadTestSingleThread");
		//no breaker
		double durationPerCallNoBreaker = singleThreadNoBreaker();
		//with breaker
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		config.setSlidingWindowSize(10);
		CircuitBreaker breaker = new CircuitBreaker(config);
		double durationPerCallWithBreaker = singleThreadWithBreaker(breaker);
		//summary
		double overHead = durationPerCallWithBreaker - durationPerCallNoBreaker;
		System.out.println("duration (ms) per oneCall - NO BREAKER: " + durationPerCallNoBreaker);
		System.out.println("duration (ms) per oneCall - WITH BREAKER: " + durationPerCallWithBreaker);
		System.out.println("Overhead (ms) per oneCall of breaker: " + overHead);
		assertTrue(overHead < 0.2);
	}
	
	@Test
	public void overHeadTestMultiThread() {
		System.out.println("Starting OverheadTest.overHeadTestMultiThread");
		//no breaker
		final DoubleHolder durationPerCallNoBreakerHolder = new DoubleHolder();
		Thread threads[] = new Thread[THREAD_COUNT];
		for(int t = 0; t<THREAD_COUNT; t++) {
			threads[t] = new Thread() {
				public void run() {
					double durationPerCallNoBreaker = singleThreadNoBreaker();
					durationPerCallNoBreakerHolder.addDouble(durationPerCallNoBreaker);
				}
			};
			threads[t].start();
		}
		for(int t = 0; t<THREAD_COUNT; t++) {
			try {
				threads[t].join();
			} catch (InterruptedException e) {
				fail("InterruptedException catched: " + e);
			}
		}
		//with breaker
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		config.setSlidingWindowSize(10);
		final CircuitBreaker breaker = new CircuitBreaker(config);
		final DoubleHolder durationPerCallWithBreakerHolder = new DoubleHolder();
		threads = new Thread[THREAD_COUNT];
		for(int t = 0; t<THREAD_COUNT; t++) {
			threads[t] = new Thread() {
				public void run() {
					double durationPerCallWithBreaker = singleThreadWithBreaker(breaker);
					durationPerCallWithBreakerHolder.addDouble(durationPerCallWithBreaker);
				}
			};
			threads[t].start();
		}
		for(int t = 0; t<THREAD_COUNT; t++)
			try {
				threads[t].join();
			} catch (InterruptedException e) {
				fail("InterruptedException catched: " + e);
			}
		//summary
		double durationPerCallNoBreaker = durationPerCallNoBreakerHolder.getDoubleAverage();
		double durationPerCallWithBreaker = durationPerCallWithBreakerHolder.getDoubleAverage();
		double overHead = durationPerCallWithBreaker - durationPerCallNoBreaker;
		System.out.println("duration (ms) per oneCall - NO BREAKER: " + durationPerCallNoBreaker);
		System.out.println("duration (ms) per oneCall - WITH BREAKER: " + durationPerCallWithBreaker);
		System.out.println("Overhead (ms) per oneCall of breaker: " + overHead);
		assertTrue(overHead < 0.2);
	}
	
	private double singleThreadNoBreaker() {
		int array[] = createArray();
		long start = System.currentTimeMillis();
		for(int i = 0; i<LOOP_TEST; i++)
			oneCall(array);
		long end = System.currentTimeMillis();
		return (double)(end-start)/(double)LOOP_TEST;
	}

	private double singleThreadWithBreaker(CircuitBreaker breaker) {
		int array[] = createArray();
		long start = System.currentTimeMillis();
		for(int i = 0; i<LOOP_TEST; i++) {
			if(breaker.isClosedForThisCall()) {
				long startCall = System.currentTimeMillis();
				oneCall(array);
				breaker.callSucceeded(System.currentTimeMillis() - startCall);
			}
		}
		long end = System.currentTimeMillis();
		return (double)(end-start)/(double)LOOP_TEST;
	}
	
	private int[] createArray() {
		int array[] = new int[ARRAY_SIZE];
		for(int i = 0; i<ARRAY_SIZE; i++)
			array[i] = i;
		return array;
	}
	
	private void oneCall(int array[]) {
//		long start = System.currentTimeMillis();
		int s = array.length;
		for(int i = 0; i<LOOP_ONE_CALL; i++)
			array[(7 * i) % s] = array[(3 * i) % s] * array [i % s] + array [(5 * i) % s];
//		long end = System.currentTimeMillis();
//		System.out.println("duration: " + (end-start));
	}
}
