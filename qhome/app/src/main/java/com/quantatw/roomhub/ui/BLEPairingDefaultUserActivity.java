package com.quantatw.roomhub.ui;

import android.app.ActionBar;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.blepair.BLEPairController;
import com.quantatw.roomhub.blepair.BLEPairReqPack;
import com.quantatw.roomhub.blepair.ScanAssetResult;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceController;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.BLEPairDef;
import com.quantatw.sls.device.FriendData;

import java.util.ArrayList;

/**
 * Created by 95011613 on 2016/7/19.
 */
public class BLEPairingDefaultUserActivity extends AbstractRoomHubActivity implements View.OnClickListener{
    private static final String TAG = BLEPairingDefaultUserActivity.class.getSimpleName();
    private static boolean DEBUG=true;

    private AccountManager mAccountMgr;
    private Context mContext;

    private Button mBtnAddPeople;
    private RadioButton mOwnerRadioBtn;
    private ListView mLstExist;
    private ImageView mBtnCancel;
    private ImageView mBtnMyList;
    private View mAddPeople;
    private View mOkBtnLayout;
    private Button mBtnOk;

    private ArrayList<FriendData> mFriendDataList;
    private PeopleAdapter mPeopleAdapter;

    private BLEPairController mBLEController;
    private BLEPairReqPack mBLEPairData;
    private ScanAssetResult mSelectedAsset;
    private String mDefaultUserId;

    private String mAssetUuid;
    private int mAssetType;
    private boolean mIsChecked;

    private final int MESSAGE_CHANGE_DEFAULT_USER = 100;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_CHANGE_DEFAULT_USER:
                    int ret=getHealthDeviceManager().SaveDeviceDefaultUser(mAssetType, mAssetUuid,mDefaultUserId);
                    Log.d(TAG,"SaveDeviceDefaultUser ret="+ret);
                    dismissProgressDialog();
                    finish();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_pair_default_user);

        mContext=this;
        getWindow().setBackgroundDrawableResource(R.drawable.background);

        LayoutInflater inflator = (LayoutInflater) this .getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.room_hub_share_header, null);

        ActionBar actionBar=getActionBar();
        actionBar.setCustomView(v);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        TextView txt_title=(TextView)actionBar.getCustomView().findViewById(R.id.txt_title);
        TextView txt_share_list=(TextView)findViewById(R.id.txt_share_list);
        String asset_name;

        mAssetUuid = getIntent().getStringExtra(AssetDef.ASSET_UUID);
        if(mAssetUuid != null){
            mAssetType = getIntent().getIntExtra(AssetDef.ASSET_TYPE, 0);
            mDefaultUserId = getIntent().getStringExtra(AssetDef.ASSET_DEFAULT_USER);
            HealthDeviceController health_controller=getHealthDeviceManager().getDeviceManager(mAssetType);
            asset_name=getString(health_controller.getTitleStringResourceId());
        }else{
            mBLEController = getBLEController();
            mBLEPairData = mBLEController.getBLEPairData();

            asset_name=mBLEPairData.getAssetName();
        }

        txt_title.setText(String.format(getString(R.string.set_default_user_title),asset_name));
        txt_share_list.setText(String.format(getString(R.string.select_default_user),asset_name));

        mBtnMyList=(ImageView)actionBar.getCustomView().findViewById(R.id.btn_people);
        mBtnMyList.setOnClickListener(this);

        mAddPeople=(View)findViewById(R.id.ll_add_people);
        mAddPeople.setVisibility(View.VISIBLE);

        mOkBtnLayout = (View) findViewById(R.id.okBtnLayout);
        mOkBtnLayout.setVisibility(View.GONE);

        mAccountMgr=getAccountManager();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(mAssetUuid == null) {
            mSelectedAsset = getIntent().getExtras().getParcelable(BLEPairDef.BLE_SELECTED_ASSET);
        }

        initLayout();
        super.onResume();
    }

    private void initLayout(){
        TextView txt_owner_name = (TextView) findViewById(R.id.txt_owner_name);
        txt_owner_name.setText(mAccountMgr.getCurrentAccountName() + "(" + getString(R.string.self) + ")");

        mOwnerRadioBtn = (RadioButton) findViewById(R.id.btn_select);
        mOwnerRadioBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mIsChecked=true;
                    UpdateRadioBtn();
                    UpdateBottomLayout();

                    mDefaultUserId = mAccountMgr.getUserId();
                    buttonView.setChecked(true);
                }
            }
        });

        if (mAccountMgr.getUserId().equalsIgnoreCase(mDefaultUserId)){
            mIsChecked=true;
            mOwnerRadioBtn.setChecked(true);
        }

        mLstExist =  (ListView) findViewById(R.id.lst_exist);
        mFriendDataList=mAccountMgr.GetUserFriendList();
        mPeopleAdapter=new PeopleAdapter(this,mFriendDataList);
        mLstExist.setAdapter(mPeopleAdapter);
        mLstExist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mIsChecked=true;
                UpdateRadioBtn();
                UpdateBottomLayout();

                RadioButton radio = (RadioButton) view.findViewById(R.id.btn_selected);
                radio.setChecked(true);
                mDefaultUserId = mFriendDataList.get(position).getUserId();
            }
        });

        mBtnAddPeople = (Button) findViewById(R.id.btn_add_people);
        mBtnAddPeople.setOnClickListener(this);

        mBtnCancel = (ImageView) findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(this);

        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);

        if(mDefaultUserId != null){
            UpdateBottomLayout();
        }
    }

    private void UpdateRadioBtn(){
        if (mOwnerRadioBtn.isChecked())
            mOwnerRadioBtn.setChecked(false);

        if(mLstExist != null){
            for (int i = 0; i < mLstExist.getCount(); i++) {
                View v = mLstExist.getChildAt(i);
                RadioButton radio = (RadioButton) v.findViewById(R.id.btn_selected);
                if(radio.isChecked())
                    radio.setChecked(false);
            }
        }
    }

    private void UpdateBottomLayout(){
        if(mIsChecked){
            if(mAddPeople.getVisibility() == View.VISIBLE)
                mAddPeople.setVisibility(View.GONE);

            if(mOkBtnLayout.getVisibility() != View.VISIBLE)
                mOkBtnLayout.setVisibility(View.VISIBLE);
        }else{
            if(mAddPeople.getVisibility() != View.VISIBLE)
                mAddPeople.setVisibility(View.VISIBLE);

            if(mOkBtnLayout.getVisibility() == View.VISIBLE)
                mOkBtnLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult resultCode=" + resultCode);
        if(mAssetUuid == null) {
            if (resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(mAssetUuid == null)
            return;
        else
            finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle bundle;
        switch (v.getId()){
            case R.id.btn_add_people:
                intent = new Intent();
                intent.setClass(this, EditPeopleActivity.class);
                bundle = new Bundle();
                bundle.putSerializable(RoomHubManager.KEY_CMD_TYPE, MyListActivity.CMD.ADD);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.btn_people:
                intent = new Intent();
                intent.setClass(this, MyListActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_cancel:
                LinearLayout ll=(LinearLayout) findViewById(R.id.ll_add_people);
                ll.setVisibility(View.GONE);
                break;
            case R.id.btn_ok:
                if(mAssetUuid == null) {
                    mSelectedAsset.getScanAsset().setDefaultUserId(mDefaultUserId);

                    intent = new Intent();
                    intent.setClass(getApplicationContext(), BLEPairingWaitScanActivity.class);
                    intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.ADD_DEVICE);
                    intent.putExtra(BLEPairDef.BLE_SELECTED_ASSET, (Parcelable) mSelectedAsset);
                    startActivityForResult(intent, 0);
                }else{
                    showProgressDialog("",getString(R.string.loading));
                    mHandler.sendEmptyMessage(MESSAGE_CHANGE_DEFAULT_USER);
                }
                break;
        }
    }

    private class PeopleAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<FriendData> mList;
        private LayoutInflater inflater = null;
        private FriendData mData;

        private class ViewHolder {
            TextView tv_name;
            RadioButton btn_action;
        }
        public PeopleAdapter(Context context, ArrayList<FriendData> data) {
            mContext = context;
            mList = data;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if(mList == null) return 0;

            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            if(mList == null) return null;

            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(mList == null) return null;

            final ViewHolder holder;
            mData=mList.get(position);
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.ble_pair_default_user_item, null);
                holder = new ViewHolder();
                holder.tv_name=(TextView) convertView.findViewById(R.id.txt_name);
                holder.btn_action=(RadioButton) convertView.findViewById(R.id.btn_selected);
                convertView.setTag(holder);
            }else
                holder = (ViewHolder)convertView.getTag();

            holder.tv_name.setText(mData.getNickName());
            if(mData.getUserId().equalsIgnoreCase(mDefaultUserId)) {
                mIsChecked=true;
                holder.btn_action.setChecked(true);
                UpdateBottomLayout();
            }else
                holder.btn_action.setChecked(false);

            return convertView;
        }
    }
}

