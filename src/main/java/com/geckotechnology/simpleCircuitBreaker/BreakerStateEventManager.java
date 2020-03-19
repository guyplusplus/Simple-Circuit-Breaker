package com.geckotechnology.simpleCircuitBreaker;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BreakerStateEventManager {
	
	private final CopyOnWriteArrayList<BreakerStateEventListener> breakerStateEventListeners;
	private static final ExecutorService eventProcessorExecutor = Executors.newSingleThreadExecutor();
	
	public BreakerStateEventManager() {
		breakerStateEventListeners = new CopyOnWriteArrayList<BreakerStateEventListener>();
	}
	
	public void addBreakerStateEventListener(BreakerStateEventListener listener) {
		if(listener == null)
			throw new IllegalArgumentException("listener can not be null");
		breakerStateEventListeners.add(listener);
	}
	
	/**
	 * 
	 * @param listener
	 * @return true if the listener was already register, false otherwise
	 */
	public boolean removeBreakerStateEventListener(BreakerStateEventListener listener) {
		if(listener == null)
			throw new IllegalArgumentException("listener can not be null");
		return breakerStateEventListeners.remove(listener);
	}
	
    //------ Only Private and Default access methods bellow --------------------------
    
	void registerEvent(final CircuitBreakerStateChangeEvent event) {
		//check if any listener. If not, then no point adding to the queue
		if(breakerStateEventListeners.size() == 0)
			return;
		eventProcessorExecutor.submit(new Runnable() {
			@Override
			public void run() {
				for(BreakerStateEventListener listener:breakerStateEventListeners) {
					try {
						listener.onCircuitBreakerStateChangeEvent(event);
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}				
			}
		});
	}
}
