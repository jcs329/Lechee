package com.quantatw.roomhub.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.blepair.BLEPairController;
import com.quantatw.roomhub.blepair.ScanAssetResult;
import com.quantatw.roomhub.manager.asset.manager.BulbManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.BLEPairDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.key.ErrorKey;

import java.util.ArrayList;

public class BulbBLEPairingScaListActivity extends AbstractRoomHubActivity implements View.OnClickListener {
    private static final String TAG = BulbBLEPairingScaListActivity.class.getSimpleName();
    private static boolean DEBUG=true;

    private View mStatusLayout;
    private View mNextBtnLyout;

    private TextView mTxtRoomHubName;
    private ListView mScaListView;
    private ScanListAdapter mScanListAdapter;
    private TextView mTxtLimitCnt;
    private TextView mTxtRemainingCnt;
    private TextView mTxtSelectedCnt;
    private Button mBtnOk;
    private Button mBtnCancel;

    private BLEPairController mBLEController;

    private BLEPairDef.STATUS mBLEStatus;
    private ArrayList<ScanAssetResult> mScanList;

    private ArrayList<ScanAssetResult> mSelectedAssetList=new ArrayList<ScanAssetResult>();
    private ArrayList<ScanAssetResult> mSuccessAssetList=new ArrayList<ScanAssetResult>();
    private ArrayList<ScanAssetResult> mFailAssetList=new ArrayList<ScanAssetResult>();

    private int mRemainingCnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bulb_ble_pair_scan_list);

        getWindow().setBackgroundDrawableResource(R.drawable.main_background);

        mBLEController=getBLEController();
        mBLEStatus = (BLEPairDef.STATUS) getIntent().getExtras().getSerializable(BLEPairDef.BLE_STATUS);

        if(mBLEStatus == BLEPairDef.STATUS.RENAME){
            mSuccessAssetList=getIntent().getExtras().getParcelableArrayList(BLEPairDef.BLE_SUCCESS_ASSET);
            mFailAssetList=getIntent().getExtras().getParcelableArrayList(BLEPairDef.BLE_FAIL_ASSET);
            getActionBar().setTitle(getResources().getString(R.string.rename));
        }else{
            mScanList=getIntent().getExtras().getParcelableArrayList(BLEPairDef.BLE_SCAN_ASSET_LIST);
            getActionBar().setTitle(getResources().getString(R.string.bulb_pairing_list));
        }
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
    public void onBackPressed() {
        return;
    }

    private void initLayout(){
        mStatusLayout = (View) findViewById(R.id.ll_status);
        mNextBtnLyout = (View) findViewById(R.id.next_btn_lyout);

        mTxtRoomHubName = (TextView) findViewById(R.id.txt_roomhub_name);
        RoomHubData roomhub_data=getRoomHubManager().getRoomHubDataByUuid(mBLEController.getBLEPairData().getRoomHubUuid());
        mTxtRoomHubName.setText(roomhub_data.getName());

        mScaListView = (ListView) findViewById(R.id.lst_bulb);
        if(mBLEStatus == BLEPairDef.STATUS.RENAME)
            mScanListAdapter = new ScanListAdapter(this,mSuccessAssetList);
        else
            mScanListAdapter = new ScanListAdapter(this,mScanList);

        mScaListView.setAdapter(mScanListAdapter);

        int max_quantity=getResources().getInteger(R.integer.config_bulb_max_quantity);
        mTxtLimitCnt=(TextView)findViewById(R.id.txt_limit_cnt);
        mTxtLimitCnt.setText(String.format(getResources().getString(R.string.bulb_pairing_list_desc1), max_quantity));

        int asset_count=roomhub_data.getAssetCount(mBLEController.getBLEPairData().getAssetType());
        mRemainingCnt=max_quantity-asset_count;
        mTxtRemainingCnt=(TextView)findViewById(R.id.txt_remaining_cnt);

        mTxtSelectedCnt=(TextView)findViewById(R.id.txt_selected_cnt);

        mBtnOk=(Button) findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);

        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(this);

        Button btn_next=(Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);
    }

    private void UpdateLayoutData() {
        if(mBLEStatus == BLEPairDef.STATUS.RENAME){
            mStatusLayout.setVisibility(View.GONE);
            mNextBtnLyout.setVisibility(View.VISIBLE);
            updateFailItemView(true);
        }else {
            mStatusLayout.setVisibility(View.VISIBLE);
            mNextBtnLyout.setVisibility(View.GONE);
            updateFailItemView(false);
            if (mSelectedAssetList.size() <= 0) {
                mBtnOk.setVisibility(View.GONE);
            } else {
                mBtnOk.setVisibility(View.VISIBLE);
            }
            mTxtRemainingCnt.setText(String.format(getResources().getString(R.string.bulb_pairing_list_desc2), mRemainingCnt));
            mTxtSelectedCnt.setText(String.format(getResources().getString(R.string.bulb_pairing_list_desc3), mSelectedAssetList.size()));
        }
    }

    private void updateFailItemView(boolean visible) {
        View failListView = findViewById(R.id.failItemLayout);
        if(visible && (mFailAssetList.size() > 0)) {
            failListView.setVisibility(View.VISIBLE);
            TextView title = (TextView) failListView.findViewById(R.id.fail_msg);
            title.setText(String.format(getString(R.string.bulb_add_fail_msg),mFailAssetList.size()));
        }
        else {
            failListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_ok:
                Intent intent = new Intent();
                intent.setClass(this, BulbBLEPairingWaitScanActivity.class);
                intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.ADD_DEVICE);
                intent.putParcelableArrayListExtra(BLEPairDef.BLE_SELECTED_ASSET,mSelectedAssetList);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_next:
            case R.id.btn_cancel:
                getBLEController().finish();
                finish();
                break;
        }
    }

    public void SelectAsset(int pos,View cur_view){
        ScanAssetResult scan_asset=mScanList.get(pos);
        int index=mSelectedAssetList.indexOf(scan_asset);

        ImageView btn_check = (ImageView) cur_view.findViewById(R.id.btn_check);
        if(index < 0) {
            if(mSelectedAssetList.size() == getResources().getInteger(R.integer.config_bulb_max_quantity)){
                Toast.makeText(this,R.string.electric_reach_limit,Toast.LENGTH_SHORT).show();
                return;
            }
            btn_check.setImageResource(R.drawable.icon_check_success);
            mSelectedAssetList.add(mScanList.get(pos));
            if(mRemainingCnt > 0)
                mRemainingCnt--;
        }else {
            btn_check.setImageResource(R.drawable.icon_check_null);
            mSelectedAssetList.remove(index);
            mRemainingCnt++;
        }
        UpdateLayoutData();
    }

    private class ScanListAdapter extends BaseAdapter {
        private Context mContext;
        ArrayList<ScanAssetResult> mList;
        private LayoutInflater inflater = null;
        private boolean mLED = false;

        public ScanListAdapter(Context context, ArrayList<ScanAssetResult> data) {
            mContext = context;
            mList = data;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        private void setData(View view, int pos) {
            ImageView btn_led = (ImageView) view.findViewById(R.id.btn_led);
            btn_led.setTag(R.id.tag_first, pos);
            btn_led.setTag(R.id.tag_second, view);
            btn_led.setOnTouchListener(touchListener);

            TextView txt_bulb_name = (TextView) view.findViewById(R.id.txt_bulb_name);
            txt_bulb_name.setText(mList.get(pos).getScanAsset().getDeviceName());
            txt_bulb_name.setTag(R.id.tag_first, pos);
            txt_bulb_name.setTag(R.id.tag_second, view);
            txt_bulb_name.setOnTouchListener(touchListener);

            Button btn_rename = (Button) view.findViewById(R.id.btn_rename);
            if(mBLEStatus == BLEPairDef.STATUS.RENAME){
                btn_rename.setVisibility(View.VISIBLE);
            }else{
                btn_rename.setVisibility(View.GONE);
            }
            btn_rename.setTag(R.id.tag_first, pos);
            btn_rename.setTag(R.id.tag_second, view);
            btn_rename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (int) v.getTag(R.id.tag_first);
                    View viewCur = (View) v.getTag(R.id.tag_second);
                    ShowEditAlertDialog(pos, viewCur);
                }
            });

            ImageView btn_check = (ImageView) view.findViewById(R.id.btn_check);
            if(mBLEStatus == BLEPairDef.STATUS.SCAN_RESULT){
                btn_check.setVisibility(View.VISIBLE);
            }else{
                btn_check.setVisibility(View.GONE);
            }
            btn_check.setTag(R.id.tag_first, pos);
            btn_check.setTag(R.id.tag_second, view);
            btn_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (int) v.getTag(R.id.tag_first);
                    View viewCur = (View) v.getTag(R.id.tag_second);
                    SelectAsset(pos, viewCur);
                    //ShowEditAlertDialog(pos, viewCur);
                }
            });
        }

        private View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int pos = (int) v.getTag(R.id.tag_first);
                View viewCur = (View) v.getTag(R.id.tag_second);
                ImageView imageLed = (ImageView) viewCur.findViewById(R.id.btn_led);
                BulbManager bulbMgr=(BulbManager)getRoomHubManager().getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.BULB);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(mBLEStatus == BLEPairDef.STATUS.RENAME) {
                        imageLed.setImageResource(R.drawable.btn_blub_off);
                        bulbMgr.setPower(mList.get(pos).getScanAsset().getUuid(), AssetDef.POWER_OFF);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_OUTSIDE ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if(mBLEStatus == BLEPairDef.STATUS.RENAME) {
                        imageLed.setImageResource(R.drawable.btn_blub_on);
                        bulbMgr.setPower(mList.get(pos).getScanAsset().getUuid(), AssetDef.POWER_ON);
                    }
                }
                return true;
            }
        };

        private void ShowEditAlertDialog(int pos, View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            final EditText input = new EditText(mContext);
            input.setText(mList.get(pos).getScanAsset().getDeviceName());
            final String uuid = mList.get(pos).getScanAsset().getUuid();
            final TextView txtBulbName = (TextView) view.findViewById(R.id.txt_bulb_name);
            builder
                    .setTitle(R.string.onboarding_roomhub_rename)
                    .setView(input)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String value = input.getText().toString();
                            if (input.getText().toString().trim().length() == 0) {
                                Toast.makeText(mContext, R.string.err_msg, Toast.LENGTH_SHORT).show();
                                input.requestFocus();
                            } else {
                                int ret=mBLEController.Rename(uuid,input.getText().toString());
                                if(ret == ErrorKey.Success) {
                                    txtBulbName.setText(input.getText().toString());
                                } else {
                                    Toast.makeText(mContext, Utils.getErrorCodeString(mContext,ret), Toast.LENGTH_SHORT).show();
                                }

                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                dialog.dismiss();

                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                            dialog.dismiss();
                        }
                    });
            builder.show();
            input.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = inflater.inflate(R.layout.bulb_ble_pair_item, parent, false);

            setData(view, position);

            return view;
        }
    }

    private void log(String msg) {
        if (DEBUG)
            Log.d(TAG, msg);
    }
}
