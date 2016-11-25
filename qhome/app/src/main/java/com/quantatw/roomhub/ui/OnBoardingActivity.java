package com.quantatw.roomhub.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.listener.OnBoardingScanStateChangedListener;
import com.quantatw.roomhub.listener.OnBoardingStateChangedListener;
import com.quantatw.roomhub.manager.OnBoardee;
import com.quantatw.roomhub.manager.OnBoardingManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.ui.circleprogress.ArcProgress;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.PreferenceEditor;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.key.ErrorKey;

import org.alljoyn.onboarding.sdk.OnboardingManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 95010915 on 2015/9/25.
 */
public class OnBoardingActivity extends AbstractRoomHubActivity implements OnBoardingStateChangedListener, OnBoardingScanStateChangedListener {

    private String mSSID;
    private String mPassword;
    private int mSecurity;
    private GlobalDef.PskType mPskType;

    private View startLayout,resultLayout,retryLayout;
    private View scanLayout, connectLayout;
    private View scanStartLayout, scanProgressLayout;

    private TextView scanResultText;
    private ArcProgress scanProgress;

    private TextView connectStatusText,connectStatusNumberText;
    private ArcProgress connectProgress;

    private Button btnNext,btnRestart;

    private static final int PROVISION_SET = 1;

    private static final String TAG = "OnBoardingActivity";
    private boolean DEBUG = true;
    private boolean mIsOnBoarding = true;

    private OnBoardingManager mOBManager;
    private RoomHubManager mRoomhubManager;

    private OnBoardee[] mClientList;
    private ArrayList<OnBoardee> mClientFailedList = new ArrayList<OnBoardee>();

    private class ClientState {
        boolean result;
        int state;
        void setResult(boolean result) { this.result = result; }
        void setState(int state) {this.state = state; }
    }

    private HashMap<String, ClientState> mClientJoinedList = new HashMap<String, ClientState>();

    private int mJoinedClientCount = 0;
    private int mJoinedFailedClientCount = 0;
    private int mCount = 0;

    private final static int PROCESS_DO_SCAN = 90011;
    private final static int PROCESS_DO_STARTONBOARDING = 90012;
    private final static int PROCESS_DO_GET_ROOMHUBLIST = 90013;
    private final static int PROCESS_DO_CHECKING_WIFI_STATUS = 90014;
    private final static int PROCESS_DO_FINISH_NO_ROOMHUB = 90015;
    private final static int PROCESS_DO_SHOW_ERROR = 90016;
    private final static int PROCESS_DO_DEMO = 90018;
    private final static int PROCESS_DO_GET_ROOMHUBLIST_DEMO = 90019;
    private final static int PROCESS_RECONNECT_CHECK_STATUS = 90020;
    private final static int PROCESS_DO_RECONNECT = 90021;
    private final static int PROCESS_DO_FAIL_RECONNECT = 90022;
    private final static int PROCESS_ONBOARDING_DONE_GO_NEXT = 90023;

    private final static int MESSAGE_UPDATE_ONBOARDING_START = 10000;
    private final static int MESSAGE_UPDATE_ONBOARDING_PROGRESS = 10001;
    private final static int MESSAGE_UPDATE_ONBOARDING_DONE = 10002;
    private final static int MESSAGE_UPDATE_VERIFY_START = 10003;
    private final static int MESSAGE_UPDATE_VERIFY_PROGRESS = 10004;
    private final static int MESSAGE_UPDATE_VERIFY_DONE = 10005;

    private final static int MESSAGE_INCREMENT_PROGRESS = 20000;

    // there are 5 states in each onboarding process
    private final int ONBOARDING_STATE_COUNT = 5;

    // there are 20 parts in each state
    private final int ONBOARDING_STATE_PARTS_COUNT = 20;

    private Context mContext;

    private ArrayList<RoomHubData> mLstRoomHub = new ArrayList<RoomHubData>();
    private ArrayList<String> mRoomHubMac;
    private ArrayList<RegisterDevice> mRegisterLists = new ArrayList<>();

    private int mWifiTimeout;

    //private final static int MAC_FIXED_LEN = 6;

    private enum OnBoardingVerifyType {
        ALL,
        RECONNECT_FAIL
    }

    private OnBoardingVerifyType mVerifyType = OnBoardingVerifyType.ALL;
    //private OnBoardee mReconnectOnBoardee;

    private ProgressRunnable mProgressRunnable;

    private final int DIALOG_ID_BACK_EXIT = 100;
    private final int DIALOG_ID_NEXT_STEP = 200;

    private Handler mOBHandler = new Handler() {
        public void handleMessage(Message msg) {
            String errString;
            switch (msg.what) {
                case PROCESS_DO_DEMO:
                    new Thread() {
                        public void run() {
                            try {
                                sleep(6000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                Message msg = new Message();
                                msg.what = PROCESS_DO_GET_ROOMHUBLIST;//PROCESS_DO_GET_ROOMHUBLIST_DEMO;
                                mOBHandler.sendMessage(msg);
                            }
                        }
                    }.start();

                    break;
                case PROCESS_DO_SCAN:
                    mOBManager.scan();
                    break;
                case PROCESS_DO_STARTONBOARDING:
                    sendEmptyMessage(MESSAGE_UPDATE_ONBOARDING_START);
                    mJoinedClientCount = 0;
                    mJoinedFailedClientCount = 0;
                    mOBManager.startOnBoarding(mOBManager.ONBOARDING_POLICY_1, getResources().getInteger(R.integer.config_onboardee_connect_timeout),
                            getResources().getInteger(R.integer.config_onboarding_target_timeout));
                    break;
                case PROCESS_DO_CHECKING_WIFI_STATUS:
                    log("--- PROCESS_DO_CHECKING_WIFI_STATUS ---");
//                    Toast.makeText(mContext, "Checking wifi connection status", Toast.LENGTH_SHORT);
                    sendEmptyMessage(MESSAGE_UPDATE_VERIFY_START);
                    checkWifiStatus();
                    break;
                case PROCESS_DO_GET_ROOMHUBLIST:
                    log("--- PROCESS_DO_GET_ROOMHUBLIST ---");
                    mIsOnBoarding = false;
                    getActionBar().show();
                    connectLayout.setVisibility(View.GONE);
                    startLayout.setVisibility(View.GONE);
                    resultLayout.setVisibility(View.VISIBLE);
                    getSuccessList();//mRoomhubManager.getUnBindingList();
                    log("total: "+mClientList.length);
                    log("success: "+mLstRoomHub.size());
                    log("fail: " + mClientFailedList.size());
                    ListView listviewRoomHub = (ListView) findViewById(R.id.lstRoomhub);

                    if(listviewRoomHub.getCount() > 0 && mLstRoomHub.size() == 0)
                        listviewRoomHub.setAdapter(null);
                    else {
                        if(listviewRoomHub.getAdapter() != null)
                            listviewRoomHub.invalidateViews();
                        else
                            listviewRoomHub.setAdapter(new RoomhubItemAdapter(mContext, mLstRoomHub));
                    }

                    // Set Failed ListView data
                    if(mClientFailedList != null && mClientFailedList.size() > 0) {
                        updateFailItemView(true);
                    }
                    else {
                        updateFailItemView(false);
                    }

                    final boolean bFlag = getResources().getBoolean(R.bool.config_force_display_skip_for_debug);
                    final boolean bProcess =  msg.getData().getBoolean("process");
                    if(bFlag == true || mLstRoomHub.size() == 0) {
                        btnNext.setText(getString(R.string.start_button_name));
                    } else {
                        btnNext.setText(getString(R.string.next_str));
                    }
                    btnNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(mClientFailedList.size() > 0)
                                showExitConfirmDialog(DIALOG_ID_NEXT_STEP);
                            else
                                sendEmptyMessage(PROCESS_ONBOARDING_DONE_GO_NEXT);
                        }
                    });
                    break;
                case PROCESS_ONBOARDING_DONE_GO_NEXT:
                    final boolean forceSkip = getResources().getBoolean(R.bool.config_force_display_skip_for_debug);
                    if(forceSkip == true || (mLstRoomHub.size() == 0)) {
                        setPrvision();
                        startNextActivity();
                    } else {
                        startSetCityActivity();
                    }
                    break;
                case PROCESS_DO_GET_ROOMHUBLIST_DEMO:
                    mLstRoomHub = mRoomhubManager.getUnBindingList();
                    ListView listviewRoomHub1 = (ListView) findViewById(R.id.lstRoomhub);
                    listviewRoomHub1.setAdapter(new RoomhubItemAdapter(mContext, mLstRoomHub));
                    btnNext.setText(getString(R.string.start_button_name));
                    btnNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setPrvision();
                            startNextActivity();
                        }
                    });
                    break;
                case PROCESS_DO_FINISH_NO_ROOMHUB:
                    btnNext.setText(getString(R.string.start_button_name));
                    btnNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setPrvision();
                            startNextActivity();
                        }
                    });
                    break;
                case PROCESS_DO_SHOW_ERROR:
                    //Toast.makeText(mContext, "Client " + msg.getData().getString("ssid") + " joined failed! " + msg.getData().getString("error_str"), Toast.LENGTH_SHORT).show();
                    break;

                case PROCESS_RECONNECT_CHECK_STATUS: {
                    if(checkOnBoardeeStatus() == true) {
                        retryLayout.setVisibility(View.GONE);
                        resultLayout.setVisibility(View.VISIBLE);
                        refreshListView();
                    }
                    else {
                        if(getResources().getBoolean(R.bool.config_onboarding_do_reconnect))
                            sendMessage(obtainMessage(PROCESS_DO_FAIL_RECONNECT));
                    }
                }
                    break;

                /*
                case PROCESS_DO_RECONNECT: {
                    Log.d(TAG,"PROCESS_DO_RECONNECT");
                    mCount=1;
                    //mVerifyType = OnBoardingVerifyType.RECONNECT_ONE;
                    mJoinedFailedClientCount--;
                    OnBoardee onBoardee = (OnBoardee)msg.obj;
                    OnBoardee[] client = new OnBoardee[1];
                    client[0] = onBoardee;
                    mReconnectOnBoardee = onBoardee;
                    removeOnBoardeeFromFailList(onBoardee);
                    sendMessage(obtainMessage(MESSAGE_UPDATE_ONBOARDING_START, true));
                    mOBManager.startOnBoarding(client, mContext.getResources().getInteger(R.integer.config_onboardee_connect_timeout),
                            mContext.getResources().getInteger(R.integer.config_onboarding_target_timeout));
                }
                    break;
                    */

                case PROCESS_DO_FAIL_RECONNECT: {
                    log("--- PROCESS_DO_FAIL_RECONNECT --- mClientFailedList=" + mClientFailedList.size());
                    mVerifyType = OnBoardingVerifyType.RECONNECT_FAIL;
                    mCount = 0;
                    sendMessage(obtainMessage(MESSAGE_UPDATE_ONBOARDING_START, true));
                    OnBoardee[] failList = mClientFailedList.toArray(new OnBoardee[mClientFailedList.size()]);
                    mOBManager.startOnBoarding(failList, mContext.getResources().getInteger(R.integer.config_onboardee_connect_timeout),
                            mContext.getResources().getInteger(R.integer.config_onboarding_target_timeout));
                }
                    break;

                case MESSAGE_UPDATE_ONBOARDING_START:
                    log("--- MESSAGE_UPDATE_ONBOARDING_START ---");
                    mIsOnBoarding=true;
                    getActionBar().hide();
                    retryLayout.setVisibility(View.GONE);
                    resultLayout.setVisibility(View.GONE);
                    startLayout.setVisibility(View.VISIBLE);
                    scanStartLayout.setVisibility(View.VISIBLE);
                    scanProgressLayout.setVisibility(View.GONE);
//                    connectStatusNumberText.setVisibility(View.VISIBLE);

                    boolean doReconnect = false;
                    int total = mClientList.length;
                    if(msg.obj != null) {
                        doReconnect = (boolean)msg.obj;
                    }
                    else if(mVerifyType == OnBoardingVerifyType.RECONNECT_FAIL){
                        doReconnect = true;
                    }

                    int connect_index = ++mCount;
                    String string = getString(R.string.onboarding_progress, connect_index);
                    if(doReconnect) {
                        total = mClientFailedList.size();
                    }
                    String connect_status = connect_index+"/"+total;

                    log("doReconnect=" + doReconnect + ",string=" + string+",num="+connect_status);

                    if(connectLayout.getVisibility()==View.GONE) {
                        scanLayout.setVisibility(View.GONE);
                        connectLayout.setVisibility(View.VISIBLE);
                        //log("MESSAGE_UPDATE_ONBOARDING_START ,total=" + total + ",max=" + total * ONBOARDING_STATE_COUNT * ONBOARDING_STATE_PARTS_COUNT);
                        connectProgress.setMax(total * ONBOARDING_STATE_COUNT * ONBOARDING_STATE_PARTS_COUNT);
                    }
                    connectStatusText.setText(string);
                    connectStatusNumberText.setText(connect_status);
                    {
                        int current = connectProgress.getProgress();
                        int progress = (mCount - 1) * ONBOARDING_STATE_COUNT * ONBOARDING_STATE_PARTS_COUNT;

                        log("MESSAGE_UPDATE_ONBOARDING_START ,mCount=" + mCount + ",current=" + current + ",progress=" + progress);
                        connectProgress.setProgress(progress);
                    }

                    break;
                case MESSAGE_UPDATE_ONBOARDING_PROGRESS: {
                    log("--- MESSAGE_UPDATE_ONBOARDING_PROGRESS ---");
                    int state = (int) msg.obj;

                    if(mProgressRunnable != null && mProgressRunnable.isRunning()) {
                        mProgressRunnable.stop();
                        removeCallbacks(mProgressRunnable);
                        mProgressRunnable = null;
                    }

                    int current=0, progress=0;
                    if(state > 1) {
                        current = connectProgress.getProgress();
                        progress = (mCount-1)*ONBOARDING_STATE_COUNT*ONBOARDING_STATE_PARTS_COUNT+(state-1)*ONBOARDING_STATE_PARTS_COUNT;

                        log("MESSAGE_UPDATE_ONBOARDING_PROGRESS ,state=" + state + ",current=" + current + ",progress=" + progress);
                        connectProgress.setProgress(progress);
                    }

                    mProgressRunnable = new ProgressRunnable();
                    post(mProgressRunnable);
                }
                    break;
                case MESSAGE_UPDATE_ONBOARDING_DONE:
                    log("--- MESSAGE_UPDATE_ONBOARDING_DONE ---");
                    connectProgress.setProgress(connectProgress.getMax());
                    break;
                case MESSAGE_UPDATE_VERIFY_START:
                    log("--- MESSAGE_UPDATE_VERIFY_START ---");
                    final int sec_onboarding_timeout =  getResources().getInteger(R.integer.config_onboarding_reconnect_timeout) / 1000;
                    connectProgress.setProgress(0);
                    connectProgress.setMax(sec_onboarding_timeout);
//                    connectStatusNumberText.setVisibility(View.GONE);
                    connectStatusText.setVisibility(View.VISIBLE);
                    connectStatusText.setText(getString(R.string.onboarding_verifying));
                    break;
                case MESSAGE_UPDATE_VERIFY_PROGRESS: {
                    int progress = (int) msg.obj;
                    connectProgress.setProgress(progress);
                }
                    break;
                case MESSAGE_UPDATE_VERIFY_DONE:
                    if(mProgressRunnable != null && mProgressRunnable.isRunning()) {
                        mProgressRunnable.stop();
                        removeCallbacks(mProgressRunnable);
                        mProgressRunnable = null;
                    }

                    connectStatusText.setText(getString(R.string.onboarding_done));
                    if(connectProgress.getProgress()<connectProgress.getMax())
                        connectProgress.setProgress(connectProgress.getMax());

                    boolean delayDimissDialog = (boolean)msg.obj;
                    if(delayDimissDialog) {
                        sendMessageDelayed(obtainMessage(MESSAGE_UPDATE_VERIFY_DONE, false), 2000);
                        return;
                    }

                    break;
                case MESSAGE_INCREMENT_PROGRESS:
                    //Log.d(TAG,"MESSAGE_INCREMENT_PROGRESS enter");
                    connectProgress.setProgress(connectProgress.getProgress() + 1);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void removeOnBoardeeFromFailList(OnBoardee client) {
        OnBoardee target = null;
        for(OnBoardee onBoardee: mClientFailedList) {
            if(client.getSsid().equals(onBoardee.getSsid())) {
                target = onBoardee;
                break;
            }
        }
        if(target != null) {
            mClientFailedList.remove(target);
        }
    }

    private class ProgressRunnable implements Runnable {
        boolean run = true;
        int cnt = 0;

        public synchronized boolean isRunning() {
            return run;
        }

        public synchronized void stop() {
            run = false;
        }

        @Override
        public void run() {
            if(cnt++ < ONBOARDING_STATE_PARTS_COUNT && run) {
                if(connectProgress.getProgress()>=connectProgress.getMax())
                    return;
                connectProgress.setProgress(connectProgress.getProgress()+1);
                mOBHandler.postDelayed(this, 1000);
            }
        }
    }

    private String getOnBoardeeSsid(OnBoardee onBoardee) {
        String clientSSID = onBoardee.getSsid();
        clientSSID = clientSSID.substring((clientSSID.length() - getResources().getInteger(R.integer.config_mac_fixed_len)) > 0 ? clientSSID.length() - getResources().getInteger(R.integer.config_mac_fixed_len) : 0);
        return clientSSID.toUpperCase();
    }

    private RoomHubData getRoomHubByMatchingSsid(String ssid) {
        ArrayList<RoomHubData> allList = mRoomhubManager.getRoomHubDataList(false);

        for(RoomHubData data: allList) {
            String deviceName = data.getName();
            deviceName = deviceName.substring((deviceName.length() - getResources().getInteger(R.integer.config_mac_fixed_len)) > 0 ? deviceName.length() - getResources().getInteger(R.integer.config_mac_fixed_len) : 0);
            deviceName = deviceName.toUpperCase();
            if(ssid.equals(deviceName)) {
                return data;
            }
        }
        return null;
    }

    private void refreshListView() {
        ListView listviewRoomHub = (ListView) findViewById(R.id.lstRoomhub);
        //ListView listviewRoomHubFailed = (ListView) findViewById(R.id.lstRoomhubFailed);

        listviewRoomHub.invalidateViews();
        //listviewRoomHubFailed.invalidateViews();
        updateFailItemView(true);
    }

    private void updateFailItemView(boolean visible) {
        View failListView = findViewById(R.id.onboarding_failItemLayout);
        if(visible) {
            failListView.setVisibility(View.VISIBLE);
            TextView title = (TextView) failListView.findViewById(R.id.onboarding_fail_msg);
            String fail_msg = getString(R.string.onboarding_fail_item_msg, mClientFailedList.size());
            title.setText(fail_msg);
            TextView reconnect = (TextView)failListView.findViewById(R.id.onboarding_fail_reconnect);
            reconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resultLayout.setVisibility(View.GONE);
                    retryLayout.setVisibility(View.VISIBLE);
                    getActionBar().setTitle(R.string.onboarding_retry);
//                    mOBHandler.sendEmptyMessage(PROCESS_RECONNECT_CHECK_STATUS);
                }
            });
        }
        else {
            failListView.setVisibility(View.GONE);
        }
    }

    private boolean checkOnBoardeeStatus() {
        boolean found = false;

        ArrayList<OnBoardee> removeList = new ArrayList<>();
        for(OnBoardee onBoardee:mClientFailedList) {
            // 1. check previous onboarding process state
            ClientState clientState = mClientJoinedList.get(onBoardee.getSsid());
            log("checkOnBoardeeStatus reconnect onBoardee:"+onBoardee.getSsid());
            log("checkOnBoardeeStatus previous onBoarding process state="+clientState.state);
            if(clientState.state > 2) {
                log("checkOnBoardeeStatus previous onBoarding process might be done!");
            }

            // 2. search in the latest Device list
            String clientSSID = getOnBoardeeSsid(onBoardee);

            log("checkOnBoardeeStatus clientSSID="+clientSSID);
            log("checkOnBoardeeStatus mLstRoomHub size=" + mLstRoomHub.size());
            //log("checkOnBoardeeStatus mClientFailedList size="+mClientFailedList.size());

            RoomHubData roomHubData = getRoomHubByMatchingSsid(clientSSID);
            if(roomHubData != null) {
                // add to success list
                mLstRoomHub.add(roomHubData);
                if(mRoomHubMac == null)
                    mRoomHubMac = new ArrayList<String>();

                mRoomHubMac.add(clientSSID);
                mClientJoinedList.get(onBoardee.getSsid()).setResult(true);

                removeList.add(onBoardee);
                /*
                for (OnBoardee client : mClientFailedList) {
                    String ssid = getOnBoardeeSsid(client);
                    if (ssid.equals(clientSSID)) {
                        mClientFailedList.remove(client);
                        break;
                    }
                }
                */
            }
            else
                log("checkOnBoardeeStatus this onBoardee not found in device list!");

        }

        // remove from fail list
        for(OnBoardee client:removeList) {
            mClientFailedList.remove(client);
            mJoinedFailedClientCount--;
        }

        found = removeList.size()>0?true:false;

        log("checkOnBoardeeStatus >>found="+found);
        log("checkOnBoardeeStatus >>mLstRoomHub size=" + mLstRoomHub.size());
        log("checkOnBoardeeStatus >>mClientFailedList size=" + mClientFailedList.size());

        return found;
    }

    private boolean checkCurrentOnBoardingStatus() {
        int found = 0;
        int verify_numbers = 0;

        if(mVerifyType == OnBoardingVerifyType.RECONNECT_FAIL) {
            verify_numbers = mClientFailedList.size();
            for(OnBoardee onBoardee: mClientFailedList) {
                String clientSSID = getOnBoardeeSsid(onBoardee);
                if(getRoomHubByMatchingSsid(clientSSID) != null) {
                    found++;
                }
            }
        }
        else {  // ALL
            verify_numbers = mClientList.length;
            for (int i = 0; i < mClientList.length; i++) {
                String clientSSID = getOnBoardeeSsid(mClientList[i]);
                if (getRoomHubByMatchingSsid(clientSSID) != null) {
                    found++;
                }
            }
        }

        log("checkCurrentOnBoardingStatus devices found=" + found + ", verify_numbers=" + verify_numbers);

        if(found == verify_numbers)
            return true;

        return false;
    }

    private void getSuccessList() {
        boolean[] joinedList = new boolean[mClientList.length];

        if(mVerifyType == OnBoardingVerifyType.RECONNECT_FAIL) {
            ArrayList<OnBoardee> removeList = new ArrayList<>();
            for(OnBoardee failOnBoardee:mClientFailedList) {
                RoomHubData roomHubData = getRoomHubByMatchingSsid(getOnBoardeeSsid(failOnBoardee));
                if(roomHubData != null) {
                    removeList.add(failOnBoardee);
                    // Add onBoardee to success list
                    for (OnBoardee onBoardee : mClientList) {
                        if (failOnBoardee.getSsid().equals(onBoardee.getSsid())) {
                            mLstRoomHub.add(roomHubData);
                            if(mRoomHubMac == null)
                                mRoomHubMac = new ArrayList<String>();
                            mRoomHubMac.add(getOnBoardeeSsid(failOnBoardee));
                        }
                    }
                }
            }

            // remove from fail list
            if(removeList.size() > 0) {
                for(OnBoardee client:removeList) {
                    mClientFailedList.remove(client);
                    mJoinedFailedClientCount--;
                }
            }
            /*
            RoomHubData roomHubData = getRoomHubByMatchingSsid(getOnBoardeeSsid(mReconnectOnBoardee));
            if(roomHubData != null) {
                // Add onBoardee to success list
                for (OnBoardee onBoardee : mClientList) {
                    if (mReconnectOnBoardee.getSsid().equals(onBoardee.getSsid())) {
                        mLstRoomHub.add(roomHubData);
						if(mRoomHubMac == null)
				            mRoomHubMac = new ArrayList<String>();
	                    mRoomHubMac.add(getOnBoardeeSsid(mReconnectOnBoardee));
                        break;
                    }
                }
            }
            else {
                // Add onBoardee to failed list
                mClientFailedList.add(mReconnectOnBoardee);
            }
            */
        }
        else {  // VERIFY ALL:
            mJoinedFailedClientCount = 0;
            mRoomHubMac = new ArrayList<String>();
            for (int i = 0; i < mClientList.length; i++) {
                boolean bFlag = false;
                String clientSSID = getOnBoardeeSsid(mClientList[i]);
                RoomHubData roomHubData = getRoomHubByMatchingSsid(clientSSID);
                if (roomHubData != null) {
                    mLstRoomHub.add(roomHubData);
                    mRoomHubMac.add(clientSSID);
                    mClientJoinedList.get(mClientList[i].getSsid()).setResult(true);
                    joinedList[i] = true;
                    bFlag = true;
                }
                if (bFlag == false) {
                    mJoinedFailedClientCount++;
                    joinedList[i] = false;
                }
            }

            // set failed list
            int iIndex = 0;
            if (mJoinedFailedClientCount > 0) {
                for (int i = 0; i < mClientList.length; i++) {
                    if (joinedList[i] == false)
                        mClientFailedList.add(mClientList[i]);
                }
            }
        }
    }

    private void initViews() {
        startLayout = findViewById(R.id.onboarding_startLayout);
        resultLayout = findViewById(R.id.onboarding_resultLayout);
        retryLayout = findViewById(R.id.onboarding_retryLayout);
        scanLayout = startLayout.findViewById(R.id.onboading_scanLayout);
        connectLayout = startLayout.findViewById(R.id.onboading_connectLayout);

        scanStartLayout = scanLayout.findViewById(R.id.scan_startLayout);
        scanProgressLayout = scanLayout.findViewById(R.id.scan_progressLayout);

        scanResultText = (TextView)scanProgressLayout.findViewById(R.id.scan_result_text);
        scanProgress = (ArcProgress)scanProgressLayout.findViewById(R.id.scan_progress);

        connectStatusText = (TextView)connectLayout.findViewById(R.id.connect_status_text);
        connectStatusNumberText = (TextView)connectLayout.findViewById(R.id.connect_status_number_text);
        connectProgress = (ArcProgress)connectLayout.findViewById(R.id.connect_progress);

        btnNext = (Button) resultLayout.findViewById(R.id.btnNext);
        btnRestart = (Button) retryLayout.findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOBHandler.sendEmptyMessage(PROCESS_RECONNECT_CHECK_STATUS);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.onboarding);
//        setContentView(R.layout.activity_onboarding);
        setContentView(R.layout.activity_onboarding_scan);

        initViews();
        getActionBar().hide();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        getData(bundle);

        mContext = this;

        mWifiTimeout = getResources().getInteger(R.integer.config_wifi_ap_connect_timeout);

        mOBManager = getOnBoardingManager();
        mOBManager.registerForOnBoardingStateChanged(this);
        mOBManager.registerForScanStateChanged(this);

        mRoomhubManager = getRoomHubManager();

        // disable LocationManager
        //getLocationManager().disable();

        Utils.setIsOnBoarding(this, true);

        Message msg = new Message();
        msg.what = PROCESS_DO_SCAN;//PROCESS_DO_DEMO;
        mOBHandler.sendMessage(msg);

        /*
         * acquire and held wakelock here and release in onDestroy
         * no need to consider power consumption for temporary
          */
        Utils.acquireOnBoardingWakeLock(this);
        /*
        * keep screen on
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toast.makeText(this, R.string.onboarding_warning_message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // enable LocationManager
        //getLocationManager().enable();

        Utils.setIsOnBoarding(this, false);

        mOBManager.unRegisterForOnBoardingStateChanged(this);
        mOBManager.unRegisterForScanStateChanged(this);

        /*
        * OnBoarding is done, release wakelock;
         */
        Utils.releaseOnBoardingWakeLock();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        log("onActivityResult enter");
        //super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if(mIsOnBoarding)
            return;

        if(mClientList != null && mClientList.length > 0)
            showExitConfirmDialog(DIALOG_ID_BACK_EXIT);


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void showExitConfirmDialog(int dialogId) {
        if(dialogId == DIALOG_ID_BACK_EXIT) {
            final Dialog dialog = new Dialog(this,R.style.CustomDialog);

            dialog.setContentView(R.layout.custom_dialog_onboarding_comfirm);

            TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
            txt_msg.setText(getString(R.string.onboarding_done_exit_msg));

            Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
            btn_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    Intent intent = new Intent(mContext, RoomHubMainPage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });

            Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
            btn_no.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.setCancelable(false);
            dialog.show();
        }
        else if(dialogId == DIALOG_ID_NEXT_STEP) {
            final Dialog dialog = new Dialog(this,R.style.CustomDialog);

            dialog.setContentView(R.layout.custom_dialog_onboarding_comfirm);

            TextView txt_msg=(TextView) dialog.findViewById(R.id.txt_message);
            txt_msg.setText(getString(R.string.onboarding_has_fail_to_next));

            Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
            btn_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOBHandler.sendEmptyMessage(PROCESS_ONBOARDING_DONE_GO_NEXT);
                    dialog.dismiss();
                }
            });

            Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
            btn_no.setVisibility(View.GONE);
            dialog.setCancelable(false);
            dialog.show();
        }

    }

    private void checkWifiStatus() {
        final int sec = mWifiTimeout / 1000;
        final int sec_onboarding_timeout =  getResources().getInteger(R.integer.config_onboarding_reconnect_timeout) / 1000;

        new Thread() {
            public void run() {
                int cnt = 0;
                boolean bConnect = false;
                while (cnt < sec) {
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (networkInfo.isConnected()) {
                        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                        if (connectionInfo != null) {
                            String ssid = connectionInfo.getSSID();
                            ssid = ssid.replaceAll("\"", "");
                            if (mSSID.equals(ssid)) {
                                bConnect = true;
                                break;
                            }
                        }
                    }
                    try {
                        sleep(1000);
                        cnt++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(!bConnect)
                    log("Check wifi connection status failed!!!");
                /*if(!bConnect) {
                                            Message msg = new Message();
                                            msg.what = PROCESS_DO_FINISH_NO_ROOMHUB;
                                            mOBHandler.sendMessage(msg);
                                        } else {*/
                cnt = 0;
                    log("Start to wait one minute!!!");
                    boolean delayDismissDialog = false;
                    while (cnt < sec_onboarding_timeout) {
                        try {
                            sleep(1000);
                            cnt++;
                            if(cnt%5 == 0) {
                                if(checkCurrentOnBoardingStatus() == true) {
                                    delayDismissDialog = true;
                                    log("All clients are found! No need to wait!!!");
                                    break;
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mOBHandler.sendMessage(mOBHandler.obtainMessage(MESSAGE_UPDATE_VERIFY_PROGRESS,cnt));
                    }
                    mOBHandler.sendMessage(mOBHandler.obtainMessage(MESSAGE_UPDATE_VERIFY_DONE,delayDismissDialog));
                    log("End to wait one minute!!!");
                    Message msg = new Message();
                    msg.what = PROCESS_DO_GET_ROOMHUBLIST;
                    Bundle data = new Bundle();
                    if(mJoinedClientCount > 0)
                        data.putBoolean("process", true);
                    else
                        data.putBoolean("process", false);
                    msg.setData(data);
                    mOBHandler.sendMessageDelayed(msg,3000);
//                    mOBHandler.sendMessage(msg);
                //}
            }
        }.start();
    }

    private void startSetCityActivity() {
        Intent intent = new Intent(this, CityChoiceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mRegisterLists.clear();
        for(RoomHubData roomHubData: mLstRoomHub) {
            mRegisterLists.add(new RegisterDevice(roomHubData.getUuid(),roomHubData.getName(), roomHubData.getVersion()));
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("DeviceList", mRegisterLists);
        bundle.putStringArrayList("DeviceMacList", mRoomHubMac);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    private void startNextActivity() {
        Intent intent = new Intent(OnBoardingActivity.this, RoomHubMainPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        //System.exit(0);
    }

    private void setPrvision() {
        PreferenceEditor pref = new PreferenceEditor(this, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        pref.setIntValue(GlobalDef.ROOMHUB_SETTINGS_PROVISION, PROVISION_SET);
    }

    private void getData(Bundle bundle) {
        mSSID = bundle.getString(GlobalDef.WIFI_AP_SSID);
        mSecurity = bundle.getInt(GlobalDef.WIFI_AP_SECURITY);
        mPskType = (GlobalDef.PskType)bundle.getSerializable(GlobalDef.WIFI_AP_PSKTYPE); // only security = SECURITY_PSK
        mPassword = bundle.getString(GlobalDef.WIFI_AP_PASSWORD);
    }

    /*
    private class WifiStateReceiver extends BroadcastReceiver {
        WifiManager mWifiManager;
        WifiStateReceiver() {
            mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                Log.d(TAG, "--- WifiManager.NETWORK_STATE_CHANGED_ACTION ---");
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, "WifiNetworkInfo: " + networkInfo.toString());
                int wifiState = mWifiManager.getWifiState();
                Log.d(TAG, "wifistate: " + wifiState);
                if (wifiState == WifiManager.WIFI_STATE_ENABLED && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        Utils.bindProcessToWiFiNetwork(mContext, true);
                        Log.d(TAG, "bindProcessToWiFiNetwork");
                    unregisterReceiver(this);
                }
            }
        }
    }
    */

    @Override
    public void onBoardingStart() {

    }

    @Override
    public void onBoardingProgress(OnBoardee client, int state) {
        log("onBoardingProgress onBoardee:"+client.getSsid()+",state=" + state);

//        if(Build.VERSION.SDK_INT >= 23) {
//            if (state == 2) {
//                boolean ret = Utils.bindProcessToWiFiNetwork(this, true);
//                log("onBoardingProgress bindProcessToWiFiNetwork ret=" + ret);
//            }
//        }

        mClientJoinedList.get(client.getSsid()).setState(state);

        mOBHandler.sendMessage(mOBHandler.obtainMessage(MESSAGE_UPDATE_ONBOARDING_PROGRESS, state));
    }

    @Override
    public void onBoardingStop() {
        log("onBoardingStop enter");
//        if(Build.VERSION.SDK_INT >= 23) {
//            boolean ret = Utils.bindProcessToWiFiNetwork(this, true);
//            log("onBoardingStop bindProcessToWiFiNetwork ret=" + ret);
//            if (ret == false) {
//                WifiStateReceiver mWifiReceiver = new WifiStateReceiver();
//                IntentFilter wifiFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//                registerReceiver(mWifiReceiver, wifiFilter);
//            }
//        }

        Message msg = new Message();
        msg.what = PROCESS_DO_CHECKING_WIFI_STATUS;//PROCESS_DO_GET_ROOMHUBLIST;
        mOBHandler.sendMessageDelayed(msg, 2000);
        if(mJoinedClientCount <= 0) {
            log("onBoarding joinedClient count = 0");
        }
    }

    @Override
    public void onClientConnect(int errorCode, OnBoardee client) {

    }

    @Override
    public void onClientJoined(int errorCode, OnboardingManager.OnboardingErrorType detailError, OnBoardee client) {
        log("onClientJoined enter onBoardee:"+client.getSsid());

        mOBHandler.sendMessage(mOBHandler.obtainMessage(MESSAGE_UPDATE_ONBOARDING_PROGRESS, 5));

        if(errorCode == mOBManager.ONBOARDING_SUCCESS) {
            mJoinedClientCount++;
            mClientJoinedList.get(client.getSsid()).setResult(true);
        } else {
            mJoinedFailedClientCount++;
            // add it to fail list when final verification
            /*
            Message msg = new Message();
            msg.what = PROCESS_DO_SHOW_ERROR;
            Bundle data = new Bundle();
            data.putString("ssid", client.getSsid());
            data.putString("error_str", detailError.toString());
            msg.setData(data);
            mOBHandler.sendMessage(msg);
            */
        }
    }

    @Override
    public void onClientJoinedEnd(OnBoardee client) {
        int total = mClientList.length;
        if(mVerifyType == OnBoardingVerifyType.RECONNECT_FAIL)
            total = mClientFailedList.size();
        log("onClientJoinedEnd enter onBoardee:" + client.getSsid() + ",mCount=" + mCount + ",total=" + total);
        if(mCount < total)
            mOBHandler.sendEmptyMessage(MESSAGE_UPDATE_ONBOARDING_START);
        else
            mOBHandler.sendEmptyMessage(MESSAGE_UPDATE_ONBOARDING_DONE);
    }

    @Override
    public void onScanStart() {

    }

    @Override
    public void onScanStop(int errorCode, OnBoardee[] clients) {
        log("onScanStop enter");
        Message msg = new Message();

        log("onScanStop errorCode=" + errorCode + ", onBoardees num=" + clients.length);

        scanStartLayout.setVisibility(View.GONE);
        scanProgressLayout.setVisibility(View.VISIBLE);

        if(errorCode == mOBManager.ONBOARDING_SCAN_SUCCESS) {
            mClientList = clients;
            //mClientJoinedList = new boolean[clients.length];

            //for(int i = 0; i < clients.length; i++)
            //    mClientJoinedList[i] = false;
            for(OnBoardee onBoardee: clients) {
                ClientState state = new ClientState();
                state.setResult(false);
                state.setState(0);
                mClientJoinedList.put(onBoardee.getSsid(),state);
            }

            if(clients.length > 0) {
                msg.what = PROCESS_DO_STARTONBOARDING;
                scanResultText.setText(getString(R.string.onboarding_scan_result, clients.length));
            }
            else {
                msg.what = PROCESS_DO_GET_ROOMHUBLIST;
                Toast.makeText(this,R.string.no_device_found,Toast.LENGTH_SHORT).show();
            }

        } else {
            msg.what = PROCESS_DO_CHECKING_WIFI_STATUS;//PROCESS_DO_FINISH_NO_ROOMHUB;
        }

        if(msg.what==PROCESS_DO_STARTONBOARDING)
            mOBHandler.sendMessageDelayed(msg,2000);
        else
            mOBHandler.sendMessage(msg);
    }

    private class RoomhubItemAdapter extends BaseAdapter {
        private Context mContext;
        ArrayList<RoomHubData> mList;
        private LayoutInflater inflater = null;
        private boolean mLED = false;

        public RoomhubItemAdapter(Context context, ArrayList<RoomHubData> data) {
            mContext = context;
            mList = data;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private void setData(View view, int pos) {
            ImageView imgLed = (ImageView) view.findViewById(R.id.imgLed);
            imgLed.setTag(R.id.tag_first, pos);
            imgLed.setTag(R.id.tag_second, view);
            imgLed.setOnTouchListener(touchListener);

            TextView txtRoomhubName = (TextView) view.findViewById(R.id.txtRoomhubName);
            txtRoomhubName.setText(mList.get(pos).getName());
            txtRoomhubName.setTag(R.id.tag_first, pos);
            txtRoomhubName.setTag(R.id.tag_second, view);
            txtRoomhubName.setOnTouchListener(touchListener);

            TextView txtRename = (TextView) view.findViewById(R.id.txtRename);
            txtRename.setTag(R.id.tag_first, pos);
            txtRename.setTag(R.id.tag_second, view);
            txtRename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (int) v.getTag(R.id.tag_first);
                    View viewCur = (View) v.getTag(R.id.tag_second);
                    ShowEditAlertDialog(pos, viewCur);
                }
            });
        }

        private View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int pos = (int) v.getTag(R.id.tag_first);
                View viewCur = (View) v.getTag(R.id.tag_second);
                ImageView imageLed = (ImageView) viewCur.findViewById(R.id.imgLed);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    imageLed.setImageResource(R.drawable.btn_led);
                    if(mLED == false) {
                        mRoomhubManager.setLed(mList.get(pos).getUuid(), RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_ON, 0, 0, 0);
                        mLED = true;
                    }
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_OUTSIDE ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    imageLed.setImageResource(R.drawable.btn_led_off);
                    mRoomhubManager.setLed(mList.get(pos).getUuid(), RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_OFF, 0, 0, 0);
                    mLED = false;
                    return true;
                }
                //imageLed.setImageResource(R.drawable.btn_led_off);
                return true;
            }
        };

        private void ShowEditAlertDialog(int pos, View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            final EditText input = new EditText(mContext);
            input.setText(mList.get(pos).getName());
            final String uuid = mList.get(pos).getUuid();
            final ImageView imgCheckOK = (ImageView) view.findViewById(R.id.imgRenameOK);
            final TextView txtRename = (TextView) view.findViewById(R.id.txtRename);
            final TextView txtRoomhubName = (TextView) view.findViewById(R.id.txtRoomhubName);
            builder
                    .setTitle(R.string.onboarding_roomhub_rename)
                    .setView(input)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String value = input.getText().toString();
                            if (input.getText().toString().trim().length() == 0) {
                                Toast.makeText(mContext, R.string.err_msg, Toast.LENGTH_SHORT).show();
                                input.requestFocus();
                            } else {
                                int ret = mRoomhubManager.modifiedDeviceName(uuid, input.getText().toString());
                                if(ret == ErrorKey.Success) {
                                    imgCheckOK.setVisibility(View.VISIBLE);
                                    txtRename.setVisibility(View.GONE);
                                    txtRoomhubName.setText(input.getText().toString());
                                } else {
                                    Toast.makeText(mContext, "Rename failed", Toast.LENGTH_SHORT).show();
                                }
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                dialog.dismiss();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                            dialog.dismiss();
                        }
                    });
            builder.show();
            input.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                //view = inflater.inflate(R.layout.roomhub_rename_item, null);
                view = inflater.inflate(R.layout.roomhub_rename_item, parent, false);

            setData(view, position);

            return view;
        }
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG, msg);
    }

    private class RoomhubFailItemAdapter extends BaseAdapter {
        private Context mContext;
        ArrayList<OnBoardee> mList;
        private LayoutInflater inflater = null;
        private boolean mLED = false;

        public RoomhubFailItemAdapter(Context context, ArrayList<OnBoardee> data) {
            mContext = context;
            mList = data;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private void setData(View view, int pos) {
            OnBoardee onBoardee = mList.get(pos);

            ImageView imgLed = (ImageView) view.findViewById(R.id.imgLed);
            imgLed.setVisibility(View.INVISIBLE);

            TextView txtRoomhubName = (TextView) view.findViewById(R.id.txtRoomhubName);
            txtRoomhubName.setText(onBoardee.getSsid());

            TextView txtReConnect = (TextView) view.findViewById(R.id.txtRename);
            txtReConnect.setText(getString(R.string.onboarding_roomhub_reconnect));
            txtReConnect.setTag(R.id.tag_first, pos);
            txtReConnect.setTag(R.id.tag_second, view);
            txtReConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (int) v.getTag(R.id.tag_first);
                    showProgressDialog("", mContext.getString(R.string.process_str));
                    mOBHandler.sendMessageDelayed(mOBHandler.obtainMessage(PROCESS_RECONNECT_CHECK_STATUS, mList.get(pos)), 5000);
                }
            });
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = inflater.inflate(R.layout.roomhub_rename_item, null);

            setData(view, position);

            return view;
        }
    }
}
