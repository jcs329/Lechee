package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.alljoyn.RoomHubAllJoynDef;
import com.quantatw.sls.pack.base.BaseReqPack;

public class SetFailRecoverReqPack extends BaseReqPack {
    private static final long serialVersionUID = -1346174651352047695L;

    private int assetType;
    private String uuid;
    private int time;
    private int temp;
    private String ownerId; //user id
    private int repeatCheck;
    private int notifyValue;
    private int autoOn; // 0: off 1:on

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public String getUserId() {
        return ownerId;
    }

    public void setUserId(String user_id) {
        this.ownerId = user_id;
    }

    public int getRepeatCheck() {
        return repeatCheck;
    }

    public void setRepeatCheck(int repeatCheck) {
        this.repeatCheck = repeatCheck;
    }

    public int getNotifyValue() {
        return notifyValue;
    }

    public void setNotifyValue(int notifyValue) {
        this.notifyValue = notifyValue;
    }

    public int getAutoOn() {
        return autoOn;
    }

    public void setAutoOn(int autoOn) {
        this.autoOn = autoOn;
    }

    /**
     * Flags for special marshaling
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Write the concrete strategy to the Parcel.
     */
    public void writeToParcel(Parcel out, int flags) {
        // Serialize "this", so that we can get it back after IPC
        out.writeSerializable(this);
    }

    /**
     * The creator that MUST be defined and named "CREATOR" so that the service
     * generated from AIDL can recreate AbstractStrategys after IPC.
     */
    public static final Creator<SetFailRecoverReqPack> CREATOR = new Creator<SetFailRecoverReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public SetFailRecoverReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (SetFailRecoverReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public SetFailRecoverReqPack[] newArray(int size) {
            return new SetFailRecoverReqPack[size];
        }
    };
}
