package com.quantatw.sls.pack.device;

import android.os.Parcel;

import com.quantatw.sls.device.AlljoynDevice;
import com.quantatw.sls.device.FriendData;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class DeviceUserResPack extends BaseResPack{

	private static final long serialVersionUID = 7167660923873727245L;

	private FriendData defaultUser;

	public FriendData getDefaultUser() {
		return defaultUser;
	}

	public void setDefaultUser(FriendData defaultUser) {
		this.defaultUser = defaultUser;
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
	public static final Creator<DeviceUserResPack> CREATOR = new Creator<DeviceUserResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public DeviceUserResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (DeviceUserResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public DeviceUserResPack[] newArray(int size) {
			return new DeviceUserResPack[size];
		}
	};
	
}
