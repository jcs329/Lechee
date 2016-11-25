package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseReqPack;

public class UpgradeReqPack extends BaseReqPack {
    private static final long serialVersionUID = -5551062312553239574L;

    private String imageURL;
    private String md5;

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
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
    public static final Creator<UpgradeReqPack> CREATOR = new Creator<UpgradeReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public UpgradeReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (UpgradeReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public UpgradeReqPack[] newArray(int size) {
            return new UpgradeReqPack[size];
        }
    };
}
