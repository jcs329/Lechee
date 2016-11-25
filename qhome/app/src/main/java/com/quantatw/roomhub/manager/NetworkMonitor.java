package com.quantatw.roomhub.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.MiddlewareApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by erin on 12/14/15.
 */
public class NetworkMonitor extends Handler {
    private final String TAG=NetworkMonitor.class.getSimpleName();

    private Context mContext;
    private MiddlewareApi mMiddlewareApi;
    private ConnectivityManager mConnectivityManager;
    private NetworkStateReceiver mReceiver;
    private List<NetworkStateReceiverListener> listeners = new ArrayList<NetworkStateReceiverListener>();
    private int currentNetworkType = -1;
    private boolean mOnboardingRunning = false;
    private boolean mBindWifiNetwork = false;

    public interface NetworkStateReceiverListener {
        public void networkAvailable(int networkType);
        public void networkUnavailable(int networkType);
        public void networkTypeChanged(int origNetworkType, int currentNetworkType);
    }

    public NetworkMonitor(Looper looper, Context context, MiddlewareApi middlewareApi) {
        super(looper);
        mMiddlewareApi = middlewareApi;
        mContext = context;

        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        /*
        * Android 6.0+
        * Register Wifi state for binding process to Wifi network
         */
        if(Build.VERSION.SDK_INT >= 23) {
            WifiStateReceiver mWifiReceiver = new WifiStateReceiver();
            IntentFilter wifiFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            context.registerReceiver(mWifiReceiver, wifiFilter);
        }

        mReceiver = new NetworkStateReceiver();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction(MiddlewareApi.ONBOARDING_START_ACTION);
        mFilter.addAction(MiddlewareApi.ONBOARDING_STOP_ACTION);
        context.registerReceiver(mReceiver, mFilter);
    }

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what) {
            default:
                super.handleMessage(msg);
                break;
        }
    }

    private class WifiStateReceiver extends BroadcastReceiver {
        WifiManager mWifiManager;
        WifiStateReceiver() {
            mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                Log.d(TAG,"--- WifiManager.NETWORK_STATE_CHANGED_ACTION ---");
                NetworkInfo networkInfo =intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, "WifiNetworkInfo: " + networkInfo.toString());
                int wifiState = mWifiManager.getWifiState();
                Log.d(TAG, "wifistate: " + wifiState);
//                if(wifiState == WifiManager.WIFI_STATE_ENABLED && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
//                    if(mOnboardingRunning) {
//                        /* bind here */
//                        mBindWifiNetwork = Utils.bindProcessToWiFiNetwork(mContext, true);
//                        Log.d(TAG, "1. mOnboardingRunning bindProcessToWiFiNetwork");
//                    }
//                }
            }
            else if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                Log.d(TAG,"--- WifiManager.WIFI_STATE_CHANGED_ACTION ---");
                int mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);
                Log.d(TAG, "mWifiState: " + mWifiState);
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                Log.d(TAG,"wifiInfo: "+wifiInfo.toString());
                if(mWifiState == WifiManager.WIFI_STATE_DISABLED) {
                    Network network = mConnectivityManager.getBoundNetworkForProcess();
                    if(network != null) {
                        NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(network);
                        if(networkInfo != null && networkInfo.getType()==ConnectivityManager.TYPE_WIFI) {
                        /* unbind here */
                            Utils.bindProcessToWiFiNetwork(mContext, false);
                        }
                    }
                    else if(mBindWifiNetwork) {
                        Utils.bindProcessToWiFiNetwork(mContext, false);
                        mBindWifiNetwork = false;
                    }
                }

            }
        }
    }

    private class NetworkStateReceiver extends BroadcastReceiver {
        boolean mIgnoreFirstIn = true;
        NetworkStateReceiver() {
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            if(networkInfo != null)
                currentNetworkType = networkInfo.getType();
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(mIgnoreFirstIn) {
                //log("--- CONNECTIVITY_ACTION --- skip first");
                mIgnoreFirstIn = false;
                return;
            }

            if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION) && !mOnboardingRunning) {
                log("--- CONNECTIVITY_ACTION ---");
                NetworkInfo mobileInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo wifiInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

                NetworkInfo info =
                        intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

                /*
                log("MOBILE: mobileInfo=" + mobileInfo);
                if (mobileInfo != null) {
                    log("mobileInfo: isConnected=" + mobileInfo.isConnected());
                }
                log("WIFI: wifiInfo=" + wifiInfo);
                if (wifiInfo != null) {
                    log("wifiInfo: isConnected=" + wifiInfo.isConnected());
                }
                */
                log("ActiveNetworkInfo:" + activeNetworkInfo);
                if (activeNetworkInfo != null) {
                    log("--- isConnected=" + activeNetworkInfo.isConnected());
                }
                log("Extra Network Info:" + info);
                if (info != null) {
                    log("--- type=" + info.getTypeName() + ",isConnected=" + info.isConnected());
                }

                if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                    log("[ NO CONNECTIVITY ]");
                    notifyState(false, info.getType());
                }
                else {
                    if (info != null && info.isConnected()) {
                        if (info.getType() != currentNetworkType) {
                            log("<SWITCH> networkType=" + info.getTypeName() + ",previousNetworkType=" + currentNetworkType);
                            notifyNetworkTypeSwitch(currentNetworkType, info.getType());
                        }
                        currentNetworkType = info.getType();
                        notifyState(true, info.getType());
                    }

                    if (info != null && !info.isConnected()) {
                        log("[ DISCONNECTED ]");
                        notifyState(false, info.getType());
                    }
                }
            }
            else {
                if(action.equals(MiddlewareApi.ONBOARDING_START_ACTION)) {
                    mOnboardingRunning = true;
                }
                else if(action.equals(MiddlewareApi.ONBOARDING_STOP_ACTION)) {
                    mOnboardingRunning = false;
                }
            }

        }

        private void notifyStateToAll() {
//            for(NetworkStateReceiverListener listener : listeners)
//                notifyState(listener);
        }

        private void notifyState(boolean connected, int networkType) {
            if(listeners == null || listeners.size() == 0)
                return;

            /* send broadcast */
            Intent intent = new Intent(GlobalDef.ACTION_NETWORK_STATE_CHAGNED);
            intent.putExtra(GlobalDef.EXTRA_NETWORK_STATE, connected);
            mContext.sendBroadcast(intent);

            for(NetworkStateReceiverListener listener : listeners) {
                if(connected == true)
                    listener.networkAvailable(networkType);
                else
                    listener.networkUnavailable(networkType);
            }
        }

        private void notifyNetworkTypeSwitch(int origNetwork, int currentNetwork) {

            /* send broadcast */
            for(NetworkStateReceiverListener listener : listeners) {
                listener.networkTypeChanged(origNetwork,currentNetwork);
            }
        }
    }

    public void addListener(NetworkStateReceiverListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeListener(NetworkStateReceiverListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
