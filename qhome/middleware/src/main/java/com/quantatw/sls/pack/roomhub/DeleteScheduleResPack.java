package com.quantatw.sls.pack.roomhub;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.pack.base.BaseResPack;

public class DeleteScheduleResPack extends BaseResPack {
    private static final long serialVersionUID = 5761343173285134303L;

    private int index;

    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
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
    public static final Creator<DeleteScheduleResPack> CREATOR = new Parcelable.Creator<DeleteScheduleResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public DeleteScheduleResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (DeleteScheduleResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public DeleteScheduleResPack[] newArray(int size) {
            return new DeleteScheduleResPack[size];
        }
    };
}