package com.quantatw.roomhub.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.listener.OnBoardingScanStateChangedListener;
import com.quantatw.roomhub.listener.OnBoardingStateChangedListener;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.PreferenceEditor;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.MiddlewareApi;

import org.alljoyn.about.AboutKeys;
import org.alljoyn.bus.AboutListener;
import org.alljoyn.bus.AboutObjectDescription;
import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.Variant;
import org.alljoyn.bus.alljoyn.DaemonInit;
import org.alljoyn.onboarding.OnboardingService;
import org.alljoyn.onboarding.sdk.OnboardingConfiguration;
import org.alljoyn.onboarding.sdk.OnboardingIllegalArgumentException;
import org.alljoyn.onboarding.sdk.OnboardingIllegalStateException;
import org.alljoyn.onboarding.sdk.OnboardingManager;
import org.alljoyn.onboarding.sdk.WiFiNetwork;
import org.alljoyn.onboarding.sdk.WiFiNetworkConfiguration;
import org.alljoyn.onboarding.sdk.WifiDisabledException;
import org.alljoyn.onboarding.transport.OnboardingTransport;
import org.alljoyn.services.android.security.AuthPasswordHandler;
import org.alljoyn.services.android.security.SrpAnonymousKeyListener;
import org.alljoyn.services.android.utils.AndroidLogger;
import org.alljoyn.services.common.utils.TransportUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by erin on 9/23/15.
 */
public class OnBoardingManager extends BaseManager implements AboutListener {
    private final String TAG="OnBoardingManager";
    private Resources mResources;
    private MiddlewareApi mApi;

    private WifiManager mWifiManager;

    private Thread mScanThread,mOnBoardingThread;
    private boolean mForceScan = false;
    private boolean isOnBoardingRunning = false;

    private Object mScanStargedGuard = new Object();
    private boolean isScanningInBackground = false;

    private Object mOnboardingJob = new Object();
    private int mCurrentStatus = ONBOARDING_INIT;
    private OnboardingManager.OnboardingErrorType mDetailError;
    private OnboardingManager.OnboardingState mCurrentState;
    private boolean mForceAbortOnboarding = false;
    private boolean isOffboarding = false;
    private int mCurrentPolicy = ONBOARDING_POLICY_1;

    private OnBoardee currentOnboardee = null;

    /**
     * Alljoyn bus attachment.
     */
    private static BusAttachment sBusAttachment = null;

    /**
     * String for Alljoyn daemon to be advertised with.
     */
    private static String sDaemonName = null;


    private ArrayList<OnBoardingScanStateChangedListener> mScanListener = new ArrayList<OnBoardingScanStateChangedListener>();
    private ArrayList<OnBoardingStateChangedListener> mOnBoardingListener = new ArrayList<OnBoardingStateChangedListener>();

    private final int MESSAGE_SCAN_DONE = 10;
    private final int MESSAGE_ONBOARDING_STATE_CONNECTING_ONBOARDEE_WIFI = 100;
    private final int MESSAGE_ONBOARDING_STATE_FINDING_ONBOARDEE = 101;
    private final int MESSAGE_ONBOARDING_STATE_CONNECTING_TARGET_WIFI = 102;
    private final int MESSAGE_ONBOARDING_STATE_VERIFYING_ONBOARDED = 103;

    public static final int ONBOARDING_SCAN_SUCCESS = 0;

    public static final int ERRORCODE_WIFI_IS_DISABLED = 100;

    // Reference Frank H60-onBoarding-flow-20151005-02.vsd
    // Home AP
    public static final int  ONBOARDING_POLICY_1  = 0;
    // Host to Home AP, the others with same SSID
    public static final int  ONBOARDING_POLICY_2  = 1;
    // Host to Home AP, the others with serial of SSID
    public static final int  ONBOARDING_POLICY_3  = 2;

    public static final int ONBOARDING_SUCCESS = 0;
    public static final int ONBOARDING_INIT = -1;
    public static final int ONBOARDING_ERROR = -100;

    public static final int ONBOARDING_WAIT_TIMEOUT = 90 * 1000; //90 seconds
    public static final int ONBOARDING_ABORT_TIMEOUT = 30 * 1000; //30 seconds

    /* OnBoarding State for onBoardingProgress() */
    public final static int ONBOARDING_STATE_CONNECTING_ONBOARDEE_WIFI = 1;
    public final static int ONBOARDING_STATE_FINDING_ONBOARDEE = 2;
    public final static int ONBOARDING_STATE_CONNECTING_TARGET_WIFI = 3;
    public final static int ONBOARDING_STATE_VERIFYING_ONBOARDED = 4;


    /**
     * Default password for daemon and the Alljoyn devices
     */
    private final static String DEFAULT_PINCODE = "000000";

    /**
     * List of AllJoyn devices the daemon is announced on.
     */
    List<Device> deviceList;

    /**
     * Unique prefix indicated that this daemon will be advertised quietly.
     */
    private static final String DAEMON_QUIET_PREFIX = "quiet@";

    /**
     *  SSID prefix for onboardable devices.
     *
     *  @deprecated Read from Resource config.xml - config_onboardee_ssid_prefix
     */
    @Deprecated
    private static final String ROOMHUB_ONBOARDABLE_PREFIX = "AJ_H60_"; // "H60AJ_";
    private static final String ROOMHUB_HOMEAP_PREFIX = "RoomHub_HomeAP";

    @Override
    public void startup() {

    }

    @Override
    public void terminate() {

    }

    /**
     * Class represents onboarded device.
     */
    public class Device {

        /**
         * Bus attachment name
         */
        public String serviceName;

        /**
         * Alljoyn port for this device
         */
        public short port;

        /**
         * This device unique application id, taken from metadataMap map.
         */
        public UUID appId;

        /**
         * This device friendly name, taken from metadataMap map.
         */
        public String name;

        public Device(String serviceName, short port, UUID appId, String name) {
            this.serviceName = serviceName;
            this.port = port;
            this.appId = appId;
            this.name = name;
        }

        public void update(String serviceName, short port, String name) {
            this.serviceName = serviceName;
            this.port = port;
            this.name = name;
        }
    }

    @Override
    public void announced(final String serviceName, final int version, final short port, final AboutObjectDescription[] objectDescriptions, final Map<String, Variant> serviceMetadata) {
        UUID appId;
        String deviceName;

        // serviceMetadata map analysis
        try {
            Map<String, Object> fromVariantMap = TransportUtil.fromVariantMap(serviceMetadata);
            if (fromVariantMap == null) {
                Log.e(TAG, "onAnnouncement: serviceMetadata map = null !! ignoring.");
                return;
            }
            appId = (UUID) fromVariantMap.get(AboutKeys.ABOUT_APP_ID);
            deviceName = (String) fromVariantMap.get(AboutKeys.ABOUT_DEVICE_NAME);
            Log.i(TAG, "onAnnouncement: ServiceName = " + serviceName + " port = " + port + " deviceId = " + appId.toString() + " deviceName = " + deviceName);
        } catch (BusException e) {
            e.printStackTrace();
            return;
        }

        // update device list
        Device oldDevice = null;
        boolean deviceAdded = false;
        for (int i = 0; i < deviceList.size(); i++) {
            oldDevice = deviceList.get(i);
            if (oldDevice.appId.equals(appId)) {
                deviceList.remove(oldDevice);
                deviceList.add(new Device(serviceName, port, appId, deviceName));
                deviceAdded = true;
            }
        }
        if (!deviceAdded) {
            deviceList.add(new Device(serviceName, port, appId, deviceName));
        }
    }

    private class OBWifiInfo {
        String ssid;
        OnboardingService.AuthType authType;
        String password;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_SCAN_DONE:
                    if(mForceScan == true) {
                        notifyScanStop(ONBOARDING_SCAN_SUCCESS);
                        mForceScan = false;
                    }
                    else
                        checkIfNewOnBoardees();
                    break;
                case MESSAGE_ONBOARDING_STATE_CONNECTING_ONBOARDEE_WIFI:
                    notifyOnboardingProgress(getCurrentConfiguredOnboardee(), ONBOARDING_STATE_CONNECTING_ONBOARDEE_WIFI);
                    break;
                case MESSAGE_ONBOARDING_STATE_FINDING_ONBOARDEE:
                    notifyOnboardingProgress(getCurrentConfiguredOnboardee(), ONBOARDING_STATE_FINDING_ONBOARDEE);
                    break;
                case MESSAGE_ONBOARDING_STATE_CONNECTING_TARGET_WIFI:
                    notifyOnboardingProgress(getCurrentConfiguredOnboardee(), ONBOARDING_STATE_CONNECTING_TARGET_WIFI);
                    break;
                case MESSAGE_ONBOARDING_STATE_VERIFYING_ONBOARDED:
                    notifyOnboardingProgress(getCurrentConfiguredOnboardee(), ONBOARDING_STATE_VERIFYING_ONBOARDED);
                    break;
                default:
                    break;
            }
        }
    };

    private BroadcastReceiver mWifiBroadcastReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                ConnectivityManager connMgr = (ConnectivityManager) mContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo wifiInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if(wifiInfo.isConnected()) {
                    scanOnBoardeesInBackground();
                }
            }
        }
    };

    private BroadcastReceiver mScanBroadcastReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(OnboardingManager.WIFI_SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                if(mForceScan)
                    mContext.unregisterReceiver(this);
                mHandler.sendEmptyMessage(MESSAGE_SCAN_DONE);
            }
        }
    };

    private BroadcastReceiver mOnBoardingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if(OnboardingManager.STATE_CHANGE_ACTION.equals(intent.getAction())) {
                if (extras != null && extras.containsKey(OnboardingManager.EXTRA_ONBOARDING_STATE)) {
                    String value = extras.getString(OnboardingManager.EXTRA_ONBOARDING_STATE);
                    mCurrentState = OnboardingManager.OnboardingState.valueOf(value);
                    Log.d(TAG, "onboarding callback: mcurrentState = " + mCurrentState.toString());
                    if (value != null && !value.isEmpty()) {
                        if (OnboardingManager.OnboardingState.ABORTED.toString().equals(value)) {
                            synchronized (mOnboardingJob) {
                                mOnboardingJob.notify();
                            }
                        } else if (OnboardingManager.OnboardingState.VERIFIED_ONBOARDED.toString().equals(value)) {
                            mCurrentStatus = ONBOARDING_SUCCESS;
                            synchronized (mOnboardingJob) {
                                Log.d(TAG, "Success, Onboarding process completed, notify");
                                mOnboardingJob.notify();
                            }
                        } else if (OnboardingManager.OnboardingState.CONFIGURED_ONBOARDEE.toString().equals(value) && isOffboarding) {
                        } else if (OnboardingManager.OnboardingState.CONNECTING_ONBOARDEE_WIFI.toString().equals(value)) {
                            mHandler.sendEmptyMessage(MESSAGE_ONBOARDING_STATE_CONNECTING_ONBOARDEE_WIFI);
                        } else if (OnboardingManager.OnboardingState.FINDING_ONBOARDEE.toString().equals(value)) {
                            mHandler.sendEmptyMessage(MESSAGE_ONBOARDING_STATE_FINDING_ONBOARDEE);
                        } else if (OnboardingManager.OnboardingState.CONNECTING_TARGET_WIFI.toString().equals(value)) {
                            mHandler.sendEmptyMessage(MESSAGE_ONBOARDING_STATE_CONNECTING_TARGET_WIFI);
                        } else if (OnboardingManager.OnboardingState.VERIFYING_ONBOARDED.toString().equals(value)) {
                            mHandler.sendEmptyMessage(MESSAGE_ONBOARDING_STATE_VERIFYING_ONBOARDED);
                        }
                    }
                }

            } else if(OnboardingManager.ERROR.equals(intent.getAction())) {
                String details = extras.getString(OnboardingManager.EXTRA_ERROR_DETAILS);
                mDetailError = OnboardingManager.OnboardingErrorType.getOnboardingErrorTypeByString(details);
                mCurrentStatus = ONBOARDING_ERROR;
                synchronized (mOnboardingJob) {
                    mOnboardingJob.notify();
                }
            }
        }
    };

    public OnBoardingManager(Context context, MiddlewareApi api) {
        super(context, BaseManager.ONBOARDING_MANAGER);
        mResources = mContext.getResources();
        mApi = api;

        mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        /*
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mWifiBroadcastReceiver,filter);
        */

        //Scan
        IntentFilter scanFilter = new IntentFilter(OnboardingManager.WIFI_SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(mScanBroadcastReceiver, scanFilter);

        //OnBoarding
        deviceList = new ArrayList<Device>();
        boolean prepareDaemonResult = DaemonInit.PrepareDaemon(context.getApplicationContext());
        mOnboardingJob = new Object();

//        IntentFilter onboardingFilter = new IntentFilter();
//        onboardingFilter.addAction(OnboardingManager.STATE_CHANGE_ACTION);
//        onboardingFilter.addAction(OnboardingManager.ERROR);
//        mContext.registerReceiver(mOnBoardingReceiver, onboardingFilter);

    }

    public void scan() {

        if(!isConnectedToBus()) {
            connectToBus();
        }

        if(mScanThread != null) {
            log("scan process is running!!!");
            return;
        }

        try {
            notifyScanStart();
            mForceScan = true;

            IntentFilter scanFilter = new IntentFilter(OnboardingManager.WIFI_SCAN_RESULTS_AVAILABLE_ACTION);
            mContext.registerReceiver(mScanBroadcastReceiver, scanFilter);

            OnboardingManager.getInstance().scanWiFi();
        } catch (WifiDisabledException e) {
            mForceScan = false;
            notifyScanStop(ERRORCODE_WIFI_IS_DISABLED);
            e.printStackTrace();
            // TODO: display alert dialog or toast?
            log("scan failed: Wi-Fi is disabled!!!");
            return;
        }

        //mScanThread = new ScanThread();
        //mScanThread.start();
    }

    public void terminateScan() {
        /*
        if(mScanThread != null && mScanThread.isAlive()) {
            mScanThread.interrupt();
            try {
                mScanThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mScanThread = null;
        }
        */
    }

    public void registerForScanStateChanged(OnBoardingScanStateChangedListener listener) {
        if(findListenerIndex(mScanListener,listener) < 0) {
            mScanListener.add(listener);
            log("registerForScanStateChanged ok");
        }
    }

    public void unRegisterForScanStateChanged(OnBoardingScanStateChangedListener listener) {
        int index = findListenerIndex(mScanListener, listener);
        if(index >=0 ) {
            mScanListener.remove(index);
            log("unRegisterForScanStateChanged ok");
        }
    }

    private void _startOnBoarding(OnBoardee[] clients, int onboardee_connect_timeout, int target_connect_timeout) {

        if(mOnBoardingThread != null) {
            if(mOnBoardingThread.isAlive()) {
                log("onBoarding process is running!!!");
                return;
            } else {
                mOnBoardingThread = null;
            }
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(OnboardingManager.STATE_CHANGE_ACTION);
        filter.addAction(OnboardingManager.ERROR);
        mContext.registerReceiver(mOnBoardingReceiver, filter);

        mOnBoardingThread = new OnBoardingThread(clients, onboardee_connect_timeout, target_connect_timeout);
        mOnBoardingThread.start();
        /*
        try {
            TimeoutController.execute(mOnBoardingThread,time);
        } catch (TimeoutController.TimeoutException e) {
            e.printStackTrace();
        }
        */

    }

    /**
     * Do Onboarding (Auto Mode)
     *
     * @param policy
     * @param onboardee_connect_timeout
     * @param target_connect_timeout
     */
    public void startOnBoarding(int policy, int onboardee_connect_timeout, int target_connect_timeout) {
        mCurrentPolicy = policy;

        switch(policy) {
            case ONBOARDING_POLICY_1:
            default:
                final List<WiFiNetwork> networks =
                        OnboardingManager.getInstance().getWifiScanResults(OnboardingManager.WifiFilter.ALL);
                if(networks.size() > 0) {
                    final List<WiFiNetwork> roomhubNetworks = processScanResults(networks, mResources.getString(R.string.config_onboardee_ssid_prefix));
                    if(roomhubNetworks.size() > 0) {
                        OnBoardee[] clients = transferToOnBoardees(roomhubNetworks);
                        _startOnBoarding(clients, onboardee_connect_timeout, target_connect_timeout);
                    }
                }
                break;
            case ONBOARDING_POLICY_2:
                break;
            case ONBOARDING_POLICY_3:
                break;
        }
    }

    /**
     * Do Onboarding (User provide Onboardee list)
     *
     * @param clients
     * @param onboardee_connect_timeout
     * @param target_connect_timeout
     */
    public void startOnBoarding(OnBoardee[] clients, int onboardee_connect_timeout, int target_connect_timeout) {
        _startOnBoarding(clients, onboardee_connect_timeout, target_connect_timeout);
    }

    public void terminateOnBoarding() {
        if(mOnBoardingThread != null && mOnBoardingThread.isAlive()) {
            mForceAbortOnboarding = true;
        }

        /*
        if(mOnBoardingThread != null && mOnBoardingThread.isAlive()) {
            mOnBoardingThread.interrupt();
            try {
                mOnBoardingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mOnBoardingThread = null;
        }
        */
    }

    public void registerForOnBoardingStateChanged(OnBoardingStateChangedListener listener) {
        if(findListenerIndex(mScanListener,listener) < 0) {
            mOnBoardingListener.add(listener);
            log("registerForOnBoardingStateChanged ok");
        }
    }

    public void unRegisterForOnBoardingStateChanged(OnBoardingStateChangedListener listener) {
        int index = findListenerIndex(mOnBoardingListener,listener);
        if(index >=0 ) {
            mOnBoardingListener.remove(index);
            log("unRegisterForOnBoardingStateChanged ok");
        }
    }

    public void onDestory() {
        //terminate Onboarding thread
        terminateOnBoarding();
        if(mOnBoardingThread != null) {
            try {
                mOnBoardingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(isConnectedToBus()) {
            disconnectFromBus();
        }
    }

    /*
    public OnBoardee[] getOnBoardedList() {

    }

    public OnBoardee[] getUnOnBoardingList() {

    }
    */

    private void scanOnBoardeesInBackground() {
        synchronized (mScanStargedGuard) {
            if(isScanningInBackground) {
                log("scanning is ongoing in background!!!");
                return;
            }
            isScanningInBackground = true;
            doScanInBackground();
        }
    }

    private void doScanInBackground() {

    }

    private void checkIfNewOnBoardees() {
        int count = 0;

        final List<WiFiNetwork> networks =
                OnboardingManager.getInstance().getWifiScanResults(OnboardingManager.WifiFilter.ONBOARDABLE);

        if(networks.size() == 0)
            return;

        // TODO: get unbinding devices from RoomHubManager and check if new onboardee be found:::
        if(count > 0) {
            // send broadcast
            Intent intent = new Intent(GlobalDef.INTENT_NEW_ONBOARDESS_FOUND);
            // TODO: put count into extras
            mContext.sendBroadcast(intent);
        }
    }

    private int findListenerIndex(ArrayList list, Object listener) {
        for(int i=0;i<list.size();i++) {
            if(list.get(i).equals(listener))
                return i;
        }
        return -1;
    }

    private void notifyScanStart() {
        for(OnBoardingScanStateChangedListener listener:mScanListener) {
            listener.onScanStart();
        }
    }

    private void notifyScanStop(int errorCode) {
        final List<WiFiNetwork> networks =
                OnboardingManager.getInstance().getWifiScanResults(OnboardingManager.WifiFilter.ALL);
                //OnboardingManager.getInstance().getWifiScanResults(OnboardingManager.WifiFilter.ONBOARDABLE);

        final List<WiFiNetwork> roomhubNetworks = processScanResults(networks, mResources.getString(R.string.config_onboardee_ssid_prefix));
        OnBoardee[] results = transferToOnBoardees(roomhubNetworks);
        for(OnBoardingScanStateChangedListener listener:mScanListener) {
            listener.onScanStop(errorCode, results);
        }
    }

    /**
     * notifyOnboardingStart: Notify OnboardingStart event
     *
     */
    private void notifyOnboardingStart() {
        for(OnBoardingStateChangedListener listener:mOnBoardingListener) {
            listener.onBoardingStart();
        }
    }

    private void notifyOnboardingProgress(OnBoardee client, int state) {
        for(OnBoardingStateChangedListener listener:mOnBoardingListener) {
            listener.onBoardingProgress(client, state);
        }
    }

    /**
     * notifyOnboardingStop: Notify OnboardingStop event
     */
    private void notifyOnboardingStop() {
        for(OnBoardingStateChangedListener listener:mOnBoardingListener) {
            listener.onBoardingStop();
        }
        Log.d(TAG, "notifyOnboardingStop!");
    }

    /**
     * notifyOnClientConnect: Notify OnClientConnect event
     *
     * @param errorCode Return error code.
     * @param client Return OnBoarde client.
     */
    private void notifyOnClientConnect(int errorCode, OnBoardee client) {
        for(OnBoardingStateChangedListener listener:mOnBoardingListener) {
            listener.onClientConnect(errorCode, client);
        }
    }

    /**
     * notifyOnClientConnect: Notify OnClientJoined event
     *
     * @param errorCode Return error code.
     * @param client  Return OnBoarde client,
     */
    private void notifyOnClientJoined(int errorCode, OnboardingManager.OnboardingErrorType detailError, OnBoardee client) {
        for(OnBoardingStateChangedListener listener:mOnBoardingListener) {
            listener.onClientJoined(errorCode, detailError, client);
        }

        Log.d(TAG, "notifyOnClientJoined - " + client.getSsid());
    }

    private void notifyOnClientJoinedEnd(OnBoardee client) {
        for(OnBoardingStateChangedListener listener:mOnBoardingListener) {
            listener.onClientJoinedEnd(client);
        }
    }

    private OnBoardee[] transferToOnBoardees(List<WiFiNetwork> allNetworks) {
        OnBoardee[] results = new OnBoardee[allNetworks.size()];
        for(int i=0;i<allNetworks.size();i++) {
            WiFiNetwork network = allNetworks.get(i);
            OnBoardee temp = new OnBoardee();
            temp.ssid = network.getSSID();
            temp.authType = network.getAuthType();
            results[i] = temp;
        }
        return results;
    }

    /*
    private class ScanThread extends Thread {

        ScanThread() {
            super("ScanThread");
        }

        @Override
        public void run() {
            notifyScanStart();
            try {
                OnboardingManager.getInstance().scanWiFi();
            } catch (WifiDisabledException e) {
                e.printStackTrace();
                log("scan onBoarding clients timeout:::");
                // TODO: show dialog or toast?
            }
        }

    };
    */

    private class OnBoardingThread extends Thread {
        OnBoardee[] onBoardees;
        int onBoardeeConnectTimeout, targetConnectTimeout, targetWaitAnnouncementTimeout;

        OnBoardingThread(OnBoardee[] clients, int onboardee_connect_timeout, int target_connect_timeout) {
            super("OnBoardingThread");
            onBoardees = clients;
            onBoardeeConnectTimeout = onboardee_connect_timeout;
            targetConnectTimeout = target_connect_timeout;
            if(onBoardeeConnectTimeout<=0)
                onBoardeeConnectTimeout = mResources.getInteger(R.integer.config_onboardee_connect_timeout);
            if(targetConnectTimeout<=0)
                targetConnectTimeout = mResources.getInteger(R.integer.config_onboarding_target_timeout);

            targetWaitAnnouncementTimeout = mResources.getInteger(R.integer.config_onboarding_target_wait_announcement_timeout);

            if(!isConnectedToBus()) {
                connectToBus();
            }
        }

        @Override
        public void run() {
            int reTryCnt;

            notifyOnboardingStart();
            Log.d(TAG, "Total " + onBoardees.length + " onboardees");
            for(int i=0;i<onBoardees.length;i++) {
                OBWifiInfo onboardeeInfo = getOnBoardeeWifiInfo(onBoardees[i]);
                Log.d(TAG, "[" + i + "], ssid = " + onboardeeInfo.ssid);
            }

            // Send Broadcast to disable Middleware WifiManager monitor
            mContext.sendBroadcast(new Intent(MiddlewareApi.ONBOARDING_START_ACTION));
            mForceAbortOnboarding = false;
            for(int i=0;i<onBoardees.length;i++) {
                OBWifiInfo onboardeeInfo = getOnBoardeeWifiInfo(onBoardees[i]);
                OBWifiInfo targetInfo = getTargetWifiInfo();

                WiFiNetworkConfiguration onboardee =
                        new WiFiNetworkConfiguration(onboardeeInfo.ssid, onboardeeInfo.authType, onboardeeInfo.password, false);
                WiFiNetworkConfiguration target =
                        new WiFiNetworkConfiguration(targetInfo.ssid, targetInfo.authType, targetInfo.password, false);

                OnboardingConfiguration config =
                        new OnboardingConfiguration(onboardee,
                                onBoardeeConnectTimeout,
                                OnboardingManager.DEFAULT_ANNOUNCEMENT_TIMEOUT,
                                target,
                                targetConnectTimeout,
                                targetWaitAnnouncementTimeout /*OnboardingManager.DEFAULT_ANNOUNCEMENT_TIMEOUT*/);
                Log.d(TAG, "");
                Log.d(TAG, "---------------------------------");
                Log.d(TAG, "[" + i + "], ssid = " + onboardeeInfo.ssid);
                Log.d(TAG, "---------------------------------");
                Log.d(TAG, "");

                reTryCnt = 0;
                mCurrentStatus = ONBOARDING_INIT;
                do {
                    setCurrentConfiguredOnboardee(onBoardees[i]);
                    synchronized (mOnboardingJob) {
                        Onboarding(config);
                        try {
                            mOnboardingJob.wait(ONBOARDING_WAIT_TIMEOUT);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            if (mCurrentStatus != ONBOARDING_SUCCESS) {
                                reTryCnt++;
                            }
                        }
                    }
                } while ((mCurrentStatus != ONBOARDING_SUCCESS && reTryCnt < 1) && !mForceAbortOnboarding);

                if(mCurrentStatus == ONBOARDING_SUCCESS) {
                    notifyOnClientJoined(mCurrentStatus, mDetailError, onBoardees[i]);
                    try {
                        // Sleep 300ms: let OnboardingManager's state machine back to IDLE state
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // TODO: Change RoomHub AP's SSID/Password
                } else {
                    notifyOnClientJoined(mCurrentStatus, mDetailError, onBoardees[i]);

                    synchronized (mOnboardingJob) {
                        // Reset OnboardingManager's State Machine to State.IDLE state
                        AbortOnboarding();
                        try {
                            mOnboardingJob.wait(ONBOARDING_ABORT_TIMEOUT);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if(mForceAbortOnboarding) {
                    break;
                }

                if(i < onBoardees.length - 1) {
                    Log.d(TAG, "i = " + i + ", sleep 15 seconds");
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "i = " + i +", doWifiOnOff");
                    doWifiOnOff();
                }
//                Log.d(TAG, "onboarding [" + i + "] status = " + onboardees[i].status + ", state = " + onboardees[i].state);

                notifyOnClientJoinedEnd(onBoardees[i]);
            }

            setCurrentConfiguredOnboardee(null);

            // Send Broadcast to enable Middleware WifiManager monitor
            mContext.sendBroadcast(new Intent(MiddlewareApi.ONBOARDING_STOP_ACTION));

            notifyOnboardingStop();
//            mContext.sendBroadcast(new Intent(MiddlewareApi.DISCONNECT_ROOMHUB_ALLJOYN_ACTION));
//            try {
//                sleep(200);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            mContext.sendBroadcast(new Intent(MiddlewareApi.CONNECT_ROOMHUB_ALLJOYN_ACTION));
            if(isConnectedToBus()) {
                disconnectFromBus();
            }
        }
    }

    private OBWifiInfo getOnBoardeeWifiInfo(OnBoardee onboardee) {
        OBWifiInfo info = new OBWifiInfo();

        if(onboardee == null) {
            return null;
        }
        info.ssid = onboardee.ssid;
        info.authType = onboardee.authType;
        if(info.authType != OnboardingService.AuthType.OPEN) {
            info.password = mResources.getString(R.string.config_onboardee_default_pass);
        }

        return info;
    }

    private OnboardingService.AuthType getAuthType() {
        PreferenceEditor prefEditor = new PreferenceEditor(mContext, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        int securityType = prefEditor.getIntValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SECURITY);
        int pskType = prefEditor.getIntValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SECURITY_PSK);
        int encryptionType = prefEditor.getIntValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SECURITY_SUB_TYPE);

        OnboardingService.AuthType authType = OnboardingService.AuthType.ANY;

        if (securityType == GlobalDef.SECURITY_NONE) {
            authType = OnboardingService.AuthType.OPEN;
        } else if (securityType == GlobalDef.SECURITY_WEP) {
            authType = OnboardingService.AuthType.WEP;
        } else if (securityType == GlobalDef.SECURITY_PSK || securityType == GlobalDef.SECURITY_EAP) {
            if(pskType == GlobalDef.PskType.WPA2.ordinal() || pskType == GlobalDef.PskType.WPA_WPA2.ordinal()) {
                if(encryptionType == GlobalDef.WPA_WAP2_SUB_TYPE.TKIP.ordinal()) {
                    authType = OnboardingService.AuthType.WPA2_TKIP;
                } else if(encryptionType == GlobalDef.WPA_WAP2_SUB_TYPE.CCMP.ordinal()) {
                    authType = OnboardingService.AuthType.WPA2_CCMP;
                } else {
                    authType = OnboardingService.AuthType.WPA2_AUTO;
                }
            } else if (pskType == GlobalDef.PskType.WPA.ordinal()) {
                if(encryptionType == GlobalDef.WPA_WAP2_SUB_TYPE.TKIP.ordinal()) {
                    authType = OnboardingService.AuthType.WPA_TKIP;
                } else if(encryptionType == GlobalDef.WPA_WAP2_SUB_TYPE.CCMP.ordinal()) {
                    authType = OnboardingService.AuthType.WPA_CCMP;
                } else {
                    authType = OnboardingService.AuthType.WPA_AUTO;
                }
            }
        }

        return authType;
    }

    private OBWifiInfo getTargetWifiInfo() {
        OBWifiInfo info = new OBWifiInfo();

        switch(mCurrentPolicy) {
            case ONBOARDING_POLICY_1:
            default:
                // TODO: get info from Preference:::
                PreferenceEditor prefEditor = new PreferenceEditor(mContext, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
                info.ssid = prefEditor.getStringValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SSID);
                info.authType = getAuthType();
                info.password = prefEditor.getStringValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_PASSWORD);
                break;
            case ONBOARDING_POLICY_2:
                break;
            case ONBOARDING_POLICY_3:
                break;
        }

//        info.ssid = "pwr-q967ef0-1";
//        info.authType = OnboardingService.AuthType.WPA2_AUTO;
//        info.password = "ericchang";

        return info;
    }

    private List<WiFiNetwork> processScanResults(List<WiFiNetwork> wifiScanResult, String searchPrefix) {
        ArrayList<WiFiNetwork> onboareeAPlist = new ArrayList<WiFiNetwork>();
        String searchprefix;

        if (searchPrefix == null || searchPrefix.isEmpty()) {
            searchprefix = mResources.getString(R.string.config_onboardee_ssid_prefix);
        } else {
            searchprefix = searchPrefix;
        }

        for (WiFiNetwork scan : wifiScanResult) {
            if (scan.getSSID() == null || scan.getSSID().isEmpty()) {
                //Log.i(TAG, "processScanResults currentScan was empty,skipping");
                continue;
            }

            if (scan.getSSID().startsWith(searchprefix)) {
                onboareeAPlist.add(scan);
            }
        }

        return onboareeAPlist;
    }

    /**
     * Creates new busAttachment, connect and register authListener. Starts
     * about service. Update the OnboardingManager with the new busAttachment
     * aboutClient objects.
     */
    private void connectToBus() {
        Log.i(TAG, "connectToBus");
        if (mContext == null) {
            Log.e(TAG, "Failed to connect AJ, m_context == null !!");
            return;
        }
        // prepare bus attachment
        sBusAttachment = new BusAttachment(mContext.getPackageName(), BusAttachment.RemoteMessage.Receive);
        sBusAttachment.connect();

        // request the name for the daemon and advertise it.
        sDaemonName = "org.alljoyn.BusNode.d" + sBusAttachment.getGlobalGUIDString();
        int flag = BusAttachment.ALLJOYN_REQUESTNAME_FLAG_DO_NOT_QUEUE;
        Status reqStatus = sBusAttachment.requestName(sDaemonName, flag);
        if (reqStatus == Status.OK) {
            // advertise the name with a quite prefix for devices to find it
            Status adStatus = sBusAttachment.advertiseName(DAEMON_QUIET_PREFIX + sDaemonName, SessionOpts.TRANSPORT_ANY);
            if (adStatus != Status.OK) {
                sBusAttachment.releaseName(sDaemonName);
                Log.w(TAG, "failed to advertise daemon name " + sDaemonName);
            } else {
                Log.d(TAG, "Succefully advertised daemon name " + sDaemonName);
            }
        }

        try {
            sBusAttachment.registerAboutListener(this);
            sBusAttachment.whoImplements(new String[] { OnboardingTransport.INTERFACE_NAME });

            // Add auth listener - needed for OnboardingService secure calls
            String keyStoreFileName = mContext.getFileStreamPath("alljoyn_keystore").getAbsolutePath();
            SrpAnonymousKeyListener m_authListener = new SrpAnonymousKeyListener(new AuthPasswordHandler() {
                private final String TAG = "AlljoynOnAuthPasswordHandler";

                @Override
                public char[] getPassword(String peerName) {
                    return DEFAULT_PINCODE.toCharArray();
                }

                @Override
                public void completed(String mechanism, String authPeer, boolean authenticated) {
                    //Log.d(TAG, "Auth completed: mechanism = " + mechanism + " authPeer= " + authPeer + " --> " + authenticated);
                    if (!authenticated) {
                        Intent AuthErrorIntent = new Intent(OnboardingManager.ERROR);
                        Bundle extra = new Bundle();
                        //extra.putString(OnboardingManager.EXTRA_ERROR_DETAILS, String.format(context.getString(R.string.auth_failed_msg), mechanism, authPeer));
                        AuthErrorIntent.putExtras(extra);
                        mContext.sendBroadcast(AuthErrorIntent);
                    }
                }

            }, new AndroidLogger(), new String[] { "ALLJOYN_SRP_KEYX", "ALLJOYN_ECDHE_PSK" });
            Log.i(TAG, "m_authListener.getAuthMechanismsAsString: " + m_authListener.getAuthMechanismsAsString());
            Status authStatus = sBusAttachment.registerAuthListener(m_authListener.getAuthMechanismsAsString(), m_authListener, keyStoreFileName);
            if (authStatus != Status.OK) {
                Log.e(TAG, "Failed to connectToBus");
            }
        } catch (Exception e) {
            Log.e(TAG, "fail to connectToBus", e);
        }

        try {
            OnboardingManager.getInstance().init(mContext, sBusAttachment);
        } catch (OnboardingIllegalArgumentException e) {
            e.printStackTrace();
        } catch (OnboardingIllegalStateException e) {
            e.printStackTrace();
        }

        Log.i(TAG, " connectToBus Done");
    }

    /**
     * Remove Match from Alljoyn bus attachment, Stop about client and cancel
     * bus advertise name.
     */
    private void disconnectFromBus() {
        Log.i(TAG, "disconnectFromBus");
        /*
         * It is important to unregister the BusObject before disconnecting from
         * the bus. Failing to do so could result in a resource leak.
         */
        try {
            if (sBusAttachment != null && sBusAttachment.isConnected()) {
                sBusAttachment.cancelWhoImplements(new String[] { OnboardingTransport.INTERFACE_NAME });
                sBusAttachment.unregisterAboutListener(this);
                sBusAttachment.cancelAdvertiseName(DAEMON_QUIET_PREFIX + sDaemonName, SessionOpts.TRANSPORT_ANY);
                sBusAttachment.releaseName(sDaemonName);
                sBusAttachment.disconnect();
                sBusAttachment = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error when disconnectFromAJ ");
            e.printStackTrace();
        }
        Log.i(TAG, "bus disconnected");
        deviceList.clear();

        try {
            OnboardingManager.getInstance().shutDown();
        } catch (OnboardingIllegalStateException e) {
            e.printStackTrace();
        }
    }

    private boolean isConnectedToBus() {
        if (sBusAttachment == null) {
            return false;
        }
        boolean isConnected = sBusAttachment.isConnected();
        Log.i(TAG, "isConnectToBus = " + isConnected);
        return isConnected;
    }

    private void Onboarding(OnboardingConfiguration config) {
        try {
            OnboardingManager.getInstance().runOnboarding(config);
            isOffboarding = false;
        } catch (OnboardingIllegalArgumentException e) {
            e.printStackTrace();
        } catch (OnboardingIllegalStateException e) {
            e.printStackTrace();
        } catch (WifiDisabledException e) {
            e.printStackTrace();
        }
    }

    private static final int ABORT_RETRY_COUNT = 3;
    private void AbortOnboarding() {
        boolean reTry = false;
        int reTryCnt = 0;

        do {
            try {
                Log.d(TAG, "AbortOnboarding: " + reTryCnt);
                OnboardingManager.getInstance().abortOnboarding();
            } catch (OnboardingIllegalStateException e) {
                e.printStackTrace();
                if(reTryCnt++ < ABORT_RETRY_COUNT) {
                    reTry = true;
                } else {
                    reTry = false;
                }
            }

            if(reTry) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while(reTry);
    }

    private void setCurrentConfiguredOnboardee(OnBoardee newOnboardee) {
        this.currentOnboardee = newOnboardee;
        return;
    }
    private OnBoardee getCurrentConfiguredOnboardee() {
        return this.currentOnboardee;
    }

    private void doWifiOnOff() {
        PreferenceEditor prefEditor = new PreferenceEditor(mContext, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        int security = prefEditor.getIntValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SECURITY);
        String password = prefEditor.getStringValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_PASSWORD);

        /*
        mWifiManager.disconnect();
        mWifiManager.setWifiEnabled(false);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mWifiManager.setWifiEnabled(true);
        */

        /*
        WifiConfiguration wfc = new WifiConfiguration();

        wfc.SSID = "\"".concat(getTargetWifiInfo().ssid).concat("\"");
        wfc.status = WifiConfiguration.Status.DISABLED;
        wfc.priority = 40;

        if(security == GlobalDef.SECURITY_NONE) {
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedAuthAlgorithms.clear();
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        } else if(security == GlobalDef.SECURITY_WEP) {
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

            if (Utils.isHexString(password))
                wfc.wepKeys[0] = password;
            else
                wfc.wepKeys[0] = "\"".concat(password).concat("\"");
            wfc.wepTxKeyIndex = 0;
        } else if(security == GlobalDef.SECURITY_PSK) {
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            wfc.preSharedKey = "\"".concat(password).concat("\"");
        }

        int networkId = mWifiManager.addNetwork(wfc);
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        if(list != null) {
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + getTargetWifiInfo().ssid + "\"")) {
                    mWifiManager.disconnect();
                    mWifiManager.enableNetwork(i.networkId, true);
                    Log.d(TAG, "do reconnect");

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    break;
                }
            }
        }
        */
        WiFiNetworkConfiguration targetWifi = new WiFiNetworkConfiguration(getTargetWifiInfo().ssid, getTargetWifiInfo().authType, getTargetWifiInfo().password, false);
        doReconnectToTargetWifi(targetWifi, 5000);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "callback !!");
    }

    private void doReconnectToTargetWifi(WiFiNetworkConfiguration network, long connectionTimeout) {
        lollipop_connectToWifiAP(network.getSSID(), network.getAuthType(), network.getPassword(), false, connectionTimeout);
        return;
    }

    private String normalizeSSID(String ssid) {
        if (ssid != null && ssid.length() > 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() -1);
        }
        return ssid;
    }

    private boolean isSsidEquals(String ssid1, String ssid2) {
        if (ssid1 == null || ssid1.length() == 0 || ssid2 == null || ssid2.length() == 0) {
            return false;
        }
        return normalizeSSID(ssid1).equals(normalizeSSID(ssid2));
    }

    private WifiConfiguration findConfiguration(String ssid) {
        // the configured Wi-Fi networks
        final List<WifiConfiguration> wifiConfigs = mWifiManager.getConfiguredNetworks();

        // for debugging purposes only log the list
        StringBuffer buff = new StringBuffer();
        for (WifiConfiguration w : wifiConfigs) {
            if (w!= null && w.SSID != null) {
                w.SSID = normalizeSSID(w.SSID);
                if (w.SSID.length() > 1) {
                    buff.append(w.SSID).append(",");
                }
            }
        }
        Log.i(TAG, "connectToWifiAP ConfiguredNetworks " + (buff.length() > 0 ? buff.toString().substring(0, buff.length() - 1) : " empty"));

        // find any existing WifiConfiguration that has the same SSID as the
        // supplied one and return it if found
        for (WifiConfiguration w : wifiConfigs) {
            if (w != null && w.SSID != null && isSsidEquals(w.SSID, ssid)) {
                Log.i(TAG, "connectToWifiAP found " + ssid + " in ConfiguredNetworks. networkId = " + w.networkId);
                return w;
            }
        }

        return null;
    }

    private final String WEP_HEX_PATTERN = "[\\dA-Fa-f]+";
    private Pair<Boolean, Boolean> checkWEPPassword(String password) {
        Log.d(TAG, "checkWEPPassword");

        if (password == null || password.isEmpty()) {
            Log.w(TAG, "checkWEPPassword empty password");
            return new Pair<Boolean, Boolean>(false, false);
        }

        int length = password.length();
        switch (length) {
            // valid ASCII keys length
            case 5:
            case 13:
            case 16:
            case 29:
                Log.d(TAG, "checkWEPPassword valid WEP ASCII password");
                return new Pair<Boolean, Boolean>(true, false);
            // valid hex keys length
            case 10:
            case 26:
            case 32:
            case 58:
                if (password.matches(WEP_HEX_PATTERN)) {
                    Log.d(TAG, "checkWEPPassword valid WEP password length, and HEX pattern match");
                    return new Pair<Boolean, Boolean>(true, true);
                }
                Log.w(TAG, "checkWEPPassword valid WEP password length, but HEX pattern matching failed: " + WEP_HEX_PATTERN);
                return new Pair<Boolean, Boolean>(false, false);
            default:
                Log.w(TAG, "checkWEPPassword invalid WEP password length: " + length);
                return new Pair<Boolean, Boolean>(false, false);
        }
    }

    private void lollipop_connectToWifiAP(String ssid, OnboardingService.AuthType authType, String password, boolean isHidden, long connectionTimeout) {
        Log.d(TAG, "lollipop_connectToWifiAP SSID = " + ssid + " authtype = " + authType.toString()+ " is hidden = "+ isHidden);

        // if networkPass is null set it to ""
        if (password == null) {
            password = "";
        }

        int networkId = -1;
        boolean shouldUpdate = false;

        WifiConfiguration wifiConfiguration = findConfiguration(ssid);

        if(wifiConfiguration == null) {
            wifiConfiguration = new WifiConfiguration();
        } else {
            shouldUpdate = true;
        }

        if (isHidden){
            wifiConfiguration.hiddenSSID=true;
        }

        Log.i(TAG, "lollipop_connectToWifiAP selectedAuthType = " + authType);

        // set the priority to something high so that the network we are entering should be used
        wifiConfiguration.priority = 40;

        // set the WifiConfiguration parameters
        switch (authType) {
            case OPEN:
                wifiConfiguration.SSID = "\"" + ssid + "\"";
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                networkId = shouldUpdate ? mWifiManager.updateNetwork(wifiConfiguration) : mWifiManager.addNetwork(wifiConfiguration);
                Log.d(TAG, "lollipop_connectToWifiAP [OPEN] add Network returned " + networkId);
                break;

            case WEP:
                wifiConfiguration.SSID = "\"" + ssid + "\"";

                // check the validity of a WEP password
                Pair<Boolean, Boolean> wepCheckResult = checkWEPPassword(password);
                if (!wepCheckResult.first) {
                    Log.i(TAG, "lollipop_connectToWifiAP  auth type = WEP: password " + password + " invalid length or charecters");
                    return;
                }
                Log.i(TAG, "lollipop_connectToWifiAP [WEP] using " + (!wepCheckResult.second ? "ASCII" : "HEX"));
                if (!wepCheckResult.second) {
                    wifiConfiguration.wepKeys[0] = "\"" + password + "\"";
                } else {
                    wifiConfiguration.wepKeys[0] = password;
                }
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                wifiConfiguration.wepTxKeyIndex = 0;
                networkId = shouldUpdate ? mWifiManager.updateNetwork(wifiConfiguration) : mWifiManager.addNetwork(wifiConfiguration);
                Log.d(TAG, "lollipop_connectToWifiAP [WEP] add Network returned " + networkId);
                break;

            case WPA_AUTO:
            case WPA_CCMP:
            case WPA_TKIP:
            case WPA2_AUTO:
            case WPA2_CCMP:
            case WPA2_TKIP: {
                wifiConfiguration.SSID = "\"" + ssid + "\"";
                // handle special case when WPA/WPA2 and 64 length password that can
                // be HEX
                if (password.length() == 64 && password.matches(WEP_HEX_PATTERN)) {
                    wifiConfiguration.preSharedKey = password;
                } else {
                    wifiConfiguration.preSharedKey = "\"" + password + "\"";
                }
                wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                networkId = shouldUpdate ? mWifiManager.updateNetwork(wifiConfiguration) : mWifiManager.addNetwork(wifiConfiguration);
                Log.d(TAG, "lollipop_connectToWifiAP  [WPA..WPA2] add Network returned " + networkId);
                break;
            }
            default:
                networkId = -1;
                break;
        }
        if (networkId < 0) {
            Log.d(TAG, "lollipop_connectToWifiAP networkId <0  WIFI_AUTHENTICATION_ERROR");
            if(Build.VERSION.SDK_INT < 23)
                return;
        }
        Log.d(TAG, "lollipop_connectToWifiAP calling connect");

        //We will now save the configuration and then look back up the networkId
        //saveConfiguration may cause networkId to change
        boolean res = mWifiManager.saveConfiguration();
        Log.d(TAG, "lollipop_connectToWifiAP saveConfiguration status=" + res);
        wifiConfiguration = findConfiguration(ssid);
        if(wifiConfiguration == null) {
            Log.d(TAG, "lollipop_connectToWifiAP Could not find configuration after adding");
            return;
        }

        lillipop_connect(wifiConfiguration, wifiConfiguration.networkId, connectionTimeout);
    }


    /**
     * Make the actual connection to the requested Wi-Fi target.
     *
     * @param wifiConfig
     *            details of the Wi-Fi access point used by the WifiManger
     * @param networkId
     *            id of the Wi-Fi configuration
     * @param timeoutMsec
     *            period of time in Msec to complete Wi-Fi connection task
     */
    private void lillipop_connect(final WifiConfiguration wifiConfig, final int networkId, final long timeoutMsec) {
        Log.i(TAG, "lillipop_connect  SSID=" + wifiConfig.SSID + " within " + timeoutMsec);
        boolean res;

        res = mWifiManager.disconnect();
        mWifiManager.setWifiEnabled(false);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "lillipop_connect disconnect  status=" + res);

        if ( !mWifiManager.isWifiEnabled() ) {
            mWifiManager.setWifiEnabled(true);
        }

        // enabling a network doesn't guarantee that it's the one that Android
        // will connect to.
        // Selecting a particular network is achieved by passing 'true' here to
        // disable all other networks.
        // the side effect is that all other user's Wi-Fi networks become
        // disabled.
        // The recovery for that is enableAllWifiNetworks method.
        res = mWifiManager.enableNetwork(networkId, true);
        Log.d(TAG, "lillipop_connect enableNetwork [true] status=" + res);
        // Wait a few for the WiFi to do something and try again just in case
        // Android has decided that the network we configured is not "good enough"
        try{ Thread.sleep(500); } catch(Exception e) {}
        res = mWifiManager.enableNetwork(networkId, true);
        Log.d(TAG, "lillipop_connect enableNetwork [true] status=" + res);
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
