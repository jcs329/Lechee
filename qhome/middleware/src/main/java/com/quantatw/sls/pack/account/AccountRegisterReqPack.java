package com.quantatw.sls.pack.account;

import java.io.Serializable;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;

public class AccountRegisterReqPack extends BaseReqPack implements Serializable,Parcelable{

	private static final long serialVersionUID = 1110573713169692750L;
	/**
	 * 
	 */
	private String userAccount;
	private String userPw;
	private String userName;
	private String email;
	private String clientId;
	public String getUserAccount() {
		return userAccount;
	}
	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}
	public String getUserPw() {
		return userPw;
	}
	public void setUserPw(String userPw) {
		this.userPw = userPw;
	}

	@Deprecated
	public String getUserName() {
		return userName;
	}
	@Deprecated
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
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
	public static final Creator<AccountRegisterReqPack> CREATOR = new Parcelable.Creator<AccountRegisterReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public AccountRegisterReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (AccountRegisterReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public AccountRegisterReqPack[] newArray(int size) {
			return new AccountRegisterReqPack[size];
		}
	};
}
