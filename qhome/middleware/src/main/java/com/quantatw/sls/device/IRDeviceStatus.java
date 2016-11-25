package com.quantatw.sls.device;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class IRDeviceStatus implements Serializable , Parcelable{
	/**
	 *
	 */
	private static final long serialVersionUID = -3864996291206328985L;

	private String roomHubUUID;
	private String uuid;
	private int power;	// power status
	private int temp;	// Temperature value
	private int mode;	// working mode
	private int swing;	// swing angle
	private int fan;	// fan level
	private int timerOn;	// timing on
	private int timerOff;	// timing off
	private String brand;	// brand name
	private String device;	// device name
	private String townId;	// town unique id
	private String userId; //user id
	private int subtype;
	private int connectionType;
	private int brandId;	// brand ID
	private String modelId;	// model ID
	private int onlineStatus=1; //0: offline 1: online,

	public void setRoomHubUUID(String roomHubUUID) { this.roomHubUUID = roomHubUUID; }

	public String getRoomHubUUID() { return this.roomHubUUID; }

	public void setUuid(String uuid) { this.uuid = uuid; }

	public String getUuid() { return this.uuid; }

	public void setPower(int power) { this.power = power; }

	public int getPower() { return this.power; }

	public void setTemp(int temp) { this.temp = temp; }

	public int getTemp() { return this.temp; }

	public void setMode(int mode) { this.mode = mode; }

	public int getMode() { return this.mode; }

	public void setSwing(int swing) { this.swing = swing; }

	public int getSwing() { return this.swing; }

	public void setFan(int fan) { this.fan = fan; }

	public int getFan() { return this.fan; }

	public void setTimeOn(int timerOn) { this.timerOn = timerOn; }

	public int getTimeOn() { return this.timerOn; }

	public void setTimeOff(int timerOn) { this.timerOff = timerOff; }

	public int getTimeOff() { return this.timerOff; }

	public void setBrand(String brand) { this.brand = brand; }

	public String getBrand() { return this.brand; }

	public void setDevice(String device) { this.device = device; }

	public String getDevice() { return this.device; }

	public void setTownId(String townId) { this.townId = townId; }

	public String getTownId() { return townId; }

	public void setUserId(String userId) { this.userId = userId; }

	public String getUserId() { return userId; }

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
	public static final Creator<IRDeviceStatus> CREATOR = new Creator<IRDeviceStatus>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 *
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public IRDeviceStatus createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (IRDeviceStatus) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public IRDeviceStatus[] newArray(int size) {
			return new IRDeviceStatus[size];
		}
	};
}
