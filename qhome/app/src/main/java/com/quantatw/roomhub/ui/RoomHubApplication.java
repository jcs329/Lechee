package com.quantatw.roomhub.ui;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.quantatw.roomhub.blepair.BLEPairController;
//import com.quantatw.roomhub.manager.ACManager;
import com.quantatw.roomhub.manager.asset.manager.ACNoticeManager;
import com.quantatw.roomhub.manager.AccountManager;
//import com.quantatw.roomhub.manager.AirPurifierManager;
//import com.quantatw.roomhub.manager.BulbManager;
//import com.quantatw.roomhub.manager.FANManager;
import com.quantatw.roomhub.manager.IRController;
import com.quantatw.roomhub.manager.OTAManager;
import com.quantatw.roomhub.manager.OnBoardingManager;
//import com.quantatw.roomhub.manager.PMManager;
import com.quantatw.roomhub.manager.RoomHubDBHelper;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
//import com.quantatw.roomhub.manager.TVManager;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceManager;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.pack.roomhub.VersionCheckUpdateResPack;

public class RoomHubApplication extends Application {
    private final String TAG=RoomHubApplication.class.getSimpleName();

    private RoomHubService mRoomHubService;
    private Messenger mRoomHubServiceMessenger;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            //Log.d(TAG,"onServiceConnected enter");
            mRoomHubService = ((RoomHubService.LocalBinder) service).getService();
            mRoomHubServiceMessenger = ((RoomHubService.LocalBinder) service).getMessenger();
            Intent intent = new Intent(RoomHubService.INTENT_ROOMHUB_SERVICE_INIT_DONE);
            sendBroadcast(intent);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //Log.d(TAG,"onServiceDisconnected enter");
            mRoomHubService = null;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Intent serviceIntent = new Intent(this, RoomHubService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Utils.setIsOnBoarding(getApplicationContext(), false);
        // for Android 6.0 Marshmallow
        if(Build.VERSION.SDK_INT >= 23) {
            Utils.setPromptDisableMobileData(getApplicationContext(), false);
        }
		/*
        Intent intent = new Intent(this,ReminderService.class);
        stopService(intent);

        if(mAccountManager != null){
            mAccountManager.onDestory();
            mAccountManager=null;
        }
        if(mRoomHubManager != null) {
            mRoomHubManager.onDestory();
        }

        if(mOnBoardingManager != null) {
            mOnBoardingManager.onDestory();
            mOnBoardingManager = null;
        }

        if(mMicroLocationManager != null) {
            mMicroLocationManager.onDestroy();
            mMicroLocationManager = null;
        }
		*/
    }

    public boolean isServiceReady() {
        if(mRoomHubService != null)
            return true;
        return false;
    }

    public VersionCheckUpdateResPack checkAppVersion() {
        if(mRoomHubService == null)
            return null;
        return mRoomHubService.checkAppVersion();
    }

    // TODO: userdata
    public void sendToServiceHandler(int messageId) {
        if(!isServiceReady())
            return;

        try {
            mRoomHubServiceMessenger.send(Message.obtain(null,messageId));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendToServiceHandler(Message message) {
        if(!isServiceReady())
            return;

        try {
            mRoomHubServiceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public synchronized RoomHubManager getRoomHubManager() {
        return mRoomHubService.getRoomHubManager();
    }

    public synchronized ACNoticeManager getACNoticeManager() {
        return mRoomHubService.getACNoticeManager();
    }

    public synchronized AccountManager getAccountManager() {
        return mRoomHubService.getAccountManager();
    }

    public synchronized OnBoardingManager getOnBoardingManager() {
        return mRoomHubService.getOnBoardingManager();
    }

    public synchronized RoomHubDBHelper getRoomHubDBHelper() {
        return mRoomHubService.getRoomHubDBHelper();
    }

    public synchronized OTAManager getOTAManager() {
        return mRoomHubService.getOTAManager();
    }

    //public synchronized MicroLocationManager getMicroLocationManager() { return mRoomHubService.getMicroLocationManager();  }

    public synchronized IRController getIRController() { return mRoomHubService.getIRController();  }
/*
    public synchronized ACManager getACManager() {
        return mRoomHubService.getACManager();
    }

    public synchronized FANManager getFANManager() {
        return mRoomHubService.getFANManager();
    }

    public synchronized AirPurifierManager getAirPurifierManager() {
        return mRoomHubService.getAirPurifierManager();
    }
    public synchronized PMManager getPMManager() {
        return mRoomHubService.getPMManager();
    }

    public synchronized BulbManager getBulbManager() {
        return mRoomHubService.getBulbManager();
    }

    public synchronized TVManager getTVManager() {
        return mRoomHubService.getTVManager();
    }
*/
    public synchronized HealthDeviceManager getHealthDeviceManager() { return mRoomHubService.getHealthDeviceManager(); }

    public synchronized BLEPairController getBLEController() { return mRoomHubService.getBLEController();  }
}
