package com.quantatw.sls.device;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ScanAsset implements Serializable , Parcelable{
	private static final long serialVersionUID = -5255358648698574602L;

	protected String uuid;
	protected int assetType;
	protected String deviceName;
	protected int connectionType;
	private String brand;	// brand name
	private String device;	// device name
	private int brandId;	// brand ID
	private String modelId;	// model ID
	private String default_user_id;

	public void setUuid(String uuid) { this.uuid = uuid; }

	public String getUuid() { return this.uuid; }

	public void setAssetType(int assetType) { this.assetType = assetType; }

	public int getAssetType() { return this.assetType; }

	public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

	public String getDeviceName() { return this.deviceName; }

	public void setConnectionType(int connectionType) { this.connectionType = connectionType; }

	public int getConnectionType() { return this.connectionType; }

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

	public String getDefaultUserId() {
		return default_user_id;
	}

	public void setDefaultUserId(String default_user_id) {
		this.default_user_id = default_user_id;
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
	public static final Creator<ScanAsset> CREATOR = new Creator<ScanAsset>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public ScanAsset createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (ScanAsset) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public ScanAsset[] newArray(int size) {
			return new ScanAsset[size];
		}
	};
}
