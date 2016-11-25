package com.quantatw.myapplication;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.quanta.hcbiapi.Gpio;

import java.io.IOException;

/**
 * Created by lecheel on 10/7/16.
 */
public class QHomeGpioService {
    private static final String LOGTAG = "GPIO";
    private static volatile boolean mStartService;
    private boolean mRunning;
    private Thread mGpioThread;
    final Gpio gpio = new Gpio();

    private Runnable GpioServerRun = new Runnable() {
        Gpio.Level[] level = new Gpio.Level[4];
        MainActivity mainActivity = MainActivity.instance();
        Handler handler = mainActivity==null?null:mainActivity.getHandler();

        @Override
        public void run() {
            mRunning = true;
            Gpio.Level lastGPI0;
            lastGPI0 = Gpio.Level.LOW;  // default GPI as low
            while (true) {
                try {
                    gpio.setGpo(Gpio.Gpo.GPO0, Gpio.Level.HIGH);
                    Thread.sleep(100);
                    level[0] = gpio.getGpi(Gpio.Gpi.GPI0);
                    level[1] = gpio.getGpi(Gpio.Gpi.GPI1);
                    level[2] = gpio.getGpi(Gpio.Gpi.GPI2);
                    level[3] = gpio.getGpi(Gpio.Gpi.GPI3);
                    if (level[0] == Gpio.Level.HIGH) {
                        if (lastGPI0 != level[0]) {
                            Message msg = Message.obtain();
                            msg.what = MainActivity.HANDLER_EVENT_GPIO;
                            msg.arg1 = 0;
                            handler.sendMessage(msg);

                            Log.d(LOGTAG, "GPI0 Active as High");
                            // action for GPI0 is going high
                            //GPI0_Action();
                        }
//                        gpio.setGpo(Gpio.Gpo.GPO2, Gpio.Level.HIGH);
                    } else {
                        if (lastGPI0 != level[0]) Log.d(LOGTAG, "GPI0 Low");
//                        gpio.setGpo(Gpio.Gpo.GPO2, Gpio.Level.LOW);
                    }
                    lastGPI0 = level[0];

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }
        }
    };


    public QHomeGpioService(Activity activity) {
        Log.d(LOGTAG, "QHomeGpioService");

        mRunning = false;
        mStartService = false;
    }

    public boolean isRunning() { return mRunning; }

    public void startService() {
        Log.d(LOGTAG, "startService");

        mStartService = true;
        mGpioThread = new Thread(GpioServerRun);
        mGpioThread.start();
    }

    public void stopService() {
        Log.d(LOGTAG, "stopService");

        mStartService = false;
        mGpioThread = null;

    }

}
