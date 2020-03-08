package com.geckotechnology.simpleCircuitBreaker;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;


public class Test2 {
	
    private static final Logger logger = Logger.getLogger(Test2.class.getName());
	private static final long RELOAD_CONFIG_INTERVAL = 5000;
	private static final Random rd = new Random(System.currentTimeMillis());
	private static final int NB_THREADS = 5;

	private CircuitBreaker circuitBreaker;
	private File configFile;
	private String propsPrefix;
	private long lastConfigLoadedTime = 0;

	public static void main(String[] args) {
//		new Thread() {
//			@Override
//			public void run() {
//				new Test1().test(
//						"AIS_Prepaid.",
//						"C:\\Data\\Simple-Circuit-Breaker\\src\\test\\java\\com\\geckotechnology\\simpleCircuitBreaker\\breaker3.config"
//						);				
//			}
//		}.start();
		new Thread() {
			@Override
			public void run() {
				new Test2().test(
						null,
						"C:\\Data\\Simple-Circuit-Breaker\\src\\test\\java\\com\\geckotechnology\\simpleCircuitBreaker\\breaker4.config"
						);				
			}
		}.start();
	}
	
	private void loadOrReloadConfigFile() {
		if(lastConfigLoadedTime == 0 || configFile.lastModified() > lastConfigLoadedTime) {
			CircuitBreakerConfig config = null;
			System.out.println("Loading configuration from file: " + configFile.getAbsolutePath());
			try {
				Properties props = new Properties();
				FileInputStream fis = new FileInputStream(configFile);
				props.load(fis);
				fis.close();
				config = new CircuitBreakerConfig(propsPrefix, props);
			} catch (Exception e) {
				logger.warning("Problem loading configuration file, using default config");
				e.printStackTrace();
				config = new CircuitBreakerConfig();
			}
			circuitBreaker = new CircuitBreaker(config);
			if(lastConfigLoadedTime == 0) {
		    	new Timer().scheduleAtFixedRate(new TimerTask() {
		            @Override
		            public void run() {
		            	loadOrReloadConfigFile();
		            }
		        }, RELOAD_CONFIG_INTERVAL, RELOAD_CONFIG_INTERVAL);
			}
			lastConfigLoadedTime = System.currentTimeMillis();
		}
	}
	
	public void test(String propsPrefix, String configPath) {
		this.propsPrefix = propsPrefix;
		configFile = new File(configPath);
		loadOrReloadConfigFile();
		for(int i = 0; i<NB_THREADS; i++) {
			new Thread() {
				@Override
				public void run() {
					while(true) {
						synchronized (circuitBreaker) {
							BreakerStateInterface breakerState = circuitBreaker.getBreakerState();
							if(breakerState instanceof BreakerClosedState) {
								((BreakerClosedState) breakerState).testCheckSumForUnitTest();
							}
						}
						if(circuitBreaker.isClosedForThisCall()) {
							boolean willFail = (rd.nextInt(1000) > 700);
							if(willFail) {
								long duration = 10;
								System.out.println("Breaker is closed. Simulating callFailed with duration: " + duration);
								circuitBreaker.callFailed(duration);
							}
							else {
								long duration = rd.nextInt(1500); 
								System.out.println("Breaker is closed. Simulating callSucceeded with duration: " + duration);
								try {
									Thread.sleep(duration);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								circuitBreaker.callSucceeded(duration);
							}					
						}
						else {
							System.out.println("Breaker is opened. Pass...");
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
	}

}
