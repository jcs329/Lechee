package com.quantatw.roomhub.manager.asset.manager;

import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.base.BaseAssetResPack;
import com.quantatw.sls.pack.homeAppliance.CommonReqPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitRemoteControlResPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitReqPack;
import com.quantatw.sls.pack.homeAppliance.GetFanAssetInfoResPack;

/**
 * Created by 95011613 on 2016/2/1.
 */
public class FANData extends BaseAssetData {
    private final String TAG = FANData.class.getSimpleName();

    private boolean DEBUG = true;

    private int mPowerStatus;
    private int mSwing;
    private int mSpeed;
    private int mION;
    private int mHumidification;
    private int mSavePower;
    private int mMode;

    private int[] mAbilityLimit;

    public FANData(RoomHubData roomhub_data,String asset_name,int asset_icon) {
        super(roomhub_data, DeviceTypeConvertApi.TYPE_ROOMHUB.FAN,asset_name,asset_icon);
    }

    public static final Creator<FANData> CREATOR = new Creator<FANData>() {
        @Override
        public FANData createFromParcel(Parcel in) {
            return (FANData) in.readSerializable();
        }

        @Override
        public FANData[] newArray(int size) {
            return new FANData[size];
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

    public int getSwing() {
        return mSwing;
    }

    protected void setSwing(int swing) {
        this.mSwing = swing;
    }

    public int getSpeed() {
        return mSpeed;
    }

    protected void setSpeed(int speed) {
        this.mSpeed = speed;
    }

    public int getION() {
        return mION;
    }

    protected void setION(int ion) {
        this.mION = ion;
    }

    public int getHumidification() {
        return mHumidification;
    }

    protected void setHumidification(int humidification) {
        this.mHumidification = humidification;
    }

    public int getSavePower() {
        return mSavePower;
    }

    protected void setSavePower(int save_power) {
        this.mSavePower = save_power;
    }

    public int getMode() {
        return mMode;
    }

    protected void setMode(int mode) {
        this.mMode = mode;
    }

    protected void setFanAssetInfo(GetFanAssetInfoResPack fan_asset_info) {
        setAssetInfoData((BaseAssetResPack) fan_asset_info);

        log("setFanAssetInfo uuid=" + fan_asset_info.getUuid() + " brandName=" + fan_asset_info.getBrand() + " deviceModel=" + fan_asset_info.getDevice() + " onlinestatus=" + fan_asset_info.getOnLineStatus());
        //setAbilityLimit(fan_asset_info.getBrand(), fan_asset_info.getDevice());

        mPowerStatus = fan_asset_info.getPower();
        mSwing = fan_asset_info.getSwing();
        mSpeed = fan_asset_info.getSpeed();
        mION = fan_asset_info.getION();
        mHumidification = fan_asset_info.getHumidification();
        mSavePower = fan_asset_info.getSavePower();
        mMode = fan_asset_info.getMode();

        //mFanAssetInfo=fan_asset_info;
    }

    protected int getFanAssetInfo() {
        RoomHubDevice device = mRoomHubData.getRoomHubDevice();
        CommonReqPack req_pack=new CommonReqPack();
        req_pack.setAssetType(mAssetType);
        req_pack.setUuid(mAssetUuid);

        GetFanAssetInfoResPack res_pack = device.getFanAssetInfo(req_pack);
        if (res_pack != null && (res_pack.getStatus_code() == ErrorKey.Success)) {
            log("getFanAssetInfo retval="+res_pack.getStatus_code());
            setFanAssetInfo(res_pack);
            mIsAssetInfo = true;
            return ErrorKey.Success;
        }
        mIsAssetInfo = false;
        return ErrorKey.FAN_ASSET_INFO_INVALID;
    }

    protected int getAssetAbility() {
        int retval = ErrorKey.FAN_ABILITY_INVALID;

        GetAbilityLimitReqPack req_pack = new GetAbilityLimitReqPack();
        req_pack.setAssetType(mAssetType);
        req_pack.setUuid(mAssetUuid);

        GetAbilityLimitRemoteControlResPack res_pack = mRoomHubData.getRoomHubDevice().getRemoteControlAbilityLimit(req_pack);
        if ((res_pack != null) && (res_pack.getStatus_code() == ErrorKey.Success)) {
            mAbilityLimit = res_pack.getAbility();
            if ((mAbilityLimit == null) || (mAbilityLimit.length <= 0))
                retval=ErrorKey.FAN_ABILITY_INVALID;
            else {
                retval = ErrorKey.Success;
            }
        }
        if(retval == ErrorKey.Success)
            mIsAbility = true;
        else
            mIsAbility = false;

        return retval;
    }

    protected int setAbilityLimit(String brandName,String deviceModel){
        log("setAbilityLimit uuid="+mAssetUuid+" brandName="+brandName+" deviceModel="+deviceModel);
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

    protected int[] getAbilityLimit(){
        return mAbilityLimit;
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG,msg);
    }
}
