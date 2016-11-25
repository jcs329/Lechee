package com.quantatw.sls.wificonfig;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;


public class SSIDInfo implements Serializable,Parcelable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7988537742779120512L;
	private String ssid;
	private String pw;
	private int signal;
	private int type;
	
	
	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getPw() {
		return pw;
	}

	public void setPw(String pw) {
		this.pw = pw;
	}

	public int getSignal() {
		return signal;
	}

	public void setSignal(int signal) {
		this.signal = signal;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
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
	public static final Creator<SSIDInfo> CREATOR = new Parcelable.Creator<SSIDInfo>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public SSIDInfo createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (SSIDInfo) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public SSIDInfo[] newArray(int size) {
			return new SSIDInfo[size];
		}
	};
}
