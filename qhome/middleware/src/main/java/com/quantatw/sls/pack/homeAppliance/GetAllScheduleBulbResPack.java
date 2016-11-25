package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

/**
 * Created by 10110012 on 2016/6/22.
 */
public class GetAllScheduleBulbResPack extends BaseResPack {

    private static final long serialVersionUID = 795716719424423164L;
    private int method;
    private int assetType;
    private int result;
    private ArrayList<BulbScheduleData> schedules;

    public ArrayList<BulbScheduleData> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<BulbScheduleData> schedules) {
        this.schedules = schedules;
    }

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
    public static final Creator<GetAllScheduleBulbResPack> CREATOR = new Creator<GetAllScheduleBulbResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public GetAllScheduleBulbResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (GetAllScheduleBulbResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public GetAllScheduleBulbResPack[] newArray(int size) {
            return new GetAllScheduleBulbResPack[size];
        }
    };
}
