package com.quantatw.sls.alljoyn;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;
import org.alljoyn.bus.annotation.BusProperty;
import org.alljoyn.bus.annotation.BusSignal;
import org.alljoyn.bus.annotation.Position;
import org.alljoyn.bus.annotation.Signature;

/*
 * The BusInterface annotation is used to tell the code this interface is an AllJoyn
 * interface.
 *
 * The 'name' value is used to specify by which name this interface will be known.  If the name is
 * not given the fully qualified name of the Java interface is be used.  In most instances its best
 * to assign an interface name since it helps promote code reuse.
 */
@BusInterface(name = "com.quantatw.roomhub")
public interface RoomHubInterface {
    public class weekDay_i {
        @Position(0) @Signature("i") public int day = 0;
    }
    public class irData_y {
        @Position(0) public byte data = 0;
    }
    /*
    public class dataList_iss {
        @Position(0) @Signature("i") public int ir_data_index = 0;
        @Position(1) @Signature("s") public String brand = "";
        @Position(2) @Signature("s") public String model = "";
    }
    */
    public class setDeviceInfo_values_ss {
        @Position(0) public String brandName = "";
        @Position(1) public String deviceModel = "";
        @Position(2) public String townId = "";
    }
    /*
    public class setDeviceInfo_return_value__ssb {
        @Position(0) public values_ss values = new values_ss();
        @Position(1) public boolean result = false;
    }
    */
    public class Command_Values_iiiiiii {
        @Position(0) @Signature("i") public int powerStatus = 0;
        @Position(1) @Signature("i") public int temperature = 0;
        @Position(2) @Signature("i") public int functionMode = 0;
        @Position(3) @Signature("i") public int swing = 0;
        @Position(4) @Signature("i") public int fan = 0;
        @Position(5) @Signature("i") public int timerOn = 0;
        @Position(6) @Signature("i") public int timerOff = 0;
        @Position(7) @Signature("s") public String userId = "";
    }
    public class getDeviceInfo_return_values_iiiiiiiss {
        @Position(0) public int powerOnOff = 0;
        @Position(1) public int temperature = 0;
        @Position(2) public int functionMode = 0;
        @Position(3) public int swing = 0;
        @Position(4) public int fan = 0;
        @Position(5) public int timerOn = 0;
        @Position(6) public int timerOff = 0;
        @Position(7) public String brandName = "";
        @Position(8) public String deviceModel = "";
        @Position(9) public String townId = "";
        @Position(10) public String userId = "";
    }
    public class AddSchedule_Schedule {
        @Position(0) @Signature("i") public int modeType = 0;
        @Position(1) @Signature("i") public int value = 0;
        @Position(2) @Signature("s") public String startTime  = "";
        @Position(3) @Signature("s") public String endTime = "";
        @Position(4) @Signature("b") public boolean repeat = false;
        @Position(5) @Signature("b") public boolean state = false;
        @Position(6) @Signature("a(i)") public RoomHubInterface.weekDay_i[] weekday;
    }
    public class GetAllSchedule_Schedules {
        @Position(0) @Signature("i") public int index = 0;
        @Position(1) @Signature("i") public int modeType = 0;
        @Position(2) @Signature("i") public int value = 0;
        @Position(3) @Signature("s") public String startTime = "";
        @Position(4) @Signature("s") public String endTime = "";
        @Position(5) @Signature("b") public boolean repeat = false;
        @Position(6) @Signature("b") public boolean state = false;
        @Position(7) @Signature("a(i)") public RoomHubInterface.weekDay_i[] weekday;
    }
    public class controlData_ississa_y {
        @Position(0) public int keyId = 0;
        @Position(1) public String stPower = "";
        @Position(2) public String stMode = "";
        @Position(3) public int stTemp = 0;
        @Position(4) public String stFan = "";
        @Position(5) public String stSwing = "";
        @Position(6) public irData_y[] irData;
    }

    public class AbilityLimit {
        @Position(0) public int mode;
        @Position(1) public int maxValue;
        @Position(2) public int minValue;
    }

    public class AcOnOffStatus {
        @Position(0) public int functionMode;
        @Position(1) public int targetTemperature;
        @Position(2) public int originTemperature;
        @Position(3) public int nowTemperature;
        @Position(4) public int timeInterval;
        @Position(5) public int lastAction;
        @Position(6) public String userId;
    }

    public class SphygmometerResult{
        @Position(0) public int maxBloodPressure;
        @Position(1) public int minBloodPressure;
        @Position(2) public int heartRate;
    }

    public class NextSchedule {
        @Position(0) public int modeType;
        @Position(1) public int value;
        @Position(2) public String startTime;
        @Position(3) public boolean powerOnOff;
    }
    /*
     * The BusMethod annotation signifies this function should be used as part of the AllJoyn
     * interface. The runtime is smart enough to figure out what the input and output of the
     * method is based on the input/output arguments of the method.
     *
     * All methods that use the BusMethod annotation can throw a BusException and should indicate
     * this fact.
     */
    @BusMethod (name = "method", signature = "is", replySignature = "s")
    String method(int type, String parameter) throws BusException;

    @BusMethod (name = "command", signature = "(iiiiiiis)", replySignature = "i")
    int command(RoomHubInterface.Command_Values_iiiiiii values) throws BusException;
    @BusMethod (name = "ledControl", signature = "iiiii", replySignature = "i")
    int ledControl(int color, int controlType, int enableMsec, int disableMsec, int loopNumber) throws BusException;
    @BusMethod (name = "startWPS", replySignature = "i")
    int startWPS() throws BusException;
    @BusMethod (name = "learning")
    void learning() throws BusException;
    @BusMethod (name = "checkIRData", signature = "a(y)", replySignature = "i")
    int checkIRData(irData_y[] irData) throws BusException;
    @BusMethod (name = "cleanIRControlData")
    void cleanIRControlData() throws BusException;
    @BusMethod (name = "addIRControlData", signature = "(ississa(y))", replySignature = "i")
    int addIRControlData(controlData_ississa_y controlData) throws BusException;

    @BusMethod (name = "addSchedule", signature = "(iissbba(i))", replySignature = "i")
    int addSchedule(RoomHubInterface.AddSchedule_Schedule schedule) throws BusException;
    @BusMethod (name = "getAllSchedule", replySignature = "a(iiissbba(i))")
    RoomHubInterface.GetAllSchedule_Schedules[] getAllSchedule() throws BusException;
    @BusMethod (name = "removeSchedule", signature = "i", replySignature = "b")
    boolean removeSchedule(int index) throws BusException;
    @BusMethod (name = "removeAllSchedule", replySignature = "b")
    boolean removeAllSchedule() throws BusException;
    @BusMethod (name = "getDeviceInfo", replySignature = "(iiiiiiissss)")
    getDeviceInfo_return_values_iiiiiiiss getDeviceInfo() throws BusException;
    @BusMethod (name = "setDeviceInfo", signature = "(sss)", replySignature = "b")
    boolean setDeviceInfo(setDeviceInfo_values_ss deviceInfo) throws BusException;
    @BusMethod (name = "getAbilityLimit", replySignature = "a(iii)")
    AbilityLimit[] getAbilityLimit() throws BusException;
    @BusMethod (name = "setIRTx", signature = "i", replySignature = "b")
    boolean setIRTx(int strength) throws BusException;
    @BusMethod (name = "modifySchedule", signature = "(iissbba(i))i", replySignature = "b")
    boolean modifySchedule(RoomHubInterface.AddSchedule_Schedule schedule, int index) throws BusException;
    @BusMethod (name = "getACOnOffStatus", replySignature = "(iiiiiis)")
    AcOnOffStatus getACOnOffStatus() throws BusException;
    @BusMethod (name = "setCloudServerAddress", signature = "s", replySignature = "b")
    boolean setCloudServerAddress(String address) throws BusException;
    @BusMethod (name = "sphygmometerCommand", signature = "b", replySignature = "b")
    boolean sphygmometerCommand(boolean connect) throws BusException;
    @BusMethod (name = "startUpgrade", signature = "s", replySignature = "b")
    boolean startUpgrade(String imageURL) throws BusException;
    @BusMethod (name = "getNextSchedule", replySignature = "(iisb)")
    NextSchedule getNextSchedule() throws BusException;

    /*
     * The BusProperty annotation signifies this property should be used as part of the
     * AllJoyn interface. The runtime is smart enough to figure out what the input and output of
     * the property is based on the input/output arguments of the property.
     *
     * All properties that use the BusProperty annotation can throw a BusException and should
     * indicate this fact.
     */

    @BusProperty (name = "name", signature = "s")
    void setname(String in_value) throws BusException;
    @BusProperty (name = "name", signature = "s")
    String getname() throws BusException;
    @BusProperty (name = "uuid", signature = "s")
    String getuuid() throws BusException;
    @BusProperty (name = "temperature", signature = "i")
    int gettemperature() throws BusException;
    @BusProperty (name = "humidity", signature = "i")
    int gethumidity() throws BusException;
    @BusProperty (name = "bluetoothUUID", signature = "s")
    String getbluetoothUUID() throws BusException;
    @BusProperty (name = "bluetoothMajor", signature = "i")
    int getbluetoothMajor() throws BusException;
    @BusProperty (name = "bluetoothMinor", signature = "i")
    int getbluetoothMinor() throws BusException;
    @BusProperty (name = "ownerId", signature = "s")
    void setownerId(String in_value) throws BusException;
    @BusProperty (name = "ownerId", signature = "s")
    String getownerId() throws BusException;
    @BusProperty (name = "SSID", signature = "s")
    String getSSID() throws BusException;
    @BusProperty (name = "SSID", signature = "s")
    void setSSID(String in_value) throws BusException;
    @BusProperty (name = "time", signature = "i")
    int getTime() throws BusException;
    @BusProperty (name = "time", signature = "i")
    void setTime(int timeStamp) throws BusException;


    /*
     * The BusSignal annotation signifies this signal should be used as part of the
     * AllJoyn interface.
     *
     * All signals that use the BusSignal annotation can throw a BusException and should
     * indicate this fact.
     */
    @BusSignal (name = "signal", signature = "s")
    void signal(String values) throws BusException;

    @BusSignal (name = "temperature", signature = "i")
    void temperature(int temperature) throws BusException;
    @BusSignal (name = "humidity", signature = "i")
    void humidity(int humidity) throws BusException;
    @BusSignal (name = "learningResult", signature = "a(y)")
    void learningResult(irData_y[] signature) throws BusException;
    @BusSignal (name = "deviceInfoChange", signature = "(iiiiiiissss)")
    void deviceInfoChange(getDeviceInfo_return_values_iiiiiiiss values) throws BusException;
    @BusSignal (name = "nameChange", signature = "s")
    void nameChange(String newName) throws BusException;
    @BusSignal (name = "syncTime")
    void syncTime() throws BusException;
    @BusSignal (name = "ACOnOffStatus", signature = "(iiiiiis)")
    void ACOnOffStatus(AcOnOffStatus status) throws BusException;
    @BusSignal (name = "updateSchedule", signature = "(iiissbba(i))")
    void updateSchedule(GetAllSchedule_Schedules schedules) throws BusException;
    @BusSignal (name = "deleteSchedule", signature = "i")
    void deleteSchedule(int index) throws BusException;
    @BusSignal (name = "findSphygmometer", signature = "b")
    void findSphygmometer(boolean result) throws BusException;
    @BusSignal (name = "sphygmometerResult", signature = "(iii)")
    void sphygmometerResult(SphygmometerResult result) throws BusException;
    @BusSignal (name = "nextSchedule", signature = "(iisb)")
    void nextSchedule(NextSchedule values) throws BusException;
}
