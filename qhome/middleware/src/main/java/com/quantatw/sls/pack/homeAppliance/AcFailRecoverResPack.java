package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

public class AcFailRecoverResPack extends BaseResPack {
    private static final long serialVersionUID = 503519009055021736L;

    private String roomHubUUID;
    private String ownerId; //user id
    private int reason; //0: turn on fail; 1: turn off fail; 2: repeat control 3: Temperature too high
    private String roomHubDeviceName;   // GCM N0006

    public String gethubUUID() {
        return roomHubUUID;
    }

    public void sethubUUID(String hubUUID) {
        this.roomHubUUID = hubUUID;
    }


    public String getUserId() {
        return ownerId;
    }

    public void setUserId(String user_id) {
        this.ownerId = user_id;
    }

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    public String getRoomHubDeviceName() {
        return roomHubDeviceName;
    }

    public void setRoomHubDeviceName(String roomHubDeviceName) {
        this.roomHubDeviceName = roomHubDeviceName;
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
    public static final Creator<AcFailRecoverResPack> CREATOR = new Creator<AcFailRecoverResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AcFailRecoverResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AcFailRecoverResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AcFailRecoverResPack[] newArray(int size) {
            return new AcFailRecoverResPack[size];
        }
    };
}
