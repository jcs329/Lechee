package com.quanta.hcbiapi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.quanta.hcbiapi.Ir.Port;

/**
 * 
 * Class to control HCBI external GPIO
 *
 */
public class Gpio {

	/**
	 * GPO pin
	 *
	 */
	public enum Gpo {
		GPO0 ("/sys/module/hcbi_gpio/parameters/gpo0"),
		GPO1 ("/sys/module/hcbi_gpio/parameters/gpo1"),
		GPO2 ("/sys/module/hcbi_gpio/parameters/gpo2"),
		GPO3 ("/sys/module/hcbi_gpio/parameters/gpo3");
		
		private String pin;
		
		Gpo(String pin) {
			this.pin = pin;
		}
		
		public String toString() {
			return pin;
		}
	}
	
	/**
	 * 
	 * GPI pin
	 *
	 */
	public enum Gpi {
		GPI0 ("/sys/module/hcbi_gpio/parameters/gpi0"),
		GPI1 ("/sys/module/hcbi_gpio/parameters/gpi1"),
		GPI2 ("/sys/module/hcbi_gpio/parameters/gpi2"),
		GPI3 ("/sys/module/hcbi_gpio/parameters/gpi3");
		
		private String pin;
		
		Gpi(String pin) {
			this.pin = pin;
		}
		
		public String toString() {
			return pin;
		}
	}
	
	/**
	 * 
	 * GPIO level
	 *
	 */
	public enum Level {
		HIGH("1"),
		LOW("0");
		
		private String level;
		
		Level(String leval) {
			this.level = leval;
		}
		
		public String toString() {
			return level;
		}
	}
	
	public Gpio() {
	}
	
	/**
	 * Set output level to a GPO pin
	 * @param pin the GPO pin to control
	 * @param level the output level
	 * @throws IOException if fail to perform HW IO
	 */
	public void setGpo(Gpo pin, Level level) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(pin.toString()));
		bw.write(level.toString());
		bw.close();
	}
	
	/**
	 * Get output level setting of a GPO pin
	 * @param pin the PGO pin to get the setting
	 * @return level setting of the GPO pin
	 * @throws IOException if fail to perform HW IO
	 */
	public Level getGpo(Gpo pin) throws IOException {
		BufferedReader bw = new BufferedReader(new FileReader(pin.toString()));
		int c = bw.read();
		bw.close();
		return c == '0' ? Level.LOW : Level.HIGH;
	}
	
	/**
	 * Get input level of a GPI pin 
	 * @param pin the GPI pin to get input level
	 * @return input level of the GPI pin
	 * @throws IOException if fail to perform HW IO
	 */
	public Level getGpi(Gpi pin) throws IOException {
		BufferedReader bw = new BufferedReader(new FileReader(pin.toString()));
		int c = bw.read();
		bw.close();
		return c == '0' ? Level.LOW : Level.HIGH;
	}
}
