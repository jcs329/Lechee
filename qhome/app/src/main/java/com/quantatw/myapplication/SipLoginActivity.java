package com.quantatw.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;


/**
 * A login screen that offers login via email/password.
 */
public class SipLoginActivity extends Activity {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    //private static final String[] DUMMY_CREDENTIALS = new String[]{
    //        "foo@example.com:hello", "bar@example.com:world"
    //};
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    final String TAG = "BridgeToLiveHD";
    final String LIVEHD_PACKAGE_NAME = "com.quanta.livehd_v2";
    final String LIVEHD_LAUNCH_ACTIVITY = "LinphoneLauncherActivity";
    final String LIVEHD_DIAL_ACTIVITY = "LinphoneActivity";
    final String LIVEHD_DEFINED_ACTION = "com.quanta.livehd_v2";
    final String EMP_DEFINED_ACTION = "com.touchlife.ehome";
    final String LIVEHD_PROPERTY_TABLE = "LiveHDPropertyTable";
    final String LIVEHD_AUTHORITY = "com.quanta.livehd_v2.utils.LiveHDContentProvider";
    final String LIVEHD_SERVICE = "com.quanta.livehd_v2.LivehdService";

    static private int _cnt = 0;
    static int getRunCycle() { return _cnt; }
    private EditText rmsAddrEtx;
    private ProgressDialog myProgress; // LiangBin add, 20160517
    private ListPopupWindow lpw;
    private String[] list;

    // UI references.
    //private EditText mUsernameView;
    //private EditText mPasswordView;
    //private View mProgressView;
    //private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sip_login);
        // Set up the login form.
        //mUsernameView = (EditText) findViewById(R.id.rmsId);
        //populateAutoComplete();
/*
        mPasswordView = (EditText) findViewById(R.id.rmsPwd);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
*/
        instance = this; // LiangBin add, 20160427
        //myProgress = new ProgressDialog(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog)); // LiangBin add, 20160517
        Button SignInButton = (Button) findViewById(R.id.email_sign_in_button);
        SignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText rmsIdEtx = (EditText) findViewById(R.id.rmsId);
                EditText rmsPwdEtx = (EditText) findViewById(R.id.rmsPwd);
                EditText rmsAddrEtx = (EditText) findViewById(R.id.rmsAddr);
                if(rmsIdEtx.getText().toString().length() == 0 || rmsPwdEtx.getText().toString().length() == 0 || rmsAddrEtx.getText().toString().length() == 0) {
                    Toast.makeText(SipLoginActivity.this, "Invalid data, please enter correctly.", Toast.LENGTH_SHORT).show();
                    return ;
                }
                //myProgress.setMessage("In progress..");
                myProgress = ProgressDialog.show(SipLoginActivity.this, null, null);
                myProgress.setContentView(R.layout.progress_layout);
                //myProgress.show(); // LiangBin add, 20160517
                attemptLogin();
            }
        });

        rmsAddrEtx = (EditText) findViewById(R.id.rmsAddr);
        rmsAddrEtx.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                // Check if touch point is in the area of the right button
                //Toast.makeText(SipLoginActivity.this, "click me..", Toast.LENGTH_LONG).show();
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getX() >= (v.getWidth() - rmsAddrEtx
                            .getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        lpw.show();
                        return true;
                    }
                }
                return false;
            }
        });

        list = new String[] { "192.168.0.102", "118.163.114.112", "rms03.vccloud.quantatw.com" };
        lpw = new ListPopupWindow(this);
        lpw.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, list));
        lpw.setAnchorView(rmsAddrEtx);
        lpw.setModal(true);
        lpw.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                //Toast.makeText(SipLoginActivity.this, "click me again..", Toast.LENGTH_LONG).show();
                String item = list[position];
                rmsAddrEtx.setText(item);
                lpw.dismiss();
            }
        });
        //mLoginFormView = findViewById(R.id.login_form);
        //mProgressView = findViewById(R.id.login_progress);
        /*Spinner spinner = (Spinner) findViewById(R.id.spinRmsAddr);

        // Spinner click listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                Object item = arg0.getItemAtPosition(arg2);
                if (item!=null) {
                    Toast.makeText(SipLoginActivity.this, item.toString(),
                            Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(SipLoginActivity.this, "Selected",
                        Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("168.168.2.102");
        categories.add("118.163.114.112");
        categories.add("rms03.vccloud.quantatw.com");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);*/
        getInfo(); // LiangBin add, 20160518
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent intent = new Intent(SipLoginActivity.this, SettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return false;
    }

    @Override
    protected void onNewIntent (Intent intent)
    {
        Log.d(TAG, "sip-onNewIntent()");
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


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // LiangBin add, update the procedure of launch LiveHD
        MainActivity.instance().resetSipStatus();
        _cnt = 0;
        killLiveHD();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        provisionLiveHD();
        final Timer t = new Timer();
        t.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                if(MainActivity.instance().getSipStatus() || _cnt++ > 2)
                    t.cancel();
                if(!MainActivity.instance().getSipStatus())
                    getInfo();
            }
        }, 4000, 3000);
        /*
        runOnUiThread(new Runnable(){
            public void run() {
                killLiveHD();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                provisionLiveHD();
                int cnt = 0;
                //MainActivity.instance().resetSipStatus();
                while (MainActivity.isInstanciated()) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    getInfo();// LiangBin add, 20160422
                    if(MainActivity.instance().getSipStatus()) {
                        //Toast.makeText(SipLoginActivity.this, "Registered OK.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SipLoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;
                    }
                    else if(!MainActivity.instance().getSipStatus() && cnt > 3) {
                        Toast.makeText(SipLoginActivity.this, "Registered Fail.", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    cnt++;
                }
            }
        });
        */
        //>>> LiangBin add, 20160425

    }

    static private SipLoginActivity instance = null;
    public static final boolean isInstanciated() {
        return instance != null;
    }
    public static final SipLoginActivity instance() {
        if (instance != null)
            return instance;
        throw new RuntimeException("SipLoginActivity not instantiated yet");
    }

    //<<< LiangBin add, for auto provision in midnight
    public android.os.Handler mProvisionHandler = new android.os.Handler(){
        public void handleMessage(Message msg) {
            myProgress.dismiss(); // LiangBin add, 20160517
            switch (msg.what) {
                case 0:
                    //if(MainActivity.instance().getSipStatus()) {
                        Toast.makeText(SipLoginActivity.this, "Registered OK.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SipLoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        //break;
                    //}
                    break;
                case 1:
                    Toast.makeText(SipLoginActivity.this, "Registered Fail..", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        };
    };
    //>>> LiangBin add, 20160427

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
                tmp = "Launch LiveHD!!!";
                direction = "-->";
                break;
            case ACTION_HANGUP_LIVEHD:
                tmp = "Hang up LiveHD!";
                direction = "-->";
                break;
            case ACTION_GET_CALL_RESULT:
            case ACTION_DOOR_CALL_FAILED:
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
                }
                else {
                    tmp = sessionType + " call connect fail\n";
                    tmp += "Direction: "+ callDirection + "\n";
                    if(callDirection.contains("Incoming"))
                        tmp += "From: " + remote.substring(4, remote.indexOf('@')) + " ("+remote+")\n"; // LiangBin add, for Incoming call, 20151221
                    else
                        tmp += "From " + local + " to " + remote + "\n";
                    tmp += "End reason: " + callStatus;
                }
                direction = "<--";
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
                tmp = "Accept call from door phone and back to TL Launcher.";
                direction = "<--";
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
                tmp = "SIP status: " + bundle.getString("RegStatus");
                direction = "<--";
                break;
            case ACTION_AUTO_PROVISION:
                tmp = "Auto provision time: " + bundle.getString("autoProvTime");
                direction = "<--";
                break;
            case ACTION_FORCE_HANGUP_TLAPP:
                tmp = "Force hangup: Launcher <-> Doorphone.";
                direction = "<--";
                break;
            case ACTION_SET_DEBUG_MODE:
                tmp = "Debug Mode = " + bundle.getBoolean("enableDebug");
                direction = "-->";
                break;
            default:
                bIsEvent = false;
                break;
        }
        Log.d(TAG, "Received LiveHD handleTLAction:" + tmp);
        /*
        if (bIsEvent) {
            addEventToList(new EventObject( Integer.toString((mEventHistoryAdapter.getCount() + 1)),
                    action.getActionName(), type, direction,
                    tmp,
                    dateFormat.format(new Date())));
        }
        */
    }

    public void provisionLiveHD()
    {
        // LiangBin add, create new provisionLiveHD method, 20151210
        if(true)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            ComponentName cn = new ComponentName(
                    LIVEHD_PACKAGE_NAME,
                    LIVEHD_PACKAGE_NAME + "." + LIVEHD_LAUNCH_ACTIVITY);

            // Please put Action parameter in the intent
            EditText rmsIdEtx = (EditText) findViewById(R.id.rmsId);
            EditText rmsPwdEtx = (EditText) findViewById(R.id.rmsPwd);
            EditText rmsAddrEtx = (EditText) findViewById(R.id.rmsAddr);
            //Spinner rmsAddrEtx = (Spinner) findViewById(R.id.spinRmsAddr);
            Bundle bundle = new Bundle();
            bundle.putInt("Action", TLAction.ACTION_LAUNCH_LIVEHD.getActionType());
            bundle.putString("rmsId", rmsIdEtx.getText().toString());
            bundle.putString("rmsPwd", rmsPwdEtx.getText().toString());
            bundle.putString("rmsAddr", rmsAddrEtx.getText().toString());
            //bundle.putBoolean("autoAnswer", autoAnsChkbox.isChecked());
            if(rmsIdEtx.getText().toString().length() == 0 || rmsPwdEtx.getText().toString().length() == 0 || rmsAddrEtx.getText().toString().length() == 0) {
                Toast.makeText(this, "Invalid data, please enter correctly.", Toast.LENGTH_SHORT).show();
                return ;
            }

            intent.setComponent(cn);
            intent.putExtras(bundle);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            handleTLAction(TLAction.ACTION_LAUNCH_LIVEHD, "I", null);
        }
        else
            Toast.makeText(this, "LiveHD already running at background.", Toast.LENGTH_SHORT).show();
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

    public void hangupLiveHD()
    {
        if(true)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            ComponentName cn = new ComponentName(
                    LIVEHD_PACKAGE_NAME,
                    LIVEHD_PACKAGE_NAME + "." + LIVEHD_LAUNCH_ACTIVITY);

            // Please put Action parameter in the intent
            Bundle bundle = new Bundle();
            bundle.putInt("Action", TLAction.ACTION_HANGUP_LIVEHD.getActionType());

            intent.setComponent(cn);
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            handleTLAction(TLAction.ACTION_HANGUP_LIVEHD, "I", null);
        }
        else
            Toast.makeText(this, "LiveHD already running at background.", Toast.LENGTH_SHORT).show();
    }

    public void killLiveHD()
    {
		/* Be careful to kill LiveHD, if LiveHD is running, send the command
		   will be failed to launch LiveHD 										*/
        if(isLiveHDRunning())
        {
            Intent intent = new Intent(LIVEHD_SERVICE);

            // Please put Action parameter in the intent
            Bundle bundle = new Bundle();
            bundle.putInt("Action", TLAction.ACTION_KILL_LIVEHD.getActionType());

            intent.putExtras(bundle);
            startService(intent);
            handleTLAction(TLAction.ACTION_KILL_LIVEHD, "I", null);
        }
    }
}

