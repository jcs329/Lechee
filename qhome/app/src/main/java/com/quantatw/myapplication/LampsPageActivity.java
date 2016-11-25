package com.quantatw.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.allseen.LSFLightingController;
import com.quantatw.myapplication.allseen.LampsInfoFragment;
import com.quantatw.myapplication.allseen.LampsTableFragment;

import org.allseen.lsf.sdk.ErrorCode;
import org.allseen.lsf.sdk.Lamp;
import org.allseen.lsf.sdk.LightingDirector;
import org.allseen.lsf.sdk.LightingItemErrorEvent;

/**
 * Created by youjun on 2016/5/3.
 */
public class LampsPageActivity extends Activity {
    public static final int NETWORK_EVENT = 0;
    public static final int LSFCONTROLLER_LEADER_EVENT = 1;
    public static final int LSFCONTROLLER_LAMP_EVENT = 2;

    private LSFLightingController lsfController;
    private LampsTableFragment lampsTable;
    private LampsInfoFragment lampsInfo;

    private LinearLayout lampsView;
    private LinearLayout loadingView;
    private Toast toast;

    private boolean mForeground;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(!mForeground) {
                Log.w("LampsPageActivity", "handleMessage - NOT in foreground");
                return;
            }

            if(msg != null) {
                Log.d("LampsPageActivity", "handleMessage - msg: " + msg.toString());

                switch(msg.what) {
                    case LampsPageActivity.NETWORK_EVENT:
                        Log.d("LampsPageActivity", "handleMessage - NETWORK_EVENT");

                        updateLampsPage();
                        break;
                    case LSFCONTROLLER_LEADER_EVENT:
                        Log.d("LampsPageActivity", "handleMessage - LSFCONTROLLER_LEADER_EVENT");

                        if(msg.arg1 == -1) { // onControllerErrors
                            LightingItemErrorEvent error = (LightingItemErrorEvent)msg.obj;
                            Log.d("LampsPageActivity", "handleMessage - LSFCONTROLLER_LEADER_EVENT - onControllerErrors - response code: " + error.responseCode + ", error: " + error.name);

                            if(error.errorCodes != null) {
                                for(ErrorCode ec : error.errorCodes) {
                                    if(!ec.equals(ErrorCode.NONE)) {
                                        StringBuilder sb = new StringBuilder();
                                        String name = ec.name();
                                        sb.append(name != null ? name : ec.ordinal());
                                        sb.append(error.name != null ? " - " + error.name : "");

                                        showToast("CONTROLLER ERROR: " + sb.toString());
                                    }
                                }
                            }
                        }
                        updateLampsPage();
                        break;
                    case LSFCONTROLLER_LAMP_EVENT:
                        Log.d("LampsPageActivity", "handleMessage - LSFCONTROLLER_LAMP_EVENT");

                        if(msg.arg1 == 0) { // onLampChanged
                            Lamp lamp = (Lamp)msg.obj;
                            Log.d("LampsPageActivity", "handleMessage - LSFCONTROLLER_LAMP_EVENT - onLampChanged(" + lamp.getName() + ")");

                            lampsTable.addLamp(lamp);

                            if(LightingDirector.get().getLampCount() == 1)
                                lampsInfo.updateInfoFields(lamp, false);
                        }
                        else if(msg.arg1 == 1) { // onLampRemoved
                            Lamp lamp = (Lamp)msg.obj;
                            Log.d("LampsPageActivity", "handleMessage - LSFCONTROLLER_LAMP_EVENT - onLampRemoved(" + lamp.getName() + ")");

                            lampsTable.removeLamp(lamp);
                        }
                        else if(msg.arg1 == -1) { // onLampError
                            LightingItemErrorEvent error = (LightingItemErrorEvent)msg.obj;
                            Log.d("LampsPageActivity", "handleMessage - LSFCONTROLLER_LAMP_EVENT - onLampError - response code: " + error.responseCode + ", error: " + error.name);

                            StringBuilder sb = new StringBuilder();
                            String name = error.responseCode.name();
                            sb.append(name != null ? name : error.responseCode.ordinal());
                            sb.append(error.name != null ? " - " + error.name : "");

                            showToast("LAMP ERROR: " + sb.toString());
                        }

                        updateLampsPage();

                        break;
                    default:
                        break;
                }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        Log.d("LampsPageActivity", "onCreate");

        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar);
        setContentView(R.layout.lamps_page_fragment);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = 640;
        params.width = 1000;
        this.getWindow().setAttributes(params);

        lampsView = (LinearLayout)findViewById(R.id.lampsView);
        loadingView = (LinearLayout)findViewById(R.id.loadingView);

        lampsTable = new LampsTableFragment();
        lampsInfo = new LampsInfoFragment();

        getFragmentManager().beginTransaction().replace(R.id.lampsTable, lampsTable).commit();
        getFragmentManager().beginTransaction().replace(R.id.lampsInfo, lampsInfo).commit();
    }

    @Override
    public void onResume() {
        Log.d("LampsPageActivity", "onResume");

        super.onResume();
        mForeground = true;

        MainActivity mainActivity = MainActivity.instance();
        if(mainActivity != null) {
            lsfController = mainActivity.getLSFController();
            Log.d("LampsPageActivity", "onResume - lsfController: " + this.lsfController);

            if(lsfController != null) {
                lsfController.setUIHandler(mHandler);
                lampsTable.setLSFController(lsfController);
                lampsInfo.setLSFController(lsfController);

                lampsTable.addLamps(lsfController.getLamps());
                if(lsfController.getLamps().length > 0)
                    lampsInfo.updateInfoFields((lsfController.getLamps())[0], true);
            }
        }

        updateLampsPage();
    }

    @Override
    public void onPause() {
        Log.d("LampsPageActivity", "onPause");

        super.onPause();
        mForeground = false;

        lsfController.setUIHandler(null);
        if(toast != null)
            toast.cancel();
    }

    @Override
    public void onBackPressed() {
        Log.d("LampsPageActivity", "onBackPressed");
        finish();
    }

    public LSFLightingController getLsfController() {
        //Log.d("LampsPageActivity", "getLsfController");
        return lsfController;
    }

    public void updateInfoFields(String itemID, Boolean forceNoShow) {
        //Log.d("LampsPageActivity", "updateInfoFields(" + itemID + ")");
        lampsInfo.updateInfoFields(LightingDirector.get().getLamp(itemID), forceNoShow);
    }

    private void updateLampsPage() {
        //Log.d("LampsPageActivity", "updateLampsPage");

        Boolean bHasLamp = LightingDirector.get().getLampCount() > 0;

        if(!MainActivity.isNetworkConnected(this)) {
            Log.w("LampsPageActivity", "updateLampsPage - NO NETWORK");

            loadingView.setVisibility(View.VISIBLE);
            lampsView.setVisibility(View.GONE);

            ((TextView) loadingView.findViewById(R.id.loadingText1)).setText(getText(R.string.no_network));
            ((TextView) loadingView.findViewById(R.id.loadingText2)).setText(getText(R.string.waiting_network));
        }
        else {
            if(!lsfController.isControllerConnected()) {
                Log.d("LampsPageActivity", "leader is DISCONNECTED");

                loadingView.setVisibility(View.VISIBLE);
                lampsView.setVisibility(View.GONE);

                ((TextView) loadingView.findViewById(R.id.loadingText1)).setText(getText(R.string.no_controller));
                ((TextView) loadingView.findViewById(R.id.loadingText2)).setText(getText(R.string.loading_controller));
            }
            else if(lsfController.isControllerConnected()) {
                Log.d("LampsPageActivity", "leader is CONNECTED");

                if(bHasLamp) {
                    loadingView.setVisibility(View.GONE);
                    lampsView.setVisibility(View.VISIBLE);
                }
                else {
                    loadingView.setVisibility(View.VISIBLE);
                    lampsView.setVisibility(View.GONE);

                    ((TextView) loadingView.findViewById(R.id.loadingText1)).setText(getText(R.string.no_lamps));
                    ((TextView) loadingView.findViewById(R.id.loadingText2)).setText(getText(R.string.loading_lamps));
                }
            }
        }
    }

    public void showToast(String text){
        Log.d("LampsPageActivity", "showToast - text: " + text);

        toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
