package com.quantatw.roomhub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.blepair.BLEPairChangeListener;
import com.quantatw.roomhub.blepair.BLEPairController;
import com.quantatw.roomhub.blepair.BLEPairReqPack;
import com.quantatw.roomhub.blepair.ScanAssetResult;
import com.quantatw.roomhub.utils.BLEPairDef;
import com.quantatw.sls.key.ErrorKey;

import java.util.ArrayList;

public class BLEPairingWaitScanActivity extends AbstractRoomHubActivity implements View.OnClickListener,BLEPairChangeListener {
    private static final String TAG = BLEPairingWaitScanActivity.class.getSimpleName();
    private static boolean DEBUG=true;

    private TextView mTxtPairTitle;

    private final int MESSAGE_SCAN_ASSET        = 100;
    private final int MESSAGE_ADD_DEVICE        = 101;
    private final int MESSAGE_SCAN_RESULT       = 102;
    private final int MESSAGE_ADD_ASSET_RESULT  = 103;
    private final int MESSAGE_RESULT            = 104;

    private BLEPairDef.STATUS mBLEStatus;
    private BLEPairController mBLEController;
    private BLEPairReqPack mBLEPairData;

    private ScanAssetResult mScanAsset;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"message what="+msg.what);
            switch(msg.what) {
                case MESSAGE_SCAN_ASSET:
                    mBLEController.scanAsset();
                    break;
                case MESSAGE_ADD_DEVICE:
                    AddBLEDevice(mScanAsset);
                    break;
                case MESSAGE_SCAN_RESULT:
                    handleScanResult((ArrayList<ScanAssetResult>) msg.obj);
                    break;
                case MESSAGE_ADD_ASSET_RESULT:
                    handleAddAssetResult((ScanAssetResult) msg.obj);
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
        mScanAsset=getIntent().getExtras().getParcelable(BLEPairDef.BLE_SELECTED_ASSET);
        initLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBLEPairData=getBLEController().getBLEPairData();
        mBLEController.registerBLEPairChange(this);

        UpdateLayoutData();

        log("mBLEStatus=" + mBLEStatus);
        if(mBLEStatus == BLEPairDef.STATUS.ADD_DEVICE){
            mHandler.sendEmptyMessage(MESSAGE_ADD_DEVICE);
        } else {
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        log("onActivityResult resultCode=" + resultCode);
        if(resultCode == RESULT_OK){
            setResult(resultCode,data);
            finish();
        }else if(resultCode == RESULT_CANCELED){
            mScanAsset=data.getParcelableExtra(BLEPairDef.BLE_SELECTED_ASSET);
            //mScanAsset=data.getExtras().getParcelable(BLEPairDef.BLE_SELECTED_ASSET);
            if(mScanAsset != null)
                mBLEController.RemoveBLEDevice(mScanAsset);
            Intent intent = new Intent();
            intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.START);

            setResult(resultCode,intent);
            finish();
        }else if(resultCode == RESULT_FIRST_USER){
            mBLEStatus = (BLEPairDef.STATUS) data.getExtras().getSerializable(BLEPairDef.BLE_STATUS);
            mScanAsset=data.getExtras().getParcelable(BLEPairDef.BLE_SELECTED_ASSET);
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void initLayout(){
        mTxtPairTitle=(TextView)findViewById(R.id.txt_asset_pair);
    }

    private void UpdateLayoutData(){
        mTxtPairTitle.setText(String.format(getString(R.string.ble_pairing_type_title), mBLEPairData.getAssetName()));
    }

    private void handleScanResult(ArrayList<ScanAssetResult> scan_asset_list){
        if (scan_asset_list != null) {
            log("handleScanResult scan_asset_list size=" + scan_asset_list.size());
            if (scan_asset_list.size() > 1) {
                Intent intent = new Intent();
                intent.setClass(this, BLEPairingScaListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(BLEPairDef.BLE_SCAN_ASSET_LIST, scan_asset_list);
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);

            } else if (scan_asset_list.size() == 1) {
                if(mBLEPairData.IsShowDefaultUser()) {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), BLEPairingDefaultUserActivity.class);

                    intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.DEFAULT_USER);
                    intent.putExtra(BLEPairDef.BLE_SELECTED_ASSET, (Parcelable) scan_asset_list.get(0));

                    startActivityForResult(intent, 0);
                }else{
                    AddBLEDevice(scan_asset_list.get(0));
                }
            }else{
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_RESULT, ErrorKey.BLE_PAIR_SCAN_NO_DATA));
            }
        }
    }

    private void handleAddAssetResult(ScanAssetResult  scan_asset){
        Intent intent=new Intent();
        intent.putExtra(BLEPairDef.BLE_SELECTED_ASSET, (Parcelable) scan_asset);
        if(mBLEPairData.IsShowRename()){
            intent.setClass(this, BLEPairingMgrActivity.class);
            startActivityForResult(intent, 0);
        }else{
            intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.SUCCESS);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void handleResult(int ret){
        Intent intent=new Intent();
        log("handleResult ret="+ret);
        if(ret == ErrorKey.Success)
            intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.SUCCESS);
        else {
            intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.FAIL);
            intent.putExtra(BLEPairDef.BLE_ERROR_CODE,ret);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    private int AddBLEDevice(ScanAssetResult scanAsset){
        return mBLEController.AddBLEDevice(scanAsset);
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
        if(result != ErrorKey.Success)
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_RESULT, result));
        else
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_ADD_ASSET_RESULT, scan_asset));
    }
}
