package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class GetAbilityLimitAcResPack extends BaseResPack {
    private static final long serialVersionUID = -7586794876054082L;

    private int method;
    private int assetType;
    private ArrayList<HomeApplianceAbilityAc> ability;

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

    public ArrayList<HomeApplianceAbilityAc> getAbility() {
        return ability;
    }

    public void setAbility(ArrayList<HomeApplianceAbilityAc> ability) {
        this.ability = ability;
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
    public static final Creator<GetAbilityLimitAcResPack> CREATOR = new Creator<GetAbilityLimitAcResPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public GetAbilityLimitAcResPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (GetAbilityLimitAcResPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public GetAbilityLimitAcResPack[] newArray(int size) {
            return new GetAbilityLimitAcResPack[size];
        }
    };
}
