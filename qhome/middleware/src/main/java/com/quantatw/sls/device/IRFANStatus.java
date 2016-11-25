package com.quantatw.sls.device;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class IRFANStatus implements Serializable , Parcelable{
	private static final long serialVersionUID = -3818662280851980591L;

	private String roomHubUUID;
	private String uuid;
	private int keyId;
	private String brand;	// brand name
	private String device;	// device name
	private String userId; //user id
	private int assetType;
	private int power;	// power status
	private String property;
	//private String property;

	public void setRoomHubUUID(String roomHubUUID) { this.roomHubUUID = roomHubUUID; }

	public String getRoomHubUUID() { return this.roomHubUUID; }

	public void setUuid(String uuid) { this.uuid = uuid; }

	public String getUuid() { return this.uuid; }

	public void setKeyId(int keyId) { this.keyId = keyId; }

	public int getKeyId() { return this.keyId; }

	public void setBrand(String brand) { this.brand = brand; }

	public String getBrand() { return this.brand; }

	public void setDevice(String device) { this.device = device; }

	public String getDevice() { return this.device; }

	public void setUserId(String userId) { this.userId = userId; }

	public String getUserId() { return userId; }

	public int getAssetType() {
		return assetType;
	}

	public void setAssetType(int assetType) {
		this.assetType = assetType;
	}

	public void setPower(int power) { this.power = power; }

	public int getPower() { return this.power; }

	public void setProperty(String property) { this.property = property; }

	public String getProperty() { return this.property; }
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
	public static final Creator<IRFANStatus> CREATOR = new Creator<IRFANStatus>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 *
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public IRFANStatus createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (IRFANStatus) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public IRFANStatus[] newArray(int size) {
			return new IRFANStatus[size];
		}
	};
}
