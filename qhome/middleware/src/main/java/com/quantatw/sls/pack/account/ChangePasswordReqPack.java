package com.quantatw.sls.pack.account;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ChangePasswordReqPack extends BaseReqPack {
	/**
	 * 
	 */
	private static final long serialVersionUID = 819688939603766927L;
	private String oriPassword;
	private String password;
	public String getOldPassword() {
		return oriPassword;
	}
	public void setOldPassword(String oriPassword) {
		this.oriPassword = oriPassword;
	}
	public String getNewPassword() {
		return password;
	}
	public void setNewPassword(String password) {
		this.password = password;
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
	public static final Creator<ChangePasswordReqPack> CREATOR = new Parcelable.Creator<ChangePasswordReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public ChangePasswordReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (ChangePasswordReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public ChangePasswordReqPack[] newArray(int size) {
			return new ChangePasswordReqPack[size];
		}
	};
}
