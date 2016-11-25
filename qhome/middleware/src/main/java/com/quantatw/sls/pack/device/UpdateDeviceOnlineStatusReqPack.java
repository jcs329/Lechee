package com.quantatw.sls.pack.device;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;

public class UpdateDeviceOnlineStatusReqPack extends BaseReqPack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7787342391446233925L;
	private String uuid;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
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
	public static final Creator<UpdateDeviceOnlineStatusReqPack> CREATOR = new Parcelable.Creator<UpdateDeviceOnlineStatusReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public UpdateDeviceOnlineStatusReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (UpdateDeviceOnlineStatusReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public UpdateDeviceOnlineStatusReqPack[] newArray(int size) {
			return new UpdateDeviceOnlineStatusReqPack[size];
		}
	};
}
