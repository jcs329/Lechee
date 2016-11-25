package com.quantatw.sls.pack.account;

import java.io.Serializable;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;

public class AccountReqPack extends BaseReqPack implements Serializable,Parcelable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2033302804707983206L;
	private String user_account;
	private String user_pw;
	public String getUser_account() {
		return user_account;
	}
	public void setUser_account(String user_account) {
		this.user_account = user_account;
	}
	public String getUser_pw() {
		return user_pw;
	}
	public void setUser_pw(String user_pw) {
		this.user_pw = user_pw;
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
	public static final Creator<AccountReqPack> CREATOR = new Parcelable.Creator<AccountReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public AccountReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (AccountReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public AccountReqPack[] newArray(int size) {
			return new AccountReqPack[size];
		}
	};
}
