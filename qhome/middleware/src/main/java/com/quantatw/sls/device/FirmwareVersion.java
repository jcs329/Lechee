package com.quantatw.sls.device;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class FirmwareVersion implements Serializable , Parcelable{
	/**
	 *
	 */
	private static final long serialVersionUID = -3864996291206328985L;

	private String name;
	private String version;
	private String url;
	private String md5;

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getUrl() {
		return url;
	}

	public String getMd5() {
		return md5;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
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
	public static final Creator<FirmwareVersion> CREATOR = new Creator<FirmwareVersion>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public FirmwareVersion createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (FirmwareVersion) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public FirmwareVersion[] newArray(int size) {
			return new FirmwareVersion[size];
		}
	};
}
