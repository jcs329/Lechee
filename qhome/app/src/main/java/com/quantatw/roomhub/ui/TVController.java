package com.quantatw.roomhub.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.manager.asset.manager.TVData;
import com.quantatw.roomhub.manager.asset.manager.TVManager;
import com.quantatw.roomhub.utils.IRAVDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.key.ErrorKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by  on 2016/5/17.
 */
public class TVController extends BaseControllerActivity implements View.OnClickListener,RoomHubViewFilpper.OnViewFlipperListener {
    private static final String TAG = TVController.class.getSimpleName();

    private TVManager mTVManager;

    private ImageButton mBtnMute, mBtnMenu, mBtnSource;
    private ImageButton mBtnVolHigher, mBtnVolLower, mBtnChHigher, mBtnChLower;
    private TextView mTxtAdvance;
    private ImageView mNextImageView,mPreviousImageView;

    private TVData mTVData;

    private RoomHubViewFilpper viewFlipper;

    private View mCurView;
    private View mNumberKeyView;
    private View mMenuView;
    private int indexPage = 1;

    private boolean IsPowerToggle;
    private boolean IsMenuOpen = false;
    private int mPowerStatus;

    private GridView mGridV;
    private String[] imgText = {
            "1", "2", "3", "4", "5", "6", "7", "8","9","","0",""
    };
    private ArrayList<String> info = new ArrayList<>();
    private int[] image = {
            R.drawable.btn_tv_number_selector, R.drawable.btn_tv_number_selector, R.drawable.btn_tv_number_selector,
            R.drawable.btn_tv_number_selector, R.drawable.btn_tv_number_selector, R.drawable.btn_tv_number_selector,
            R.drawable.btn_tv_number_selector, R.drawable.btn_tv_number_selector, R.drawable.btn_tv_number_selector,
            R.drawable.btn_tv_ch_back_selector, R.drawable.btn_tv_number_selector, R.drawable.btn_tv_hundred_selector
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tv_controller_flipper);

        getWindow().setBackgroundDrawableResource(R.drawable.bg_aq_good);
        mType= DeviceTypeConvertApi.TYPE_ROOMHUB.TV;

        mTVManager=(TVManager)mRoomHubMgr.getAssetDeviceManager(mType);
        mTVManager.registerAssetsChange(this);

//        mCurUuid=getIntent().getStringExtra(BaseAssetManager.KEY_ASSET_UUID);
        mTVData=mTVManager.getTVDataByUuid(mCurUuid);
//        mRoomHubData=mTVData.getRoomHubData();
        mRoomHubMgr.setLed(mRoomHubUuid, RoomHubDef.LED_COLOR_BLUE, RoomHubDef.LED_FLASH, 3000, 0, 1);

        info.add("");
        info.add("");
        info.add("");
        info.add("");
        info.add("");
        info.add("");
        info.add("");
        info.add("");
        info.add("");
        info.add(getString(R.string.tv_controller_back));
        info.add("   ");
        info.add(getString(R.string.tv_controller_hundred_key));

        viewFlipper = (RoomHubViewFilpper) findViewById(R.id.body_flipper);
        viewFlipper.setLongClickable(true);
        viewFlipper.setClickable(true);
        viewFlipper.setOnViewFlipperListener(this);

        createView(mCurUuid);
        if(mCurView != null) {
            viewFlipper.removeAllViews();
            viewFlipper.addView(mCurView);
        }else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTVManager.unRegisterAssetsChange(this);
    }

    private void createView(String uuid) {
        if (DEBUG)
            Log.d(TAG, "createView uuid=" + uuid );

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        mCurView = layoutInflater.inflate(R.layout.tv_controller, null);
      //  mCurView.setLongClickable(true);
        mCurView.setFocusable(true);
        mCurView.setFocusableInTouchMode(true);

        mBtnMute = (ImageButton) mCurView.findViewById(R.id.btn_mute);
        mBtnMenu = (ImageButton) mCurView.findViewById(R.id.btn_menu);
        mBtnSource = (ImageButton) mCurView.findViewById(R.id.btn_source);
        mBtnMute.setOnClickListener(this);
        mBtnMenu.setOnClickListener(this);
        mBtnSource.setOnClickListener(this);

        mBtnVolHigher = (ImageButton) mCurView.findViewById(R.id.btn_vol_higher);
        mBtnVolLower = (ImageButton) mCurView.findViewById(R.id.btn_vol_lower);
        mBtnChHigher = (ImageButton) mCurView.findViewById(R.id.btn_ch_higher);
        mBtnChLower = (ImageButton) mCurView.findViewById(R.id.btn_ch_lower);
        mBtnVolHigher.setOnClickListener(this);
        mBtnVolLower.setOnClickListener(this);
        mBtnChHigher.setOnClickListener(this);
        mBtnChLower.setOnClickListener(this);

        mMenuView= layoutInflater.inflate(R.layout.tv_controller_menu, null);
        //  mCurView.setLongClickable(true);
        mMenuView.setFocusable(true);
        mMenuView.setFocusableInTouchMode(true);

        mMenuView.findViewById(R.id.btn_menu_up).setOnClickListener(this);
        mMenuView.findViewById(R.id.btn_menu_down).setOnClickListener(this);
        mMenuView.findViewById(R.id.btn_menu_left).setOnClickListener(this);
        mMenuView.findViewById(R.id.btn_menu_right).setOnClickListener(this);
        mMenuView.findViewById(R.id.btn_menu_ok).setOnClickListener(this);

        findViewById(R.id.btn_power).setOnClickListener(this);
        findViewById(R.id.txt_advance).setOnClickListener(this);
        mNextImageView = (ImageView) findViewById(R.id.img_next);
        mPreviousImageView = (ImageView) findViewById(R.id.img_back);
        mNextImageView.setOnClickListener(this);
        mPreviousImageView.setOnClickListener(this);


        mNumberKeyView = layoutInflater.inflate(R.layout.tv_controller_numberkey, null);
        //  mNumberKeyView.setLongClickable(true);
        mNumberKeyView.setFocusable(true);
        mNumberKeyView.setFocusableInTouchMode(true);
        mGridV = (GridView) mNumberKeyView.findViewById(R.id.grid_numberkey);
        mGridV.setSelector(new ColorDrawable(Color.TRANSPARENT));
        List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
        for (int i = 0; i < image.length; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("image", image[i]);
            item.put("text", imgText[i]);
            item.put("text2", info.get(i));
            items.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(this,
                items, R.layout.tv_num_grid_item, new String[]{"image", "text","text2"},
                new int[]{R.id.img_item, R.id.txt_item, R.id.txt_info});
        mGridV.setAdapter(adapter);
        mGridV.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int ret = ErrorKey.Success;
                switch (position){
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        ret = pressNumbKey(position+1);
                        break;
                    case 10:
                        ret = pressNumbKey(0);
                        break;
                    case 9:
                        ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_LAST_CH);
                        break;
                    case 11:
                        ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_DIGIT_MULTI);
                        break;
                }
                if (ret == ErrorKey.Success){
                    Log.d(TAG, "showProgressDialog");
                    if(!isShowing()) {
                        mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS_DIALOG);
                    }
                }
            }

        });

        UpdateTVLayout();
    }

    @Override
    public void onClick(View v) {
        boolean is_show_progress = true;
        int ret = ErrorKey.Success;
         switch (v.getId()){
             case R.id.btn_power:
                 ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_POWER);
//                 if(IsPowerToggle){
//                     //mTVManager.setPowerStatus(mCurUuid,TVDef.POWER_TOGGLE);
//                     mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_POWER);
//                 }else{
//                    if(mPowerStatus == TVDef.STATUS_POWER_OFF){
//                        //mTVManager.setPowerStatus(mCurUuid,TVDef.POWER_ON);
//                        mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_POWER_ON);
//                    }else if(mPowerStatus == TVDef.STATUS_POWER_ON) {
//                        //mTVManager.setPowerStatus(mCurUuid, TVDef.POWER_OFF);
//                        mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_POWER_OFF);
//                    }
//                 }
                 break;
             case R.id.btn_mute:
                 ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_MUTE);
                 break;
             case R.id.btn_menu:
                 ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_MENU);
                 break;
             case R.id.btn_source:
                 ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_MULT_IN);
                 break;
             case R.id.btn_vol_higher:
                 ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_VOL_UP);
                 break;
             case R.id.btn_vol_lower:
                 ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_VOL_DOWN);
                 break;
             case R.id.btn_ch_higher:
                 ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_CH_UP);
                 break;
             case R.id.btn_ch_lower:
                 ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_CH_DOWN);
                 break;
             case R.id.btn_menu_up:
                 ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_UP);
                 break;
             case R.id.btn_menu_left:
                 ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_LEFT);
                 break;
             case R.id.btn_menu_right:
                 ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_RIGHT);
                 break;
             case R.id.btn_menu_down:
                 ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_DOWN);
                 break;
             case R.id.btn_menu_ok:
                 ret = mTVManager.setKeyId(mCurUuid, IRAVDef.IR_AV_KEYID_OK);
                 break;
             case R.id.txt_advance:
                 is_show_progress = false;
                 Intent intent = new Intent();
                 intent.setClass(this, TVAdvFunctionActivity.class);
                 intent.putExtra(RoomHubManager.KEY_UUID,mCurUuid);
                 startActivity(intent);
                 break;
             case R.id.img_next:
                 is_show_progress = false;
                 goNext();
                 break;
             case R.id.img_back:
                 is_show_progress = false;
                 goBack();
                 break;
        }
        if(is_show_progress && ret == ErrorKey.Success) {
            Log.d(TAG, "showProgressDialog");
            if(!isShowing()) {
                mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS_DIALOG);
            }
        }
    }


    private void UpdateTVLayout(){
        IsPowerToggle=mTVManager.IsAbility(mCurUuid, IRAVDef.IR_AV_KEYID_POWER);
        if(!IsPowerToggle) {
            mPowerStatus = mTVData.getPowerStatus();
        }
    }

    protected void Controller_UpdateAssetData(Object asset_data){
        if(asset_data != null) {
            TVData data=(TVData)asset_data;
            String uuid=data.getAssetUuid();
            if(uuid.equals(mCurUuid)) {
                if (!data.IsIRPair())
                    finish();
                else {
                    mTVData = data;
                    UpdateTVLayout();
                }
            }
        }
    }


    //Number
    private int pressNumbKey(int numb) {
        //0~9 = keyid 13~22
        return mTVManager.setKeyId(mCurUuid, numb + IRAVDef.IR_AV_KEYID_DIGIT_0);
    }

    public void showMenuPage(){
        if(mMenuView != null) {
            indexPage = 2;
            viewFlipper.removeAllViews();
            viewFlipper.addView(mMenuView);
            mNextImageView.setVisibility(View.INVISIBLE);
            mPreviousImageView.setVisibility(View.VISIBLE);
        }
    }
    public void showNumberPage(){
        if(mNumberKeyView != null) {
            indexPage = 0;
            viewFlipper.removeAllViews();
            viewFlipper.addView(mNumberKeyView);
            mNextImageView.setVisibility(View.VISIBLE);
            mPreviousImageView.setVisibility(View.INVISIBLE);
        }
    }
    public void showPrimaryPage(){
        if(mCurView != null) {
            indexPage = 1;
            viewFlipper.removeAllViews();
            viewFlipper.addView(mCurView);
            mNextImageView.setVisibility(View.VISIBLE);
            mPreviousImageView.setVisibility(View.VISIBLE);
        }
    }
    private void goNext(){
        if (indexPage == 0){
            showPrimaryPage();
        }else if (indexPage == 1){
            showMenuPage();
        }
    }

    private void goBack(){
        if (indexPage == 2){
            showPrimaryPage();
        }else if (indexPage == 1){
            showNumberPage();
        }
    }

    @Override
    public View getNextView() {
        goNext();
        return null;
    }

    @Override
    public View getPreviousView() {
        goBack();
        return null;
    }
}
