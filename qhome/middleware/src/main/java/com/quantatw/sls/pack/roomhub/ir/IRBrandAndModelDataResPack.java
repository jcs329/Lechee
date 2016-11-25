package com.quantatw.sls.pack.roomhub.ir;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.object.IRBrandAndModelData;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class IRBrandAndModelDataResPack extends BaseResPack {
    private static final long serialVersionUID = 8178791084964889855L;
    private ArrayList<IRBrandAndModelData> data;

    public ArrayList<IRBrandAndModelData> getBrandAndModelDataList() {
        return data;
    }
    public void setBrandAndModelDataList(ArrayList<IRBrandAndModelData> data) {
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
    public static final Creator<IRBrandAndModelDataResPack> CREATOR = new Parcelable.Creator<IRBrandAndModelDataResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRBrandAndModelDataResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRBrandAndModelDataResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRBrandAndModelDataResPack[] newArray(int size) {
            return new IRBrandAndModelDataResPack[size];
        }
    };
}
