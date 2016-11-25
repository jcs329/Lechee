package com.quantatw.myapplication.information_delivery.connection;

import com.quantatw.myapplication.MainActivity;
import com.quantatw.myapplication.R;
import com.quantatw.myapplication.information_delivery.sqlite.DBHelper;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.app.Activity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.util.UUID;

import android.widget.TextView;
import android.view.View;
import android.view.LayoutInflater;
import android.os.Message;
import android.os.Handler;

import org.json.JSONObject;
import org.json.JSONArray;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.*;

public class GetCloudData implements MqttCallback{

	final String TAG = "Cloud";
	private String userid;
	public DBHelper dbhelper = null;
	public Activity activity;
	private Context context;
	String state;
	View v=null;
	TextView textV1=null;
	MqttClient client;
	MqttAndroidClient mqttc=null;
	private static String uuid = null;
	public GetCloudData(final MainActivity _activity)
	{
		this.activity=_activity;
		dbhelper = new DBHelper(_activity.getBaseContext());
		
		Log.d(TAG, "MQTT Enter"); 
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable(){

		@Override
		public void run() {
			CloudConnect(_activity.getUserId());    
		}}, 6000);
	}
	
	
	
	public void CloudSubscribe()
	{
		//String topic = "Package/" + userid.replaceAll("\\s+","");
		String topic = userid.trim();
		Log.d(TAG, "MQTT topic by " + userid.trim() + ",length = " + userid.trim().length());
		int qos = 1;
		try {
			IMqttToken subToken = mqttc.subscribe(topic, qos);
			subToken.setActionCallback(new IMqttActionListener() {
			@Override
			public void onSuccess(IMqttToken asyncActionToken) {
					Log.d(TAG, "MQTT Subscribe by " + userid);
			}
	 
			@Override
			public void onFailure(IMqttToken asyncActionToken,
								  Throwable exception) {
			}
		});
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public String get_uuid(){
		if (uuid == null)
			uuid = UUID.randomUUID().toString();
		return uuid;
	}


        public void CloudDisconnect() throws MqttException {
                if(mqttc!=null){
                               mqttc.unregisterResources();
                               mqttc.close();
                }
        }

	public void CloudConnect(final String userid){

		MemoryPersistence mem = new MemoryPersistence();
		this.userid=userid;
	
	    if(mqttc==null){ 
			mqttc = new MqttAndroidClient(this.activity.getBaseContext(), "tcp://118.163.114.112:1883", get_uuid(),mem);
		}
		
		if (!mqttc.isConnected()) {
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			connOpts.setConnectionTimeout(3000);
			connOpts.setKeepAliveInterval(10 * 60);

			Log.d(TAG, "MQTT Connect by " + get_uuid());
			
			try {
				mqttc.setCallback(this);
				mqttc.connect(connOpts, null, new IMqttActionListener() {
					@Override
					public void onFailure(IMqttToken token, Throwable e) {
						
						Log.d(TAG, "MQTT Fail ");
						
						e.printStackTrace();
						Handler handler = new Handler();
						handler.postDelayed(new Runnable(){
						
						@Override
						public void run() {
							CloudConnect(userid);    
						}}, 3000);
						
					}

					@Override
					public void onSuccess(IMqttToken token) {
						Log.d(TAG, "MQTT OK ");
						CloudSubscribe();
					}
				});
			} catch (MqttException e) {
				e.printStackTrace();

			}
		}
		
	}		
	public void updateInformation(final Handler myHandler, String name){
		try {
			client = new MqttClient("tcp://118.163.114.112:1883",name,null);
			client.connect();
			client.setCallback(this);
			client.subscribe(name);	
			
        }catch(Exception e) {
			e.printStackTrace();
        }
	}	
	

	@Override
	public void connectionLost(Throwable cause) {
		
		Log.d(TAG, "MQTT Connection Lost ");
		Log.d(TAG, "MQTT Reconnect by " + userid);
		
		try{	
			CloudConnect(userid);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void messageArrived(String topic, MqttMessage message)
        throws Exception {
	                    Log.d(TAG, "MQTTReceived= " + message);
						try{
							JSONObject jsonobj = new JSONObject(message.toString());
							String mailfrom=jsonobj.getString("from");
							String mailto = jsonobj.getString("to");
							String receive_time= jsonobj.getString("Uploadtime");
							String filename=jsonobj.getString("img");
							String URL = "http://rms03.vccloud.quantatw.com:88/upload/upload_file/timthumb.php?src=" + filename ;
							String URL1="http://rms03.vccloud.quantatw.com:88/upload/upload_file/"+filename;
							dbhelper.add("infoperson",receive_time,URL,mailto,mailfrom,receive_time,URL1);
							MainActivity mainActivity = MainActivity.instance();
							Handler handler = mainActivity==null?null:mainActivity.getHandler();
							updateUnreadCun(handler);
							//Notify infoView update the unread message
							LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mainActivity);
							Intent intent = new Intent("android.intent.action.CART");
							int personunread = dbhelper.getTablePartialUnreadCount("infoperson");
							Log.d(TAG, "MQTTReceived personunread= " + personunread);
							intent.putExtra("infoperson",Integer.toString(personunread));
							manager.sendBroadcast(intent);
							//Add Notify Ringtone
							Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
							Ringtone r = RingtoneManager.getRingtone(mainActivity.getBaseContext(),notification);
							r.play();
						}catch(Exception e) {
							e.printStackTrace();
						}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {

	}
	public void updateUnreadCun(final Handler myHandler){
		int cnt;
		cnt=dbhelper.getTableUnreadCount();

		Message m = new Message();
		String obj=Integer.toString(cnt);
		m = myHandler.obtainMessage(5,obj);

		myHandler.sendMessage(m);
	}
	public void doupdateInformation(final Handler myHandler){
		try {	
						/* Update Text */

						int cnt=dbhelper.getTableCount();
						
                        URL mURL = new URL("http://219.87.191.181:8081");
                        HttpURLConnection conn = (HttpURLConnection) mURL.openConnection();

						
						if(conn!=null)
						{
						
							conn.setRequestMethod("POST");
							conn.setReadTimeout(5000);
							conn.setConnectTimeout(2000);
							conn.setDoOutput(true);

							String data="username=hcbi&password=hcbi&count=" + cnt;

							OutputStream out = conn.getOutputStream();
							out.write(data.getBytes());
							out.flush();
							out.close();

							int responseCode = conn.getResponseCode();

							if(responseCode == 200){
								InputStream inputdata = conn.getInputStream();
								state = getStringFromInputStream(inputdata);

							} else {
								Log.i(TAG, "Fail" + responseCode);
							}

							conn.disconnect();

						}
						
						
						JSONObject jsonobj = new JSONObject(state);
						String jsondata = jsonobj.getString("data");
						JSONArray dataArray = new JSONArray(jsondata);
						
						Log.d(TAG, "Received " + dataArray.length());
						
						for (int i = 0; i < dataArray.length(); i++) {


							String mailfrom=dataArray.getJSONObject(i).getString("from");
							String mailto = dataArray.getJSONObject(i).getString("to");
							String receive_time= dataArray.getJSONObject(i).getString("receive_time");
							String URL=dataArray.getJSONObject(i).getString("img");
							String URL1=URL.substring(URL.indexOf("jpg")+4,URL.length());
							URL=URL.substring(4,URL.indexOf("jpg")+3);


							dbhelper.add("infocomm",receive_time,URL,mailto,mailfrom,receive_time,URL1);
						}
						
						cnt=dbhelper.getTableUnreadCount();
						
						Message m = new Message();  
						String obj=Integer.toString(cnt);						
						m = myHandler.obtainMessage(5,obj);

						myHandler.sendMessage(m); 						
						
                    }catch(Exception e) {

                    }
		
	}
	
	public void updateInformationbyHTTP(final Handler myHandler){
		
        new Thread(){
            public void run(){
				
                while(true)
                {
                    try {
						
						/* Update Text */
						int cnt=dbhelper.getTableUnreadCount();
						
						Message m = new Message();  
						String obj=Integer.toString(cnt);						
						m = myHandler.obtainMessage(5,obj);


						myHandler.sendMessage(m); 
				 
						Thread.sleep(60000);
                        
						cnt=dbhelper.getTableCount();
						
                        URL mURL = new URL("http://219.87.191.181:8081");
                        HttpURLConnection conn = (HttpURLConnection) mURL.openConnection();

						
						if(conn==null)
							continue;
						
                        conn.setRequestMethod("POST");
                        conn.setReadTimeout(5000);
                        conn.setConnectTimeout(2000);
                        conn.setDoOutput(true);

                        String data="username=hcbi&password=hcbi&count=" + cnt;

                        OutputStream out = conn.getOutputStream();
                        out.write(data.getBytes());
                        out.flush();
                        out.close();

                        int responseCode = conn.getResponseCode();

                        if(responseCode == 200){
                            InputStream inputdata = conn.getInputStream();
							state = getStringFromInputStream(inputdata);

                        } else {
                            Log.i(TAG, "Fail" + responseCode);
                        }

                        conn.disconnect();
						
						JSONObject jsonobj = new JSONObject(state);
						String jsondata = jsonobj.getString("data");
						JSONArray dataArray = new JSONArray(jsondata);
						
						for (int i = 0; i < dataArray.length(); i++) {
							
							String mailfrom=dataArray.getJSONObject(i).getString("from");
							String mailto = dataArray.getJSONObject(i).getString("to");
							String receive_time= dataArray.getJSONObject(i).getString("receive_time");
							String URL=dataArray.getJSONObject(i).getString("img");
							String URL1=URL.substring(URL.indexOf("jpg")+4,URL.length());
							URL=URL.substring(4,URL.indexOf("jpg")+3);
							
							dbhelper.add("infocomm",receive_time,URL,mailto,mailfrom,receive_time,URL1);
						}	
                    }catch(Exception e) {

                    }
                }
            }
        }.start();
    }
	private static String getStringFromInputStream(InputStream inputdata)
            throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len = -1;


        while ((len = inputdata.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        inputdata.close();
        String state = os.toString();

        os.close();
        return state;
    }

}
