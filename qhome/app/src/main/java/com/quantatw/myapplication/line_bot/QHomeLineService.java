package com.quantatw.myapplication.line_bot;

import android.app.Activity;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.quantatw.myapplication.MainActivity;
import com.quantatw.myapplication.mUtil;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class QHomeLineService {
    private static final String TAG = "MQTT";

    final String mqttServer = "tcp://118.163.114.112:1883";
    private static MqttClient client;

    String clientId = "HCBI_TODO";
    String subscriptionTopic = "hcbi/rock02";
    private ScheduledExecutorService scheduler;
    private MqttConnectOptions options;
    boolean isSubscript = false;

    final MqttCallback mqttCallback = new MqttCallback() {

        public void messageArrived(String topic, MqttMessage message) throws Exception {
            // TODO Auto-generated method stub
            String msgData = message.toString();
            Log.d(TAG, "MQTT: " + msgData);
            MainActivity mainActivity = MainActivity.instance();
            Handler handler = mainActivity==null?null:mainActivity.getHandler();
            int intCMD=0;
            if (msgData.equals("curtain_on")) intCMD = 21;
            if (msgData.equals("curtain_off")) intCMD = 20;
            if (msgData.equals("fanon")) intCMD = 11;
            if (msgData.equals("fanoff")) intCMD = 10;
            if (msgData.equals("snapshot")) intCMD = 33;
            if (msgData.equals("light_on")) intCMD = 41;
            if (msgData.equals("light_off")) intCMD = 40;
            if (msgData.equals("bind_reset_ok")) intCMD = 91;
            if (msgData.equals("bind_reset_fail")) intCMD = 90;
            if(handler != null) {
                Message msg = Message.obtain();
                msg.what = MainActivity.HANDLER_EVENT_LINEBOT_MESSAGE;
                msg.arg1 = intCMD;
                handler.sendMessage(msg);
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
                Log.d(TAG,"connectionLost");
				isSubscript = false;
				
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };

	

    String randomString(final int length) {
        Random r = new Random(); // perhaps make it a class variable so you don't make a new one every time
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < length; i++) {
            char c = (char)(r.nextInt((int)(Character.MAX_VALUE)));
            sb.append(c);
        }
        return sb.toString();
    }

    private void initMQTT() {
        Log.d(TAG, "connectMQTT() Line enter");
        clientId="LINE==="+ mUtil.getRandomHexString(16);
        try {
            client = new MqttClient(mqttServer, clientId, new MemoryPersistence());

            options = new MqttConnectOptions();
//            options.setCleanSession(true);
            options.setConnectionTimeout(15);
            options.setKeepAliveInterval(30);
            client.setCallback(mqttCallback);

//            client.connect(options);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectMQTT() {
        new Thread(new Runnable() {

            @Override
            public void run() {
				Log.d(TAG, "Do connectMQTT()");
                try {
                    client.connect(options);
					Log.d(TAG, "Do connectMQTT() OK");
                    if (!isSubscript) {
                        subscriptionTopic = MainActivity.deviceID+"_line";
                        client.subscribe(subscriptionTopic);
                        Log.d(TAG, "<>>>>>>" + subscriptionTopic);
                        isSubscript = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
				Log.d(TAG,"MQTT LINE Check =" + client.isConnected());
                if(!client.isConnected()) {
                    connectMQTT();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    public QHomeLineService(Activity activity) {
        Log.d(TAG, "QHomeLineService");

        initMQTT();
        startReconnect();

    }
}
