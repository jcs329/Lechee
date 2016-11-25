package com.quantatw.sls.pack.account;

import java.io.Serializable;

import com.quantatw.sls.pack.base.BaseResPack;

import android.os.Parcel;
import android.os.Parcelable;

public class AccountResPack extends BaseResPack implements Serializable,Parcelable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9214516788979239881L;

	private String token;
	private String tokenType;
	private int expireIn;
	private String mqttTopic;

	public String getToken() {
		return token;
	}
	public String getTokenType() {
		return tokenType;
	}
	public int getExpireIn() {
		return expireIn;
	}

	public String getMqttTopic() {
		return mqttTopic;
	}

	public void setMqttTopic(String mqttTopic) {
		mqttTopic = mqttTopic;
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
	public static final Creator<AccountResPack> CREATOR = new Parcelable.Creator<AccountResPack>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public AccountResPack createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (AccountResPack) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public AccountResPack[] newArray(int size) {
			return new AccountResPack[size];
		}
	};
}
