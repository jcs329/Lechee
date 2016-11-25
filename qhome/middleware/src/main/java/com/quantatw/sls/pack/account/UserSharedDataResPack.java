package com.quantatw.sls.pack.account;

import android.os.Parcel;

import com.quantatw.sls.pack.base.BaseResPack;

public class UserSharedDataResPack extends BaseResPack{
	private static final long serialVersionUID = -2643802287949311614L;

	private String methodType; //Method type (Add / Del)
	private int deviceType;
	private String sharedUserId;
	private String sharedUserAccount;
	private String userId;
	private String userAccount;

	public String getMethodType() {
		return methodType;
	}

	public void setMethodType(String methodType) {
		this.methodType = methodType;
	}

	public int getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}

	public String getSharedUserId() {
		return sharedUserId;
	}

	public void setSharedUserId(String sharedUserId) {
		this.sharedUserId = sharedUserId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSharedUserAccount() {
		return sharedUserAccount;
	}

	public void setSharedUserAccount(String sharedUserAccount) {
		this.sharedUserAccount = sharedUserAccount;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
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
	public static final Creator<UserSharedDataResPack> CREATOR = new Creator<UserSharedDataResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public UserSharedDataResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (UserSharedDataResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public UserSharedDataResPack[] newArray(int size) {
			return new UserSharedDataResPack[size];
		}
	};
	
}
