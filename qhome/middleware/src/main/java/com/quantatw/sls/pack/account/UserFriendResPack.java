package com.quantatw.sls.pack.account;

import android.os.Parcel;

import com.quantatw.sls.device.FriendData;
import com.quantatw.sls.pack.base.BaseResPack;

import java.util.ArrayList;

public class UserFriendResPack extends BaseResPack{

	private static final long serialVersionUID = 4809949536329653275L;
	private String nickName;
	private String userAccount;
	private String userId;

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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
	public static final Creator<UserFriendResPack> CREATOR = new Creator<UserFriendResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public UserFriendResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (UserFriendResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public UserFriendResPack[] newArray(int size) {
			return new UserFriendResPack[size];
		}
	};
	
}
