package com.quantatw.sls.pack.roomhub;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.device.FirmwareVersion;
import com.quantatw.sls.pack.base.BaseResPack;

import java.io.Serializable;

public class VersionCheckUpdateResPack extends BaseResPack implements Serializable,Parcelable{

	private static final long serialVersionUID = -6959092342282030203L;

	private FirmwareVersion data;

	public FirmwareVersion getData() {
		return data;
	}

	public void setData(FirmwareVersion data) {
		this.data = data;
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
	public static final Creator<VersionCheckUpdateResPack> CREATOR = new Creator<VersionCheckUpdateResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public VersionCheckUpdateResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (VersionCheckUpdateResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public VersionCheckUpdateResPack[] newArray(int size) {
			return new VersionCheckUpdateResPack[size];
		}
	};
}
