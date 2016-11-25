package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseResPack;

public class SetAssetInfoResPack extends BaseResPack {
    private static final long serialVersionUID = -4676054614440536806L;

    private int method;
    private int assetType;
    private int subtype;
    private int connectionType;
    private int result;

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

    public int getSubType() {
        return subtype;
    }

    public void setSubType(int subtype) {
        this.subtype = subtype;
    }

    public int getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
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
    public static final Creator<SetAssetInfoResPack> CREATOR = new Parcelable.Creator<SetAssetInfoResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public SetAssetInfoResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (SetAssetInfoResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public SetAssetInfoResPack[] newArray(int size) {
            return new SetAssetInfoResPack[size];
        }
    };
}
