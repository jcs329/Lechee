package com.quantatw.roomhub.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.quantatw.myapplication.R;
//import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.ui.BluetoothLeService;
import com.quantatw.roomhub.ui.RoomHubApplication;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * Created by erin on 5/3/16.
 */
public class RoomHubBLEController {
    private static final String TAG=RoomHubBLEController.class.getSimpleName();
    private Context mContext;
    private RoomHubManager mRoomHubManager;

    private HandlerThread mThread;
    private BackgroundHandler mBackgroundHandler;

    private BluetoothLeService mBluetoothLeService;

    private ArrayList<RoomHubBleDevice> deviceArrayList;
    private int mConnectDeviceIndex = -1;
    private int mWriteCommandIndex = -1;
    private boolean mHasRegistered = false;
    private BluetoothGattCharacteristic mWriteGattCharacteristic;

    private LinkedHashSet<RoomHubBleListener> roomHubBleListeners = new LinkedHashSet<>();

    public enum JOB_TYPE {
        LOAD_DEVICE,
        AP_TRANSFER
    }
    private JOB_TYPE mJobType = JOB_TYPE.LOAD_DEVICE;

    private String mSsid, mPassword;
    private boolean mIsDone = false;

    class WriteCommand {
        byte[] commandBytes;
        boolean hasChanged;

        WriteCommand(byte[] cmd) {
            this.commandBytes = cmd;
            this.hasChanged = false;
        }

    }

    private enum WriteType {
        SSID,
        PASSWORD
    }

    class CheckWriteCommand {
        ArrayList<WriteCommand> writeCommandArrayList = new ArrayList<>();

        WriteCommand addCommand(byte[] command) {
            WriteCommand cmd = new WriteCommand(command);
            writeCommandArrayList.add(cmd);
            return cmd;
        }

        void setChanged(String command, boolean changed) {
            for(WriteCommand writeCommand: writeCommandArrayList) {
                if(writeCommand.commandBytes != null) {
                    String value = new String(writeCommand.commandBytes);
                    if (value.equals(command)) {
                        writeCommand.hasChanged = changed;
                        break;
                    }
                }
            }
        }

        void clearAll() {
            writeCommandArrayList.clear();
        }

        boolean isAllDone() {
            for(WriteCommand writeCommand:writeCommandArrayList) {
                if(writeCommand.hasChanged == false)
                    return false;
            }
            return true;
        }
    }

    private HashMap<RoomHubBleDevice, CheckWriteCommand> mCheckWriteCommands = new HashMap<>();

    private int mTimeoutCount=0;

    private final String QCI_COMMAND_PREFIX="QCI";
    private final String COMMAND_UUID="9";
    private final String WRITE_COMMAND_UUID=QCI_COMMAND_PREFIX+COMMAND_UUID;
    /*
    * Command 0,1+SSID
    * ex: QCI0HomeWifi
     */
    private final String COMMAND_SSID_INDEX[]={"0","1"};
    /*
    * Command 2,3,4,5+PASSWORD
    * ex: QCI2abcdefghijklmnop,QCI3qrstuvwxyz123456...
     */
    private final String COMMAND_PASS_INDEX[]={"2","3","4","5"};
    /*
    * Command 6+2 bytes
    * 2 bytes:
    * byte 1: Authorize
    * byte 2: Encryption
    * ex: QCI600
     */
    private final String COMMAND_SECURITY="6";
    private final String WRITE_COMMAND_SECURITY=QCI_COMMAND_PREFIX+COMMAND_SECURITY;

    private final int MESSAGE_LOAD_NEXT = 100;

    private final int MESSAGE_CONNECT_DEVICE = 150;
    private final int MESSAGE_DISCONNECT_DEVICE = 160;
    private final int MESSAGE_WRITE_COMMAND_START = 170;
    private final int MESSAGE_WRITE_COMMAND_NEXT = 171;
    private final int MESSAGE_DO_RETRY = 180;

    private final int MESSAGE_GATT_CONNECTED = 200;
    private final int MESSAGE_GATT_DISCONNECTED = 300;
    private final int MESSAGE_GATT_SERVICES_DISCOVERED = 400;
    private final int MESSAGE_DATA_AVAILABLE = 500;

    private final int MESSAGE_GATT_TIMEOUT = 1000;
    private final int STATE_TIMEOUT = 5*1000;  // 5 secs
    private final int MAX_TIMEOUT_COUNT = 1;

    private final class BackgroundHandler extends Handler {

        public BackgroundHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if(mIsDone)
                return;
            switch (msg.what) {
                case MESSAGE_LOAD_NEXT: {
                    loadNext();
                }
                    break;
                case MESSAGE_CONNECT_DEVICE: {
                    mTimeoutCount=0;
                    RoomHubBleDevice roomHubBleDevice = (RoomHubBleDevice)msg.obj;
                    connectDevice(roomHubBleDevice);
                    sendEmptyMessageDelayed(MESSAGE_GATT_TIMEOUT,STATE_TIMEOUT);
                }
                    break;
                case MESSAGE_DISCONNECT_DEVICE:
                    disconnectDevice();
                    sendEmptyMessageDelayed(MESSAGE_GATT_TIMEOUT,STATE_TIMEOUT);
                    break;
                case MESSAGE_WRITE_COMMAND_START: {
                }
                break;
                case MESSAGE_WRITE_COMMAND_NEXT: {
                    log("--- MESSAGE_WRITE_COMMAND_NEXT ---");
                    RoomHubBleDevice roomHubBleDevice = deviceArrayList.get(mConnectDeviceIndex);
                    CheckWriteCommand checkWriteCommand = mCheckWriteCommands.get(roomHubBleDevice);
                    if(++mWriteCommandIndex >= checkWriteCommand.writeCommandArrayList.size()) {
                        // DONE
                        mWriteCommandIndex = -1;
                        return;
                    }
                    log("--- MESSAGE_WRITE_COMMAND_NEXT mWriteCommandIndex="+mWriteCommandIndex);
                    WriteCommand writeCommand = checkWriteCommand.writeCommandArrayList.get(mWriteCommandIndex);
                    boolean result = writeCommands(mWriteGattCharacteristic,writeCommand);
                    sendEmptyMessageDelayed(MESSAGE_GATT_TIMEOUT,STATE_TIMEOUT);
                }
                break;
                case MESSAGE_DO_RETRY: {
                    RoomHubBleDevice roomHubBleDevice = deviceArrayList.get(mConnectDeviceIndex);
                    roomHubBleDevice.setRetryCount(roomHubBleDevice.getRetryCount()+1);
                    log("--- MESSAGE_DO_RETRY --- retryCount="+roomHubBleDevice.getRetryCount());
                    sendMessage(obtainMessage(MESSAGE_CONNECT_DEVICE,roomHubBleDevice));
                }
                break;
                case MESSAGE_GATT_CONNECTED: {
                    RoomHubBleDevice roomHubBleDevice = deviceArrayList.get(mConnectDeviceIndex);
                    roomHubBleDevice.setDeviceBLEState(RoomHubBleDevice.DeviceBLEState.CONNECTED);
                    removeMessages(MESSAGE_GATT_TIMEOUT);
                    sendEmptyMessageDelayed(MESSAGE_GATT_TIMEOUT,STATE_TIMEOUT);
                }
                    break;
                case MESSAGE_GATT_DISCONNECTED: {
                    mTimeoutCount=0;
                    removeMessages(MESSAGE_GATT_TIMEOUT);
                    RoomHubBleDevice roomHubBleDevice = deviceArrayList.get(mConnectDeviceIndex);
                    mBluetoothLeService.close();
                    log("--- MESSAGE_GATT_DISCONNECTED --- current state="+roomHubBleDevice.getDeviceBLEState());
                    if(roomHubBleDevice.getDeviceBLEState() <= RoomHubBleDevice.DeviceBLEState.WRITE
                            && !roomHubBleDevice.isRetryFinished()) {
                        sendEmptyMessageDelayed(MESSAGE_DO_RETRY,500);
                        return;
                    }
                    else {
                        if(roomHubBleDevice.isRetryFinished()) {
                            log("This device has retried for 2 times, load the next one!");
                        }
                    }

                    roomHubBleDevice.setDeviceBLEState(RoomHubBleDevice.DeviceBLEState.DISCONNECTED);
                    sendEmptyMessageDelayed(MESSAGE_LOAD_NEXT,1000);
                }
                    break;
                case MESSAGE_GATT_SERVICES_DISCOVERED: {
                    RoomHubBleDevice roomHubBleDevice = deviceArrayList.get(mConnectDeviceIndex);
                    roomHubBleDevice.setDeviceBLEState(RoomHubBleDevice.DeviceBLEState.SERVICE_DISCOVERED);
                    removeMessages(MESSAGE_GATT_TIMEOUT);
                    handleServices();
                }
                    break;
                case MESSAGE_DATA_AVAILABLE: {
                    RoomHubBleDevice roomHubBleDevice = deviceArrayList.get(mConnectDeviceIndex);
                    roomHubBleDevice.setDeviceBLEState(RoomHubBleDevice.DeviceBLEState.NOTIFIED);
                    if(mJobType == JOB_TYPE.LOAD_DEVICE) {
                        String uuid = (String) msg.obj;
                        // skip write command feedback, only wait for notification feedback
                        if (!TextUtils.isEmpty(uuid) && !uuid.equals(WRITE_COMMAND_UUID)) {
                            removeMessages(MESSAGE_GATT_TIMEOUT);
                            roomHubBleDevice.setRoomHubUuid(uuid);
                            RoomHubData roomHubData = mRoomHubManager.getRoomHubDataByUuid(roomHubBleDevice.getRoomHubUuid());
                            boolean skipOwner = mContext.getResources().getBoolean(R.bool.config_test_ap_transfer_skip_owner);
                            if(skipOwner) {
                                if(roomHubData != null) {
                                    roomHubBleDevice.setRoomHubName(roomHubData.getName());
                                    notifyLoaded(roomHubBleDevice);
                                }
                                else {
                                    log("Can't find roomHubData by Uuid:"+roomHubBleDevice.getRoomHubUuid());
                                }
                            }
                            else {
                                if (roomHubData != null && roomHubData.IsOwner()) {
                                    roomHubBleDevice.setRoomHubName(roomHubData.getName());
                                    notifyLoaded(roomHubBleDevice);
                                } else {
                                    log("Can't find roomHubData by Uuid:" + roomHubBleDevice.getRoomHubUuid());
                                }
                            }
                            sendEmptyMessage(MESSAGE_DISCONNECT_DEVICE);
                        }
                    }
                    else if(mJobType == JOB_TYPE.AP_TRANSFER) {
                        removeMessages(MESSAGE_GATT_TIMEOUT);
                        String value = (String) msg.obj;
                        log("value="+value);
                        CheckWriteCommand checkWriteCommand = mCheckWriteCommands.get(roomHubBleDevice);
                        checkWriteCommand.setChanged(value,true);
                        boolean isDone = checkWriteCommand.isAllDone();
                        log("isDone="+isDone);
                        if(isDone) {
                            sendEmptyMessageDelayed(MESSAGE_DISCONNECT_DEVICE,2000);
                        }
                        else
                            sendEmptyMessage(MESSAGE_WRITE_COMMAND_NEXT);
                    }
                }
                    break;
                case MESSAGE_GATT_TIMEOUT: {
                    RoomHubBleDevice roomHubBleDevice = deviceArrayList.get(mConnectDeviceIndex);
                    log("--- MESSAGE_GATT_TIMEOUT ---");
                    if(++mTimeoutCount > MAX_TIMEOUT_COUNT)
                    {
                        /*
                        * For the weird situation is that service doesn't received DISCONNECT event at all
                         */
                        mBluetoothLeService.close();
                        if(!roomHubBleDevice.isRetryFinished()) {
                            log("!!!Weird timeout has 2 times , do Retry");
                            mTimeoutCount = 0;
                            sendEmptyMessageDelayed(MESSAGE_DO_RETRY,500);
                        }
                        else {
                            log("!!!Weird timeout has retried 2 times , close it and load the next one");
                            sendEmptyMessageDelayed(MESSAGE_LOAD_NEXT,500);
                        }
                        return;
                    }
                    sendEmptyMessage(MESSAGE_DISCONNECT_DEVICE);
                }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            log("onReceive","action="+action);
            if(mIsDone)
                return;
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mBackgroundHandler.sendEmptyMessage(MESSAGE_GATT_CONNECTED);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mBackgroundHandler.sendEmptyMessage(MESSAGE_GATT_DISCONNECTED);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                mBackgroundHandler.sendEmptyMessage(MESSAGE_GATT_SERVICES_DISCOVERED);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String uuid = intent.getStringExtra(BluetoothLeService.EXTRA_DATA_UUID);
                String value = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_DATA_AVAILABLE, value));
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void init() {
        mThread = new HandlerThread("RoomHubBLEController");
        mThread.start();
        mBackgroundHandler = new BackgroundHandler(mThread.getLooper());
    }

    private void close() {
        if(mHasRegistered) {
            mContext.unregisterReceiver(mGattUpdateReceiver);
            mHasRegistered=false;
        }
        if (mThread != null ) {
            mThread.getLooper().quit();
            mThread.quit();
            mThread = null;
        }
    }

    private void notifyLoadStart() {
        for (Iterator<RoomHubBleListener> it = roomHubBleListeners.iterator(); it.hasNext(); ) {
            RoomHubBleListener roomHubBleListener = it.next();
            roomHubBleListener.onLoadDeviceStart(mJobType);
        }
    }

    private void notifyLoaded(RoomHubBleDevice roomHubBleDevice) {
        for (Iterator<RoomHubBleListener> it = roomHubBleListeners.iterator(); it.hasNext(); ) {
            RoomHubBleListener roomHubBleListener = it.next();
            roomHubBleListener.onLoadDevice(mJobType,roomHubBleDevice);
        }
    }

    private void notifyLoadDone() {
        mHasRegistered=false;
        mContext.unregisterReceiver(mGattUpdateReceiver);
        for (Iterator<RoomHubBleListener> it = roomHubBleListeners.iterator(); it.hasNext(); ) {
            RoomHubBleListener roomHubBleListener = it.next();
            roomHubBleListener.onLoadDeviceDone(mJobType);
        }
    }

    private ArrayList<byte[]> getCommandData(byte[] value) {
        ArrayList<byte[]> stringArrayList = new ArrayList<>();
        int commandNumbers = 1;
        if(value.length > 16) {
            if(value.length%16==0)
                commandNumbers = value.length/16;
            else
                commandNumbers = value.length/16+1;
        }
        for (int i = 0; i < commandNumbers; i++) {
            int startPos = 0;
            int endPos = 0;

            if(i > 0)
                startPos = i * 16;
            endPos = startPos + 16;
            if(endPos>value.length)
                endPos = value.length;
            byte[] subValue = Arrays.copyOfRange(value,startPos,endPos);
            stringArrayList.add(subValue);
        }
        return stringArrayList;
    }

    private void addLongCommandData(WriteType writeType, CheckWriteCommand checkWriteCommand, byte[] value) {
        ArrayList<byte[]> commandStrings = getCommandData(value);
        for(int i=0;i<commandStrings.size();i++) {
            String prefix=null;
            if(writeType == WriteType.SSID)
                prefix = QCI_COMMAND_PREFIX+COMMAND_SSID_INDEX[i];
            else if(writeType == WriteType.PASSWORD)
                prefix = QCI_COMMAND_PREFIX+COMMAND_PASS_INDEX[i];
            byte[] prefixBytes = prefix.getBytes(StandardCharsets.US_ASCII);
            byte[] combinedBytes = new byte[prefixBytes.length+commandStrings.get(i).length];
            for(int j=0;j<combinedBytes.length;j++)
                combinedBytes[j] = j < prefixBytes.length ? prefixBytes[j] : commandStrings.get(i)[j-prefixBytes.length];
            checkWriteCommand.addCommand(combinedBytes);
        }
    }

    private void handleServices() {
        RoomHubBleDevice currentRoomHubBleDevice = deviceArrayList.get(mConnectDeviceIndex);

        /* for debug:::
        List<BluetoothGattService> gattServiceArrayList = mBluetoothLeService.getSupportedGattServices();
        log("getSupportedGattServices: ");
        for(BluetoothGattService bluetoothGattService:gattServiceArrayList) {
            log(bluetoothGattService.getUuid().toString());
            List<BluetoothGattCharacteristic> bluetoothGattCharacteristics = bluetoothGattService.getCharacteristics();
            log("getCharacteristics");
            for(BluetoothGattCharacteristic bluetoothGattCharacteristic:bluetoothGattCharacteristics) {
                log(bluetoothGattCharacteristic.getUuid().toString());
            }
        }*/

        BluetoothGattService service = mBluetoothLeService.getBindGattServices(RoomHubGattAttributes.QCI_SERVICE);
        if(service != null) {
            // Enable notification
            BluetoothGattCharacteristic gattCharacteristic_notify =
                    service.getCharacteristic(UUID.fromString(RoomHubGattAttributes.CHARACTERISTIC_NOTIFY));
            if(mJobType == JOB_TYPE.LOAD_DEVICE && gattCharacteristic_notify != null) {
                mBluetoothLeService.setCharacteristicNotification(
                        gattCharacteristic_notify, true);
                currentRoomHubBleDevice.setDeviceBLEState(RoomHubBleDevice.DeviceBLEState.ENABLE_NOTIFY);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Write command
            BluetoothGattCharacteristic gattCharacteristic_write =
                    service.getCharacteristic(UUID.fromString(RoomHubGattAttributes.CHARACTERISTIC_WRITE));
            if(gattCharacteristic_write != null) {
                mWriteGattCharacteristic = gattCharacteristic_write;
                currentRoomHubBleDevice.setDeviceBLEState(RoomHubBleDevice.DeviceBLEState.WRITE);
                if(mJobType == JOB_TYPE.LOAD_DEVICE) {
                    writeCharacteristic(gattCharacteristic_write, WRITE_COMMAND_UUID);
                    mBackgroundHandler.sendEmptyMessageDelayed(MESSAGE_GATT_TIMEOUT,STATE_TIMEOUT);
                }
                else if(mJobType == JOB_TYPE.AP_TRANSFER) {
                    CheckWriteCommand checkWriteCommand = mCheckWriteCommands.get(currentRoomHubBleDevice);
                    // SSID
                    try {
                        addLongCommandData(WriteType.SSID, checkWriteCommand, mSsid.getBytes("utf-8"));
                    }catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    // PASSWORD
                    addLongCommandData(WriteType.PASSWORD, checkWriteCommand, mPassword.getBytes());

                    // AUTH/ENCRYPT
                    checkWriteCommand.addCommand(new String(WRITE_COMMAND_SECURITY + "00").getBytes());

                    mWriteCommandIndex=-1;
                    mBackgroundHandler.sendEmptyMessage(MESSAGE_WRITE_COMMAND_NEXT);
                }
            }
        }
        else {
            log("Service not found!!!");
            mBackgroundHandler.sendEmptyMessage(MESSAGE_DISCONNECT_DEVICE);
        }
    }

    private boolean writeCommands(BluetoothGattCharacteristic gattCharacteristic, WriteCommand writeCommand) {
        boolean result = writeCharacteristic(gattCharacteristic, writeCommand.commandBytes);
//        log("writeCommands result="+result);
        return result;
    }

    private boolean writeCharacteristic(BluetoothGattCharacteristic gattCharacteristic, String string) {
        byte[] bytes = string.getBytes(StandardCharsets.US_ASCII);
        boolean ret = gattCharacteristic.setValue(bytes);
        mBluetoothLeService.writeCharateristic(gattCharacteristic);
        return ret;
    }

    private boolean writeCharacteristic(BluetoothGattCharacteristic gattCharacteristic, byte[] bytes) {
        boolean ret = gattCharacteristic.setValue(bytes);
        mBluetoothLeService.writeCharateristic(gattCharacteristic);
        return ret;
    }

    private void connectDevice(RoomHubBleDevice roomHubBleDevice) {
        log("connectDevice","BluetoothDevice="+roomHubBleDevice.getBluetoothDevice().getAddress());
        mBluetoothLeService.connect(roomHubBleDevice.getBluetoothDevice().getAddress());
    }

    private void disconnectDevice() {
        if(mBluetoothLeService == null)
            return;
        mBluetoothLeService.disconnect();
    }

    private void loadNext() {
        log("loadNext mConnectDeviceIndex="+mConnectDeviceIndex+", list size="+deviceArrayList.size());
        if(++mConnectDeviceIndex >= deviceArrayList.size()) {
            // all devices has been connected done:
            mIsDone=true;
            notifyLoadDone();
            return;
        }

        for(int i=0;i<deviceArrayList.size();i++) {
            if(i == mConnectDeviceIndex) {
                mBackgroundHandler.sendMessage(
                        mBackgroundHandler.obtainMessage(MESSAGE_CONNECT_DEVICE,deviceArrayList.get(i)));
                break;
            }
        }
    }

    private void startup(JOB_TYPE jobType, ArrayList<RoomHubBleDevice> roomHubBleDevices) {
        notifyLoadStart();
        mIsDone = false;
        mJobType = jobType;
        deviceArrayList = roomHubBleDevices;
        mConnectDeviceIndex = -1;
        mHasRegistered = true;
        mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        mBackgroundHandler.sendEmptyMessage(MESSAGE_LOAD_NEXT);
    }


    public RoomHubBLEController(Context context) {
        mContext = context;
        mRoomHubManager = ((RoomHubApplication)context.getApplicationContext()).getRoomHubManager();
        init();
    }

    public void destroy() {
        close();
    }

    public void setBluetoothLeService(BluetoothLeService bluetoothLeService) {
        mBluetoothLeService = bluetoothLeService;
    }

    public void doAPTransfer(ArrayList<RoomHubBleDevice> roomHubBleDevices, String ssid, String password) {
        mSsid = ssid;
        mPassword = password;
        mCheckWriteCommands.clear();
        for(RoomHubBleDevice roomHubBleDevice: roomHubBleDevices) {
            mCheckWriteCommands.put(roomHubBleDevice,new CheckWriteCommand());
        }
        startup(JOB_TYPE.AP_TRANSFER, roomHubBleDevices);
    }

    public void loadDevices(ArrayList<RoomHubBleDevice> roomHubBleDevices) {
        log("loadDevices","roomHubBleDevices ="+roomHubBleDevices.size());
        startup(JOB_TYPE.LOAD_DEVICE, roomHubBleDevices);
    }

    public void registerBLEListener(RoomHubBleListener roomHubBleListener) {
        roomHubBleListeners.add(roomHubBleListener);
    }

    public void unregisterBLEListener(RoomHubBleListener roomHubBleListener) {
        roomHubBleListeners.remove(roomHubBleListener);
    }

    public JOB_TYPE getCurrentJobType() {
        return mJobType;
    }

    public void terminate() {
        if(mJobType == JOB_TYPE.LOAD_DEVICE) {
            disconnectDevice();
            mBluetoothLeService.close();
        }
    }

    public static void log(String msg) {
        log("",msg);
    }

    public static void log(String tag, String msg) {
        Log.d(TAG,"["+tag+"] "+msg);
    }

}
