package com.quantatw.roomhub.ir;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.object.IRACKeyData;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by erin on 10/27/15.
 */
public class ApIRParingInfo implements Serializable,Parcelable{

    String uuid;
    String assetUuid;
    int assetType;
    String brandName;
    String devModelNumber;
    String remoteModelNum;
    int codeNum;

    // test key button:
    int testButton;
    // test mode:
    int testCurrentMode;
    // test temperature:
    int testCurrentTemp;

    // paring ir data:
    ArrayList<IRACKeyData> iracKeyDataList;

    // for auto scan:
    String autoScanIrData;

    private int brandId;	// brand ID
    private String modelId;	// model ID

    private int subtype;
    private int connectionType;

    private boolean checkIrDataFailed;

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public void setCheckIrDataFailed(boolean isFail) {
        this.checkIrDataFailed = isFail;
    }

    public boolean isCheckIrDataFailed() {
        return this.checkIrDataFailed;
    }

    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getUuid() { return this.uuid; }

    public String getAssetUuid() {
        return assetUuid;
    }

    public void setAssetUuid(String assetUuid) {
        this.assetUuid = assetUuid;
    }

    public int getAssetType() {
        return assetType;
    }

    public void setAssetType(int assetType) {
        this.assetType = assetType;
    }

    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getBrandName() { return this.brandName; }

    public void setDevModelNumber(String devModelNumber) { this.devModelNumber = devModelNumber; }

    public String getDevModelNumber() { return this.devModelNumber; }

    public void setCodeNum(int codeNum) { this.codeNum = codeNum; }

    public int getCodeNum() { return this.codeNum; }

    public void setIracKeyDataList(ArrayList<IRACKeyData> list) { this.iracKeyDataList = list; }

    public ArrayList<IRACKeyData> getIracKeyDataList() { return this.iracKeyDataList; }

    public void setAutoScanIrData(String irData) { this.autoScanIrData = irData; }

    public String getAutoScanIrData() { return this.autoScanIrData; }

    public String getRemoteModelNum() {
        return remoteModelNum;
    }

    public void setRemoteModelNum(String remoteModelNum) {
        this.remoteModelNum = remoteModelNum;
    }

    public int getTestButton() {
        return testButton;
    }

    public void setTestButton(int testButton) {
        this.testButton = testButton;
    }

    public int getTestCurrentMode() {
        return testCurrentMode;
    }

    public void setTestCurrentMode(int testCurrentMode) {
        this.testCurrentMode = testCurrentMode;
    }

    public int getTestCurrentTemp() {
        return testCurrentTemp;
    }

    public void setTestCurrentTemp(int testCurrentTemp) {
        this.testCurrentTemp = testCurrentTemp;
    }

    public int getSubType() {
        return subtype;
    }

    public void setSubType(int subtype) {
        this.subtype = subtype;
    }

    public int getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * The creator that MUST be defined and named "CREATOR" so that the service
     * generated from AIDL can recreate AbstractStrategys after IPC.
     */
    public static final Creator<ApIRParingInfo> CREATOR = new Creator<ApIRParingInfo>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public ApIRParingInfo createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (ApIRParingInfo) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public ApIRParingInfo[] newArray(int size) {
            return new ApIRParingInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
