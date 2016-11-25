package com.quantatw.sls.handler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.alljoyn.bus.AboutKeys;
import org.alljoyn.bus.AboutListener;
import org.alljoyn.bus.AboutObjectDescription;
import org.alljoyn.bus.AboutProxy;
import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.OnJoinSessionListener;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.Variant;
import org.alljoyn.bus.annotation.BusSignalHandler;
import org.alljoyn.services.android.security.AuthPasswordHandler;
import org.alljoyn.services.android.security.SrpAnonymousKeyListener;
import org.alljoyn.services.android.utils.AndroidLogger;

import com.quantatw.sls.alljoyn.RoomHubInterface;
import com.quantatw.sls.alljoyn.ConfigCtrlInterface;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.device.RoomHubDeviceManagement;
import com.quantatw.sls.key.ReasonType;
import com.quantatw.sls.key.SensorTypeKey;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.listener.HomeApplianceSignalListener;
import com.quantatw.sls.listener.RoomHubDeviceListener;
import com.quantatw.sls.listener.RoomHubSignalListener;
import com.quantatw.sls.object.AlljoynAboutData;
import com.quantatw.sls.pack.roomhub.AcOnOffStatusResPack;
import com.quantatw.sls.pack.roomhub.DeleteScheduleResPack;
import com.quantatw.sls.pack.roomhub.DeviceInfoChangeResPack;
import com.quantatw.sls.pack.roomhub.LearningResultResPack;
import com.quantatw.sls.pack.roomhub.NameChangeResPack;
import com.quantatw.sls.pack.roomhub.NextScheduleResPack;
import com.quantatw.sls.pack.roomhub.RoomHubDataResPack;
import com.quantatw.sls.pack.roomhub.UpdateScheduleResPack;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class RoomHubBusHandler extends Handler {
    static {
        System.loadLibrary("alljoyn_java");
    }
	/*
	 * Name used as the well-known name and the advertised name of the
	 * service this client is interested in. This name must be a unique name
	 * both to the bus and to the network as a whole.
	 * 
	 * The name uses reverse URL style of naming, and matches the name used
	 * by the service.
	 */

    private static final String LOG_TAG = "RoomHubBusHandler";
    private static final String INTERFACE_NAME = "com.quantatw.roomhub";
    private static final String CONFIG_INTERFACE_NAME = "org.alljoyn.Config";

    private static final String AJ_ER_BUS_REPLY_IS_ERROR_MESSAGE = "ER_BUS_REPLY_IS_ERROR_MESSAGE";

    private BusAttachment mBus;
    private ProxyBusObject mProxyObj;
    // private SensorStoneInterface mBasicInterface;
    private ArrayList<RoomHubInterface> mbArrayList;
    Context mContext;
    private boolean mIsStoppingDiscovery;

    private RoomHubDeviceManagement mDeviceManagement;

    private RoomHubDeviceListener deviceListener = null;
    private RoomHubSignalListener signalListener = null;
    private HomeApplianceSignalListener homeApplianceSignalListener = null;

/**
 * Default password for daemon and the Alljoyn devices
 */
    private final static String DEFAULT_PINCODE = "000000";
    private AboutListener mListener = null;

    /* These are the messages sent to the BusHandler from the UI. */
    public static final int CONNECT = 1;
    public static final int JOIN_SESSION = 2;
    public static final int DISCONNECT = 3;
    public static final int JOINED = 4;
    public static final int RECONNECT = 5;
    // public static final int CAT = 4;

    class AboutInfo {
        public String busName;
        public int version;
        public short port;
        public AboutObjectDescription[] objectDescriptions;
        public Map<String, Variant> aboutData;
    }

    class MyAboutListener implements AboutListener {
        public void announced(String busName, int version, short port, AboutObjectDescription[] objectDescriptions,
                              Map<String, Variant> aboutData) {
            // Place code here to handle Announce signal.
            Log.d(LOG_TAG, "Temp - About: " + busName);
            AboutInfo info = new AboutInfo();
            info.aboutData = aboutData;
            info.busName = busName;
            info.objectDescriptions = objectDescriptions;
            info.port = port;
            info.version = version;
            Message msg = obtainMessage(JOIN_SESSION);

            msg.obj = info;
            sendMessage(msg);

        }
    }

    @BusSignalHandler(iface = "com.quantatw.sls.alljoyn.RoomHubInterface", signal = "signal")
    public void signal(String values) throws BusException {
        Log.d(LOG_TAG, "signal : " + values);
        Log.d(LOG_TAG, mBus.getMessageContext().sender + " " + mBus.getMessageContext().sessionId);
        RoomHubDevice deviceStatus = new RoomHubDevice();
        deviceStatus.setSessionid(mBus.getMessageContext().sessionId);

        int ret = mDeviceManagement.indexOfAlljoynDevice(deviceStatus);
        if (ret >= 0) {
            final RoomHubDevice dev = mDeviceManagement.GetAlljoynDevice(ret);
            RoomHubSignalDispatchHandler.dispatch(homeApplianceSignalListener, dev, values);
            // TODO: roomhub 1.0.0 (values: JSON String)
        }

    }

    @BusSignalHandler(iface = "com.quantatw.sls.alljoyn.RoomHubInterface", signal = "temperature")
    public void temperature(int temperature) throws BusException {
        Log.d(LOG_TAG, "temperature : " + temperature);
        Log.d(LOG_TAG, mBus.getMessageContext().sender + " " + mBus.getMessageContext().sessionId);
        RoomHubDevice deviceStatus = new RoomHubDevice();
        deviceStatus.setSessionid(mBus.getMessageContext().sessionId);

        int ret = mDeviceManagement.indexOfAlljoynDevice(deviceStatus);
        if (ret >= 0) {
            final RoomHubDevice dev = mDeviceManagement.GetAlljoynDevice(ret);

            RoomHubDataResPack temperaturePack = new RoomHubDataResPack();
            temperaturePack.setUuid(dev.getUuid());
            temperaturePack.setSensorDataType(SensorTypeKey.SENSOR_TEMPERATURE);
            temperaturePack.setValue(temperature);
            if(signalListener != null)
                signalListener.RoomHubDataUpdate(temperaturePack, SourceType.ALLJOYN);
        }

    }

    @BusSignalHandler(iface = "com.quantatw.sls.alljoyn.RoomHubInterface", signal = "humidity")
    public void humidity(int humidity) throws BusException {
        Log.d(LOG_TAG, "humidity : " + humidity);
        Log.d(LOG_TAG, mBus.getMessageContext().sender + " " + mBus.getMessageContext().sessionId);
        RoomHubDevice deviceStatus = new RoomHubDevice();
        deviceStatus.setSessionid(mBus.getMessageContext().sessionId);

        int ret = mDeviceManagement.indexOfAlljoynDevice(deviceStatus);
        if (ret >= 0) {
            final RoomHubDevice dev = mDeviceManagement.GetAlljoynDevice(ret);

            RoomHubDataResPack humidityPack = new RoomHubDataResPack();
            humidityPack.setUuid(dev.getUuid());
            humidityPack.setSensorDataType(SensorTypeKey.SENSOR_HUMIDITY);
            humidityPack.setValue(humidity);
            if(signalListener != null)
                signalListener.RoomHubDataUpdate(humidityPack, SourceType.ALLJOYN);
        }

    }

    @BusSignalHandler(iface = "com.quantatw.sls.alljoyn.RoomHubInterface", signal = "learningResult")
    public void learningResult(RoomHubInterface.irData_y[] signature) throws BusException {
        Log.d(LOG_TAG, "learningResult!");
        Log.d(LOG_TAG, mBus.getMessageContext().sender + " " + mBus.getMessageContext().sessionId);
        RoomHubDevice deviceStatus = new RoomHubDevice();
        deviceStatus.setSessionid(mBus.getMessageContext().sessionId);

        int ret = mDeviceManagement.indexOfAlljoynDevice(deviceStatus);
        if (ret >= 0) {
            final RoomHubDevice dev = mDeviceManagement.GetAlljoynDevice(ret);

            LearningResultResPack learningResultPack = new LearningResultResPack();
            learningResultPack.setUuid(dev.getUuid());
            learningResultPack.setIrData(signature);
            if(signalListener != null)
                signalListener.RoomHubLearningResultUpdate(learningResultPack);
        }

    }

    @BusSignalHandler(iface = "com.quantatw.sls.alljoyn.RoomHubInterface", signal = "deviceInfoChange")
    public void deviceInfoChange(RoomHubInterface.getDeviceInfo_return_values_iiiiiiiss values) throws BusException {
        Log.d(LOG_TAG, "deviceInfoChange!");
        Log.d(LOG_TAG, mBus.getMessageContext().sender + " " + mBus.getMessageContext().sessionId);
        RoomHubDevice deviceStatus = new RoomHubDevice();
        deviceStatus.setSessionid(mBus.getMessageContext().sessionId);

        int ret = mDeviceManagement.indexOfAlljoynDevice(deviceStatus);
        if (ret >= 0) {
            final RoomHubDevice dev = mDeviceManagement.GetAlljoynDevice(ret);

            DeviceInfoChangeResPack deviceInfoChangeResPack = new DeviceInfoChangeResPack();
            deviceInfoChangeResPack.setUuid(dev.getUuid());
            deviceInfoChangeResPack.setDeviceInfo(values);
            if(signalListener != null)
                signalListener.RoomHubDeviceInfoChangeUpdate(deviceInfoChangeResPack, SourceType.ALLJOYN);
        }
    }

    @BusSignalHandler(iface = "com.quantatw.sls.alljoyn.RoomHubInterface", signal = "nameChange")
    public void nameChange(String name) throws BusException {
        Log.d(LOG_TAG, "name : " + name);
        Log.d(LOG_TAG, mBus.getMessageContext().sender + " " + mBus.getMessageContext().sessionId);
        RoomHubDevice deviceStatus = new RoomHubDevice();
        deviceStatus.setSessionid(mBus.getMessageContext().sessionId);

        int ret = mDeviceManagement.indexOfAlljoynDevice(deviceStatus);
        if (ret >= 0) {
            final RoomHubDevice dev = mDeviceManagement.GetAlljoynDevice(ret);

            NameChangeResPack namePack = new NameChangeResPack();
            namePack.setUuid(dev.getUuid());
            namePack.setName(name);
            if(signalListener != null)
                signalListener.RoomHubNameChangeUpdate(namePack);
        }

    }

    @BusSignalHandler(iface = "com.quantatw.sls.alljoyn.RoomHubInterface", signal = "syncTime")
    public void syncTime() throws BusException {
        Log.d(LOG_TAG, "syncTime");
        Log.d(LOG_TAG, mBus.getMessageContext().sender + " " + mBus.getMessageContext().sessionId);
        RoomHubDevice deviceStatus = new RoomHubDevice();
        deviceStatus.setSessionid(mBus.getMessageContext().sessionId);

        int ret = mDeviceManagement.indexOfAlljoynDevice(deviceStatus);
        if (ret >= 0) {
            final RoomHubDevice dev = mDeviceManagement.GetAlljoynDevice(ret);

            if(signalListener != null)
                signalListener.RoomHubSyncTime();
        }
    }

    @BusSignalHandler(iface = "com.quantatw.sls.alljoyn.RoomHubInterface", signal = "ACOnOffStatus")
    public void ACOnOffStatus(RoomHubInterface.AcOnOffStatus status) throws BusException {
        Log.d(LOG_TAG, "ACOnOffStatus");
        Log.d(LOG_TAG, mBus.getMessageContext().sender + " " + mBus.getMessageContext().sessionId);
        RoomHubDevice deviceStatus = new RoomHubDevice();
        deviceStatus.setSessionid(mBus.getMessageContext().sessionId);

        int ret = mDeviceManagement.indexOfAlljoynDevice(deviceStatus);
        if (ret >= 0) {
            final RoomHubDevice dev = mDeviceManagement.GetAlljoynDevice(ret);

            AcOnOffStatusResPack resPack = new AcOnOffStatusResPack();
            resPack.setUuid(dev.getUuid());
            resPack.setFunctionMode(status.functionMode);
            resPack.setTargetTemperature(status.targetTemperature);
            resPack.setOriginTemperature(status.originTemperature);
            resPack.setNowTemperature(status.nowTemperature);
            resPack.setTimeInterval(status.timeInterval);
            resPack.setLastAction(status.lastAction);
            resPack.setUserId(status.userId);

            if(signalListener != null)
                signalListener.RoomHubAcOnOffStatusUpdate(resPack);
        }
    }

    @BusSignalHandler(iface = "com.quantatw.sls.alljoyn.RoomHubInterface", signal = "updateSchedule")
    public void updateSchedule(RoomHubInterface.GetAllSchedule_Schedules status) throws BusException {
        Log.d(LOG_TAG, "updateSchedule");
        Log.d(LOG_TAG, mBus.getMessageContext().sender + " " + mBus.getMessageContext().sessionId);
        RoomHubDevice deviceStatus = new RoomHubDevice();
        deviceStatus.setSessionid(mBus.getMessageContext().sessionId);

        int ret = mDeviceManagement.indexOfAlljoynDevice(deviceStatus);
        if (ret >= 0) {
            final RoomHubDevice dev = mDeviceManagement.GetAlljoynDevice(ret);

            UpdateScheduleResPack resPack = new UpdateScheduleResPack();
            resPack.setUuid(dev.getUuid());
            resPack.setIndex(status.index);
            resPack.setModeType(status.modeType);
            resPack.setValue(status.value);
            resPack.setStartTime(status.startTime);
            resPack.setEndTime(status.endTime);
            resPack.setRepeat(status.repeat);
            resPack.setState(status.state);
            resPack.setWeekday(status.weekday);

            if(signalListener != null)
                signalListener.RoomHubUpdateSchedule(resPack);
        }
    }

    @BusSignalHandler(iface = "com.quantatw.sls.alljoyn.RoomHubInterface", signal = "deleteSchedule")
    public void deleteSchedule(int index) throws BusException {
        Log.d(LOG_TAG, "deleteSchedule");
        Log.d(LOG_TAG, mBus.getMessageContext().sender + " " + mBus.getMessageContext().sessionId);
        RoomHubDevice deviceStatus = new RoomHubDevice();
        deviceStatus.setSessionid(mBus.getMessageContext().sessionId);

        int ret = mDeviceManagement.indexOfAlljoynDevice(deviceStatus);
        if (ret >= 0) {
            final RoomHubDevice dev = mDeviceManagement.GetAlljoynDevice(ret);

            DeleteScheduleResPack resPack = new DeleteScheduleResPack();
            resPack.setUuid(dev.getUuid());
            resPack.setIndex(index);

            if(signalListener != null)
                signalListener.RoomHubDeleteSchedule(resPack);
        }
    }

    @BusSignalHandler(iface = "com.quantatw.sls.alljoyn.RoomHubInterface", signal = "nextSchedule")
    public void nextSchedule(RoomHubInterface.NextSchedule values) throws BusException {
        Log.d(LOG_TAG, "nextSchedule");
        Log.d(LOG_TAG, mBus.getMessageContext().sender + " " + mBus.getMessageContext().sessionId);
        RoomHubDevice deviceStatus = new RoomHubDevice();
        deviceStatus.setSessionid(mBus.getMessageContext().sessionId);

        int ret = mDeviceManagement.indexOfAlljoynDevice(deviceStatus);
        if (ret >= 0) {
            final RoomHubDevice dev = mDeviceManagement.GetAlljoynDevice(ret);

            NextScheduleResPack resPack = new NextScheduleResPack();
            resPack.setUuid(dev.getUuid());
            resPack.setModeType(values.modeType);
            resPack.setValue(values.value);
            resPack.setStartTime(values.startTime);
            resPack.setPowerOnOff(values.powerOnOff);

            if(signalListener != null)
                signalListener.RoomHubNextSchedule(resPack);
        }
    }

    @BusSignalHandler(iface = "com.quantatw.sls.alljoyn.RoomHubInterface", signal = "findSphygmometer")
    public void findSphygmometer(boolean result) throws BusException {
        Log.d(LOG_TAG, "findSphygmometer");
        Log.d(LOG_TAG, mBus.getMessageContext().sender + " " + mBus.getMessageContext().sessionId);
        RoomHubDevice deviceStatus = new RoomHubDevice();
        deviceStatus.setSessionid(mBus.getMessageContext().sessionId);

        int ret = mDeviceManagement.indexOfAlljoynDevice(deviceStatus);
        if (ret >= 0) {
            final RoomHubDevice dev = mDeviceManagement.GetAlljoynDevice(ret);

            // TODO
        }
    }

    @BusSignalHandler(iface = "com.quantatw.sls.alljoyn.RoomHubInterface", signal = "sphygmometerResult")
    public void sphygmometerResult(RoomHubInterface.SphygmometerResult result) throws BusException {
        Log.d(LOG_TAG, "sphygmometerResult: maxBP = " + result.maxBloodPressure + ", minBP = " + result.minBloodPressure + ", heartRate = " + result.heartRate);
        Log.d(LOG_TAG, mBus.getMessageContext().sender + " " + mBus.getMessageContext().sessionId);
        RoomHubDevice deviceStatus = new RoomHubDevice();
        deviceStatus.setSessionid(mBus.getMessageContext().sessionId);

        int ret = mDeviceManagement.indexOfAlljoynDevice(deviceStatus);
        if (ret >= 0) {
            final RoomHubDevice dev = mDeviceManagement.GetAlljoynDevice(ret);

            // TODO
        }
    }

    public RoomHubBusHandler(Looper looper,Context mContext) {
        super(looper);
        this.mContext = mContext;
        mIsStoppingDiscovery = false;
        mbArrayList = new ArrayList<RoomHubInterface>();

        mDeviceManagement = new RoomHubDeviceManagement();
    }


    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {

            case CONNECT: {
                org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(mContext);
                mBus = new BusAttachment("RoomHub", BusAttachment.RemoteMessage.Receive);

                Log.d(LOG_TAG, "connect!!");

                synchronized (mBus) {

                    // mBus.registerBusListener(new BusListener() {
                    // @Override
                    // public void foundAdvertisedName(String name,
                    // short transport, String namePrefix) {
                    //
                    // {
                    // Log.d(LOG_TAG, "foundAdvertisedName : " + name
                    // + " " + namePrefix);
                    // Message msg = obtainMessage(JOIN_SESSION);
                    // msg.arg1 = transport;
                    // msg.obj = name;
                    // sendMessage(msg);
                    // }
                    // }
                    // });

                    Status status = mBus.connect();

                    if (Status.OK != status) {
                        Log.d(LOG_TAG, "Alljoyn connect Error!");
                        return;
                    } else {
                        mIsStoppingDiscovery = false;
                        Log.d(LOG_TAG, "Alljoyn connect success!");
                    }

                    status = mBus.registerSignalHandlers(this);
                    if (Status.OK != status) {
                        Log.d(LOG_TAG, "registerSignalHandlers Error!");
                        return;
                    } else {
                        Log.d(LOG_TAG, "registerSignalHandlers success!");
                    }

                    mListener = new MyAboutListener();
                    mBus.registerAboutListener(mListener);

                    String ifaces[] = { INTERFACE_NAME };
                    status = mBus.whoImplements(ifaces);
                    // TODO: Add Onboarding & Configuration INTERFACE
//                    String ifaces[] = { OnboardingTransport.INTERFACE_NAME };
//                    status = mBus.whoImplements(ifaces);
//                    String ifaces[] = { Configuration.INTERFACE_NAME };
//                    status = mBus.whoImplements(ifaces);
                    if (status != Status.OK) {
                        Log.d(LOG_TAG, "whoImplements error!");
                        return;
                    }
                    Log.d(LOG_TAG, "whoImplements success!");
                    // status = mBus.findAdvertisedName(SERVICE_NAME);
                    //
                    // if (Status.OK != status) {
                    // Log.d(LOG_TAG, "findAdvertisedName Error!");
                    // return;
                    // }
                    // else {
                    // Log.d(LOG_TAG, "findAdvertisedName success!");
                    // }

                    // Add auth listener - needed for Configuration secure calls
                    String keyStoreFileName = mContext.getFileStreamPath("alljoyn_keystore").getAbsolutePath();
                    final String[] authMechanisms = new String[] { "ALLJOYN_SRP_KEYX", "ALLJOYN_ECDHE_PSK" };

                    SrpAnonymousKeyListener authListener = new SrpAnonymousKeyListener(new AuthPasswordHandler() {
                        @Override
                        public char[] getPassword(String peerName) {
                            return DEFAULT_PINCODE.toCharArray();
                        }

                        @Override
                        public void completed(String mechanism, String authPeer, boolean authenticated) {
                            //Log.d(LOG_TAG, "Auth completed: mechanism = " + mechanism + " authPeer= " + authPeer + " --> " + authenticated);
                        }

                    }, new AndroidLogger(), authMechanisms);
                    Status authStatus = mBus.registerAuthListener(authListener.getAuthMechanismsAsString(), authListener, keyStoreFileName);
                    //Log.d(LOG_TAG, "BusAttachment.registerAuthListener status = " + authStatus);
                    if (authStatus != Status.OK) {
                        Log.d(LOG_TAG, "Failed to register Auth listener status = " + authStatus.toString());
                        return;
                    }

                }

                break;
            }

            case RECONNECT: {

                if(mBus == null) {
                    break;
                }

                synchronized (mBus) {
                    Status status = mBus.connect();

                    if (Status.OK != status) {
                        Log.d(LOG_TAG, "re-connect Error!");
                        return;
                    } else {
                        mIsStoppingDiscovery = false;
                        Log.d(LOG_TAG, "re-connect success!");
                    }

                }

                break;
            }

            case (JOIN_SESSION): {
                Log.d(LOG_TAG, "Join session, mIsStoppingDiscovery = " + mIsStoppingDiscovery);
                if (mIsStoppingDiscovery) {
                    break;
                }

                final AboutInfo info = (AboutInfo) msg.obj;

                short contactPort = info.port;
                SessionOpts sessionOpts = new SessionOpts();
                sessionOpts.transports = SessionOpts.TRANSPORT_ANY;
                sessionOpts.isMultipoint = false;
                sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES;
                sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;
                final Mutable.IntegerValue sessionID = new Mutable.IntegerValue();

                Log.d(LOG_TAG, "joinSession()");
                Status status = mBus.joinSession(info.busName, info.port, sessionOpts, new SessionListener() {
                    @Override
                    public void sessionLost(int sessionId, int reason) {
                        RoomHubDevice deviceStatus = new RoomHubDevice();

                        deviceStatus.setSessionid(sessionId);
                        Log.d(LOG_TAG, "lostSession : " + sessionId + "  " + mBus.isConnected());
                        RemoveDevice(deviceStatus,ReasonType.ALLJOYN);

                    }

                    @Override
                    public void sessionMemberAdded(int sessionId, String uniqueName) {
                        // TODO Auto-generated method stub
                        super.sessionMemberAdded(sessionId, uniqueName);

                        Log.d(LOG_TAG, "addSession : " + sessionId + "  ");

                    }

                    @Override
                    public void sessionMemberRemoved(int sessionId, String uniqueName) {
                        // TODO Auto-generated method stub
                        super.sessionMemberRemoved(sessionId, uniqueName);
                        RoomHubDevice deviceStatus = new RoomHubDevice();

                        deviceStatus.setSessionid(sessionId);
                        Log.d(LOG_TAG, "removedSession : " + sessionId + "  ");
                        RemoveDevice(deviceStatus,ReasonType.ALLJOYN);

                    }

                }, new OnJoinSessionListener() {

                    @Override
                    public void onJoinSession(Status status, int sessionId, SessionOpts opts, Object context) {
                        // TODO Auto-generated method stub
                        super.onJoinSession(status, sessionId, opts, context);
                        if(status == Status.OK) {
                            sessionID.value = sessionId;

                            Message msg = obtainMessage(JOINED);

                            msg.obj = info;
                            msg.arg1 = sessionId;
                            sendMessage(msg);
                        }

                        Log.d(LOG_TAG, "onJoin : " + sessionId + "  , status = " + status);
                    }
                }, mContext);
                break;
            }

            // if (status == Status.OK)
            case (JOINED): {

                Log.d(LOG_TAG, "Joined");
                final AboutInfo info = (AboutInfo) msg.obj;
                int sessionID = msg.arg1;
                RoomHubDevice deviceStatus = null;

                for (int x = 0; x < info.objectDescriptions.length; x++) {
                    String[] interfaces = info.objectDescriptions[x].interfaces;
                    for (int y = 0; y < interfaces.length; y++) {
//					System.out.println("Temp " + interfaces[y]);
                        Log.d(LOG_TAG, "Temp " +  interfaces[y]);
                        // TODO: get Onboarding & Configuration interface
                        if (interfaces[y].equals(CONFIG_INTERFACE_NAME)) {
                            if (deviceStatus == null) {
                                deviceStatus = new RoomHubDevice();
                            }
                            ConfigCtrlInterface mBasicInterface = null;
                            try {
                                mProxyObj = mBus.getProxyBusObject(info.busName, info.objectDescriptions[x].path,
                                        sessionID, new Class<?>[] { ConfigCtrlInterface.class }, true);
                                mBus.setLinkTimeout(sessionID, new Mutable.IntegerValue(10));
                                Log.d(LOG_TAG, "Temp " + info.objectDescriptions[x].path);
							    /*
							     * We make calls to the methods of the AllJoyn
							     * object through one of its interfaces.
							     */
                                mBasicInterface = mProxyObj.getInterface(ConfigCtrlInterface.class);
                                deviceStatus.setConfigCtrlInterface(mBasicInterface);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        } else if (interfaces[y].equals(INTERFACE_NAME)) {
                            if (deviceStatus == null) {
                                deviceStatus = new RoomHubDevice();
                            }
                            RoomHubInterface mBasicInterface = null;
                            try {
                                mProxyObj = mBus.getProxyBusObject(info.busName, info.objectDescriptions[x].path,
                                        sessionID, new Class<?>[] { RoomHubInterface.class });
                                mBus.setLinkTimeout(sessionID, new Mutable.IntegerValue(10));
//							    System.out.println("Temp " + info.objectDescriptions[x].path);
                                Log.d(LOG_TAG, "Temp " + info.objectDescriptions[x].path);
							    /*
							     * We make calls to the methods of the AllJoyn
							     * object through one of its interfaces.
							     */
                                mBasicInterface = mProxyObj.getInterface(RoomHubInterface.class);
                                mbArrayList.add(mBasicInterface);
                                deviceStatus.setRoomHubInterface(mBasicInterface);
                            } catch (Exception e) {
                                // TODO: handle exception
                            }

                            if (mBasicInterface != null) {
                                try {
                                    deviceStatus.setUuid(mBasicInterface.getuuid());
                                    deviceStatus.setName(mBasicInterface.getname());
                                    // deviceStatus.set(mBasicInterface.getname());
                                    // deviceStatus.setVideoUrl(mBasicInterface.videoResolutionURL(0));

                                    // Save AboutData
                                    AboutProxy mAboutProxy = new AboutProxy(mBus, info.busName, sessionID);
                                    Map<String, Variant> aboutData = mAboutProxy.getAboutData("en");
                                    deviceStatus.setAboutData(CreateAlljoynAboutData(aboutData));
                                } catch (BusException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                    // Device session lost
                                    if(e.getMessage().equals(AJ_ER_BUS_REPLY_IS_ERROR_MESSAGE)) {
                                        return;
                                    }

                                }
                            }

                            Log.d(LOG_TAG, "JoinSession : " + sessionID + "  " + deviceStatus.getUuid());
                            deviceStatus.setSessionid(sessionID);

                            // try {
                            // System.out.println("Temp " +
                            // mBasicInterface.getname());
                            // } catch (BusException e) {
                            // // TODO Auto-generated catch block
                            // e.printStackTrace();
                            // }
                        }

                    }

                }

                if (sessionID != 0)
                    if (deviceStatus != null)

                        AddDevice(deviceStatus);

                // mProxyObj = mBus.getProxyBusObject((String) msg.obj,
                // SERVICE_PATH, sessionId.value,
                // new Class<?>[] { RoomHubInterface.class });
                //
                // /*
                // * We make calls to the methods of the AllJoyn object
                // * through one of its interfaces.
                // */
                // RoomHubInterface mBasicInterface = mProxyObj
                // .getInterface(RoomHubInterface.class);

                break;
            }

			/* Release all resources acquired in the connect. */
            case DISCONNECT: {

                Log.d(LOG_TAG, "disconnect!!" + ", mBus = " + mBus);
                if(mBus == null) {
                    break;
                }

                synchronized (mBus) {
                    mIsStoppingDiscovery = true;
                    ArrayList<RoomHubDevice> deviceStatus = mDeviceManagement.GetClone();

                    for (int x = 0; x < deviceStatus.size(); x++) {
                        mBus.leaveSession(deviceStatus.get(x).getSessionid());
                        RoomHubDevice dev = new RoomHubDevice();

                        dev.setSessionid(deviceStatus.get(x).getSessionid());
                        Log.d(LOG_TAG, "DISCONNECT - removedSession : " + deviceStatus.get(x).getSessionid() + "  ");
                        RemoveDevice(dev,ReasonType.NETWORKCHANGE);
                    }

                    if (mBus != null && mBus.isConnected()) {
                        mBus.cancelWhoImplements(new String[]{INTERFACE_NAME});
                        mBus.unregisterSignalHandlers(this);
                        mBus.unregisterAboutListener(mListener);
                        mBus.disconnect();
                        mBus.release();
                        mBus = null;
                    }
                    // getLooper().quit();
                    // mBusHandler.sendEmptyMessage(BusHandler.RECONNECT);
                    break;
                }

            }

            default:
                break;
        }
    }

    private void AddDevice(final RoomHubDevice dev) {
        // Make sure RoomHubInterface did exist
        if(dev.getRoomHubInterface() == null) {
            return;
        }

        Log.d(LOG_TAG, "AddDevice: [" + dev.getUuid() + "]");
        dev.setSourceType(SourceType.ALLJOYN);
        mDeviceManagement.AddAlljoynDevice(dev);

        if(deviceListener != null) {
            //deviceListener.addDevice(dev.clone());
            deviceListener.addDevice(dev,ReasonType.ALLJOYN);
        }
//				Boolean exist = mDeviceManagement.CheckAlljoynDeviceInCloud(dev);
//				if (exist) {
//					AddDeviceResPack resPack = new AddDeviceResPack();
//					resPack.setDevice(dev.clone());
//
//					SetMSG(resPack, CommandKey.AddDeviceNotice);
//				} else if (nowWifi.contains("RoomHub")) {
//					AddDeviceResPack resPack = new AddDeviceResPack();
//					resPack.setDevice(dev.clone());
//
//					SetMSG(resPack, CommandKey.AddDeviceNotice);
//				}


    }

    private void RemoveDevice(final RoomHubDevice dev,ReasonType reason) {
        // TODO Auto-generated method stub
        dev.setSourceType(SourceType.ALLJOYN);
        final RoomHubDevice ret = mDeviceManagement.RemoveAlljoynDevice(dev);

        if(deviceListener != null && ret != null)
            deviceListener.removeDevice(ret.clone(), reason);
//					Boolean exist = mDeviceManagement.CheckAlljoynDeviceInCloud(dev);
//					if (exist) {
//						RemoveDeviceResPack resPack = new RemoveDeviceResPack();
//						resPack.setDevice(ret.clone());
//
//						SetMSG(resPack, CommandKey.RemoveDeviceNotice);
//					}


    }

    public RoomHubDeviceManagement getDeviceManagment()
    {
        return mDeviceManagement;

    }

    public RoomHubDeviceManagement getmDeviceManagement() {
        return mDeviceManagement;
    }

    public void setmDeviceManagement(RoomHubDeviceManagement mDeviceManagement) {
        this.mDeviceManagement = mDeviceManagement;
    }

    public RoomHubDeviceListener getRoomHubDeviceListener() {
        return deviceListener;
    }

    public void setRoomHubDeviceListener(RoomHubDeviceListener deviceListener) {
        this.deviceListener = deviceListener;
    }

    public RoomHubSignalListener getRoomHubSignalListener() {
        return signalListener;
    }

    public void setRoomHubSignalListener(RoomHubSignalListener signalListener) {
        this.signalListener = signalListener;
    }

    public HomeApplianceSignalListener getHomeApplianceSignalListener() {
        return homeApplianceSignalListener;
    }

    public void setHomeApplianceSignalListener(HomeApplianceSignalListener homeApplianceSignalListener) {
        this.homeApplianceSignalListener = homeApplianceSignalListener;
    }

    private AlljoynAboutData CreateAlljoynAboutData(Map<String, Variant> aboutData) throws BusException {
        AlljoynAboutData alljoynAboutData = new AlljoynAboutData();
        //Map<String, Object> fromVariantMap = TransportUtil.fromVariantMap(aboutData);
        //alljoynAboutData.setDeviceId((String) fromVariantMap.get(AboutKeys.ABOUT_DEVICE_ID));

        alljoynAboutData.setDeviceId(aboutData.get(AboutKeys.ABOUT_DEVICE_ID).getObject(String.class));
        alljoynAboutData.setAppName(aboutData.get(AboutKeys.ABOUT_APP_NAME).getObject(String.class));
        if(aboutData.get("state_file") != null) {
            alljoynAboutData.setStateFile(aboutData.get("state_file").getObject(String.class));
        }
        alljoynAboutData.setDescription(aboutData.get(AboutKeys.ABOUT_DESCRIPTION).getObject(String.class));
        alljoynAboutData.setDateOfManufacture(aboutData.get(AboutKeys.ABOUT_DATE_OF_MANUFACTURE).getObject(String.class));

        if(aboutData.get("connect_cmd") != null) {
            alljoynAboutData.setConnectCmd(aboutData.get("connect_cmd").getObject(String.class));
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(aboutData.get(AboutKeys.ABOUT_APP_ID).getObject(byte[].class));
        alljoynAboutData.setAppId(new UUID(byteBuffer.getLong(), byteBuffer.getLong()).toString());
        alljoynAboutData.setManufacturer(aboutData.get(AboutKeys.ABOUT_MANUFACTURER).getObject(String.class));
        alljoynAboutData.setSoftwareVersion(aboutData.get(AboutKeys.ABOUT_SOFTWARE_VERSION).getObject(String.class));
        alljoynAboutData.setHardwareVersion(aboutData.get(AboutKeys.ABOUT_HARDWARE_VERSION).getObject(String.class));

        alljoynAboutData.setSupportedLanguages(aboutData.get(AboutKeys.ABOUT_SUPPORTED_LANGUAGES).getObject(String[].class));
        if(aboutData.get("error_file") != null) {
            alljoynAboutData.setErrorFile(aboutData.get("error_file").getObject(String.class));
        }
        alljoynAboutData.setAJSoftwareVersion(aboutData.get(AboutKeys.ABOUT_AJ_SOFTWARE_VERSION).getObject(String.class));
        if(aboutData.get("offboard_cmd") != null) {
            alljoynAboutData.setOffboardCmd(aboutData.get("offboard_cmd").getObject(String.class));
        }
        alljoynAboutData.setModelNumber(aboutData.get(AboutKeys.ABOUT_MODEL_NUMBER).getObject(String.class));

        if(aboutData.get("scan_cmd") != null) {
            alljoynAboutData.setScanCmd(aboutData.get("scan_cmd").getObject(String.class));
        }
        alljoynAboutData.setDeviceName(aboutData.get(AboutKeys.ABOUT_DEVICE_NAME).getObject(String.class));
        alljoynAboutData.setSupportUrl(aboutData.get(AboutKeys.ABOUT_SUPPORT_URL).getObject(String.class));
        if(aboutData.get("Daemonrealm") != null) {
            alljoynAboutData.setDaemonrealm(aboutData.get("Daemonrealm").getObject(String.class));
        }
        if(aboutData.get("scan_file") != null) {
            alljoynAboutData.setScanFile(aboutData.get("scan_file").getObject(String.class));
        }

        if(aboutData.get("configure_cmd") != null) {
            alljoynAboutData.setConfigureCmd(aboutData.get("configure_cmd").getObject(String.class));
        }
        alljoynAboutData.setDefaultLanguage(aboutData.get(AboutKeys.ABOUT_DEFAULT_LANGUAGE).getObject(String.class));
        if(aboutData.get("Passcode") != null) {
            alljoynAboutData.setPasscode(aboutData.get("Passcode").getObject(String.class));
        }
        return alljoynAboutData;
    }
}
