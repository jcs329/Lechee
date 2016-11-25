package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.ble.RoomHubBLEController;
import com.quantatw.roomhub.ble.RoomHubBleDevice;
import com.quantatw.roomhub.ble.RoomHubBleListener;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.PreferenceEditor;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by erin on 4/29/16.
 */
public class APTransferActivity extends AbstractRoomHubActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener,RoomHubBleListener{
    private final String TAG=APTransferActivity.class.getSimpleName();

    private Context mContext;

    private ListView mRoomHubList;
    private ProgressDialog mProgressDialog;

    private RoomHubBLEController mRoomHubBLEController;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLeScanner;
    private ScanCallback mScanCallback;

    private ArrayList<RoomHubBleDevice> mDeviceList = new ArrayList<>();
    private ArrayList<RoomHubBleDevice> mTransferList = new ArrayList<>();
    private ArrayList<RoomHubBleDevice> roomHubBleDeviceArrayList = new ArrayList<>();

    private DeviceAdapter mAdapter;
    private boolean mScanning = false;
    private int mScanDuration;
    private Runnable mScanRunnable;

    private final int MESSAGE_SCAN_START = 100;
    private final int MESSAGE_SCAN_STOP = 101;

    private final int MESSAGE_DO_LOAD = 200;
    private final int MESSAGE_LOAD_START = 201;
    private final int MESSAGE_LOAD_TIMEOUT = 202;
    private final int MESSAGE_ONLOAD_DEVICE = 203;
    private final int MESSAGE_LOAD_DONE = 204;
    private final int MESSAGE_AP_TRANSFER_START = 300;
    private final int MESSAGE_FORCE_EXIT = 350;
    private final int MESSAGE_RESTART = 351;

    private final int MESSAGE_REFRESH_LIST = 500;
    private final int MESSAGE_NO_DEVICE = 600;

    private final String KEY_JOB_TYPE = "JobType";
    private final String KEY_DEVICE = "device";

    private final int REQUEST_CODE_SETUP_HOME_WIFI_DONE = 999;
    private final int REQUEST_ENABLE_BT = 900;

    private class ViewHolder {
        TextView roomHubName;
        ImageView confirmImageView;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SCAN_START:
                    if(mProgressDialog == null)
                        mProgressDialog = ProgressDialog.show(
                                mContext, "", getString(R.string.change_wifi_scanning), true, true, new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        showConfirmCancelDialog();
                                    }
                                });
                    else {
                        mProgressDialog.setMessage(getString(R.string.change_wifi_scanning));
                        mProgressDialog.show();
                    }
                    roomHubBleDeviceArrayList.clear();
                    mDeviceList.clear();
                    mTransferList.clear();
                    scanLeDevice(true);
                    break;
                case MESSAGE_SCAN_STOP:
                    log("--- MESSAGE_SCAN_STOP ---");
                    sendEmptyMessage(MESSAGE_DO_LOAD);
                    break;
                case MESSAGE_DO_LOAD:
                    log("--- MESSAGE_DO_LOAD ---");
                    if(roomHubBleDeviceArrayList.size() > 0) {
                        mProgressDialog.setMessage(getString(R.string.change_wifi_reading));
                        loadDevices();
                    }
                    else
                        mHandler.sendEmptyMessage(MESSAGE_NO_DEVICE);
                    break;
                case MESSAGE_LOAD_START: {
                    RoomHubBLEController.JOB_TYPE type = (RoomHubBLEController.JOB_TYPE)msg.getData().getSerializable(KEY_JOB_TYPE);
                    log("--- MESSAGE_LOAD_START --- type="+type);
                }
                    break;
                case MESSAGE_LOAD_TIMEOUT:
                    break;
                case MESSAGE_ONLOAD_DEVICE: {
                    Bundle bundle = msg.getData();
                    RoomHubBLEController.JOB_TYPE type = (RoomHubBLEController.JOB_TYPE)msg.getData().getSerializable(KEY_JOB_TYPE);
                    RoomHubBleDevice roomHubBleDevice = (RoomHubBleDevice)bundle.getParcelable(KEY_DEVICE);
                    log("--- MESSAGE_ONLOAD_DEVICE --- type="+type+",roomHubBleDevice="+roomHubBleDevice);
                    if(type == RoomHubBLEController.JOB_TYPE.LOAD_DEVICE) {
                        addToList(roomHubBleDevice);
//                        String numbers = mDeviceList.size() > 0 ? Integer.toString(mDeviceList.size()) : "";
//                        mProgressDialog.setMessage("Reading..." + numbers);
                    }
                    else {  // AP Transfer

                    }
                }
                    break;
                case MESSAGE_LOAD_DONE: {
                    RoomHubBLEController.JOB_TYPE type = (RoomHubBLEController.JOB_TYPE)msg.getData().getSerializable(KEY_JOB_TYPE);
                    log("--- MESSAGE_LOAD_DONE --- type="+type);
                    mProgressDialog.dismiss();
                    if(type == RoomHubBLEController.JOB_TYPE.LOAD_DEVICE) {
                        if (mDeviceList.size() == 0)
                            mHandler.sendEmptyMessage(MESSAGE_NO_DEVICE);
                        else
                            mHandler.sendEmptyMessage(MESSAGE_REFRESH_LIST);
                    }
                    else {  // AP Transfer
                        showDoneDialog();
                    }

                }
                    break;
                case MESSAGE_AP_TRANSFER_START:
                    startApTranfer();
                    break;
                case MESSAGE_REFRESH_LIST:
                    log("","--- MESSAGE_REFRESH_LIST ---");
                    refreshList();
                    break;
                case MESSAGE_NO_DEVICE:
                    showNoDevicesDialog();
                    break;
                case MESSAGE_FORCE_EXIT:
                    forceExit();
                    break;
                case MESSAGE_RESTART:
                    restart();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private void showDoneDialog() {
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.custom_dialog);
        TextView txt_msg = (TextView) dialog.findViewById(R.id.txt_message);
        txt_msg.setText(R.string.change_wifi_done);

        Button btn_yes = (Button) dialog.findViewById(R.id.btn_yes);
        btn_yes.setText(R.string.exit);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        Button btn_no = (Button) dialog.findViewById(R.id.btn_no);
        btn_no.setVisibility(View.GONE);

        dialog.setCancelable(false);
        dialog.show();
    }

    private void showNoDevicesDialog() {
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.custom_dialog);
        TextView txt_msg = (TextView) dialog.findViewById(R.id.txt_message);
        txt_msg.setText(R.string.change_wifi_no_devices);

        Button btn_yes = (Button) dialog.findViewById(R.id.btn_yes);
        btn_yes.setText(R.string.change_wifi_rescan);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mHandler.sendEmptyMessage(MESSAGE_SCAN_START);
            }
        });

        Button btn_no = (Button) dialog.findViewById(R.id.btn_no);
        btn_no.setText(R.string.exit);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void startApTranfer() {
        PreferenceEditor pref = new PreferenceEditor(this, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        String ssid = pref.getStringValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SSID);
        String password = pref.getStringValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_PASSWORD);

        mProgressDialog.setMessage(getString(R.string.change_wifi_start));
        mProgressDialog.show();
        mRoomHubBLEController.doAPTransfer(mTransferList, ssid, password);
    }

    private void refreshList() {
        mAdapter.notifyDataSetChanged();
    }

    private void scanLeDevice(final boolean enable) {
        // Stops scanning after a pre-defined scan period.
        if(Build.VERSION.SDK_INT > 21) {
            if (enable) {
                mHandler.postDelayed(mScanRunnable=new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        if(mLeScanner != null && mBluetoothAdapter.isEnabled())
                            mLeScanner.stopScan(mScanCallback);
                        mHandler.sendEmptyMessage(MESSAGE_SCAN_STOP);
                    }
                }, mScanDuration);

                mScanning = true;

                mLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                ScanSettings.Builder scanSettings = new ScanSettings.Builder();
                if(mBluetoothAdapter.isOffloadedScanBatchingSupported())
                    scanSettings.setReportDelay(500);
                scanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);

                if(mScanCallback == null)
                    mScanCallback = initScanCallback();

                String test_ble_mac = getResources().getString(R.string.config_test_ap_transfer_ble_mac);
                if(!TextUtils.isEmpty(test_ble_mac)) {
                    ArrayList<ScanFilter> scanFilters = new ArrayList<>();
                    ScanFilter scanFilter = new ScanFilter.Builder()
                            .setDeviceName(getString(R.string.config_roomhub_ble_name_prefix))
                            .setDeviceAddress(test_ble_mac)
                            .build();
                    scanFilters.add(scanFilter);
                    mLeScanner.startScan(scanFilters, scanSettings.build(), mScanCallback);
                }
                else {
                    mLeScanner.startScan(null, scanSettings.build(), mScanCallback);
                }
            } else {
                mScanning = false;
                if(mLeScanner != null && mBluetoothAdapter.isEnabled())
                    mLeScanner.stopScan(mScanCallback);
            }
        }
        else {
            if(enable) {
                mHandler.postDelayed(mScanRunnable=new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mHandler.sendEmptyMessage(MESSAGE_SCAN_STOP);
                    }
                }, mScanDuration);

                mScanning = true;
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }

    private boolean isExist(BluetoothDevice bluetoothDevice) {
        for(RoomHubBleDevice device: roomHubBleDeviceArrayList) {
            if(device.getBluetoothDevice().equals(bluetoothDevice))
                return true;
        }
        return false;
    }

    private void addRoomHubBleDevice(BluetoothDevice bluetoothDevice) {
        if(isExist(bluetoothDevice)) {
//            log("addRoomHubBleDevice","is exist! "+bluetoothDevice.toString());
            return;
        }

//        log("addRoomHubBleDevice","add to list: "+bluetoothDevice.toString());
        roomHubBleDeviceArrayList.add(new RoomHubBleDevice(bluetoothDevice));
    }

    private ScanCallback initScanCallback() {
        if(Build.VERSION.SDK_INT > 21) {
            // Device scan callback.
            return new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
//                    log("onScanResult callbackType", String.valueOf(callbackType));
//                    log("onScanResult result", result.toString());
                    BluetoothDevice btDevice = result.getDevice();
                    if(result.getScanRecord().getDeviceName() != null &&
                            result.getScanRecord().getDeviceName().equals(
                                    getResources().getString(R.string.config_roomhub_ble_name_prefix))) {
                            addRoomHubBleDevice(btDevice);
                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult sr : results) {
//                        log("onBatchScanResults - device:", sr.getScanRecord().getDeviceName() + "/" + sr.getDevice().getAddress() + "\n" + sr.toString());
//                        roomHubBleDeviceArrayList.add(new RoomHubBleDevice(sr.getDevice()));
                        if(sr.getScanRecord().getDeviceName() != null &&
                                (sr.getScanRecord().getDeviceName().equals(
                                        getResources().getString(R.string.config_roomhub_ble_name_prefix)))) {
                            addRoomHubBleDevice(sr.getDevice());
                        }
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    log("Scan Failed", "Error Code: " + errorCode);
                    // TODO: show error?
                }
            };
        }
        return null;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
//                    log(TAG, "onLeScan device:" + device.getName() + ",address:" + device.getAddress());
                    if (device.getName() != null &&
                            device.getName().equals(
                                    getResources().getString(R.string.config_roomhub_ble_name_prefix))) {
                        addRoomHubBleDevice(device);
//                        roomHubBleDeviceArrayList.add(new RoomHubBleDevice(device));
                    }
                }
            };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            log("onServiceConnected","enter");
            if (!mBluetoothLeService.initialize()) {
                log("onServiceConnected", "Unable to initialize BluetoothLeService");
                return;
            }
            mRoomHubBLEController.setBluetoothLeService(mBluetoothLeService);
            if(mBluetoothAdapter.isEnabled())
                mHandler.sendEmptyMessage(MESSAGE_SCAN_START);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            log("onServiceDisconnected","enter");
            mRoomHubBLEController.setBluetoothLeService(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_transfer);

        mContext = this;

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mRoomHubBLEController = new RoomHubBLEController(this);
        mRoomHubBLEController.registerBLEListener(this);

        mScanDuration = getResources().getInteger(R.integer.config_search_devices_duration)*1000;

        mRoomHubList = (ListView)findViewById(R.id.lstRoomhub);
        mRoomHubList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mAdapter = new DeviceAdapter(mDeviceList);
        mRoomHubList.setAdapter(mAdapter);
        mRoomHubList.setOnItemClickListener(this);

        Button btnChange = (Button)findViewById(R.id.btnChange);
        btnChange.setOnClickListener(this);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRoomHubBLEController.unregisterBLEListener(this);
        unbindService(mServiceConnection);
        mRoomHubBLEController.destroy();
    }

    @Override
    public void onLoadDeviceDone(RoomHubBLEController.JOB_TYPE workingType) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_JOB_TYPE,workingType);
        Message message = (mHandler.obtainMessage(MESSAGE_LOAD_DONE));
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    @Override
    public void onLoadDeviceStart(RoomHubBLEController.JOB_TYPE workingType) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_JOB_TYPE,workingType);
        Message message = (mHandler.obtainMessage(MESSAGE_LOAD_START));
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    @Override
    public void onLoadDevice(RoomHubBLEController.JOB_TYPE workingType, RoomHubBleDevice roomHubBleDevice) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_JOB_TYPE,workingType);
        bundle.putParcelable(KEY_DEVICE,roomHubBleDevice);
        Message message = mHandler.obtainMessage(MESSAGE_ONLOAD_DEVICE);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int first_visible=mRoomHubList.getFirstVisiblePosition();
        int last_visible=mRoomHubList.getLastVisiblePosition();

        if((position >= first_visible) && (position <= last_visible)) {
            int pos = position - first_visible;
            View currentView = mRoomHubList.getChildAt(pos);
            ViewHolder viewHolder = (ViewHolder)currentView.getTag();
            if(viewHolder != null) {
                if(viewHolder.confirmImageView.getVisibility()==View.INVISIBLE) {
                    viewHolder.confirmImageView.setVisibility(View.VISIBLE);
                    currentView.setSelected(true);
                }
                else {
                    viewHolder.confirmImageView.setVisibility(View.INVISIBLE);
                    currentView.setSelected(false);
                }
            }
        }

    }

    @Override
    public void onClick(View v) {
        mTransferList.clear();
        SparseBooleanArray sparseBooleanArray = mRoomHubList.getCheckedItemPositions();
        int size = sparseBooleanArray.size();
        for(int i=0;i<size;i++) {
//            log("","keyAt pos="+sparseBooleanArray.keyAt(i));
//            log("","valueAt pos="+sparseBooleanArray.valueAt(i));
            if(sparseBooleanArray.valueAt(i)==true) {
                mTransferList.add((RoomHubBleDevice)mAdapter.getItem(sparseBooleanArray.keyAt(i)));
            }
        }
        log("","mTransferList size="+mTransferList.size());

        Intent intent = new Intent(this, WifiList.class);
        intent.putExtra(GlobalDef.USE_TYPE,GlobalDef.TYPE_AP_TRANSFER);
        intent.putParcelableArrayListExtra(GlobalDef.AP_TRANSFER_LIST,mTransferList);
        startActivityForResult(intent,REQUEST_CODE_SETUP_HOME_WIFI_DONE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_SETUP_HOME_WIFI_DONE && resultCode==RESULT_OK) {
            log("onActivityResult enter");
            mHandler.sendEmptyMessage(MESSAGE_AP_TRANSFER_START);
        }
        else if(requestCode == REQUEST_ENABLE_BT) {
            if(mBluetoothAdapter.isEnabled())
                mHandler.sendEmptyMessage(MESSAGE_SCAN_START);
            else
                finish();
        }
    }

    private void forceExit() {
        log("forceExit enter");

        if(mScanning == true) {
            mHandler.removeCallbacks(mScanRunnable);
            mScanning=false;
        }
        else {
            scanLeDevice(false);
        }

        if(mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        if(mRoomHubBLEController != null)
            mRoomHubBLEController.terminate();

        finish();
    }

    private void restart() {
        if(mScanning == true) {
            mHandler.removeCallbacks(mScanRunnable);
            mScanning=false;
        }
        else {
            scanLeDevice(false);
        }
        if(mBluetoothAdapter.isEnabled())
            mHandler.sendEmptyMessage(MESSAGE_SCAN_START);
    }

    private void showConfirmCancelDialog() {
        final Dialog dialog = new Dialog(mContext,R.style.CustomDialog);

        int msg_resource = R.string.change_wifi_exit_scan_msg;
        if(mRoomHubBLEController != null
                && mRoomHubBLEController.getCurrentJobType()== RoomHubBLEController.JOB_TYPE.AP_TRANSFER)
            msg_resource = R.string.change_wifi_exit_ongoing_msg;

        dialog.setContentView(R.layout.custom_dialog);
        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(msg_resource));

        final boolean forceExit = msg_resource==R.string.change_wifi_exit_scan_msg?true:false;
        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(forceExit)
                    mHandler.sendEmptyMessage(MESSAGE_FORCE_EXIT);
                else
                    mProgressDialog.show();
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        if(!forceExit) {
            btn_no.setVisibility(View.GONE);
        }
        else {
            btn_no.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    mProgressDialog.show();
                }
            });
        }

        dialog.show();
    }

    private void loadDevices() {
        mRoomHubBLEController.loadDevices(roomHubBleDeviceArrayList);
    }

    private void addToList(RoomHubBleDevice roomHubBleDevice) {
        String roomHubUuid = roomHubBleDevice.getRoomHubUuid();
        log("addToList","roomHubUuid="+roomHubUuid);
        mDeviceList.add(roomHubBleDevice);
    }

    private class DeviceAdapter extends BaseAdapter {
        ArrayList<RoomHubBleDevice> roomHubDataArrayList;
        LayoutInflater inflater;

        DeviceAdapter(ArrayList<RoomHubBleDevice> list) {
            roomHubDataArrayList = list;
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if(roomHubDataArrayList != null)
                return roomHubDataArrayList.size();
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if(roomHubDataArrayList != null)
                return roomHubDataArrayList.get(position);
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder viewHolder;
            if (view == null) {
                view = inflater.inflate(R.layout.ap_transfer_roomhub_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.roomHubName = (TextView)view.findViewById(R.id.txtRoomhubName);
                viewHolder.confirmImageView = (ImageView)view.findViewById(R.id.img_confirm_icon);
                view.setTag(viewHolder);
            }
            else
                viewHolder = (ViewHolder)view.getTag();

            RoomHubBleDevice roomHubBleDevice = (RoomHubBleDevice)getItem(position);
            if(roomHubBleDevice != null) {
                viewHolder.roomHubName.setText(roomHubBleDevice.getRoomHubName());
                viewHolder.confirmImageView.setSelected(true);
                mRoomHubList.setItemChecked(position,true);
            }

            return view;
        }
    }

    private void log(String msg) {
        RoomHubBLEController.log(msg);
    }

    private void log(String item,String msg) {
        RoomHubBLEController.log(item, msg);
    }
}
