package com.quantatw.sls.pack.roomhub.ir;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.object.IRBrandData;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class IRBrandListResPack extends BaseResPack {
    private static final long serialVersionUID = 1108694817387863701L;
    private ArrayList<IRBrandData> data;

    public ArrayList<IRBrandData> getBrandList() {
        return data;
    }
    public void setBrandList(ArrayList<IRBrandData> data) {
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
    public static final Creator<IRBrandListResPack> CREATOR = new Parcelable.Creator<IRBrandListResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRBrandListResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRBrandListResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRBrandListResPack[] newArray(int size) {
            return new IRBrandListResPack[size];
        }
    };
}
