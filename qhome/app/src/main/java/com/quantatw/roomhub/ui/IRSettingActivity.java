package com.quantatw.roomhub.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.ir.ApIRBrandData;
import com.quantatw.roomhub.ir.ApIRModelData;
import com.quantatw.roomhub.ir.ApIRParingInfo;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.IRController;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.CloudDevice;

import java.util.ArrayList;

/**
 * Created by erin on 10/12/15.
 */
public class IRSettingActivity extends AbstractRoomHubActivity implements
AdapterView.OnItemClickListener, TextView.OnEditorActionListener,RoomHubChangeListener {
    private final String TAG=IRSettingActivity.class.getSimpleName();
    private Context mContext;
    private IRController mIRController;

    private EditText mSearchEdit;
    private ListView mListView;
    private IRBrandListAdapter mAdapter;
    /* RoomHub uuid */
    private String mUuid;
    /* Asset uuid */
    private String mAssetUuid;
    /* Asset type */
    private int mAssetType;

    private final int MESSAGE_GET_LIST_DONE = 100;
    private final int MESSAGE_SEARCH_TIMEOUT = 200;
    private final int MESSAGE_LAUNCH_DEVICE_LIST = 300;

    private RoomHubManager mRoomHubMgr;

    private ArrayList<ApIRModelData> modelList;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_GET_LIST_DONE:
                    dismissProgressDialog();
                    ArrayList<ApIRBrandData> list = (ArrayList<ApIRBrandData>)msg.obj;
                    setAdapterView(list);
                    break;
                case MESSAGE_SEARCH_TIMEOUT:
                    dismissProgressDialog();
                    Toast.makeText(mContext,"Can't find any devices!",Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_LAUNCH_DEVICE_LIST:
                    launchDeviceList();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private void setAdapterView(ArrayList<ApIRBrandData> list) {
        if(list != null && list.size() > 0 ) {
            mAdapter = new IRBrandListAdapter(list);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(this);
        }
        else {
            Toast.makeText(mContext, getString(R.string.ir_learning_get_brandlist_fail), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IRSettingDataValues.ACTION_IR_SEARCH_RESULTS)) {
                mIRController.log(TAG, "got ACTION_IR_SEARCH_RESULTS");
                dismissProgressDialog();
                mHandler.removeMessages(MESSAGE_SEARCH_TIMEOUT);
                ArrayList<ApIRParingInfo> list = intent.getParcelableArrayListExtra(IRSettingDataValues.KEY_IR_SEARCH_RESULTS);
                if(list != null && list.size() > 0) {
                    Intent sendintent = new Intent(context, IRPairingActivity.class);
                    sendintent.putExtra(IRSettingDataValues.KEY_DATA_IR_SETTING_MODE, IRSettingDataValues.IR_SETTING_MODE_SEARCH);
                    sendintent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, mUuid);
                    sendintent.putParcelableArrayListExtra(IRSettingDataValues.KEY_DATA_IR_PARING_INFO, list);
                    startActivityForResult(sendintent, IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE);
                }
                else
                    Toast.makeText(context,getString(R.string.ir_search_fail),Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void launchIRBrandSearchActivity() {
        Intent intent = new Intent(this,IRBrandSearchActivity.class);
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, mUuid);
        intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, mAssetType);
        intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_UUID, mAssetUuid);
        startActivityForResult(intent, IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ir_setting);

        mContext = this;
        mIRController = getIRController();
        mRoomHubMgr=getRoomHubManager();
        mRoomHubMgr.registerRoomHubChange(this);
        mSearchEdit = (EditText)findViewById(R.id.search);
        mSearchEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(action == MotionEvent.ACTION_DOWN)
                    launchIRBrandSearchActivity();
                return false;
            }
        });
        //mSearchEdit.setOnEditorActionListener(this);

        mListView = (ListView)findViewById(R.id.IRBrandlist);

        mUuid = getIntent().getStringExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID);
        mAssetUuid = getIntent().getStringExtra(IRSettingDataValues.KEY_ELECTRIC_UUID);
        mAssetType = getIntent().getIntExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, DeviceTypeConvertApi.TYPE_ROOMHUB.AC);

        final View ir_learning_view = findViewById(R.id.ir_learning_layout);

        if(mAssetType == DeviceTypeConvertApi.TYPE_ROOMHUB.AC) {
            ir_learning_view.setVisibility(View.VISIBLE);
        }else {
            ir_learning_view.setVisibility(View.GONE);
        }

        ImageView ir_learning_cancel = (ImageView)findViewById(R.id.ir_learning_cancel);
        ir_learning_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ir_learning_view.setVisibility(View.GONE);
            }
        });

        /* IR Learning */
        Button ir_learning = (Button)findViewById(R.id.ir_learning_btn);
        ir_learning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, IRLearningActivity.class);
                intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, mUuid);
                intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, mAssetType);
                intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_UUID, mAssetUuid);
                startActivityForResult(intent, IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE);
            }
        });

        showProgressDialog("", getString(R.string.ir_learning_get_brandlist));
        Thread thread = new Thread() {
            @Override
            public void run() {
                // TODO: currently AC/Fan all included in IrDeviceType.AIR_CONDITIONER
                ArrayList<ApIRBrandData> list = mIRController.getBrandList(mAssetType);
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_GET_LIST_DONE,list));
            }
        };
        thread.start();

        Toast.makeText(this,R.string.ir_pairing_warning_message,Toast.LENGTH_LONG).show();
//        IntentFilter filter = new IntentFilter(IRSettingDataValues.ACTION_IR_SEARCH_RESULTS);
//        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mSearchEdit.setText("");
        mSearchEdit.clearFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRoomHubMgr.unRegisterRoomHubChange(this);
        //unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        setResult(resultCode,data);
//        finish();
        if(resultCode == RESULT_OK && requestCode == IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE) {
            setResult(resultCode,data);
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ApIRBrandData brandData = (ApIRBrandData)mListView.getItemAtPosition(position);

        showProgressDialog("", getString(R.string.loading));
        Thread thread = new Thread() {
            @Override
            public void run() {
                startIRParingActivity(mUuid, brandData);
            }
        };
        thread.start();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(actionId == EditorInfo.IME_ACTION_SEARCH) {
            Intent intent = new Intent(this,IRBrandSearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID,mUuid);
            startActivity(intent);

//            showProgressDialog("",getString(R.string.ir_searching_now));
//            mIRController.searchByKeyword(IrDeviceType.AIR_CONDITIONER, v.getText().toString());
//            mHandler.sendEmptyMessageDelayed(MESSAGE_SEARCH_TIMEOUT, getResources().getInteger(R.integer.config_ir_search_timeout));
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID,mUuid);
        intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE,mAssetType);
        setResult(RESULT_CANCELED,intent);
        finish();
    }

    private boolean startIRParingActivity(String uuid, ApIRBrandData brandData) {
        boolean ret = false;

        ArrayList<ApIRParingInfo> pairingList =
                mIRController.getPairingListByBrandData(mAssetType, uuid, mAssetUuid, brandData);

        if(pairingList != null && pairingList.size() > 0) {
            ret = true;
            mIRController.log(TAG, "matched pairing list found! size=" + pairingList.size());
            Intent intent = new Intent(mContext, IRModelListActivity.class);
            intent.putExtra(IRSettingDataValues.KEY_DATA_IR_SETTING_MODE,IRSettingDataValues.IR_SETTING_MODE_GET_LIST);
            intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, uuid);
            intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, mAssetType);
            intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_UUID, mAssetUuid);
            intent.putExtra(IRSettingDataValues.KEY_DATA_IR_BRAND_NAME, brandData.getBrandName());
            intent.putExtra(IRSettingDataValues.KEY_DATA_IR_PARING_INFO, pairingList);
            startActivityForResult(intent, IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE);
        }
        else {
            mIRController.log(TAG,"Can't find matched model list by brandId!");
        }

        dismissProgressDialog();
        return ret;
    }

    @Override
    public void addDevice(RoomHubData data) {

    }

    @Override
    public void removeDevice(RoomHubData data) {
        if(data != null) {
            if (data.getUuid().equals(mUuid)) {
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        if(data != null) {
            if ((type == RoomHubManager.UPDATE_ROOMHUB_DATA)) {
                if (data.getUuid().equals(mUuid) && !data.IsOnLine()) {
                    mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
                }
            }
        }
    }
    /*
    @Override
    public void UpdateRoomHubDeviceSeq(MicroLocationData locationData) {

    }
    */
    @Override
    public void UpdateDeviceShareUser(CloudDevice device) {

    }

    @Override
    public void UpgradeStatus(String uuid, boolean is_upgrade) {
        if(uuid.equals(mUuid) && (is_upgrade == true)){
            mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
    }

    private class IRBrandListAdapter extends BaseAdapter {
        private ArrayList<ApIRBrandData> mList;

        private class ViewHolder {
            TextView brandText;
        }

        public IRBrandListAdapter(ArrayList<ApIRBrandData> list) {
            mList = list;
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.ir_brand_list_item, parent, false);
                holder = new ViewHolder();
                holder.brandText = (TextView)convertView.findViewById(R.id.brand);
                convertView.setTag(holder);
            }
            else
                holder = (ViewHolder)convertView.getTag();

            final ApIRBrandData item = (ApIRBrandData)getItem(position);
            holder.brandText.setText(item.getBrandName());

            return convertView;
        }
    };
}
