package org.linphone.setup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.mediastream.Log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * @author Neil Chen Quanta Qic provision
 */
@SuppressLint("TrulyRandom")
public class Qprovision extends AsyncTask<String, Integer, Void> {
	public static class profileParam {
		public String profileid;
		public String identityUsername;
		public String usernameForAuth;
		public String passwordForAuth;
		public String sip_registrarAddress;
		public String sip_registrarPort;
		public String sip_useRegistrar;
		public String sip_outboundProxyAddress;
		public String sip_outBoundProxyPort;
		public String sip_useOutboundProxy;
		public String sip_transportType;
		public String sip_tlsRegisterMode;
		public String sip_registrarAddress_alg;
		public String sip_registrarPort_alg;
		public String sip_stunServerAddress;
		public String sip_stunServerPort;
		public String sip_useStun;
		public String sip_iceTurnServerAddress;
		public String sip_iceTurnServerUserName;
		public String sip_iceTurnServerPassword;
		public String sip_useTurn;
		public String sip_iceEnabled;
		public String bEnableVideo;
		public String bUseH264;
		public String bBfcpEnabled;
		public String bPresenceEnabled;
		public String bIMsgEnabled;
		public String bestRouteEnable;
		public String mtu_detectAddress;
	}

	protected static JSONObject provision_json;
	public static boolean usehttp = true;
	public static boolean flag_provision = false;
	public static boolean flag_ping = false;
	public static boolean flag_get_data = false;
	public static String userId = null;
	public static String password = null;
	public static profileParam[] profile;
	public static int numberofprofile = 0;
	public static int usedprofileId = -1;

	private static Context mContext;
	private static int connect_timeout=3000;

	public Qprovision(Context context) {
		mContext = context;
	}

	/*
	 * LIVEHD-SSteven, 2014/04/24, create javax.net.ssl.SSLSocketFactory to use
	 * by HttpUrlConnection <--
	 */
	public static javax.net.ssl.SSLSocketFactory getSSLSocketFactory() throws Exception {
		final java.security.cert.X509Certificate[] _AcceptedIssuers = new java.security.cert.X509Certificate[] {};
		try {
			SSLContext ctx = SSLContext.getInstance("SSL");
			X509TrustManager tm = new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return _AcceptedIssuers;
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}
			};

			ctx.init(null, new TrustManager[] { tm }, new SecureRandom());
			return ctx.getSocketFactory();
		} catch (Exception e) {
			throw e;
		}
	}

	/*
	 * LIVEHD-SSteven, 2014/04/24, create javax.net.ssl.SSLSocketFactory to use
	 * by HttpUrlConnection -->
	 */
	public static DefaultHttpClient getSecuredHttpClient(HttpClient httpClient) throws Exception {
		final java.security.cert.X509Certificate[] _AcceptedIssuers = new java.security.cert.X509Certificate[] {};
		try {
			SSLContext ctx = SSLContext.getInstance("SSL");
			X509TrustManager tm = new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return _AcceptedIssuers;
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}
			};

			ctx.init(null, new TrustManager[] { tm }, new SecureRandom());
			SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = httpClient.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));

			return new DefaultHttpClient(ccm, httpClient.getParams());
		} catch (Exception e) {
			throw e;
		}
	}

	public static class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("SSL");

		public MySSLSocketFactory(SSLContext context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
			super(null);
			sslContext = context;
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

	@Override
	protected Void doInBackground(String... params) {
		// public static profileParam[] getProfiles(String p1,String p2) {
		// TODO Auto-generated method stub
		if (flag_provision) {
			publishProgress(0);
			return null;
		}
		flag_provision = true;
		String userId = (String) params[0];// p1;
		String password = (String) params[1];// p2;
		String rms_address = (String) params[2];
		Log.d("[Neil]provision start!!");
		// connect to server & get json file
		LinphonePreferences mPrefs = LinphonePreferences.instance();
		String req_url="";
		/*
		if(mPrefs.isDebugMode()){
			req_url = "https://rms.vccloud.quantatw.com";
		}else{
			req_url = "https://rms03.vccloud.quantatw.com";
			//req_url = "https://rms.vccloud.quantatw.com";
		}
		*/
		if(rms_address.indexOf("https://")!=-1){
			req_url=rms_address;
		}
		else if(rms_address.indexOf("http://")!=-1){
			req_url=rms_address.replace("http://", "https://");
		}
		else{
			req_url="https://"+rms_address;
		}
		Log.d("[Neil]connect to rms server:"+req_url);
		String json_str="";
		DefaultHttpClient client;
		
		try {
			client = getSecuredHttpClient(new DefaultHttpClient());

			HttpPost post = new HttpPost(req_url);
			List<NameValuePair> post_params = new ArrayList<NameValuePair>();
			post_params.add(new BasicNameValuePair("employeeid", userId));
			post_params.add(new BasicNameValuePair("password", password));
			post.setEntity(new UrlEncodedFormEntity(post_params, HTTP.UTF_8));
		
			HttpResponse response = client.execute(post);
			HttpEntity resEntity = response.getEntity();
			json_str = EntityUtils.toString(resEntity);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("[Neil]UnsupportedEncodingException::Exception_error=" + e);
			publishProgress(1);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("[Neil]ClientProtocolException::Exception_error=" + e);
			publishProgress(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("[Neil]IOException::Exception_error=" + e);
			publishProgress(1);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("[Neil]getSecuredHttpClient::Exception_error=" + e);
			publishProgress(1);
		}
		
		if (json_str == "") {
			Log.d("[Neil]json string is null!");
			publishProgress(1);
			cancel(true);
			return null;
		}
		
		//Log.d("[Neil]","[Neil]json_str="+json_str);
		// parse json
		try{
			provision_json = new JSONObject(json_str);
			String response_str = provision_json.getString("response");
	
			flag_get_data=true; 
			/*
			String id = new	JSONObject(response_str).getString("id");
			String status = new	JSONObject(response_str).getString("status"); 
			String reason = new	JSONObject(response_str).getString("reason"); 
			String version = new JSONObject(response_str).getString("version"); 
			Log.d("[Neil]get provision profile success!!id="+id+",status="+status+",reason="+reason+",version="+version);
			if(Integer.parseInt(status)!=0){
				Log.d("[Neil]get provision profile error!!");
				publishProgress(4);
				cancel(true);
				return null;
			}
			*/
			JSONArray profile_jsonarray = new JSONObject(response_str).getJSONArray("profile");
			profile=new profileParam[profile_jsonarray.length()]; 
			numberofprofile = profile_jsonarray.length(); 
			for(int	i=0;i<profile_jsonarray.length();i++){ 
				
				JSONObject curr_profile = profile_jsonarray.getJSONObject(i); 
				profile[i]= new profileParam();
				profile[i].profileid = curr_profile.getString("profileid");
				profile[i].identityUsername = curr_profile.getString("identityUsername");
				profile[i].usernameForAuth = curr_profile.getString("usernameForAuth"); 
				profile[i].passwordForAuth = curr_profile.getString("passwordForAuth");
				profile[i].sip_registrarAddress = curr_profile.getString("sip_registrarAddress");
				profile[i].sip_registrarPort = curr_profile.getString("sip_registrarPort");
				profile[i].sip_useRegistrar = curr_profile.getString("sip_useRegistrar");
				profile[i].sip_outboundProxyAddress = curr_profile.getString("sip_outboundProxyAddress");
				profile[i].sip_outBoundProxyPort = curr_profile.getString("sip_outBoundProxyPort");
				profile[i].sip_useOutboundProxy = curr_profile.getString("sip_useOutboundProxy");
				profile[i].sip_transportType = curr_profile.getString("sip_transportType");
				profile[i].sip_tlsRegisterMode = curr_profile.getString("sip_tlsRegisterMode");
				profile[i].sip_registrarAddress_alg = curr_profile.getString("sip_registrarAddress_alg");
				profile[i].sip_registrarPort_alg = curr_profile.getString("sip_registrarPort_alg");
				profile[i].sip_stunServerAddress = curr_profile.getString("sip_stunServerAddress");
				profile[i].sip_stunServerPort = curr_profile.getString("sip_stunServerPort"); 
				profile[i].sip_useStun = curr_profile.getString("sip_useStun");
				profile[i].sip_iceTurnServerAddress = curr_profile.getString("sip_iceTurnServerAddress");
				profile[i].sip_iceTurnServerUserName = curr_profile.getString("sip_iceTurnServerUserName");
				profile[i].sip_iceTurnServerPassword = curr_profile.getString("sip_iceTurnServerPassword"); 
				profile[i].sip_useTurn = curr_profile.getString("sip_useTurn"); 
				profile[i].sip_iceEnabled = curr_profile.getString("sip_iceEnabled"); 
				profile[i].bEnableVideo = curr_profile.getString("bEnableVideo"); 
				profile[i].bUseH264 = curr_profile.getString("bUseH264"); 
				profile[i].bBfcpEnabled = curr_profile.getString("bBfcpEnabled"); 
				profile[i].bPresenceEnabled = curr_profile.getString("bPresenceEnabled"); 
				profile[i].bestRouteEnable = curr_profile.getString("bestRouteEnable"); 
				profile[i].mtu_detectAddress = curr_profile.getString("mtu_detectAddress");
			  
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("[Neil]JSONException::Exception_error=" + e);
			publishProgress(2);
			cancel(true);
			return null;
		}
		for (int i = 0; i < numberofprofile; i++) {
			Log.d("[Neil]start ping "+ profile[i].sip_outboundProxyAddress);
			Boolean tmp=testserver(profile[i].sip_outboundProxyAddress,Integer.parseInt(profile[i].sip_outBoundProxyPort),connect_timeout);
			Log.d("[Neil]ping="+ profile[i].sip_outboundProxyAddress+":"+profile[i].sip_outBoundProxyPort+",result="+tmp);
			if(tmp){			
				usedprofileId=i;
				Log.d("[Neil]use profileid:"+usedprofileId);
				break;
			}
		}
		if(usedprofileId==-1){
			Log.d("[Neil]use profileid:"+usedprofileId+",ping error");
			publishProgress(3);
			cancel(true);
		}
		return null;

	}

	private static InetAddress isResolvable(String hostname) {
		try {
			return InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	private static boolean canConnect(InetAddress address, int port, int timeout) {
		Socket socket = new Socket();
		SocketAddress Socketaddress = new InetSocketAddress(address, port);
		try {
			socket.connect(Socketaddress, timeout);
			;
		} catch (IOException e) {
			return false;
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public static boolean testserver(String hostname, int port, int timeout) {
		InetAddress addr = isResolvable(hostname);
		if (addr != null) {
			if (canConnect(addr, port, timeout)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onProgressUpdate(Integer... value) {
		//Log.d("[Neil]onProgress,value=" + value[0]);
		switch (value[0]) {
		case 0:// provision already running
			// Toast.makeText(mContext, "provision already running", Toast.LENGTH_SHORT).show();
			Log.d("[Neil]provision already running!");
			break;
		case 1://provisioin error
			//Toast.makeText(mContext, "provisioin error!", Toast.LENGTH_LONG).show();
			Toast.makeText(mContext, "Invalid Username or Password!", Toast.LENGTH_LONG).show();
			Log.d("[Neil]provisioin error!");
			break;
		case 2://parse json error
			//Toast.makeText(mContext, "parse json error!", Toast.LENGTH_LONG).show();
			Toast.makeText(mContext, "Invalid Username or Password!", Toast.LENGTH_LONG).show();
			Log.d("[Neil]parse json error!");
			break;
		case 3://ping error
			//Toast.makeText(mContext, "", Toast.LENGTH_LONG).show();
			Log.d("[Neil]ping error!");
			break;
		case 4:
			Toast.makeText(mContext, "Invaild username or password!!", Toast.LENGTH_LONG).show();
			break;
		default:
			Log.d("[Neil]onProgress,error value=" + value[0]);
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		Log.d("[Neil]onPostExecute");
		flag_provision = false;
		super.onPostExecute(result);
		// Toast.makeText(mContext, "onPostExecute", Toast.LENGTH_SHORT).show();
		return;
	}

	@Override
	protected void onCancelled() {
		Log.d("[Neil]onCancelled");
		flag_provision = false;
		super.onCancelled();
		//Toast.makeText(mContext, "provision inturrept!!", Toast.LENGTH_SHORT).show();
		return;
	}

	public static int getNumOfProfile() {
		return numberofprofile;
	}

	public static profileParam getProfile(int i) {
		return profile[i];
	}

	public static profileParam getUsedProfile() {
		if (usedprofileId == -1) {
			return null;
		}
		return profile[usedprofileId];
	}
}