package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class GetHomeApplianceAllAssetsResPack extends BaseResPack {
    private static final long serialVersionUID = -2599819705751336043L;

    private ArrayList<HomeApplianceAsset> assets;
    private int method;
    private int result;

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public ArrayList<HomeApplianceAsset> getAssets() {
        return assets;
    }

    public void setAssets(ArrayList<HomeApplianceAsset> assets) {
        this.assets = assets;
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
    public static final Creator<GetHomeApplianceAllAssetsResPack> CREATOR = new Creator<GetHomeApplianceAllAssetsResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public GetHomeApplianceAllAssetsResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (GetHomeApplianceAllAssetsResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public GetHomeApplianceAllAssetsResPack[] newArray(int size) {
            return new GetHomeApplianceAllAssetsResPack[size];
        }
    };
}
