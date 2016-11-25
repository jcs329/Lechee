package com.quantatw.roomhub.blepair;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by cherry on 2016/05/20.
 */
public class BLEPairReqPack implements Serializable,Parcelable{

    private String roomHubUUID;
    private String assetUuid;
    private int catetory;
    private int assetType;
    private String prefixName;
    private int expireTime;
    private BLEControllerCallback callback;
    private boolean showRename;
    private String assetName;
    private int assetIcon;
    private String bottom_hint;
    private boolean is_use_default;
    private boolean show_default_user;

    public String getRoomHubUuid() {
        return roomHubUUID;
    }

    public void setRoomHubUuid(String roomHubUUID) {
        this.roomHubUUID = roomHubUUID;
    }

    public String getAssetUuid() {
        return assetUuid;
    }

    public void setAssetUuid(String assetUuid) {
        this.assetUuid = assetUuid;
    }

    public int getCatetory() {
        return catetory;
    }

    public void setCatetory(int catetory) {
        this.catetory = catetory;
    }

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
    }

    public String getPrefixName() {
        return prefixName;
    }

    public void setPrefixName(String prefixName) {
        this.prefixName = prefixName;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public BLEControllerCallback getCallback() {
        return callback;
    }

    public void setCallback(BLEControllerCallback callback) {
        this.callback = callback;
    }

    public void setShowRename(boolean showRename) {
        this.showRename = showRename;
    }

    public boolean IsShowRename() {
        return showRename;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public void setAssetIcon(int assetIcon) {
        this.assetIcon = assetIcon;
    }

    public int getAssetIcon() {
        return assetIcon;
    }

    public void setBottomHint(String bottom_hint) {
        this.bottom_hint = bottom_hint;
    }

    public String getBottomHint() {
        return bottom_hint;
    }

    public void setUseDefault(boolean is_use) {
        this.is_use_default = is_use;
    }

    public boolean IsUseDefault() {
        return is_use_default;
    }

    public void setShowDefaultUser(boolean show_default_user) {
        this.show_default_user = show_default_user;
    }

    public boolean IsShowDefaultUser() {
        return show_default_user;
    }
    /**
     * The creator that MUST be defined and named "CREATOR" so that the service
     * generated from AIDL can recreate AbstractStrategys after IPC.
     */
    public static final Creator<BLEPairReqPack> CREATOR = new Creator<BLEPairReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public BLEPairReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (BLEPairReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public BLEPairReqPack[] newArray(int size) {
            return new BLEPairReqPack[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
