package org.linphone;

/*
 LinphoneActivity.java
 Copyright (C) 2012  Belledonne Communications, Grenoble, France

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
import static android.content.Intent.ACTION_MAIN;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import org.apache.http.client.ClientProtocolException;

import org.linphone.LinphoneManager.AddressType;
import org.linphone.LinphonePreferences.AccountBuilder;
import org.linphone.LinphoneSimpleListener.LinphoneOnCallStateChangedListener;
import org.linphone.LinphoneSimpleListener.LinphoneOnMessageReceivedListener;
import org.linphone.LinphoneSimpleListener.LinphoneOnRegistrationStateChangedListener;
import org.linphone.compatibility.Compatibility;
import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCallLog.CallStatus;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneFriend;
import org.linphone.mediastream.Log;
import org.linphone.setup.RemoteProvisioningLoginActivity;
import org.linphone.setup.SetupActivity;
import org.linphone.ui.AddressText;
import org.linphone.setup.Qprovision;
import android.os.AsyncTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.AlertDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * @author Sylvain Berfini
 */
public class LinphoneActivity extends FragmentActivity implements
		OnClickListener, ContactPicked, LinphoneOnCallStateChangedListener,
		LinphoneOnMessageReceivedListener,
		LinphoneOnRegistrationStateChangedListener {
	public static final String PREF_FIRST_LAUNCH = "pref_first_launch";
	private static final String TAG = SetupActivity.class.toString(); // LiangBin add, 20150106
	public static final String GOOGLE_PROJECT_ID = "310942679287"; // LiangBin add, 20150122
	private String LOG_TAG = "ShareExternalServer"; // LiangBin add, 20150128
	private static final int SETTINGS_ACTIVITY = 123;
	private static final int FIRST_LOGIN_ACTIVITY = 101;
	private static final int REMOTE_PROVISIONING_LOGIN_ACTIVITY = 102;
	private static final int CALL_ACTIVITY = 19;

	private static LinphoneActivity instance;

	private StatusFragment statusFragment;
	//private TextView missedCalls, missedChats;
	//private ImageView dialer;
	private LinearLayout menu, mark;
	private RelativeLayout contacts, history, settings, /*chat, aboutChat,*/dialer, aboutSettings;
	private FragmentsAvailable currentFragment, nextFragment;
	private List<FragmentsAvailable> fragmentsHistory;
	private Fragment dialerFragment, messageListenerFragment, messageListFragment, friendStatusListenerFragment;
	private SavedState dialerSavedState;
	private boolean preferLinphoneContacts = false, isAnimationDisabled = false, isContactPresenceDisabled = true;
	private Handler mHandler = new Handler();
	private List<Contact> contactList, sipContactList;
	private Cursor contactCursor, sipContactCursor;
	private OrientationEventListener mOrientationHelper;
	private UpdateManager mUpdateManager; // LiangBIn add, 20150120
	private GoogleCloudMessaging gcm; // LiangBin add, 20150122
	private String mRegId = ""; // LiangBin add, 20150128
	private Context mContext = null;

	static final boolean isInstanciated() {
		return instance != null;
	}

	public static final LinphoneActivity instance() {
		if (instance != null)
			return instance;
		throw new RuntimeException("LinphoneActivity not instantiated yet");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (isTablet() && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else if (!isTablet() && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
		
		if (!LinphoneManager.isInstanciated()) {
			Log.e("No service running: avoid crash by starting the launcher", this.getClass().getName());
			// super.onCreate called earlier
			finish();
			startActivity(getIntent().setClass(this, LinphoneLauncherActivity.class));
			return;
		}
		
		boolean useFirstLoginActivity = getResources().getBoolean(R.bool.display_account_wizard_at_first_start);
		/*
		if (LinphonePreferences.instance().isProvisioningLoginViewEnabled()) {
			Intent wizard = new Intent();
			wizard.setClass(this, RemoteProvisioningLoginActivity.class);
			wizard.putExtra("Domain", LinphoneManager.getInstance().wizardLoginViewDomain);
			startActivityForResult(wizard, REMOTE_PROVISIONING_LOGIN_ACTIVITY);
		} else if (useFirstLoginActivity && LinphonePreferences.instance().isFirstLaunch()) {
			if (LinphonePreferences.instance().getAccountCount() > 0) {
				LinphonePreferences.instance().firstLaunchSuccessful();
			} else {
				startActivityForResult(new Intent().setClass(this, SetupActivity.class), FIRST_LOGIN_ACTIVITY);
			}
		}
		*/
		if (LinphonePreferences.instance().getAccountCount() == 0) // LiangBin add, launch login page while no any account, 20150115
			startActivityForResult(new Intent().setClass(this, SetupActivity.class), FIRST_LOGIN_ACTIVITY); // LiangBin add, always launch login page, 20141222

		setContentView(R.layout.main);
		instance = this;
		fragmentsHistory = new ArrayList<FragmentsAvailable>();
		initButtons();

		currentFragment = nextFragment = FragmentsAvailable.DIALER;
		fragmentsHistory.add(currentFragment);
		if (savedInstanceState == null) {
			if (findViewById(R.id.fragmentContainer) != null) {
				dialerFragment = new DialerFragment();
				dialerFragment.setArguments(getIntent().getExtras());
				getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, dialerFragment, currentFragment.toString()).commit();
				selectMenu(FragmentsAvailable.DIALER);
			}
		}

		int missedCalls = LinphoneManager.getLc().getMissedCallsCount();
		//displayMissedCalls(missedCalls);

		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
			rotation = 0;
			break;
		case Surface.ROTATION_90:
			rotation = 90;
			break;
		case Surface.ROTATION_180:
			rotation = 180;
			break;
		case Surface.ROTATION_270:
			rotation = 270;
			break;
		}

		LinphoneManager.getLc().setDeviceRotation(rotation);
		mAlwaysChangingPhoneAngle = rotation;

		updateAnimationsState();
		
		registerReceiver(mBroadcast, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)); // LiangBin add, 20150106
		
		//<<< LiangBin add, auto update
        mUpdateManager = new UpdateManager(this);
        //mUpdateManager.checkUpdateInfo();
        /**/
        Thread vthread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url_ver = new URL("http://autov01.vccloud.quantatw.com/Androidx86/livehd_android_version.txt");
                    BufferedReader in = new BufferedReader(new InputStreamReader(url_ver.openStream()));
                    String latest_ver = in.readLine();
                    in.close();

                    PackageManager packageManager = getPackageManager();
                    String myVersionName = "not available"; // initialize String

                    try {
                        myVersionName = packageManager.getPackageInfo(getPackageName(), 0).versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    if(compare(latest_ver, myVersionName)) {
                        //mUpdateManager = new UpdateManager(getApplicationContext());
                        //mUpdateManager.checkUpdateInfo();
                        mUpdateManager.setNewAPKName(latest_ver);
                        Message message;
                        //Handler handler = new Handler();
                        String obj = "OK";
                        message = updateHandler.obtainMessage(1,obj);
                        updateHandler.sendMessage(message);
                    }

                }catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
            private boolean compare(String v1, String v2) {
                String s1 = normalisedVersion(v1);
                String s2 = normalisedVersion(v2);
                int cmp = s1.compareTo(s2);
                String cmpStr = cmp < 0 ? "<" : cmp > 0 ? ">" : "==";
                System.out.printf("'%s' %s '%s'%n", v1, cmpStr, v2);
                if(cmpStr == ">") return true;
                return false;
            }
            public String normalisedVersion(String version) {
                return normalisedVersion(version, ".", 4);
            }

            public String normalisedVersion(String version, String sep, int maxWidth) {
                String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
                StringBuilder sb = new StringBuilder();
                for (String s : split) {
                    sb.append(String.format("%" + maxWidth + 's', s));
                }
                return sb.toString();
            }
        });
        vthread.start();
		/**/
		//>>> LiangBin add, 20150121
        
        // LiangBin add, initial and register GCM
        mContext = this;
        gcm = GoogleCloudMessaging.getInstance(this);
        registerGCM();
        //>>> LiangBin add, 20150122
	}

	private void initButtons() {
		menu = (LinearLayout) findViewById(R.id.menu);
		mark = (LinearLayout) findViewById(R.id.mark);

		history = (RelativeLayout) findViewById(R.id.history);
		history.setOnClickListener(this);
		contacts = (RelativeLayout) findViewById(R.id.contacts);
		contacts.setOnClickListener(this);
		dialer = (RelativeLayout) findViewById(R.id.dialer);
		dialer.setOnClickListener(this);
		settings = (RelativeLayout) findViewById(R.id.settings);
		settings.setOnClickListener(this);
/*
		chat = (RelativeLayout) findViewById(R.id.chat);
		chat.setOnClickListener(this);
		aboutChat = (RelativeLayout) findViewById(R.id.about_chat);
*/
		aboutSettings = (RelativeLayout) findViewById(R.id.about_settings);
		/*
		if (getResources().getBoolean(R.bool.replace_chat_by_about)) {
			chat.setVisibility(View.GONE);
			chat.setOnClickListener(null);
			findViewById(R.id.completeChat).setVisibility(View.GONE);
			aboutChat.setVisibility(View.VISIBLE);
			aboutChat.setOnClickListener(this);
		}
		*/
		if (getResources().getBoolean(R.bool.replace_settings_by_about)) {
			settings.setVisibility(View.GONE);
			settings.setOnClickListener(null);
			aboutSettings.setVisibility(View.VISIBLE);
			aboutSettings.setOnClickListener(this);
		}
/*
		missedCalls = (TextView) findViewById(R.id.missedCalls);
		missedChats = (TextView) findViewById(R.id.missedChats);
*/
	}
	
	///<<< LiangBin add, register GCM
    private void registerGCM() {
        // register with Google.
        new AsyncTask<Void,String,String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(mContext);
                    }
                    String strRegId = gcm.register(GOOGLE_PROJECT_ID);
                    mRegId = strRegId; // LiangBin add, 20150128
                    msg = "Device registered, registration id=" + strRegId;
                 
                    // send id to our server
                    sendRegIdToServer(strRegId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //tvRegisterMsg.append(msg + "\n");
            }

        }.execute(null, null, null);
    }
    
    private void sendRegIdToServer(final String strId) {
    	new AsyncTask<Void,String,String>() {
    	  @Override
    	  protected String doInBackground(Void... params) {
             	LinphonePreferences mPrefs = LinphonePreferences.instance();
            	String rmsId = mPrefs.getRmsid();
            	shareRegIdWithAppServer(rmsId, mRegId, "http://rms.vccloud.quantatw.com:88/apn/reg.php");
            	return "200";
            	/*
    	   String strResponseCode = "";
    	   try {
    		   URL url = new URL("http://sip.vccloud.quantatw.com/ios/reg.php?id="+strId);
    		   HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		   conn.setRequestMethod("POST");
    		   conn.setRequestProperty("Content-Type", "application/json");
    		   conn.setRequestProperty("User-Agent", "Android");
    	       conn.setRequestProperty("Authorization", "key="+strId);
    	       conn.setDoOutput(true);
    	       strResponseCode = String.valueOf(conn.getResponseCode());
    	       Log.d(TAG,"[L.B.track] sendRegIdToServer and get response: "+strResponseCode);
//    	       if(strResponseCode == "200") {
    	    	   Message message;
                   String obj = "GcmAvailable";
                   message = updateHandler.obtainMessage(1,obj);
                   updateHandler.sendMessage(message);
//    	       }
    	    
    	   } catch (ClientProtocolException e) {
    	    // TODO Auto-generated catch block
    	    e.printStackTrace();
    	   } catch (IOException e) {
    	    // TODO Auto-generated catch block
    	    e.printStackTrace();
    	   }
    	   
    	   return strResponseCode;
    	   /**/
    	  }
    	  
    	  @Override
    	  protected void onPostExecute(String msg) {
    	   //tvRegisterMsg.append("status code:  " + msg + "\n");
    	  }
    	  
    	}.execute(null, null, null);
    }
    
    public class GcmBroadcastReceiver extends BroadcastReceiver {
    	 static final String TAG = "GCMDemo";
    	 public static final int NOTIFICATION_ID = 1;
    	 private NotificationManager mNotificationManager;
    	 NotificationCompat.Builder builder;
    	 Context ctx;

    	 @Override
    	 public void onReceive(Context context, Intent intent) {
    	  GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
    	  ctx = context;
    	  String messageType = gcm.getMessageType(intent);
    	  if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
    	   sendNotification("Send error: " + intent.getExtras().toString());
    	  } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED .equals(messageType)) {
    	   sendNotification("Deleted messages on server: " + intent.getExtras().toString());
    	  } else {
    	   sendNotification("Received: " + intent.getExtras().toString());
    	  }
    	  setResultCode(Activity.RESULT_OK);
    	 }

    	 // Put the GCM message into a notification and post it.
    	 private void sendNotification(String msg) {
    	  mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

    	  PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
    	    new Intent(ctx, LinphoneActivity.class), 0);

    	  NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
    	    .setSmallIcon(R.drawable.about_chat)
    	    .setContentTitle("GCM Notification")
    	    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
    	    .setContentText(msg);

    	  mBuilder.setContentIntent(contentIntent);
    	  mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    	 }
    }
	///>>> LiangBin add, 20150122
    
    ///<<< LiangBin add, for missed call via server
	public void shareRegIdWithAppServer(final String workId, final String regId, final String serverAddress) {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String result = "";
				Map<String, String> paramsMap = new HashMap<String, String>();
				paramsMap.put("id", workId);
				paramsMap.put("os", "android");
				paramsMap.put("token", regId);
		
				try {
					
					URL serverUrl = null;
					String serverUrl_str = serverAddress;
					if(serverUrl_str == "")
						return "No GCM Server Address";
					StringBuilder postBody = new StringBuilder();
					Iterator<Entry<String, String>> iterator = paramsMap.entrySet()
							.iterator();
		
					while (iterator.hasNext()) {
						Entry<String, String> param = iterator.next();
						postBody.append(param.getKey()).append('=')
								.append(param.getValue());
						if (iterator.hasNext()) {
							postBody.append('&');
						}
					}
					serverUrl_str = serverUrl_str + "?" + postBody.toString();
		
					try {
						//sample to register sip vccloud server  http://sip.vccloud.quantatw.com/ios/reg.php?id=99042624&os=android&token=12323424342342
						serverUrl = new URL(serverUrl_str);
					} catch (MalformedURLException e) {
						Log.e(LOG_TAG, "URL Connection Error: "
								+ serverAddress, e);
						result = "Invalid URL: " + serverAddress;
					}
					HttpURLConnection httpCon = null;
					try {
						httpCon = (HttpURLConnection) serverUrl.openConnection();
						httpCon.setReadTimeout(5000);
            			httpCon.setConnectTimeout(3000);
						httpCon.setRequestMethod("GET");
		
						int status = httpCon.getResponseCode();
						if (status == 200) {
							result = "RegId shared with Application Server. RegId: "
									+ regId +"   WorkId:" + workId + "   OS:" + "android";
							Log.d(LOG_TAG, result);
							//GlobalStorage.bRegToAppServer = true;
						} else {
							result = "Get Failure." + " Status: " + status;
						}
					} finally {
						if (httpCon != null) {
							httpCon.disconnect();
						}
					}
		
				} catch (IOException e) {
					result = "Post Failure. Error in sharing with App Server.";
					Log.e(LOG_TAG, "Error in sharing with App Server: " + e);
				}
				return result;
			}
			
			@Override
			protected void onPostExecute(String result) {
				Log.d(LOG_TAG,"shareRegIdWithAppServer(" + workId +"," + regId +") - " + result);
			}	
		}.execute(null,null,null);
	}
    ///>>> LiangBin add, 20150128
	
	//<<< LiangBin add, handle of version update
    private Handler updateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String MsgString = (String)msg.obj;
            if (MsgString.equals("OK"))
            {
                //mUpdateManager = new UpdateManager(this);
                mUpdateManager.checkUpdateInfo();
            }
            /*if(MsgString.equals("GcmAvailable")) {
            	LinphonePreferences mPrefs = LinphonePreferences.instance();
            	String rmsId = mPrefs.getRmsid();
            	shareRegIdWithAppServer(rmsId, mRegId, "http://rms.vccloud.quantatw.com:88/apn/reg.php");
            }/**/
        }
    };
	//>>> LiangBin add, 20150121

    //<<< LiangBin add, Broadcast
	private boolean isprovision = false;
	private BroadcastReceiver mBroadcast =  new BroadcastReceiver() {
        //private final static String MY_MESSAGE = "com.givemepass.sendmessage";
        @Override
        public void onReceive(Context mContext, Intent mIntent) {
        	Log.d(TAG,"[L.B.track] onReceive in LinphoneActivity.");
            // TODO Auto-generated method stub
            //if(MY_MESSAGE.equals(mIntent.getAction())){
            final ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            final NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            	/*
                new AlertDialog.Builder(LinphoneActivity.this)
                        .setMessage("Network changed!")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                //unregisterReceiver(mBroadcast);
                            }
                        })
                        .show();
                */
            	if(isprovision)
            		return;
            	isprovision =true;
                new Thread(new Runnable() {
                    public void run() {
                    	LinphonePreferences mPrefs = LinphonePreferences.instance();
                    	if(mPrefs.getRmsflag() /*&& (Qprovision.userId!=null && Qprovision.password!=null)*/){//Neil
                    		if(mPrefs.getAccountCount()==0){
                    			isprovision =false;
                    			return;
                    		}
                    		int defaultAccount_idx=mPrefs.getDefaultAccountIndex();
                    		String original_proxy=mPrefs.getAccountProxy(defaultAccount_idx);
                    		original_proxy=original_proxy.replace("<sip:", "");
                    		original_proxy=original_proxy.substring(0,original_proxy.indexOf(";"));
                    		if (!LinphoneManager.isInstanciated()){
                				Log.i("[Neil]", "No Instanciated!");
                				isprovision =false;
                				return;
                			}
                			else{
                				int port=mPrefs.getRmsport();
                				if(!Qprovision.testserver(original_proxy,port,3000)){
                					Log.d("[Neil]","ping_original_proxy="+ original_proxy+":"+port+"=false");
                					String userId=mPrefs.getRmsid();
                            		String password=mPrefs.getRmspw();
                            		String rms_address =mPrefs.getRmsAddress();
                            		if(userId==null || password==null){
                        				Log.d("Neil", "No User info,stop provision");
                        				isprovision =false;
                        				return;
                            		}

                            		Qprovision Qprovision=new Qprovision(getApplicationContext());
                            		Qprovision.execute(userId,password,rms_address);
                            		int count=0;
                            		Qprovision.usedprofileId=-1;
                            		while(count<10 && Qprovision.usedprofileId==-1 && !Qprovision.getStatus().equals(AsyncTask.Status.FINISHED) && !Qprovision.isCancelled()){
                            			try{
                            				Thread.sleep(1000);
                            				Log.d("[Neil]","count="+count+",Qprovision_statu:"+Qprovision.getStatus());
                            				count++;
                            			}catch(InterruptedException e){
                            				Log.d("[Neil]","getUsedProfile():Thread.sleep.Exception_error="+e);
                            				e.printStackTrace();
                            			}
                            		}
                            		Qprovision.profileParam p=Qprovision.getUsedProfile();
                            		if(Qprovision.usedprofileId==-1){
                            			Log.d("Neil","provision error:ping error,status="+Qprovision.getStatus());
                            			//Toast.makeText(getApplicationContext(), "provision error", Toast.LENGTH_LONG).show();
                            			isprovision =false;
                            			return;
                            		}
                            		
                            		String transport;
                            		if(p.sip_transportType.equals("TLS")){	                            			
                            			transport=getString(R.string.pref_transport_tls_key);
                            		}
                            		else if(p.sip_transportType.equals("TCP")){
                            			transport=getString(R.string.pref_transport_tcp_key);
                            		}
                            		else{
                            			transport=getString(R.string.pref_transport_udp_key);
                            		}
                            		
                            		Log.d("[Neil]","setting:proxy="+p.sip_outboundProxyAddress+",transport="+p.sip_transportType);
                            		mPrefs.setAccountProxyAndTransport(defaultAccount_idx,p.sip_outboundProxyAddress,transport);
                            		
                        			if(p.sip_useStun.equals("true")){
                        				Log.d("[Neil]","useStun="+p.sip_useStun+",setting:setStunServer="+p.sip_stunServerAddress);
                        				mPrefs.setStunServer(p.sip_stunServerAddress);
                        			}else{
                        				Log.d("[Neil]","useStun="+p.sip_useStun+",setting:setStunServer=\"\",null");
                        				mPrefs.setStunServer("");
                        			}

                        			Log.d("[Neil]","IceEnabled="+p.sip_iceEnabled.equals("true"));
                        			mPrefs.setIceEnabled(p.sip_iceEnabled.equals("true"));

                        			/*
                        			Log.d("[Neil]","setting:port=5060");
                                	mPrefs.useRandomPort(false);
                                	mPrefs.setSipPort(5060);
                                	*/
            					}
                			}
                    		isprovision =false;
                    	}
                    	else{//LiangBin
	                    	boolean bInQuanta = YouAreInQuanta();
	                        if(bInQuanta == true) 
	                            Log.i(TAG, "[L.B.track] You are in Quanta");
	                        else
	                        	Log.i(TAG, "[L.B.track] You are out of Quanta");
	                    	/*Neil Modified, 2015/02/03 */
	                        //<<< LiangBin add, reset configuration
	                        /*
	                        AccountBuilder builder = new AccountBuilder(LinphoneManager.getLc());
	                    	builder.setExpires("604800")
	                    		.setOutboundProxyEnabled(true)
	                    		.setProxy(bInQuanta?"sipprx03.vccloud.quantatw.com":"sipsrv03.vccloud.quantatw.com"); 
	                    		
	                    	//LinphonePreferences mPrefs = LinphonePreferences.instance();
	                    	mPrefs.setStunServer("stun03.vccloud.quantatw.com");
	                    	mPrefs.setIceEnabled(!bInQuanta); 
	                    	//>>> LiangBin add, 20150106
	                    	*/
	                        mPrefs.setAccountProxy(mPrefs.getDefaultAccountIndex(), bInQuanta?"sipprx03.vccloud.quantatw.com":"sipsrv03.vccloud.quantatw.com");
	                        mPrefs.setStunServer("stun03.vccloud.quantatw.com");
	                    	mPrefs.setIceEnabled(!bInQuanta); 
	                        /*Neil Modified, 2015/02/03 */
	                    	isprovision =false;
                    	}
                    }
                }).start(); // LiangBin add, best route, 20150106
            }
            //}
        }
    };
    //>>> LiangBin add, 20150106
    
	//<<< LiangBin add, best route
    private boolean YouAreInQuanta(){
    	/*Neil Modified, 2015/02/03 */
    	/*
        int count = 0;
        String str = "connectless..";

        try {

            Process process = null;
            String url = "sipsrv03.vccloud.quantatw.com";

            if(android.os.Build.VERSION.SDK_INT <= 16) {
                // shiny APIS
                process = Runtime.getRuntime().exec(
                        "/system/bin/ping -w 1 -c 1 " + url);


            }
            else
            {

                process = new ProcessBuilder()
                        .command("/system/bin/ping", url)
                        .redirectErrorStream(true)
                        .start();

            }



            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));

            StringBuffer output = new StringBuffer();
            String temp;

            while ( (temp = reader.readLine()) != null)//.read(buffer)) > 0)
            {
                output.append(temp);
                count++;
            }

            reader.close();


            if(count > 0)
                str = output.toString();

            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("PING Count:", ""+count);
        Log.i("PING String: ", str);
        
        if(count > 0) return false;

        return true;
        */
    	String url = "sipsrv03.vccloud.quantatw.com";
    	int port = 5060;
    	return !Qprovision.testserver(url,port,3000);
    	/*Neil Modified, 2015/02/03 */
    }
	//>>> LiangBin add, 20150106
	
	private boolean isTablet() {
		return getResources().getBoolean(R.bool.isTablet);
	}

	public void hideStatusBar() {
		if (isTablet()) {
			return;
		}
		
		findViewById(R.id.status).setVisibility(View.GONE);
		findViewById(R.id.fragmentContainer).setPadding(0, 0, 0, 0);
	}

	public void showStatusBar() {
		if (isTablet()) {
			return;
		}
		
		if (statusFragment != null && !statusFragment.isVisible()) {
			// Hack to ensure statusFragment is visible after coming back to
			// dialer from chat
			statusFragment.getView().setVisibility(View.VISIBLE);
		}
		findViewById(R.id.status).setVisibility(View.VISIBLE);
		findViewById(R.id.fragmentContainer).setPadding(0, LinphoneUtils.pixelsToDpi(getResources(), 40), 0, 0);
	}

	private void changeCurrentFragment(FragmentsAvailable newFragmentType, Bundle extras) {
		changeCurrentFragment(newFragmentType, extras, false);
	}

	@SuppressWarnings("incomplete-switch")
	private void changeCurrentFragment(FragmentsAvailable newFragmentType, Bundle extras, boolean withoutAnimation) {
		if (newFragmentType == currentFragment && newFragmentType != FragmentsAvailable.CHAT) {
			return;
		}
		nextFragment = newFragmentType;

		if (currentFragment == FragmentsAvailable.DIALER) {
			try {
				dialerSavedState = getSupportFragmentManager().saveFragmentInstanceState(dialerFragment);
			} catch (Exception e) {
			}
		}

		Fragment newFragment = null;

		switch (newFragmentType) {
		case HISTORY:
			if (getResources().getBoolean(R.bool.use_simple_history)) {
				newFragment = new HistorySimpleFragment();
			} else {
				newFragment = new HistoryFragment();
			}
			break;
		case HISTORY_DETAIL:
			newFragment = new HistoryDetailFragment();
			break;
		case CONTACTS:
			if (getResources().getBoolean(R.bool.use_android_native_contact_edit_interface)) {
				Intent i = new Intent();
			    i.setComponent(new ComponentName("com.android.contacts", "com.android.contacts.DialtactsContactsEntryActivity"));
			    i.setAction("android.intent.action.MAIN");
			    i.addCategory("android.intent.category.LAUNCHER");
			    i.addCategory("android.intent.category.DEFAULT");
			    startActivity(i);
			} else {
				newFragment = new ContactsFragment();
				friendStatusListenerFragment = newFragment;
			}
			break;
		case CONTACT:
			newFragment = new ContactFragment();
			break;
		case EDIT_CONTACT:
			newFragment = new EditContactFragment();
			break;
		case DIALER:
			newFragment = new DialerFragment();
			if (extras == null) {
				newFragment.setInitialSavedState(dialerSavedState);
			}
			dialerFragment = newFragment;
			break;
		case SETTINGS:
			newFragment = new SettingsFragment();
			break;
		case ACCOUNT_SETTINGS:
			newFragment = new AccountPreferencesFragment();
			break;
		case ABOUT:
		case ABOUT_INSTEAD_OF_CHAT:
		case ABOUT_INSTEAD_OF_SETTINGS:
			newFragment = new AboutFragment();
			break;
		case CHAT:
			newFragment = new ChatFragment();
			messageListenerFragment = newFragment;
			break;
		case CHATLIST:
			newFragment = new ChatListFragment();
			messageListFragment = new Fragment();
			break;
		}

		if (newFragment != null) {
			newFragment.setArguments(extras);
			if (isTablet()) {
				changeFragmentForTablets(newFragment, newFragmentType, withoutAnimation);
			} else {
				changeFragment(newFragment, newFragmentType, withoutAnimation);
			}
		}
	}

	private void updateAnimationsState() {
		isAnimationDisabled = getResources().getBoolean(R.bool.disable_animations) || !LinphonePreferences.instance().areAnimationsEnabled();
		isContactPresenceDisabled = !getResources().getBoolean(R.bool.enable_linphone_friends);
	}

	public boolean isAnimationDisabled() {
		return isAnimationDisabled;
	}

	public boolean isContactPresenceDisabled() {
		return isContactPresenceDisabled;
	}

	private void changeFragment(Fragment newFragment, FragmentsAvailable newFragmentType, boolean withoutAnimation) {
		if (statusFragment != null) {
			statusFragment.closeStatusBar();
		}

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		if (!withoutAnimation && !isAnimationDisabled && currentFragment.shouldAnimate()) {
			if (newFragmentType.isRightOf(currentFragment)) {
				transaction.setCustomAnimations(R.anim.slide_in_right_to_left,
						R.anim.slide_out_right_to_left,
						R.anim.slide_in_left_to_right,
						R.anim.slide_out_left_to_right);
			} else {
				transaction.setCustomAnimations(R.anim.slide_in_left_to_right,
						R.anim.slide_out_left_to_right,
						R.anim.slide_in_right_to_left,
						R.anim.slide_out_right_to_left);
			}
		}
		try {
			getSupportFragmentManager().popBackStackImmediate(newFragmentType.toString(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		} catch (java.lang.IllegalStateException e) {

		}

		transaction.addToBackStack(newFragmentType.toString());
		transaction.replace(R.id.fragmentContainer, newFragment, newFragmentType.toString());
		transaction.commitAllowingStateLoss();
		getSupportFragmentManager().executePendingTransactions();

		currentFragment = newFragmentType;
	}

	private void changeFragmentForTablets(Fragment newFragment, FragmentsAvailable newFragmentType, boolean withoutAnimation) {
//		if (getResources().getBoolean(R.bool.show_statusbar_only_on_dialer)) {
//			if (newFragmentType == FragmentsAvailable.DIALER) {
//				showStatusBar();
//			} else {
//				hideStatusBar();
//			}
//		}
		if (statusFragment != null) {
			statusFragment.closeStatusBar();
		}

		LinearLayout ll = (LinearLayout) findViewById(R.id.fragmentContainer2);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		if (newFragmentType.shouldAddItselfToTheRightOf(currentFragment)) {
			ll.setVisibility(View.VISIBLE);
			
			transaction.addToBackStack(newFragmentType.toString());
			transaction.replace(R.id.fragmentContainer2, newFragment);
		} else {
			if (newFragmentType == FragmentsAvailable.DIALER 
					|| newFragmentType == FragmentsAvailable.ABOUT 
					|| newFragmentType == FragmentsAvailable.ABOUT_INSTEAD_OF_CHAT 
					|| newFragmentType == FragmentsAvailable.ABOUT_INSTEAD_OF_SETTINGS
					|| newFragmentType == FragmentsAvailable.SETTINGS 
					|| newFragmentType == FragmentsAvailable.ACCOUNT_SETTINGS) {
				ll.setVisibility(View.GONE);
			} else {
				ll.setVisibility(View.INVISIBLE);
			}
			
			if (!withoutAnimation && !isAnimationDisabled && currentFragment.shouldAnimate()) {
				if (newFragmentType.isRightOf(currentFragment)) {
					transaction.setCustomAnimations(R.anim.slide_in_right_to_left, R.anim.slide_out_right_to_left, R.anim.slide_in_left_to_right, R.anim.slide_out_left_to_right);
				} else {
					transaction.setCustomAnimations(R.anim.slide_in_left_to_right, R.anim.slide_out_left_to_right, R.anim.slide_in_right_to_left, R.anim.slide_out_right_to_left);
				}
			}
			
			try {
				getSupportFragmentManager().popBackStackImmediate(newFragmentType.toString(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
			} catch (java.lang.IllegalStateException e) {
				
			}
			
			transaction.addToBackStack(newFragmentType.toString());
			transaction.replace(R.id.fragmentContainer, newFragment);
		}
		transaction.commitAllowingStateLoss();
		getSupportFragmentManager().executePendingTransactions();
		
		currentFragment = newFragmentType;
		if (currentFragment == FragmentsAvailable.DIALER) {
			fragmentsHistory.clear();
		}
		fragmentsHistory.add(currentFragment);
	}

	public void displayHistoryDetail(String sipUri, LinphoneCallLog log) {
		LinphoneAddress lAddress;
		try {
			lAddress = LinphoneCoreFactory.instance().createLinphoneAddress(sipUri);
		} catch (LinphoneCoreException e) {
			Log.e("Cannot display history details",e);
			return;
		}
		Uri uri = LinphoneUtils.findUriPictureOfContactAndSetDisplayName(lAddress, getContentResolver());

		String displayName = lAddress.getDisplayName();
		String pictureUri = uri == null ? null : uri.toString();

		String status;
		if (log.getDirection() == CallDirection.Outgoing) {
			status = "Outgoing";
		} else {
			if (log.getStatus() == CallStatus.Missed) {
				status = "Missed";
			} else {
				status = "Incoming";
			}
		}

		String callTime = secondsToDisplayableString(log.getCallDuration());
		String callDate = String.valueOf(log.getTimestamp());

		Fragment fragment2 = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer2);
		if (fragment2 != null && fragment2.isVisible() && currentFragment == FragmentsAvailable.HISTORY_DETAIL) {
			HistoryDetailFragment historyDetailFragment = (HistoryDetailFragment) fragment2;
			historyDetailFragment.changeDisplayedHistory(sipUri, displayName, pictureUri, status, callTime, callDate);
		} else {
			Bundle extras = new Bundle();
			extras.putString("SipUri", sipUri);
			if (displayName != null) {
				extras.putString("DisplayName", displayName);
				extras.putString("PictureUri", pictureUri);
			}
			extras.putString("CallStatus", status);
			extras.putString("CallTime", callTime);
			extras.putString("CallDate", callDate);

			changeCurrentFragment(FragmentsAvailable.HISTORY_DETAIL, extras);
		}
	}

	@SuppressLint("SimpleDateFormat")
	private String secondsToDisplayableString(int secs) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.set(0, 0, 0, 0, 0, secs);
		return dateFormat.format(cal.getTime());
	}

	public void displayContact(Contact contact, boolean chatOnly) {
		Fragment fragment2 = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer2);
		if (fragment2 != null && fragment2.isVisible() && currentFragment == FragmentsAvailable.CONTACT) {
			ContactFragment contactFragment = (ContactFragment) fragment2;
			contactFragment.changeDisplayedContact(contact);
		} else {
			Bundle extras = new Bundle();
			extras.putSerializable("Contact", contact);
			extras.putBoolean("ChatAddressOnly", chatOnly);
			changeCurrentFragment(FragmentsAvailable.CONTACT, extras);
		}
	}

	public void displayContacts(boolean chatOnly) {
		if (chatOnly) {
			preferLinphoneContacts = true;
		}

		Bundle extras = new Bundle();
		extras.putBoolean("ChatAddressOnly", chatOnly);
		changeCurrentFragment(FragmentsAvailable.CONTACTS, extras);
		preferLinphoneContacts = false;
	}

	public void displayContactsForEdition(String sipAddress) {
		Bundle extras = new Bundle();
		extras.putBoolean("EditOnClick", true);
		extras.putString("SipAddress", sipAddress);
		changeCurrentFragment(FragmentsAvailable.CONTACTS, extras);
	}

	public void displayAbout() {
		changeCurrentFragment(FragmentsAvailable.ABOUT, null);
	}

	public void displayChat(String sipUri) {
		if (getResources().getBoolean(R.bool.disable_chat)) {
			return;
		}

		LinphoneAddress lAddress;
		try {
			lAddress = LinphoneCoreFactory.instance().createLinphoneAddress(sipUri);
		} catch (LinphoneCoreException e) {
			Log.e("Cannot display chat",e);
			return;
		}
		Uri uri = LinphoneUtils.findUriPictureOfContactAndSetDisplayName(lAddress, getContentResolver());
		String displayName = lAddress.getDisplayName();
		String pictureUri = uri == null ? null : uri.toString();

		if (currentFragment == FragmentsAvailable.CHATLIST || currentFragment == FragmentsAvailable.CHAT) {
			Fragment fragment2 = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer2);
			if (fragment2 != null && fragment2.isVisible() && currentFragment == FragmentsAvailable.CHAT) {
				ChatFragment chatFragment = (ChatFragment) fragment2;
				chatFragment.changeDisplayedChat(sipUri, displayName, pictureUri);
			} else {
				Bundle extras = new Bundle();
				extras.putString("SipUri", sipUri);
				if (lAddress.getDisplayName() != null) {
					extras.putString("DisplayName", displayName);
					extras.putString("PictureUri", pictureUri);
				}
				changeCurrentFragment(FragmentsAvailable.CHAT, extras);
			}
		} else {
			changeCurrentFragment(FragmentsAvailable.CHATLIST, null);
			displayChat(sipUri);
		}
		LinphoneService.instance().resetMessageNotifCount();
		LinphoneService.instance().removeMessageNotification();
		//displayMissedChats(getChatStorage().getUnreadMessageCount());
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		resetSelection();

		if (id == R.id.history) {
			changeCurrentFragment(FragmentsAvailable.HISTORY, null);
			history.setSelected(true);
			LinphoneManager.getLc().resetMissedCallsCount();
			//displayMissedCalls(0);
		} else if (id == R.id.contacts) {
			changeCurrentFragment(FragmentsAvailable.CONTACTS, null);
			contacts.setSelected(true);
		} else if (id == R.id.dialer) {
			changeCurrentFragment(FragmentsAvailable.DIALER, null);
			dialer.setSelected(true);
		} else if (id == R.id.settings) {
			changeCurrentFragment(FragmentsAvailable.SETTINGS, null);
			settings.setSelected(true);
		} else if (id == R.id.about_chat) {
			Bundle b = new Bundle();
			b.putSerializable("About", FragmentsAvailable.ABOUT_INSTEAD_OF_CHAT);
			changeCurrentFragment(FragmentsAvailable.ABOUT_INSTEAD_OF_CHAT, b);
			//aboutChat.setSelected(true);
		} else if (id == R.id.about_settings) {
			Bundle b = new Bundle();
			b.putSerializable("About", FragmentsAvailable.ABOUT_INSTEAD_OF_SETTINGS);
			changeCurrentFragment(FragmentsAvailable.ABOUT_INSTEAD_OF_SETTINGS, b);
			aboutSettings.setSelected(true);
		} else if (id == R.id.chat) {
			changeCurrentFragment(FragmentsAvailable.CHATLIST, null);
			//chat.setSelected(true);
		}
	}

	private void resetSelection() {
		history.setSelected(false);
		contacts.setSelected(false);
		dialer.setSelected(false);
		settings.setSelected(false);
		//chat.setSelected(false);
		//aboutChat.setSelected(false);
		aboutSettings.setSelected(false);
	}

	@SuppressWarnings("incomplete-switch")
	public void selectMenu(FragmentsAvailable menuToSelect) {
		currentFragment = menuToSelect;
		resetSelection();

		switch (menuToSelect) {
		case HISTORY:
		case HISTORY_DETAIL:
			history.setSelected(true);
			break;
		case CONTACTS:
		case CONTACT:
		case EDIT_CONTACT:
			contacts.setSelected(true);
			break;
		case DIALER:
			dialer.setSelected(true);
			break;
		case SETTINGS:
		case ACCOUNT_SETTINGS:
			settings.setSelected(true);
			break;
		case ABOUT_INSTEAD_OF_CHAT:
			//aboutChat.setSelected(true);
			break;
		case ABOUT_INSTEAD_OF_SETTINGS:
			aboutSettings.setSelected(true);
			break;
		case CHAT:
		case CHATLIST:
			//chat.setSelected(true);
			break;
		}
	}

	public void updateDialerFragment(DialerFragment fragment) {
		dialerFragment = fragment;
		// Hack to maintain soft input flags
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	public void updateChatFragment(ChatFragment fragment) {
		messageListenerFragment = fragment;
		// Hack to maintain soft input flags
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	public void updateChatListFragment(ChatListFragment fragment) {
		messageListFragment = fragment;
	}

	public void hideMenu(boolean hide) {
		menu.setVisibility(hide ? View.GONE : View.VISIBLE);
		mark.setVisibility(hide ? View.GONE : View.VISIBLE);
	}

	public void updateStatusFragment(StatusFragment fragment) {
		statusFragment = fragment;

		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null && lc.getDefaultProxyConfig() != null) {
			statusFragment.registrationStateChanged(LinphoneManager.getLc().getDefaultProxyConfig().getState());
		}
	}

	public void displaySettings() {
		changeCurrentFragment(FragmentsAvailable.SETTINGS, null);
		settings.setSelected(true);
	}

	public void applyConfigChangesIfNeeded() {
		if (nextFragment != FragmentsAvailable.SETTINGS && nextFragment != FragmentsAvailable.ACCOUNT_SETTINGS) {
			updateAnimationsState();
		}
	}

	public void displayAccountSettings(int accountNumber) {
		Bundle bundle = new Bundle();
		bundle.putInt("Account", accountNumber);
		changeCurrentFragment(FragmentsAvailable.ACCOUNT_SETTINGS, bundle);
		settings.setSelected(true);
	}

	public StatusFragment getStatusFragment() {
		return statusFragment;
	}

	public List<String> getChatList() {
		return getChatStorage().getChatList();
	}

	public List<String> getDraftChatList() {
		return getChatStorage().getDrafts();
	}

	public List<ChatMessage> getChatMessages(String correspondent) {
		return getChatStorage().getMessages(correspondent);
	}

	public void removeFromChatList(String sipUri) {
		getChatStorage().removeDiscussion(sipUri);
	}

	public void removeFromDrafts(String sipUri) {
		getChatStorage().deleteDraft(sipUri);
	}

	@Override
	public void onMessageReceived(LinphoneAddress from, LinphoneChatMessage message, int id) {
		ChatFragment chatFragment = ((ChatFragment) messageListenerFragment);
		if (messageListenerFragment != null && messageListenerFragment.isVisible() && chatFragment.getSipUri().equals(from.asStringUriOnly())) {
			chatFragment.onMessageReceived(id, from, message);
			getChatStorage().markMessageAsRead(id);
		} else if (LinphoneService.isReady()) {
			//displayMissedChats(getChatStorage().getUnreadMessageCount());
			if (messageListFragment != null && messageListFragment.isVisible()) {
				((ChatListFragment) messageListFragment).refresh();
			}
		}
	}

	public void updateMissedChatCount() {
		//displayMissedChats(getChatStorage().getUnreadMessageCount());
	}

	public int onMessageSent(String to, String message) {
		getChatStorage().deleteDraft(to);
		return getChatStorage().saveTextMessage("", to, message, System.currentTimeMillis());
	}

	public int onMessageSent(String to, Bitmap image, String imageURL) {
		getChatStorage().deleteDraft(to);
		return getChatStorage().saveImageMessage("", to, image, imageURL, System.currentTimeMillis());
	}

	public void onMessageStateChanged(String to, String message, int newState) {
		getChatStorage().updateMessageStatus(to, message, newState);
	}

	public void onImageMessageStateChanged(String to, int id, int newState) {
		getChatStorage().updateMessageStatus(to, id, newState);
	}

	public void onRegistrationStateChanged(LinphoneProxyConfig proxy, RegistrationState state, String message) {
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (statusFragment != null) {
			if (lc != null)
				if(lc.getDefaultProxyConfig() == null)
					statusFragment.registrationStateChanged(proxy.getState());
				else 
					statusFragment.registrationStateChanged(lc.getDefaultProxyConfig().getState());
			else
				statusFragment.registrationStateChanged(RegistrationState.RegistrationNone);
		}
		
		if(state.equals(RegistrationState.RegistrationCleared)){ 
			if(lc != null){
				LinphoneAuthInfo authInfo = lc.findAuthInfo(proxy.getIdentity(), proxy.getRealm(), proxy.getDomain());
				if(authInfo != null)
					lc.removeAuthInfo(authInfo);
			}
		}
	}
/*
	private void displayMissedCalls(final int missedCallsCount) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (missedCallsCount > 0) {
					missedCalls.setText(missedCallsCount + "");
					missedCalls.setVisibility(View.VISIBLE);
					if (!isAnimationDisabled) {
						missedCalls.startAnimation(AnimationUtils.loadAnimation(LinphoneActivity.this, R.anim.bounce));
					}
				} else {
					missedCalls.clearAnimation();
					missedCalls.setVisibility(View.GONE);
				}
			}
		});
	}
*/
/*
	private void displayMissedChats(final int missedChatCount) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (missedChatCount > 0) {
					missedChats.setText(missedChatCount + "");
					if (missedChatCount > 99) {
						missedChats.setTextSize(12);
					} else {
						missedChats.setTextSize(20);
					}
					missedChats.setVisibility(View.VISIBLE);
					if (!isAnimationDisabled) {
						missedChats.startAnimation(AnimationUtils.loadAnimation(LinphoneActivity.this, R.anim.bounce));
					}
				} else {
					missedChats.clearAnimation();
					missedChats.setVisibility(View.GONE);
				}
			}
		});
	}
*/
	@Override
	public void onCallStateChanged(LinphoneCall call, State state, String message) {
		if (state == State.IncomingReceived) {
			startActivity(new Intent(this, IncomingCallActivity.class));
		} else if (state == State.OutgoingInit) {
			if (call.getCurrentParamsCopy().getVideoEnabled()) {
				startVideoActivity(call);
			} else {
				startIncallActivity(call);
			}
		} else if (state == State.CallEnd || state == State.Error || state == State.CallReleased) {
			// Convert LinphoneCore message for internalization
			if (message != null && message.equals("Call declined.")) { 
				displayCustomToast(getString(R.string.error_call_declined), Toast.LENGTH_LONG);
			} else if (message != null && message.equals("Not Found")) {
				displayCustomToast(getString(R.string.error_user_not_found), Toast.LENGTH_LONG);
			} else if (message != null && message.equals("Unsupported media type")) {
				displayCustomToast(getString(R.string.error_incompatible_media), Toast.LENGTH_LONG);
			}
			resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
		}

		int missedCalls = LinphoneManager.getLc().getMissedCallsCount();
		//displayMissedCalls(missedCalls);
	}

	public void displayCustomToast(final String message, final int duration) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastRoot));

				TextView toastText = (TextView) layout.findViewById(R.id.toastMessage);
				toastText.setText(message);

				final Toast toast = new Toast(getApplicationContext());
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.setDuration(duration);
				toast.setView(layout);
				toast.show();
			}
		});
	}

	@Override
	public void setAddresGoToDialerAndCall(String number, String name, Uri photo) {
//		Bundle extras = new Bundle();
//		extras.putString("SipUri", number);
//		extras.putString("DisplayName", name);
//		extras.putString("Photo", photo == null ? null : photo.toString());
//		changeCurrentFragment(FragmentsAvailable.DIALER, extras);
		
		AddressType address = new AddressText(this, null);
		address.setDisplayedName(name);
		address.setText(number);
		LinphoneManager.getInstance().newOutgoingCall(address);
	}

	public void setAddressAndGoToDialer(String number) {
		Bundle extras = new Bundle();
		extras.putString("SipUri", number);
		changeCurrentFragment(FragmentsAvailable.DIALER, extras);
	}

	@Override
	public void goToDialer() {
		changeCurrentFragment(FragmentsAvailable.DIALER, null);
	}

	public void startVideoActivity(LinphoneCall currentCall) {
		Intent intent = new Intent(this, InCallActivity.class);
		intent.putExtra("VideoEnabled", true);
		startOrientationSensor();
		startActivityForResult(intent, CALL_ACTIVITY);
	}

	public void startIncallActivity(LinphoneCall currentCall) {
		Intent intent = new Intent(this, InCallActivity.class);
		intent.putExtra("VideoEnabled", false);
		startOrientationSensor();
		startActivityForResult(intent, CALL_ACTIVITY);
	}

	/**
	 * Register a sensor to track phoneOrientation changes
	 */
	private synchronized void startOrientationSensor() {
		if (mOrientationHelper == null) {
			mOrientationHelper = new LocalOrientationEventListener(this);
		}
		mOrientationHelper.enable();
	}

	private int mAlwaysChangingPhoneAngle = -1;
	private AcceptNewFriendDialog acceptNewFriendDialog;

	private class LocalOrientationEventListener extends OrientationEventListener {
		public LocalOrientationEventListener(Context context) {
			super(context);
		}

		@Override
		public void onOrientationChanged(final int o) {
			if (o == OrientationEventListener.ORIENTATION_UNKNOWN) {
				return;
			}

			int degrees = 270;
			if (o < 45 || o > 315)
				degrees = 0;
			else if (o < 135)
				degrees = 90;
			else if (o < 225)
				degrees = 180;

			if (mAlwaysChangingPhoneAngle == degrees) {
				return;
			}
			mAlwaysChangingPhoneAngle = degrees;

			Log.d("Phone orientation changed to ", degrees);
			int rotation = (360 - degrees) % 360;
			LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
			if (lc != null) {
				lc.setDeviceRotation(rotation);
				LinphoneCall currentCall = lc.getCurrentCall();
				if (currentCall != null && currentCall.cameraEnabled() && currentCall.getCurrentParamsCopy().getVideoEnabled()) {
					lc.updateCall(currentCall, null);
				}
			}
		}
	}

	public void showPreferenceErrorDialog(String message) {

	}

	public List<Contact> getAllContacts() {
		return contactList;
	}

	public List<Contact> getSIPContacts() {
		return sipContactList;
	}

	public Cursor getAllContactsCursor() {
		return contactCursor;
	}

	public Cursor getSIPContactsCursor() {
		return sipContactCursor;
	}

	public void setLinphoneContactsPrefered(boolean isPrefered) {
		preferLinphoneContacts = isPrefered;
	}

	public boolean isLinphoneContactsPrefered() {
		return preferLinphoneContacts;
	}

	public void onNewSubscriptionRequestReceived(LinphoneFriend friend,
			String sipUri) {
		if (isContactPresenceDisabled) {
			return;
		}

		sipUri = sipUri.replace("<", "").replace(">", "");
		if (LinphonePreferences.instance().shouldAutomaticallyAcceptFriendsRequests()) {
			Contact contact = findContactWithSipAddress(sipUri);
			if (contact != null) {
				friend.enableSubscribes(true);
				try {
					LinphoneManager.getLc().addFriend(friend);
					contact.setFriend(friend);
				} catch (LinphoneCoreException e) {
					e.printStackTrace();
				}
			}
		} else {
			Contact contact = findContactWithSipAddress(sipUri);
			if (contact != null) {
				FragmentManager fm = getSupportFragmentManager();
				acceptNewFriendDialog = new AcceptNewFriendDialog(contact, sipUri);
				acceptNewFriendDialog.show(fm, "New Friend Request Dialog");
			}
		}
	}

	private Contact findContactWithSipAddress(String sipUri) {
		if (!sipUri.startsWith("sip:")) {
			sipUri = "sip:" + sipUri;
		}

		for (Contact contact : sipContactList) {
			for (String addr : contact.getNumerosOrAddresses()) {
				if (addr.equals(sipUri)) {
					return contact;
				}
			}
		}
		return null;
	}

	public void onNotifyPresenceReceived(LinphoneFriend friend) {
		if (!isContactPresenceDisabled && currentFragment == FragmentsAvailable.CONTACTS && friendStatusListenerFragment != null) {
			((ContactsFragment) friendStatusListenerFragment).invalidate();
		}
	}

	public boolean newFriend(Contact contact, String sipUri) {
		LinphoneFriend friend = LinphoneCoreFactory.instance().createLinphoneFriend(sipUri);
		friend.enableSubscribes(true);
		friend.setIncSubscribePolicy(LinphoneFriend.SubscribePolicy.SPAccept);
		try {
			LinphoneManager.getLc().addFriend(friend);
			contact.setFriend(friend);
			return true;
		} catch (LinphoneCoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void acceptNewFriend(Contact contact, String sipUri, boolean accepted) {
		acceptNewFriendDialog.dismissAllowingStateLoss();
		if (accepted) {
			newFriend(contact, sipUri);
		}
	}

	public boolean removeFriend(Contact contact, String sipUri) {
		LinphoneFriend friend = LinphoneManager.getLc().findFriendByAddress(sipUri);
		if (friend != null) {
			friend.enableSubscribes(false);
			LinphoneManager.getLc().removeFriend(friend);
			contact.setFriend(null);
			return true;
		}
		return false;
	}

	private void searchFriendAndAddToContact(Contact contact) {
		if (contact == null || contact.getNumerosOrAddresses() == null) {
			return;
		}

		for (String sipUri : contact.getNumerosOrAddresses()) {
			if (LinphoneUtils.isSipAddress(sipUri)) {
				LinphoneFriend friend = LinphoneManager.getLc().findFriendByAddress(sipUri);
				if (friend != null) {
					friend.enableSubscribes(true);
					friend.setIncSubscribePolicy(LinphoneFriend.SubscribePolicy.SPAccept);
					contact.setFriend(friend);
					break;
				}
			}
		}
	}
	
	public void removeContactFromLists(Contact contact) {
		for (Contact c : contactList) {
			if (c != null && c.getID().equals(contact.getID())) {
				contactList.remove(c);
				contactCursor = Compatibility.getContactsCursor(getContentResolver());
				break;
			}
		}
		
		for (Contact c : sipContactList) {
			if (c != null && c.getID().equals(contact.getID())) {
				sipContactList.remove(c);
				sipContactCursor = Compatibility.getSIPContactsCursor(getContentResolver());
				break;
			}
		}
	}

	public synchronized void prepareContactsInBackground() {
		if (contactCursor != null) {
			contactCursor.close();
		}
		if (sipContactCursor != null) {
			sipContactCursor.close();
		}

		contactCursor = Compatibility.getContactsCursor(getContentResolver());
		sipContactCursor = Compatibility.getSIPContactsCursor(getContentResolver());

		Thread sipContactsHandler = new Thread(new Runnable() {
			@Override
			public void run() {
				if(sipContactCursor != null) {
					for (int i = 0; i < sipContactCursor.getCount(); i++) {
						Contact contact = Compatibility.getContact(getContentResolver(), sipContactCursor, i);
						if (contact == null)
							continue;
						
						contact.refresh(getContentResolver());
						if (!isContactPresenceDisabled) {
							searchFriendAndAddToContact(contact);
						}
						sipContactList.add(contact);
					}
				}
				if(contactCursor != null) {
					for (int i = 0; i < contactCursor.getCount(); i++) {
						Contact contact = Compatibility.getContact(getContentResolver(), contactCursor, i);
						if (contact == null)
							continue;
						
						for (Contact c : sipContactList) {
							if (c != null && c.getID().equals(contact.getID())) {
								contact = c;
								break;
							}
						}
						contactList.add(contact);
					}
				}
			}
		});

		contactList = new ArrayList<Contact>();
		sipContactList = new ArrayList<Contact>();
		
		sipContactsHandler.start();
	}

	private void initInCallMenuLayout(boolean callTransfer) {
		selectMenu(FragmentsAvailable.DIALER);
		if (dialerFragment != null) {
			((DialerFragment) dialerFragment).resetLayout(callTransfer);
		}
	}

	public void resetClassicMenuLayoutAndGoBackToCallIfStillRunning() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (dialerFragment != null) {
					((DialerFragment) dialerFragment).resetLayout(false);
				}

				if (LinphoneManager.isInstanciated() && LinphoneManager.getLc().getCallsNb() > 0) {
					LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
					if (call.getState() == LinphoneCall.State.IncomingReceived) {
						startActivity(new Intent(LinphoneActivity.this, IncomingCallActivity.class));
					} else if (call.getCurrentParamsCopy().getVideoEnabled()) {
						startVideoActivity(call);
					} else {
						startIncallActivity(call);
					}
				}
			}
		});
	}

	public FragmentsAvailable getCurrentFragment() {
		return currentFragment;
	}

	public ChatStorage getChatStorage() {
		return ChatStorage.getInstance();
	}
	
	public void addContact(String displayName, String sipUri)
	{
		if (getResources().getBoolean(R.bool.use_android_native_contact_edit_interface)) {
			Intent intent = Compatibility.prepareAddContactIntent(displayName, sipUri);
			startActivity(intent);
		} else {
			Bundle extras = new Bundle();
			extras.putSerializable("NewSipAdress", sipUri);
			changeCurrentFragment(FragmentsAvailable.EDIT_CONTACT, extras);
		}
	}
	
	public void editContact(Contact contact)
	{
		if (getResources().getBoolean(R.bool.use_android_native_contact_edit_interface)) {
			Intent intent = Compatibility.prepareEditContactIntent(Integer.parseInt(contact.getID()));
			startActivity(intent);
		} else {
			Bundle extras = new Bundle();
			extras.putSerializable("Contact", contact);
			changeCurrentFragment(FragmentsAvailable.EDIT_CONTACT, extras);
		}
	}
	
	public void editContact(Contact contact, String sipAddress)
	{
		if (getResources().getBoolean(R.bool.use_android_native_contact_edit_interface)) {
			Intent intent = Compatibility.prepareEditContactIntentWithSipAddress(Integer.parseInt(contact.getID()), sipAddress);
			startActivity(intent);
		} else {
			Bundle extras = new Bundle();
			extras.putSerializable("Contact", contact);
			extras.putSerializable("NewSipAdress", sipAddress);
			changeCurrentFragment(FragmentsAvailable.EDIT_CONTACT, extras);
		}
	}

	public void exit() {
		finish();
		stopService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_FIRST_USER && requestCode == SETTINGS_ACTIVITY) {
			if (data.getExtras().getBoolean("Exit", false)) {
				exit();
			} else {
				FragmentsAvailable newFragment = (FragmentsAvailable) data.getExtras().getSerializable("FragmentToDisplay");
				changeCurrentFragment(newFragment, null, true);
				selectMenu(newFragment);
			}
		} else if (resultCode == Activity.RESULT_FIRST_USER && requestCode == CALL_ACTIVITY) {
			getIntent().putExtra("PreviousActivity", CALL_ACTIVITY);
			boolean callTransfer = data == null ? false : data.getBooleanExtra("Transfer", false);
			if (LinphoneManager.getLc().getCallsNb() > 0) {
				initInCallMenuLayout(callTransfer);
			} else {
				resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	@Override
	protected void onPause() {
		getIntent().putExtra("PreviousActivity", 0);
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (!LinphoneService.isReady())  {
			startService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
		}

		// Remove to avoid duplication of the listeners
		LinphoneManager.removeListener(this);
		LinphoneManager.addListener(this);

		prepareContactsInBackground();

		updateMissedChatCount();
		
		//displayMissedCalls(LinphoneManager.getLc().getMissedCallsCount());

		LinphoneManager.getInstance().changeStatusToOnline();

		if(getIntent().getIntExtra("PreviousActivity", 0) != CALL_ACTIVITY){
			if (LinphoneManager.getLc().getCalls().length > 0) {
				LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
				LinphoneCall.State callState = call.getState();
				if (callState == State.IncomingReceived) {
					startActivity(new Intent(this, IncomingCallActivity.class));
				} else {
					
						if (call.getCurrentParamsCopy().getVideoEnabled()) {
							startVideoActivity(call);
						} else {
							startIncallActivity(call);
						}
					}
				}
		}
	}

	@Override
	protected void onDestroy() {
		LinphoneManager.removeListener(this);

		if (mOrientationHelper != null) {
			mOrientationHelper.disable();
			mOrientationHelper = null;
		}

		instance = null;
		super.onDestroy();
		unregisterReceiver(mBroadcast); // Neil add, 2015/01/28
		unbindDrawables(findViewById(R.id.topLayout));
		System.gc();
	}

	private void unbindDrawables(View view) {
		if (view != null && view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Bundle extras = intent.getExtras();
		if (extras != null && extras.getBoolean("GoToChat", false)) {
			LinphoneService.instance().removeMessageNotification();
			String sipUri = extras.getString("ChatContactSipUri");
			displayChat(sipUri);
		} else if (extras != null && extras.getBoolean("Notification", false)) {
			if (LinphoneManager.getLc().getCallsNb() > 0) {
				LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
				if (call.getCurrentParamsCopy().getVideoEnabled()) {
					startVideoActivity(call);
				} else {
					startIncallActivity(call);
				}
			}
		} else {
			if (dialerFragment != null) {
				if (extras != null && extras.containsKey("SipUriOrNumber")) {
					if (getResources().getBoolean(R.bool.automatically_start_intercepted_outgoing_gsm_call)) {
						((DialerFragment) dialerFragment).newOutgoingCall(extras.getString("SipUriOrNumber"));
					} else {
						((DialerFragment) dialerFragment).displayTextInAddressBar(extras.getString("SipUriOrNumber"));
					}
				} else {
					((DialerFragment) dialerFragment).newOutgoingCall(intent);
				}
			}
			if (LinphoneManager.getLc().getCalls().length > 0) {
				LinphoneCall calls[] = LinphoneManager.getLc().getCalls();
				if (calls.length > 0) {
					LinphoneCall call = calls[0];
					
					if (call != null && call.getState() != LinphoneCall.State.IncomingReceived) {
						if (call.getCurrentParamsCopy().getVideoEnabled()) {
							startVideoActivity(call);
						} else {
							startIncallActivity(call);
						}
					}
				}
				
				// If a call is ringing, start incomingcallactivity
				Collection<LinphoneCall.State> incoming = new ArrayList<LinphoneCall.State>();
				incoming.add(LinphoneCall.State.IncomingReceived);
				if (LinphoneUtils.getCallsInState(LinphoneManager.getLc(), incoming).size() > 0) {
					if (InCallActivity.isInstanciated()) {
						InCallActivity.instance().startIncomingCallActivity();
					} else {
						startActivity(new Intent(this, IncomingCallActivity.class));
					}
				}
			}
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (currentFragment == FragmentsAvailable.DIALER) {
				boolean isBackgroundModeActive = LinphonePreferences.instance().isBackgroundModeEnabled();
				if (!isBackgroundModeActive) {
					stopService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
					finish();
				} else if (LinphoneUtils.onKeyBackGoHome(this, keyCode, event)) {
					return true;
				}
			} else {
				if (isTablet()) {
					if (currentFragment == FragmentsAvailable.SETTINGS) {
						updateAnimationsState();
					}
					
					fragmentsHistory.remove(fragmentsHistory.size() - 1);
					if (fragmentsHistory.size() > 0) {
						FragmentsAvailable newFragmentType = fragmentsHistory.get(fragmentsHistory.size() - 1);
						LinearLayout ll = (LinearLayout) findViewById(R.id.fragmentContainer2);
						if (newFragmentType.shouldAddItselfToTheRightOf(currentFragment)) {
							ll.setVisibility(View.VISIBLE);
						} else {
							if (newFragmentType == FragmentsAvailable.DIALER 
									|| newFragmentType == FragmentsAvailable.ABOUT 
									|| newFragmentType == FragmentsAvailable.ABOUT_INSTEAD_OF_CHAT 
									|| newFragmentType == FragmentsAvailable.ABOUT_INSTEAD_OF_SETTINGS
									|| newFragmentType == FragmentsAvailable.SETTINGS 
									|| newFragmentType == FragmentsAvailable.ACCOUNT_SETTINGS) {
								ll.setVisibility(View.GONE);
							} else {
								ll.setVisibility(View.INVISIBLE);
							}
						}
					}
				}
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU && statusFragment != null) {
			if (event.getRepeatCount() < 1) {
				statusFragment.openOrCloseStatusBar(true);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@SuppressLint("ValidFragment")
	class AcceptNewFriendDialog extends DialogFragment {
		private Contact contact;
		private String sipUri;

		public AcceptNewFriendDialog(Contact c, String a) {
			contact = c;
			sipUri = a;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.new_friend_request_dialog, container);

			getDialog().setTitle(R.string.linphone_friend_new_request_title);

			Button yes = (Button) view.findViewById(R.id.yes);
			yes.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					acceptNewFriend(contact, sipUri, true);
				}
			});

			Button no = (Button) view.findViewById(R.id.no);
			no.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					acceptNewFriend(contact, sipUri, false);
				}
			});

			return view;
		}
	}

}

interface ContactPicked {
	void setAddresGoToDialerAndCall(String number, String name, Uri photo);
	void goToDialer();
}

//<<< LiangBin add, auto update
class UpdateManager {

  private Context mContext;

  private String updateMsg = "There is new LiveHD APK, please download it!";
  private String apkUrl = "http://autov01.vccloud.quantatw.com/Androidx86/linphone-android_beta_02.apk";
  private Dialog noticeDialog;

  private Dialog downloadDialog;
  // local store path
  private static final String savePath = "/sdcard/LiveHD/";

  private static final String saveFileName = savePath + "linphone-android.apk";

  private ProgressBar mProgress;


  private static final int DOWN_UPDATE = 1;

  private static final int DOWN_OVER = 2;

  private int progress;

  private Thread downLoadThread;

  private boolean interceptFlag = false;

  private Handler mHandler = new Handler(){
      public void handleMessage(Message msg) {
          switch (msg.what) {
              case DOWN_UPDATE:
                  mProgress.setProgress(progress);
                  break;
              case DOWN_OVER:

                  installApk();
                  break;
              default:
                  break;
          }
      };
  };

  public UpdateManager(Context context) {
      this.mContext = context;
  }

  //Main Activity invoke interface
  public void checkUpdateInfo(){
      showNoticeDialog();
  }

  public void setNewAPKName(String version) {
      String ver = "-v";
      ver = ver + version;
      apkUrl = apkUrl.replace("_beta_02", ver);
  }

  private void showNoticeDialog(){
      AlertDialog.Builder builder = new Builder(mContext);
      builder.setTitle("Software version update");
      builder.setMessage(updateMsg);
      builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              showDownloadDialog();
          }
      });
      builder.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
          }
      });
      noticeDialog = builder.create();
      noticeDialog.show();
  }

  private void showDownloadDialog(){
      AlertDialog.Builder builder = new Builder(mContext);
      builder.setTitle("Software version update");

      final LayoutInflater inflater = LayoutInflater.from(mContext);
      View v = inflater.inflate(R.layout.progress, null);
      mProgress = (ProgressBar)v.findViewById(R.id.progress);

      builder.setView(v);
      builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              interceptFlag = true;
          }
      });
      downloadDialog = builder.create();
      downloadDialog.show();

      downloadApk();
  }

  private Runnable mdownApkRunnable = new Runnable() {
      @Override
      public void run() {
          try {
              URL url = new URL(apkUrl);

              HttpURLConnection conn = (HttpURLConnection)url.openConnection();
              conn.connect();
              int length = conn.getContentLength();
              InputStream is = conn.getInputStream();

              File file = new File(savePath);
              if(!file.exists()){
                  file.mkdir();
              }
              String apkFile = saveFileName;
              File ApkFile = new File(apkFile);
              FileOutputStream fos = new FileOutputStream(ApkFile);

              int count = 0;
              byte buf[] = new byte[1024];

              do{
                  int numread = is.read(buf);
                  count += numread;
                  progress =(int)(((float)count / length) * 100);
                  //
                  mHandler.sendEmptyMessage(DOWN_UPDATE);
                  if(numread <= 0){
                      //
                      mHandler.sendEmptyMessage(DOWN_OVER);
                      break;
                  }
                  fos.write(buf,0,numread);
              }while(!interceptFlag);// stop downloading while click cancel button.

              fos.close();
              is.close();
          } catch (MalformedURLException e) {
              e.printStackTrace();
          } catch(IOException e){
              e.printStackTrace();
          }

      }
  };

  /**
   * download apk
   * @param url
   */

  private void downloadApk(){
      downLoadThread = new Thread(mdownApkRunnable);
      downLoadThread.start();
  }
  /**
   * install apk
   * @param url
   */
  private void installApk(){
      File apkfile = new File(saveFileName);
      if (!apkfile.exists()) {
          return;
      }
      Intent i = new Intent(Intent.ACTION_VIEW);
      i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
      mContext.startActivity(i);

  }
}
//>>> LiangBin add, 20150120