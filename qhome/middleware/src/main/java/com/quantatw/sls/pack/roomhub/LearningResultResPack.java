package com.quantatw.sls.pack.roomhub;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.alljoyn.RoomHubInterface;
import com.quantatw.sls.pack.base.BaseResPack;

public class LearningResultResPack extends BaseResPack {
    private static final long serialVersionUID = 5965555696215126744L;

    private RoomHubInterface.irData_y[] signature;

    public RoomHubInterface.irData_y[] getIrData() {
        return signature;
    }
    public void setIrData(RoomHubInterface.irData_y[] signature) {
        this.signature = signature;
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
    public static final Creator<LearningResultResPack> CREATOR = new Parcelable.Creator<LearningResultResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public LearningResultResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (LearningResultResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public LearningResultResPack[] newArray(int size) {
            return new LearningResultResPack[size];
        }
    };

}
