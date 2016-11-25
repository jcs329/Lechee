package com.quantatw.sls.pack.setting;


import com.quantatw.sls.pack.base.BaseResPack;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SettingResPack extends BaseResPack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7746470646601056073L;
	

	private String className;
	private int device_type;
	private String device_setting;
	
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public int getDevice_type() {
		return device_type;
	}
	public void setDevice_type(int device_type) {
		this.device_type = device_type;
	}
	public String getDevice_setting() {
		return device_setting;
	}
	public void setDevice_setting(String device_setting) {
		this.device_setting = device_setting;
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
	public static final Creator<SettingResPack> CREATOR = new Parcelable.Creator<SettingResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public SettingResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (SettingResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public SettingResPack[] newArray(int size) {
			return new SettingResPack[size];
		}
	};
}
