package com.quantatw.roomhub.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.ir.ApIRParingInfo;
import com.quantatw.roomhub.manager.IRController;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;

import java.util.ArrayList;

/**
 * Created by erin on 1/26/16.
 */
public class IRBrandSearchActivity extends AbstractRoomHubActivity implements TextView.OnEditorActionListener {
    private final String TAG=IRBrandSearchActivity.class.getSimpleName();

    private Context mContext;
    private String mUuid;
    private String mAssetUuid;
    private int mAssetType;
    private EditText mSearchEdit;
    private IRController mIRController;
    private TextView mIrsearchHint2;

    private final int MESSAGE_SEARCH_TIMEOUT = 100;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SEARCH_TIMEOUT:
                    dismissProgressDialog();
                    Toast.makeText(mContext,R.string.ir_search_fail,Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

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
                    sendintent.putExtra(IRSettingDataValues.KEY_ELECTRIC_UUID,mAssetUuid);
                    sendintent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE,mAssetType);
                    sendintent.putParcelableArrayListExtra(IRSettingDataValues.KEY_DATA_IR_PARING_INFO, list);
                    startActivityForResult(sendintent, IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE);
                }
                else
                    Toast.makeText(context,getString(R.string.ir_search_fail),Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ir_brand_search);
        mContext = this;

        mUuid = getIntent().getStringExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID);
        mAssetUuid = getIntent().getStringExtra(IRSettingDataValues.KEY_ELECTRIC_UUID);
        mAssetType = getIntent().getIntExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, DeviceTypeConvertApi.TYPE_ROOMHUB.AC);
        mIrsearchHint2 = (TextView) findViewById(R.id.irsearch_hint2);
        mSearchEdit = (EditText)findViewById(R.id.search);
        mSearchEdit.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mSearchEdit.setOnEditorActionListener(this);

        mIRController = getIRController();
        IntentFilter filter = new IntentFilter(IRSettingDataValues.ACTION_IR_SEARCH_RESULTS);
        registerReceiver(mReceiver, filter);

        if (mAssetType == DeviceTypeConvertApi.TYPE_ROOMHUB.FAN)
            mIrsearchHint2.setVisibility(View.GONE);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            if (v.getText().toString().length() < 1) {
                Toast.makeText(mContext, R.string.ir_search_keyword_too_short, Toast.LENGTH_SHORT).show();
                return true;
            }
            showProgressDialog("",getString(R.string.ir_searching_now));
            mIRController.searchByKeyword(mAssetType, mUuid, mAssetUuid, v.getText().toString());
            mHandler.sendEmptyMessageDelayed(MESSAGE_SEARCH_TIMEOUT, getResources().getInteger(R.integer.config_ir_search_timeout));
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID,mUuid);
        intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE,mAssetType);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setResult(resultCode,data);
        finish();
    }
}
