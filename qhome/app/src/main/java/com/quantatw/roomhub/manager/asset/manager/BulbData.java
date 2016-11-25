package com.quantatw.roomhub.manager.asset.manager;

import android.os.Parcel;
import android.util.Log;

import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.base.BaseAssetResPack;
import com.quantatw.sls.pack.homeAppliance.BulbScheduleData;
import com.quantatw.sls.pack.homeAppliance.CommonReqPack;
import com.quantatw.sls.pack.homeAppliance.GetAllScheduleBulbResPack;
import com.quantatw.sls.pack.homeAppliance.GetAssetProfileResPack;
import com.quantatw.sls.pack.homeAppliance.GetBulbAssetInfoResPack;
import java.util.List;

/**
 * Created by 95011613 on 2016/4/26.
 */
public class BulbData extends BaseAssetData {
    private final String TAG=BulbData.class.getSimpleName();

    private boolean DEBUG = true;

    private int mPower;
    private int mLuminance;
    private List<BulbScheduleData> mSchedules;
    private String name;

    public BulbData(RoomHubData roomhub_data,String asset_name,int asset_icon) {
        super(roomhub_data, DeviceTypeConvertApi.TYPE_ROOMHUB.BULB,asset_name,asset_icon);
        mIsAbility=true;
    }

    public String getName() {
        if (name == null) {
            return "";
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BulbScheduleData> getSchedules() {
        return mSchedules;
    }

    public void setSchedules(List<BulbScheduleData> schedules) {
        this.mSchedules = schedules;
    }

    public static final Creator<BulbData> CREATOR = new Creator<BulbData>() {
        @Override
        public BulbData createFromParcel(Parcel in) {
            return (BulbData)in.readSerializable();
        }

        @Override
        public BulbData[] newArray(int size) {
            return new BulbData[size];
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

    public int getPower() {
        return mPower;
    }

    protected void setPower(int power) {
        this.mPower = power;
    }

    public int getLuminance() {
        return mLuminance;
    }

    protected void setLuminance(int luminance) {
        this.mLuminance = luminance;
    }

    protected int getBulbAssetInfo(){
        RoomHubDevice device=mRoomHubData.getRoomHubDevice();
        CommonReqPack req_pack=new CommonReqPack();
        req_pack.setAssetType(mAssetType);
        req_pack.setUuid(mAssetUuid);

        GetBulbAssetInfoResPack res_pack =device.getBlubAssetInfo(req_pack);
        if(res_pack != null && (res_pack.getStatus_code() == ErrorKey.Success)) {
            setBulbAssetInfo(res_pack);
            mIsAssetInfo = true;

            GetAssetProfileResPack assetProfileResPack = device.getAssetProfile(req_pack);
            if(assetProfileResPack != null && (assetProfileResPack.getStatus_code() == ErrorKey.Success)) {
                setName(assetProfileResPack.getName());
            }else {
                log("GetAssetProfileResPack is null");
            }

            if (mSchedules == null){
                GetAllScheduleBulbResPack resPack = device.getAllScheduleBulb(req_pack);
                if (resPack != null && (res_pack.getStatus_code() == ErrorKey.Success)){
                    setSchedules(resPack.getSchedules());
                }else {
                    log("GetAllScheduleBulbResPack resPack is null");
                }
            }

            return ErrorKey.Success;
        }else {
            log("GetBulbAssetInfoResPack is null");
        }
        mIsAssetInfo = false;
        return ErrorKey.TV_ASSET_INFO_INVALID;
    }

    protected void setBulbAssetInfo(GetBulbAssetInfoResPack blub_asset_info) {
        setAssetInfoData((BaseAssetResPack) blub_asset_info);

        mPower=blub_asset_info.getPower();
        mLuminance=blub_asset_info.getLuminance();
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG,msg);
    }
}
