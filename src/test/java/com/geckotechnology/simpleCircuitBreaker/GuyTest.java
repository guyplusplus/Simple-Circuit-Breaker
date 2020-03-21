package com.geckotechnology.simpleCircuitBreaker;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


public class GuyTest {
	
    private static final Logger logger = Logger.getLogger(GuyTest.class.getName());
	private static final long RELOAD_CONFIG_INTERVAL = 5000;
	private static final Random rd = new Random(System.currentTimeMillis());
	private static final int NB_THREADS = 5;

	private CircuitBreaker circuitBreaker;
	private File configFile;
	private String propsPrefix;
	private long lastConfigLoadedTime = 0;

	public static void main(String[] args) {
		TestUtils.outputJVMInfo();
		try {
			FileHandler fh = new FileHandler("d:\\tmp\\breaker.log", true);
			fh.setFormatter(new Formatter() {
				@Override
			    public String format(LogRecord record) {
			        return record.getThreadID()+"::"+record.getSourceClassName()+"::"
			                +record.getSourceMethodName()+"::"
			                +new Date(record.getMillis())+"::"
			                +record.getMessage()+"\n";
				}
			});
			Logger.getLogger("com.geckotechnology.simpleCircuitBreaker").addHandler(fh);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		new Thread() {
//			@Override
//			public void run() {
//				new Test2().test(
//						"AIS_Prepaid.",
//						"C:\\Data\\Simple-Circuit-Breaker\\src\\test\\java\\com\\geckotechnology\\simpleCircuitBreaker\\breaker3.config"
//						);				
//			}
//		}.start();
		new Thread() {
			@Override
			public void run() {
				new GuyTest().test(
						null,
						"C:\\Data\\Simple-Circuit-Breaker\\src\\test\\java\\com\\geckotechnology\\simpleCircuitBreaker\\guy-breaker.config"
						);				
			}
		}.start();
	}
	
	private void loadOrReloadConfigFile() {
		if(lastConfigLoadedTime == 0 || configFile.lastModified() > lastConfigLoadedTime) {
			final CircuitBreakerConfig config;
			System.out.println("Loading configuration from file: " + configFile.getAbsolutePath());
			CircuitBreakerConfig tmpConfig = null;
			try {
				Properties props = new Properties();
				FileInputStream fis = new FileInputStream(configFile);
				props.load(fis);
				fis.close();
				tmpConfig = new CircuitBreakerConfig(propsPrefix, props);
			} catch (Exception e) {
				logger.warning("Problem loading configuration file, using default config");
				e.printStackTrace();
				tmpConfig = new CircuitBreakerConfig();
			}
			config = tmpConfig;
			circuitBreaker = new CircuitBreaker(config);
			circuitBreaker.getBreakerStateEventManager().addBreakerStateEventListener(new BreakerStateEventListener() {
				@Override
				public void onCircuitBreakerStateChangeEvent(CircuitBreakerStateChangeEvent event) {
					System.out.println("CircuitBreaker state changed. " + event);
					if(event.getNewBreakerStateType() == BreakerStateType.OPEN)
						System.out.println("  breaker " + event.getCircuitBreakerName() + " will remain open until " +
								new Date(event.getCreationTime() + config.getWaitDurationInOpenState()));
				}
			});
//			circuitBreaker.getBreakerStateEventManager().addBreakerStateEventListener(new BreakerStateEventListener() {
//				@Override
//				public void onCircuitBreakerStateChangeEvent(CircuitBreakerStateChangeEvent event) {
//					System.err.println("CircuitBreaker state changed. " + event);
//				}
//			});
			if(lastConfigLoadedTime == 0) {
		    	new Timer().scheduleAtFixedRate(new TimerTask() {
		            @Override
		            public void run() {
		            	loadOrReloadConfigFile();
		            }
		        }, RELOAD_CONFIG_INTERVAL, RELOAD_CONFIG_INTERVAL);
			}
			System.out.println("config:{" + config.toString() + "}");
			lastConfigLoadedTime = System.currentTimeMillis();
		}
	}
	
	public void test(String propsPrefix, String configPath) {
		this.propsPrefix = propsPrefix;
		configFile = new File(configPath);
		loadOrReloadConfigFile();
		for(int i = 0; i<NB_THREADS; i++) {
			final AtomicInteger threadId = new AtomicInteger(i);
			new Thread() {
				@Override
				public void run() {
					while(true) {	
						synchronized (circuitBreaker) {
							if(circuitBreaker.getBreakerState().getBreakerStateType() == BreakerStateType.CLOSED)
								TestUtils.validateAggregatedCountStatsMatches(circuitBreaker);
						}
						if(circuitBreaker.isClosedForThisCall()) {
							boolean willFail = (rd.nextInt(1000) > 700);
							if(willFail) {
								long duration = 10;
								System.out.println("[" + threadId.get() + "] Breaker is closed. Simulating callFailed with duration: " + duration);
								circuitBreaker.callFailed(duration);
							}
							else {
								long duration = rd.nextInt(1500); 
								System.out.println("[" + threadId.get() + "] Breaker is closed. Simulating callSucceeded with duration: " + duration);
								try {
									Thread.sleep(duration);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								circuitBreaker.callSucceeded(duration);
							}					
						}
						else {
							System.out.println("[" + threadId.get() + "] Breaker is opened. Pass...");
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
	}

}
