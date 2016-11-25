package com.quantatw.sls.pack.device;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;

public class DeleteDeviceReqPack extends BaseReqPack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1171010290955255311L;
	private String uuid;
	private int deviceType;
	private int category;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
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
	public static final Creator<DeleteDeviceReqPack> CREATOR = new Parcelable.Creator<DeleteDeviceReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public DeleteDeviceReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (DeleteDeviceReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public DeleteDeviceReqPack[] newArray(int size) {
			return new DeleteDeviceReqPack[size];
		}
	};
}
