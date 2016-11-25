package org.linphone.setup;
/*
SetupActivity.java
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import net.ser1.stomp.Client;
import net.ser1.stomp.Listener;
import net.ser1.stomp.Message;

import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphonePreferences.AccountBuilder;
import org.linphone.LinphoneSimpleListener.LinphoneOnRegistrationStateChangedListener;
import org.linphone.R;
import org.linphone.core.LinphoneAddress.TransportType;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.mediastream.Log;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;
/**
 * @author Sylvain Berfini
 */
public class SetupActivity extends FragmentActivity implements OnClickListener {
	private static final String TAG = SetupActivity.class.toString(); // LiangBin add, 20141219
	private String QUEUE_NAME = "/queue/server"; // LiangBin add, 20141219
	private Client con; // LiangBin add, 20141219
	private static boolean bInQuanta = false; // LiangBin add, 20141231
	private static SetupActivity instance;
	//private RelativeLayout back, next, cancel;
	private SetupFragmentsEnum currentFragment;
	private SetupFragmentsEnum firstFragment;
	private Fragment fragment;
	private LinphonePreferences mPrefs;
	private boolean accountCreated = false;
	private Handler mHandler = new Handler();
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getResources().getBoolean(R.bool.isTablet) && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
		
		setContentView(R.layout.setup);
		firstFragment = getResources().getBoolean(R.bool.setup_use_linphone_as_first_fragment) ?
				SetupFragmentsEnum.LINPHONE_LOGIN : SetupFragmentsEnum.WELCOME;
        if (findViewById(R.id.fragmentContainer) != null) {
            if (savedInstanceState == null) {
            	display(firstFragment);
            } else {
            	currentFragment = (SetupFragmentsEnum) savedInstanceState.getSerializable("CurrentFragment");
            }
        }
        mPrefs = LinphonePreferences.instance();
        
        initUI();
        instance = this;
        
        //new ConnectQueueServer().execute(); // LiangBin add, 20141219, cancel on 20141222
        if(!mPrefs.getRmsflag()){
            new Thread(new Runnable() {
                public void run() {
                    if( (bInQuanta = YouAreInQuanta()) == true) {
                        Log.i(TAG, "[L.B.track] You are in Quanta");
                    }
                    else {
                        Log.i(TAG, "[L.B.track] You are out of Quanta");
                    }
                }
            }).start(); // LiangBin add, best route, 20141231
        }
        
	};
	
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
	//>>> LiangBin add, 20141231
	
	//<<< LiangBin add, stomp connection
	public class Wrapper
	{
	    public String username;
	    public String password;
	    public String rms_address;
	}
	private class ConnectQueueServer extends AsyncTask<String, Void, Wrapper> {
		private StringBuffer xmlQueue = null; // LiangBin add, 20141222
		@Override
		protected Wrapper doInBackground(String... params) {
			// AES encrypt
			//Log.d("TAG","[L.B.track] encrypted qic message is " + encryptFromJNI("qic1"));
			String enUser = encryptFromJNI((String)params[0]);
			enUser = enUser.substring(0, encryptLenFromJNI(""));
			Log.d(TAG,"[L.B.track] doInBackground(), enLen: " + enUser);
			Log.d(TAG,"[L.B.track] doInBackground(), enLen: " + encryptLenFromJNI(""));
			String enPwd = encryptFromJNI((String)params[1]);
			enPwd = enPwd.substring(0, encryptLenFromJNI(""));
			String enQueue = encryptFromJNI(String.format("/queue/%s", params[0]));
			enQueue = enQueue.substring(0, encryptLenFromJNI(""));
			// Create the consumer
			Log.d(TAG,"[L.B.track] doInBackground(), Queue: " + String.format("/queue/%s", params[0]));
			Log.d(TAG,"[L.B.track] doInBackground(), enQueue: " + enQueue);
			String request1="<request><type>Client Account Provision</type><name>inquery</name><seq></seq><id>11001</id><parameter><group>profile</group><key>version</key><value>0</value></parameter></request>";
			String rms_address=params[2];
			Wrapper ans = new Wrapper(); // LiangBin add, 20141222
			try {
				
				if(con != null) return null;
				
				//con = new Client("rms03.vccloud.quantatw.com", 80, enUser/*"db+SkYTCrEv2BV0xAmEPng=="*/, enPwd/*"4R4f2+HV8fRSjA2M5n0/FQ=="*/);
				con = new Client(rms_address, 80, enUser/*"db+SkYTCrEv2BV0xAmEPng=="*/, enPwd/*"4R4f2+HV8fRSjA2M5n0/FQ=="*/);
				// subscribe
			    HashMap headers = new HashMap();
			    headers.put( "type", "security" );
			    headers.put( "tag1", enQueue/*"cnMU8IClH0EO5plYDoztGA=="*/ ); // encrypted '/queue/qic1'
			    headers.put( "tag2", enUser/*"db+SkYTCrEv2BV0xAmEPng=="*/ ); // encrypted username
			    
				con.subscribe(QUEUE_NAME, new Listener() {
					@Override 
					public void message(Map header, String body ) {
						Log.d(TAG,"[L.B.track] onSubscribeMessage()");
					    Log.d(TAG,"[L.B.track] message is: " + body);
					    
					    //message = "\n("+ counterReceive +")<-- " + body;
					    //myHandler.post(myRunnable);
					    //counterReceive++;
					}
				}, headers);
				
				// auto provision
				//String request1="<request><type>Client Account Provision</type><name>inquery</name><seq></seq><id>11001</id><parameter><group>profile</group><key>version</key><value>0</value></parameter></request>";
				
			    HashMap header2 = new HashMap();
			    header2.put( "type", "security" );
			    header2.put( "destination", "/queue/server" ); // "/queue/server"
			    header2.put( "tag2", enUser/*"db+SkYTCrEv2BV0xAmEPng=="*/ ); // encrypted username, qic1
			    //header2.put( "receipt", "message-id-123" );
			    
			    Log.d(TAG,"[L.B.track] send message start");
				con.send( "/queue/server", request1, header2 );
				
				Thread.sleep(2500);
				if(xmlQueue == null)
					xmlQueue = new StringBuffer(); // LiangBin add, 20141222
				Message msg = con.getNext();
				while(msg != null && msg.body().length() > 0) {
					xmlQueue.append(msg.body()); // LiangBin add, 20141222
					Log.d(TAG,"[L.B.track] send message end1:" + msg.body());
					msg = con.getNext();
				}
				
				con.addErrorListener(new Listener() {
					@Override
					public void message(Map header, String message) {
						Log.d(TAG,"[L.B.track2] message()");
						// Auto-generated method stub
				    	Log.d(TAG,"onError()");
				    	Log.d(TAG,"[L.B.track2] onError() - Error message is " + message);
				    	//Log.d(TAG,"onError() - Error content is " + errorMsg.getContentAsString());					
					}
				});
			} catch (LoginException e) {
				// Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Auto-generated method stub
			//<<< LiangBin add, got username and pwd
			/*Neil ADD*/			
			if(xmlQueue==null){
				isProvisionSuccess=false;
				return null;
			}
			/*Neil ADD*/
			int u_index = xmlQueue.indexOf("<key>usernameForAuth</key><value>");
			String _username = xmlQueue.substring(u_index + "<key>usernameForAuth</key><value>".length(), u_index + "<key>usernameForAuth</key><value>".length() + 50);
			//Log.d(TAG,"[L.B.track] reduntant username: " + _username);
			int p_index = xmlQueue.indexOf("<key>passwordForAuth</key><value>");
			String _passwd = xmlQueue.substring(p_index + "<key>passwordForAuth</key><value>".length(), p_index + "<key>passwordForAuth</key><value>".length() + 50);
			//Log.d(TAG,"[L.B.track] reduntant passwd: " + _passwd);
			
			ans.username = decryptFromJNI(_username.substring(0, _username.indexOf("</")));
			Log.d(TAG,"[L.B.track] doInBackground(), id Len: " + decryptLenFromJNI(""));
			Log.d(TAG,"[L.B.track] the ontent of, uid: " + byteArrayToHex(ans.username.getBytes()) );
			//Log.d(TAG,"[L.B.track] encrypted username: " + _username.substring(0, _username.indexOf("</")) );
			ans.password = decryptFromJNI(_passwd.substring(0, _passwd.indexOf("</")));
			Log.d(TAG,"[L.B.track] doInBackground(), pwd Len: " + decryptLenFromJNI(""));
			Log.d(TAG,"[L.B.track] the content of, pass: " + byteArrayToHex(ans.password.getBytes()) );
			//Log.d(TAG,"[L.B.track] encrypted passwd: " + _passwd.substring(0, _passwd.indexOf("</")) );
			//>>> LiangBin add, 20141222
			//<<< LiangBin add, cover the server issue
			//if(ans.username.length() >= 16) {
				int trim_index = ans.username.indexOf(ans.username.charAt(ans.username.length()-1));
				ans.username = ans.username.substring(0, trim_index);
			//}
			if(ans.password.length() > 8) {
				int trim_index2 = ans.password.indexOf(ans.password.charAt(ans.password.length()-1));
				ans.password = ans.password.substring(0, trim_index2);
			}
			ans.rms_address=rms_address;
			//>>> LiangBin add, 20141223
			Log.d(TAG,"[L.B.track] end of doinbackground, uid: " + ans.username);
			Log.d(TAG,"[L.B.track] end of doinbackground, pass: " + ans.password);
			return ans;
		}
		
		@Override
	    protected void onPostExecute(Wrapper result) {
	        super.onPostExecute(result);
			/*Neil ADD*/
	        if(result==null){
	        	Toast.makeText(SetupActivity.this, "Invalid Username or Password!", Toast.LENGTH_LONG).show();
	        	return;
	        }
			/*Neil ADD*/
	        Log.d(TAG,"[L.B.track] start to onPostExecute, uid: " + byteArrayToHex(result.username.getBytes()) );
			Log.d(TAG,"[L.B.track] start to onPostExecute, pass: " + byteArrayToHex(result.password.getBytes()) );
			isProvisionSuccess = true;//Neil ADD
			logIn(result.username, result.password,"quantatw.com", result.rms_address, false);
			//logIn("qic1", "78050583", "quantatw.com", false);
	    }
		
		public String byteArrayToHex(byte[] a) {
			   StringBuilder sb = new StringBuilder(a.length * 2);
			   for(byte b: a)
			      sb.append(String.format("%02x:", b & 0xff));
			   return sb.toString();
			}
	}
	//>>> LiangBin add, 20141219
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("CurrentFragment", currentFragment);
		super.onSaveInstanceState(outState);
	}
	
	public static SetupActivity instance() {
		return instance;
	}
	
	private void initUI() {
/*
		back = (RelativeLayout) findViewById(R.id.setup_back);
		back.setOnClickListener(this);
		next = (RelativeLayout) findViewById(R.id.setup_next);
		next.setOnClickListener(this);
		cancel = (RelativeLayout) findViewById(R.id.setup_cancel);
		cancel.setOnClickListener(this);
*/
	}
	
	private void changeFragment(Fragment newFragment) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		
//		transaction.addToBackStack("");
		transaction.replace(R.id.fragmentContainer, newFragment);
		
		transaction.commitAllowingStateLoss();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		/*
		if (id == R.id.setup_cancel) {
			LinphonePreferences.instance().firstLaunchSuccessful();
			if (getResources().getBoolean(R.bool.setup_cancel_move_to_back)) {
				moveTaskToBack(true);
			} else {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		} else if (id == R.id.setup_next) {
			if (firstFragment == SetupFragmentsEnum.LINPHONE_LOGIN) {
				LinphoneLoginFragment linphoneFragment = (LinphoneLoginFragment) fragment;
				linphoneFragment.linphoneLogIn();
			} else {
				if (currentFragment == SetupFragmentsEnum.WELCOME) {
					MenuFragment fragment = new MenuFragment();
					changeFragment(fragment);
					currentFragment = SetupFragmentsEnum.MENU;
					
					next.setVisibility(View.GONE);
					back.setVisibility(View.VISIBLE);
				} else if (currentFragment == SetupFragmentsEnum.WIZARD_CONFIRM) {
					finish();
				}
			}
		} else if (id == R.id.setup_back) {
			onBackPressed();
		}
		*/
	}

	@Override
	public void onBackPressed() {
		if (currentFragment == firstFragment) {
			LinphonePreferences.instance().firstLaunchSuccessful();
			if (getResources().getBoolean(R.bool.setup_cancel_move_to_back)) {
				moveTaskToBack(true);
			} else {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		}
		if (currentFragment == SetupFragmentsEnum.MENU) {
			/*
			WelcomeFragment fragment = new WelcomeFragment();
			changeFragment(fragment);
			currentFragment = SetupFragmentsEnum.WELCOME;
			
			next.setVisibility(View.VISIBLE);
			back.setVisibility(View.GONE);
			*/
		} else if (/* currentFragment == SetupFragmentsEnum.GENERIC_LOGIN  
				|| */currentFragment == SetupFragmentsEnum.LINPHONE_LOGIN 
				|| currentFragment == SetupFragmentsEnum.WIZARD 
				|| currentFragment == SetupFragmentsEnum.REMOTE_PROVISIONING) {
			MenuFragment fragment = new MenuFragment();
			changeFragment(fragment);
			currentFragment = SetupFragmentsEnum.MENU;
		} else if (currentFragment == SetupFragmentsEnum.WELCOME) {
			finish();
		}
	}

	private void launchEchoCancellerCalibration(boolean sendEcCalibrationResult) {
		boolean needsEchoCalibration = LinphoneManager.getLc().needsEchoCalibration();
		if (needsEchoCalibration && mPrefs.isFirstLaunch()) {
			EchoCancellerCalibrationFragment fragment = new EchoCancellerCalibrationFragment();
			fragment.enableEcCalibrationResultSending(sendEcCalibrationResult);
			changeFragment(fragment);
			currentFragment = SetupFragmentsEnum.ECHO_CANCELLER_CALIBRATION;
			/*
			back.setVisibility(View.VISIBLE);
			next.setVisibility(View.GONE);
			next.setEnabled(false);
			cancel.setEnabled(false);
			*/
		} else {
			if (mPrefs.isFirstLaunch()) {
				mPrefs.setEchoCancellation(false);
			}
			success();
		}		
	}

	private void logIn(String username, String password,String domain, String rms_address, boolean sendEcCalibrationResult) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && getCurrentFocus() != null) {
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}

        saveCreatedAccount(username, password,domain, rms_address);

        //if (LinphoneManager.getLc().getDefaultProxyConfig() != null ) {
        if (LinphoneManager.getLc().getDefaultProxyConfig() != null && isProvisionSuccess) {
			launchEchoCancellerCalibration(sendEcCalibrationResult);
		}
	}
	
	
	private LinphoneOnRegistrationStateChangedListener registrationListener = new LinphoneOnRegistrationStateChangedListener() {
		public void onRegistrationStateChanged(LinphoneProxyConfig proxy, RegistrationState state, String message) {
			if (state == RegistrationState.RegistrationOk) {
				LinphoneManager.removeListener(registrationListener);
				
				if (LinphoneManager.getLc().getDefaultProxyConfig() != null) {
					mHandler .post(new Runnable () {
						public void run() {
							launchEchoCancellerCalibration(true);
						}
					});
				}
			} else if (state == RegistrationState.RegistrationFailed) {
				LinphoneManager.removeListener(registrationListener);
				mHandler.post(new Runnable () {
					public void run() {
						Toast.makeText(SetupActivity.this, getString(R.string.first_launch_bad_login_password), Toast.LENGTH_LONG).show();
					}
				});
			}
		}
	};
	public void checkAccount(String username, String password, String domain) {
		LinphoneManager.removeListener(registrationListener);
		LinphoneManager.addListener(registrationListener);
		
		saveCreatedAccount(username, password, domain,"");
	}

	public void linphoneLogIn(String username, String password, boolean validate) {
		if (validate) {
			checkAccount(username, password, getString(R.string.default_domain));
		} else {
			logIn(username, password, getString(R.string.default_domain),"", true);
		}
	}

	public void genericLogIn(String username, String password, String rms_address) {
		new ConnectQueueServer().execute(username, password,rms_address); // LiangBin add, instead of logIn below, 20141222
		//logIn(username, password, domain, false);
	}
	/*Neil add, 2015/01/06*/
	private boolean isProvisionSuccess = false;
	public void Q_genericLogIn(String username, String password, String rms_address, Boolean flag) {
		mPrefs.setRmsflag(flag);
		if(flag){
			//Neil
			isProvisionSuccess = false;
			logIn(username, password,"", rms_address, false);
		}
		else{
			// LiangBin
			isProvisionSuccess = false;//Neil ADD
			new ConnectQueueServer().execute(username, password,rms_address); // LiangBin add, instead of logIn below, 20141222
		}
	}
	/*Neil add, 2015/01/06*/
	private void display(SetupFragmentsEnum fragment) {
		displayLoginGeneric(); // LiangBin add, enter log in page directly, 20141218
		/*
		switch (fragment) {
		case WELCOME:
			displayWelcome();
			break;
		case LINPHONE_LOGIN:
			displayLoginLinphone();
			break;
		default:
			throw new IllegalStateException("Can't handle " + fragment);
		}
		*/
	}

	public void displayWelcome() {
		fragment = new WelcomeFragment();
		changeFragment(fragment);
		currentFragment = SetupFragmentsEnum.WELCOME;
	}

	public void displayLoginGeneric() {
		fragment = new GenericLoginFragment();
		changeFragment(fragment);
		currentFragment = SetupFragmentsEnum.GENERIC_LOGIN;
	}
	
	public void displayLoginLinphone() {
		fragment = new LinphoneLoginFragment();
		changeFragment(fragment);
		currentFragment = SetupFragmentsEnum.LINPHONE_LOGIN;
	}

	public void displayWizard() {
		fragment = new WizardFragment();
		changeFragment(fragment);
		currentFragment = SetupFragmentsEnum.WIZARD;
	}

	public void displayRemoteProvisioning() {
		fragment = new RemoteProvisioningFragment();
		changeFragment(fragment);
		currentFragment = SetupFragmentsEnum.REMOTE_PROVISIONING;
	}
	
	public void saveCreatedAccount(String username, String password, String domain,String rms_address) {
		if (accountCreated)
			return;
/*Neil Modified,2014/01/05*/
		if(mPrefs.getRmsflag()){//Neil
			Qprovision Qprovision=new Qprovision(getApplicationContext());
			Qprovision.execute(username,password,rms_address);
			Qprovision.profileParam p= new Qprovision.profileParam();
			int count=0;
			Qprovision.usedprofileId=-1;
    		while(count<10 && Qprovision.usedprofileId==-1 && !Qprovision.getStatus().equals(AsyncTask.Status.FINISHED) && !Qprovision.isCancelled()){
    			try{
    				Thread.sleep(1000);
   					count++;
    			}catch(InterruptedException e){
    				Log.d("[Neil]getUsedProfile():Thread.sleep.Exception_error="+e);
    				e.printStackTrace();
    			}
    		}
    		if(Qprovision.usedprofileId==-1){
    			/*
    			Log.d("[Neil]provision error:ping error,status="+Qprovision.getStatus());
    			Toast.makeText(this, "provision error", Toast.LENGTH_LONG);
    			Qprovision.cancel(true);
    			*/
    			isProvisionSuccess=false;
    			return;
    		}
    		p =Qprovision.getUsedProfile();
			int total_Account=mPrefs.getAccountCount();
			for(int i=0;i<total_Account;i++){
				mPrefs.deleteAccount(0);
				Log.d("[Neil]total_account:"+total_Account+",delete_account_idx:"+0);
			}
			//boolean isMainAccountLinphoneDotOrg = domain.equals(getString(R.string.default_domain));
			//boolean useLinphoneDotOrgCustomPorts = getResources().getBoolean(R.bool.use_linphone_server_ports);
			AccountBuilder builder = new AccountBuilder(LinphoneManager.getLc())
			.setUsername(p.usernameForAuth)
			.setDomain(p.sip_registrarAddress)
			.setPassword(p.passwordForAuth);
			
			Log.d("[Neil]setting:proxy="+p.sip_outboundProxyAddress+",iceEnable="+(p.sip_useStun=="true")+",StunServer="+p.sip_stunServerAddress+",sip_port="+p.sip_registrarPort+",transport="+p.sip_transportType);		
    		if(p.sip_transportType.equals("TLS")){
    			builder.setTransport(TransportType.LinphoneTransportTls);
    		}
    		else if(p.sip_transportType.equals("TCP")){
    			builder.setTransport(TransportType.LinphoneTransportTcp);
    		}
    		else{
    			builder.setTransport(TransportType.LinphoneTransportUdp);
    		}
			
			builder.setExpires("604800")
			.setOutboundProxyEnabled(true)
			.setProxy(p.sip_outboundProxyAddress);
			
			if(p.sip_useStun.equals("true")){
				Log.d("[Neil]useStun="+p.sip_useStun+",setting:setStunServer="+p.sip_stunServerAddress);
				mPrefs.setStunServer(p.sip_stunServerAddress);
			}else{
				Log.d("[Neil]useStun="+p.sip_useStun+",setting:setStunServer=\"\",null");
				mPrefs.setStunServer("");
			}
			
			Log.d("[Neil]IceEnabled="+p.sip_iceEnabled.equals("true"));
			mPrefs.setIceEnabled(p.sip_iceEnabled.equals("true"));
			
			/*
			mPrefs.setSipPort(5060);
			mPrefs.useRandomPort(false);	
			*/
			mPrefs.setRmsid(username);
			mPrefs.setRmspw(password);
			mPrefs.setRmsAddress(rms_address);
			mPrefs.setRmsport(Integer.parseInt(p.sip_registrarPort_alg));
			
			isProvisionSuccess=true;
			try {
				builder.saveNewAccount();
				accountCreated = true;
			} catch (LinphoneCoreException e) {
				e.printStackTrace();
			}
		}
		else{//LiangBin
			boolean isMainAccountLinphoneDotOrg = domain.equals(getString(R.string.default_domain));
			boolean useLinphoneDotOrgCustomPorts = getResources().getBoolean(R.bool.use_linphone_server_ports);
			AccountBuilder builder = new AccountBuilder(LinphoneManager.getLc())
			.setUsername(username)
			.setDomain(domain)
			.setPassword(password);
			
			if (isMainAccountLinphoneDotOrg && useLinphoneDotOrgCustomPorts) {
				if (getResources().getBoolean(R.bool.disable_all_security_features_for_markets)) {
					builder.setProxy(domain + ":5228")
					.setTransport(TransportType.LinphoneTransportTcp);
				}
				else {
					builder.setProxy(domain + ":5223")
					.setTransport(TransportType.LinphoneTransportTls);
				}
				
				builder.setExpires("604800")
				.setOutboundProxyEnabled(true)
				.setAvpfEnabled(true)
				.setAvpfRRInterval(3)
				.setQualityReportingCollector("sip:voip-metrics@sip.linphone.org")
				.setQualityReportingEnabled(true)
				.setQualityReportingInterval(180)
				.setRealm("sip.linphone.org");
				
				
				mPrefs.setStunServer(getString(R.string.default_stun));
				mPrefs.setIceEnabled(true);
				mPrefs.setPushNotificationEnabled(true);
			} else {
				String forcedProxy = getResources().getString(R.string.setup_forced_proxy);
				if (!TextUtils.isEmpty(forcedProxy)) {
					builder.setProxy(forcedProxy)
					.setOutboundProxyEnabled(true)
					.setAvpfRRInterval(5);
				}
			}
			
			if (getResources().getBoolean(R.bool.enable_push_id)) {
				String regId = mPrefs.getPushNotificationRegistrationID();
				String appId = getString(R.string.push_sender_id);
				if (regId != null && mPrefs.isPushNotificationEnabled()) {
					String contactInfos = "app-id=" + appId + ";pn-type=google;pn-tok=" + regId;
					builder.setContactParameters(contactInfos);
				}
			}
			
			//<<< LiangBin add, reset configuration
			builder.setExpires("604800")
			.setOutboundProxyEnabled(true)
			.setProxy(bInQuanta?"sipprx03.vccloud.quantatw.com":"sipsrv03.vccloud.quantatw.com"); // LiangBin add, register proxy while in Quanta, 20141231
			
			mPrefs.setStunServer("stun03.vccloud.quantatw.com");
			mPrefs.setIceEnabled(!bInQuanta); // LiangBin add, disable ICE while in Quanta, 20141231
			//>>> LiangBin add, 20141218
			
			try {
				builder.saveNewAccount();
				accountCreated = true;
			} catch (LinphoneCoreException e) {
				e.printStackTrace();
			}
		}
	}

	public void displayWizardConfirm(String username) {
		WizardConfirmFragment fragment = new WizardConfirmFragment();
		
		Bundle extras = new Bundle();
		extras.putString("Username", username);
		fragment.setArguments(extras);
		changeFragment(fragment);
		
		currentFragment = SetupFragmentsEnum.WIZARD_CONFIRM;
		/*
		next.setVisibility(View.VISIBLE);
		next.setEnabled(false);
		back.setVisibility(View.GONE);
		*/
	}
	
	public void isAccountVerified() {
		Toast.makeText(this, getString(R.string.setup_account_validated), Toast.LENGTH_LONG).show();
		launchEchoCancellerCalibration(true);
	}

	public void isEchoCalibrationFinished() {
		success();
	}
	
	public void success() {
		mPrefs.firstLaunchSuccessful();
		setResult(Activity.RESULT_OK);
		finish();
	}
	
    public native String  encryptFromJNI(String rawData);
    public native String  decryptFromJNI(String encryptedData);
    public native int  encryptLenFromJNI(String rawData);
    public native int  decryptLenFromJNI(String encryptedData);
    
    public native String  unimplementedStringFromJNI();
	
    static {
        System.loadLibrary("aes-jni");
    }
}
