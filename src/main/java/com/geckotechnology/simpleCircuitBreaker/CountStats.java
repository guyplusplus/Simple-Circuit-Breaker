package com.geckotechnology.simpleCircuitBreaker;

public class CountStats {
    public int callCount = 0;
    public int failureCallCount = 0;
    public int slowCallDurationCount = 0;
    public float failureRate = -1;
    public float slowCallRate = -1;
    
    public void calculateRates() {
    	if(callCount == 0) {
    		failureRate = -1;
    		slowCallRate = -1;
    		return;
    	}
    	failureRate = (float)failureCallCount * 100f / (float)callCount;
    	slowCallRate = (float)slowCallDurationCount * 100f / (float)callCount; 
    }
    
    public String toCountStatsString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("callCount:").append(callCount);
 		sb.append(", ").append("failureCallCount:").append(failureCallCount);
 		sb.append(", ").append("slowCallDurationCount:").append(slowCallDurationCount);
 		return sb.toString();
     }

    public String toExpressiveStatsString() {
    	if(failureRate == -1 || slowCallRate == -1)
    		calculateRates();
 		StringBuilder sb = new StringBuilder(toCountStatsString());
 		sb.append(", failureRate:").append(failureRate);
 		sb.append(", slowCallRate:").append(slowCallRate);
 		return sb.toString();
     }
}
