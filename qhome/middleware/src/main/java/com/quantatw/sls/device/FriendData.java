package com.quantatw.sls.device;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.key.SourceType;

import java.io.Serializable;

public class FriendData implements Serializable , Parcelable,Comparable<FriendData>{
	/**
	 *
	 */
	private static final long serialVersionUID = -3864996291206328985L;

	protected String nickName;
	protected String userAccount;
	protected String email;
	protected String userId;
	protected boolean shared;

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean getShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
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
	public static final Creator<FriendData> CREATOR = new Creator<FriendData>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public FriendData createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (FriendData) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public FriendData[] newArray(int size) {
			return new FriendData[size];
		}
	};

	@Override
	public int compareTo(FriendData another) {
		return userId.compareToIgnoreCase(another.getUserId());
	}
}
