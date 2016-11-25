package com.quantatw.roomhub.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.blepair.BLEPairReqPack;
import com.quantatw.roomhub.blepair.ScanAssetResult;
import com.quantatw.roomhub.utils.BLEPairDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.key.ErrorKey;

public class BLEPairingRenameActivity extends AbstractRoomHubActivity implements View.OnClickListener {
    private static final String TAG = BLEPairingRenameActivity.class.getSimpleName();
    private static boolean DEBUG=true;

    private TextView mTxtPairName;
    private EditText mTxtRename;
    private Button mBtnOK;
    private String mBLEName;
    private ScanAssetResult mScanAsset;
    private Context mContext;

    private final int MESSAGE_SHOW_TOAST         = 100;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"message what="+msg.what);
            switch(msg.what) {
                case MESSAGE_SHOW_TOAST:
                    Toast.makeText(mContext, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_pair_rename);
        mContext=this;

        getWindow().setBackgroundDrawableResource(R.drawable.main_background);

        mScanAsset=getIntent().getExtras().getParcelable(BLEPairDef.BLE_SELECTED_ASSET);
        initLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();

        BLEPairReqPack data=getBLEController().getBLEPairData();

        mTxtPairName.setText(String.format(getString(R.string.ble_pairing_type_title),data.getAssetName()));
        mBLEName=getIntent().getExtras().getString(BLEPairDef.BLE_NAMME);
        mTxtRename.setText(mBLEName);
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
            case R.id.btn_ok:
                final String new_name=mTxtRename.getText().toString();
                if(mBLEName.equals(new_name)) {
                    Intent intent = new Intent();
                    intent.putExtra(BLEPairDef.BLE_NAMME, mTxtRename.getText().toString());
                    setResult(RESULT_OK,intent);

                    finish();
                }else {

                    showProgressDialog("", getString(R.string.processing_str));

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            int retval = getBLEController().Rename(mScanAsset.getScanAsset().getUuid(), new_name);

                            Log.d(TAG, "modify name retval=" + retval);
                            dismissProgressDialog();
                            if (retval != ErrorKey.Success)
                                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_TOAST, Utils.getErrorCodeString(getApplicationContext(), retval)));
                            else {
                                Intent intent = new Intent();
                                intent.putExtra(BLEPairDef.BLE_NAMME, mTxtRename.getText().toString());
                                setResult(RESULT_OK, intent);

                                finish();
                            }
                        }
                    };
                    thread.start();
                }
                break;
        }
    }

    private void initLayout(){
        mTxtPairName=(TextView)findViewById(R.id.txt_asset_name);
        mTxtRename=(EditText)findViewById(R.id.edit_txt_rename);
        mBtnOK=(Button)findViewById(R.id.btn_ok);
        mBtnOK.setOnClickListener(this);
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG, msg);
    }
}
