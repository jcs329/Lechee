package com.quantatw.sls.pack.device;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;

public class GetDevicesReqPack extends BaseReqPack {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9106057783635414468L;

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
	public static final Creator<GetDevicesReqPack> CREATOR = new Parcelable.Creator<GetDevicesReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public GetDevicesReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (GetDevicesReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public GetDevicesReqPack[] newArray(int size) {
			return new GetDevicesReqPack[size];
		}
	};
}
