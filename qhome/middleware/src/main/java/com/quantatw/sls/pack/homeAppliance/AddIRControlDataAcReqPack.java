package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;

import com.quantatw.sls.alljoyn.RoomHubAllJoynDef;
import com.quantatw.sls.pack.base.BaseReqPack;

public class AddIRControlDataAcReqPack extends BaseReqPack {
    private static final long serialVersionUID = 6197614668616524974L;

    private int assetType = RoomHubAllJoynDef.assetType.ASSET_TYPE_AC;
    private String uuid;
    private int keyId;
    private String irData;
    private String stPower;
    private String stMode;
    private int stTemp;
    private String stFan;
    private String stSwing;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public String getIrData() {
        return irData;
    }

    public void setIrData(String irData) {
        this.irData = irData;
    }

    public String getStPower() {
        return stPower;
    }

    public void setStPower(String stPower) {
        this.stPower = stPower;
    }

    public String getStMode() {
        return stMode;
    }

    public void setStMode(String stMode) {
        this.stMode = stMode;
    }

    public int getStTemp() {
        return stTemp;
    }

    public void setStTemp(int stTemp) {
        this.stTemp = stTemp;
    }

    public String getStFan() {
        return stFan;
    }

    public void setStFan(String stFan) {
        this.stFan = stFan;
    }

    public String getStSwing() {
        return stSwing;
    }

    public void setStSwing(String stSwing) {
        this.stSwing = stSwing;
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
    public static final Creator<AddIRControlDataAcReqPack> CREATOR = new Creator<AddIRControlDataAcReqPack>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AddIRControlDataAcReqPack createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AddIRControlDataAcReqPack) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AddIRControlDataAcReqPack[] newArray(int size) {
            return new AddIRControlDataAcReqPack[size];
        }
    };
}
