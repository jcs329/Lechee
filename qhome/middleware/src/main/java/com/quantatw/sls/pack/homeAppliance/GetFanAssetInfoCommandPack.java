package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

public class GetFanAssetInfoCommandPack extends BaseResPack {
    private static final long serialVersionUID = -1021804732401183455L;

    private GetFanAssetInfoResPack data;

    public GetFanAssetInfoResPack getData() {
        return data;
    }

    public void setData(GetFanAssetInfoResPack data) {
        this.data = data;
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
    public static final Creator<RemoveHomeApplianceResPack> CREATOR = new Creator<RemoveHomeApplianceResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public RemoveHomeApplianceResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (RemoveHomeApplianceResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public RemoveHomeApplianceResPack[] newArray(int size) {
            return new RemoveHomeApplianceResPack[size];
        }
    };
}
