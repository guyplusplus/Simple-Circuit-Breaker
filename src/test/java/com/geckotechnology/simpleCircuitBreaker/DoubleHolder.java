package com.geckotechnology.simpleCircuitBreaker;

public class DoubleHolder {
	
	private double d = 0;
	private int count = 0;
	
	public synchronized void addDouble(double value) {
		d += value;
		count++;
	}
	public double getDoubleAverage() {
		return d/(double)count;
	}
}
