package com.geckotechnology.simpleCircuitBreaker;

public class OverheadTest {

	CircuitBreaker breaker;
	private static final int ARRAY_SIZE = 5017;
	private static final int LOOP_CALC_COUNT = 500000;
	private static final int LOOP_TEST = 1000;
	private static final int LOOP_TEST_WARMUP = 10000;
	
	public static void main(String[] args) {
		new OverheadTest().go();

	}

	public void go() {
		CircuitBreakerConfig config = new CircuitBreakerConfig();
		config.setSlidingWindowSize(10);
		breaker = new CircuitBreaker(config);

		int array[] = null;
		long start, end = 0;
		
		//warm-up
		array = createArray();
		for(int i = 0; i<LOOP_TEST_WARMUP; i++)
			oneCall(array);
		
		for(int j = 0; j<5; j++) {
			//no breaker
			array = createArray();
			start = System.currentTimeMillis();
			for(int i = 0; i<LOOP_TEST; i++)
				oneCall(array);
			end = System.currentTimeMillis();
			double durationPerCallNoBreaker = (double)(end-start)/(double)LOOP_TEST;
			System.out.println("duration (ms) per oneCall - NO BREAKER: " + durationPerCallNoBreaker);
	
			//breaker
			array = createArray();
			start = System.currentTimeMillis();
			for(int i = 0; i<LOOP_TEST; i++) {
				if(breaker.isClosedForThisCall()) {
					long startCall = System.currentTimeMillis();
					oneCall(array);
					breaker.callSucceeded(System.currentTimeMillis() - startCall);
				}
			}
			end = System.currentTimeMillis();
			double durationPerCallWithBreaker = (double)(end-start)/(double)LOOP_TEST;
			System.out.println("duration (ms) per oneCall - WITH BREAKER: " + durationPerCallWithBreaker);
			System.out.println("Overhead (ms) per oneCall of breaker: " + (durationPerCallWithBreaker - durationPerCallNoBreaker));
		}
		System.out.println("Done");
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
		for(int i = 0; i<LOOP_CALC_COUNT; i++)
			array[(7 * i) % s] = array[(3 * i) % s] * array [i % s] + array [(5 * i) % s];
//		long end = System.currentTimeMillis();
//		System.out.println("duration: " + (end-start));
	}
}
