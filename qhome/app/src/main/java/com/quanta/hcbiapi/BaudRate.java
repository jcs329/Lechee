package com.quanta.hcbiapi;

/**
 * Baud rate for Rs485
 *
 */
public enum BaudRate {
	BPS9600		(9600),
	BPS115200	(115200),
	BPS2000000	(2000000);	
		
	private int baudRate;
	
	BaudRate(int baudRate) {
		this.baudRate = baudRate;
	}
	
	int getVal() {
		return baudRate;
	}
}