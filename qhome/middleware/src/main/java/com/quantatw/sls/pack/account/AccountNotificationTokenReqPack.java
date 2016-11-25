package com.quantatw.sls.pack.account;

import java.io.Serializable;

import com.quantatw.sls.pack.base.BaseReqPack;

import android.os.Parcel;
import android.os.Parcelable;

public class AccountNotificationTokenReqPack extends BaseReqPack implements Serializable,Parcelable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2033302804707983206L;

	private String uuid;
	private String appName;
	private String deviceType;
	private String token;

	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
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
	public static final Creator<AccountNotificationTokenReqPack> CREATOR = new Parcelable.Creator<AccountNotificationTokenReqPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public AccountNotificationTokenReqPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (AccountNotificationTokenReqPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public AccountNotificationTokenReqPack[] newArray(int size) {
			return new AccountNotificationTokenReqPack[size];
		}
	};
}
