package com.quantatw.sls.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class AlljoynAboutData implements Serializable, Parcelable {
    private static final long serialVersionUID = 8383897040515091554L;

    private String DeviceId;
    private String AppName;
    private String state_file;
    private String Description;
    private String DateOfManufacture;

    private String connect_cmd;
    private String AppId;
    private String Manufacturer;
    private String SoftwareVersion;
    private String HardwareVersion;

    private String SupportedLanguages[];
    private String error_file;
    private String AJSoftwareVersion;
    private String offboard_cmd;
    private String ModelNumber;

    private String scan_cmd;
    private String DeviceName;
    private String SupportUrl;
    private String Daemonrealm;
    private String scan_file;

    private String configure_cmd;
    private String DefaultLanguage;
    private String Passcode;

    public String getDeviceId() {
        return DeviceId;
    }
    public void setDeviceId(String DeviceId) {
        this.DeviceId = DeviceId;
    }

    public String getAppName() {
        return AppName;
    }
    public void setAppName(String AppName) {
        this.AppName = AppName;
    }

    public String getStateFile() {
        return state_file;
    }
    public void setStateFile(String state_file) {
        this.state_file = state_file;
    }

    public String getDescription() {
        return Description;
    }
    public void setDescription(String Description) {
        this.Description = Description;
    }

    public String getDateOfManufacture() {
        return DateOfManufacture;
    }
    public void setDateOfManufacture(String DateOfManufacture) {
        this.DateOfManufacture = DateOfManufacture;
    }

    public String getConnectCmd() {
        return connect_cmd;
    }
    public void setConnectCmd(String connect_cmd) {
        this.connect_cmd = connect_cmd;
    }

    public String getAppId() {
        return AppId;
    }
    public void setAppId(String AppId) {
        this.AppId = AppId;
    }

    public String getManufacturer() {
        return Manufacturer;
    }
    public void setManufacturer(String Manufacturer) {
        this.Manufacturer = Manufacturer;
    }

    public String getSoftwareVersion() {
        return SoftwareVersion;
    }
    public void setSoftwareVersion(String SoftwareVersion) {
        this.SoftwareVersion = SoftwareVersion;
    }

    public String getHardwareVersion() {
        return HardwareVersion;
    }
    public void setHardwareVersion(String HardwareVersion) {
        this.HardwareVersion = HardwareVersion;
    }

    public String[] getSupportedLanguages() {
        return SupportedLanguages;
    }
    public void setSupportedLanguages(String[] SupportedLanguages) {
        this.SupportedLanguages = SupportedLanguages;
    }

    public String getErrorFile() {
        return error_file;
    }
    public void setErrorFile(String error_file) {
        this.error_file = error_file;
    }

    public String getAJSoftwareVersion() {
        return AJSoftwareVersion;
    }
    public void setAJSoftwareVersion(String AJSoftwareVersion) {
        this.AJSoftwareVersion = AJSoftwareVersion;
    }

    public String getOffboardCmd() {
        return offboard_cmd;
    }
    public void setOffboardCmd(String offboard_cmd) {
        this.offboard_cmd = offboard_cmd;
    }

    public String getModelNumber() {
        return ModelNumber;
    }
    public void setModelNumber(String ModelNumber) {
        this.ModelNumber = ModelNumber;
    }

    public String getScanCmd() {
        return scan_cmd;
    }
    public void setScanCmd(String scan_cmd) {
        this.scan_cmd = scan_cmd;
    }

    public String getDeviceName() {
        return DeviceName;
    }
    public void setDeviceName(String DeviceName) {
        this.DeviceName = DeviceName;
    }

    public String getSupportUrl() {
        return SupportUrl;
    }
    public void setSupportUrl(String SupportUrl) {
        this.SupportUrl = SupportUrl;
    }

    public String getDaemonrealm() {
        return Daemonrealm;
    }
    public void setDaemonrealm(String Daemonrealm) {
        this.Daemonrealm = Daemonrealm;
    }

    public String getScanFile() {
        return scan_file;
    }
    public void setScanFile(String scan_file) {
        this.scan_file = scan_file;
    }

    public String getConfigureCmd() {
        return configure_cmd;
    }
    public void setConfigureCmd(String configure_cmd) {
        this.configure_cmd = configure_cmd;
    }

    public String getDefaultLanguage() {
        return DefaultLanguage;
    }
    public void setDefaultLanguage(String DefaultLanguage) {
        this.DefaultLanguage = DefaultLanguage;
    }

    public String getPasscode() {
        return Passcode;
    }
    public void setPasscode(String Passcode) {
        this.Passcode = Passcode;
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
    public static final Creator<AlljoynAboutData> CREATOR = new Parcelable.Creator<AlljoynAboutData>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public AlljoynAboutData createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (AlljoynAboutData) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public AlljoynAboutData[] newArray(int size) {
            return new AlljoynAboutData[size];
        }
    };
}
