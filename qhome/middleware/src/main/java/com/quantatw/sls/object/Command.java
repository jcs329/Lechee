package com.quantatw.sls.object;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;
import com.quantatw.sls.key.CommandKey;

public class Command implements Serializable , Parcelable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6755980279026858107L;
	CommandKey commandKey;
	Object	   pack;
	public CommandKey getCommandKey() {
		return commandKey;
	}
	public void setCommandKey(CommandKey commandKey) {
		this.commandKey = commandKey;
	}
	public Object getPack() {
		return pack;
	}
	public void setPack(Object pack) {
		this.pack = pack;
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
	public static final Creator<Command> CREATOR = new Parcelable.Creator<Command>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public Command createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (Command) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public Command[] newArray(int size) {
			return new Command[size];
		}
	};
}
