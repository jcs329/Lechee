package com.quantatw.myapplication.allseen;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.quantatw.myapplication.LampsPageActivity;
import com.quantatw.myapplication.MainActivity;
import com.quantatw.myapplication.R;
import com.quantatw.myapplication.voiceAssistant.VoiceAssistant;

import org.allseen.lsf.sdk.Color;
import org.allseen.lsf.sdk.Controller;
import org.allseen.lsf.sdk.ControllerListener;
import org.allseen.lsf.sdk.Lamp;
import org.allseen.lsf.sdk.LampAbout;
import org.allseen.lsf.sdk.LampListener;
import org.allseen.lsf.sdk.LightingController;
import org.allseen.lsf.sdk.LightingControllerConfigurationBase;
import org.allseen.lsf.sdk.LightingDirector;
import org.allseen.lsf.sdk.LightingItemErrorEvent;
import org.allseen.lsf.sdk.LightingSystemQueue;
import org.allseen.lsf.sdk.MutableColorItem;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by youjun on 2016/4/19.
 */
public class LSFLightingController implements LampListener,
                                              ControllerListener,
                                              MainActivity.NetworkEventListener,
                                              VoiceAssistant.ICommandHandler {

    //private static HandlerThread mHanderThread;
    //private static Handler mHandler;
    private Handler controllerUIHandler;

    private LightingController controllerService;
    private volatile boolean controllerServiceStarted = false;
    private volatile boolean controllerClientConnected = false;

    private static long lastFieldChangeMillis = 0;
    private static final long FIELD_CHANGE_HOLDOFF = 25;

    private static final int MAX_ERROR_TIMES = 5;
    private int mErrorTimes;

    private List<VoiceAssistant.VoiceCommand> mVoiceCommand;

    public static final String LOGTAG = "LSFLightingController";

    public LSFLightingController(Activity activity) {
        Log.i(LOGTAG, "LSFLightingController");

        mErrorTimes = 0;
        prepareVoiceCommand();

        // Setup localized strings in data models
        Controller.setDefaultName(activity.getString(R.string.default_controller_name));
        LampAbout.setDataNotFound(activity.getString(R.string.data_not_found));
        Lamp.setDefaultName(activity.getString(R.string.default_lamp_name));

        controllerService = LightingController.get();
        controllerService.init(new ControllerConfiguration(activity.getFileStreamPath("").getAbsolutePath(), activity));
    }

    @Override
    public void onNetworkChanged(NetworkInfo networkInfo) {
        //Log.d(LOGTAG, "onNetworkChanged");

        Handler handler = MainActivity.instance().getHandler();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        if(isConnected) {
            Log.i(LOGTAG, "onNetworkChanged - connected");

            if(controllerServiceStarted == false) {

                if(handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(LOGTAG, "onNetworkChanged - setControllerStarted");

                            LightingDirector.get().setNetworkConnectionStatus(true);
                            setControllerStarted(true);
                        }
                    });
                }
            }
        }
        else {
            Log.i(LOGTAG, "onNetworkChanged - NOT connected");

            if(handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(LOGTAG, "wifiConnectionStateUpdate - wifi connected");

                        LightingDirector.get().setNetworkConnectionStatus(false);
                        setControllerStarted(false);
                    }
                });
            }
        }

        if(controllerUIHandler != null) {
            Message msg = Message.obtain();
            msg.what = LampsPageActivity.NETWORK_EVENT;
            controllerUIHandler.sendMessage(msg);
        }
    }

    @Override
    public void onLampInitialized(final Lamp lamp) {
        Log.d(LOGTAG, "onLampInitialized(" + lamp.getName() + ")");
    }

    @Override
    public void onLampChanged(final Lamp lamp) {
        Log.d(LOGTAG, "onLampChanged(" + lamp.getName() + ")");

        if(controllerUIHandler != null) {
            Message msg = Message.obtain();
            msg.what = LampsPageActivity.LSFCONTROLLER_LAMP_EVENT;
            msg.arg1 = 0;
            msg.obj = lamp;
            controllerUIHandler.sendMessage(msg);
        }
    }

    @Override
    public void onLampRemoved(final Lamp lamp) {
        Log.d(LOGTAG, "onLampRemoved(" + lamp.getName() + ")");

        if(controllerUIHandler != null) {
            Message msg = Message.obtain();
            msg.what = LampsPageActivity.LSFCONTROLLER_LAMP_EVENT;
            msg.arg1 = 1;
            msg.obj = lamp;
            controllerUIHandler.sendMessage(msg);
        }
    }

    @Override
    public void onLampError(final LightingItemErrorEvent error) {
        Log.d(LOGTAG, "onLampError: " + error.name);

        if(controllerUIHandler != null) {
            Message msg = Message.obtain();
            msg.what = LampsPageActivity.LSFCONTROLLER_LAMP_EVENT;
            msg.arg1 = -1;
            msg.obj = error;
            controllerUIHandler.sendMessage(msg);
        }

        /*mErrorTimes++;
        if(mErrorTimes >= MAX_ERROR_TIMES) {
            Log.w(LOGTAG, "onLampError - restart controller");
            mErrorTimes = 0;
            restart();
        }*/
    }

    @Override
    public void onLeaderChange(Controller leader) {
        Log.d(LOGTAG, "onLeaderChange - name: " + leader.getName() + ", version: " + leader.getVersion() + ", connected: " + leader.isConnected());

        if(controllerServiceStarted)
            controllerClientConnected = leader.isConnected();

        if(controllerUIHandler != null) {
            Message msg = Message.obtain();
            msg.what = LampsPageActivity.LSFCONTROLLER_LEADER_EVENT;
            msg.arg1 = 0;
            msg.obj = "LEADER CHANGED";
            controllerUIHandler.sendMessage(msg);
        }
    }

    @Override
    public void onControllerErrors(final LightingItemErrorEvent event) {
        Log.d(LOGTAG, "onControllerErrors - event: " + event.name);

        if(controllerUIHandler != null) {
            Message msg = Message.obtain();
            msg.what = LampsPageActivity.LSFCONTROLLER_LEADER_EVENT;
            msg.arg1 = -1;
            msg.obj = event;
            controllerUIHandler.sendMessage(msg);
        }
    }

    //---------------- Voice Assistant ----------------
    @Override
    public List<VoiceAssistant.VoiceCommand> getCommandTable() {
        //Log.d(LOGTAG, "getCommandTable");

        if(mVoiceCommand == null)
            prepareVoiceCommand();

        return mVoiceCommand;
    }

    @Override
    public boolean commandHandler(String main, String second, List<String> result) {
        Log.d(LOGTAG, "commandHandler - main: " + main + ", second: " + second);

        MainActivity activity = MainActivity.instance();
        VoiceAssistant voiceAssistant = activity != null ? activity.getVoiceAssistant() : null;
        if(voiceAssistant == null) {
            Log.w(LOGTAG, "commandHandler - no voiceAssistant");
            return false;
        }

        boolean ret = true;
        if(main.contentEquals(activity.getString(R.string.cmd_allseen_light_main))
                && second.contentEquals(activity.getString(R.string.cmd_action_turn_on))) {
            Log.d(LOGTAG, "commandHandler - turn on lamps");

            if(LightingDirector.get().getLampCount() > 0) {
                String res = activity.getString(R.string.cmd_allseen_light_main)+activity.getString(R.string.res_action_opening);
                voiceAssistant.createDialog(res+"...", 0);
                voiceAssistant.speakOut(res, false);

                if(!voiceAssistantTurnOnLamps()) {
                    Log.d(LOGTAG, "commandHandler - turn on lamps failed");

                    voiceAssistant.createDialog(activity.getString(R.string.res_action_open_failed), 0);
                    voiceAssistant.speakOut(activity.getString(R.string.res_action_open_failed), true);
                }
                else {
                    res = activity.getString(R.string.cmd_allseen_light_main)+activity.getString(R.string.res_action_opened);
                    voiceAssistant.createDialog(res, 0);
                    voiceAssistant.speakOut(res, true);
                }
            }
            else {
                Log.d(LOGTAG, "commandHandler - turn on lamps - no lamps");

                voiceAssistant.createDialog(activity.getString(R.string.res_allseen_light_unknown), 0);
                voiceAssistant.speakOut(activity.getString(R.string.res_allseen_light_unknown), true);
            }
        }
        else if(main.contentEquals(activity.getString(R.string.cmd_allseen_light_main))
                && second.contentEquals(activity.getString(R.string.cmd_action_turn_off))) {
            Log.d(LOGTAG, "commandHandler - turn off lamps");

            if(LightingDirector.get().getLampCount() > 0) {
                String res = activity.getString(R.string.cmd_allseen_light_main)+activity.getString(R.string.res_action_closing);
                if(voiceAssistant != null) {
                    voiceAssistant.createDialog(res+"...", 0);
                    voiceAssistant.speakOut(res, false);
                }

                if(!voiceAssistantTurnOffLamps()) {
                    Log.d(LOGTAG, "commandHandler - turn off lamps failed");

                    voiceAssistant.createDialog(activity.getString(R.string.res_action_close_failed), 0);
                    voiceAssistant.speakOut(activity.getString(R.string.res_action_close_failed), true);
                }
                else {
                    res = activity.getString(R.string.cmd_allseen_light_main)+activity.getString(R.string.res_action_closed);
                    voiceAssistant.createDialog(res, 0);
                    voiceAssistant.speakOut(res, true);
                }
            }
            else {
                Log.d(LOGTAG, "commandHandler - turn on lamps - no lamps");

                voiceAssistant.createDialog(activity.getString(R.string.res_allseen_light_unknown), 0);
                voiceAssistant.speakOut(activity.getString(R.string.res_allseen_light_unknown), true);
            }
        }

        return ret;
    }

    // voiceAssistant API -----------------------------------
    public boolean voiceAssistantTurnOnLamps() {
        Log.d(LOGTAG, "voiceAssistantTurnOnLamps");

        Lamp[] lamps = LightingDirector.get().getLamps();
        for(Lamp lamp : lamps) {
            if(lamp.isOn() && lamp.getColor().getBrightness() == 0) {
                Log.i(LOGTAG, "voiceAssistantTurnOnLamps - Raise brightness to 25%");

                Color color = lamp.getColor();
                color.setBrightness(25);
                lamp.setColor(color);

                continue;
            }

            if(lamp.isOn()) {
                //Log.d(LOGTAG, "voiceAssistantTurnOnLamps - lamp is on");
                continue;
            }

            lamp.togglePower();
        }

        return true;
    }

    public boolean voiceAssistantTurnOffLamps() {
        Log.d(LOGTAG, "voiceAssistantTurnOffLamps");

        Lamp[] lamps = LightingDirector.get().getLamps();
        for(Lamp lamp : lamps) {
            if(lamp.isOff()) {
                //Log.d(LOGTAG, "voiceAssistantTurnOffLamps - lamp is off");
                continue;
            }

            lamp.togglePower();
        }

        return true;
    }
    //-------------------------------------------------------

    public void start() {
        Log.i(LOGTAG, "start");

        /*if(mHanderThread != null) {
            mHanderThread.quit();
            mHanderThread = null;
            mHandler = null;
        }

        mHanderThread = new HandlerThread("LSFLightingController");
        mHanderThread.start();
        mHandler = new Handler(mHanderThread.getLooper());*/

        final Handler handler = MainActivity.instance().getHandler();

        LightingDirector.get().addListener(this);
        LightingDirector.get().start(
                "LSFLightingController",
                new LightingSystemQueue() {
                    @Override
                    public void post(Runnable r) {
                        //Log.d(LOGTAG, "LightingSystemQueue - post: " + r);
                        if(handler != null)
                            handler.post(r);
                    }

                    @Override
                    public void postDelayed(Runnable r, int delay) {
                        //Log.d(LOGTAG, "LightingSystemQueue - postDelayed: " + r);
                        if(handler != null)
                            handler.postDelayed(r, delay);
                    }

                    @Override
                    public void stop() {
                        //Log.d(LOGTAG, "LightingSystemQueue - stop");
                        // Currently nothing to do
                    }
                }
        );

        MainActivity.addNetworkListener(this);
        VoiceAssistant.register(this);

        if(MainActivity.isNetworkConnected(MainActivity.instance())) {
            Log.d(LOGTAG, "start - network is avaliable");
            LightingDirector.get().setNetworkConnectionStatus(true);
            setControllerStarted(true);
        }
    }

    public void stop() {
        Log.i(LOGTAG, "stop");

        MainActivity.removeNetworkListener(this);
        //LightingDirector.get().removeListener(this);
        VoiceAssistant.unregister(this);

        LightingDirector.get().stop();
        setControllerStarted(false);
    }

    public void restart() {
        Log.i(LOGTAG, "restart");

        stop();

        Handler handler = MainActivity.instance().getHandler();
        if(handler != null) {
            Message msg = Message.obtain();
            msg.what = MainActivity.HANDLER_EVENT_LSF_CONTROLLER;
            msg.arg1 = 0;
            msg.obj = "Start LSFLightingController";

            handler.sendMessage(msg);
        }
    }

    private void setControllerStarted(boolean startController) {
        //Log.d(LOGTAG, "setControllerStarted(" + startController + ")");

        if(startController) {
            if(!controllerServiceStarted) {
                controllerServiceStarted = true;
                controllerService.start();
                Log.i(LOGTAG, "setControllerStarted - START constrollerService");
            }
        }
        else {
            controllerService.stop();
            controllerServiceStarted = false;
            Log.i(LOGTAG, "setControllerStarted - STOP constrollerService");
        }
    }

    public boolean isControllerConnected() {
        Log.d(LOGTAG, "isControllerConnected() - return " + controllerClientConnected);
        return controllerClientConnected;
    }

    public void setUIHandler(Handler handler) {
        //Log.d(LOGTAG, "setUIHandler");

        if(handler != null)
            controllerUIHandler = handler;
    }

    public Lamp[] getLamps() {
        Log.d(LOGTAG, "getLamps");
        return LightingDirector.get().getLamps();
    }

    public void togglePower(String itemID) {
        //Log.d(LOGTAG, "togglePower - itemID: " + itemID);

        MutableColorItem colorItem = LightingDirector.get().getLamp(itemID);
        if (colorItem != null) {
            if (colorItem.isOff() && colorItem.getColor().getBrightness() == 0) {
                // Raise brightness to 25% if needed
                Color color = colorItem.getColor();
                color.setBrightness(25);
                colorItem.setColor(color);

                Log.i(LOGTAG, "togglePower - Raise brightness to 25%");
            }

            colorItem.togglePower();
            Log.i(LOGTAG, "togglePower - Toggle power for " + colorItem.getName());
        }
    }

    public void setBrightness(String itemID, int newViewBrightness) {
        //Log.d(LOGTAG, "setBrightness - itemID: " + itemID + ", newViewBrightness: " + newViewBrightness);

        if(allowFieldChange()) {
            MutableColorItem colorItem = LightingDirector.get().getLamp(itemID);

            if(colorItem != null) {
                Color color = colorItem.getColor();

                int oldViewBrightness = color.getBrightness();
                colorItem.setBrightness(newViewBrightness);
                Log.i(LOGTAG, "setBrightness - Set brightness for " + colorItem.getName() + " from " + oldViewBrightness + " to " + newViewBrightness);

                if(newViewBrightness == 0) {
                    colorItem.turnOff();
                    Log.i(LOGTAG, "setBrightness - Setting brightness to zero forces the power off");
                }
                else if(oldViewBrightness == 0 && colorItem.isOff()) {
                    // Raising the brightness on a dark item forces the power on
                    colorItem.turnOn();
                }
            }
        }
    }

    public void setHue(String itemID, int viewHue) {
        //Log.d(LOGTAG, "setHue - itemID: " + itemID + ", vieHue: " + viewHue);

        if(allowFieldChange()) {
            MutableColorItem colorItem = LightingDirector.get().getLamp(itemID);

            if(colorItem != null) {
                colorItem.setHue(viewHue);
                Log.i(LOGTAG, "setHue - Set hue for " + colorItem.getName() + " to " + viewHue);
            }
        }
    }

    public void setSaturation(String itemID, int viewSaturation) {
        //Log.d(LOGTAG, "setSaturation - itemID: " + itemID + ", viewSaturation: " + viewSaturation);

        if(allowFieldChange()) {
            MutableColorItem colorItem = LightingDirector.get().getLamp(itemID);

            if(colorItem != null) {
                colorItem.setSaturation(viewSaturation);
                Log.i(LOGTAG, "setSaturation - Set saturation for " + colorItem.getName() + " to " + viewSaturation);
            }
        }
    }

    public void setColorTemp(String itemID, int viewColorTemp) {
        //Log.d(LOGTAG, "setColorTemp - itemID: " + itemID + ", viewColorTemp: " + viewColorTemp);

        if (allowFieldChange()) {
            MutableColorItem colorItem = LightingDirector.get().getLamp(itemID);

            if (colorItem != null) {
                colorItem.setColorTemperature(viewColorTemp);
                Log.d(LOGTAG, "setColorTemp - Set color temp for " + colorItem.getName() + " to " + viewColorTemp);
            }
        }
    }

    public int getColorTempMin(Lamp lamp) {
        //Log.d(LOGTAG, "getColorTempMin");

        int colorTempMin = lamp != null ? lamp.getColorTempMin() : LightingDirector.COLORTEMP_MIN;
        return colorTempMin;
    }

    public int getColorTempSpan(Lamp lamp) {
        //Log.d(LOGTAG, "getColorTempSpan");

        int colorTempMin = lamp != null ? lamp.getColorTempMin() : LightingDirector.COLORTEMP_MIN;
        int colorTempMax = lamp != null ? lamp.getColorTempMax() : LightingDirector.COLORTEMP_MAX;
        return colorTempMax - colorTempMin;
    }

    private boolean allowFieldChange() {
        boolean allow = false;
        long currentTimeMillis = Calendar.getInstance().getTimeInMillis();

        if(currentTimeMillis - lastFieldChangeMillis > FIELD_CHANGE_HOLDOFF) {
            lastFieldChangeMillis = currentTimeMillis;
            allow = true;
        }

        Log.d(LOGTAG, "allowFieldChange - return " + allow);
        return allow;
    }

    private void prepareVoiceCommand() {
        Log.d(LOGTAG, "prepareVoiceCommand");

        mVoiceCommand = new ArrayList<VoiceAssistant.VoiceCommand>();

        MainActivity activity = MainActivity.instance();
        VoiceAssistant.VoiceCommand voiceCmd1 = new VoiceAssistant.VoiceCommand(activity.getString(R.string.cmd_allseen_light_main), activity.getString(R.string.cmd_action_turn_on));
        mVoiceCommand.add(voiceCmd1);

        VoiceAssistant.VoiceCommand voiceCmd2 = new VoiceAssistant.VoiceCommand(activity.getString(R.string.cmd_allseen_light_main), activity.getString(R.string.cmd_action_turn_off));
        mVoiceCommand.add(voiceCmd2);
    }
}

class ControllerConfiguration extends LightingControllerConfigurationBase {

    private final Context appContext;

    public ControllerConfiguration(String keystorePath, Context context) {
        super(keystorePath);
        appContext = context;
        Log.d(LSFLightingController.LOGTAG, "ControllerConfiguration - keystorePath: " + keystorePath);
    }

    @Override
    public String getMacAddress(String generatedMacAddress) {
        Log.d(LSFLightingController.LOGTAG, "ControllerConfiguration - getMacAddress(" + generatedMacAddress + ")");

        if (appContext == null) {
            return generatedMacAddress;
        }

        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String originalMacAddress = wInfo.getMacAddress();

        if (originalMacAddress == null) {
            // If we don't have mac address, create one of length 12 which is
            // the usual length of mac address.
            originalMacAddress = generatedMacAddress;
        }

        originalMacAddress = originalMacAddress.replace(":", "");
        return originalMacAddress;
    }

    @Override
    public boolean isNetworkConnected() {
        Log.d(LSFLightingController.LOGTAG, "ControllerConfiguration - isNetworkConnected");

        if (appContext == null) {
            return super.isNetworkConnected();
        }

        return MainActivity.isNetworkConnected(appContext) || isWifiAPMode();
    }

    private boolean isWifiAPMode() {
        Log.d(LSFLightingController.LOGTAG, "ControllerConfiguration - isWifiAPMode");

        boolean isWifiApEnabled = false;
        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

        try {
            // need reflection because wifi ap is not in the public API
            Method isWifiApEnabledMethod = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            isWifiApEnabled = (Boolean) isWifiApEnabledMethod.invoke(wifiManager);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return isWifiApEnabled;
    }
}
