package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

/**
 * Created by 10110012 on 2016/6/22.
 */
public class ModifyScheduleBulbResPack extends BaseResPack {
    private static final long serialVersionUID = 2094358958636576590L;

    private int method;
    private int assetType;
    private int groupId;
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

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
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
    public static final Creator<ModifyScheduleBulbResPack> CREATOR = new Creator<ModifyScheduleBulbResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public ModifyScheduleBulbResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (ModifyScheduleBulbResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public ModifyScheduleBulbResPack[] newArray(int size) {
            return new ModifyScheduleBulbResPack[size];
        }
    };
}
