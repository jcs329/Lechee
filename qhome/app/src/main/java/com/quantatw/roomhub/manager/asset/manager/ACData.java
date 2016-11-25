package com.quantatw.roomhub.manager.asset.manager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import com.quantatw.roomhub.manager.NoticeSetting;
import com.quantatw.roomhub.manager.RoomHubDBHelper;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.device.Schedule;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.base.BaseAssetResPack;
import com.quantatw.sls.pack.homeAppliance.CommonReqPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitAcResPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitRemoteControlResPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitReqPack;
import com.quantatw.sls.pack.homeAppliance.GetAcAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.HomeApplianceAbilityAc;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by 95011613 on 2016/2/1.
 */
public class ACData extends BaseAssetData {
    private final String TAG = ACData.class.getSimpleName();

    private boolean DEBUG = true;
    private int mPowerStatus;
    private int mTemp;
    private int mFunctionMode;
    private int mSwing;
    private int mFan;
    private int mTimerOn,mTimerOff;

    private ArrayList<HomeApplianceAbilityAc> mAbilityLimit;
    private int[] mRemoteControlAbilityLimit;

    private ArrayList<Schedule> mScheduleLst;

    private NoticeSetting mNoticeSetting;
    private RoomHubDBHelper mRoomHubDB;

    public ACData(RoomHubData roomhub_data,String asset_name,int asset_icon) {
        super(roomhub_data, DeviceTypeConvertApi.TYPE_ROOMHUB.AC,asset_name,asset_icon);
    }

    public static final Creator<ACData> CREATOR = new Creator<ACData>() {
        @Override
        public ACData createFromParcel(Parcel in) {
            return (ACData) in.readSerializable();
        }

        @Override
        public ACData[] newArray(int size) {
            return new ACData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    public int getPowerStatus() {
        return mPowerStatus;
    }

    protected void setPowerStatus(int status) {
        this.mPowerStatus = status;
    }

    public int getTemperature() {
        return mTemp;
    }

    protected void setTemperature(int temp) {
        this.mTemp = temp;
    }

    public int getFunctionMode() {
        return mFunctionMode;
    }

    protected void setFunctionMode(int function_mode) {
        this.mFunctionMode = function_mode;
    }

    public int getSwing() {
        return mSwing;
    }

    protected void setSwing(int swing) {
        this.mSwing = swing;
    }

    public int getFan() {
        return mFan;
    }

    protected void setFan(int fan) {
        this.mFan = fan;
    }

    protected void setTimer(int timer_on,int timer_off){
        this.mTimerOn=timer_on;
        this.mTimerOff=timer_off;
    }

    public int getTimerOff(){
        return this.mTimerOff;
    }

    public int getTimerOn(){
        return this.mTimerOn;
    }

    protected void setRoomHubDB(RoomHubDBHelper roomhub_db){
        mRoomHubDB = roomhub_db;
    }

    protected void setAcAssetInfo(GetAcAssetInfoResPack ac_asset_info) {
        setAssetInfoData((BaseAssetResPack) ac_asset_info);

        log("setAcAssetInfo uuid=" + ac_asset_info.getUuid() + " brandName=" + ac_asset_info.getBrand() + " deviceModel=" + ac_asset_info.getDevice() + " onlinestatus=" + ac_asset_info.getOnLineStatus());

        mPowerStatus = ac_asset_info.getPower();
        mTemp = ac_asset_info.getTemp();
        mFunctionMode = ac_asset_info.getMode();
        mSwing = ac_asset_info.getSwing();
        mFan = ac_asset_info.getFan();
        mTimerOn = ac_asset_info.getTimeOn();
        mTimerOff = ac_asset_info.getTimeOff();
    }

    protected int getAcAssetInfo() {
        RoomHubDevice device=mRoomHubData.getRoomHubDevice();
        CommonReqPack reqPack=new CommonReqPack();
        reqPack.setAssetType(mAssetType);
        reqPack.setUuid(mAssetUuid);

        GetAcAssetInfoResPack res_pack= device.getAcAssetInfo(reqPack);
        if((res_pack != null) && (res_pack.getStatus_code() == ErrorKey.Success)) {
            setAcAssetInfo(res_pack);
            mScheduleLst=device.getAllSchedule();
            mIsAssetInfo = true;
            return ErrorKey.Success;
        }

        mIsAssetInfo = false;
        return ErrorKey.AC_ASSET_INFO_INVALID;
    }

    protected int getAssetAbility() {
        int retval = ErrorKey.AC_ABILITY_INVALID;

        GetAbilityLimitReqPack req_pack = new GetAbilityLimitReqPack();
        req_pack.setAssetType(mAssetType);
        req_pack.setUuid(mAssetUuid);

        if(mSubType == ACDef.AC_SUBTYPE_WINDOW_TYPE){
            GetAbilityLimitRemoteControlResPack res_pack=mRoomHubData.getRoomHubDevice().getRemoteControlAbilityLimit(req_pack);
            if ((res_pack != null) && (res_pack.getStatus_code() == ErrorKey.Success)) {
                mRemoteControlAbilityLimit = res_pack.getAbility();
                if ((mRemoteControlAbilityLimit == null) || (mRemoteControlAbilityLimit.length <= 0))
                    retval = ErrorKey.AC_ABILITY_INVALID;
                else
                    retval = ErrorKey.Success;
            }
            if(mAbilityLimit != null) {
                mAbilityLimit.clear();
                mAbilityLimit=null;
            }
        }else {
            GetAbilityLimitAcResPack res_pack = mRoomHubData.getRoomHubDevice().getAcAbilityLimit(req_pack);
            if ((res_pack != null) && (res_pack.getStatus_code() == ErrorKey.Success)) {
                mAbilityLimit = res_pack.getAbility();
                if ((mAbilityLimit == null) || (mAbilityLimit.size() <= 0))
                    retval=ErrorKey.AC_ABILITY_INVALID;
                else
                    retval = ErrorKey.Success;
            }
            mRemoteControlAbilityLimit=null;
        }

        if(retval == ErrorKey.Success)
            mIsAbility = true;
        else
            mIsAbility = false;

        return retval;
    }

    protected int setAbilityLimit(String brandName,String deviceModel){
        log("setAbilityLimit uuid=" + mAssetUuid + " brandName=" + brandName + " deviceModel=" + deviceModel);
        int retval=ErrorKey.Success;

        if(!TextUtils.isEmpty(brandName) && !TextUtils.isEmpty(deviceModel)) {
            boolean is_ability=false;

            if (!mBrandName.equals(brandName) ||
                    (!mModelName.equals(deviceModel))){
                is_ability=true;
            }

            if(is_ability){
                retval = getAssetAbility();
            }
        }

        return retval;
    }

    protected ArrayList<HomeApplianceAbilityAc> getAbilityLimit(){
        return mAbilityLimit;
    }
    protected int[] getRemoteControlAbilityLimit(){
        return mRemoteControlAbilityLimit;
    }

    public NoticeSetting getNoticeSetting(){
        SQLiteDatabase db = mRoomHubDB.getWritableDatabase();
        mNoticeSetting=mRoomHubDB.QueryNoticeSetting(db, mAssetUuid);

        return mNoticeSetting;
    }

    public void setNoticeSetting(NoticeSetting notice_setting){
        SQLiteDatabase db = mRoomHubDB.getWritableDatabase();
        mRoomHubDB.InsertNoticeSetting(db,mAssetUuid,notice_setting);
        this.mNoticeSetting=notice_setting;
    }

    protected boolean removeSchedule(int idx){
        Schedule schedule=getScheduleByIdx(idx);
        if(schedule != null){
            synchronized(mScheduleLst) {
                mScheduleLst.remove(schedule);
            }
        }

        return true;
    }

    protected boolean updateSchedule(Schedule schedule){
        int idx=getIdxByScheduleIdx(schedule.getIndex());
        synchronized(mScheduleLst) {
            if (idx < 0) {
                log("add schedule idx=" + schedule.getIndex() + " mode=" + schedule.getType() + " start_time=" + schedule.getStartTime() + " end_time=" + schedule.getEndTime());
                mScheduleLst.add(schedule);
            } else {
                log("update schedule idx="+schedule.getIndex()+" mode="+schedule.getType()+" start_time="+schedule.getStartTime()+" end_time="+schedule.getEndTime());
                mScheduleLst.set(idx, schedule);
            }
        }
        return true;
    }

    public Schedule getScheduleByIdx(int idx){
        synchronized (mScheduleLst){
            for (Iterator<Schedule> it = mScheduleLst.iterator();it.hasNext();) {
                Schedule schedule = it.next();
                if(schedule.getIndex() == idx)
                    return schedule;
            }
        }

        return null;
    }

    private int getIdxByScheduleIdx(int idx){
        if(mScheduleLst == null) return -1;

        synchronized (mScheduleLst){
            for(int i=0 ;i < mScheduleLst.size();i++){
                if(mScheduleLst.get(i).getIndex() == idx)
                    return i;
            }
        }

        return -1;
    }

    protected int getMaxValuebyFunMode(int fun_mode){
        if(mAbilityLimit == null) return ErrorKey.AC_ABILITY_INVALID;

        synchronized (mAbilityLimit) {
            for (int i = 0; i < mAbilityLimit.size(); i++) {
                HomeApplianceAbilityAc ability = mAbilityLimit.get(i);
                if (ability.getMode() == fun_mode) {
                    log("getMinValuebyFunMode max_value="+ability.getMaxValue());
                    return ability.getMaxValue();
                }
            }
        }
        return ErrorKey.AC_ABILITY_INVALID;
    }

    protected int getMinValuebyFunMode(int fun_mode){
        if(mAbilityLimit == null) return ErrorKey.AC_ABILITY_INVALID;

        synchronized (mAbilityLimit) {
            for (int i = 0; i < mAbilityLimit.size(); i++) {
                HomeApplianceAbilityAc ability = mAbilityLimit.get(i);
                if (ability.getMode() == fun_mode) {
                    log("getMinValuebyFunMode min_value=" + ability.getMinValue());
                    return ability.getMinValue();
                }
            }
        }
        return ErrorKey.AC_ABILITY_INVALID;
    }
    public HomeApplianceAbilityAc getAbilityByFunMode(int fun_mode){
        if(mAbilityLimit == null) return null;
        log("getAbilityByFunMode uuid=" + mAssetUuid + " fun_mode=" + fun_mode);
        synchronized (mAbilityLimit) {
            for (int i = 0; i < mAbilityLimit.size(); i++) {
                HomeApplianceAbilityAc ability = mAbilityLimit.get(i);
                if (ability.getMode() == fun_mode) {
                    log("getAbilityByFunMode ability fan="+ability.getFan()+" swing="+ability.getSwing()+" min_value="+ability.getMinValue()+" max_value="+ability.getMaxValue());
                    return ability;
                }
            }
        }
        return null;
    }


    protected void setAllSchedule(ArrayList<Schedule> schedules){
        this.mScheduleLst=schedules;
    }

    public ArrayList<Schedule> getAllSchedule(){
        return mScheduleLst;
    }

    public boolean IsRemind(){
        boolean retval=true;

        SQLiteDatabase db = mRoomHubDB.getWritableDatabase();
        Cursor c=mRoomHubDB.QueryACToggle(db, mAssetUuid);
        if ((c != null) && (c.getCount() > 0)) {
            retval = false;
        }

        c.close();
        return retval;
    }

    public void AddToggleNotRemind(){
        mRoomHubDB.InserACToggle(mRoomHubDB.getWritableDatabase(), mAssetUuid, mBrandId, mModelId);
    }

    public void DeleteToggle(String uuid,int brand_id,String model_id){
        if(model_id == null)
            return;

        SQLiteDatabase db = mRoomHubDB.getWritableDatabase();
        Cursor c=mRoomHubDB.QueryACToggle(db,uuid);
        if ((c != null) && (c.getCount() > 0)) {
            c.moveToFirst();
            int old_brand_id=c.getInt(2);
            String old_model_id=c.getString(3);

            if((old_brand_id != brand_id) ||
                    !(old_model_id.equalsIgnoreCase(model_id))){
                mRoomHubDB.DeleteACToggle(mRoomHubDB.getWritableDatabase(),mAssetUuid);
            }
            c.close();
        }
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG,msg);
    }
}
