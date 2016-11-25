package com.quantatw.roomhub.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.blepair.BLEPairController;
import com.quantatw.roomhub.blepair.BLEPairReqPack;
import com.quantatw.roomhub.blepair.ScanAssetResult;
import com.quantatw.roomhub.utils.BLEPairDef;

import java.util.ArrayList;

public class BLEPairingScaListActivity extends AbstractRoomHubActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = BLEPairingScaListActivity.class.getSimpleName();
    private static boolean DEBUG=true;

    private TextView mTxtPairTitle;
    private ListView mScaListView;
    private ScanListAdapter mScanListAdapter;

    private BLEPairController mBLEController;
    private BLEPairReqPack mBLEPairData;

    private final int MESSAGE_ADD_ASSET  = 100;

    private ArrayList<ScanAssetResult> mScanList;
    private ScanAssetResult mSelectedAsset;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"message what="+msg.what);
            switch(msg.what) {
                case MESSAGE_ADD_ASSET:

                    if(mBLEPairData.IsShowDefaultUser()){
                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), BLEPairingDefaultUserActivity.class);

                        intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.DEFAULT_USER);
                        intent.putExtra(BLEPairDef.BLE_SELECTED_ASSET, (Parcelable) mSelectedAsset);

                        startActivityForResult(intent, 0);
                    }else {
                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), BLEPairingWaitScanActivity.class);

                        intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.ADD_DEVICE);
                        intent.putExtra(BLEPairDef.BLE_SELECTED_ASSET, (Parcelable) mSelectedAsset);

                        startActivityForResult(intent, 0);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_scan_asset_list);

        getWindow().setBackgroundDrawableResource(R.drawable.main_background);

        mBLEController=getBLEController();
        mScanList=getIntent().getExtras().getParcelableArrayList(BLEPairDef.BLE_SCAN_ASSET_LIST);

        initLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBLEPairData=mBLEController.getBLEPairData();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            setResult(resultCode,data);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void initLayout(){
        mTxtPairTitle=(TextView)findViewById(R.id.txt_asset_pair);

        mScaListView = (ListView) findViewById(R.id.ble_scan_list);
        mScanListAdapter = new ScanListAdapter(this,mScanList);
        mScaListView.setAdapter(mScanListAdapter);
        mScaListView.setOnItemClickListener(this);
    }

    private void UpdateLayoutData(){
        mTxtPairTitle.setText(String.format(getString(R.string.ble_pairing_type_title), mBLEPairData.getAssetName()));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mSelectedAsset=mScanList.get(position);
        mHandler.sendEmptyMessage(MESSAGE_ADD_ASSET);
    }

    private class ScanListAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<ScanAssetResult> mScanList;

        private class ViewHolder {
            TextView AssetText;
        }

        public ScanListAdapter(Context context, ArrayList<ScanAssetResult> data) {
            mContext = context;
            mScanList = data;
        }

        @Override
        public int getCount() {
            return mScanList.size();
        }

        @Override
        public Object getItem(int position) {
            return mScanList.get(position);
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
                convertView = inflater.inflate(R.layout.city_choice_item, parent, false);
                holder = new ViewHolder();
                holder.AssetText = (TextView)convertView.findViewById(R.id.txtName);
                convertView.setTag(holder);
            }
            else
                holder = (ViewHolder)convertView.getTag();

            final ScanAssetResult item = (ScanAssetResult)getItem(position);
            holder.AssetText.setText(item.getScanAsset().getDeviceName());

            return convertView;
        }
    }


    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG, msg);
    }
}
