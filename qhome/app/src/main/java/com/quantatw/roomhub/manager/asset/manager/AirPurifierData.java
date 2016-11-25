package com.quantatw.roomhub.manager.asset.manager;

import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.base.BaseAssetResPack;
import com.quantatw.sls.pack.homeAppliance.CommonReqPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitRemoteControlResPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitReqPack;
import com.quantatw.sls.pack.homeAppliance.GetAirPurifierAssetInfoResPack;

/**
 * Created by 95011613 on 2016/2/1.
 */
public class AirPurifierData extends BaseAssetData {
    private final String TAG=AirPurifierData.class.getSimpleName();

    private boolean DEBUG = true;

    //For IR and BT
    private int mKeyId;
    private int mPowerStatus; /* [IR] 0: off 1: on 2: switch [BT] 0: off 1: on */
    private int mAutoOn; /* [IR][BT] 0: off 1:on */
    private int mNotifyValue; /* [IR][BT] 54 unit: μg/m3 */
    private int mSpeed; /* [IR] 0: decrease 1: increase 2: switch  [BT] 1 ~ 9*/

    //For IR
    private int mSwing; //0: off 1: on 2: switch
    private int mMode; //0: normal 1: sleep 2: nature wind 3: special 4: save power 5: switch

    //For BT
    private int mQuality; // 0 ~ 500
    private int mAutoFan; // 0: off 1: on_green 2: on_yellow 3: on_red
    private int mUv; //0: off 1: on
    private int mAnion; //0: off 1: on
    private int mTimer; //Hour: int
    private int mStrainer; // 2 bytes: high byte: 1 ~ 5,low byte: 1 ~ 3; 1: Color Green 2: Yellow 3: Red

    private long mUpdateTime;

    private int[] mAbilityLimit;
    //private GetAirPurifierAssetInfoResPack mAirPurifierAssetInfo;

    public AirPurifierData(RoomHubData roomhub_data,String asset_name,int asset_icon) {
        super(roomhub_data, DeviceTypeConvertApi.TYPE_ROOMHUB.AIR_PURIFIER,asset_name,asset_icon);
    }

    public static final Creator<AirPurifierData> CREATOR = new Creator<AirPurifierData>() {
        @Override
        public AirPurifierData createFromParcel(Parcel in) {
            return (AirPurifierData)in.readSerializable();
        }

        @Override
        public AirPurifierData[] newArray(int size) {
            return new AirPurifierData[size];
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

    public int getKeyId() {
        return mKeyId;
    }

    protected void setKeyId(int mKeyId) {
        this.mKeyId = mKeyId;
    }

    public int getPowerStatus() {
        return mPowerStatus;
    }

    protected void setPowerStatus(int status) {
        this.mPowerStatus = status;
    }

    public int getAutoOn() {
        return mAutoOn;
    }

    protected void setAutoOn(int mAutoOn) {
        this.mAutoOn = mAutoOn;
    }

    public int getNotifyValue() {
        return mNotifyValue;
    }

    protected void setNotifyValue(int mNotifyValue) {
        this.mNotifyValue = mNotifyValue;
    }

    public int getSpeed() {
        return mSpeed;
    }

    protected void setSpeed(int mSpeed) {
        this.mSpeed = mSpeed;
    }

    //For IR
    public int getSwing() {
        return mSwing;
    }

    protected void setSwing(int mSwing) {
        this.mSwing = mSwing;
    }

    public int getMode() {
        return mMode;
    }

    protected void setMode(int mMode) {
        this.mMode = mMode;
    }

    //For BT

    public int getQuality() {
        return mQuality;
    }

    protected void setQuality(int mQuality) {
        this.mQuality = mQuality;
    }

    public int getAutoFan() {
        return mAutoFan;
    }

    protected void setAutoFan(int mAutoFan) {
        this.mAutoFan = mAutoFan;
    }

    public int getUv() {
        return mUv;
    }

    protected void setUv(int mUv) {
        this.mUv = mUv;
    }

    public int getAnion() {
        return mAnion;
    }

    protected void setAnion(int mAnion) {
        this.mAnion = mAnion;
    }

    public int getClock() {
        return mTimer;
    }

    protected void setClock(int mTimer) {
        this.mTimer = mTimer;
    }

    public int getStrainer() {
        return mStrainer;
    }

    protected void setStrainer(int mStrainer) {
        this.mStrainer = mStrainer;
    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    protected int getAirPurifierAssetInfo(){
        CommonReqPack req_pack=new CommonReqPack();
        req_pack.setAssetType(mAssetType);
        req_pack.setUuid(mAssetUuid);

        GetAirPurifierAssetInfoResPack res_pack =mRoomHubData.getRoomHubDevice().getAirPurifierAssetInfo(req_pack);
        if(res_pack != null && (res_pack.getStatus_code() == ErrorKey.Success)) {
            setAirPurifierAssetInfo(res_pack);
            mIsAssetInfo = true;
            return ErrorKey.Success;
        }

        mIsAssetInfo = false;
        return ErrorKey.AP_ASSET_INFO_INVAILD;
    }

    protected void setAirPurifierAssetInfo(GetAirPurifierAssetInfoResPack asset_info){
        setAssetInfoData((BaseAssetResPack) asset_info);

        log("setAirPurifierAssetInfo uuid=" + asset_info.getUuid() + " connection_type=" + asset_info.getConnectionType() + " brand_name=" + asset_info.getBrand() + " device_model=" + asset_info.getDevice());
        mUpdateTime=System.currentTimeMillis();
        //setAbilityLimit(asset_info.getBrand(), asset_info.getDevice());

        mPowerStatus = asset_info.getPower();
        mAutoOn = asset_info.getAutoOn();
        mNotifyValue = asset_info.getNotifyValue();

        if(asset_info.getConnectionType() == AssetDef.CONNECTION_TYPE_BT) {
            mSpeed = asset_info.getSpeed();
            mQuality = asset_info.getQuality();
            mAutoFan = asset_info.getAutoFan();
            mUv = asset_info.getUv();
            mAnion = asset_info.getAnion();
            mSpeed = asset_info.getSpeed();
            mTimer = asset_info.getTimer();
            mStrainer = asset_info.getStrainer();
        }else{
            mSwing = asset_info.getSwing();
            mSpeed = asset_info.getFanSpeed();
            mMode = asset_info.getMode();
        }
    }

    protected int getAssetAbility(){
        int retval = ErrorKey.AP_ABILITY_INVALID;

        GetAbilityLimitReqPack req_pack=new GetAbilityLimitReqPack();
        req_pack.setAssetType(mAssetType);
        req_pack.setUuid(mAssetUuid);

        GetAbilityLimitRemoteControlResPack res_pack=mRoomHubData.getRoomHubDevice().getRemoteControlAbilityLimit(req_pack);
        if((res_pack != null) && (res_pack.getStatus_code() == ErrorKey.Success)) {
            mAbilityLimit=res_pack.getAbility();
            if((mAbilityLimit == null) || (mAbilityLimit.length <= 0))
                retval=ErrorKey.AP_ABILITY_INVALID;
            else
                retval=ErrorKey.Success;
        }

        if(retval == ErrorKey.Success)
            mIsAbility = true;
        else
            mIsAbility = false;

        return retval;
    }

    /*
    // assetType 4:   connectionType 0: IR
	21: Power Toggle 1: Power On 2: Power Off 12: Swing Toggle 39: Swing On 40: Swing Off
    27: FanSpeed Switch 28: FanSpeed Increase 29: FanSpeed Decrease 38: ION Toggle 37: Humidification Toggle
    31: E-Saver Toggle 22: Mode Switch 23: Normal Mode 24: Sleep Mode 25: Natural Mode 26: Special Mode
	"ability": [ 1, 2, 6, 17, ...]
	// assetType 4:   connectionType 1: BT
    1: Power ON 2: Power OFF 6: SPPED (FAN SPEED) 17: AUTO ON 18: AUTO OFF 19: UV ON
    20: UV OFF 21: ANION ON 22: ANION OFF 26: Timer 30: STRAINER
    */
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

    protected int[] getAbilityLimit(){
        return mAbilityLimit;
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG,msg);
    }
}
