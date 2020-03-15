package com.geckotechnology.simpleCircuitBreaker;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

public class BreakerStateEventManager {
	
	private final CopyOnWriteArrayList<BreakerStateEventListener> breakerStateEventListeners;
	private final LinkedList<CircuitBreakerStateChangeEvent> eventQueue;
	
	public BreakerStateEventManager() {
		breakerStateEventListeners = new CopyOnWriteArrayList<BreakerStateEventListener>();
		eventQueue = new LinkedList<CircuitBreakerStateChangeEvent>();
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
    
	void registerEvent(CircuitBreakerStateChangeEvent event) {
		//check if any listener. If not, then no point adding to the queue
		if(breakerStateEventListeners.size() == 0)
			return;
		synchronized (eventQueue) {
			eventQueue.addLast(event);
		}
	}
	
	void processEventQueue() {
		CircuitBreakerStateChangeEvent event = null;
		while(true) {
			synchronized (eventQueue) {
				event = eventQueue.pollFirst();
			}
			if(event == null)
				//no more event in the queue
				return;
			//inform all listeners
			for(BreakerStateEventListener listener:breakerStateEventListeners) {
				try {
					listener.onCircuitBreakerStateChangeEvent(event);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	int getEventQueueLength() {
		return eventQueue.size();
	}
}
