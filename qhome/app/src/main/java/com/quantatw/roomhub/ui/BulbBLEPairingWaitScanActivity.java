package com.quantatw.roomhub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.blepair.BLEPairChangeListener;
import com.quantatw.roomhub.blepair.BLEPairController;
import com.quantatw.roomhub.blepair.BLEPairReqPack;
import com.quantatw.roomhub.blepair.ScanAssetResult;
import com.quantatw.roomhub.utils.BLEPairDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.key.ErrorKey;

import java.util.ArrayList;
import java.util.Iterator;

public class BulbBLEPairingWaitScanActivity extends AbstractRoomHubActivity implements View.OnClickListener,BLEPairChangeListener {
    private static final String TAG = BulbBLEPairingWaitScanActivity.class.getSimpleName();
    private static boolean DEBUG=true;

    private TextView mTxtPairTitle;
    private View mWaitLayout;
    private View mResultLayout;
    private ImageView mImgResult;
    private TextView mTxtMsg;
    private Button mBtnNext;

    private final int MESSAGE_SCAN_ASSET        = 100;
    private final int MESSAGE_ADD_DEVICE        = 101;
    private final int MESSAGE_SCAN_RESULT       = 102;
    private final int MESSAGE_ADD_ASSET_RESULT  = 103;
    private final int MESSAGE_RESULT            = 104;

    private BLEPairDef.STATUS mBLEStatus;
    private BLEPairController mBLEController;
    private BLEPairReqPack mBLEPairData;

    private ArrayList<ScanAssetResult> mSelectedAssetList;
    private ArrayList<ScanAssetResult> mSuccessAssetList=new ArrayList<ScanAssetResult>();
    private ArrayList<ScanAssetResult> mFailAssetList=new ArrayList<ScanAssetResult>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"message what="+msg.what);
            switch(msg.what) {
                case MESSAGE_SCAN_ASSET:
                    mBLEController.scanAsset();
                    break;
                case MESSAGE_ADD_DEVICE:
                    AddBLEDevice(mSelectedAssetList);
                    break;
                case MESSAGE_SCAN_RESULT:
                    handleScanResult((ArrayList<ScanAssetResult>) msg.obj);
                    break;
                case MESSAGE_ADD_ASSET_RESULT:
                    handleAddAssetResult((ScanAssetResult) msg.obj,msg.arg1);
                    break;
                case MESSAGE_RESULT:
                    handleResult((int)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_pair_info);

        getWindow().setBackgroundDrawableResource(R.drawable.main_background);

        mBLEController=getBLEController();
        mBLEStatus = (BLEPairDef.STATUS) getIntent().getExtras().getSerializable(BLEPairDef.BLE_STATUS);
        mSelectedAssetList=getIntent().getExtras().getParcelableArrayList(BLEPairDef.BLE_SELECTED_ASSET);

        initLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBLEPairData=getBLEController().getBLEPairData();
        mBLEController.registerBLEPairChange(this);

        UpdateLayoutData(0);

        log("mBLEStatus=" + mBLEStatus);
        if(mBLEStatus == BLEPairDef.STATUS.ADD_DEVICE){
            mHandler.sendEmptyMessage(MESSAGE_ADD_DEVICE);
        } else if(mBLEStatus == BLEPairDef.STATUS.SCAN_ASSET){
            mHandler.sendEmptyMessage(MESSAGE_SCAN_ASSET);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBLEController.unRegisterBLEPairChange(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_next){
            if(mBLEStatus == BLEPairDef.STATUS.FAIL){
                getBLEController().finish();
                finish();
            }else if (mBLEStatus == BLEPairDef.STATUS.SUCCESS){
                Intent intent=new Intent();
                intent.setClass(this, BulbBLEPairingScaListActivity.class);
                intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.RENAME);
                intent.putParcelableArrayListExtra(BLEPairDef.BLE_SUCCESS_ASSET, mSuccessAssetList);
                intent.putParcelableArrayListExtra(BLEPairDef.BLE_FAIL_ASSET, mFailAssetList);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void initLayout(){
        mTxtPairTitle=(TextView)findViewById(R.id.txt_asset_pair);
        mWaitLayout=(View)findViewById(R.id.bulb_wait_layout);
        mResultLayout=(View)findViewById(R.id.bulb_result_layout);
        mImgResult=(ImageView)findViewById(R.id.imgResult);
        mTxtMsg=(TextView)findViewById(R.id.txt_msg);
        mBtnNext=(Button)findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(this);
    }

    private void UpdateLayoutData(int error_code){
        String str;
        if(mBLEStatus == BLEPairDef.STATUS.SUCCESS) {
            mWaitLayout.setVisibility(View.GONE);
            mResultLayout.setVisibility(View.VISIBLE);
            mTxtMsg.setText(R.string.ble_pairing_success_title);
            str = getString(R.string.ble_pairing_success_title);
            mBtnNext.setText(R.string.next_str);
        }else if(mBLEStatus == BLEPairDef.STATUS.FAIL) {
            mWaitLayout.setVisibility(View.GONE);
            mResultLayout.setVisibility(View.VISIBLE);
            mTxtMsg.setText(Utils.getErrorCodeString(this, error_code));
            str = getString(R.string.ble_pairing_fail_title);
            mBtnNext.setText(R.string.ok);
        }else{
            mResultLayout.setVisibility(View.GONE);
            mWaitLayout.setVisibility(View.VISIBLE);
            str=String.format(getString(R.string.ble_pairing_type_title), mBLEPairData.getAssetName());
        }

        mTxtPairTitle.setText(str);
    }

    private void handleScanResult(ArrayList<ScanAssetResult> scan_asset_list){
        if (scan_asset_list != null) {
            log("handleScanResult scan_asset_list size=" + scan_asset_list.size());
            if(scan_asset_list.size() == 0){
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_RESULT, ErrorKey.BLE_PAIR_SCAN_NO_DATA));
            }else{
                Intent intent = new Intent();
                intent.setClass(this, BulbBLEPairingScaListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(BLEPairDef.BLE_SCAN_ASSET_LIST, scan_asset_list);
                intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.SCAN_RESULT);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        }
    }

    private void handleAddAssetResult(ScanAssetResult  scan_asset,int result){
        if(result == ErrorKey.Success){
            mSuccessAssetList.add(scan_asset);
        }else{
            mFailAssetList.add(scan_asset);
        }

        if(mSelectedAssetList.size() == (mSuccessAssetList.size()+mFailAssetList.size())){
            if(mSuccessAssetList.size() == 0){
                mBLEStatus=BLEPairDef.STATUS.FAIL;
                UpdateLayoutData(ErrorKey.BLE_PAIR_ADD_ASSET_FAILURE);
            }else{
                mBLEStatus=BLEPairDef.STATUS.SUCCESS;
                UpdateLayoutData(0);
            }
        }
    }

    private void handleResult(int ret){
        log("handleResult ret="+ret);
        if(ret != ErrorKey.Success){
            mBLEStatus = BLEPairDef.STATUS.FAIL;
            UpdateLayoutData(ret);
        }
    }

    private void AddBLEDevice(ArrayList<ScanAssetResult> scan_asset_list){
        if(scan_asset_list != null){
            for (Iterator<ScanAssetResult> it = scan_asset_list.iterator(); it.hasNext(); ) {
                ScanAssetResult scanAsset = it.next();
                mBLEController.AddBLEDevice(scanAsset);
            }
        }
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG, msg);
    }

    @Override
    public void onScanAssetResult(ArrayList<ScanAssetResult> scan_asset_list,int result) {
        if(result != ErrorKey.Success) {
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_RESULT, result));
        }else{
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SCAN_RESULT, scan_asset_list));
        }
    }

    @Override
    public void onAddResult(ScanAssetResult scan_asset,int result) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_ADD_ASSET_RESULT,result,0, scan_asset));
    }
}
