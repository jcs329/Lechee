package com.quantatw.roomhub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.blepair.BLEPairController;
import com.quantatw.roomhub.blepair.BLEPairReqPack;
import com.quantatw.roomhub.blepair.ScanAssetResult;
import com.quantatw.roomhub.utils.BLEPairDef;

public class BLEPairingMgrActivity extends AbstractRoomHubActivity implements View.OnClickListener{
    private static final String TAG = BLEPairingMgrActivity.class.getSimpleName();
    private static boolean DEBUG=true;

    private TextView mTxtPairName;
    private TextView mTxtDevName;
    private TextView mTxtName;
    private Button mBtnRename;
    private Button mBtnStart;

    private BLEPairController mBLEController;
    private ScanAssetResult mScanAsset;
    private BLEPairReqPack mBLEPairData;

    private String mDevName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_pair_mgr);

        getWindow().setBackgroundDrawableResource(R.drawable.main_background);

        mBLEController=getBLEController();

        mScanAsset=getIntent().getExtras().getParcelable(BLEPairDef.BLE_SELECTED_ASSET);
        mBLEPairData=mBLEController.getBLEPairData();

        initLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();

        UpdateLayoutData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_rename:
                LaunchRename();
                break;
            case R.id.btn_start:
                LaunchNextPage();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            mDevName=data.getExtras().getString(BLEPairDef.BLE_NAMME);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        intent.putExtra(BLEPairDef.BLE_SELECTED_ASSET, (Parcelable) mScanAsset);
        setResult(RESULT_CANCELED);
        finish();
    }

    private void initLayout(){
        mTxtPairName=(TextView)findViewById(R.id.txt_asset_name);
        mTxtDevName=(TextView)findViewById(R.id.txt_dev_name);
        mTxtName=(TextView)findViewById(R.id.txt_name);
        mBtnRename=(Button)findViewById(R.id.btn_rename);
        mBtnRename.setOnClickListener(this);
        mBtnStart=(Button)findViewById(R.id.btn_start);
        mBtnStart.setOnClickListener(this);
    }

    private void UpdateLayoutData(){
        mTxtPairName.setText(String.format(getString(R.string.ble_pairing_type_title), mBLEPairData.getAssetName()));
        mTxtDevName.setText(mScanAsset.getScanAsset().getDeviceName());
        if(mDevName == null)
            mDevName=mScanAsset.getScanAsset().getDeviceName();
        mTxtName.setText(mDevName);
    }

    private void LaunchRename(){
        Intent intent = new Intent();
        intent.setClass(this, BLEPairingRenameActivity.class);
        intent.putExtra(BLEPairDef.BLE_SELECTED_ASSET, (Parcelable) mScanAsset);
        intent.putExtra(BLEPairDef.BLE_NAMME, mDevName);
        startActivityForResult(intent, 0);
    }

    private void LaunchNextPage(){
        Intent intent = new Intent();
        intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.SUCCESS);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG, msg);
    }
}
