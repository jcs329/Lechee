package com.quantatw.sls.pack.homeAppliance;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class FirmwareUpdateStateResPack implements Serializable , Parcelable{
	private static final long serialVersionUID = 8270343155919563980L;

	protected int type;
	protected int state;
	protected String hubUUID;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getHubUUID() {
		return hubUUID;
	}

	public void setHubUUID(String hubUUID) {
		this.hubUUID = hubUUID;
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
	public static final Creator<FirmwareUpdateStateResPack> CREATOR = new Creator<FirmwareUpdateStateResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public FirmwareUpdateStateResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (FirmwareUpdateStateResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public FirmwareUpdateStateResPack[] newArray(int size) {
			return new FirmwareUpdateStateResPack[size];
		}
	};
}
