package com.quantatw.sls.wificonfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import android.util.Log;

public class CommandSocketClient {

	private int ServerPort = 80;// 要監控的port
	
	
	
	public CommandSocketClient(int port)
	{
		ServerPort = port;
	}
	public byte[] SendCommandByIP(String ip, byte[] command) {

		Socket client = new Socket();

		InetSocketAddress isa = new InetSocketAddress(ip, ServerPort);
		int ret = 0;
		byte[] b = new byte[1024];
		try {
			client.connect(isa);
			BufferedOutputStream out = new BufferedOutputStream(
					client.getOutputStream());
			// 送出字串
			out.write(command);
			out.flush();
			//out.close();
			
			BufferedInputStream in = new BufferedInputStream(client.getInputStream());
            
            
            int length;
            length = in.read(b);// &lt;=0的話就是結束了
            
           
			
			out = null;
			client.close();
			client = null;

		} catch (java.io.IOException e) {
//			com.piddas21.log.Log.println(0,"CommandSocketClient Send Error : " + ip);
//			
//			com.piddas21.log.Log.println(0,"IOException :" + e.toString());
			Log.d("WifiSetting","IOException :"+ e.toString());
			return null;
		}
		return b;
	}
}
