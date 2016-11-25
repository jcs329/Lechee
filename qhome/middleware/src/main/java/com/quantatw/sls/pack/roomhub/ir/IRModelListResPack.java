package com.quantatw.sls.pack.roomhub.ir;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.object.IRModelData;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class IRModelListResPack extends BaseResPack {
    private static final long serialVersionUID = 1108694817387863701L;
    private ArrayList<IRModelData> data;

    public ArrayList<IRModelData> getModelList() {
        return data;
    }
    public void setModelList(ArrayList<IRModelData> data) {
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
    public static final Creator<IRModelListResPack> CREATOR = new Parcelable.Creator<IRModelListResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public IRModelListResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (IRModelListResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public IRModelListResPack[] newArray(int size) {
            return new IRModelListResPack[size];
        }
    };
}
