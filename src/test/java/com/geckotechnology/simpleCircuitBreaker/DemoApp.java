package com.geckotechnology.simpleCircuitBreaker;

import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class DemoApp {
	
	private static final Random rd = new Random(System.currentTimeMillis());
	private static final String CONFIG_RESOURCE = "/demo.config";
	private static final int NB_THREADS = 5;
	private static final float ACTUAL_SUCCESS_RATE_RANGE_PCT = 70;
	private static final long ACTUAL_CALL_DURATION_RANGE_MS = 1500;
	private static final long WAIT_BETWEEN_LOOP_RANGE_MS = 1000;

	private CircuitBreaker circuitBreaker;

	public static void main(String[] args) {
		TestUtils.outputJVMInfo();
		new Thread() {
			@Override
			public void run() {
				new DemoApp().test();
			}
		}.start();
	}
	
	private void loadConfigFile() {
		//load configuration from resource
		try {
			Properties props = new Properties();
			InputStream is = CircuitBreakerConfigTest.class.getResourceAsStream(CONFIG_RESOURCE);
			props.load(is);
			is.close();
			CircuitBreakerConfig config = new CircuitBreakerConfig(props);
			System.out.println("config:{" + config.toString() + "}");
			circuitBreaker = new CircuitBreaker(config);
		} catch (Exception e) {
			//failed to load the configuration
			e.printStackTrace();
			System.exit(-1);
		}
		//add a simple listener to log events to console
		circuitBreaker.getBreakerStateEventManager().addBreakerStateEventListener(new BreakerStateEventListener() {
			@Override
			public void onCircuitBreakerStateChangeEvent(CircuitBreakerStateChangeEvent event) {
				System.out.println("CircuitBreaker state changed. " + event);
			}
		});
	}
	
	public void test() {
		loadConfigFile();
		for(int i = 0; i<NB_THREADS; i++) {
			final AtomicInteger threadId = new AtomicInteger(i);
			new Thread() {
				@Override
				public void run() {
					long duration;
					while(true) {
						if(circuitBreaker.isClosedForThisCall()) {
							boolean willFail = (rd.nextInt(1000) > 10 * ACTUAL_SUCCESS_RATE_RANGE_PCT);
							duration = rd.nextInt((int)ACTUAL_CALL_DURATION_RANGE_MS); 
							if(willFail) {
								System.out.println("[" + threadId.get() + "] Breaker is closed. Simulating callFailed with duration: " + duration);
								TestUtils.sleep(duration);
								circuitBreaker.callFailed(duration);
							}
							else {
								System.out.println("[" + threadId.get() + "] Breaker is closed. Simulating callSucceeded with duration: " + duration);
								TestUtils.sleep(duration);
								circuitBreaker.callSucceeded(duration);
							}
						}
						else {
							System.out.println("[" + threadId.get() + "] Breaker is opened. Pass...");
						}
						duration = rd.nextInt((int)WAIT_BETWEEN_LOOP_RANGE_MS); 
						TestUtils.sleep(duration);
					}
				}
			}.start();
		}
	}

}
