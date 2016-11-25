package com.quanta.hcbiapi;

import java.io.IOException;

//import android.util.Log;

/**
 * This class is for open/read/write RS485 port.
 */
public class Rs485 extends Tty{

	/**
	 * RS485 port
	 *
	 */
	public enum Port {
		PORT0 ("/dev/ttyUSB0"),
		PORT1 ("/dev/ttyUSB1");
		
		private String port;
		
		Port(String port) {
			this.port = port;
		}
				
		public String toString() {
			return port;
		}
	}
	

	/**
	 * Open a RS485 port.
	 * @param port specify RS485 port 0 or port 1 to be opened.
	 * @param baudrate specify baudrate of the RS485 port.
	 * @throws IOException if fail to open the RS485 port.
	 */
	public Rs485(Port port, BaudRate baudrate) throws IOException {
		super(port.toString(), baudrate, 0);
	}
	
	/**
	 * Writes b.length bytes of raw data from the specified byte array to this RS485 port.
	 * @param buf the raw data.
	 * @throws IOException if an I/O error occurs.
	 */
	public void write(byte[] buf) throws IOException {
		super.write(buf);
	}
	
	/**
	 * Reads some number of raw data bytes from the RS485 port and stores them into the buffer array b. The number of bytes actually read is returned as an integer. This method blocks until input data is available, end of file is detected, or an exception is thrown.
	 * If the length of b is zero, then no bytes are read and 0 is returned; otherwise, there is an attempt to read at least one byte. If no byte is available because the stream is at the end of the file, the value -1 is returned; otherwise, at least one byte is read and stored into b.
	 *
	 * The first byte read is stored into element b[0], the next one into b[1], and so on. The number of bytes read is, at most, equal to the length of b. Let k be the number of bytes actually read; these bytes will be stored in elements b[0] through b[k-1], leaving elements b[k] through b[b.length-1] unaffected.
	 * @param buf the buffer into which the data is read.
	 * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the stream has been reached.
	 * @throws IOException If the first byte cannot be read for any reason other than the end of the file, if the input stream has been closed, or if some other I/O error occurs.
	 */
	public int read(byte[] buf) throws IOException {
		return super.read(buf);
	}

}
