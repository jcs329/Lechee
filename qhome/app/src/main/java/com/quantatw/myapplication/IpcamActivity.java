package com.quantatw.myapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;
import android.content.SharedPreferences;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class IpcamActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipcam);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = 800;
        params.width = 800;
        this.getWindow().setAttributes(params);

        final String androidId = "hcbi/"+Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        SharedPreferences.Editor editor = getSharedPreferences("DID_PREFS", MODE_PRIVATE).edit();
        editor.putString("deviceid", androidId);
        editor.commit();

        TextView mydid = (TextView)findViewById(R.id.textDID);
        mydid.setText(androidId);
        try {

            ImageView myImage = (ImageView) findViewById(R.id.imageDID);
            String barcode_data=androidId;
            Bitmap bitmap = encodeAsBitmap(barcode_data, BarcodeFormat.QR_CODE, 500, 500);
            myImage.setImageBitmap(bitmap);


        } catch (WriterException e) {
            e.printStackTrace();
        }
        /*Wei reset LINE binding*/
        Button button = (Button)findViewById(R.id.resetbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String client_id02 = md5(MainActivity.deviceID+"02");
                String mqttServer = "tcp://118.163.114.112:1883";
                String android_id = androidId;
                String msg = android_id + " reset";
                    try {
                        MqttClient client = new MqttClient(mqttServer, android_id, new MemoryPersistence());
                        MqttConnectOptions options = new MqttConnectOptions();
                        options.setCleanSession(true);
                        client.connect(options);
                        MqttMessage m = new MqttMessage();
                        m.setPayload(msg.getBytes());
                        m.setQos(2);
                        m.setRetained(false);
                        client.publish("hcbi/rock03", m);
                        client.disconnect();
                    } catch (MqttException me) {
                        me.printStackTrace();
                    }
            }
        });

    }

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

}