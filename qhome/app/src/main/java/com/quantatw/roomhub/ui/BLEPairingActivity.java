package com.quantatw.roomhub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.blepair.BLEPairReqPack;
import com.quantatw.roomhub.blepair.ScanAssetResult;
import com.quantatw.roomhub.utils.BLEPairDef;
import com.quantatw.roomhub.utils.Utils;

import java.util.ArrayList;

public class BLEPairingActivity extends AbstractRoomHubActivity implements View.OnClickListener {
    private static final String TAG = BLEPairingActivity.class.getSimpleName();
    private static boolean DEBUG=true;

    private TextView mTxtPairTitle;
    private TextView mTxtAssetName;
    private ImageView mImgAsset;
    private TextView mTxtPairDesc;
    private LinearLayout ll_hint;
    private TextView mTxtPairHint;
    private LinearLayout ll_next;
    private Button mBtnNext;

    private BLEPairDef.STATUS mBLEStatus;
    private BLEPairReqPack mBLEPairData;
    private int mErrorCode;

    private final int MESSAGE_SUCCESS_FINISH  = 100;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"message what="+msg.what);
            switch(msg.what) {
                case MESSAGE_SUCCESS_FINISH:
                    getBLEController().finish();
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_pair);

        getWindow().setBackgroundDrawableResource(R.drawable.main_background);

        mBLEStatus= (BLEPairDef.STATUS) getIntent().getExtras().getSerializable(BLEPairDef.BLE_STATUS);

        initLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBLEPairData= getBLEController().getBLEPairData();

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
        if(v.getId() == R.id.btn_next){
            Intent intent = new Intent();
            intent.setClass(this, BLEPairingWaitScanActivity.class);
            log("SCAN_ASSET");
            intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.SCAN_ASSET);
            startActivityForResult(intent,0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mBLEStatus= (BLEPairDef.STATUS) data.getExtras().getSerializable(BLEPairDef.BLE_STATUS);
        mErrorCode=data.getExtras().getInt(BLEPairDef.BLE_ERROR_CODE);
        log("onActivityResult mErrorCOde="+mErrorCode);
        UpdateLayoutData();
    }

    @Override
    public void onBackPressed() {
        getBLEController().finish();
        finish();
    }

    private void initLayout(){
        mTxtPairTitle=(TextView)findViewById(R.id.txt_asset_pair);
        mTxtAssetName=(TextView)findViewById(R.id.txt_asset_name);
        mImgAsset=(ImageView)findViewById(R.id.img_asset);
        mTxtPairDesc=(TextView)findViewById(R.id.txt_ble_msg);
        ll_hint=(LinearLayout)findViewById(R.id.ll_hint);
        mTxtPairHint=(TextView)findViewById(R.id.txt_ble_hint);
        ll_next=(LinearLayout)findViewById(R.id.ll_next_btn);
        mBtnNext=(Button)findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(this);
    }

    private void UpdateLayoutData(){
        mTxtPairTitle.setText(String.format(getString(R.string.ble_pairing_type_title),mBLEPairData.getAssetName()));
        mImgAsset.setImageResource(mBLEPairData.getAssetIcon());

        String desc_str = "";
        String bottom_str = "";

        switch (mBLEStatus){
            case START:
                desc_str=String.format(getString(R.string.ble_pairing_desc),mBLEPairData.getAssetName());
                bottom_str=mBLEPairData.getBottomHint();
                mTxtAssetName.setVisibility(View.INVISIBLE);
                ll_hint.setVisibility(View.VISIBLE);
                ll_next.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:
                desc_str=getString(R.string.ble_pairing_success_title);
                ArrayList<ScanAssetResult> asset_list=getBLEController().getAddAsset();
                if(asset_list.size() > 0) {
                    mTxtAssetName.setText(asset_list.get(0).getScanAsset().getDeviceName());
                    mTxtAssetName.setVisibility(View.VISIBLE);
                }
                ll_hint.setVisibility(View.INVISIBLE);
                ll_next.setVisibility(View.INVISIBLE);
                mHandler.sendEmptyMessageDelayed(MESSAGE_SUCCESS_FINISH,3000);
                break;
            case FAIL:
                desc_str=String.format(getString(R.string.ble_pairing_fail_desc),Utils.getErrorCodeString(this,mErrorCode));
                bottom_str=mBLEPairData.getBottomHint();
                mTxtAssetName.setVisibility(View.INVISIBLE);
                ll_hint.setVisibility(View.VISIBLE);
                ll_next.setVisibility(View.VISIBLE);
                /*
                if(getBLEController().getAddAsset() != null){
                    getRoomHubManager().RemoveElectric(getBLEController().getAddAsset().getRoomHubUuid(), getBLEController().getAddAsset().getScanAsset().getAssetType());
                }
                */
                break;
        }

        mTxtPairDesc.setText(desc_str);
        mTxtPairHint.setText(bottom_str);
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG, msg);
    }
}
