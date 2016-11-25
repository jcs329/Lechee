package com.quantatw.sls.pack.base;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseReqPack implements Serializable,Parcelable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6812878506036987984L;
	
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
	public static final Creator<BaseReqPack> CREATOR = new Parcelable.Creator<BaseReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public BaseReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (BaseReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public BaseReqPack[] newArray(int size) {
			return new BaseReqPack[size];
		}
	};


}
