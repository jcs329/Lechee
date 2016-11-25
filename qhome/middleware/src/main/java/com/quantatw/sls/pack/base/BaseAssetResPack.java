package com.quantatw.sls.pack.base;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class BaseAssetResPack extends BaseResPack {
	private static final long serialVersionUID = 303833873766923187L;

	private int method;
	private int assetType;
	private int subtype;
	private int connectionType;
	private String brand="";	// brand name
	private String device="";	// device name
	private int brandId;	// brand ID
	private String modelId="";	// model ID
	private int onlineStatus=1; //0: offline 1: online,

	public int getAssetType() {
		return assetType;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
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

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

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

	public int getOnLineStatus() {
		return onlineStatus;
	}

	public void setOnLineStatus(int onlineStatus) {
		this.onlineStatus = onlineStatus;
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
	public static final Creator<BaseAssetResPack> CREATOR = new Creator<BaseAssetResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public BaseAssetResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (BaseAssetResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public BaseAssetResPack[] newArray(int size) {
			return new BaseAssetResPack[size];
		}
	};

}
