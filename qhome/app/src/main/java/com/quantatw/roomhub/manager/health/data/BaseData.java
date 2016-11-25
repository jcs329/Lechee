package com.quantatw.roomhub.manager.health.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.device.FriendData;

import java.io.Serializable;

/**
 * Created by erin on 4/28/16.
 */
public class BaseData implements Serializable, Parcelable, Comparable<BaseData> {

    private static final long serialVersionUID = -8848758797517930466L;
    protected String mUuid;
    protected String mRoomHubUuid;
    protected int mType;
    protected int mSubType;
    protected String mDeviceName;
    protected int mConnectionType;
    protected int mBrandId;
    protected String mBrandName;
    protected int mModelId;
    protected String mModelNumber;
    protected int mOnlineStatus; //0: offline 1: online,
    protected String mOwnerId;
    protected String mRoleName;
    protected int mSourceType;
    protected FriendData mFriendData;

    public BaseData(int type) {
        this.mType = type;
    }

    /**
     * The creator that MUST be defined and named "CREATOR" so that the service
     * generated from AIDL can recreate AbstractStrategys after IPC.
     */
    public static final Creator<BaseData> CREATOR = new Parcelable.Creator<BaseData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public BaseData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (BaseData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public BaseData[] newArray(int size) {
            return new BaseData[size];
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

    @Override
    public int compareTo(BaseData another) {
        return mDeviceName.compareToIgnoreCase(another.getDeviceName());
    }

    public String getRoleName() {
        return mRoleName;
    }

    public void setRoleName(String mRoleName) {
        this.mRoleName = mRoleName;
    }

    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String mUuid) {
        this.mUuid = mUuid;
    }

    public String getRoomHubUuid() {
        return mRoomHubUuid;
    }

    public void setRoomHubUuid(String mRoomHubUuid) {
        this.mRoomHubUuid = mRoomHubUuid;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public int getSubType() {
        return mSubType;
    }

    public void setSubType(int mSubType) {
        this.mSubType = mSubType;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public void setDeviceName(String mDeviceName) {
        this.mDeviceName = mDeviceName;
    }

    public int getConnectionType() {
        return mConnectionType;
    }

    public void setConnectionType(int mConnectionType) {
        this.mConnectionType = mConnectionType;
    }

    public int getBrandId() {
        return mBrandId;
    }

    public void setBrandId(int mBrandId) {
        this.mBrandId = mBrandId;
    }

    public String getBrandName() {
        return mBrandName;
    }

    public void setBrandName(String mBrandName) {
        this.mBrandName = mBrandName;
    }

    public int getModelId() {
        return mModelId;
    }

    public void setModelId(int mModelId) {
        this.mModelId = mModelId;
    }

    public String getModelNumber() {
        return mModelNumber;
    }

    public void setModelNumber(String mModelNumber) {
        this.mModelNumber = mModelNumber;
    }

    public int getOnlineStatus() {
        return mOnlineStatus;
    }

    public void setOnlineStatus(int mOnlineStatus) {
        this.mOnlineStatus = mOnlineStatus;
    }

    public String getOwnerId() {
        return mOwnerId;
    }

    public void setOwnerId(String mOwnerId) {
        this.mOwnerId = mOwnerId;
    }

    public int getSourceType() {
        return mSourceType;
    }

    public void setSourceType(int mSourceType) {
        this.mSourceType = mSourceType;
    }

    public FriendData getFriendData() {
        return mFriendData;
    }

    public void setFriendData(FriendData mFriendData) {
        this.mFriendData = mFriendData;
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

    public boolean IsOwner(){
        if(mRoleName.equals(RoomHubDef.ROLE_OWNER) || mRoleName.equals(RoomHubDef.ROLE_ADMIN))
            return true;

        return false;
    }

    public boolean IsFriend(){
        if(mRoleName.equals(RoomHubDef.ROLE_USER))
            return true;

        return false;
    }

    public String toString() {
        return "\n[BaseData] uuid="+mUuid+",roomHubUuid="+mRoomHubUuid+"\n"+
                "type="+mType+",subType="+mSubType+"\n"+
                "deviceName="+mDeviceName+",connectionType="+mConnectionType+"\n"+
                "brandId="+mBrandId+",brandName="+mBrandName+"\n"+
                "modelId="+mModelId+",modelNumber="+mModelNumber+"\n"+
                "onlineStatus="+mOnlineStatus+",ownerId="+mOwnerId+"\n"+
                "roleName="+mRoleName;
    }
}
