package com.quantatw.sls.pack.account;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

public class UserDataSharedReqPack extends BaseResPack{
	private static final long serialVersionUID = 936314067636506858L;

	private int deviceType;
	private String userId;
	private String type;

	public int getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
	public static final Creator<UserDataSharedReqPack> CREATOR = new Creator<UserDataSharedReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public UserDataSharedReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (UserDataSharedReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public UserDataSharedReqPack[] newArray(int size) {
			return new UserDataSharedReqPack[size];
		}
	};
	
}
