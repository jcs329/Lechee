package com.quantatw.roomhub.manager.asset.manager;

import android.os.Parcel;
import android.util.Log;

import com.quantatw.roomhub.manager.NoticeSetting;
import com.quantatw.sls.alljoyn.RoomHubAllJoynDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.base.BaseAssetResPack;
import com.quantatw.sls.pack.homeAppliance.CommandResPack;
import com.quantatw.sls.pack.homeAppliance.CommonReqPack;
import com.quantatw.sls.pack.homeAppliance.GetPMAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.SetFailRecoverReqPack;

/**
 * Created by 95011613 on 2016/2/1.
 */
public class PMData extends BaseAssetData{
    private final String TAG=PMData.class.getSimpleName();

    private boolean DEBUG = true;

    private long mUpdateTime;
    private int mValue; //pm2.5 sensor value
    private int mCapacity; //battery capacity: 5: Full 4: Mid 3: Average 2: Low 1: Empty
    private int mAdapter; //1 means the adpater is inserted. otherwise means adapter is not inserted.

    // use NoticeSetting in case there have more attributes in the future
    private NoticeSetting mNoticeSetting;

    public PMData(RoomHubData roomhub_data,String asset_name,int asset_icon) {
        super(roomhub_data, DeviceTypeConvertApi.TYPE_ROOMHUB.PM25,asset_name,asset_icon);
        mNoticeSetting = new NoticeSetting(0,0,0);
        mIsAbility=true;
    }

    public static final Creator<PMData> CREATOR = new Creator<PMData>() {
        @Override
        public PMData createFromParcel(Parcel in) {
            return (PMData)in.readSerializable();
        }

        @Override
        public PMData[] newArray(int size) {
            return new PMData[size];
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

    public int getValue() {
        return mValue;
    }

    protected void setValue(int value) {
        this.mValue = value;
    }

    public int getCapacity() {
        return mCapacity;
    }

    protected void setCapacity(int capacity) {
        this.mCapacity = capacity;
    }

    public int getAdapter() {
        return mAdapter;
    }

    protected void setAdapter(int adapter) {
        this.mAdapter = adapter;
    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    protected void setUpdateTime(long update_time) {
        this.mUpdateTime = update_time;
    }

    public NoticeSetting getNoticeSetting() {
        return mNoticeSetting;
    }

    protected int SetNoticeSetting(NoticeSetting newSettings) {
        RoomHubDevice device=mRoomHubData.getRoomHubDevice();

        SetFailRecoverReqPack setFailRecoverAcReqPack = new SetFailRecoverReqPack();
        setFailRecoverAcReqPack.setAssetType(RoomHubAllJoynDef.assetType.ASSET_TYPE_PM25);
        setFailRecoverAcReqPack.setUuid(mAssetUuid);
        // time
        Log.d(TAG,"SetNoticeSetting noticeTime="+newSettings.getNoticeTime());
        setFailRecoverAcReqPack.setTime(newSettings.getNoticeTime());
        setFailRecoverAcReqPack.setNotifyValue(newSettings.getNoticeDelta());
        // notifyValue
        setFailRecoverAcReqPack.setNotifyValue(newSettings.getNoticeDelta());
        CommandResPack commandResPack = device.setFailRecover(setFailRecoverAcReqPack);

        return commandResPack.getStatus_code();
    }

    protected void setPM25AssetInfo(GetPMAssetInfoResPack asset_info) {
        setAssetInfoData((BaseAssetResPack) asset_info);

        log("setPM25AssetInfo roomhub_uuid=" + mRoomHubUuid + " asset_uuid=" + asset_info.getUuid() + " brandName=" + asset_info.getBrand() + " deviceModel=" + asset_info.getDevice() + " onlinestatus=" + asset_info.getOnLineStatus());
        mUpdateTime=System.currentTimeMillis();
        mValue=asset_info.getValue();
        mCapacity=asset_info.getCapacity();
        mAdapter=asset_info.getAdapter();
        // time
        mNoticeSetting.setNoticeTime(asset_info.getTime());
        // notifyValue
        mNoticeSetting.setNoticeDelta(asset_info.getNotifyValue());

        log("setPMAssetInfo uuid="+mAssetName+" Value=" + mValue + " Capacity=" + mCapacity+" Adapter="+mAdapter+" onlinestatus="+asset_info.getOnLineStatus());
    }

    protected int getPM25AssetInfo() {
        RoomHubDevice device = mRoomHubData.getRoomHubDevice();
        CommonReqPack req_pack=new CommonReqPack();
        req_pack.setAssetType(mAssetType);
        req_pack.setUuid(mAssetUuid);
        log("getPM25AssetInfo roomhub_uuid=" + mRoomHubUuid + " mAssetType=" + mAssetType + " mAssetUuid=" + mAssetUuid);
        GetPMAssetInfoResPack res_pack = device.getPMAssetInfo(req_pack);
        if (res_pack != null && (res_pack.getStatus_code() == ErrorKey.Success)) {
            log("getPM25AssetInfo roomhub_uuid="+mRoomHubUuid+" asset_uuid=" + res_pack.getUuid() + " onlinestatus=" + res_pack.getOnLineStatus());
            setPM25AssetInfo(res_pack);
            mIsAssetInfo = true;
            return ErrorKey.Success;
        }
        mIsAssetInfo = false;
        return ErrorKey.PM_ASSET_INFO_INVAILD;
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG,msg);
    }
}
