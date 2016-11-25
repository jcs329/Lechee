/******************************************************************************
 * Copyright AllSeen Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for any
 *    purpose with or without fee is hereby granted, provided that the above
 *    copyright notice and this permission notice appear in all copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/
// Sample code comes from : https://goo.gl/lN4MAh - MainActivity.java & ProtocolManager.java

package com.quantatw.sls.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import org.alljoyn.onboarding.sdk.OffboardingConfiguration;
import org.alljoyn.onboarding.sdk.OnboardingConfiguration;
import org.alljoyn.onboarding.sdk.OnboardingIllegalArgumentException;
import org.alljoyn.onboarding.sdk.OnboardingIllegalStateException;
import org.alljoyn.onboarding.sdk.OnboardingManager;
import org.alljoyn.onboarding.sdk.WiFiNetworkConfiguration;
import org.alljoyn.onboarding.sdk.WifiDisabledException;
import org.alljoyn.onboarding.transport.OnboardingTransport;
import org.alljoyn.services.android.security.AuthPasswordHandler;
import org.alljoyn.services.android.security.SrpAnonymousKeyListener;
import org.alljoyn.services.android.utils.AndroidLogger;
import org.alljoyn.services.common.utils.TransportUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Initializes an Alljoyn daemon and manages a list of AllJoyn devices the
 * daemon is announced on. This class will also enable the user to connect
 * Alljoyn bus attachment and disconnect from it.
 */
public class OnboardingBusHandler implements AboutListener {
    private final String TAG = this.getClass().getSimpleName();
    private static final String INTERFACE_NAME = OnboardingTransport.INTERFACE_NAME;
    private boolean isOffboarding = false;

    private IntentFilter mainFilter;

    static {
        try {
            // load alljoyn lib.
            System.loadLibrary("alljoyn_java");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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
     * AllJoynManager singleton.
     */
    private static OnboardingBusHandler instance = null;

    /**
     * Android application context.
     */
    private Context context = null;

    /**
     * Alljoyn bus attachment.
     */
    private static BusAttachment busAttachment = null;

    /**
     * String for Alljoyn daemon to be advertised with.
     */
    private static String daemonName = null;

    private Object OnboardingJob;
    private int currentState;

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

    public static OnboardingBusHandler getInstance() {
        if (instance == null) {
            instance = new OnboardingBusHandler();
        }
        return instance;
    }

    /**
     * Initialize the device list and starts the Alljoyn daemon.
     *
     * @param context
     *            Android application context
     */
    public void init(Context context) {
        Log.i(TAG, "init");
        this.context = context;
        deviceList = new ArrayList<Device>();
        boolean prepareDaemonResult = DaemonInit.PrepareDaemon(context.getApplicationContext());
        Log.i(TAG, "PrepareDaemon returned " + prepareDaemonResult);

        OnboardingJob = new Object();

        connectToBus();

        // Creates new IntentFilter and add two main OnboardingManager Actions
        // to
        // it.
        mainFilter = new IntentFilter();
        mainFilter.addAction(OnboardingManager.STATE_CHANGE_ACTION);
        mainFilter.addAction(OnboardingManager.ERROR);
        this.context.registerReceiver(mainReceiver, mainFilter);
    }

    /**
     * Listen to aboutService Announcement call and manage the DeviceList
     * accordingly.
     */
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

    /**
     * Listen to aboutService onDeviceLost call and manage the DeviceList
     * accordingly.
     */
    public void onDeviceLost(String serviceName) {
        Log.d(TAG, "onDeviceLost serviceName = " + serviceName);
        Device device = null;
        for (int i = 0; i < deviceList.size(); i++) {
            device = deviceList.get(i);
            if (device.serviceName.equals(serviceName)) {
                Log.i(TAG, "remove device from list, friendly name = " + device.name);
                deviceList.remove(device);
                break;
            }
        }
    }

    public List<Device> getDeviceList() {
        return deviceList;
    }

    /**
     * Creates new busAttachment, connect and register authListener. Starts
     * about service. Update the OnboardingManager with the new busAttachment
     * aboutClient objects.
     */
    private void connectToBus() {
        Log.i(TAG, "connectToBus");
        if (context == null) {
            Log.e(TAG, "Failed to connect AJ, m_context == null !!");
            return;
        }
        // prepare bus attachment
        busAttachment = new BusAttachment(context.getPackageName(), BusAttachment.RemoteMessage.Receive);
        busAttachment.connect();

        // request the name for the daemon and advertise it.
        daemonName = "org.alljoyn.BusNode.d" + busAttachment.getGlobalGUIDString();
        int flag = BusAttachment.ALLJOYN_REQUESTNAME_FLAG_DO_NOT_QUEUE;
        Status reqStatus = busAttachment.requestName(daemonName, flag);
        if (reqStatus == Status.OK) {
            // advertise the name with a quite prefix for devices to find it
            Status adStatus = busAttachment.advertiseName(DAEMON_QUIET_PREFIX + daemonName, SessionOpts.TRANSPORT_ANY);
            if (adStatus != Status.OK) {
                busAttachment.releaseName(daemonName);
                Log.w(TAG, "failed to advertise daemon name " + daemonName);
            } else {
                Log.d(TAG, "Succefully advertised daemon name " + daemonName);
            }
        }

        try {
            busAttachment.registerAboutListener(this);
            busAttachment.whoImplements(new String[] { OnboardingTransport.INTERFACE_NAME });

            // Add auth listener - needed for OnboardingService secure calls
            String keyStoreFileName = context.getFileStreamPath("alljoyn_keystore").getAbsolutePath();
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
                        context.sendBroadcast(AuthErrorIntent);
                    }
                }

            }, new AndroidLogger(), new String[] { "ALLJOYN_SRP_KEYX", "ALLJOYN_ECDHE_PSK" });
            Log.i(TAG, "m_authListener.getAuthMechanismsAsString: " + m_authListener.getAuthMechanismsAsString());
            Status authStatus = busAttachment.registerAuthListener(m_authListener.getAuthMechanismsAsString(), m_authListener, keyStoreFileName);
            if (authStatus != Status.OK) {
                Log.e(TAG, "Failed to connectToBus");
            }
        } catch (Exception e) {
            Log.e(TAG, "fail to connectToBus", e);
        }

        try {
            OnboardingManager.getInstance().init(context, busAttachment);
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
            if (busAttachment != null && busAttachment.isConnected()) {
                busAttachment.cancelWhoImplements(new String[] { OnboardingTransport.INTERFACE_NAME });
                busAttachment.unregisterAboutListener(this);
                busAttachment.cancelAdvertiseName(DAEMON_QUIET_PREFIX + daemonName, SessionOpts.TRANSPORT_ANY);
                busAttachment.releaseName(daemonName);
                busAttachment.disconnect();
                busAttachment = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error when disconnectFromAJ ");
            e.printStackTrace();
        }
        Log.i(TAG, "bus disconnected");
        deviceList.clear();
    }

    private boolean isConnectedToBus() {
        if (busAttachment == null) {
            return false;
        }
        boolean isConnected = busAttachment.isConnected();
        Log.i(TAG, "isConnectToBus = " + isConnected);
        return isConnected;
    }

    /**
     * Listen to the two main OnboardingManager Intents. Log intents with the
     * action "OnboardingManager.STATE_CHANGE_ACTION" in a list Display alert
     * dialog for intents with the action "OnboardingManager.ERROR".
     */
    private final BroadcastReceiver mainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String intentString = "Action = " + intent.getAction() + (extras != null ? bundleToString(extras) : "");
            Log.i(TAG, intentString);
            if (intent.getAction().equals(OnboardingManager.STATE_CHANGE_ACTION)) {
                if (extras != null && extras.containsKey(OnboardingManager.EXTRA_ONBOARDING_STATE)) {
                    String value = extras.getString(OnboardingManager.EXTRA_ONBOARDING_STATE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                        if (value != null && !value.isEmpty()) {
                            if (OnboardingManager.OnboardingState.ABORTED.toString().equals(value)) {
                                Log.d(TAG, "Success, Abort has been completed");
                            } else if (OnboardingManager.OnboardingState.CONNECTING_ONBOARDEE_WIFI.toString().equals(value)) {
                                Log.d(TAG, "Success, Onboarding process connecting onboarde wifi");
                            } else if (OnboardingManager.OnboardingState.VERIFIED_ONBOARDED.toString().equals(value)) {
                                Log.d(TAG, "Success, Onboarding process completed");
                                OnboardingJob.notify();
                            } else if (OnboardingManager.OnboardingState.CONFIGURED_ONBOARDEE.toString().equals(value) && isOffboarding) {
                                Log.d(TAG, "Success, Offboarding process completed");
                            }
                        }
                    }
                }
            } else if (intent.getAction().equals(OnboardingManager.ERROR)) {
                Log.d(TAG, "Error, " + intentString);
            }
        }
    };

    /**
     * Call runOnboarding with the given parameters.
     */
    public void Onboarding(WiFiNetworkConfiguration onboardee, WiFiNetworkConfiguration target) {
        OnboardingConfiguration config = new OnboardingConfiguration(onboardee, OnboardingManager.DEFAULT_WIFI_CONNECTION_TIMEOUT, OnboardingManager.DEFAULT_ANNOUNCEMENT_TIMEOUT, target, OnboardingManager.DEFAULT_WIFI_CONNECTION_TIMEOUT, OnboardingManager.DEFAULT_ANNOUNCEMENT_TIMEOUT);

        try {
            OnboardingManager.getInstance().runOnboarding(config);
            isOffboarding = false;
        } catch (OnboardingIllegalArgumentException e) {
//            showErrorMessage(getString(R.string.alert_title_runonboarding_error), getString(R.string.alert_msg_invalid_configuration));
            e.printStackTrace();
        } catch (OnboardingIllegalStateException e) {
//            showErrorMessage(getString(R.string.alert_title_runonboarding_error), getString(R.string.alert_msg_runonboarding_invalid_state));
            e.printStackTrace();
        } catch (WifiDisabledException e) {
//            showErrorMessage(getString(R.string.alert_title_wifi_error), getString(R.string.alert_msg_wifi_disabled));
            e.printStackTrace();
        }
    }

    public class OnBoardee {
        protected String ssid;
        protected OnboardingService.AuthType authType;
        protected String password;
    }

    public void Onboarding(/* OnBoardee[] onBoardees , TargetWifi targetWifi */) {
        OnBoardee[] onboardees = new OnBoardee[5];
        int reTryCnt = 0;

        for(int i = 0; i < 5; i++) {
            onboardees[i] = new OnBoardee();
            onboardees[i].ssid = "testap";
            onboardees[i].authType = OnboardingService.AuthType.WPA2_AUTO;
            onboardees[i].password = "aaaabbbb";

            //WiFiNetworkConfiguration onboardee = new WiFiNetworkConfiguration("testap", OnboardingService.AuthType.WPA2_AUTO, "aaaabbbb", true);
            //WiFiNetworkConfiguration target = new WiFiNetworkConfiguration("pwr-q967ef0-1", OnboardingService.AuthType.WPA2_AUTO , "ericchang", false);

        }
        ArrayList<OnBoardee> obBoardeeList = new ArrayList<OnBoardee>(Arrays.asList(onboardees));

        currentState = -1;
        for(int i = 0; i < onboardees.length; i++) {
            WiFiNetworkConfiguration onboardee = new WiFiNetworkConfiguration(onboardees[i].ssid, onboardees[i].authType, onboardees[i].password, true);
            WiFiNetworkConfiguration target = new WiFiNetworkConfiguration("pwr-q967ef0-1", OnboardingService.AuthType.WPA2_AUTO , "ericchang", false);

            do {
                Onboarding(onboardee, target);
                synchronized (OnboardingJob) {
                    try {
                        OnboardingJob.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        if (currentState != OnboardingManager.OnboardingState.VERIFIED_ONBOARDED.getValue()) {
                            reTryCnt++;
                        }
                    }
                }
            } while (reTryCnt <= 2);
        }
        currentState = -1;
    }

    /**
     * Display a list represent all the available onboarded devices on the
     * current network. Select item will allow the user to offboard the selected
     * device.
     */
    public void Offboarding(String uuid) {
        Device dev = null;
        for(Device temp : deviceList) {
            if(temp.appId.equals(uuid)) {
                dev = temp;
                break;
            }
        }

        if(dev != null) {
            OffboardingConfiguration config = new OffboardingConfiguration(dev.serviceName, dev.port);
            try {
                OnboardingManager.getInstance().runOffboarding(config);
                isOffboarding = true;
            } catch (OnboardingIllegalArgumentException e) {
//            showErrorMessage(getString(R.string.alert_title_runoffboarding_error), getString(R.string.alert_msg_invalid_configuration));
                e.printStackTrace();
            } catch (OnboardingIllegalStateException e) {
//            showErrorMessage(getString(R.string.alert_title_runoffboarding_error), getString(R.string.alert_msg_runonboarding_invalid_state));
                e.printStackTrace();
            } catch (WifiDisabledException e) {
//            showErrorMessage(getString(R.string.alert_title_wifi_error), getString(R.string.alert_msg_wifi_disabled));
                e.printStackTrace();
            }
        }

    }

    /**
     * Call abortOnboarding at the OnboardingManager
     */
    public void AbortOnboarding() {

        try {
            OnboardingManager.getInstance().abortOnboarding();
        } catch (OnboardingIllegalStateException e) {
//            showErrorMessage(getString(R.string.alert_title_abort_error), e.getMessage());
            e.printStackTrace();
        }
    }

    public int ScanWifi() {
        BroadcastReceiver wifireceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
            }
        };

        IntentFilter wifiFilter = new IntentFilter(OnboardingManager.WIFI_SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifireceiver, wifiFilter);

        try {
            OnboardingManager.getInstance().scanWiFi();
//            progressDialog.show();
        } catch (WifiDisabledException e) {
//            showErrorMessage(getString(R.string.alert_title_wifi_error), getString(R.string.alert_msg_wifi_disabled));
//            progressDialog.dismiss();
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Utility to flatten bundle to String. Required for the log list.
     */
    private String bundleToString(Bundle bundle) {
        if (bundle == null) {
            return "";
        }
        Set<String> keys = bundle.keySet();
        if (keys == null || keys.size() == 0) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        for (String key : keys) {
            s.append("  " + key + " = " + bundle.get(key));
        }
        return s.toString();
    }

}
