package com.quantatw.roomhub.manager.asset.manager;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.base.BaseAssetResPack;
import com.quantatw.sls.pack.homeAppliance.CommonReqPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitRemoteControlResPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitReqPack;
import com.quantatw.sls.pack.homeAppliance.GetTVAssetInfoResPack;

import java.io.Serializable;

/**
 * Created by 10110012 on 2016/05/03.
 */
public class TVData extends BaseAssetData{
    private final String TAG=TVData.class.getSimpleName();

    private boolean DEBUG = true;

    private int mPowerStatus;
    private int mKeyId;
    private int[] mAbilityLimit;

    public TVData(RoomHubData roomhub_data,String asset_name,int asset_icon) {
        super(roomhub_data, DeviceTypeConvertApi.TYPE_ROOMHUB.TV,asset_name,asset_icon);
    }

    public static final Creator<TVData> CREATOR = new Creator<TVData>() {
        @Override
        public TVData createFromParcel(Parcel in) {
            return (TVData)in.readSerializable();
        }

        @Override
        public TVData[] newArray(int size) {
            return new TVData[size];
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


    public int getKeyId() {
        return mKeyId;
    }

    protected void setKeyId(int mKeyId) {
        this.mKeyId = mKeyId;
    }

    protected int getTVAssetInfo(){
        RoomHubDevice device=mRoomHubData.getRoomHubDevice();
        CommonReqPack req_pack=new CommonReqPack();
        req_pack.setAssetType(mAssetType);
        req_pack.setUuid(mAssetUuid);

        GetTVAssetInfoResPack res_pack =device.getTVAssetInfo(req_pack);
        if(res_pack != null && (res_pack.getStatus_code() == ErrorKey.Success)) {
            setTVAssetInfo(res_pack);
            mIsAssetInfo = true;
            return ErrorKey.Success;
        }else {
            log("setTVAssetInfo res_pack is null");
        }
        mIsAssetInfo = false;
        return ErrorKey.TV_ASSET_INFO_INVALID;
    }

    protected void setTVAssetInfo(GetTVAssetInfoResPack asset_info) {
        setAssetInfoData((BaseAssetResPack) asset_info);
        log("setTVAssetInfo brandName=" + asset_info.getBrand() + " deviceModel=" + asset_info.getDevice());
       // setAbilityLimit(asset_info.getBrand(), asset_info.getDevice());

        mPowerStatus = asset_info.getPower();
    }

    protected int getAssetAbility() {
        int retval = ErrorKey.TV_ABILITY_INVALID;

        GetAbilityLimitReqPack req_pack = new GetAbilityLimitReqPack();
        req_pack.setAssetType(mAssetType);
        req_pack.setUuid(mAssetUuid);

        GetAbilityLimitRemoteControlResPack res_pack = mRoomHubData.getRoomHubDevice().getRemoteControlAbilityLimit(req_pack);
        if ((res_pack != null) && (res_pack.getStatus_code() == ErrorKey.Success)) {
            mAbilityLimit = res_pack.getAbility();
            if ((mAbilityLimit == null) || (mAbilityLimit.length <= 0))
                retval=ErrorKey.TV_ABILITY_INVALID;
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

    public int[] getAbilityLimit(){
        return mAbilityLimit;
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG,msg);
    }
}
