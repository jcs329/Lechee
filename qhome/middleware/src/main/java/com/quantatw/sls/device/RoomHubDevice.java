package com.quantatw.sls.device;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quantatw.sls.alljoyn.ConfigCtrlInterface;
import com.quantatw.sls.alljoyn.RoomHubAllJoynDef;
import com.quantatw.sls.alljoyn.RoomHubInterface;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.cloudapi.CloudApi;
import com.quantatw.sls.json.ExangeJson;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.SensorTypeKey;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.object.AlljoynAboutData;
import com.quantatw.sls.pack.base.BaseAlljoynResPack;
import com.quantatw.sls.pack.base.BaseReqPack;
import com.quantatw.sls.pack.base.BaseResPack;
import com.quantatw.sls.pack.device.GetAllDeviceSettingResPack;
import com.quantatw.sls.pack.device.GetIRDeviceAbilityResPack;
import com.quantatw.sls.pack.device.GetIRDeviceStatusResPack;
import com.quantatw.sls.pack.device.IRCommandReqPack;
import com.quantatw.sls.pack.device.IRDeviceScheduleReqPack;
import com.quantatw.sls.pack.device.ModifyDeviceNameReqPack;
import com.quantatw.sls.pack.device.ScanAssetReqPack;
import com.quantatw.sls.pack.homeAppliance.AddHomeApplianceReqPack;
import com.quantatw.sls.pack.homeAppliance.AddHomeApplianceResPack;
import com.quantatw.sls.pack.homeAppliance.AddIRControlDataAcReqPack;
import com.quantatw.sls.pack.homeAppliance.AddIRControlDataReqPack;
import com.quantatw.sls.pack.homeAppliance.AddScheduleBulbReqPack;
import com.quantatw.sls.pack.homeAppliance.AddScheduleBulbResPack;
import com.quantatw.sls.pack.homeAppliance.AutoSwitchWifiBridgeReqPack;
import com.quantatw.sls.pack.homeAppliance.AutoSwitchWifiBridgeResPack;
import com.quantatw.sls.pack.homeAppliance.BaseHomeApplianceResPack;
import com.quantatw.sls.pack.homeAppliance.BaseScheduleBulbResPack;
import com.quantatw.sls.pack.homeAppliance.CheckIrDataReqPack;
import com.quantatw.sls.pack.homeAppliance.CheckIrDataResPack;
import com.quantatw.sls.pack.homeAppliance.CleanIRControlDataReqPack;
import com.quantatw.sls.pack.homeAppliance.CleanIRControlDataResPack;
import com.quantatw.sls.pack.homeAppliance.CommandAcReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandBulbReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandRemoteControlReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandResPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitAcResPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitRemoteControlResPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitReqPack;
import com.quantatw.sls.pack.homeAppliance.GetAcAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.GetAirPurifierAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.GetAllScheduleBulbResPack;
import com.quantatw.sls.pack.homeAppliance.CommonReqPack;
import com.quantatw.sls.pack.homeAppliance.GetAssetProfileResPack;
import com.quantatw.sls.pack.homeAppliance.GetBulbAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.GetDeviceInfoResPack;
import com.quantatw.sls.pack.homeAppliance.GetFanAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.GetHomeApplianceAllAssetsResPack;
import com.quantatw.sls.pack.homeAppliance.GetPMAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.GetTVAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.ModifyScheduleBulbReqPack;
import com.quantatw.sls.pack.homeAppliance.ModifyScheduleBulbResPack;
import com.quantatw.sls.pack.homeAppliance.OnboardingReqPack;
import com.quantatw.sls.pack.homeAppliance.OnboardingResPack;
import com.quantatw.sls.pack.homeAppliance.RemoveHomeApplianceReqPack;
import com.quantatw.sls.pack.homeAppliance.RemoveHomeApplianceResPack;
import com.quantatw.sls.pack.homeAppliance.RemoveScheduleBulbReqPack;
import com.quantatw.sls.pack.homeAppliance.ScheduleReqPack;
import com.quantatw.sls.pack.homeAppliance.SetAssetInfoReqPack;
import com.quantatw.sls.pack.homeAppliance.SetAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.SetAssetProfileReqPack;
import com.quantatw.sls.pack.homeAppliance.SetFailRecoverReqPack;
import com.quantatw.sls.pack.homeAppliance.UpgradeReqPack;
import com.quantatw.sls.pack.roomhub.sensor.SensorDataResPack;

import org.alljoyn.bus.BusException;

import java.util.ArrayList;
import java.util.Calendar;

public class RoomHubDevice extends AlljoynDevice {

    /**
     *
     */
    private static final long serialVersionUID = 6503384330508979731L;
    private RoomHubInterface mRoomHubInterface;
    // TODO: Add Onboarding & Configuration interface
    private ConfigCtrlInterface mConfigCtrlInterface;

    private AlljoynAboutData mAlljoynAboutData;
    private static Gson gson = null;

    public RoomHubInterface getRoomHubInterface() {
        return mRoomHubInterface;
    }
    public void setRoomHubInterface(RoomHubInterface mRoomHubInterface) {
        this.mRoomHubInterface = mRoomHubInterface;
    }

    public ConfigCtrlInterface getConfigCtrlInterface() {
        return mConfigCtrlInterface;
    }
    public void setConfigCtrlInterface(ConfigCtrlInterface mConfigCtrlInterface) {
        this.mConfigCtrlInterface = mConfigCtrlInterface;
    }

    public AlljoynAboutData getAboutData() {
        return mAlljoynAboutData;
    }
    public void setAboutData(AlljoynAboutData mAlljoynAboutData) {
        this.mAlljoynAboutData = mAlljoynAboutData;
    }

    public static RoomHubDevice createDevice(CloudDevice extraInfo, SourceType sourceType) {
        RoomHubDevice device = new RoomHubDevice();
        if(extraInfo != null) {
            device.setExtraInfo(extraInfo);
            // lookup for app category and type:
            DeviceTypeConvertApi.AppDeviceCategoryType appDeviceCategoryType =
                    DeviceTypeConvertApi.ConvertType_CloudToApp(extraInfo.getDeviceType());
            device.setCategory(appDeviceCategoryType.getCategory());
            device.setMappingType(appDeviceCategoryType.getType());
        }
        device.setSourceType(sourceType);

        return device;
    }

    private static com.google.gson.Gson getGsonInstance() {
        if (gson == null) {
            gson = new GsonBuilder().create();
        }

        return gson;
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
    public static final Creator<RoomHubDevice> CREATOR = new Parcelable.Creator<RoomHubDevice>() {

        /**
         * Read the serialized concrete strategy from the parcel.
         *
         * @param in
         *            The parcel to read from
         * @return An AbstractStrategy
         */
        public RoomHubDevice createFromParcel(Parcel in) {
            // Read serialized concrete strategy from parcel
            return (RoomHubDevice) in.readSerializable();
        }

        /**
         * Required by Creator
         */
        public RoomHubDevice[] newArray(int size) {
            return new RoomHubDevice[size];
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
        RoomHubDevice other = (RoomHubDevice) obj;

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


    public RoomHubDevice clone()  {
        // TODO Auto-generated method stub

        RoomHubDevice dev = new RoomHubDevice();
        dev.setName(name);

        dev.setSessionid(sessionid);

        dev.setUuid(uuid);
        dev.setExtraInfo(extraInfo);
        dev.setSourceType(sourceType);

        return dev;
    }



    // Method

    /**
     *
     * AC Command
     *
     * @param values
     *          i: Power on/off. value: 0: off 1:on.
     *          i: Set the temperature. value: The temperature we desire.
     *          i: Function mode. value: 0: Auto 1: Cool 2: Dry 3: Fan 4: Heat.
     *          i: Swing. value: none.
     *          i: Fan. value: 0: Auto 1:High 2:Low 3: Soft.
     *          i: Timer on. value: Hour.
     *          i: Timer off. value: Hour.
     * @return         {@code 0}:: success; {@code -1}: error.
     */
    public int command(RoomHubInterface.Command_Values_iiiiiii values) {
        int retval = -1;

        if(mRoomHubInterface != null)
        {
            try {
                retval = mRoomHubInterface.command(values);
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // Cloud:
            IRCommandReqPack irCommandReqPack = new IRCommandReqPack();
            irCommandReqPack.setPower(values.powerStatus);
            irCommandReqPack.setTemp(values.temperature);
            irCommandReqPack.setMode(values.functionMode);
            irCommandReqPack.setSwing(values.swing);
            irCommandReqPack.setFan(values.fan);
            irCommandReqPack.setTimeOn(values.timerOn);
            irCommandReqPack.setTimeOff(values.timerOff);
            irCommandReqPack.setUserId(values.userId);

            BaseResPack resPack = CloudApi.getInstance().SendIRCommand(uuid, irCommandReqPack);
            retval = resPack.getStatus_code();
        }

        return retval;
    }

    @Deprecated
    public int ledControl(int color, boolean on) {
        // TODO: Use new ledControl(int color, boolean on, boolean controlType, int enableMsec, int disableMsec, int loopNumber)
        return ledControl(color, (on == false ? 0 : 1) , 0, 0, 0);
    }

    /**
     *
     * Control LED
     *
     * @param color    v0.0.6 {@code 0}: red; {@code 1}: green; {@code 2}: blue
     *                 v0.0.7 {@code 0}: dark; {@code 1}: red; {@code 2}: green; {@code 3}: blue;
     *                        {@code 4}: purple; {@code 5}: yellow; {@code 6}: sky; {@code 7}: white;
     * @param        {@code true}: turn on led; {@code false}: turn off led.
     * @return         {@code 0}:: success; {@code -1}: error.
     */
    public int ledControl(int color, int controlType, int enableMsec, int disableMsec, int loopNumber) {
        int retval = ErrorKey.ROOMHUB_LED_CONTROL_FAILURE;

        if(mRoomHubInterface != null)
        {
            try {
                retval = mRoomHubInterface.ledControl(color, controlType, enableMsec, disableMsec, loopNumber);
                if(retval < 0)
                    retval=ErrorKey.ROOMHUB_LED_CONTROL_FAILURE;
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            retval=ErrorKey.CLOUDAPI_NOT_ALLOWED;
            // TODO: Cloud
        }

        return retval;
    }

    /**
     *
     * StartWPS
     *
     * @return {@code 0}: success; {@code -1}: error
     */
    public int startWPS() {
        int retval = -1;

        if(mRoomHubInterface != null)
        {
            try {
                retval = mRoomHubInterface.startWPS();
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Cloud
        }

        return retval;
    }

    /**
     *
     * IR learning, call this method will triggered the signal "learningResult"
     */
    @Deprecated
    public void learning() {
        if(mRoomHubInterface != null)
        {
            try {
                mRoomHubInterface.learning();
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Cloud
        }
    }

    /**
     * User confirm the result data. Call this method the device will send IR data to AC.
     *
     * @param irData
     * @return  @return {@code 0}: success; {@code -1}: error
     */
    public int checkIRData(RoomHubInterface.irData_y[] irData) {
        int retval = ErrorKey.IR_CHECK_DATA_FAILURE;

        if(mRoomHubInterface != null)
        {
            try {
                retval = mRoomHubInterface.checkIRData(irData);
                if(retval < 0)
                    retval = ErrorKey.IR_CHECK_DATA_FAILURE;
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            retval=ErrorKey.CLOUDAPI_NOT_ALLOWED;
            // TODO: Cloud
        }

        return retval;
    }

    /**
     * Clean the ir control data that stored on device.
     */
    public void cleanIRControlData() {
        if(mRoomHubInterface != null)
        {
            try {
                mRoomHubInterface.cleanIRControlData();
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Cloud
        }
    }

    /**
     *
     * Add IR control datas to device.
     *
     * @param controlData
     * @return  @return {@code 0}: success; {@code -1}: error
     */
    public int addIRControlData(RoomHubInterface.controlData_ississa_y controlData) {
        int retval = -1;

        if(mRoomHubInterface != null)
        {
            try {
                retval = mRoomHubInterface.addIRControlData(controlData);
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Cloud
        }

        return retval;
    }

    /**
     *
     * Add Schedule
     *
     * @param schedule
     *          i: mode type
     *          i: value
     *          s: startTime
     *          s: endTime
     *          a(i): weekday  ex:{2,3,4}
     * @return schedule index
     */
    public int addSchedule(RoomHubInterface.AddSchedule_Schedule schedule) {
        int scheduleIndex = ErrorKey.AC_ADD_SCHEDULE_FAILURE;

        if(mRoomHubInterface != null)
        {
            try {
                scheduleIndex = mRoomHubInterface.addSchedule(schedule);
                if(scheduleIndex < 0)
                    scheduleIndex = ErrorKey.AC_ADD_SCHEDULE_FAILURE;
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Cloud

            IRDeviceScheduleReqPack irDeviceScheduleReqPack = new IRDeviceScheduleReqPack();
            irDeviceScheduleReqPack.setAction(0);
            irDeviceScheduleReqPack.setIndex(0);
            irDeviceScheduleReqPack.setMode(schedule.modeType);
            irDeviceScheduleReqPack.setValue(schedule.value);
            irDeviceScheduleReqPack.setStartTime(schedule.startTime);
            irDeviceScheduleReqPack.setEndTime(schedule.endTime);
            irDeviceScheduleReqPack.setRepeat(schedule.repeat == true ? 1 : 0);
            irDeviceScheduleReqPack.setState(schedule.state == true ? 1 : 0);
            int[] weekday = new int[schedule.weekday.length];
            for(int i=0;i<schedule.weekday.length;i++) {
                weekday[i] = schedule.weekday[i].day;
            }
            irDeviceScheduleReqPack.setWeekday(weekday);
            BaseResPack resPack=CloudApi.getInstance().SendIRDeviceSchedule(uuid, irDeviceScheduleReqPack);
            scheduleIndex=resPack.getStatus_code();
        }

        return scheduleIndex;
    }

    /**
     * Modify Schedule
     *
     * @param schedule
     * @param index
     * @return
     */
    public int modifySchedule(RoomHubInterface.AddSchedule_Schedule schedule, int index) {
        int retval = ErrorKey.AC_MODIFY_SCHEDULE_FAILURE;

        if(mRoomHubInterface != null)
        {
            try {
                boolean ret = mRoomHubInterface.modifySchedule(schedule, index);
                if(ret)
                    retval=ErrorKey.Success;
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Cloud
            IRDeviceScheduleReqPack irDeviceScheduleReqPack = new IRDeviceScheduleReqPack();
            irDeviceScheduleReqPack.setAction(1);
            irDeviceScheduleReqPack.setIndex(index);
            irDeviceScheduleReqPack.setMode(schedule.modeType);
            irDeviceScheduleReqPack.setValue(schedule.value);
            irDeviceScheduleReqPack.setStartTime(schedule.startTime);
            irDeviceScheduleReqPack.setEndTime(schedule.endTime);
            irDeviceScheduleReqPack.setRepeat(schedule.repeat == true ? 1 : 0);
            irDeviceScheduleReqPack.setState(schedule.state == true ? 1 : 0);
            int[] weekday = new int[schedule.weekday.length];
            for(int i=0;i<schedule.weekday.length;i++) {
                weekday[i] = schedule.weekday[i].day;
            }
            irDeviceScheduleReqPack.setWeekday(weekday);
            BaseResPack resPack=CloudApi.getInstance().SendIRDeviceSchedule(uuid, irDeviceScheduleReqPack);
            retval=resPack.getStatus_code();
        }

        return retval;
    }

    /**
     *
     *  Get All Schedule
     *
     * @return
     *    i: index
     *    i: mode type
     *    i: value
     *    s: startTime
     *    s: endTime
     *    b: repeat
     *    b: state
     *    a(i): weekday  ex:{2,3,4}
     *
     */
/*
    public RoomHubInterface.GetAllSchedule_Schedules[] getAllSchedule() {
        RoomHubInterface.GetAllSchedule_Schedules[] allSchedule = null;
        if(mRoomHubInterface != null)
        {
            try {
                allSchedule = mRoomHubInterface.getAllSchedule();
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Cloud
            GetAllDeviceSettingResPack resPack = CloudApi.getInstance().GetDeviceSetting(uuid);
            if(resPack.getStatus_code() == 0) {
                if(resPack.getScheduleList() != null) {
                    ArrayList<Schedule> scheduleLists = resPack.getScheduleList();
                    allSchedule = new RoomHubInterface.GetAllSchedule_Schedules[scheduleLists.size()];
                    for (int i = 0; i < scheduleLists.size(); i++) {
                        Schedule schedule = scheduleLists.get(i);
                        allSchedule[i] = new RoomHubInterface.GetAllSchedule_Schedules();
                        allSchedule[i].index = schedule.getIndex();
                        allSchedule[i].modeType = schedule.getType();
                        allSchedule[i].value = schedule.getValue();
                        allSchedule[i].startTime = schedule.getStartTime();
                        allSchedule[i].endTime = schedule.getEndTime();
                        allSchedule[i].repeat = schedule.getRepeat();
                        allSchedule[i].state = schedule.getEnable();
                        RoomHubInterface.weekDay_i[] week = new RoomHubInterface.weekDay_i[schedule.getWeek().length];
                        for (int j = 0; j < week.length; j++) {
                            week[j] = new RoomHubInterface.weekDay_i();
                            week[j].day = schedule.getWeek()[j];
                        }
                        allSchedule[i].weekday = week;
                    }
                }
            }

        }

        return allSchedule;
    }
*/
    public ArrayList<Schedule> getAllSchedule() {
        ArrayList<Schedule> scheduleLists = null;

        if(mRoomHubInterface != null)
        {
            try {
                RoomHubInterface.GetAllSchedule_Schedules[] allSchedule = mRoomHubInterface.getAllSchedule();
                if(allSchedule != null) {
                    scheduleLists=new ArrayList<Schedule>();
                    for(int i = 0; i< allSchedule.length; i++) {
                        Schedule schedule = new Schedule();
                        schedule.setIndex(allSchedule[i].index);
                        schedule.setType(allSchedule[i].modeType);
                        schedule.setValue(allSchedule[i].value);
                        schedule.setRepeat(allSchedule[i].repeat);
                        schedule.setEnable(allSchedule[i].state);
                        schedule.setStartTime(allSchedule[i].startTime);
                        schedule.setEndTime(allSchedule[i].endTime);
                        int[] week = new int[allSchedule[i].weekday.length];
                        for (int j = 0; j < week.length; j++) {
                            week[j] = allSchedule[i].weekday[j].day;
                        }
                        schedule.setWeek(week);

                        scheduleLists.add(schedule);
                    }
                }
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Cloud
            GetAllDeviceSettingResPack resPack = CloudApi.getInstance().GetDeviceSetting(uuid);
            if(resPack.getStatus_code() == 0) {
                if(resPack.getScheduleList() != null) {
                    scheduleLists = resPack.getScheduleList();
                }
            }

        }

        return scheduleLists;
    }
    /**
     *
     * Remove Schedule
     *
     * @param index Schedule Index
     * @return {@code true}: success; {@code false}: error.
     */
    public int removeSchedule(int index) {
        int retval = ErrorKey.AC_REMOVE_SCHEDULE_FAILURE;

        if(mRoomHubInterface != null)
        {
            try {
                boolean ret = mRoomHubInterface.removeSchedule(index);
                if(ret)
                    retval=ErrorKey.Success;
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Cloud
            IRDeviceScheduleReqPack irDeviceScheduleReqPack = new IRDeviceScheduleReqPack();
            irDeviceScheduleReqPack.setAction(2);
            irDeviceScheduleReqPack.setIndex(index);

            BaseResPack resPack=CloudApi.getInstance().SendIRDeviceSchedule(uuid, irDeviceScheduleReqPack);
            retval=resPack.getStatus_code();
        }

        return retval;
    }

    /**
     *
     * Remove All Schedule
     *
     * @return {@code true}: success; {@code false}: error.
     */
    public int removeAllSchedule() {
        int retval = ErrorKey.AC_REMOVE_ALL_SCHEDULE_FAILURE;

        if(mRoomHubInterface != null)
        {
            try {
                boolean ret = mRoomHubInterface.removeAllSchedule();
                if(ret)
                    retval=ErrorKey.Success;
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Cloud
        }

        return retval;

    }

    /**
     *
     * Get Device Info
     *
     * @return
     *    i: Power on/off. value: 0: off 1:on.
     *    i: Set the temperature. value: The temperature we desire.
     *    i: Function mode. value: 0: Auto 1: Cool 2: Dry 3: Fan 4: Heat.
     *    i: Swing. value: none.
     *    i: Fan. value: 0: Auto 1:High 2:Low 3: Soft.
     *    i: Timer on. value: Hour.
     *    i: Timer off. value: Hour.
     *    s: Brand name.
     *    s: Device model.
     *    s: Town id
     */
    @Deprecated
    public RoomHubInterface.getDeviceInfo_return_values_iiiiiiiss getDeviceInfo() {
        RoomHubInterface.getDeviceInfo_return_values_iiiiiiiss deviceInfo = null;

        if(mRoomHubInterface != null)
        {
            try {
                deviceInfo = mRoomHubInterface.getDeviceInfo();
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // Cloud
            GetIRDeviceStatusResPack irDeviceStatusResPack = CloudApi.getInstance().GetIRDeviceStatus(uuid);
            if(irDeviceStatusResPack.getStatus_code() == 0) {
                IRDeviceStatus irDeviceStatus = irDeviceStatusResPack.getData();
                deviceInfo = new RoomHubInterface.getDeviceInfo_return_values_iiiiiiiss();
                deviceInfo.powerOnOff = irDeviceStatus.getPower();
                deviceInfo.temperature = irDeviceStatus.getTemp();
                deviceInfo.functionMode = irDeviceStatus.getMode();
                deviceInfo.swing = irDeviceStatus.getSwing();
                deviceInfo.fan = irDeviceStatus.getFan();
                deviceInfo.timerOn = irDeviceStatus.getTimeOn();
                deviceInfo.timerOff = irDeviceStatus.getTimeOff();
                deviceInfo.brandName = irDeviceStatus.getBrand();
                deviceInfo.deviceModel = irDeviceStatus.getDevice();
                deviceInfo.townId = irDeviceStatus.getTownId();
            }
        }

        return deviceInfo;
    }

    /**
     *
     * Set Device Info
     *
     * @param deviceInfo
     *                   s: Brand name.
     *                   s: Device model.
     * @return
     */
    public boolean setDeviceInfo(RoomHubInterface.setDeviceInfo_values_ss deviceInfo) {
        boolean retval = false;
        if(mRoomHubInterface != null)
        {
            try {
                retval = mRoomHubInterface.setDeviceInfo(deviceInfo);
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Cloud
        }

        return retval;
    }

    /**
     *
     * Get Device ability Limite
     *
     * @return AbilityLimite[]
     *         mode:
     *                 -1: none module.
     *                  0: auto.
     *                  1: cool.
     *                  2: dry.
     *                  3: fan.
     *                  4: heat
     *          maxValue
     *          minValue
     */
    public RoomHubInterface.AbilityLimit[] getAbilityLimit() {
        RoomHubInterface.AbilityLimit[] deviceAbility = null;
        if(mRoomHubInterface != null)
        {
            try {
                deviceAbility = mRoomHubInterface.getAbilityLimit();
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // Cloud
            GetIRDeviceAbilityResPack resPack = CloudApi.getInstance().GetIRDeviceAbility(uuid);
            if(resPack.getStatus_code() == 0) {
                IRDeviceAbilityData irDeviceAbilityData = resPack.getData();
                if(irDeviceAbilityData != null) {
                    ArrayList<IRDeviceAbility> abilities = irDeviceAbilityData.getAbilitys();
                    if(abilities != null && abilities.size() > 0) {
                        deviceAbility = new RoomHubInterface.AbilityLimit[abilities.size()];
                        for(int i=0;i<deviceAbility.length;i++) {
                            IRDeviceAbility irDeviceAbility = abilities.get(i);
                            deviceAbility[i] = new RoomHubInterface.AbilityLimit();
                            deviceAbility[i].mode = irDeviceAbility.getMode();
                            deviceAbility[i].maxValue = irDeviceAbility.getMaxValue();
                            deviceAbility[i].minValue = irDeviceAbility.getMinValue();
                        }
                    }
                }
            }
        }

        return deviceAbility;
    }

    /**
     *
     * 設定IR發射強度用
     *
     * @param strength
     * @return
     */
    public boolean setIRTx(int strength) {
        boolean retval = false;

        if(mRoomHubInterface != null)
        {
            try {
                retval = mRoomHubInterface.setIRTx(strength);
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Cloud
        }

        return retval;
    }

    // Property
    public String getCurrentName() {
        String name = "";
        if(mRoomHubInterface != null)
        {
            try {
                name = mRoomHubInterface.getname();

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            name = this.name;
        }


        return name;
    }
    public int modifyDeviceName(String name) {
        int modifyResult = ErrorKey.ROOMHUB_RENAME_FAILURE;

        if(mRoomHubInterface != null) {
            try {
                mRoomHubInterface.setname(name);
                // TODO: setname() didn't return any status
                modifyResult = ErrorKey.Success;
                this.setName(name);
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            ModifyDeviceNameReqPack req = new ModifyDeviceNameReqPack();
            req.setDeviceName(name);
            BaseResPack res = CloudApi.getInstance().ModifyDeviceNameREQ(this.getUuid(),req);
            modifyResult = res.getStatus_code();
            if(modifyResult >= ErrorKey.Success) {
                this.setName(name);
            }
        }

        return modifyResult;
    }
    public String getuuid() {
        String uuid = "";

        if(mRoomHubInterface != null)
        {
            try {
                uuid = mRoomHubInterface.getuuid();

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            uuid = this.uuid;
        }

        return uuid;
    }
    public int gettemperature() {
        int temperature = ErrorKey.SENSOR_TEMPERATURE_INVALID;

        if(mRoomHubInterface != null)
        {
            try {
                temperature = mRoomHubInterface.gettemperature();
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            if(CloudApi.getInstance() != null) {
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH)+1;
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                String today = String.valueOf(mMonth) + "-" + String.valueOf(mDay);
                SensorDataResPack resPack = CloudApi.getInstance().GetSensorDailyData(this.getUuid(), SensorTypeKey.SENSOR_TEMPERATURE, today);
                if (resPack != null && resPack.getStatus_code() == ErrorKey.Success) {
                    temperature = (int) resPack.getNowValue();
                }
            }
        }

        if(temperature < -900)
            temperature = ErrorKey.SENSOR_TEMPERATURE_INVALID;

        return temperature;
    }
    public int gethumidity() {
        int humidity = ErrorKey.SENSOR_HUMIDITY_INVALID;

        if(mRoomHubInterface != null)
        {
            try {
                humidity = mRoomHubInterface.gethumidity();
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            if(CloudApi.getInstance() != null) {
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH)+1;
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                String today = String.valueOf(mYear) + "-" + String.valueOf(mMonth) + "-" + String.valueOf(mDay);
                SensorDataResPack resPack = CloudApi.getInstance().GetSensorDailyData(this.getUuid(), SensorTypeKey.SENSOR_HUMIDITY, today);
                if (resPack != null && resPack.getStatus_code() == ErrorKey.Success) {
                    humidity = (int) resPack.getNowValue();
                }
            }
        }
        if(humidity < -900)
            humidity = ErrorKey.SENSOR_HUMIDITY_INVALID;

        return humidity;
    }
    public String getbluetoothUUID() {
        String btMacAddr = "";
        if(mRoomHubInterface != null)
        {
            try {
                btMacAddr = mRoomHubInterface.getbluetoothUUID();

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: Get humidity from Cloud
        }

        return btMacAddr;
    }

    public int getbluetoothMajor() {
        int btMajor = -1;

        if(mRoomHubInterface != null)
        {
            try {
                btMajor = mRoomHubInterface.getbluetoothMajor();

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: Get btMajor from Cloud
        }

        return btMajor;
    }

    public int getbluetoothMinor() {
        int btMinor = -1;

        if(mRoomHubInterface != null)
        {
            try {
                btMinor = mRoomHubInterface.getbluetoothMinor();

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: getbluetoothMinor through CloudApi
        }

        return btMinor;
    }

    public String getOwnerId() {
        String ownerId = null;

        if(mRoomHubInterface != null)
        {
            try {
                ownerId = mRoomHubInterface.getownerId();

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: getOwnerId through CloudApi
        }

        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        if(mRoomHubInterface != null)
        {
            try {
                mRoomHubInterface.setownerId(ownerId);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: SetOwnerId through CloudApi
        }
    }

    public String getSSID() {
        String ssid = null;

        if(mRoomHubInterface != null)
        {
            try {
                ssid = mRoomHubInterface.getSSID();

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: getOwnerId through CloudApi
        }

        return ssid;
    }
    public void setSSID(String ssid) {
        if(mRoomHubInterface != null)
        {
            try {
                mRoomHubInterface.setSSID(ssid);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: SetOwnerId through CloudApi
        }
    }

    public int getTime() {
        int timeStamp = -1;

        if(mRoomHubInterface != null)
        {
            try {
                timeStamp = mRoomHubInterface.getTime();

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: getOwnerId through CloudApi
        }

        return timeStamp;
    }
    public void setTime(int timeStamp) {
        if(mRoomHubInterface != null)
        {
            try {
                mRoomHubInterface.setTime(timeStamp);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: SetOwnerId through CloudApi
        }
    }

    public RoomHubInterface.AcOnOffStatus getACOnOffStatus() {
         RoomHubInterface.AcOnOffStatus status = null;

        if(mRoomHubInterface != null)
        {
            try {
                status = mRoomHubInterface.getACOnOffStatus();

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: getOwnerId through CloudApi
        }

        return status;
    }

    public boolean setCloudServerAddress(String address) {
        boolean ret = false;

        if(mRoomHubInterface != null)
        {
            try {
                ret = mRoomHubInterface.setCloudServerAddress(address);

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: getOwnerId through CloudApi
        }

        return ret;
    }

    public boolean sphygmometerCommand(boolean connect) {
        boolean ret = false;

        if(mRoomHubInterface != null)
        {
            try {
                ret = mRoomHubInterface.sphygmometerCommand(connect);

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO:
        }

        return ret;
    }

    public boolean startUpgrade(String imageURL) {
        boolean ret = false;

        if(mRoomHubInterface != null)
        {
            try {
                ret = mRoomHubInterface.startUpgrade(imageURL);

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: startUpgrade through CloudApi
        }

        return ret;
    }

    public RoomHubInterface.NextSchedule getNextSchedule() {
        RoomHubInterface.NextSchedule ret = null;

        if(mRoomHubInterface != null)
        {
            try {
                ret = mRoomHubInterface.getNextSchedule();

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: getNextSchedule through CloudApi
        }

        return ret;
    }

    // type: 0, command - assetType = 0
    public CommandResPack commandAc(CommandAcReqPack reqPack) {
        CommandResPack resPack = new CommandResPack();
        resPack.setStatus_code(ErrorKey.AC_COMMAND_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_COMMAND, getGsonInstance().toJson(reqPack));

                ExangeJson<CommandResPack> mExangeJson = new ExangeJson<CommandResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                int result=resPack.getResult();
                if(result == -7)
                    resPack.setStatus_code(ErrorKey.FUNCTION_NOT_SUPPORTED);
                else if(result < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.AC_COMMAND_FAILURE);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                resPack = CloudApi.getInstance().SendDeviceAssetsIRCommand_Ac(uuid, reqPack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resPack;
    }

    // type: 0, command - assetType = 5 (Bulb)
    public CommandResPack commandBulb(CommandBulbReqPack reqPack) {
        CommandResPack resPack = new CommandResPack();;
        resPack.setStatus_code(ErrorKey.BULB_COMMAND_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_COMMAND, getGsonInstance().toJson(reqPack));

                ExangeJson<CommandResPack> mExangeJson = new ExangeJson<CommandResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                int result=resPack.getResult();
                if(result == -7)
                    resPack.setStatus_code(ErrorKey.FUNCTION_NOT_SUPPORTED);
                else if(result < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.BULB_COMMAND_FAILURE);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            //TODO Not check
            try {
                resPack = CloudApi.getInstance().SendDeviceAssetsIRCommand_Bulb(uuid, reqPack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resPack;
    }

    // type: 0, command - assetType = 1 (FAN),3 (PM2.5),4 (Air purifier),6 (TV)
    public CommandResPack commandRemoteControl(CommandRemoteControlReqPack reqPack) {
        CommandResPack resPack = new CommandResPack();;
        int error_code = 0;
        if(reqPack.getAssetType() == RoomHubAllJoynDef.assetType.ASSET_TYPE_AC)
            error_code=ErrorKey.AC_COMMAND_FAILURE;
        else if(reqPack.getAssetType() == RoomHubAllJoynDef.assetType.ASSET_TYPE_FAN)
            error_code=ErrorKey.FAN_COMMAND_FAILURE;
        else if(reqPack.getAssetType() == RoomHubAllJoynDef.assetType.ASSET_TYPE_PM25)
            error_code=ErrorKey.PM_COMMAND_FAILURE;
        else if(reqPack.getAssetType() == RoomHubAllJoynDef.assetType.ASSET_TYPE_AIRPURIFIER)
            error_code=ErrorKey.AP_COMMAND_FAILURE;
        else if(reqPack.getAssetType() == RoomHubAllJoynDef.assetType.ASSET_TYPE_TV)
            error_code=ErrorKey.TV_COMMAND_FAILURE;

        resPack.setStatus_code(error_code);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_COMMAND, getGsonInstance().toJson(reqPack));

                ExangeJson<CommandResPack> mExangeJson = new ExangeJson<CommandResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                int result=resPack.getResult();
                if(result == -7)
                    resPack.setStatus_code(ErrorKey.FUNCTION_NOT_SUPPORTED);
                else if(result < ErrorKey.Success)
                    resPack.setStatus_code(error_code);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                resPack = CloudApi.getInstance().SendDeviceAssetsIRCommand_RemoteControl(uuid, reqPack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resPack;
    }
    // type: 1
    // type: 2

    // type: 3, learning
    public void learningV1() {
        BaseResPack resPack = null;

        if(mRoomHubInterface != null) {
            try {
                BaseReqPack reqPack = new BaseReqPack();
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_LEARNING, getGsonInstance().toJson(reqPack));

                ExangeJson<BaseAlljoynResPack> mExangeJson = new ExangeJson<BaseAlljoynResPack>();
                BaseAlljoynResPack ajResPack = new BaseAlljoynResPack();

                resPack = (BaseResPack) mExangeJson.Exe(ret, ajResPack);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
        }

        return;
    }

    // type: 4, checkIRData
    public CheckIrDataResPack checkIRData(CheckIrDataReqPack reqPack) {
        CheckIrDataResPack resPack = null;

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_CHECK_IR_DATA, getGsonInstance().toJson(reqPack));

                ExangeJson<CheckIrDataResPack> mExangeJson = new ExangeJson<CheckIrDataResPack>();
                resPack = new CheckIrDataResPack();

                resPack = mExangeJson.Exe(ret, resPack);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
        }
        return resPack;
    }

    // type: 5, cleanIRControlData
    public CleanIRControlDataResPack cleanIRControlData(CleanIRControlDataReqPack reqPack) {
        CleanIRControlDataResPack resPack = null;

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_CLEAN_IR_CONTROL_DATA, getGsonInstance().toJson(reqPack));

                ExangeJson<CleanIRControlDataResPack> mExangeJson = new ExangeJson<CleanIRControlDataResPack>();
                resPack = new CleanIRControlDataResPack();

                resPack = mExangeJson.Exe(ret, resPack);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
        }
        return resPack;

    }

    // type: 6, addIRControlData
    /* AC */
    public BaseHomeApplianceResPack addIRControlDataAc(AddIRControlDataAcReqPack reqPack) {
        BaseHomeApplianceResPack resPack = new BaseHomeApplianceResPack();;
        resPack.setStatus_code(ErrorKey.IR_ADD_CONTROL_DATA_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_ADD_IR_CONTROL_DATA, getGsonInstance().toJson(reqPack));

                ExangeJson<BaseHomeApplianceResPack> mExangeJson = new ExangeJson<BaseHomeApplianceResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.IR_ADD_CONTROL_DATA_FAILURE);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
            resPack.setStatus_code(ErrorKey.CLOUDAPI_NOT_ALLOWED);
        }

        return resPack;
    }
    /* FAN, Air Purifier,TV*/
    public BaseHomeApplianceResPack addIRControlData(AddIRControlDataReqPack reqPack) {
        BaseHomeApplianceResPack resPack = new BaseHomeApplianceResPack();
        resPack.setStatus_code(ErrorKey.IR_ADD_CONTROL_DATA_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_ADD_IR_CONTROL_DATA, getGsonInstance().toJson(reqPack));

                ExangeJson<BaseHomeApplianceResPack> mExangeJson = new ExangeJson<BaseHomeApplianceResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.IR_ADD_CONTROL_DATA_FAILURE);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
            resPack.setStatus_code(ErrorKey.CLOUDAPI_NOT_ALLOWED);
        }

        return resPack;
    }

    // type: 7
    public AddScheduleBulbResPack addScheduleBulb(AddScheduleBulbReqPack reqPack){
        AddScheduleBulbResPack resPack = new AddScheduleBulbResPack();
        resPack.setStatus_code(ErrorKey.BULB_ADD_SCHEDULE_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_ADD_SCHEDULE, getGsonInstance().toJson(reqPack));

                ExangeJson<AddScheduleBulbResPack> mExangeJson = new ExangeJson<AddScheduleBulbResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.BULB_ADD_SCHEDULE_FAILURE);
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            try {
                CommandResPack res = CloudApi.getInstance().ModifySchedule_Bulb(uuid, new ScheduleReqPack(reqPack));
                resPack.setStatus_code(res.getStatus_code());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resPack;
    }
    // type: 8
    public ModifyScheduleBulbResPack modifyScheduleBulb(ModifyScheduleBulbReqPack reqPack){
        ModifyScheduleBulbResPack resPack = new ModifyScheduleBulbResPack();
        resPack.setStatus_code(ErrorKey.BULB_MODIFY_SCHEDULE_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_MODIFY_SCHEDULE, getGsonInstance().toJson(reqPack));

                ExangeJson<ModifyScheduleBulbResPack> mExangeJson = new ExangeJson<ModifyScheduleBulbResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.BULB_MODIFY_SCHEDULE_FAILURE);
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            try {
                CommandResPack res = CloudApi.getInstance().ModifySchedule_Bulb(uuid, new ScheduleReqPack(reqPack));
                resPack.setStatus_code(res.getStatus_code());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resPack;
    }
    // type: 9
    public GetAllScheduleBulbResPack getAllScheduleBulb(CommonReqPack reqPack){
        GetAllScheduleBulbResPack resPack = new GetAllScheduleBulbResPack();
        resPack.setStatus_code(ErrorKey.BULB_GET_ALL_SCHEDULE_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_GET_ALL_SCHEDULE, getGsonInstance().toJson(reqPack));

                ExangeJson<GetAllScheduleBulbResPack> mExangeJson = new ExangeJson<GetAllScheduleBulbResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.BULB_GET_ALL_SCHEDULE_FAILURE);
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
            resPack.setStatus_code(ErrorKey.CLOUDAPI_NOT_ALLOWED);
        }
        return resPack;
    }
    // type: 10
    public BaseScheduleBulbResPack removeScheduleBulb(RemoveScheduleBulbReqPack reqPack){
        BaseScheduleBulbResPack resPack = new BaseScheduleBulbResPack();
        resPack.setStatus_code(ErrorKey.BULB_REMOVE_SCHEDULE_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_REMOVE_SCHEDULE, getGsonInstance().toJson(reqPack));

                ExangeJson<BaseScheduleBulbResPack> mExangeJson = new ExangeJson<BaseScheduleBulbResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.BULB_REMOVE_SCHEDULE_FAILURE);
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            try {
                CommandResPack res = CloudApi.getInstance().ModifySchedule_Bulb(uuid, new ScheduleReqPack(reqPack));
                resPack.setStatus_code(res.getStatus_code());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resPack;
    }
    // type: 11
    public BaseScheduleBulbResPack removeAllScheduleBulb(CommonReqPack reqPack){
        BaseScheduleBulbResPack resPack = new BaseScheduleBulbResPack();
        resPack.setStatus_code(ErrorKey.BULB_REMOVE_ALL_SCHEDULE_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_REMOVE_ALL_SCHEDULE, getGsonInstance().toJson(reqPack));

                ExangeJson<BaseScheduleBulbResPack> mExangeJson = new ExangeJson<BaseScheduleBulbResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.BULB_REMOVE_ALL_SCHEDULE_FAILURE);
            } catch (BusException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
            resPack.setStatus_code(ErrorKey.CLOUDAPI_NOT_ALLOWED);
        }
        return resPack;
    }
    // type: 12, get device info - TYPE_GET_DEVICE_INFO
    public GetDeviceInfoResPack getDeviceInfoV1() {
        GetDeviceInfoResPack resPack = null;

        if(mRoomHubInterface != null) {
            try {
                BaseReqPack reqPack = new BaseReqPack();
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_GET_DEVICE_INFO, getGsonInstance().toJson(reqPack));

                ExangeJson<GetDeviceInfoResPack> mExangeJson = new ExangeJson<GetDeviceInfoResPack>();
                resPack = new GetDeviceInfoResPack();

                resPack = mExangeJson.Exe(ret, resPack);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
        }

        return resPack;
    }

    // type: 13

    // type: 14, getAssetInfo - AC
    public GetAcAssetInfoResPack getAcAssetInfo(CommonReqPack reqPack) {
        GetAcAssetInfoResPack resPack = new GetAcAssetInfoResPack();
        resPack.setStatus_code(ErrorKey.AC_ASSET_INFO_INVALID);
        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_GET_ASSET_TYPE, getGsonInstance().toJson(reqPack));

                ExangeJson<GetAcAssetInfoResPack> mExangeJson = new ExangeJson<GetAcAssetInfoResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                resPack = CloudApi.getInstance().GetAcAssetInfo(uuid, reqPack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resPack;
    }

    // type: 14, getAssetInfo - FAN
    public GetFanAssetInfoResPack getFanAssetInfo(CommonReqPack reqPack) {
        GetFanAssetInfoResPack resPack = new GetFanAssetInfoResPack();
        resPack.setStatus_code(ErrorKey.FAN_ASSET_INFO_INVALID);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_GET_ASSET_TYPE, getGsonInstance().toJson(reqPack));

                ExangeJson<GetFanAssetInfoResPack> mExangeJson = new ExangeJson<GetFanAssetInfoResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                resPack = CloudApi.getInstance().GetFanAssetInfo(uuid, reqPack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resPack;
    }

    private static byte[] intToByteArray(int a)
    {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);
        ret[2] = (byte) ((a >> 8) & 0xFF);
        ret[1] = (byte) ((a >> 16) & 0xFF);
        ret[0] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }

    // type: 14, getAssetInfo - Air Purifier
    public GetAirPurifierAssetInfoResPack getAirPurifierAssetInfo(CommonReqPack reqPack) {
        GetAirPurifierAssetInfoResPack resPack = new GetAirPurifierAssetInfoResPack();
        resPack.setStatus_code(ErrorKey.AP_ASSET_INFO_INVAILD);
        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_GET_ASSET_TYPE, getGsonInstance().toJson(reqPack));

                ExangeJson<GetAirPurifierAssetInfoResPack> mExangeJson = new ExangeJson<GetAirPurifierAssetInfoResPack>();
                resPack = mExangeJson.Exe(ret, resPack);

                {
                    int strainer = resPack.getStrainer();
                    byte[] val = intToByteArray(strainer);
                    Log.d("RoomHubDevice","val len="+val.length);
                }
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: CloudAPI
            try {
               resPack = CloudApi.getInstance().GetAirPurifierAssetInfo(uuid, reqPack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resPack;
    }

    // type: 14, getAssetInfo - PM2.5
    public GetPMAssetInfoResPack getPMAssetInfo(CommonReqPack reqPack) {
        GetPMAssetInfoResPack resPack = new GetPMAssetInfoResPack();
        resPack.setStatus_code(ErrorKey.PM_ASSET_INFO_INVAILD);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_GET_ASSET_TYPE, getGsonInstance().toJson(reqPack));

                ExangeJson<GetPMAssetInfoResPack> mExangeJson = new ExangeJson<GetPMAssetInfoResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: CloudAPI
            try {
                resPack = CloudApi.getInstance().GetPMAssetInfo(uuid, reqPack);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return resPack;
    }

    // type: 14, getAssetInfo - Bulb
    public GetBulbAssetInfoResPack getBlubAssetInfo(CommonReqPack reqPack) {
        GetBulbAssetInfoResPack resPack = new GetBulbAssetInfoResPack();
        resPack.setStatus_code(ErrorKey.BULB_ASSET_INFO_INVAILD);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_GET_ASSET_TYPE, getGsonInstance().toJson(reqPack));

                ExangeJson<GetBulbAssetInfoResPack> mExangeJson = new ExangeJson<GetBulbAssetInfoResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                resPack = CloudApi.getInstance().GetBulbAssetInfo(uuid, reqPack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resPack;
    }

    // type: 14, getAssetInfo - TV
    public GetTVAssetInfoResPack getTVAssetInfo(CommonReqPack reqPack) {
        GetTVAssetInfoResPack resPack = null;

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_GET_ASSET_TYPE, getGsonInstance().toJson(reqPack));

                ExangeJson<GetTVAssetInfoResPack> mExangeJson = new ExangeJson<GetTVAssetInfoResPack>();
                resPack = new GetTVAssetInfoResPack();

                resPack = mExangeJson.Exe(ret, resPack);
            } catch (BusException e) {
                if(resPack == null)
                    resPack = new GetTVAssetInfoResPack();
                resPack.setStatus_code(-1);
                // TODO Auto-generated catch block
            e.printStackTrace();
            }
        } else {
            try {
                resPack = CloudApi.getInstance().GetTVAssetInfo(uuid, reqPack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resPack;
    }

    // type: 15, setAssetInfo
    public SetAssetInfoResPack setAssetInfo(SetAssetInfoReqPack reqPack) {
        SetAssetInfoResPack resPack = new SetAssetInfoResPack();
        resPack.setStatus_code(ErrorKey.ASSET_INFO_NOT_SET);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_SET_ASSET_TYPE, getGsonInstance().toJson(reqPack));

                ExangeJson<SetAssetInfoResPack> mExangeJson = new ExangeJson<SetAssetInfoResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.ASSET_INFO_NOT_SET);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
        }

        return resPack;
    }

    // type: 16, getAbilityLimit -  - assetType = 0 (AC)
    public GetAbilityLimitAcResPack getAcAbilityLimit(GetAbilityLimitReqPack reqPack) {
        GetAbilityLimitAcResPack resPack = new GetAbilityLimitAcResPack();
        resPack.setStatus_code(ErrorKey.AC_ABILITY_INVALID);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_GET_ABILITY_LIMIT, getGsonInstance().toJson(reqPack));

                ExangeJson<GetAbilityLimitAcResPack> mExangeJson = new ExangeJson<GetAbilityLimitAcResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                resPack = CloudApi.getInstance().GetAcAbilityLimit(uuid, reqPack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resPack;
    }

    // type: 16, getAbilityLimit - assetType = 1 (FAN),4 (Air purifier),6 (TV)
    public GetAbilityLimitRemoteControlResPack getRemoteControlAbilityLimit(GetAbilityLimitReqPack reqPack) {
        GetAbilityLimitRemoteControlResPack resPack = new GetAbilityLimitRemoteControlResPack();
        int error_code = 0;
        if(reqPack.getAssetType() == RoomHubAllJoynDef.assetType.ASSET_TYPE_AC)
            error_code=ErrorKey.AC_ABILITY_INVALID;
        else if(reqPack.getAssetType() == RoomHubAllJoynDef.assetType.ASSET_TYPE_FAN)
            error_code=ErrorKey.FAN_ABILITY_INVALID;
        else if(reqPack.getAssetType() == RoomHubAllJoynDef.assetType.ASSET_TYPE_AIRPURIFIER)
            error_code=ErrorKey.AP_ABILITY_INVALID;
        else if(reqPack.getAssetType() == RoomHubAllJoynDef.assetType.ASSET_TYPE_TV)
            error_code=ErrorKey.TV_ABILITY_INVALID;

        resPack.setStatus_code(error_code);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_GET_ABILITY_LIMIT, getGsonInstance().toJson(reqPack));

                ExangeJson<GetAbilityLimitRemoteControlResPack> mExangeJson = new ExangeJson<GetAbilityLimitRemoteControlResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                resPack = CloudApi.getInstance().GetRemoteControlAbilityLimit(uuid, reqPack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resPack;
    }

    // type: 17
    // type: 18
    // type: 19
    // type: 20
    // type: 21, startUpgrade
    public BaseHomeApplianceResPack startUpgrade(UpgradeReqPack reqPack) {
        BaseHomeApplianceResPack resPack = new BaseHomeApplianceResPack();
        resPack.setStatus_code(ErrorKey.START_UPGRADE_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_START_UPGRADE, getGsonInstance().toJson(reqPack));

                ExangeJson<BaseHomeApplianceResPack> mExangeJson = new ExangeJson<BaseHomeApplianceResPack>();

                resPack = mExangeJson.Exe(ret, resPack);

                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.START_UPGRADE_FAILURE);

            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                resPack = CloudApi.getInstance().SendDeviceAssetsIRCommand_StartUpgrade(uuid, reqPack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resPack;

    }

    // type: 22

    // type: 23, add device
    public AddHomeApplianceResPack addHomeAppliance(AddHomeApplianceReqPack reqPack) {
        AddHomeApplianceResPack resPack = new AddHomeApplianceResPack();
        resPack.setStatus_code(ErrorKey.ADD_APPLIANCES_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_ADD_DEVICE, getGsonInstance().toJson(reqPack));

                ExangeJson<AddHomeApplianceResPack> mExangeJson = new ExangeJson<AddHomeApplianceResPack>();

                resPack = mExangeJson.Exe(ret, resPack);

                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.ADD_APPLIANCES_FAILURE);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
            resPack.setStatus_code(ErrorKey.CLOUDAPI_NOT_ALLOWED);
        }

        return resPack;
    }

    // type: 24, remove device
    public RemoveHomeApplianceResPack removeHomeAppliance(RemoveHomeApplianceReqPack reqPack) {
        RemoveHomeApplianceResPack resPack = new RemoveHomeApplianceResPack();
        resPack.setStatus_code(ErrorKey.DELETE_APPLIANCES_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_REMOVE_DEVICE, getGsonInstance().toJson(reqPack));

                ExangeJson<RemoveHomeApplianceResPack> mExangeJson = new ExangeJson<RemoveHomeApplianceResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.DELETE_APPLIANCES_FAILURE);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
            resPack.setStatus_code(ErrorKey.CLOUDAPI_NOT_ALLOWED);
        }

        return resPack;
    }

    //type: 25, get all asset
    public GetHomeApplianceAllAssetsResPack getHomeApplianceAllAssets() {
        GetHomeApplianceAllAssetsResPack resPack = null;

        if(mRoomHubInterface != null) {
            try {
                BaseReqPack reqPack = new BaseReqPack();
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_GET_ALL_ASSETS, getGsonInstance().toJson(reqPack));

                ExangeJson<GetHomeApplianceAllAssetsResPack> mExangeJson = new ExangeJson<GetHomeApplianceAllAssetsResPack>();

                resPack = new GetHomeApplianceAllAssetsResPack();

                resPack = mExangeJson.Exe(ret, resPack);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
        }

        return resPack;
    }

    // type: 27 Auto WiFi bridge switch
    public BaseHomeApplianceResPack switchAutoWifiBridge(AutoSwitchWifiBridgeReqPack reqPack) {
        BaseHomeApplianceResPack resPack = new BaseHomeApplianceResPack();
        resPack.setStatus_code(ErrorKey.AUTO_WIFI_BRIDGE_FAILURE);
        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_AUTO_WIFI_BRIDGE_SWITCH, getGsonInstance().toJson(reqPack));

                ExangeJson<BaseHomeApplianceResPack> mExangeJson = new ExangeJson<BaseHomeApplianceResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.AUTO_WIFI_BRIDGE_FAILURE);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
            resPack.setStatus_code(ErrorKey.CLOUDAPI_NOT_ALLOWED);
        }

        return resPack;
    }

    // type: 28 Get WiFi bridge state
    public AutoSwitchWifiBridgeResPack getWifiBridgeState() {
        AutoSwitchWifiBridgeResPack resPack = new AutoSwitchWifiBridgeResPack();;
        resPack.setStatus_code(ErrorKey.GET_WIFI_BRIDGE_FAILURE);
        if(mRoomHubInterface != null) {
            try {
                BaseReqPack reqPack = new BaseReqPack();
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_GET_WIFI_BRIDGE_STATE, getGsonInstance().toJson(reqPack));

                ExangeJson<AutoSwitchWifiBridgeResPack> mExangeJson = new ExangeJson<AutoSwitchWifiBridgeResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                GetDeviceInfoResPack device_info_resPack = CloudApi.getInstance().GetDeviceInfo(uuid);
                resPack.setEnable(device_info_resPack.getWiFiBridge());
                resPack.setStatus_code(device_info_resPack.getStatus_code());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resPack;
    }

    //  type: 29: Reboot room hub
    public CommandResPack RebootRoomHub() {
        CommandResPack resPack = new CommandResPack();
        resPack.setStatus_code(ErrorKey.ROOMHUB_REBOOT_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                BaseReqPack reqPack = new BaseReqPack();
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_REBOOT_ROOM_HUB, getGsonInstance().toJson(reqPack));

                ExangeJson<CommandResPack> mExangeJson = new ExangeJson<CommandResPack>();

                resPack = mExangeJson.Exe(ret, resPack);

                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.ROOMHUB_REBOOT_FAILURE);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
            try {
                resPack = CloudApi.getInstance().SendDeviceAssetsIRCommand_RebootRoomHub(uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resPack;
    }

    //  type: 30: Fail recover
    public CommandResPack setFailRecover(SetFailRecoverReqPack reqPack) {
        CommandResPack resPack = new CommandResPack();
        resPack.setStatus_code(ErrorKey.FAIL_RECOVER_NOT_SET);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_FAIL_RECOVER, getGsonInstance().toJson(reqPack));

                ExangeJson<CommandResPack> mExangeJson = new ExangeJson<CommandResPack>();

                resPack = mExangeJson.Exe(ret, resPack);

                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.FAIL_RECOVER_NOT_SET);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
            try {
                resPack = CloudApi.getInstance().SendDeviceAssetsIRCommand_AcFailRecover(uuid, reqPack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resPack;
    }

    // type: 31 Onboarding
    public OnboardingResPack setOnboarding(OnboardingReqPack reqPack) {
        OnboardingResPack resPack = new OnboardingResPack();
        resPack.setStatus_code(ErrorKey.ONBOARDING_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_ONBOARDING, getGsonInstance().toJson(reqPack));

                ExangeJson<OnboardingResPack> mExangeJson = new ExangeJson<OnboardingResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                if(resPack.getResult() < ErrorKey.Success)
                    resPack.setStatus_code(ErrorKey.ONBOARDING_FAILURE);
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
        }
        return resPack;
    }

    // type: 33 Scan asset
    public CommandResPack ScanAsset(ScanAssetReqPack reqPack) {
        CommandResPack resPack = new CommandResPack();
        resPack.setStatus_code(ErrorKey.SCAN_ASSET_FAILURE);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_SCAN_ASSET, getGsonInstance().toJson(reqPack));

                ExangeJson<CommandResPack> mExangeJson = new ExangeJson<CommandResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                int result=resPack.getResult();
                if(result < ErrorKey.Success) {
                    if(result == -2)
                        resPack.setStatus_code(ErrorKey.SCAN_ASSET_NOT_SUPPORTED);
                    else
                        resPack.setStatus_code(ErrorKey.SCAN_ASSET_FAILURE);
                }
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
        }
        return resPack;
    }
    // type: 34 set Asset Profile
    public CommandResPack setAssetProfile(SetAssetProfileReqPack reqPack) {
        CommandResPack resPack = new CommandResPack();
        resPack.setStatus_code(ErrorKey.ASSET_PROFILE_NOT_SET);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_SET_ASSET_PROFILE, getGsonInstance().toJson(reqPack));

                ExangeJson<CommandResPack> mExangeJson = new ExangeJson<CommandResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                int result=resPack.getResult();
                if(result < ErrorKey.Success) {
                    resPack.setStatus_code(ErrorKey.ASSET_PROFILE_NOT_SET);
                }
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
        }
        return resPack;
    }
    // type: 35 get Asset Profile
    public GetAssetProfileResPack getAssetProfile(CommonReqPack reqPack) {
        GetAssetProfileResPack resPack = new GetAssetProfileResPack();
        resPack.setStatus_code(ErrorKey.ASSET_PROFILE_INVALID);

        if(mRoomHubInterface != null) {
            try {
                String ret = mRoomHubInterface.method(RoomHubAllJoynDef.method.METHOD_TYPE_GET_ASSET_PROFILE, getGsonInstance().toJson(reqPack));

                ExangeJson<GetAssetProfileResPack> mExangeJson = new ExangeJson<GetAssetProfileResPack>();

                resPack = mExangeJson.Exe(ret, resPack);
                int result=resPack.getResult();
                if(result < ErrorKey.Success) {
                    resPack.setStatus_code(ErrorKey.ASSET_PROFILE_INVALID);
                }
            } catch (BusException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // TODO: through CloudApi
        }
        return resPack;
    }
}


