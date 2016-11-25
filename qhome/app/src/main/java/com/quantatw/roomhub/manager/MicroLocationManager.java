package com.quantatw.roomhub.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.quantatw.roomhub.listener.AccountLoginStateListener;
import com.quantatw.roomhub.listener.MicroLocationSequenceChangeListener;
import com.quantatw.roomhub.listener.MicroLocationSequenceFirstChangeListener;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.ui.RoomHubService;
import com.quantatw.roomhub.utils.Utils;
import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.sql.Timestamp;

/**
 * Created by jungle on 2015/10/5.
 */

public class MicroLocationManager extends BaseManager implements AccountLoginStateListener {

    protected static final String TAG = "MicroLocationManager";
    private static boolean DEBUG = true;
    private static boolean TRACE = false;
    private static boolean ENABLE_IBEACON_TEST = false;

    private ArrayList<IBeacon> mServcieIBeaconDevList = null;
    private ArrayList<AlljyonIBeacon> mAlljyonIBeaconDevList = null;

    private LinkedHashSet<MicroLocationSequenceFirstChangeListener> mSequenceFirstChangeListenerList = null;
    private LinkedHashSet<MicroLocationSequenceChangeListener> mSequenceChangeListenerList = null;

    private IBeaconManager miBeaconManager = null;

    private MicroLocationService mService;
    private AccountManager mAccountMgr;
    private boolean mBindResult = false;

    public void rhmlm_addDevice(String uuid, String btuuid,int major,int minor) {
        ChangeIBeaconDeviceList(RoomHubData.ACTION.ADD_DEVICE, uuid, btuuid ,major,minor);
    }


    public void rhmlm_removeDevice(String uuid) {
        ChangeIBeaconDeviceList(RoomHubData.ACTION.REMOVE_DEVICE, uuid, "", 0, 0);
    }

    public void rhmlm_removeDeviceAll() {
        ChangeIBeaconDeviceList(RoomHubData.ACTION.REMOVE_DEVICE_ALL, "", "", 0, 0);
    }

    @Override
    public void startup() {
        mAccountMgr=((RoomHubService) mContext).getAccountManager();
        mAccountMgr.registerForLoginState(this);

        //This filter check the bluetoothadapter state.
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(GlobalDef.ACTION_SETTINGS_LOCATE_ME_CHANGED);
        mContext.registerReceiver(mReceiver, filter);

        //enable();
    }

    @Override
    public void terminate() {
        mContext.unbindService(mServiceConnection);
        mBindResult = false;
    }

    public interface IBeaconCallback {
        public void onConnect(Collection<IBeacon> iBeacons, Region region);
    }

    public MicroLocationManager(Context context) {
        super(context, BaseManager.LOCATION_MANAGER);

        mServcieIBeaconDevList = new ArrayList<IBeacon>();
        mAlljyonIBeaconDevList = new ArrayList<AlljyonIBeacon>();

        mSequenceFirstChangeListenerList = new LinkedHashSet<MicroLocationSequenceFirstChangeListener>();
        mSequenceChangeListenerList = new LinkedHashSet<MicroLocationSequenceChangeListener>();

    }

    public void onDestroy() {
        disable();
    }

    @Override
    public void onLogin() {
        boolean isLogin = false;
        isLogin = mAccountMgr.isLogin();
        if (isLogin) {
            rhmlm_removeDeviceAll();
        }
    }

    @Override
    public void onLogout(){

    }

    @Override
    public void onSkipLogin(){

    }

    public void registerForFirstSequenceChange(MicroLocationSequenceFirstChangeListener listener) {
        synchronized(mSequenceFirstChangeListenerList) {
            mSequenceFirstChangeListenerList.add(listener);
        }
    }

    public void unRegisterForFirstSequenceChange(MicroLocationSequenceFirstChangeListener listener) {
        synchronized (mSequenceFirstChangeListenerList) {
            mSequenceFirstChangeListenerList.remove(listener);
        }
    }

    public void registerForSequenceChange(MicroLocationSequenceChangeListener listener) {
        synchronized(mSequenceChangeListenerList) {
            mSequenceChangeListenerList.add(listener);
        }
    }

    public void unRegisterForSequenceChange(MicroLocationSequenceChangeListener listener) {
        synchronized (mSequenceChangeListenerList) {
            mSequenceChangeListenerList.remove(listener);
        }
    }

    public void enable() {
        Log.d(TAG, "enable:::");
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (Utils.isLocateMeOn(mContext) && mBluetoothAdapter.isEnabled()) {
            startIBeaconService();
        }
    }

    public void disable() {
        Log.d(TAG,"disable:::");
       // unRegisterForFirstSequenceChange(mSequenceFirstChangeListenerList);
       // unRegisterForSequenceChange(mSequenceChangeListenerList);
        if(mBindResult)
            mContext.unbindService(mServiceConnection);
        mBindResult = false;
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((MicroLocationService.MyBinder)service).getService();
            //Log.d(TAG, "onServiceConnected enter ++, mService=" + mService);
            mService.setCallback(iBeaconCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Log.d(TAG, "onServiceDisconnected enter ++");
            mBindResult = false;
            mService = null;
        }
    };

    private void enableBluetooth() {
        try {
            //Log.d(TAG, "enableBluetooth ++...checkAvailability=" + IBeaconManager.getInstanceForApplication(mContext).checkAvailability());
            //If BLUETOOTH is off, to enable BLUETOOTH_SERVICE
            //Log.i(TAG, "enableBluetooth ++....BluetoothManager.isEnabled()=" + ((BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled());
            if (!IBeaconManager.getInstanceForApplication(mContext).checkAvailability()) {
                ((BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().enable();
            }
            //enable();

        } catch (RuntimeException e) {
        }
    }

    public final IBeaconCallback iBeaconCallback = new IBeaconCallback() {
        @Override
        public void onConnect(Collection<IBeacon> iBeacons, Region region) {
            boolean bHadUpdate = false;
            synchronized (mServcieIBeaconDevList) {
                mServcieIBeaconDevList.clear();
                mServcieIBeaconDevList.addAll(iBeacons);

                if (DEBUG)
                    Log.d(TAG, "IBeaconCallback ++..: mIBeaconServcieDevList.size=" + mServcieIBeaconDevList.size());

                for (IBeacon ibeaconNode : mServcieIBeaconDevList) {
                    //Update IBeacon data in AlljyonIBeaconDevList queue that it has pass from IBeacon service.
                    bHadUpdate = UpdateBeaconDeviceDistance(ibeaconNode);
                }

                //if enable IBeacon test, register to listen IBeacon data from IBeacon service.
                if (ENABLE_IBEACON_TEST) {
                    if (mSequenceChangeListenerList != null) {
                        for (MicroLocationSequenceChangeListener listener : mSequenceChangeListenerList) {
                            if (mServcieIBeaconDevList.size() > 0)
                                listener.onServiceIBeaconSequenceChange(mServcieIBeaconDevList);
                        }
                    }
                }
            }

            //when finish update IBeacon data, start to sorting the IBeacon data order by the distance asec to sequence list.
            if (bHadUpdate)
                notifySequencesChanged();
        }
    };

    private void startIBeaconService() {
        if (mBindResult) return;

        //explicit Intent, safe
        Intent serviceIntent = new Intent(mContext, MicroLocationService.class);
        serviceIntent.setPackage("com.quantatw.roomhub");
        mBindResult = mContext.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "startIBeaconService ++...after do start service...mBindResult=" + mBindResult);
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Log.d(TAG,"OnReceive ++, action="+action);
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                //Log.d(TAG,"OnReceive ++ ACTION_STATE_CHANGED, state="+state);
                switch (state)
                {
                    case BluetoothAdapter.STATE_ON:
                    {
                        //Do something you need here
                        Log.d(TAG, "bluetooth STATE_ON");
                        enable();
//                        if(Utils.isLocateMeOn(mContext)) {
//                            Log.d(TAG, "LocateMe ON");
//                            enable();
//                        }
                        break;
                    }
                    case BluetoothAdapter.STATE_OFF:
                    {
                        //Do something you need here
                        Log.d(TAG, "bluetooth STATE_OFF");
                        disable();
                        //mContext.getApplicationContext().unbindService(mServiceConnection);
                        break;
                    }
                    default:
                        //System.out.println("Default");
                        break;
                }
            }
            else if(action.equals(GlobalDef.ACTION_SETTINGS_LOCATE_ME_CHANGED)) {
                boolean btEnable = ((BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled();
                boolean locateMe = intent.getBooleanExtra(GlobalDef.KEY_SETTINGS_VALUE, true);
                Log.d(TAG, "got ACTION_SETTINGS_LOCATE_ME_CHANGED ,bluetooth "+btEnable+",locateMe "+locateMe);
                if(btEnable & locateMe) {
                    Log.d(TAG, "LocateMe ON");
                    startIBeaconService();
                }
                else if(locateMe & !btEnable) {
                    Log.d(TAG, "LocateMe ON but BT OFF, Switch BT ON!");
                    enableBluetooth();
                }
                else if(!locateMe) {
                    Log.d(TAG, "LocateMe OFF");
                    disable();
                }
            }

        }
    };

    private int getIdxByUuid(String Uuid){
        if(mAlljyonIBeaconDevList.size() == 0) return -1;

        for(int i=0; i < mAlljyonIBeaconDevList.size();i++){
            if(Uuid.equals(mAlljyonIBeaconDevList.get(i).getUuid())){
                return i;
            }
        }
        return -1;
    }

    public void ChangeIBeaconDeviceList(RoomHubData.ACTION action, String uuid, String btuuid,int major,int minor){
        if(getIdxByUuid(uuid) >= 0){
            if(action == RoomHubData.ACTION.REMOVE_DEVICE) {
                if(DEBUG)
                    Log.d(TAG,"ChangeIBeaconDeviceList remove device uuid="+uuid);

                removeDevice(uuid);
                notifySequencesChanged();
            }
            else if (action == RoomHubData.ACTION.REMOVE_DEVICE_ALL) {
                removeDeviceAll();
            }
        } else {
            if(action == RoomHubData.ACTION.ADD_DEVICE) {
                if (DEBUG) Log.d(TAG, "ChangeIBeaconDeviceList: uuid=" + uuid +",btuuid=" + btuuid + ",major=" + major + ",minor=" + minor);
                if ((!uuid.isEmpty()) && (!btuuid.isEmpty()) && (major != -1) && (minor != -1))
                    addDevice(uuid, btuuid, major, minor);
                if (DEBUG) Log.d(TAG, "ChangeIBeaconDeviceList: mAlljyonIBeaconDevList.size=" + mAlljyonIBeaconDevList.size());
            }
        }
        //notifySequencesChanged();
    }

    public int addDevice(String stUuid, String btuuid, int major, int minor) {
        Log.d(TAG, "addDevice ++ : stUuid=" + stUuid + ", btuuid=" + btuuid + " major=" + major + " minor=" + minor);
        boolean isAdd = false;

        synchronized (mAlljyonIBeaconDevList) {
            for (IBeacon ibeacon : mServcieIBeaconDevList) {
//              if (stBTMacAddr.equals(ibeacon.getDeviceMACAddress())) {
                if (DEBUG)
                    Log.d(TAG, "addDevice ++, ibeacon.btuuid=" + ibeacon.getProximityUuid()+" major="+ibeacon.getMajor()+" minor="+ibeacon.getMinor());

                if ((ibeacon.getProximityUuid().equals(btuuid)) && (major == ibeacon.getMajor()) && (minor == ibeacon.getMinor())) {
                    if (DEBUG)
                        Log.d(TAG, "mAlljyonIBeaconDevList add, ibeacon.btuuid=" + ibeacon.getProximityUuid()+" major="+ibeacon.getMajor()+" minor="+ibeacon.getMinor());
                    AlljyonIBeacon iBeaconData = newAlljyonIBeaconData(stUuid, btuuid, major, minor, ibeacon);
                    mAlljyonIBeaconDevList.add(iBeaconData);
                    isAdd = true;
                    break;
                }
            }
        }

        if (!isAdd) {
            //to handle if Alljyon wifi is ready, but IBeacon device isn't ready
            AlljyonIBeacon iBeaconData = newAlljyonIBeaconData(stUuid, btuuid, major, minor, null);
            mAlljyonIBeaconDevList.add(iBeaconData);
            isAdd = true;
        }

        if (isAdd) {
            return 0;
        }

        return -1;
    }

    public AlljyonIBeacon newAlljyonIBeaconData(String stUuid, String btuuid, int major, int minor, IBeacon beacon){
        AlljyonIBeacon alljyonBeaconData = new AlljyonIBeacon();
        alljyonBeaconData.setUuid(stUuid);
        alljyonBeaconData.setBtUuid(btuuid);
        alljyonBeaconData.setIBeaconMajor(major);
        alljyonBeaconData.setIBeaconMinor(minor);
        alljyonBeaconData.setMicroLocationData(new MicroLocationData());
        return alljyonBeaconData;
    }

    public int removeDevice(String aUuid) {
        Log.d(TAG, "removeDevice ++ : aUuid=" + aUuid);
        boolean isRemove = false;
        synchronized (mAlljyonIBeaconDevList) {
            for (AlljyonIBeacon beaconNode : mAlljyonIBeaconDevList) {
                if (DEBUG) Log.d(TAG, "addDevice ++, beaconNode.getUuid()=" + beaconNode.getUuid());
                if (aUuid.equals(beaconNode.getUuid())) {
                    mAlljyonIBeaconDevList.remove(beaconNode);
                    isRemove = true;
                    break;
                }
            }
        }

        if (isRemove)
            return 0;

        return  -1;
    }

    public int removeDeviceAll() {
        synchronized (mAlljyonIBeaconDevList) {
            for (AlljyonIBeacon beaconNode : mAlljyonIBeaconDevList) {
                if (TRACE)
                    Log.d(TAG, "removeDeviceAll ++, beaconNode.getUuid()=" + beaconNode.getUuid());
                mAlljyonIBeaconDevList.remove(beaconNode);
            }
        }

        if (mAlljyonIBeaconDevList.size() == 0) {
            return 0;
        }
        else {
            return  -1;
        }
    }

    public AlljyonIBeacon findByAlljyonBeaconList(IBeacon ibeaconNode){
        if (ibeaconNode == null)
            return null;

        synchronized (mAlljyonIBeaconDevList) {
            for (AlljyonIBeacon beaconNode : mAlljyonIBeaconDevList) {
                if (DEBUG)
                    Log.d(TAG, "findByCurrentBeaconDataList ++,beaconNode.getIsAvailable()=" + beaconNode.getIsAvailable()+", beaconNode.getIBeaconUuid()="+beaconNode.getIBeaconUuid());
                //if (beaconNode.getIsAvailable())
                {
                    if ( beaconNode.getBtUuid().equalsIgnoreCase(ibeaconNode.getProximityUuid()) && beaconNode.getIBeaconMajor() == ibeaconNode.getMajor()
                            && beaconNode.getIBeaconMinor() == ibeaconNode.getMinor()) {
                        return beaconNode;
                    }
                }
            }
        }

        return null;
    }

    public boolean UpdateBeaconDeviceDistance(IBeacon ibeaconNode){
        if (ibeaconNode == null)
            return false;

        boolean isFindUpdate = false;
        AlljyonIBeacon alljyonIBeaconNode = findByAlljyonBeaconList(ibeaconNode);
        if(alljyonIBeaconNode != null) {
            if (alljyonIBeaconNode.getUuid() != null) {
                if (DEBUG)
                    Log.d(TAG, "UpdateBeaconDeviceDistance ++ : beaconNode.getDistance()=" + alljyonIBeaconNode.getDistance() + ",ibeaconNode.getAccuracy()=" + ibeaconNode.getAccuracy());

                if (alljyonIBeaconNode.getDistance() != ibeaconNode.getAccuracy()) {
                    alljyonIBeaconNode.setIBeaconData(ibeaconNode);
                    alljyonIBeaconNode.setDistance(ibeaconNode.getAccuracy());
                    alljyonIBeaconNode.setProximity(ibeaconNode.getProximity());
                    alljyonIBeaconNode.setIsAvailable(true);
                    alljyonIBeaconNode.setTimeStamp(GetCurrentTimeStamp());
                    isFindUpdate = true;
                }
            }
        }

        return  isFindUpdate;
    }

    public void notifySequencesChanged() {
        sortDistanceByAsce();
        if(Utils.isLocateMeOn(mContext))
            updateSquences();
    }

    private Timestamp GetCurrentTimeStamp() {
       java.util.Date date = new java.util.Date();
       return new Timestamp(date.getTime());
    }

    public void sortDistanceByAsce() {
        synchronized (mAlljyonIBeaconDevList) {
            Collections.sort(mAlljyonIBeaconDevList, AlljyonIBeacon.getCompByDistance());
        }
        /*synchronized (mAlljyonIBeaconDevList) {
            for (int i = 0; i < mAlljyonIBeaconDevList.size() - 1; i++) {
                for (int j = 1; j < mAlljyonIBeaconDevList.size() - i; j++) {
                    if (mAlljyonIBeaconDevList.get(j - 1).getDistance() > mAlljyonIBeaconDevList.get(j).getDistance()) {
                        Collections.swap(mAlljyonIBeaconDevList, j - 1, j);
                    }
                }
            }
        }*/

        if (TRACE) {
            Log.d(TAG, "sortDistanceByAsce ++.. Sorted list entries: ");
            if(mAlljyonIBeaconDevList != null) {
                for (AlljyonIBeacon beaconNode : mAlljyonIBeaconDevList) {
                    Log.d(TAG, "sortDistanceByAsce  ++, UUID: " + beaconNode.getUuid());
                    Log.d(TAG, "sortDistanceByAsce  ++, distance: " + beaconNode.getDistance());
                }
            }
        }
    }

    public void updateSquences() {
        int sequence = 0;
        boolean isChange = false;

        synchronized (mAlljyonIBeaconDevList) {
            for (AlljyonIBeacon beaconNode : mAlljyonIBeaconDevList) {
                sequence++;
                beaconNode.setSequence(sequence);
                UpdateMicroLocationData(beaconNode);

                if (DEBUG)
                    Log.d(TAG, "updateSquences  ++, sequence=" + sequence + ",beaconNode.getOldSeqence()=" + beaconNode.getOldSeqence());

                //if sequence is 1 and different the OldSequence, then send sequence 1 change notify
                if (sequence == 1 && sequence != beaconNode.getOldSeqence()) {
                    sendFirstSequenceChange(beaconNode);
                }
            }

            //Save current sequence to last sequnce for all list

            for (AlljyonIBeacon beaconNode : mAlljyonIBeaconDevList) {
                if (beaconNode.getSeqence() != beaconNode.getOldSeqence()) {
                    isChange = true;
                }
                beaconNode.setOldSequence(beaconNode.getSeqence());
            }
        }

        //if any sequence change. send all sequence change notify
        if (isChange) {
            if (mSequenceChangeListenerList != null) {
                for (MicroLocationSequenceChangeListener listener : mSequenceChangeListenerList) {
                    if (mAlljyonIBeaconDevList.size() > 0) {
                        listener.onSequenceChange(getAllSequenceData());
                        isChange = false;
                    }
                }
            }
        }
    }

    public void sendFirstSequenceChange(AlljyonIBeacon beaconData) {
        if (beaconData == null)
            return;

        if (mSequenceFirstChangeListenerList != null) {
            for (MicroLocationSequenceFirstChangeListener listener : mSequenceFirstChangeListenerList) {
                listener.onFirstSequence(getFirstSequenceData());
                //Save current sequence to old sequence
                beaconData.setOldSequence(beaconData.getSeqence());
            }
        }
    }

    public void UpdateMicroLocationData(AlljyonIBeacon beaconNode) {
        if (beaconNode == null)
            return;

        beaconNode.getMicroLocationData().setUuid(beaconNode.getUuid());
        beaconNode.getMicroLocationData().setDistance(beaconNode.getDistance());
        beaconNode.getMicroLocationData().setProximity(beaconNode.getProximity());
        beaconNode.getMicroLocationData().setSequence(beaconNode.getSeqence());
    }

    public MicroLocationData getFirstSequenceData() {
        for (AlljyonIBeacon beaconNode: mAlljyonIBeaconDevList) {
            if (beaconNode.getMicroLocationData().getSequence() == 1) {
                if (TRACE) Log.d(TAG, "getFirstSequenceData++ , uuid=" + beaconNode.getMicroLocationData().getUuid());
                if (TRACE) Log.d(TAG, "getFirstSequenceData++ , distance=" + beaconNode.getMicroLocationData().getDistance());
                if (TRACE) Log.d(TAG, "getFirstSequenceData++ , proximity=" + beaconNode.getMicroLocationData().getProximity());
                if (TRACE) Log.d(TAG, "getFirstSequenceData++ , sequence=" + beaconNode.getMicroLocationData().getSequence());
                return beaconNode.getMicroLocationData();
            }
        }
        return null;
    }

    public ArrayList<MicroLocationData> getAllSequenceData(){
        ArrayList<MicroLocationData> allSequenceList = new ArrayList<MicroLocationData>();
        if(mAlljyonIBeaconDevList != null) {
            for (AlljyonIBeacon beaconNode : mAlljyonIBeaconDevList) {
                if (TRACE)
                    Log.d(TAG, "getAllSequenceData++ , uuid=" + beaconNode.getMicroLocationData().getUuid());
                if (TRACE)
                    Log.d(TAG, "getAllSequenceData++ , distance=" + beaconNode.getMicroLocationData().getDistance());
                if (TRACE)
                    Log.d(TAG, "getAllSequenceData++ , proximity=" + beaconNode.getMicroLocationData().getProximity());
                if (TRACE)
                    Log.d(TAG, "getAllSequenceData++ , sequence=" + beaconNode.getMicroLocationData().getSequence());
                allSequenceList.add(beaconNode.getMicroLocationData());
            }
        }

        return allSequenceList;
    }

}
