package com.quantatw.sls.pack.setting;


import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SettingReqPack extends BaseReqPack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7746470646601056073L;
	
	private String uuid;
	private String className;
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
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
	public static final Creator<SettingReqPack> CREATOR = new Parcelable.Creator<SettingReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public SettingReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (SettingReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public SettingReqPack[] newArray(int size) {
			return new SettingReqPack[size];
		}
	};
}
