package com.quantatw.roomhub.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.utils.BLEPairDef;

public class BulbBLEPairingActivity extends AbstractRoomHubActivity implements View.OnClickListener {
    private static final String TAG = BulbBLEPairingActivity.class.getSimpleName();
    private static boolean DEBUG=true;

    private Button mBtnStep;
    private ImageView mImgAsset;
    private TextView mTxtHint;
    private Button mBtnNext;

    private BULB_STATE mStep=BULB_STATE.STEP1;

    private enum BULB_STATE{
        STEP1,
        STEP2
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bulb_ble_pair_guide);

        getWindow().setBackgroundDrawableResource(R.drawable.main_background);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        initLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateLayoutData(BULB_STATE.STEP1);
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
            case R.id.btn_next:
                if(mStep == BULB_STATE.STEP2){
                    Intent intent = new Intent();
                    intent.setClass(this, BulbBLEPairingWaitScanActivity.class);
                    intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.SCAN_ASSET);
                    startActivity(intent);
                    finish();
                }else {
                    UpdateLayoutData(BULB_STATE.STEP2);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(mStep == BULB_STATE.STEP2){
            UpdateLayoutData(BULB_STATE.STEP1);
        }else {
            getBLEController().finish();
            finish();
        }
    }

    private void initLayout(){
        mBtnStep=(Button)findViewById(R.id.btn_step);
        mImgAsset=(ImageView)findViewById(R.id.img_asset);
        mTxtHint=(TextView)findViewById(R.id.txt_hint);
        mBtnNext=(Button)findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(this);
    }

    private void UpdateLayoutData(BULB_STATE step){
        mStep=step;
        switch (step){
            case STEP1:
                mBtnStep.setText(String.format(getString(R.string.bulb_pairing_step), 1));
                mImgAsset.setImageResource(R.drawable.img_install_1);
                mBtnNext.setText(R.string.next_str);
                mTxtHint.setText(R.string.bulb_pairing_step1_hint);
                break;
            case STEP2:
                mBtnStep.setText(String.format(getString(R.string.bulb_pairing_step), 2));
                mImgAsset.setImageResource(R.drawable.img_install_2);
                mBtnNext.setText(R.string.bulb_pairing_step2_flashes_once);
                mTxtHint.setText(R.string.bulb_pairing_step2_hint);
                break;
        }
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG, msg);
    }
}
