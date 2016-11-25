package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.alljoyn.RoomHubAllJoynDef;
import com.quantatw.sls.pack.base.BaseReqPack;

/**
 * Created by 10110012 on 2016/6/22.
 */
public class RemoveScheduleBulbReqPack extends BaseReqPack {

    private static final long serialVersionUID = 1435220356951904742L;
    private int assetType = RoomHubAllJoynDef.assetType.ASSET_TYPE_BULB;
    private String uuid;
    private int index;

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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
    public static final Creator<RemoveScheduleBulbReqPack> CREATOR = new Creator<RemoveScheduleBulbReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public RemoveScheduleBulbReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (RemoveScheduleBulbReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public RemoveScheduleBulbReqPack[] newArray(int size) {
            return new RemoveScheduleBulbReqPack[size];
        }
    };
}
