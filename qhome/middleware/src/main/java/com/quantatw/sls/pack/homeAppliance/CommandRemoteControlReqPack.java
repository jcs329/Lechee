package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseReqPack;

public class CommandRemoteControlReqPack extends BaseReqPack {
    private static final long serialVersionUID = 1097359535790264023L;

    private int assetType;
    private String uuid;
    private int keyId;

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
    public static final Parcelable.Creator<CommandRemoteControlReqPack> CREATOR = new Parcelable.Creator<CommandRemoteControlReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public CommandRemoteControlReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (CommandRemoteControlReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public CommandRemoteControlReqPack[] newArray(int size) {
            return new CommandRemoteControlReqPack[size];
        }
    };
}
