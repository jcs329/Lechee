package com.quantatw.sls.device;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.quantatw.sls.key.SourceType;

public class AlljoynDevice implements Serializable , Parcelable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3864996291206328985L;

	protected CloudDevice extraInfo;
	protected SourceType sourceType;

	protected int sessionid;
	protected String uuid;
	protected String name;
//	protected String ip ;
	protected int category;	// device category
	protected int mappingType;	// device type after converted. ex: app device type

	public CloudDevice getExtraInfo() { return extraInfo; }
	public void setExtraInfo(CloudDevice extraInfo) { this.extraInfo = extraInfo; }

	public SourceType getSourceType() { return this.sourceType; }
	public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }

	public AlljoynDevice()
	{
		sessionid = 0;
	}

	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	

	public int getSessionid() {
		return sessionid;
	}
	public void setSessionid(int sessionid) {
		this.sessionid = sessionid;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getMappingType() {
		return mappingType;
	}

	public void setMappingType(int mappingType) {
		this.mappingType = mappingType;
	}

//	public String getIp() {
//		return ip;
//	}
//
//	public void setIp(String ip) {
//		this.ip = ip;
//	}

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
	public static final Creator<AlljoynDevice> CREATOR = new Parcelable.Creator<AlljoynDevice>() {

		/**
		 * Read the serialized concrete strategy from the parcel.
		 * 
		 * @param in
		 *            The parcel to read from
		 * @return An AbstractStrategy
		 */
		public AlljoynDevice createFromParcel(Parcel in) {
			// Read serialized concrete strategy from parcel
			return (AlljoynDevice) in.readSerializable();
		}

		/**
		 * Required by Creator
		 */
		public AlljoynDevice[] newArray(int size) {
			return new AlljoynDevice[size];
		}
	};
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + sessionid;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AlljoynDevice other = (AlljoynDevice) obj;
		
		if(sessionid == other.sessionid  && sessionid != 0)
		{
			return true;
		}else 
		{
			if(uuid == null)
			{
				return false;
			}
			else {
				if(other.uuid == null)
				{
					return false;
				}
				else {
					if(uuid.equalsIgnoreCase(other.uuid))
					{
						return true;
					}
					else {
						return false;
					}
				}
			}
		}
		

	}

	
	public AlljoynDevice clone()  {
		// TODO Auto-generated method stub
		
		AlljoynDevice dev = new AlljoynDevice();
		dev.setName(name);

		dev.setSessionid(sessionid);

		dev.setUuid(uuid);

		
		return dev;
	}
	
	public void UpdateSensor(AlljoynDevice dev)
	{
		
		//if((device_name == null || device_name.length() == 0) && (dev.getDevice_name() != null && dev.getDevice_name().length() >0) )
		//	device_name = dev.getDevice_name();
        
//		ip = dev.getIp();
	}

}
