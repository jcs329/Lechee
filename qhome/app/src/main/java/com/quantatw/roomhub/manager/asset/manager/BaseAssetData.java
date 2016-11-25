package com.quantatw.roomhub.manager.asset.manager;

import android.os.Parcel;

/**
 * Created by cherry on 2016/6/21.
 */
public class BaseAssetData extends AssetInfoData{

    protected RoomHubData mRoomHubData;
    protected String mAssetName;
    protected int mAssetIcon;
    protected boolean mIsAssetInfo=false;
    protected boolean mIsAbility=false;

    public BaseAssetData(RoomHubData mRoomHubData,int asset_type,String asset_name,int asset_icon){
        super(asset_type);
        mAssetName=asset_name;
        mAssetIcon=asset_icon;
        mRoomHubUuid = mRoomHubData.getUuid();
        this.mRoomHubData=mRoomHubData;
    }

    public RoomHubData getRoomHubData(){
        return this.mRoomHubData;
    }

    protected void setRoomHubData(RoomHubData roomhub_data){
        this.mRoomHubData=roomhub_data;
    }

    public boolean IsReady(){
        if(mIsAssetInfo && mIsAbility)
            return true;

        return false;
    }

    protected void setAssetName(String asset_name){
        mAssetName=asset_name;
    }

    public String getAssetName(){
        return mAssetName;
    }

    protected void setAssetIcon(int asset_icon){
        mAssetIcon=asset_icon;
    }

    public int getAssetIcon(){
        return mAssetIcon;
    }

    public static final Creator<BaseAssetData> CREATOR = new Creator<BaseAssetData>() {
        @Override
        public BaseAssetData createFromParcel(Parcel in) {
            return (BaseAssetData)in.readSerializable();
        }

        @Override
        public BaseAssetData[] newArray(int size) {
            return new BaseAssetData[size];
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
}
