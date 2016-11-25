package com.quantatw.sls.pack.base;

import android.os.Parcel;

public class BaseAssetDetailInfoResPack extends BaseResPack {
	private static final long serialVersionUID = 6010595400923246228L;

	private int subtype;
	private int connectionType;
	private String brand="";	// brand name
	private String device="";	// device name
	private int brandId;	// brand ID
	private String modelId="";	// model ID
	private int onlineStatus=1; //0: offline 1: online,
	private String roomHubUUID;

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

	public void setRoomHubUUID(String roomHubUUID) { this.roomHubUUID = roomHubUUID; }

	public String getRoomHubUUID() { return this.roomHubUUID; }
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
	public static final Creator<BaseAssetDetailInfoResPack> CREATOR = new Creator<BaseAssetDetailInfoResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public BaseAssetDetailInfoResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (BaseAssetDetailInfoResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public BaseAssetDetailInfoResPack[] newArray(int size) {
			return new BaseAssetDetailInfoResPack[size];
		}
	};

}
