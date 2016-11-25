package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.ACData;
import com.quantatw.roomhub.manager.asset.manager.ACManager;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Cherry.Yang on 2016/05/10.
 */
public class WindowTypeACController extends BaseControllerActivity implements View.OnClickListener {
    private static final String TAG = WindowTypeACController.class.getSimpleName();
    private ACManager mACManager;
    private ACData mACData;

    private class FunModeInfo {
        private int keyId;
        //private int btn_resId;
        private ImageView btn_view;
        private int str_resId;
        private boolean is_support;
        private int bg_img_resId;
        private int icon_resId;
        private int fun_mode;

        private FunModeInfo(int keyId,ImageView btn_view,int str_resId,int icon_resId,int bg_img_resId,int fun_mode) {
            this.keyId = keyId;
            this.btn_view = btn_view;
            this.str_resId = str_resId;
            this.bg_img_resId=bg_img_resId;
            this.icon_resId=icon_resId;
            this.fun_mode=fun_mode;
        }
    }

    private HashMap<Integer,FunModeInfo> mFunModeList=new HashMap<Integer,FunModeInfo>();

    private boolean mIsPowerToggle;
    private boolean mIsTempIncrease;
    private boolean mIsTempDecrease;

    private static  TextView mTxtFunMode;
    private ImageView mImgFunMode;
    private ImageView mBtnLower;
    private ImageView mBtnHigher;
    private ImageView mBtnPower;
    private Button mBtnAdvance;

    private int mCurFunMode= ACDef.FUN_MODE_COOL;
    private int mCurPowerStatus=ACDef.POWER_OFF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.window_type_ac_controller);

        mType= DeviceTypeConvertApi.TYPE_ROOMHUB.AC;

        mACManager=(ACManager)mRoomHubMgr.getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.AC);
        //mCurUuid=getIntent().getStringExtra(BaseAssetManager.KEY_ASSET_UUID);
        mACData=mACManager.getCurrentACDataByUuid(mCurUuid);
      //  mRoomHubData=mACData.getRoomHubData();

        mFunModeList.put(ACDef.FUN_MODE_AUTO, new FunModeInfo(ACDef.WINDOW_TYPE_KEY_ID_MODE_AUTO, (ImageView) findViewById(R.id.btn_auto), R.string.auto, R.drawable.img_auto, R.drawable.background_dehum, ACDef.FUN_MODE_AUTO));
        mFunModeList.put(ACDef.FUN_MODE_COOL,new FunModeInfo(ACDef.WINDOW_TYPE_KEY_ID_MODE_COOL, (ImageView) findViewById(R.id.btn_cooler), R.string.cooler, R.drawable.img_cooler, R.drawable.background_cooler, ACDef.FUN_MODE_COOL));
        mFunModeList.put(ACDef.FUN_MODE_DRY, new FunModeInfo(ACDef.WINDOW_TYPE_KEY_ID_MODE_DRY, (ImageView) findViewById(R.id.btn_dry), R.string.dehumidifier, R.drawable.img_dry, R.drawable.background_heater, ACDef.FUN_MODE_DRY));
        mFunModeList.put(ACDef.FUN_MODE_FAN, new FunModeInfo(ACDef.WINDOW_TYPE_KEY_ID_MODE_FAN, (ImageView) findViewById(R.id.btn_fan), R.string.fan, R.drawable.icon_fan_big_02_off, R.drawable.background_fan, ACDef.FUN_MODE_FAN));
    }

    @Override
    protected void onResume() {
        super.onResume();

        mACManager.registerAssetsChange(this);

        if(mACData == null)
            finish();

        mCurFunMode=mACData.getFunctionMode();
        mRoomHubMgr.setLed(mRoomHubUuid, RoomHubDef.LED_COLOR_BLUE, RoomHubDef.LED_FLASH, 3000, 0, 1);

        initLayout();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mACManager.unRegisterAssetsChange(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initLayout() {
        mTxtFunMode=(TextView)findViewById(R.id.txt_fun_mode);
        mImgFunMode=(ImageView)findViewById(R.id.img_fun_mode);

        mBtnLower=(ImageView)findViewById(R.id.btn_lower);
        mBtnLower.setOnClickListener(this);
        mBtnHigher=(ImageView)findViewById(R.id.btn_higher);
        mBtnHigher.setOnClickListener(this);

        mBtnPower=(ImageView)findViewById(R.id.btn_power);
        mBtnPower.setOnClickListener(this);

        mBtnAdvance=(Button)findViewById(R.id.btn_advance);
        mBtnAdvance.setOnClickListener(this);

        UpdateLayout();
        SwitchFunModeBtn();
    }

    private void UpdateLayout(){

        for (Iterator<FunModeInfo> it = mFunModeList.values().iterator(); it.hasNext(); ) {
            FunModeInfo fun_mode_info = it.next();
            fun_mode_info.btn_view.setOnClickListener(this);
            fun_mode_info.is_support=mACManager.IsAbilityByKeyId(mCurUuid, fun_mode_info.keyId);
        }

        mIsTempIncrease=mACManager.IsAbilityByKeyId(mCurUuid, ACDef.WINDOW_TYPE_KEY_ID_TEMP_INCREASE);
        if(!mIsTempIncrease)
            mBtnHigher.setVisibility(View.INVISIBLE);
        else
            mBtnHigher.setVisibility(View.VISIBLE);

        mIsTempDecrease=mACManager.IsAbilityByKeyId(mCurUuid,ACDef.WINDOW_TYPE_KEY_ID_TEMP_DECREASE);
        if(!mIsTempDecrease)
            mBtnLower.setVisibility(View.INVISIBLE);
        else
            mBtnLower.setVisibility(View.VISIBLE);

        mIsPowerToggle=mACManager.IsAbilityByKeyId(mCurUuid, ACDef.WINDOW_TYPE_KEY_ID_POWER_TOGGLE);
        if(!mIsPowerToggle)
            mCurPowerStatus=mACData.getPowerStatus();
    }

    @Override
    public void onClick(View v) {
         boolean is_show_progress=true;
         switch (v.getId()){
             case R.id.btn_auto:
             case R.id.btn_cooler:
             case R.id.btn_dry:
             case R.id.btn_fan:
                 FunModeInfo fun_mode_info=getFunModeInfoByResId(v.getId());
                 if(fun_mode_info != null) {
                     if(mCurFunMode == fun_mode_info.fun_mode)
                         return;

                     if (fun_mode_info.is_support) {
                         mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.KEY_ID,fun_mode_info.keyId);
                         mCurFunMode = fun_mode_info.fun_mode;

                     } else {
                         Toast.makeText(this, getResources().getString(R.string.controller_warning), Toast.LENGTH_SHORT).show();
                         return;
                     }
                 }
                 break;
            case R.id.btn_power:
                if(mIsPowerToggle){
                    is_show_progress=false;
                    ProcessToggle();
                }else{
                    if(mCurPowerStatus == ACDef.POWER_ON){
                        mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.KEY_ID, ACDef.WINDOW_TYPE_KEY_ID_POWER_OFF);
                    }else if(mCurPowerStatus == ACDef.POWER_OFF)
                        mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.KEY_ID, ACDef.WINDOW_TYPE_KEY_ID_POWER_ON);
                }
                break;
            case R.id.btn_lower:
                mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.KEY_ID, ACDef.WINDOW_TYPE_KEY_ID_TEMP_DECREASE);
                break;
            case R.id.btn_higher:
                mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.KEY_ID, ACDef.WINDOW_TYPE_KEY_ID_TEMP_INCREASE);
                break;
             case R.id.btn_advance:
                 is_show_progress=false;

                 Intent intent = new Intent();
                 intent.setClass(this, AdvanceSettingActivity.class);
                 Bundle bundle = new Bundle();
                 bundle.putString(RoomHubManager.KEY_UUID, mCurUuid);
                 intent.putExtras(bundle);
                 startActivity(intent);
                 break;
        }

        if(is_show_progress) {
            if(isShowing() == false)
                mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS_DIALOG);
        }
    }

    private FunModeInfo getFunModeInfoByResId(int res_id){

        for(int i=0;i<mFunModeList.size();i++){
            FunModeInfo fun_mode_info=mFunModeList.get(i);
            if(fun_mode_info.btn_view.getId() == res_id)
                return fun_mode_info;
        }
        return null;
    }

    private void SwitchFunModeBtn(){
        FunModeInfo fun_mode_info;
        if(IsFunModeAbility()){
            fun_mode_info=mFunModeList.get(mCurFunMode);
        }else{
            if(mACManager.IsAbilityByKeyId(mCurUuid,ACDef.WINDOW_TYPE_KEY_ID_TEMP_DECREASE) ||
                    mACManager.IsAbilityByKeyId(mCurUuid,ACDef.WINDOW_TYPE_KEY_ID_TEMP_INCREASE)){
                mCurFunMode=ACDef.FUN_MODE_COOL;
                fun_mode_info=mFunModeList.get(ACDef.FUN_MODE_COOL);
            }else{
                mCurFunMode=ACDef.FUN_MODE_AUTO;
                fun_mode_info=mFunModeList.get(ACDef.FUN_MODE_AUTO);
            }
        }

        mTxtFunMode.setText(getResources().getString(fun_mode_info.str_resId));

        for(int i=0;i<mFunModeList.size();i++){
            ImageView btn_view=mFunModeList.get(i).btn_view;
            if(i==mCurFunMode) {
                btn_view.setSelected(true);
            }else {
                btn_view.setSelected(false);
            }
        }

        LinearLayout ll_temp= (LinearLayout) findViewById(R.id.ll_temp);
        if(mCurFunMode == ACDef.FUN_MODE_COOL)
            ll_temp.setVisibility(View.VISIBLE);
        else
            ll_temp.setVisibility(View.INVISIBLE);

        mImgFunMode.setBackgroundResource(fun_mode_info.icon_resId);
        getWindow().setBackgroundDrawableResource(fun_mode_info.bg_img_resId);
    }

    protected void Controller_UpdateAssetData(Object asset_data){
        if(asset_data != null) {
            ACData ac_data=(ACData)asset_data;
            String uuid=ac_data.getAssetUuid();
            if(uuid.equals(mCurUuid)){
                if(!ac_data.IsIRPair())
                    finish();
                else{
                    mACData = ac_data;
                    mCurFunMode = mACData.getFunctionMode();
                    mCurPowerStatus = mACData.getPowerStatus();

                    log(TAG,"Controller_UpdateACData window_type ac mCurFunMode=" + mCurFunMode + " mCurPowerStatus=" + mCurPowerStatus);

                    UpdateLayout();
                    SwitchFunModeBtn();

                    mHandler.sendEmptyMessage(MESSAGE_DISMISS_PROGRESS_DIALOG);
                }
            }
        }
    }

    private void ProcessToggle() {
        if(mACData.IsRemind()){
            ShowToggleDialog();
        }else
            mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.KEY_ID, ACDef.WINDOW_TYPE_KEY_ID_POWER_TOGGLE);
    }

    private void ShowToggleDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.ac_toggle_dialog);
        final CheckBox cb_remind=(CheckBox)dialog.findViewById(R.id.cb_remind);

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_ok);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cb_remind.isChecked()){
                    mACData.AddToggleNotRemind();
                }
                mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.KEY_ID,ACDef.WINDOW_TYPE_KEY_ID_POWER_TOGGLE);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean IsFunModeAbility(){
        if(mACManager.IsAbilityByKeyId(mCurUuid,ACDef.WINDOW_TYPE_KEY_ID_MODE_AUTO) ||
           mACManager.IsAbilityByKeyId(mCurUuid,ACDef.WINDOW_TYPE_KEY_ID_MODE_COOL) ||
           mACManager.IsAbilityByKeyId(mCurUuid,ACDef.WINDOW_TYPE_KEY_ID_MODE_DRY) ||
           mACManager.IsAbilityByKeyId(mCurUuid,ACDef.WINDOW_TYPE_KEY_ID_MODE_FAN)){
            return true;
        }
        return false;
    }
}
