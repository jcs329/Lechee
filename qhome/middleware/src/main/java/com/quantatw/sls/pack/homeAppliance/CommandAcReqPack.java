package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.alljoyn.RoomHubAllJoynDef;
import com.quantatw.sls.pack.base.BaseReqPack;
import com.quantatw.sls.pack.device.IRCommandReqPack;

public class CommandAcReqPack extends IRCommandReqPack {
    private static final long serialVersionUID = -1976145164268487105L;

    private int assetType = RoomHubAllJoynDef.assetType.ASSET_TYPE_AC;
    private String uuid;

    public int getAssetType() {
        return assetType;
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
    public static final Creator<CommandAcReqPack> CREATOR = new Parcelable.Creator<CommandAcReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public CommandAcReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (CommandAcReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public CommandAcReqPack[] newArray(int size) {
            return new CommandAcReqPack[size];
        }
    };
}
