package com.quantatw.myapplication;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.quanta.hcbiapi.Adam4055;
import com.quanta.hcbiapi.Rs485Event;
import com.quanta.hcbiapi.Rs485EventListener;
import com.quanta.hcbiapi.Rs485P0;
import com.quanta.hcbiapi.Rs485P1;
import com.quantatw.myapplication.information_delivery.infoView;
import com.quantatw.myapplication.information_delivery.connection.GetCloudData;
import com.quantatw.myapplication.allseen.LSFLightingController;
import com.quantatw.myapplication.information_delivery.sqlite.DBHelper;
import com.quantatw.myapplication.line_bot.QHomeLineService;
import com.quantatw.myapplication.line_bot.SnapshotLineActivity;
import com.quantatw.myapplication.voiceAssistant.VoiceAssistant;
//import com.quantatw.roomhub.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.asset.manager.FANManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.ui.AbstractRoomHubActivity;
import com.quantatw.roomhub.ui.RoomHubApplication;
import com.quantatw.roomhub.ui.RoomHubMainPage;
import com.quantatw.roomhub.utils.FANDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.CloudDevice;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import android.content.SharedPreferences;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
//import com.quantatw.myapplication.voiceAssistant.KeywordDetect;

public class MainActivity extends AbstractRoomHubActivity implements Rs485EventListener,
                                                                     RoomHubChangeListener,
                                                                     Rs485EventDialogFragment.NoticeDialogListener,
                                                                     VoiceAssistant.ICommandHandler {
    private static final int SCREEN_SAVE_TIMEOUT = 180000; //millisecond

    private static final int MESSAGE_ADD_DEVICE             =100;
    private static final int MESSAGE_REMOVE_DEVICE          =101;
    private static final int MESSAGE_UPDATE_DEVICE          =102;
    private static final int MESSAGE_UPDATE_ROOMHUB_DATA    =103;
    private static final int MESSAGE_SET_COMMAND            =104;
    private static final int MESSAGE_UPGRADE_STATUS_CHANGE  =105;
    private static final int MESSAGE_FAN_ASSET_INFO_CHANGE  =106;
    private static final int MESSAGE_COMMAND_TIMEOUT        =107;
    private static final int MESSAGE_RETRY                  =108;

    public static final int HANDLER_EVENT_SYSTEM = 0; /* YouJun, 2016/06/02, system */
    public static final int HANDLER_EVENT_KEYWORD_DETECT = 1; /* YouJun, 2016/05/13, keyword detect */
    public static final int HANDLER_EVENT_VOICE_ASSISTANT = 2; /* YouJun, 2016/05/13, voice assistant */
    public static final int HANDLER_EVENT_LSF_CONTROLLER = 3; /* YouJun, 2016/05/13, lighting controller */
    public static final int HANDLER_EVENT_TCP_SERVICE = 4; /* YouJun, 2016/05/13, tcp service */
    public static final int HANDLER_EVENT_UNREAD_MESSAGE= 5; /* information delivery */
    public static final int HANDLER_EVENT_TELBOT_MESSAGE= 6; /* Telegram2 Service */
    public static final int HANDLER_EVENT_LINEBOT_MESSAGE=7; /* Line Service */
    public static final int HANDLER_EVENT_GPIO=8; /* GPiO Service */


    public static final String KEY_ROOMHUB_DATA= "roomhub_data";
    public static final String KEY_DEVICE_INFO= "dev_info";
    public static final String KEY_CMD_TYPE= "command_type";
    public static final String KEY_CMD_VALUE= "command_value";
    public static final String KEY_CMD_VALUE1= "command_value1";
    public  static boolean b_security=true;
    public static final String KEY_ELECTRIC_DATA= "electric_data";
    public static final String LOGTAG = "QHOME";

    public static String deviceID ="";
    public static String lastTopicID ="";

	ImageView dialView;
    ImageView hangupView;
    ImageView centerCtlView; // LiangBin add, 20160503
    TextView lastCallView; // LiangBin add, 20160511
    ImageView lastCallIconView; // LiangBin add, 20160512
    ImageView settingView;
    ImageView launcherView;
    ImageView rs485View;
    ImageView elevatorView;
    ImageView set_security;
//    ImageView addressView;
    RelativeLayout contactView;
    RelativeLayout RoomHubView;
    RoomHubData room_data;
    private ArrayList<RoomHubData> mRoomHubDevList=null;
    AudioManager audiomanager; // LiangBin add, 20160523
    private ImageButton setGasView;

    public void turnonSpeaker() {audiomanager.setParameters("route-fm=speaker");} // LiangBin add, 20160527
    public void turnoffSpeaker() {audiomanager.setParameters("route-fm=off");} // LiangBin add, 20160524
    public void killSophia() {
        voiceAssistant.destroy();
        voiceAssistant = null;
    }
    public void resetSophia() {
        if(voiceAssistant == null) {
            voiceAssistant = new VoiceAssistant();
            voiceAssistant.create();
            VoiceAssistant.register(this);
        }
    }
    /* ST, 2016/04/25, add ipcam view <-- */
    private ImageView ipcamView;
    private VideoView myVideoView;
    private ProgressDialog progressDialog;
    private MediaController mediaControls;
    private static boolean ipcamStatus = false;
    private int mVideo_width=0;
    private int mVideo_height=0;
    private boolean b_from_door_ = false; // LiangBin add, 20160530
    private static int _count = 0; // LiangBin add, 20160604
    private Timer continueMonitorOn_; // LiangBin add, 20160606
    private boolean need_monitor_ = true; // LiangBin add, 20160606
    /* ST, 2016/04/25, add ipcam view --> */
    public static String mCurUUID=null;
    private RelativeLayout energyLayout; /* YouJun, 2016/05/02 */
//    ListView listView;

    TextView tv1;
    boolean longClick;
    int contactIdx = 0;
    private ImageButton setSecurityView;
    private ImageButton buttonCloseAll;
    private MediaPlayer mp;

    public void setContactIndex(int index) {
        contactIdx=index;
        tv1.setText("" + _staff.get(contactIdx));
    }
    String _contacts = "";
    List<String> _staff = new ArrayList<String>(); // LiangBin add, 20160510
    List<String> _numberOrAddress = new ArrayList<String>(); // LiangBin add, 20160511

    Typeface weatherFont;
    TextView weatherIcon;
    TextView temperatureText;
    TextView H60_name;
    TextView H60_temp;
    TextView H60_hum;

    private BackgroundHandler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    TextView statusIcon; // LiangBin add, 20160422

    Context context;
    private RoomHubManager mRoomHubMgr=null;
    private MainActivity mRs;
    private boolean bForeground; /* YouJun, 2016/06/02 */

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg != null) {
                Log.d(LOGTAG, "handleMessage - msg: " + msg.toString());

                switch(msg.what) {
                    /* YouJun, 2016/06/02, system <-- */
                    case HANDLER_EVENT_SYSTEM:
                        if(msg.arg1 == 0) {
                            Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_SYSTEM - turn off display");
                            screenToggle(false);
                        }

                        break;
                    /* YouJun, 2016/06/02, system --> */

                    /* YouJun, 2016/05/13, keyword detect <-- */
                    /*case HANDLER_EVENT_KEYWORD_DETECT:
                        //Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_KEYWORD_DETECT");

                        if(msg.arg1 == 0) { // Create KeywordDetect
                            Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_KEYWORD_DETECT - Create KeywordDetect");
                            if(okSophia != null)
                                okSophia.create();
                        }
                        else if(msg.arg1 == 1) { // Start KeywordDetect
                            Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_KEYWORD_DETECT - Start KeywordDetect");
                            if(okSophia != null)
                                okSophia.startKeywordDetect();
                        }
                        else if(msg.arg1 == 2) { // Destroy KeywordDetect
                            Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_KEYWORD_DETECT - Destroy KeywordDetect");
                            if(okSophia != null) {
                                okSophia.destroy();
                            }
                        }

                        break;
                    */
                    /* YouJun, 2016/05/13, keyword detect --> */

                    case HANDLER_EVENT_TELBOT_MESSAGE:
                        Log.d(LOGTAG, "handleMessage - TELBOT MESSAGE");
                        if(!bForeground && msg.arg1 >= 0) {
                            Log.w(LOGTAG, "handleMessage - not in foreground");
                            break;
                        }

                        if (msg.arg1 == 33) { // Snapshot
                            Log.d(LOGTAG, "handleMessage - TELBOT -- snapshot >>>");
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, SnapshotActivity.class);
                            startActivity(intent);
                        }
                        if(msg.arg1 == 20) {
                            Log.d(LOGTAG, "handleMessage - TELBOT -- curtain close >>>");
                            Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO1, Adam4055.Level.LOW);
                            Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO2, Adam4055.Level.LOW);
                            Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO3, Adam4055.Level.HIGH);
                        }

                        if(msg.arg1 == 21) {
                            Log.d(LOGTAG, "handleMessage - TELBOT -- curtain open >>>");
                            Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO1, Adam4055.Level.HIGH);
                            Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO2, Adam4055.Level.LOW);
                            Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO3, Adam4055.Level.LOW);
                        }

                        if(msg.arg1 == 11) {
                            Log.d(LOGTAG, "handleMessage - TELBOT -- fanon >>>");
                            FANManager mFANM = (FANManager)mRoomHubMgr.getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.FAN);
                            if(mFANM != null && MainActivity.mCurUUID != null) {
                                String res = getString(R.string.cmd_h60_fan_main) + getString(R.string.res_action_opening);
                                voiceAssistant.createDialog(res + "...", 0);
                                voiceAssistant.speakOut(res, true);
                                mFANM.setKeyId(MainActivity.mCurUUID, FANDef.KEY_ID_POWER_TOGGLE);
//                                mFANM.setPowerStatus(MainActivity.mCurUUID, FANDef.KEY_ID_POWER_TOGGLE);
                            }
                        }
                        if(msg.arg1 == 10) {
                            Log.d(LOGTAG, "handleMessage - TELBOT -- fanoff >>>");
                            FANManager mFANM = (FANManager)mRoomHubMgr.getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.FAN);
                            if(mFANM != null && MainActivity.mCurUUID != null) {

                            String res = getString(R.string.cmd_h60_fan_main) + getString(R.string.res_action_closing);
                            voiceAssistant.createDialog(res + "...", 0);
                            voiceAssistant.speakOut(res, true);
                                mFANM.setKeyId(MainActivity.mCurUUID, FANDef.KEY_ID_POWER_TOGGLE);
//                                mFANM.setPowerStatus(MainActivity.mCurUUID, FANDef.KEY_ID_POWER_TOGGLE);
                            }
                        }
                        break;

                    case HANDLER_EVENT_GPIO:
                        if(msg.arg1 == 0)
                            Toast.makeText(MainActivity.this, "GPIO 0", Toast.LENGTH_LONG).show();
                        break;

                     case HANDLER_EVENT_LINEBOT_MESSAGE:
                        if(msg.arg1 == 33)
                            Toast.makeText(MainActivity.this, "snapshot!", Toast.LENGTH_LONG).show();
                        if(msg.arg1 == 20)
                            Toast.makeText(MainActivity.this, "curtain close!", Toast.LENGTH_LONG).show();
                        if(msg.arg1 == 21)
                            Toast.makeText(MainActivity.this, "curtain open!", Toast.LENGTH_LONG).show();
                        if(msg.arg1 == 11)
                            Toast.makeText(MainActivity.this, "fan on!", Toast.LENGTH_LONG).show();
                        if(msg.arg1 == 10)
                            Toast.makeText(MainActivity.this, "fan off!", Toast.LENGTH_LONG).show();
                         if(msg.arg1 == 40)
                             Toast.makeText(MainActivity.this, "light off!", Toast.LENGTH_LONG).show();
                         if(msg.arg1 == 41)
                             Toast.makeText(MainActivity.this, "light on!", Toast.LENGTH_LONG).show();
                         if(msg.arg1 == 91)
                             Toast.makeText(MainActivity.this, "Binding Reset Successful", Toast.LENGTH_LONG).show();
                         if(msg.arg1 == 90)
                             Toast.makeText(MainActivity.this, "No Device Binding", Toast.LENGTH_LONG).show();
                         if (msg.arg1 == 33) { // Snapshot
                             Log.d(LOGTAG, "handleMessage -- mqtt snapshot >>>");
                             Intent intent = new Intent();
                             intent.setClass(MainActivity.this, SnapshotLineActivity.class);
                             startActivity(intent);
                         }
                         if(msg.arg1 == 20) {
                             Log.d(LOGTAG, "handleMessage -- curtain close >>>");
                             Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO1, Adam4055.Level.LOW);
                             Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO2, Adam4055.Level.LOW);
                             Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO3, Adam4055.Level.HIGH);
                         }

                         if(msg.arg1 == 21) {
                             Log.d(LOGTAG, "handleMessage -- curtain open >>>");
                             Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO1, Adam4055.Level.HIGH);
                             Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO2, Adam4055.Level.LOW);
                             Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO3, Adam4055.Level.LOW);
                         }

                         if(msg.arg1 == 11) {
                             Log.d(LOGTAG, "handleMessage -- fanon >>>");
                             FANManager mFANM = (FANManager)mRoomHubMgr.getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.FAN);
                             if(mFANM != null && MainActivity.mCurUUID != null) {
                                 String res = getString(R.string.cmd_h60_fan_main) + getString(R.string.res_action_opening);
                                 voiceAssistant.createDialog(res + "...", 0);
                                 voiceAssistant.speakOut(res, true);
                                 mFANM.setKeyId(MainActivity.mCurUUID, FANDef.KEY_ID_POWER_TOGGLE);
//                                mFANM.setPowerStatus(MainActivity.mCurUUID, FANDef.KEY_ID_POWER_TOGGLE);
                             }
                         }
                         if(msg.arg1 == 10) {
                             Log.d(LOGTAG, "handleMessage - TELBOT -- fanoff >>>");
                             FANManager mFANM = (FANManager)mRoomHubMgr.getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.FAN);
                             if(mFANM != null && MainActivity.mCurUUID != null) {

                                 String res = getString(R.string.cmd_h60_fan_main) + getString(R.string.res_action_closing);
                                 voiceAssistant.createDialog(res + "...", 0);
                                 voiceAssistant.speakOut(res, true);
                                 mFANM.setKeyId(MainActivity.mCurUUID, FANDef.KEY_ID_POWER_TOGGLE);
//                                mFANM.setPowerStatus(MainActivity.mCurUUID, FANDef.KEY_ID_POWER_TOGGLE);
                             }
                         }
                         if (msg.arg1 == 40) { // Snapshot
                             Log.d(LOGTAG, "handleMessage -- mqtt light off >>>");
                         }
                         if (msg.arg1 == 41) { // Snapshot
                             Log.d(LOGTAG, "handleMessage -- mqtt light on >>>");
                         }

                        break;

                    /* YouJun, 2016/05/13, voice assistant <-- */
                    case HANDLER_EVENT_VOICE_ASSISTANT:
                        //Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_VOICE_ASSISTANT");
                        if(!bForeground && msg.arg1 >= 0) {
                            Log.w(LOGTAG, "handleMessage - HANDLER_EVENT_VOICE_ASSISTANT - not in foreground");
                            break;
                        }

                        if(msg.arg1 == 0) { // Start SpeechRecognition
                            Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_VOICE_ASSISTANT - Start SpeechRecognition");

                            if(voiceAssistant != null) {
                                voiceAssistant.startSpeechRecognition(msg.arg2 == 0 ? false : true);
                            }
                        }
                        else if(msg.arg1 == 1) { // Start VoiceAssistant
                            Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_VOICE_ASSISTANT - Start VoiceAssistant");

                            if(voiceAssistant != null) {
                                /*if(okSophia != null)
                                    okSophia.destroy();*/

                                voiceAssistant.startWelcome();
                            }
                        }
                        else if(msg.arg1 == 2) { // Dialog changed
                            Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_VOICE_ASSISTANT - Dialog changed");

                            if(voiceAssistant != null) {
                                if(msg.obj != null) {
                                    if(String.class.isAssignableFrom(msg.obj.getClass())) {
                                        List<String> dialogs = new ArrayList<String>(1);
                                        dialogs.add((String)msg.obj);

                                        voiceAssistant.updateDialog(dialogs, msg.arg2);
                                    }
                                    else if(List.class.isAssignableFrom(msg.obj.getClass())) {
                                        voiceAssistant.updateDialog((List<String>)msg.obj, msg.arg2);
                                    }
                                }
                            }
                        }
                        else if(msg.arg1 == -1) { // Stop VoiceAssistant
                            Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_VOICE_ASSISTANT - Stop VoiceAssistant");

                            if(voiceAssistant != null && voiceAssistant.getDialog() != null) {
                                Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_VOICE_ASSISTANT - call to dismiss");
                                voiceAssistant.dismissAllowingStateLoss();
                            }
                        }
                        else if(msg.arg1 == -2) { // Stop SpeechRecognition
                            Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_VOICE_ASSISTANT - Stop SpeechRecognition");

                            if(voiceAssistant != null) {
                                voiceAssistant.stopSpeechRecognition(false);
                            }
                        }

                        break;
                    /* YouJun, 2016/05/13, voice assistant --> */

                    /* YouJun, 2016/05/13, lighting controller <-- */
                    case HANDLER_EVENT_LSF_CONTROLLER:
                        //Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_LSF_CONTROLLER");

                        if(msg.arg1 == 0) { // Start LSFLightingController
                            Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_LSF_CONTROLLER - Start LSFLightingController");

                            if(lsfController != null)
                                lsfController.start();
                        }

                        break;
                    /* YouJun, 2016/05/13, lighting controller --> */

                    /* YouJun, 2016/06/17, tcp service <-- */
                    case HANDLER_EVENT_TCP_SERVICE:
                        Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_TCP_SERVICE");

                        if(msg.arg1 == 0) { // Start TcpService
                            Log.d(LOGTAG, "handleMessage - HANDLER_EVENT_TCP_SERVICE - Start TcpService");

                            if(mTcpService != null) {
                                if(!mTcpService.isRunning())
                                    mTcpService.startService();
                            }

                            Message heartbeat = Message.obtain();
                            heartbeat.what = HANDLER_EVENT_TCP_SERVICE;
                            heartbeat.arg1 = 0;
                            this.sendMessageDelayed(heartbeat, 5000);
                        }

                        break;
                    /* YouJun, 2016/06/17, tcp service --> */

                    /* information delivery */
                    case HANDLER_EVENT_UNREAD_MESSAGE:
						TextView textV1 = (TextView) findViewById(R.id.unread);
						String cnt =(String)msg.obj;
                        if (!cnt.equals("0"))
						    textV1.setText(cnt);
                        else
                            textV1.setText("");
                        break;
                    /* information delivery */

                    default:
                        break;
                }
            }

            super.handleMessage(msg);
        }
    };

    public Handler getHandler() {
        Log.d(LOGTAG, "getHandler");
        return mHandler;
    }

    //private LampsPageFragment lampsPage;
    private static LSFLightingController lsfController; /* YouJun, 2016/04/19, lighting control */
    //private static KeywordDetect okSophia; /* YouJun, 2016/05/13, keyword detect */
    private VoiceAssistant voiceAssistant; /* YouJun, 2016/05/13, voice assistant */
    private List<VoiceAssistant.VoiceCommand> mVoiceCommand; /* YouJun, 2016/05/13, voice assistant */
    private QHomeTcpService mTcpService; /* YouJun, 2016/06/17, tcp service */
    private QHomeTelegramService mTelegramService;
    private QHomeLineService mLineService;
    private QHomeGpioService mGpioService;



    // LiangBin add, global variable, 20160425
    private LiveHDBroadcastReceiver mLiveHDReceiver;
    static private boolean isSipRegistered = false;
    static private MainActivity instance = null;
    public static final boolean isInstanciated() {
        return instance != null;
    }
    public static final MainActivity instance() {
        if (instance != null)
            return instance;
        throw new RuntimeException("MainActivity not instantiated yet");
    }
    public boolean getSipStatus() {
        return isSipRegistered;
    }
    static public void resetSipStatus() {
        isSipRegistered = false;
    }
    static private String _myId;
    public String getUserId() {
        return _myId;
    }
    public String getAllContactText() {
        return _contacts;
    }
    // LiangBin add, 20160425

    /* YouJun, 2016/04/19, network monitor <-- */
    private static List<NetworkEventListener> networkListener = new ArrayList<NetworkEventListener>();

    private NetworkReceiver mNetworkReceiver;

    public class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOGTAG, "NetworkReceiver - onReceive");

            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            for(NetworkEventListener l : networkListener) {
                l.onNetworkChanged(networkInfo);
            }
        }
    }

    public static interface NetworkEventListener {
        void onNetworkChanged(NetworkInfo networkInfo);
    }
    /* YouJun, 2016/04/19, network monitor --> */

    //Implement NoticeDialogListener methods
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        dialNum(new CityPreference(MainActivity.this).getGuardPhoneNo());
    }

    @Override
    public void onDialogDismiss(DialogFragment dialog) {
        if (mp != null) {
            if (mp.isPlaying())
                mp.stop();
            mp.release();
            mp = null;
        }
    }

    private final class BackgroundHandler extends Handler {
        public BackgroundHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
//            log("message what="+msg.what);
            switch (msg.what) {
                case MESSAGE_ADD_DEVICE:
                    room_data=(RoomHubData) msg.getData().getParcelable(KEY_ROOMHUB_DATA);
                    updateH60_sensor((RoomHubData) msg.getData().getParcelable(KEY_ROOMHUB_DATA));
//                    FANMgr_AddDevice((ElectricData) msg.getData().getParcelable(KEY_ELECTRIC_DATA), (RoomHubData) msg.getData().getParcelable(KEY_ROOMHUB_DATA));
                    break;
                case MESSAGE_REMOVE_DEVICE:
//                    FANMgr_RemoveDevice((ElectricData) msg.getData().getParcelable(KEY_ELECTRIC_DATA), (RoomHubData) msg.getData().getParcelable(KEY_ROOMHUB_DATA));
                    break;
                case MESSAGE_UPDATE_DEVICE:
//                    FANMgr_UpdateDevice((ElectricData) msg.getData().getParcelable(KEY_ELECTRIC_DATA), (RoomHubData) msg.getData().getParcelable(KEY_ROOMHUB_DATA));
                    break;
                case MESSAGE_UPDATE_ROOMHUB_DATA:
//                    FANMgr_UpdateRoomHubData(msg.getData().getInt(KEY_CMD_TYPE), (RoomHubData) msg.getData().getParcelable(KEY_ROOMHUB_DATA));
                    break;
                case MESSAGE_UPGRADE_STATUS_CHANGE:
//                    FANMgr_UpgradeStats(msg.getData().getString(KEY_UUID), msg.getData().getBoolean(KEY_CMD_VALUE));
                    break;

//                    break;
            }
        }
    }



    private void updateH60_sensor(final RoomHubData data)
    {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub

                double sensor_temp=data.getSensorTemp();
                double sensor_hum=data.getSensorHumidity();

//        if (DEBUG)

//                mCurUUID=data.getElectricList().get(1).getElectricUuid();
//            Log.d(TAG, "UpdateSensorData temp=" + sensor_temp + " hum=" + sensor_hum);
                TextView tv_name = (TextView) findViewById(R.id.textView);
                TextView tv_temp = (TextView) findViewById(R.id.txt_sensor_temp);
                TextView tv_hum = (TextView) findViewById(R.id.txt_sensor_hum);
                tv_name.setText(data.getName());
                if(sensor_temp < -900)
                    tv_temp.setText("--°");
                else
                    tv_temp.setText(String.valueOf((int) sensor_temp) + "°");

                if(sensor_hum < -900)
                    tv_hum.setText("--%");
                else
                    tv_hum.setText(String.valueOf((int) sensor_hum) + "%");
            }
        });


//        H60_name.setText(data.getName());
//        H60_temp.setText(String.valueOf(data.getSensorTemp())+"C");
//        H60_hum.setText(String.valueOf(data.getSensorHumidity())+"%");
    }
    @Override
    public void addDevice(RoomHubData data) {
        Log.d(LOGTAG, "addDevice");
//        H60_name.setText(data.);
        Log.d(TAG, "addDevice: "+String.valueOf(data.getSensorTemp())+"C");
        Message msg=new Message();
        msg.what=MESSAGE_ADD_DEVICE;
        Bundle bundle=new Bundle();
//        bundle.putInt(KEY_CMD_TYPE, type);
        bundle.putParcelable(KEY_ROOMHUB_DATA, data);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
//        H60_temp.setText("C");
//        H60_temp.setText(String.valueOf(data.getSensorTemp())+"C");
//        H60_hum.setText(String.valueOf(data.getSensorHumidity())+"%");
    }

    @Override
    public void removeDevice(RoomHubData data) {
        Log.d(LOGTAG, "removeDevice");
    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        Log.d(LOGTAG, "UpdateRoomHubData");
        Message msg=new Message();
        msg.what=MESSAGE_UPDATE_ROOMHUB_DATA;
        Bundle bundle=new Bundle();
        bundle.putInt(KEY_CMD_TYPE, type);
        bundle.putParcelable(KEY_ROOMHUB_DATA, data);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
//        H60_name.setText(data.getName());
//        H60_temp.setText(String.valueOf(data.getSensorTemp())+"C");
//        H60_hum.setText(String.valueOf(data.getSensorHumidity())+"%");
//        H60_name=data.getName();
    }

//    @Override
//    public void UpdateRoomHubDeviceSeq(MicroLocationData locationData) {
//        Log.d(LOGTAG, "UpdateRoomHubDeviceSeq");
//    }

    @Override
    public void UpdateDeviceShareUser(CloudDevice device) {
        Log.d(LOGTAG, "UpdateDeviceShareUser");
    }

    @Override
    public void UpgradeStatus(String uuid, boolean is_upgrade) {

    }

    final String TAG = "BridgeToLiveHD";
    final String LIVEHD_PACKAGE_NAME = "com.quanta.livehd_v2";
    final String LIVEHD_LAUNCH_ACTIVITY = "LinphoneLauncherActivity";
    final String LIVEHD_DIAL_ACTIVITY = "LinphoneActivity";
    final String LIVEHD_DEFINED_ACTION = "com.quanta.livehd_v2";
    final String EMP_DEFINED_ACTION = "com.touchlife.ehome";
    final String LIVEHD_PROPERTY_TABLE = "LiveHDPropertyTable";
    final String LIVEHD_AUTHORITY = "com.quanta.livehd_v2.utils.LiveHDContentProvider";
    final String LIVEHD_SERVICE = "com.quanta.livehd_v2.LivehdService";
    private DialogInterface.OnClickListener listenerAccept;

    /* information delivery */
    public final static String infoComm= "infocomm";
    public final static String infoPerson= "infoperson";
    public final static String infoFood= "infofood";
    public final static String infoCountry= "infocountry";
    public Handler info_handle;
    public GetCloudData getdata;
    /* information delivery */	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        instance = this; // LiangBin add, 20160425
        initNetworkMonitoring(); /* YouJun, 2016/04/19, network monitor */
        if(!isLiveHDRunning() && isLiveHDInstalled()) {
            Log.d(TAG,"isSipRegistered is false, need to launchRawLiveHD ..");
            isSipRegistered = false; // LiangBin add, 20160608
            launchRawLiveHD(); // LiangBin add, 20160604
            progressDialog = ProgressDialog.show(MainActivity.this, null, null);
            progressDialog.setContentView(R.layout.progress_layout);
        }
        initLiveHDNotify(); // LiangBin add, 20160425

        SharedPreferences prefs = getSharedPreferences("DID_PREFS", MODE_PRIVATE);
        MainActivity.deviceID=prefs.getString("deviceid", "hcbi/rock01");

        //<<< LiangBin add, copy raw file
        final String[] str_song_name = new String[] { "ringback.wav", "oldphone_mono.wav" };
        final int[] mSongs = new int[] { R.raw.ringback, R.raw.oldphone_mono };
        for (int i = 0; i < mSongs.length; i++) {
            try {
                String path = "/data/data/com.quantatw.myapplication/files";
                java.io.File dir = new java.io.File(path);
                if (dir.mkdirs() || dir.isDirectory()) {
                    copyRaw2Card(mSongs[i], path + java.io.File.separator + str_song_name[i]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //>>> LiangBin add, 20160630
        setContentView(R.layout.activity_main);

        /* YouJun, 2016/05/13, voice assistant <-- */
        voiceAssistant = new VoiceAssistant();
        voiceAssistant.create();
        VoiceAssistant.register(this);
        /* YouJun, 2016/05/13, voice assistant --> */

        mTcpService = new QHomeTcpService(this); /* YouJun, 2016/06/17, tcp service */
        mTelegramService = new QHomeTelegramService(this);
        mLineService = new QHomeLineService(this);
        mGpioService = new QHomeGpioService(this);
        mGpioService.startService();

        takeSnapShots();

        getContacts(); // Liangbin add, get contacts from LiveHD, 20160506

        RoomHubView =(RelativeLayout)findViewById(R.id.RoomHubView);
        RoomHubView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, RoomHubMainPage.class);
                startActivity(intent);

            }

        });
        centerCtlView = (ImageView) findViewById(R.id.dial_center_control_icon);
        centerCtlView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //String num = "t22";
                dialNum(new CityPreference(MainActivity.this).getGuardPhoneNo());
                //Toast.makeText(MainActivity.this, "dial to center control is not ready! " + num, Toast.LENGTH_LONG).show();
            }

        });

        setGasView = (ImageButton) findViewById(R.id.close_gas_icon);
        setGasView.setOnClickListener(new View.OnClickListener() {
                                          public void onClick(View v) {
                                              // TODO for debug
                                              Intent intent = new Intent();
                                              intent.setClass(MainActivity.this, SnapshotActivity.class);
                                              startActivity(intent);

                                          }
                                      });



        setSecurityView = (ImageButton) findViewById(R.id.set_security_icon);
        CityPreference pref = new CityPreference(MainActivity.this);
        boolean securityEnabled = pref.getSecurityEnabled();
        if (securityEnabled) {
            setSecurityView.setImageResource(R.drawable.set_security_icon);
        } else {
            setSecurityView.setImageResource(R.drawable.set_security_icon_off);
        }
        setSecurityView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                // For Test FAN on off Hans
//                MainActivity.b_security=!MainActivity.b_security;
//                FANManager mFANM=getFANManager();
//
//                if(mFANM!=null)
//                {
//                    if(MainActivity.mCurUUID!=null) {
//                        mFANM.setPowerStatus(MainActivity.mCurUUID, FANDef.KEY_ID_POWER_TOGGLE);
//                    }
//                }

                CityPreference pref = new CityPreference(MainActivity.this);
                boolean securityEnabled = pref.getSecurityEnabled();
                if (securityEnabled) {
                    setSecurityView.setImageResource(R.drawable.set_security_icon_off);
                    securityEnabled = false;
                } else {
                    setSecurityView.setImageResource(R.drawable.set_security_icon);
                    securityEnabled = true;
                }
                pref.setSecurityEnabled(securityEnabled);
            }
        });

        buttonCloseAll = (ImageButton) findViewById(R.id.button_border);
        buttonCloseAll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //Close a light
                Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO0, Adam4055.Level.LOW);
                //Close a curtain
                Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO1, Adam4055.Level.LOW);
                Rs485P1.getInstance().setGpo(Adam4055.Gpo.GPO3, Adam4055.Level.HIGH);
            }
        });


        settingView = (ImageView) findViewById(R.id.setting);
        settingView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SettingActivity.class);
                startActivity(intent);

            }

        });

        /* YouJun, 2016/05/18, long click event <-- */
        settingView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // temporary used to trigger VoiceAssistant
                /*if(okSophia != null)
                    okSophia.destroy();*/
                if(voiceAssistant != null)
                    voiceAssistant.startSpeechRecognition(true);

                /*if(mHandler != null) {
                    Log.d(LOGTAG, "setting long click event - start speechRecognition");

                    Message msg = Message.obtain();
                    msg.what = HANDLER_EVENT_VOICE_ASSISTANT;
                    msg.arg1 = 0;
                    msg.obj = "Start SpeechRecognition";

                    mHandler.sendMessage(msg);
                }*/
                return true;
            }
        });
        /* YouJun, 2016/05/18, long click event --> */

        launcherView = (ImageView) findViewById(R.id.image_global);
        launcherView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    //actions for setting
                    Intent intent = new Intent();
                    intent.setClassName("com.android.launcher", "com.android.launcher2.Launcher");
                    startActivity(intent);
                }

            });

        rs485View = (ImageView) findViewById(R.id.switch_control_icon);
        rs485View.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //actions for setting
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, rs485Activity.class);
                startActivity(intent);
            }

        });


        /* toDO disable lastCall first */

        lastCallView = (TextView) findViewById(R.id.last_call);
        lastCallView.setText("last call.. ");
        lastCallView.setTextSize(18);
        /*
        lastCallView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String to_go = lastCallView.getText().toString();
                String[] afterSpilt = to_go.split(",");
                if(afterSpilt.length >= 2)
                    dialNum(afterSpilt[1].replaceAll("\\s+",""));
            }
        });
        */
        lastCallIconView = (ImageView) findViewById(R.id.image_last_call);
        getCallLogs(); // LiangBin add, 20160512


        tv1 = (TextView) findViewById(R.id.contactTV1);
        tv1.setOnTouchListener(new View.OnTouchListener() {

            int downX, upX;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = MotionEventCompat.getActionMasked(event);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        longClick = false;
                        downX = (int)event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getEventTime() - event.getDownTime() > 500 && Math.abs((int)event.getX() - downX) < 100) {
                            longClick = true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (event.getEventTime() - event.getDownTime() > 500) {
                            Log.i("qhome","Long Pressed !!!");
                            contactOnLongpress();
                        } else {
                            upX = (int) event.getX();

                            if ( Math.abs(upX - downX) > 99 ) {
                                if (upX - downX > 100) {
                                    Log.i("qhome", "Swipe right"); // swipe right
                                    contactOnSwipeRight();
                                } else if (downX - upX > -100) {
                                    Log.i("qhome", "Swipe left"); // swipe left
                                    contactOnSwipeLeft();
                                    // swipe left
                                }
                            } else {
                                Log.i("qhome", "Click"); // Click Event
                                contactOnClick();
                            }
                        }
                }
                return false;
            }

            public void contactOnSwipeRight() {
                if (contactIdx<_staff.size()-1) {
                    contactIdx++;
                    //tv1.setText("" + contactValue[contactIdx]);
                    tv1.setText("" + _staff.get(contactIdx));
                }
                if(_staff.size()==0)
                    tv1.setText("" + "No contact..");
            }

            public void contactOnSwipeLeft() {
                if (contactIdx>0) {
                    contactIdx--;
                    //tv1.setText("" + contactValue[contactIdx]);
                    tv1.setText("" + _staff.get(contactIdx));
                }
                if(_staff.size()==0)
                    tv1.setText("" + "No contact..");

            }
            public void contactOnClick() {
                if(_staff.size()==0)
                {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ContactBookActivity.class);
                    startActivity(intent);
                }
                else {
                    dialNum(_numberOrAddress.get(contactIdx));
                }
            }

            public void contactOnLongpress() {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ContactBookActivity.class);
                startActivity(intent);
            }

        });

// for RelativeLaout
        contactView = (RelativeLayout) findViewById(R.id.DialLiveHDView);
        contactView.setOnTouchListener(new View.OnTouchListener() {

            int downX, upX;


            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = MotionEventCompat.getActionMasked(event);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        longClick = false;
                        downX = (int)event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getEventTime() - event.getDownTime() > 500 && Math.abs((int)event.getX() - downX) < 100) {
                            longClick = true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (event.getEventTime() - event.getDownTime() > 500) {
                            Log.i("qhome","Long Pressed !!!");
                            contactOnLongpress();
                        } else {
                            upX = (int) event.getX();

                            if ( Math.abs(upX - downX) > 99 ) {
                                if (upX - downX > 100) {
                                    Log.i("qhome", "Swipe right"); // swipe right
                                    contactOnSwipeRight();
                                } else if (downX - upX > -100) {
                                    Log.i("qhome", "Swipe left"); // swipe left
                                    contactOnSwipeLeft();
                                    // swipe left
                                }
                            } else {
                                Log.i("qhome", "Click"); // Click Event
                                contactOnClick();
                            }
                        }
                }
                return false;
            }

            public void contactOnSwipeRight() {
                if (contactIdx<_staff.size()-1) {
                    contactIdx++;
                    //tv1.setText("" + contactValue[contactIdx]);
                    tv1.setText("" + _staff.get(contactIdx));
                }
                if(_staff.size()==0)
                    tv1.setText("" + "No contact..");
            }

            public void contactOnSwipeLeft() {
                if (contactIdx>0) {
                    contactIdx--;
                    //tv1.setText("" + contactValue[contactIdx]);
                    tv1.setText("" + _staff.get(contactIdx));
                }
                if(_staff.size()==0)
                    tv1.setText("" + "No contact..");

            }
            public void contactOnClick() {
                if(_staff.size()==0)
                {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ContactBookActivity.class);
                    startActivity(intent);
                }
                else {
                    dialNum(_numberOrAddress.get(contactIdx));
                }
            }

            public void contactOnLongpress() {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ContactBookActivity.class);
                startActivity(intent);
            }

        });

        hangupView = (ImageView) findViewById(R.id.image_hangup);
        hangupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        //Toast.makeText(MainActivity.this, "Not yet also..", Toast.LENGTH_LONG).show();
                        displayCallDialog("Call Message", "Please add number ..", false);
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:{
                        Toast.makeText(MainActivity.this, "Please install LiveHD first2!", Toast.LENGTH_LONG).show();
                        break;
                    }
                }
                return true;
            }
        });

        audiomanager= (AudioManager) getSystemService(AUDIO_SERVICE); // LiangBin add, 20160523
        elevatorView = (ImageView) findViewById(R.id.elevator_icon);
        elevatorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        //displayAnswerDoorDialog("Door phone", "Please answer door call ..", false);
                        //Toast.makeText(MainActivity.this, "Door phone call is not yet now..", Toast.LENGTH_LONG).show();
                        //displayCallDialog("Call Message", "Please add number ..", false);
                        //dialFromDoor();
                        //audiomanager.setParameters("route-fm=speaker"); // LiangBin add, 20160523
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, CameraViewActivity.class);
                        Bundle b = new Bundle();
                        b.putBoolean("auto_answer", false);
                        intent.putExtras(b);
                        startActivity(intent);
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:{
                        Toast.makeText(MainActivity.this, "Please install LiveHD first2!", Toast.LENGTH_LONG).show();
                        break;
                    }
                }
                return true;
            }
        });

        /* ST, 2016/04/25, add ipcam view <-- */
        ipcamView = (ImageView) findViewById(R.id.real_time_video_icon);
        myVideoView = (VideoView) findViewById(R.id.videoView);
//        myVideoView.getMeasuredWidth()

        myVideoView.setBackgroundResource(R.drawable.house);

        ipcamView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mUtil.telbot_msg(deviceID+" Got A RTSP request");
                openRealTimeVideo();
            }
        });
        /* ST, 2016/04/25, add ipcam view --> */

        weatherFont = Typeface.createFromAsset(getAssets(), "weather.ttf");
        weatherIcon = (TextView)findViewById(R.id.image_weather);
        weatherIcon.setTypeface(weatherFont);

        temperatureText = (TextView)findViewById(R.id.temperature);

        statusIcon = (TextView)findViewById(R.id.image_status);
        if(!isSipRegistered) {
            Log.d(TAG,"isSipRegistered: " + isSipRegistered);
            //getInfo(); // LiangBin add, 20160425, 20160427
            //<<< LiangBin add, get info counter
            final Timer t = new Timer();
            t.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    if(isSipRegistered || _count++ > 10 || !isLiveHDInstalled()) {
                        if(progressDialog!=null) progressDialog.dismiss(); // LiangBin add, 20160606
                        t.cancel();
                    }
                    if(!isSipRegistered)
                        getInfo();
                    Log.d(TAG,"isSipRegistered 2: " + isSipRegistered);
                }
            }, 5000, 4000);
            //>>> LiangBin add, 20160604
        }
        else {
            setLiveHDStat(_myId, isSipRegistered);
            enableDebug(true); // LiangBin add, 20160602
        }
        if(!isLiveHDInstalled()) {
            statusIcon.setText("Nonexistent LiveHD");
            statusIcon.setTextColor(Color.GRAY);
            statusIcon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.led_disconnected, 0,0,0);
        }
        context = getApplicationContext();
        updateWeatherData(new CityPreference(this).getCity());

        /* YouJun, 2016/04/19, lighting control <-- */
        energyLayout = (RelativeLayout)findViewById(R.id.energy); //allseen lighting controller used temporarily
        energyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!lsfController.isControllerConnected()) {
                    Log.d(LOGTAG,"onCreate - energy is clicked - try to start controllerService");

                    ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                    lsfController.onNetworkChanged(networkInfo);
                    //lsfController.restart();
                }

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LampsPageActivity.class);
                startActivity(intent);
            }
        });

        if(lsfController == null) {
            lsfController = new LSFLightingController(this);
            lsfController.start();
        }
        /* YouJun, 2016/04/19, lighting control --> */

        /* YouJun, 2016/05/13, keyword detect <-- */
        //okSophia = new KeywordDetect(this);
        //okSophia.create();
        /* YouJun, 2016/05/13, keyword detect --> */

        // polling fire alarms
        String s="Debug-infos:";
        s += "\n OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
        s += "\n OS API Level: " + android.os.Build.VERSION.SDK_INT;
        s += "\n Device: " + android.os.Build.DEVICE;
        s += "\n Model (and Product): " + android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")";
        Log.i(TAG, "onCreate: Device info" + s);

        if(android.os.Build.DEVICE.equals("byt_t_crv2")) {
            Rs485P0.getInstance().startPollingRS485();
            Rs485P0.addRs485EventListener(this);
//            Rs485P1.getInstance().startPollingRS485();
//            Rs485P1.addRs485EventListener(this);
        }

        if(room_data!=null) {
            updateH60_sensor(room_data);
//            H60_name = (TextView) findViewById(R.id.textView);
//            H60_name.setText();
//            H60_temp = (TextView) findViewById(R.id.txt_sensor_temp);
//            H60_temp.setText("NA");
//            H60_hum = (TextView) findViewById(R.id.txt_sensor_hum);
//            H60_hum.setText("NA");
        }
        boolean isReady = ((RoomHubApplication)getApplication()).isServiceReady();
//        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());
        mBackgroundThread=new HandlerThread("MainHubScreen");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());
        mRs=this;
        if(isReady) {
          mRoomHubMgr=getRoomHubManager();
            mRoomHubMgr.registerRoomHubChange(this);
        }else
        {
            new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            mRoomHubMgr=getRoomHubManager();
                            mRoomHubMgr.registerRoomHubChange(mRs);
                        }
                    },
                    3000
            );

        }

        prepareVoiceCommand();
		
	/* information delivery */
        DBHelper dbHelper = new DBHelper(this.context);
        TextView textV1 = (TextView) findViewById(R.id.unread);
        String cnt =Integer.toString(dbHelper.getTableUnreadCount());
        if (!cnt.equals("0"))
            textV1.setText(cnt);
        else
            textV1.setText("");

        getdata = new GetCloudData(MainActivity.this);
		
        RelativeLayout infoView = (RelativeLayout) findViewById(R.id.infosend);
        infoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN: {

                        findViewById(R.id.mainActivity).setVisibility(View.INVISIBLE);
                        LayoutInflater inflater = getLayoutInflater();
                        View tmpView;
                        tmpView = inflater.inflate(R.layout.fragment_info_view, null);
                        getWindow().addContentView(tmpView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));

                        infoView fr = new infoView();
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fm.beginTransaction();
                        fragmentTransaction.replace(R.id.fraglinear, fr);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        break;
                    }
                }
                return true;
            }
        });

        /* information delivery */
    }

    @Override
    public void onRs485EventReceived(Rs485Event event) {
        Log.i(TAG, "onRs485EventReceived:" + event.toString());
        String userId = _myId.replaceAll("\\s+", "");

        if (event == Rs485Event.GasAlarm) {
            mHandler.post(new Runnable(){
                public void run(){
                    DialogFragment newFragment = Rs485EventDialogFragment.newInstance(getResources().getString(R.string.dialog_get_gas_alarm));
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.add(newFragment, null);
                    ft.commitAllowingStateLoss();
                }
            });
        }
        if (event == Rs485Event.DoorPhone) {
            Log.i(TAG, "onRs485EventReceived: Got door call");
            //<<< LiangBin add, for call forwarding
            continueMonitorOn_ = new Timer();
            continueMonitorOn_.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    //Log.i(TAG, "onRs485EventReceived: auto answer: " + b_from_door_);
                    //if(!b_from_door_)
                    //    answerDoorCall();
                    restartDoorMonitor(); // 0x79
                }
            }, 28500, 20000);
            //>>> LiangBin add, 20160729
            mHandler.post(new Runnable(){
                public void run(){
                    //audiomanager.setParameters("route-fm=speaker"); // LiangBin add, 20160523
                    screenToggle(true); // LiangBin add, 20160606
                    b_from_door_ = true; // LiangBin add, 20160530
                    dialFromDoor();
                }
            });
        }

        if (event == Rs485Event.DoorOpen) {
            CityPreference pref = new CityPreference(MainActivity.this);
            if (pref.getSecurityEnabled()) {
                if(userId != null && userId.length() > 0) {
                    try {
                        sendGcmMessage(userId, URLEncoder.encode("The door was opened", "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                playRingtone (1);
                mHandler.post(new Runnable() {
                    public void run() {
                        DialogFragment newFragment = Rs485EventDialogFragment.newInstance("Door was opened");
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.add(newFragment, null);
                        ft.commitAllowingStateLoss();
                    }
                });
            }
        }

        if (event == Rs485Event.HelpAlarm) {
            if(userId != null && userId.length() > 0) {
                try {
                    String msg="Got a help alarm";
                    String msgbot=deviceID+" "+msg;

                    sendGcmMessage(userId, URLEncoder.encode(msg, "UTF-8"));
                    mUtil.telbot_msg(msgbot);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            playRingtone (5);
            mHandler.post(new Runnable(){
                public void run(){
                    DialogFragment newFragment = Rs485EventDialogFragment.newInstance("Got a help alarm");
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.add(newFragment, null);
                    ft.commitAllowingStateLoss();
                }
            });
        }
    }

    private void playRingtone (final int loopingNumber) {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (mp != null) {
            if (mp.isPlaying())
                mp.stop();
            mp.release();
            mp = null;
        }

        mp = MediaPlayer.create(getApplicationContext(), notification);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            int n = 0;

            @Override
            public void onCompletion(MediaPlayer mp) {
                n++;
                if (n < loopingNumber) {
                    mp.start();
                }
            }
        });
    }

    private void sendGcmMessage(final String sender, final String msg)
    {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                HttpURLConnection httpCon = null;
                URL serverUrl = null;

                String result ="";

                try {
                    String GCMSendAddress = "http://rms03.vccloud.quantatw.com:88/apn/alert.php";
                    serverUrl = new URL(GCMSendAddress + "?from="+ sender + "&msg=" + msg);
                    Log.d(TAG, "Alert message url: " + serverUrl.toString());
                    httpCon = (HttpURLConnection) serverUrl.openConnection();
                    httpCon.setReadTimeout(5000);
                    httpCon.setConnectTimeout(3000);
                    httpCon.setRequestMethod("GET");

                    int status = httpCon.getResponseCode();
                    if (status == 200) {
                        result = "Send GCM nofication: " + msg;
                    } else {
                        result = "Send GCM nofication failure." + " Status: " + status;
                    }
                    Log.d(TAG, "HTTP Response: " + result);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (httpCon != null) {
                        httpCon.disconnect();
                    }
                }
                return result;
            }
            @Override
            protected void onPostExecute(String result) {
                Log.d(TAG, "HTTP Response: " + result);
            }

        }.execute(null, null, null);
    }

    @Override
    protected void onNewIntent (Intent intent)
    {
        Log.d(TAG, "onNewIntent()");
        setIntent(intent);

        Bundle bundle = getIntent().getExtras();
        if(bundle == null)
        {
            super.onNewIntent(intent);
            return;
        }

        TLAction action = TLAction.actionMapping(bundle.getInt("Action"));
        Log.d(TAG,"Received action:" + action.getActionName() + " from LiveHD");

        handleTLAction(action, "I", bundle);

        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        Log.d(LOGTAG, "onDestroy");

        super.onDestroy();

        try {
            getdata.CloudDisconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        unregisterReceiver(mNetworkReceiver);
        unregisterReceiver(mLiveHDReceiver); // LiangBin add, 20160425
        if (mBackgroundThread != null ) {
            mBackgroundThread.quit();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }

        if(mRoomHubMgr != null) {
            mRoomHubMgr.unRegisterRoomHubChange(this);
//            mRoomHubMgr.unRegisterElectricChange(this);
        }

        if(android.os.Build.DEVICE.equals("byt_t_crv2")) {
            Rs485P0.getInstance().stopPollingRS485();
            Rs485P0.removeRs485Listener(this);
//        Rs485P1.getInstance().stopPollingRS485();
//        Rs485P1.removeRs485Listener(this);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //Log.d(LOGTAG, "dispatchTouchEvent");

        if(event.getAction() == MotionEvent.ACTION_UP) {
            //Log.d(LOGTAG, "onTouchEvent - ACTION_UP");
            startScreenSaveTimer();
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //Log.i(LOGTAG, "onKeyUp");

        startScreenSaveTimer();
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onResume() {
        Log.d(LOGTAG, "onResume");

        super.onResume();
        bForeground = true; /* YouJun, 2016/06/02 */

        startScreenSaveTimer(); /* YouJun, 2016/06/02 */

        if(mRoomHubMgr!=null) {
            mRoomHubMgr.registerRoomHubChange(this);
            mRoomHubDevList = mRoomHubMgr.getRoomHubDataList(true);
//        if(DEBUG)
//            Log.d(TAG, "onResume dev_list count=" + mRoomHubDevList.size());
            if (mRoomHubDevList.size() > 0) {
                RoomHubData data = mRoomHubDevList.get(0);

                updateH60_sensor(data);


            }
        }
        updateWeatherData(new CityPreference(this).getCity());
        //getInfo();
        //setLiveHDStat(_myId, isSipRegistered);

        /* YouJun, 2016/05/13, keyword detect <-- */
        /*if(okSophia != null) {
            okSophia.startKeywordDetect();
        }*/
        CityPreference pref = new CityPreference(this);
        if(pref.getVoiceAssistantEnabled()) {
            if(voiceAssistant != null)
                voiceAssistant.startSpeechRecognition(false);

            if(pref.getVoiceAssistantTcpService()) {
                mTcpService.startService();

                Message heartbeat = Message.obtain();
                heartbeat.what = HANDLER_EVENT_TCP_SERVICE;
                heartbeat.arg1 = 0;
                mHandler.sendMessageDelayed(heartbeat, 5000);
            }
        }
        /* YouJun, 2016/05/13, keyword detect --> */
    }

    @Override
    protected void onPause() {
        Log.d(LOGTAG, "onPause");

        bForeground = false; /* YouJun, 2016/06/02 */
        stopScreenSaveTimer(); /* YouJun, 2016/06/02 */

        /* YouJun, 2016/05/13, voice assistant <-- */
        if(mHandler != null) {
            Message msg = Message.obtain();
            msg.what = HANDLER_EVENT_VOICE_ASSISTANT;
            msg.arg1 = -2;

            mHandler.sendMessage(msg);
        }
        /* YouJun, 2016/05/13, voice assistant --> */

        mTcpService.stopService();

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Log.d(LOGTAG, "onBackPressed");

        /* YouJun, 2016/05/13, keyword detect <-- */
        /*if(okSophia != null) {
            okSophia.stopKeywordDetect();
        }*/
        /* YouJun, 2016/05/13, keyword detect --> */

		/* information delivery */
        if (getFragmentManager().getBackStackEntryCount() >=1){
            getFragmentManager().popBackStack();
            View myView = findViewById(R.id.fraglinear);
            if (myView != null) {
                ViewGroup Parent = (ViewGroup) myView.getParent();
                if (Parent != null) {
                    Parent.removeView(myView);
                    findViewById(R.id.mainActivity).setVisibility(View.VISIBLE);
                }
                Handler handler = this.getApplication()==null?null:this.getHandler();
                DBHelper dbHelper = new DBHelper(this.context);
                int unread = dbHelper.getTableUnreadCount();
                Message m = new Message();
                String obj = Integer.toString(unread);
                m = handler.obtainMessage(5, obj);
                handler.sendMessage(m);

            }else{
                super.onBackPressed();
                finish();
            }

        }else{
            super.onBackPressed();
            finish();
        }
		/* information delivery */
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }

    /* YouJun, 2016/04/19, network monitor <-- */
    public static void addNetworkListener(NetworkEventListener listener) {
        Log.d(LOGTAG, "addNetworkListener");
        if (!networkListener.contains(listener)) {
            networkListener.add(listener);
        }
    }
    public static void removeNetworkListener(NetworkEventListener listener) {
        Log.d(LOGTAG, "removeNetworkListener");
        networkListener.remove(listener);
    }

    private void initNetworkMonitoring() {
        Log.i(LOGTAG, "initNetworkMonitoring");

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkReceiver = new NetworkReceiver();

        registerReceiver(mNetworkReceiver, filter);
    }

    private void initLiveHDNotify() {
        Log.i(LOGTAG, "initNetworkMonitoring");

        IntentFilter livehd = new IntentFilter();
        livehd.addAction(LIVEHD_PACKAGE_NAME);
        mLiveHDReceiver = new LiveHDBroadcastReceiver();

        registerReceiver(mLiveHDReceiver, livehd);
    }

    public static boolean isNetworkConnected(Context context) {
        Log.d(LOGTAG, "isNetworkConnected");

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        Log.d(LOGTAG, "isNetworkConnected return " + isConnected);

        return isConnected;
    }
    /* YouJun, 2016/04/19, network monitor --> */

    public LSFLightingController getLSFController() {
        Log.d(LOGTAG, "getLSFController");
        return lsfController;
    }

    /* YouJun, 2016/05/13, voice assistant <-- */
    public VoiceAssistant getVoiceAssistant() {
        //Log.d(LOGTAG, "getVoiceAssistant");
        return voiceAssistant;
    }

    private void prepareVoiceCommand() {
        Log.d(LOGTAG, "prepareVoiceCommand");

        mVoiceCommand = new ArrayList<VoiceAssistant.VoiceCommand>();

        VoiceAssistant.VoiceCommand voiceCmd1 = new VoiceAssistant.VoiceCommand(getString(R.string.cmd_weather_main), "");
        mVoiceCommand.add(voiceCmd1);

        VoiceAssistant.VoiceCommand voiceCmd2 = new VoiceAssistant.VoiceCommand(getString(R.string.cmd_time_main), "");
        mVoiceCommand.add(voiceCmd2);

        VoiceAssistant.VoiceCommand voiceCmd3 = new VoiceAssistant.VoiceCommand(getString(R.string.cmd_realTime_video_main), getString(R.string.cmd_action_open));
        mVoiceCommand.add(voiceCmd3);

        VoiceAssistant.VoiceCommand voiceCmd4 = new VoiceAssistant.VoiceCommand(getString(R.string.cmd_realTime_video_main), getString(R.string.cmd_action_close));
        mVoiceCommand.add(voiceCmd4);

        VoiceAssistant.VoiceCommand voiceCmd5 = new VoiceAssistant.VoiceCommand(getString(R.string.cmd_livehd_contacts_main), getString(R.string.cmd_action_open));
        mVoiceCommand.add(voiceCmd5);

        VoiceAssistant.VoiceCommand voiceCmd6 = new VoiceAssistant.VoiceCommand(getString(R.string.cmd_action_call), getString(R.string.cmd_livehd_guard_second));
        mVoiceCommand.add(voiceCmd6);

        VoiceAssistant.VoiceCommand voiceCmd7 = new VoiceAssistant.VoiceCommand(getString(R.string.cmd_action_call), "");
        mVoiceCommand.add(voiceCmd7);

        VoiceAssistant.VoiceCommand voiceCmd8 = new VoiceAssistant.VoiceCommand(getString(R.string.cmd_h60_fan_main), getString(R.string.cmd_action_turn_on));
        mVoiceCommand.add(voiceCmd8);

        VoiceAssistant.VoiceCommand voiceCmd9 = new VoiceAssistant.VoiceCommand(getString(R.string.cmd_h60_fan_main), getString(R.string.cmd_action_turn_off));
        mVoiceCommand.add(voiceCmd9);
    }

    @Override
    public List<VoiceAssistant.VoiceCommand> getCommandTable() {
        //Log.d(LOGTAG, "getCommandTable");

        if(mVoiceCommand == null)
            prepareVoiceCommand();

        return mVoiceCommand;
    }

    @Override
    public boolean commandHandler(String main, String second, List<String> result) {
        Log.d(LOGTAG, "commandHandler - main: " + main + ", second: " + second);

        if(voiceAssistant == null) {
            Log.w(LOGTAG, "commandHandler - no voiceAssistant");
            return false;
        }

        boolean ret = true;
        if(main.contentEquals(getString(R.string.cmd_weather_main))) {
            //Log.d(LOGTAG, "commandHandler - ask weather");

            StringBuffer res = new StringBuffer("");

            String weather = (String) weatherIcon.getText();
            if(weather.length() > 0) {
                if(weather.contentEquals(getString(R.string.weather_sunny)) || weather.contentEquals(getString(R.string.weather_clear_night))) {
                    //Log.d(LOGTAG, "commandHandler - ask weather - clear sky");
                    res.append(getString(R.string.res_weather_clearSky));
                }
                else if (weather.contentEquals(getString(R.string.weather_foggy))) {
                    //Log.d(LOGTAG, "commandHandler - ask weather - foggy");
                    res.append(getString(R.string.res_weather_foggy));
                }
                else if (weather.contentEquals(getString(R.string.weather_cloudy))) {
                    //Log.d(LOGTAG, "commandHandler - ask weather - cloudy");
                    res.append(getString(R.string.res_weather_cloudy));
                }
                else if (weather.contentEquals(getString(R.string.weather_rainy))) {
                    //Log.d(LOGTAG, "commandHandler - ask weather - rainy");
                    res.append(getString(R.string.res_weather_rainy));
                }
                else if (weather.contentEquals(getString(R.string.weather_snowy))) {
                    //Log.d(LOGTAG, "commandHandler - ask weather - snowy");
                    res.append(getString(R.string.res_weather_snowy));
                }
                else if (weather.contentEquals(getString(R.string.weather_thunder))) {
                    //Log.d(LOGTAG, "commandHandler - ask weather - thunder");
                    res.append(getString(R.string.res_weather_thunder));
                }
                else if (weather.contentEquals(getString(R.string.weather_drizzle))) {
                    //Log.d(LOGTAG, "commandHandler - ask weather - drizzle");
                    res.append(getString(R.string.res_weather_drizzle));
                }
            }

            String temperature = (String)temperatureText.getText();
            if(temperature.length() > 0) {
                res.append(" ");
                res.append(getString(R.string.res_weather_temp) + temperature.substring(0, temperature.indexOf(" ")) + getString(R.string.res_weather_degree));
            }

            if(res.length() == 0) {
                voiceAssistant.createDialog(getString(R.string.res_weather_unknown), 0);
                voiceAssistant.speakOut(getString(R.string.res_weather_unknown), true);
            }
            else {
                voiceAssistant.createDialog(res.toString(), 0);
                voiceAssistant.speakOut(res.toString(), true);
            }
        }
        else if(main.contentEquals(getString(R.string.cmd_time_main))) {
            //Log.d(LOGTAG, "commandHandler - ask time");

            String res;
            if(getString(R.string.voice_assistant_nationality).contentEquals("Taiwanese")) {
                Calendar calendar = Calendar.getInstance(Locale.TAIWAN);

                int month  = calendar.get(Calendar.MONTH);
                int day    = calendar.get(Calendar.DAY_OF_MONTH);
                int hour   = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                res = getString(R.string.res_time_now) + " " + String.valueOf(month) + getString(R.string.res_time_month) + String.valueOf(day) + getString(R.string.res_time_day)
                        + " " + String.valueOf(hour) + getString(R.string.res_time_hour) + String.valueOf(minute) + getString(R.string.res_time_min);
            }
            else {
                Calendar calendar = Calendar.getInstance(Locale.US);
                SimpleDateFormat fmt1 = new SimpleDateFormat("HH:mm");
                SimpleDateFormat fmt2 = new SimpleDateFormat("E MM/dd, yyy");
                res = getString(R.string.res_time_now) + fmt1.format(calendar.getTime()) + " on " + fmt2.format(calendar.getTime()) + ".";
            }
            voiceAssistant.createDialog(res, 0);
            voiceAssistant.speakOut(res, true);
        }
        else if(main.contentEquals(getString(R.string.cmd_realTime_video_main))
                && second.contentEquals(getString(R.string.cmd_action_open))) {
            //Log.d(LOGTAG, "commandHandler - open real time video");

            if(ipcamStatus) {
                String res = getString(R.string.cmd_realTime_video_main)+getString(R.string.res_action_opened);
                voiceAssistant.createDialog(res, 0);
                voiceAssistant.speakOut(res, true);
            }
            else {
                String res = getString(R.string.cmd_realTime_video_main)+getString(R.string.res_action_opening);
                voiceAssistant.createDialog(res+"...", 0);
                voiceAssistant.speakOut(res, true);

                if(mHandler != null) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            openRealTimeVideo();
                        }
                    }, 500);
                }
            }
        }
        else if(main.contentEquals(getString(R.string.cmd_realTime_video_main))
                && second.contentEquals(getString(R.string.cmd_action_close))) {
            //Log.d(LOGTAG, "commandHandler - close real time video");

            if(ipcamStatus) {
                String res = getString(R.string.cmd_realTime_video_main)+getString(R.string.res_action_closing);
                voiceAssistant.createDialog(res+"...", 0);
                voiceAssistant.speakOut(res, true);

                if(mHandler != null) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            openRealTimeVideo();
                        }
                    }, 500);
                }
            }
            else {
                String res = getString(R.string.cmd_realTime_video_main)+getString(R.string.res_action_closed);
                voiceAssistant.createDialog(res, 0);
                voiceAssistant.speakOut(res, true);
            }
        }
        else if(main.contentEquals(getString(R.string.cmd_livehd_contacts_main))
                && second.contentEquals(getString(R.string.cmd_action_open))) {
            //Log.d(LOGTAG, "commandHandler - LiveHD - open contacts list");

            String res = getString(R.string.cmd_livehd_contacts_main)+getString(R.string.res_action_opening);
            voiceAssistant.createDialog(res+"...", 0);
            voiceAssistant.speakOut(res, true);

            Intent intent = new Intent();
            intent.setClass(MainActivity.this, ContactBookActivity.class);
            startActivity(intent);
        }
        else if(main.contentEquals(getString(R.string.cmd_action_call))) {
            if(second.contentEquals(getString(R.string.cmd_livehd_guard_second))) {
                //Log.d(LOGTAG, "commandHandler - call to guard");

                String res = getString(R.string.res_action_calling)+getString(R.string.cmd_livehd_guard_second);
                voiceAssistant.createDialog(res+"...", 0);
                voiceAssistant.speakOut(res, true);

                dialNum(new CityPreference(MainActivity.this).getGuardPhoneNo());
                return true;
            }

            voiceAssistant.recognizeWordsByNumber(_numberOrAddress, result);
            Map<Integer, String> candidate = voiceAssistant.getCandidate();

            if(candidate.size() == 1) {
                String res = getString(R.string.res_action_calling) + candidate.values().toArray()[0];
                voiceAssistant.createDialog(res +"...", 0);
                voiceAssistant.speakOut(res, true);

                dialNum((String)candidate.values().toArray()[0]);
                return true;
            }

            voiceAssistant.recognizeWordsByString(_staff, result);
            voiceAssistant.recongizeWordsByVoice(_staff, result); // Notice: blocked function

            Map<Integer, String> possibility = voiceAssistant.getPossibility();
            candidate = voiceAssistant.getCandidate();

            if(candidate.size() == 1) {
                String res = getString(R.string.res_action_calling) + candidate.values().toArray()[0] + "...";
                voiceAssistant.createDialog(res, 0);
                voiceAssistant.speakOut(res, true);

                for(int i = 0; i < _staff.size(); i++) {
                    if(_staff.get(i).contentEquals((String)candidate.values().toArray()[0])) {
                        dialNum(_numberOrAddress.get(i));
                    }
                }
            }
            else if(candidate.size() > 1) {
                String prefix = getString(R.string.res_livehd_contacts_prefix);
                String multiple = getString(R.string.res_livehd_multi_possibility);
                String res = String.format(prefix+"%s"+multiple, String.valueOf(candidate.size()));

                res += getString(R.string.res_livehd_call_which);
                //voiceAssistant.createDialog(res, 0);

                List<String> candidateList = new ArrayList<String>(candidate.values());
                voiceAssistant.createDialog(candidateList, 1, this, "dialNumByName");

                voiceAssistant.speakOut(res, false);
            }
            else {
                if(possibility.size() == 0) {
                    Log.d(LOGTAG, "commandHandler - no possibility");

                    voiceAssistant.createDialog(getString(R.string.res_livehd_unknown), 0);
                    voiceAssistant.speakOut(getString(R.string.res_livehd_unknown), true);
                }
                else {
                    String prefix = getString(R.string.res_livehd_contacts_prefix);
                    String multiple = getString(R.string.res_livehd_multi_possibility);
                    String res = String.format(prefix+"%s"+multiple, String.valueOf(candidate.size()));

                    if(possibility.size() == 1) {
                        res += getString(R.string.res_livehd_call_check);
                        //voiceAssistant.createDialog(res, 0);

                        List<String> possibilityList = new ArrayList<String>(possibility.values());
                        voiceAssistant.createDialog(possibilityList, 1, this, "dialNumByName");

                        voiceAssistant.speakOut(res, false);
                    }
                    else {
                        res += getString(R.string.res_livehd_call_which);
                        //voiceAssistant.createDialog(res, 0);

                        List<String> possibilityList = new ArrayList<String>(candidate.values());
                        voiceAssistant.createDialog(possibilityList, 1, this, "dialNumByName");

                        voiceAssistant.speakOut(res, false);
                    }
                }
            }
        }
        else if(main.contentEquals(getString(R.string.cmd_h60_fan_main))
                && second.contentEquals(getString(R.string.cmd_action_turn_on))) {
            //Log.d(LOGTAG, "commandHandler - open fan");

            FANManager mFANM = (FANManager)mRoomHubMgr.getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.FAN);
            if(mFANM != null && MainActivity.mCurUUID != null) {
                String res = getString(R.string.cmd_h60_fan_main)+getString(R.string.res_action_opening);
                voiceAssistant.createDialog(res+"...", 0);
                voiceAssistant.speakOut(res, true);

                mFANM.setKeyId(MainActivity.mCurUUID, FANDef.KEY_ID_POWER_TOGGLE);

//                mFANM.setPowerStatus(MainActivity.mCurUUID, FANDef.KEY_ID_POWER_TOGGLE);
            }
            else {
                voiceAssistant.createDialog(getString(R.string.res_h60_fan_unkown), 0);
                voiceAssistant.speakOut(getString(R.string.res_h60_fan_unkown), true);
            }
        }
        else if(main.contentEquals(getString(R.string.cmd_h60_fan_main))
                && second.contentEquals(getString(R.string.cmd_action_turn_off))) {
            //Log.d(LOGTAG, "commandHandler - close fan");

            FANManager mFANM = (FANManager)mRoomHubMgr.getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.FAN);
            if(mFANM != null && MainActivity.mCurUUID != null) {
                String res = getString(R.string.cmd_h60_fan_main)+getString(R.string.res_action_closing);
                voiceAssistant.createDialog(res+"...", 0);
                voiceAssistant.speakOut(res, true);
                mFANM.setKeyId(MainActivity.mCurUUID, FANDef.KEY_ID_POWER_TOGGLE);
//                mFANM.setPowerStatus(MainActivity.mCurUUID, FANDef.KEY_ID_POWER_TOGGLE);
            }
            else {
                voiceAssistant.createDialog(getString(R.string.res_h60_fan_unkown), 0);
                voiceAssistant.speakOut(getString(R.string.res_h60_fan_unkown), true);
            }
        }

        return ret;
    }

    public void dialNumByName(String name) {
        Log.d(LOGTAG, "dialNumByName - name: " + name);

        for(int i = 0; i < _staff.size(); i++) {
            Log.d(LOGTAG, "dialNumByName - handle: " + _staff.get(i));

            if(_staff.get(i).contentEquals(name)) {
                dialNum(_numberOrAddress.get(i));
                return;
            }
        }
    }
    /* YouJun, 2016/05/13, voice assistant --> */

    private void openRealTimeVideo() {
        Log.d(LOGTAG, "openRealTimeVideo");
        /* ++TODO
            should implement media control out, change control place, ipcam selection */
        if (mediaControls == null) {
            mediaControls = new MediaController(MainActivity.this);
        }

        if (myVideoView == null) {
            // Find your VideoView in your video_main.xml layout
            myVideoView = (VideoView) findViewById(R.id.videoView);
            myVideoView.getLayoutParams().width=mVideo_width;
            myVideoView.getLayoutParams().height=mVideo_height;
            myVideoView.setBackgroundResource(R.drawable.house);
        }

        if (ipcamStatus) {
            myVideoView.stopPlayback();
            myVideoView.getLayoutParams().width=mVideo_width;
            myVideoView.getLayoutParams().height=mVideo_height;
            myVideoView.setBackgroundResource(R.drawable.house);
            ipcamStatus = false;

        } else {
            /* un-mark if you need a progress bar indication
            // Create a progressbar
            progressDialog = new ProgressDialog(MainActivity.this);
            // Set progressbar title
            //      progressDialog.setTitle("JavaCodeGeeks Android Video View Example");
            // Set progressbar message
            progressDialog.setMessage("Loading...");

            progressDialog.setCancelable(false);
            // Show progressbar
            progressDialog.show();*/

            mVideo_height=myVideoView.getHeight();
            mVideo_width=myVideoView.getWidth();
//            mVideo_height=294;
//            mVideo_width=4;
            myVideoView.setBackgroundResource(0);

            try {
                myVideoView.setMediaController(mediaControls);
                //myVideoView.setVideoURI(Uri.parse("rtsp://168.168.2.142:8554/av_streaming"));
//                myVideoView.setVideoURI(Uri.parse("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov"));
                myVideoView.setVideoURI(Uri.parse(new CityPreference(MainActivity.this).getRTSPip()));

                myVideoView.requestFocus();
                myVideoView.start();


                /*myVideoView.setOnPreparedListener(new OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mp) {
                        int ipcamPosition = 0;
                        progressDialog.dismiss();
                        myVideoView.seekTo(ipcamPosition);
                        if (ipcamPosition == 0) {
                            myVideoView.start();
                        } else {
                            myVideoView.pause();
                        }
                    }
                });*/
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            ipcamStatus = true;
        }
    }

    public void screenToggle(boolean on) {
        Log.d(LOGTAG, "screenToogle - on: " + on);

        Intent intent = new Intent("android.intent.action.SCREEN_TOGGLE");
        intent.putExtra("on", on);
        sendBroadcast(intent);
    }

    public void startScreenSaveTimer() {
        //Log.d(LOGTAG, "startScreenSaveTimer");

        if(mHandler != null) {
            mHandler.removeMessages(HANDLER_EVENT_SYSTEM);

            Message msg = Message.obtain();
            msg.what = HANDLER_EVENT_SYSTEM;
            msg.arg1 = 0;

            mHandler.sendMessageDelayed(msg, SCREEN_SAVE_TIMEOUT);
        }
    }

    public void stopScreenSaveTimer() {
        //Log.d(LOGTAG, "stopScreenSaveTimer");

        if(mHandler != null)
            mHandler.removeMessages(HANDLER_EVENT_SYSTEM);
    }

    private void handleTLAction(TLAction action, String type, Bundle bundle)
    {
        String tmp = "";
        String direction = "";
        boolean bIsEvent = true;

        switch(action)
        {
            case ACTION_NONE:
                break;
            case ACTION_LAUNCH_LIVEHD:
                tmp = "Launch LiveHD!!";
                direction = "-->";
                break;
            case ACTION_HANGUP_LIVEHD:
                tmp = "Hang up LiveHD!";
                direction = "-->";
                break;
            case ACTION_DOOR_CALL_READY:
                tmp = "Door call is ready!";
                direction = "<--";
            {
                answerDoorCall();
                //turnonSpeaker(); // LiangBin add, 20160603
                need_monitor_ = true; // LiangBin add, 20160608
                /*
                continueMonitorOn_ = new Timer();
                continueMonitorOn_.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if(need_monitor_)
                            restartDoorMonitor(); // 0x79
                    }
                }, 2000, 18000);
                */
            }
                break;
            case ACTION_DOOR_CALL_CANCEL:
                tmp = "Door call is cancelled or closed!";
                direction = "<--";
                turnoffSpeaker();
                stopDoorRing(); // 0x03
                hangupDoorCall(); // 0x78
                b_from_door_ = false;
                break;
            case ACTION_DOOR_CALL_FAILED:
                tmp = "Door call failed!";
                direction = "<--";
                {
                    b_from_door_ = false;
                    killSophia();
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, CameraViewActivity.class);
                    Bundle b = new Bundle();
                    b.putBoolean("auto_answer", false);
                    intent.putExtras(b);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
                break;
            case ACTION_GET_CALL_RESULT:
            {
                String local  = bundle.getString("LocalUrl");
                String remote = bundle.getString("RemoteUrl");
                //if(remote.length() == 0 && bFromDoorCall == false)
                //    remote = dialNumberEtx.getText().toString(); // LiangBin add, 20160311
                String callStatus  = bundle.getString("CallStatus");
                String sessionType = bundle.getString("SessionType");
                String duration    = bundle.getString("Duration");
                String callDirection   = bundle.getString("Direction");
                String snapshotPath   = bundle.getString("SnapshotPath", ""); // LiangBin add, 20160121
                Boolean connection = bundle.getBoolean("Connection");
                if(connection) {
                    tmp = sessionType + " call connected\n";
                    tmp += "Direction: "+ callDirection + "\n";
                    if(callDirection.contains("Incoming"))
                        tmp += "From: " + remote.substring(4, remote.indexOf('@')) + " ("+remote+")\n"; // LiangBin add, for Incoming call, 20151221
                    else
                        tmp += "From " + local + " to " + remote + " in " + duration + " seconds\n";
                    if(snapshotPath.length() > 0)
                        tmp += "Snapshot path: " + snapshotPath +"\n"; // LiangBin add, 20160121
                    tmp += "End reason: " + callStatus;
                    lastCallIconView.setImageResource(R.drawable.icon_out);
                }
                else {
                    tmp = sessionType + " call connect fail\n";
                    tmp += "Direction: "+ callDirection + "\n";
                    if(callDirection.contains("Incoming"))
                        tmp += "From: " + remote.substring(4, remote.indexOf('@')) + " ("+remote+")\n"; // LiangBin add, for Incoming call, 20151221
                    else
                        tmp += "From " + local + " to " + remote + "\n";
                    tmp += "End reason: " + callStatus;
                    lastCallIconView.setImageResource(R.drawable.icon_in);
                }
                direction = "<--";
                java.text.DateFormat f = new java.text.SimpleDateFormat("MM/dd HH:mm:ss");
                java.util.Date d = new java.util.Date();
                if(remote.length()>4)
                    lastCallView.setText("last call: " + f.format(d) + ", " + remote.substring(4, remote.indexOf('@')));
                else
                    lastCallView.setText("last call: " + f.format(d) + ", " + remote);

                if(tmp.contains("Call terminated, user not found")){
                    //Log.d(TAG, "door: Call terminated");
                    //Intent intent = new Intent();
                    //intent.setClass(MainActivity.this, CameraViewActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    //startActivity(intent);
                }
                else if(tmp.contains("Call terminated") && sessionType.contains("VIDEO")/*&& remote.contains(local)*/ && b_from_door_){
                    Log.d(TAG, "door: Call terminated, b_from_door_=>" + b_from_door_);
                    turnoffSpeaker();
                    stopDoorRing(); // 0x03
                    hangupDoorCall(); // 0x78
                    b_from_door_ = false; // LiangBin add, 20160530
                    need_monitor_ = false; // LiangBin add, 20160606
                    if(continueMonitorOn_!=null)continueMonitorOn_.cancel(); // LiangBin add, 20160606
                }

                if(tmp.contains("This is not valid house"))
                    Toast.makeText(MainActivity.this, _myId + " isn't house typed account caused door call failed..", Toast.LENGTH_LONG).show();
            }
            //dialNumberEtx.setText("");
            break;
            case ACTION_GET_SIPNAME:
                tmp = "Get info ["+ bundle.getString("name") +"="+ bundle.getString("value") +"] from LiveHD";
                direction = "-->";
                break;
            case ACTION_GET_SIPPASSWORD:
            {
                String password = "";
                for(int i = 0 ; i < bundle.getString("value").length() ; i++)
                    password += "*";
                tmp = "Get info ["+ bundle.getString("name") +"="+ password +"] from LiveHD";
                direction = "-->";
            }
            break;
            case ACTION_ANSWER_DOOR_CALL:
            {
                /*
                byte[] ANSWER_CALL= new byte[]{0x0f, 0x30, 0x32, 0x37, 0x31, 0x0d};
                try {
                    Rs485P0.getInstance().write(ANSWER_CALL);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
                */
                killSophia(); // LiangBin add, 20160622
                Timer t = new Timer();
                t.schedule(new java.util.TimerTask() {
                               @Override
                               public void run() {
                                    Intent intent = new Intent();
                                    intent.setClass(MainActivity.this, CameraViewActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    //t.cancel();
                               }
                }, 600);
                tmp = "Accept call from door phone and back to TL Launcher.";
                direction = "<--";
            }
                break;
            case ACTION_OPEN_DOOR:
                tmp = "Open door request from LiveHD.";
                direction = "<--";
                break;
            case ACTION_DIAL:
                tmp = "Call to " + (bundle.getString("dialName")==""?"Door-phone":bundle.getString("dialName")); // LiangBin modified, 20151224
                direction = "-->";
                break;
            case ACTION_ENABLE_DOOR:
                tmp = "Enable Door = " + bundle.getBoolean("showDoor");
                direction = "-->";
                break;
            case ACTION_ENABLE_DONOTDISTURB:
                tmp = "Set Do-Not-Disturb = " + bundle.getBoolean("showDND");
                direction = "-->";
                break;
            case ACTION_PUT_SIP_STATUS:
                Log.d(TAG, "ACTION_LAUNCH_LIVEHD, OK: ");
                String sipId = bundle.getString("RegStatus");
                tmp = "SIP status: " + bundle.getString("RegStatus");
                android.os.Message msg = new android.os.Message();
                if(tmp.contains("OK")) {
                    enableDebug(true); // LiangBin add, 20160602
                    isSipRegistered = true;
                    msg.what = 0;
                }
                else if(tmp.contains("Fail")) {
                    isSipRegistered = false;
                    msg.what = 1;
                }
                try {
                    if(SipLoginActivity.isInstanciated() && (isSipRegistered || SipLoginActivity.getRunCycle() > 3)) {
                        Log.d(TAG, "ACTION_PUT_SIP_STATUS, OK: " + 1);
                        SipLoginActivity.instance().mProvisionHandler.sendMessage(msg);
                    }
                }
                catch(Exception ex) {
                    //Log.e("AlarmReceiver", "onReceive - sendMessage to LinphoneActivity occur exception: " + ex.toString());
                }
                String[] myId = sipId.split("\\]");
                String[] myId2 = myId[1].split(",");
                _myId = myId2[0];
                setLiveHDStat(_myId, isSipRegistered);
                direction = "<--";
                break;
            case ACTION_AUTO_PROVISION:
                tmp = "Auto provision time: " + bundle.getString("autoProvTime");
                direction = "<--";
                break;
            case ACTION_PUT_CONTACTS:
                tmp = "Get contacts ["+ bundle.getString("GetContacts") + "] from LiveHD";
                _contacts = bundle.getString("GetContacts");
                _staff.clear();
                _numberOrAddress.clear();
                String[] afterSplit = _contacts.split(";");
                for(int i = 0; i < afterSplit.length; i++) {
                    String[] staff_ = afterSplit[i].split(",");
                    if(staff_[0].length() > 0) {
                        _staff.add(staff_[0]);
                        _numberOrAddress.add(staff_[1]); // LiangBin add, 20160511
                    }
                }
                if(_staff.size()>0)
                    tv1.setText("" + _staff.get(contactIdx));
                else
                    tv1.setText("" + "No contact..");
                direction = "<--";

                /* YouJun, 2016/05/20, voice assistant <-- */
                if(voiceAssistant != null)
                    voiceAssistant.prepareTargets(_staff);
                /* YouJun, 2016/05/20, voice assistant --> */

                break;
            case ACTION_PUT_CALLLOGS:
                Log.d(TAG, "Received LiveHD call logs action:" + bundle.getString("GetCallLogs"));
                String logs = bundle.getString("GetCallLogs");
                String[] logsSplit = logs.split(",");
                if(logsSplit.length >=4) {
                    if(logsSplit[3].contains("out"))
                        lastCallIconView.setImageResource(R.drawable.icon_out);
                    else
                        lastCallIconView.setImageResource(R.drawable.icon_in);
                    lastCallView.setText("last call: " + logsSplit[0] + ", " + (logsSplit[3].contains("out") ? logsSplit[2].substring(4, logsSplit[2].indexOf('@')):logsSplit[1].substring(4, logsSplit[1].indexOf('@'))));
                }
                break;
            case ACTION_FORCE_HANGUP_TLAPP:
                tmp = "Force hangup: Launcher <-> Doorphone.";
                direction = "<--";
                screenToggle(true);
                break;
            case ACTION_SET_DEBUG_MODE:
                tmp = "Debug Mode = " + bundle.getBoolean("enableDebug");
                direction = "-->";
                break;
            default:
                bIsEvent = false;
                break;
        }
        Log.d(TAG, "Finished LiveHD handleTLAction:" + tmp);
        /*
        if (bIsEvent) {
            addEventToList(new EventObject( Integer.toString((mEventHistoryAdapter.getCount() + 1)),
                    action.getActionName(), type, direction,
                    tmp,
                    dateFormat.format(new Date())));
        }*/
    }

    public void dialNum(String callee)
    {
        //if(isLiveHDRunning())
        {
            String dialNum = callee;//dialNumberEtx.getText().toString();
            if(dialNum.length() == 0)
            {
                Toast.makeText(this, "Please input numbers first", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_MAIN);

            ComponentName cn = new ComponentName(
                    LIVEHD_PACKAGE_NAME,
                    LIVEHD_PACKAGE_NAME + "." + LIVEHD_DIAL_ACTIVITY);

            // Please put Action parameter in the intent
            Bundle bundle = new Bundle();
            bundle.putInt("Action", TLAction.ACTION_DIAL.getActionType());
            //bundle.putString("dialname", dialNum);
            bundle.putString("dialName", dialNum); // LiangBin add, change argument style, 20151223
            bundle.putBoolean("fromDoor", false); // LiangBin add, for dialing form door, 20151223, 20151228

            intent.setComponent(cn);
            intent.putExtras(bundle);
            startActivity(intent);
            handleTLAction(TLAction.ACTION_DIAL, "I", bundle);
        }
    }

    public void displayAnswerDoorDialog(final String title,final String msg, final boolean needBack) {
        mHandler.post(new Runnable() {
            @Override
            public void run(){
                android.media.MediaPlayer ringTone = new android.media.MediaPlayer();
                try{
                    ringTone.setDataSource("/data/data/com.quantatw.myapplication/files/ringback.wav");
                    ringTone.prepare();
                    ringTone.start();
                }
                catch(java.io.IOException e){
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(title);
                builder.setMessage(msg);
                //final EditText input = new EditText(MainActivity.this);
                //input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                //builder.setView(input);
                builder.setPositiveButton("Answer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      /*
                      Intent intent = new Intent(InCallActivity.this, LinphoneActivity.class);
                      intent.putExtra("AddCall", true);
                      intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                      startActivity(intent);
                      */
                    }
                });
                if(needBack) {
                    builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //dialog.dismiss();
                            //pauseOrResumeCall();
                        }
                    });
                } else {
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                }

                final Dialog msgDialog = builder.create();
                msgDialog.show();
                ((AlertDialog) msgDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        msgDialog.dismiss();
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, CameraViewActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }
                });
            }
        });
    }
    
    public void displayCallDialog(final String title,final String msg, final boolean needBack) {
        mHandler.post(new Runnable() {
            @Override
            public void run(){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(title);
                builder.setMessage(msg);
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("Dial", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
				      /*
					  Intent intent = new Intent(InCallActivity.this, LinphoneActivity.class);
				      intent.putExtra("AddCall", true);
				      intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				      startActivity(intent);
				      */
                    }
                });
                if(needBack) {
                    builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //dialog.dismiss();
                            //pauseOrResumeCall();
                        }
                    });
                } else {
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                }

                final Dialog msgDialog = builder.create();
                msgDialog.show();
                ((AlertDialog) msgDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String callee = input.getText().toString();
                        if(callee.length() > 0) {
                            //LinphoneManager.getInstance().set3wayCallState(true);
                            msgDialog.dismiss();
                            dialNum(callee);
                        }
                    }
                });
            }
        });
    }

    private void updateWeatherData(final String city){
        new Thread(){
            public void run(){
                final JSONObject json = RemoteFetch.getJSON(context, city);
                if(json == null){
                    mHandler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(context,
                                    getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    mHandler.post(new Runnable(){
                        public void run(){
                            try {
                                JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                                JSONObject main = json.getJSONObject("main");
                                setWeatherIcon(details.getInt("id"),
                                        json.getJSONObject("sys").getLong("sunrise") * 1000,
                                        json.getJSONObject("sys").getLong("sunset") * 1000);
                                temperatureText.setText(
                                        String.format("%.0f", main.getDouble("temp"))+ " ℃");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }.start();
    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
                icon = getString(R.string.weather_sunny);
            } else {
                icon = getString(R.string.weather_clear_night);
            }
        } else {
            switch(id) {
                case 2 : icon = getString(R.string.weather_thunder);
                    break;
                case 3 : icon = getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = getString(R.string.weather_foggy);
                    break;
                case 8 : icon = getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = getString(R.string.weather_snowy);
                    break;
                case 5 : icon = getString(R.string.weather_rainy);
                    break;
            }
        }
        weatherIcon.setText(icon);
    }

    public void setLiveHDStat(String myId, boolean stat) {
        //statusIcon.setTypeface(weatherFont);
        if(stat) {
            statusIcon.setText(myId + ", online");
            statusIcon.setTextColor(Color.argb(255,108,191,17));
            statusIcon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.led_connected, 0, 0, 0);
        }
        else {
            statusIcon.setText(myId + ",offline");
            statusIcon.setTextColor(Color.GRAY);
            statusIcon.setCompoundDrawablesWithIntrinsicBounds(R.drawable.led_disconnected, 0,0,0);
        }
    }

    private boolean isLiveHDInstalled()
    {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(0);

        for (ApplicationInfo packageInfo : packages)
        {
            if(packageInfo.packageName.equals(LIVEHD_PACKAGE_NAME)) return true;
        }
        return false;
    }

    private boolean isLiveHDRunning() {

        ActivityManager mActivityManager = 	(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager
                .getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {

            String[] packageList = appProcessInfo.pkgList;
            for (String pkg : packageList) {
                if(pkg.contentEquals(LIVEHD_PACKAGE_NAME))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public void launchRawLiveHD() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        ComponentName cn = new ComponentName(
                LIVEHD_PACKAGE_NAME,
                LIVEHD_PACKAGE_NAME + "." + LIVEHD_LAUNCH_ACTIVITY);
        intent.setComponent(cn);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void getInfo()
    {
        if(isLiveHDRunning())
        {
            Intent intent = new Intent(LIVEHD_SERVICE);

            Bundle bundle = new Bundle();
            bundle.putInt("Action", TLAction.ACTION_GET_SIP_STATUS.getActionType());
            intent.putExtras(bundle);
            startService(intent);
        }
    }

    public void getCallLogs()
    {
        if(isLiveHDRunning())
        {
            Intent intent = new Intent(LIVEHD_SERVICE);

            Bundle bundle = new Bundle();
            bundle.putInt("Action", TLAction.ACTION_GET_CALLLOGS.getActionType());
            intent.putExtras(bundle);
            startService(intent);
        }
    }

    public void getContacts()
    {
        if(isLiveHDRunning())
        {
            Intent intent = new Intent(LIVEHD_SERVICE);

            Bundle bundle = new Bundle();
            bundle.putInt("Action", TLAction.ACTION_GET_CONTACTS.getActionType());
            intent.putExtras(bundle);
            startService(intent);
        }
    }

    public void addContact(String firstName, String lastName, String number)
    {
        if(isLiveHDRunning())
        {
            Intent intent = new Intent(LIVEHD_SERVICE);

            Bundle bundle = new Bundle();
            bundle.putInt("Action", TLAction.ACTION_ADD_CONTACT.getActionType());
            bundle.putString("ContactId1", firstName);
            bundle.putString("ContactId2", lastName);
            bundle.putString("ContactNum", number);
            intent.putExtras(bundle);
            startService(intent);
        }
    }

    public void deleteContact(String name)
    {
        if(isLiveHDRunning())
        {
            Intent intent = new Intent(LIVEHD_SERVICE);

            Bundle bundle = new Bundle();
            bundle.putInt("Action", TLAction.ACTION_DELETE_CONTACT.getActionType());
            bundle.putString("ContactId", name);
            intent.putExtras(bundle);
            startService(intent);
        }
    }

    public void enableDebug(boolean bDebugChk)
    {
        if(isLiveHDRunning())
        {
            Intent intent = new Intent(LIVEHD_SERVICE);

            Bundle bundle = new Bundle();
            bundle.putInt("Action", TLAction.ACTION_SET_DEBUG_MODE.getActionType());
            bundle.putBoolean("enableDebug", bDebugChk);

            intent.putExtras(bundle);
            startService(intent);
            handleTLAction(TLAction.ACTION_SET_DEBUG_MODE, "I", bundle);
        }
    }

    public void answerDoorCall()
    {
        // 0x02
        byte[] ANSWER_CALL = new byte[]{0x0f, 0x30, 0x32, 0x37, 0x31, 0x0d};
        try {
            Rs485P0.getInstance().write(ANSWER_CALL);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void stopDoorRing()
    {
        // 0x03
        byte[] RING_OFF = new byte[]{0x0f, 0x30, 0x33, 0x37, 0x32, 0x0d};
        try {
            Rs485P0.getInstance().write(RING_OFF);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void enableDoorMonitor()
    {
        // 0x06
        byte[] MONITOR_ON = new byte[]{0x0f, 0x30, 0x36, 0x37, 0x35, 0x0d};
        try {
            Rs485P0.getInstance().write(MONITOR_ON);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void restartDoorMonitor()
    {
        // 0x79
        byte[] MONITOR_ON = new byte[]{0x0f, 0x37, 0x39, 0x37, 0x46, 0x0d};
        try {
            Rs485P0.getInstance().write(MONITOR_ON);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void hangupDoorCall()
    {
        // 0x78
        byte[] DOOR_OFF = new byte[]{0x0f, 0x37, 0x38, 0x37, 0x45, 0x0d};
        try {
            Rs485P0.getInstance().write(DOOR_OFF);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void dialFromDoor()
    {
        if(isLiveHDRunning())
        {
			/*
			String dialNum = dialNumberEtx.getText().toString();
			if(dialNum.length() == 0)
			{
				Toast.makeText(this, "Please input numbers first", Toast.LENGTH_SHORT).show();
				return;
			}
			*/

            Intent intent = new Intent(Intent.ACTION_MAIN);

            ComponentName cn = new ComponentName(
                    LIVEHD_PACKAGE_NAME,
                    LIVEHD_PACKAGE_NAME + "." + LIVEHD_DIAL_ACTIVITY);

            // Please put Action parameter in the intent
            Bundle bundle = new Bundle();
            bundle.putInt("Action", TLAction.ACTION_DIAL.getActionType());
            //bundle.putString("dialname", dialNum);
            bundle.putString("dialName", ""); // LiangBin add, null number while door call, 20151224
            bundle.putBoolean("fromDoor", true);
            bundle.putString("displayName", "_小門口機");
            //bundle.putString("displayName", "_小门口机");
            bundle.putInt("videoin_width", 720);
            //bundle.putInt("videoin_height", 576);		//Media Player
            bundle.putInt("videoin_height", 528);		//TL Door-phone Call

            intent.setComponent(cn);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // LiangBin add, 20160713
            startActivity(intent);
            handleTLAction(TLAction.ACTION_DIAL, "I", bundle);
        }
    }

    private void copyRaw2Card(int id, String path) throws IOException {
        java.io.File f = new java.io.File(path);
        if(f.exists())
            return;
        java.io.InputStream in = getResources().openRawResource(id);
        java.io.FileOutputStream out = new java.io.FileOutputStream(path);
        byte[] buff = new byte[1024];
        int read = 0;
        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    //<<< LiangBin add,receive broadcast from LiveHD
    class LiveHDBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == LIVEHD_DEFINED_ACTION)
            {
                Bundle actionBundle = intent.getExtras();
                TLAction action = TLAction.actionMapping(actionBundle.getInt("Action"));
                if(action != null) {
                    Log.d(TAG, "Received LiveHD action:" + action.getActionName());
                    handleTLAction(action, "B", actionBundle);
                }
            }
        }

    }
    //>>> LiangBin add, 20160425

    public void takeSnapShots() {
    }
}
