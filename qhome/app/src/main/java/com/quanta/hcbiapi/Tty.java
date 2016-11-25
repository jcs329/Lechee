package com.quanta.hcbiapi;

import java.io.*;
import android.util.Log;

class Tty {

	private String TAG = "Tty";
	private FileDescriptor mFd;
	private InputStream inStream;
	private OutputStream outStream;
	
	private native static FileDescriptor open(String path, int baudrate, int flags);
		
	protected Tty(String path, BaudRate baudrate, int flag) throws StreamCorruptedException, IOException {
		mFd = open(path, baudrate.getVal(), flag);

		if (mFd == null) {
			Log.e(TAG, "native open returns null");
		}
		
		inStream = new FileInputStream(mFd);
		outStream = new FileOutputStream(mFd);			
	}
	
	protected void write(byte[] buf) throws IOException {
		outStream.write(buf);
	}
	
	

	protected int read(byte[] buf) throws IOException {
		return inStream.read(buf);
	}
	
	protected int available() throws IOException {
		return inStream.available();
	}

    static {
        System.loadLibrary("HCBIAPI");
    }

}
