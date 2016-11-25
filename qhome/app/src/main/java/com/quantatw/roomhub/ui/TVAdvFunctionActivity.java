package com.quantatw.roomhub.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.manager.asset.manager.TVData;
import com.quantatw.roomhub.manager.asset.manager.TVManager;
import com.quantatw.roomhub.utils.IRAVDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.key.ErrorKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by  on 2016/5/17.
 */
public class TVAdvFunctionActivity extends BaseControllerActivity implements
        RoomHubViewFilpper.OnViewFlipperListener {
    private static final String TAG = TVAdvFunctionActivity.class.getSimpleName();

    private TVManager mTVManager;

    private GridView mGridV;
    private ArrayList<Integer> keyIDs = new ArrayList<>();
    private int[] excludeKeyIDs = {
            IRAVDef.IR_AV_KEYID_POWER,
            IRAVDef.IR_AV_KEYID_CH_DOWN,
            IRAVDef.IR_AV_KEYID_CH_UP,
            IRAVDef.IR_AV_KEYID_VOL_DOWN,
            IRAVDef.IR_AV_KEYID_VOL_UP,
            IRAVDef.IR_AV_KEYID_MUTE,
            IRAVDef.IR_AV_KEYID_DIGIT_0,
            IRAVDef.IR_AV_KEYID_DIGIT_1,
            IRAVDef.IR_AV_KEYID_DIGIT_2,
            IRAVDef.IR_AV_KEYID_DIGIT_3,
            IRAVDef.IR_AV_KEYID_DIGIT_4,
            IRAVDef.IR_AV_KEYID_DIGIT_5,
            IRAVDef.IR_AV_KEYID_DIGIT_6,
            IRAVDef.IR_AV_KEYID_DIGIT_7,
            IRAVDef.IR_AV_KEYID_DIGIT_8,
            IRAVDef.IR_AV_KEYID_DIGIT_9,
            IRAVDef.IR_AV_KEYID_MENU,
            IRAVDef.IR_AV_KEYID_LAST_CH,
            IRAVDef.IR_AV_KEYID_LEFT,
            IRAVDef.IR_AV_KEYID_UP,
            IRAVDef.IR_AV_KEYID_RIGHT,
            IRAVDef.IR_AV_KEYID_OK,
            IRAVDef.IR_AV_KEYID_DOWN,
            IRAVDef.IR_AV_KEYID_DIGIT_MULTI
    };


    private TVData mTVData;
    private RoomHubViewFilpper viewFlipper;

    private View mCurView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_hub_controller_flipper);

        getWindow().setBackgroundDrawableResource(R.drawable.bg_aq_good);

        mType= DeviceTypeConvertApi.TYPE_ROOMHUB.TV;

        mTVManager=(TVManager)mRoomHubMgr.getAssetDeviceManager(mType);
        mTVManager.registerAssetsChange(this);

        mCurUuid=getIntent().getStringExtra(RoomHubManager.KEY_UUID);
        mTVData=mTVManager.getTVDataByUuid(mCurUuid);
        mRoomHubData=mTVData.getRoomHubData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        viewFlipper = (RoomHubViewFilpper) findViewById(R.id.body_flipper);
        viewFlipper.setLongClickable(true);
        viewFlipper.setClickable(true);
        viewFlipper.setOnViewFlipperListener(this);

        View v=createView(mCurUuid);
        if(v != null)
            viewFlipper.addView(v, 0);
        else
            finish();

       //initLayout();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(viewFlipper != null)
            viewFlipper.removeAllViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTVManager.unRegisterAssetsChange(this);
    }

    private View createView(String uuid) {

        updateKeyIDs();


        if (DEBUG)
            Log.d(TAG, "createView uuid=" + uuid );

     //   if(mTVData == null) return null;

        mCurUuid=uuid;

      //  mRoomHubMgr.setLed(mTVData.getRoomHub().getUuid(), RoomHubDef.LED_COLOR_BLUE, RoomHubDef.LED_FLASH, 3000, 0, 1);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        mCurView = (View) layoutInflater.inflate(R.layout.tv_adv_function_activity, null);
      //  mCurView.setLongClickable(true);
        mCurView.setFocusable(true);
        mCurView.setFocusableInTouchMode(true);
        initLayout(mCurView);

        return mCurView;
    }

    private void initLayout(View v){
        mGridV = (GridView) v.findViewById(R.id.grid_func);
        mGridV.setSelector(new ColorDrawable(Color.TRANSPARENT));
        List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
        for (int i = 0; i < keyIDs.size(); i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("text", getFunctionName(keyIDs.get(i)));
            items.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(this,
                items, R.layout.tv_func_grid_item, new String[]{"text"},
                new int[]{ R.id.txt_item});
        mGridV.setAdapter(adapter);
        mGridV.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mTVManager.setKeyId(mCurUuid,keyIDs.get(position)) == ErrorKey.Success){
                    Log.d(TAG, "showProgressDialog");
                    if(!isShowing()) {
                        mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS_DIALOG);
                    }
                }
            }

        });

      //  UpdateSensorData(mRoomHubData);


      //  UpdateTVLayout();
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
                }
            }
        }
    }

    private String getFunctionName(int keyID) {
        String name = "tv_controller_function_" + keyID;
        int resourceID = getResources().getIdentifier(name, "string", getPackageName());
        return resourceID == 0 ? "" : getResources().getString(resourceID);
    }

    private void updateKeyIDs(){
        keyIDs.clear();
        for (int key1:mTVData.getAbilityLimit()
             ) {
            boolean found = false;
            for (int key2: excludeKeyIDs){
                if (key1 == key2) {
                    found = true;
                    break;
                }
            }
            if (!found && !keyIDs.contains(key1)){
                keyIDs.add(key1);
                Log.d("TEST","key1 = "+key1);
            }
        }

    }

    @Override
    public View getNextView() {
        return null;
    }

    @Override
    public View getPreviousView() {
        return null;
    }
}
