package com.quanta.hcbiapi;

import java.io.IOException;

import android.util.Log;

/**
 * 
 * Class to control ADAM 4055 RS485 to GPO converter
 *
 */
public class Adam4055 extends Rs485{
	private static final String TAG = "Adam4055";
	Level gpo_state[];

	/**
	 * GPO number of ADAM 4055
	 *
	 */
	public enum Gpo {
		GPO0(0),
		GPO1(1),
		GPO2(2),
		GPO3(3),
		GPO4(4),
		GPO5(5),
		GPO6(6),
		GPO7(7);
		
		int gpo;
		
		Gpo(int gpo) {
			this.gpo = gpo;
		}
		
		int getVal() {
			return gpo;
		}
	}
	
	/**
	 * GPO level of ADAM 4055
	 *
	 */
	public enum Level {
		LOW		(0),
		HIGH 	(1);
		
		int level;
		
		Level(int level) {
			this.level = level;
		}
		
		int getVal() {
			return level;
		}
	}
	
	/**
	 * Setup Adam4055. Set all GPO as low
	 * @param port select which RS485 port is used
	 * @throws IOException if the connection can be setup
	 */
	public Adam4055(Rs485.Port port) throws IOException {
		super(port, BaudRate.BPS9600);
		gpo_state = new Level [8];
		for(int i = 0; i < gpo_state.length; i++) {
			gpo_state[i] = Level.LOW;
		}
		write("#010000\r".getBytes());
	}
	
	/**
	 * Set one GPO value of ADAM 4055
	 * @param gpo the GPO number to be controled
	 * @param level HIGH or LOW
	 */
	public void setGpo(Gpo gpo, Level level) {
		
		gpo_state[gpo.getVal()] = level;

		int all_gpo = 0;
		
		for(int i = 0; i < gpo_state.length; i++)
		{
			all_gpo |= gpo_state[i].getVal()  << i;
		}
		
		String str = "#0100"+ String.format("%02x", all_gpo) + "\r";
		
		str = str.toUpperCase();
		try {
			write(str.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
