package com.quantatw.sls.device;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Asset implements Serializable , Parcelable{

	private static final long serialVersionUID = -6672066423637294421L;

	protected String roomHubUUID;
	protected String uuid;
	protected int brandId;
	protected String modelId;
	protected String brandName;
	protected String modelName;
	protected int assetType;
	protected int power;

	public String getUuid() {
		return roomHubUUID;
	}

	public void setUuid(String roomhub_uuid) {
		this.roomHubUUID = roomhub_uuid;
	}

	public String getAssetUuid() {
		return uuid;
	}

	public void setAssetUuid(String uuid) {
		this.uuid = uuid;
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

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public int getAssetType() {
		return assetType;
	}

	public void setAssetType(int assetType) {
		this.assetType = assetType;
	}

	public int isPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
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
	public static final Creator<Asset> CREATOR = new Creator<Asset>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public Asset createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (Asset) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public Asset[] newArray(int size) {
			return new Asset[size];
		}
	};



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Asset other = (Asset) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
	
	
}
