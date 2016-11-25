package com.quantatw.sls.pack.account;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ForgetPasswordReqPack extends BaseReqPack {
	/**
	 * 
	 */
	private static final long serialVersionUID = 819688939603766927L;
	private String email;

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
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
	public static final Creator<ForgetPasswordReqPack> CREATOR = new Parcelable.Creator<ForgetPasswordReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public ForgetPasswordReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (ForgetPasswordReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public ForgetPasswordReqPack[] newArray(int size) {
			return new ForgetPasswordReqPack[size];
		}
	};
}
