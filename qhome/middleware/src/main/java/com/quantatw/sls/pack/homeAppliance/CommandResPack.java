package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseResPack;

public class CommandResPack extends BaseResPack {
    private static final long serialVersionUID = -2483960582285515471L;

    private int method;
    private int assetType;
    private int result;     // 0: success; -1: error

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
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
    public static final Creator<RemoveHomeApplianceResPack> CREATOR = new Parcelable.Creator<RemoveHomeApplianceResPack>() {

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
