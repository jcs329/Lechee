package com.quantatw.sls.pack.device;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;

public class AddDeviceReqPack extends BaseReqPack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1258287310259156170L;

	private String user_id;
	private String uuid;
	private String deviceName;
	private int deviceType;
	private int category;
	private String townId;
	private String version;
	private String brandName;
	private String modelName;
	private String roomHubUUID;

	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getDevice_name() {
		return deviceName;
	}
	public void setDevice_name(String deviceName) {
		this.deviceName = deviceName;
	}
	
	
	public int getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public String getTownId() {
		return townId;
	}
	public void setTownId(String townId) {
		this.townId = townId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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

	public String getRoomHubUUID() {
		return roomHubUUID;
	}

	public void setRoomHubUUID(String roomHubUUID) {
		this.roomHubUUID = roomHubUUID;
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
	public static final Creator<AddDeviceReqPack> CREATOR = new Parcelable.Creator<AddDeviceReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public AddDeviceReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (AddDeviceReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public AddDeviceReqPack[] newArray(int size) {
			return new AddDeviceReqPack[size];
		}
	};
	
}
