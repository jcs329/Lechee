package com.quantatw.roomhub.manager.asset.manager;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.quantatw.myapplication.R;
import com.quantatw.sls.pack.base.BaseAssetResPack;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by cherry on 2016/06/20.
 */
public class AssetInfoData implements Serializable, Parcelable, Comparable<AssetInfoData> {
    private static final long serialVersionUID = -8274666446002990760L;

    protected static final int SOURCE_TYPE_ALLJOYN = 1 << 0;
    protected static final int SOURCE_TYPE_CLOUD = 1 << 1;

    protected int mAssetType;
    protected String mAssetUuid;
    protected String mRoomHubUuid;
    protected int mSubType;
    protected int mConnectionType;
    protected String mBrandName;
    protected int mBrandId;
    protected String mModelName;
    protected String mModelId;
    protected int mOnLineStatus;
    protected int mSourceType;

    public AssetInfoData(int asset_type) {
        this.mAssetType = asset_type;
    }

    /**
     * The creator that MUST be defined and named "CREATOR" so that the service
     * generated from AIDL can recreate AbstractStrategys after IPC.
     */
    public static final Creator<AssetInfoData> CREATOR = new Creator<AssetInfoData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AssetInfoData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AssetInfoData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AssetInfoData[] newArray(int size) {
            return new AssetInfoData[size];
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

    public int getAssetType() {
        return mAssetType;
    }

    public String getAssetUuid() {
        return mAssetUuid;
    }

    protected void setAssetUuid(String asset_uuid) {
        this.mAssetUuid = asset_uuid;
    }

    public String getRoomHubUuid() {
        return mRoomHubUuid;
    }

    protected void setRoomHubUuid(String mRoomHubUuid) {
        this.mRoomHubUuid = mRoomHubUuid;
    }

    public int getSubType() {
        return mSubType;
    }

    protected void setSubType(int sub_type) {
        this.mSubType = sub_type;
    }

    public int getConnectionType() {
        return mConnectionType;
    }

    protected void setConnectionType(int connection_type) {
        this.mConnectionType = connection_type;
    }

    public String getBrandName() {
        return mBrandName;
    }

    protected void setBrandName(String mBrandName) {
        this.mBrandName = mBrandName;
    }

    public int getBrandId() {
        return mBrandId;
    }

    protected void setBrandId(int mBrandId) {
        this.mBrandId = mBrandId;
    }

    public String getModelNumber() {
        return mModelName;
    }

    protected void setModelNumber(String model_name) {
        this.mModelName = model_name;
    }

    public String getModelId() {
        return mModelId;
    }

    protected void setModelId(String model_id) {
        this.mModelId = model_id;
    }

    public int getOnlineStatus() {
        return mOnLineStatus;
    }

    protected void setOnlineStatus(int online_status) {
        this.mOnLineStatus = online_status;
    }

    protected void setAssetInfoData(BaseAssetResPack asset_data){
        mSubType = asset_data.getSubType();
        mConnectionType = asset_data.getConnectionType();
        mBrandName = asset_data.getBrand();
        mBrandId = asset_data.getBrandId();
        mModelName = asset_data.getDevice();
        mModelId = asset_data.getModelId();
        mOnLineStatus = asset_data.getOnLineStatus();
    }

    public boolean IsIRPair(){
        if (!TextUtils.isEmpty(mBrandName)
                && !TextUtils.isEmpty(mModelName))
            return true;

        return false;
    }

    protected void setSourceType(int type){
        mSourceType |=type;
    }

    public int getSourceType(){
        return mSourceType;
    }

    public void RemoveSourceType(int type){
        mSourceType &=~type;
    }

    public boolean IsAlljoyn(){
        if((mSourceType & SOURCE_TYPE_ALLJOYN) != 0){
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(AssetInfoData another) {
        return Integer.compare(mAssetType,another.getAssetType());
    }

    /*
    public int getSourceType() {
        return mSourceType;
    }

    public void setSourceType(int mSourceType) {
        this.mSourceType = mSourceType;
    }

    public boolean IsAlljoyn(){
        if((mSourceType & RoomHubData.SOURCE_TYPE_ALLJOYN) != 0){
            return true;
        }
        return false;
    }

    public boolean IsCloud(){
        if((mSourceType & RoomHubData.SOURCE_TYPE_CLOUD) != 0){
            return true;
        }
        return false;
    }
    */
}
