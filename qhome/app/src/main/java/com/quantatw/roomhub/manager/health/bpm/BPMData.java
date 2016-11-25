package com.quantatw.roomhub.manager.health.bpm;

import android.os.Parcel;
import android.os.Parcelable;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.pack.healthcare.BPMDataInfo;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by erin on 4/28/16.
 */
public class BPMData extends HealthData {

    private BPMDataInfo mLastHistory;
    private ArrayList<BPMDataInfo> mHistoryList;

    public BPMData() {
        super(DeviceTypeConvertApi.TYPE_HEALTH.BPM);
    }

    public int getDaysBeforeCheck() {
        BPMDataInfo lastData = mLastHistory;

        if(lastData != null) {
            return BPMUtils.getDaysBetweenDates(lastData.getMeasureDate(),new Date(System.currentTimeMillis()));
        }

        return -1;
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
    public static final Creator<BPMData> CREATOR = new Parcelable.Creator<BPMData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public BPMData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (BPMData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public BPMData[] newArray(int size) {
            return new BPMData[size];
        }
    };

    public BPMDataInfo getLastHistory() {
        return mLastHistory;
    }

    public void setLastHistory(BPMDataInfo mLastHistory) {
        this.mLastHistory = mLastHistory;
    }

    public ArrayList<BPMDataInfo> getHistoryList() {
        return mHistoryList;
    }

    public void setHistoryList(ArrayList<BPMDataInfo> mHistoryList) {
        this.mHistoryList = mHistoryList;
    }
}
