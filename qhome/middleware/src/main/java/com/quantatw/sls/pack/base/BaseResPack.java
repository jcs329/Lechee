package com.quantatw.sls.pack.base;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseResPack implements Serializable,Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8547277331766167081L;
	
	private int statusCode;
	private String statusMsg;
	private String uuid;
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getStatus_code() {
		return statusCode;
	}

	public void setStatus_code(int statusCode) {
		this.statusCode = statusCode;
	}
	
	public String getMessgae() {
		return statusMsg;
	}

	public void setMessgae(String messgae) {
		this.statusMsg = messgae;
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
	public static final Creator<BaseResPack> CREATOR = new Parcelable.Creator<BaseResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public BaseResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (BaseResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public BaseResPack[] newArray(int size) {
			return new BaseResPack[size];
		}
	};

}
