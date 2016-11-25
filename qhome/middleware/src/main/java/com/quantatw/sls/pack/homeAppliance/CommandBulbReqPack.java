package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.alljoyn.RoomHubAllJoynDef;
import com.quantatw.sls.pack.base.BaseReqPack;

public class CommandBulbReqPack extends BaseReqPack {
    private static final long serialVersionUID = -7723030880934628746L;

    private int assetType = RoomHubAllJoynDef.assetType.ASSET_TYPE_BULB;
    private String uuid;
    private int power;
    private int luminance;

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

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getLuminance() {
        return luminance;
    }

    public void setLuminance(int luminance) {
        this.luminance = luminance;
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
    public static final Creator<CommandBulbReqPack> CREATOR = new Creator<CommandBulbReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public CommandBulbReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (CommandBulbReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public CommandBulbReqPack[] newArray(int size) {
            return new CommandBulbReqPack[size];
        }
    };
}
