package com.quantatw.myapplication.line_bot;

import android.util.Log;

import com.quantatw.myapplication.MainActivity;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Random;

/**
 * Created by lecheel on 8/1/16.
 */
public class mUtilLine {

    private static final String TAG = "mUTIL";
    final static String mqttServer = "tcp://118.163.114.112:1883";

    public static void telbot_msg(String msg) {
        String client_id02 = md5(MainActivity.deviceID+"02");

        try {
            Log.d(TAG, "mqtt - mUtilLine telbot_msg >>>");
            MqttClient client = new MqttClient(mqttServer, client_id02, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.connect(options);
            MqttMessage m = new MqttMessage();
            m.setPayload(msg.getBytes());
            m.setQos(2);
            m.setRetained(false);
            client.publish("hcbi/rock02", m);
            client.disconnect();
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

    public static void telbot_sendImage(File mfile) throws IOException {
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(mfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        long ii = inStream.getChannel().size();
        byte[] message = new byte[(int)ii];
        String client_id01 = md5(MainActivity.deviceID+"01");

        inStream.read(message);

        try {
            Log.d(TAG, "mqtt - mUtilLine telbot_sendImage >>>");
            MqttClient client = new MqttClient(mqttServer, client_id01, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.connect(options);
            MqttMessage m = new MqttMessage();
            m.setPayload(message);
            m.setQos(2);
            m.setRetained(false);
            client.publish(MainActivity.lastTopicID, m);
            client.disconnect();
        } catch (MqttException me) {
            me.printStackTrace();
        }
        inStream.close();

    }

    public static final String md5(final String toEncrypt) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(toEncrypt.getBytes());
            final byte[] bytes = digest.digest();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(String.format("%02X", bytes[i]));
            }
            return sb.toString().toLowerCase();
        } catch (Exception exc) {
            return ""; // Impossibru!
        }
    }

    public static String getRandomHexString(int numchars){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < numchars){
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }

}
