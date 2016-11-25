package com.quantatw.roomhub.manager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.ibeacon.BeaconServiceUtility;
import com.radiusnetworks.ibeacon.service.StartRMData;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by jungle on 2015/10/6.
 */
public class MicroLocationService extends Service implements IBeaconConsumer {
    protected static final String TAG = "MicroLocationService";
    private Context mContext;

    private ArrayList<IBeacon> mIBeaconServcieDevList = null;

    private BeaconServiceUtility beaconUtill = null;
    private IBeaconManager miBeaconManager = null;
    private MicroLocationManager.IBeaconCallback mCallback = null;

    private final IBinder mBinder = new MyBinder();

    /** Command to the service to display a message */
    public static final int MSG_UPDATE_IBEACON = 2;
    public static final int MSG_STOP_RANGING = 3;
    public static final int MSG_START_MONITORING = 4;
    public static final int MSG_STOP_MONITORING = 5;

   /* @Override
    public void handleMessage(Message msg)
    {
        IBeaconService service = mService.get();
        StartRMData startRMData = (StartRMData) msg.obj;

        if (service != null) {
            switch (msg.what) {
                case MSG_START_RANGING:
                    Log.d(TAG, "start ranging received");
                    service.startRangingBeaconsInRegion(startRMData.getRegionData(), new com.radiusnetworks.ibeacon.service.Callback(msg.replyTo, startRMData.getIntentActionForCallback()));
                    break;
                case MSG_STOP_RANGING:
                    Log.d(TAG, "stop ranging received");
                    service.stopRangingBeaconsInRegion(startRMData.getRegionData());
                    break;
                case MSG_START_MONITORING:
                    Log.d(TAG, "start monitoring received");
                    service.startMonitoringBeaconsInRegion(startRMData.getRegionData(), new com.radiusnetworks.ibeacon.service.Callback(msg.replyTo, startRMData.getIntentActionForCallback()));
                    break;
                case MSG_STOP_MONITORING:
                    Log.d(TAG, "stop monitoring received");
                    service.stopMonitoringBeaconsInRegion(startRMData.getRegionData());
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate enter");
        mContext = this;
        miBeaconManager = IBeaconManager.getInstanceForApplication(mContext);

        mIBeaconServcieDevList = new ArrayList<IBeacon>();

        MLM_start(mContext);
    }

    @Override
    public void onIBeaconServiceConnect() {
        miBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {

                mIBeaconServcieDevList.clear();
                mIBeaconServcieDevList.addAll(iBeacons);
                Log.d(TAG, "didRangeBeaconsInRegion: mIBeaconServcieDevList.size=" + mIBeaconServcieDevList.size());
               /* for (int i=0; i < mIBeaconServcieDevList.size(); i++) {
                    Log.d(TAG, "didRangeBeaconsInRegion: iBeacons=" + iBeacons + "region=" + region);
                    Log.d(TAG, "UUID: " + mIBeaconServcieDevList.get(i).getProximityUuid());
                    Log.d(TAG, "Major: " + mIBeaconServcieDevList.get(i).getMajor());
                    Log.d(TAG, "Minor: " + mIBeaconServcieDevList.get(i).getMinor());
                    Log.d(TAG, "Proximity: " + mIBeaconServcieDevList.get(i).getProximity());
                    Log.d(TAG, "Rssi: " + mIBeaconServcieDevList.get(i).getRssi());
                    Log.d(TAG, "TxPower: " + mIBeaconServcieDevList.get(i).getMajor());
                    Log.d(TAG, "distance: " + mIBeaconServcieDevList.get(i).getAccuracy());
                }*/

                if (mCallback != null)
                    mCallback.onConnect(iBeacons, region);
            }

        });

        miBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.e("BeaconDetactorService", "didEnterRegion");
                // logStatus("I just saw an iBeacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.e("BeaconDetactorService", "didExitRegion");
                // logStatus("I no longer see an iBeacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.e("BeaconDetactorService", "didDetermineStateForRegion");
                // logStatus("I have just switched from seeing/not seeing iBeacons: " + state);
            }

        });

        try {
            miBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            miBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //這裡實作你想做的工作

        return Service.START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind enter");
        beaconUtill.onStop(miBeaconManager, this);
        /*
        * if not call stopSelf here,
        * callee should use getApplicationContext().unbindService(mServiceConnection)
        * instead of mContext.unbindService()
         */
        stopSelf();
        return super.onUnbind(intent);

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy enter");
        super.onDestroy();
    }

    public class MyBinder extends Binder {
        MicroLocationService getService() {
            return MicroLocationService.this;
        }

    }
    public int MLM_start(Context context ) {
        mContext = context;
        beaconUtill = new BeaconServiceUtility(mContext);
        beaconUtill.onStart(miBeaconManager, this);
        //mLoginStateListenerList.add(deviceListener);

        return 0;
    }

    public void setCallback(MicroLocationManager.IBeaconCallback callback) {
        mCallback = callback;
    }
}
