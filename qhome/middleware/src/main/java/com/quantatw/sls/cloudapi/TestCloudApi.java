package com.quantatw.sls.cloudapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quantatw.sls.json.ExangeJson;
import com.quantatw.sls.pack.base.BaseReqPack;
import com.quantatw.sls.pack.base.BaseResPack;

import android.util.Log;

public abstract class TestCloudApi<T extends BaseReqPack,P extends BaseResPack> {

	public static String CloudAddress = "http://cloudss.azure-mobile.net";	
	
	public static String API_USERREGISTER = "/User/Register";
	public static String API_USERLOGIN = "/User/Login/1";
	public static String API_FBUSERLOGIN = "/User/Login/2";
	public static String API_ADDDEVICE = "/Device";
	
	protected Gson gson;
	
	public TestCloudApi()
	{
		gson = new GsonBuilder().create();
	}
	
	protected String PostBaseReq(String URL, String in) {
		String data = in;

//		List<NameValuePair> nvpList = new ArrayList<NameValuePair>(1);
//		BasicNameValuePair bnvp = new BasicNameValuePair("ReqData", data);
//		nvpList.add(bnvp);

		
		
		HttpParams params = new BasicHttpParams();

		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		SchemeRegistry sr = new SchemeRegistry();
		sr.register(new Scheme("http", PlainSocketFactory.getSocketFactory(),
				80));
		
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, sr);

		HttpClient httpClient = new DefaultHttpClient(cm, params);
		
		HttpPost httpPost = null;
		try {
				
				httpPost = new HttpPost(new URI(CloudAddress + URL));
				Log.d("CloudAPI", "URL: "+CloudAddress + URL);

		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		httpPost.setHeader("Accept-Language","en");

		httpPost.setHeader("User-Agent", "CloudAPI");

		try {

			httpPost.setEntity(new StringEntity(data));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		HttpResponse httpResp = null;
		try {
			httpResp = httpClient.execute(httpPost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return null;
		}
		// for( Header header : httpResp.getAllHeaders()) {
		// // cookie = header.getValue();
		// System.out.println( header);
		// }

		String resp = null;
		try {
			resp = EntityUtils.toString(httpResp.getEntity());
//			System.out.println("BASE64 " + resp);
			Log.d("CloudAPI","Resp: "+ resp);
			
			
			if(resp != null)
			{
				Log.d("CloudAPI","StatusCode: "+ httpResp.getStatusLine().getStatusCode());
//				if(resp.contains("503 Service Unavailable"))
//					return null;
				if(httpResp.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
					return null;

				
			}
			
			
				
		}catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return null;
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return null;
		}
		Log.d("CloudAPI","Resp: "+ resp);


		return resp;
	}
	
	protected String GetBaseReq(String URL) {


//		List<NameValuePair> nvpList = new ArrayList<NameValuePair>(1);
//		BasicNameValuePair bnvp = new BasicNameValuePair("ReqData", data);
//		nvpList.add(bnvp);

		
		
		HttpParams params = new BasicHttpParams();

		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		SchemeRegistry sr = new SchemeRegistry();
		sr.register(new Scheme("http", PlainSocketFactory.getSocketFactory(),
				80));
		
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, sr);

		HttpClient httpClient = new DefaultHttpClient(cm, params);
		
		HttpGet httpGet = null;
		try {
				
			httpGet = new HttpGet(new URI(CloudAddress + URL ));
				Log.d("CloudAPI", "URL: "+CloudAddress + URL );

		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		httpGet.setHeader("Accept-Language","en");

		httpGet.setHeader("User-Agent", "CloudAPI");

		

		HttpResponse httpResp = null;
		try {
			httpResp = httpClient.execute(httpGet);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return null;
		}
		// for( Header header : httpResp.getAllHeaders()) {
		// // cookie = header.getValue();
		// System.out.println( header);
		// }

		String resp = null;
		try {
			resp = EntityUtils.toString(httpResp.getEntity());
//			System.out.println("BASE64 " + resp);
			Log.d("CloudAPI","Resp: "+ resp);
			
			
			
			if(resp != null)
			{
				
//				if(resp.contains("503 Service Unavailable"))
					
				Log.d("CloudAPI","StatusCode: "+ httpResp.getStatusLine().getStatusCode());
				if(httpResp.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
					return null;
			}
			
			
				
		}catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return null;
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return null;
		}
		Log.d("CloudAPI","Resp: "+ resp);


		return resp;
	}
	
	protected String DeleteBaseReq(String URL ) {


//		List<NameValuePair> nvpList = new ArrayList<NameValuePair>(1);
//		BasicNameValuePair bnvp = new BasicNameValuePair("ReqData", data);
//		nvpList.add(bnvp);

		
		
		HttpParams params = new BasicHttpParams();

		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		SchemeRegistry sr = new SchemeRegistry();
		sr.register(new Scheme("http", PlainSocketFactory.getSocketFactory(),
				80));
		
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, sr);

		HttpClient httpClient = new DefaultHttpClient(cm, params);
		
		HttpDelete httpDelete = null;
		try {
		
			httpDelete = new HttpDelete(new URI(CloudAddress + URL));
				Log.d("CloudAPI", "URL: "+CloudAddress + URL);

		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		httpDelete.setHeader("Accept-Language","en");

		httpDelete.setHeader("User-Agent", "CloudAPI");

		

		HttpResponse httpResp = null;
		try {
			httpResp = httpClient.execute(httpDelete);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return null;
		}
		// for( Header header : httpResp.getAllHeaders()) {
		// // cookie = header.getValue();
		// System.out.println( header);
		// }

		String resp = null;
		try {
			resp = EntityUtils.toString(httpResp.getEntity());
//			System.out.println("BASE64 " + resp);
			Log.d("CloudAPI","Resp: "+ resp);
			
			
			if(resp != null)
			{
				
				Log.d("CloudAPI","StatusCode: "+ httpResp.getStatusLine().getStatusCode());
//				if(resp.contains("503 Service Unavailable"))
//					return null;
				if(httpResp.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
					return null;

				
			}
			
			
				
		}catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return null;
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return null;
		}
		Log.d("CloudAPI","Resp: "+ resp);


		return resp;
	}
	
	protected P execute(T reqPack,P resPack)
	{
		

		
		String ret = Request(reqPack);
		
		ExangeJson<P> mExangeJson = new ExangeJson<P>() ;
		
		
		
		resPack = mExangeJson.Exe(ret, resPack);
		
		return resPack;
	}
	
	abstract String Request(T reqPack);
	
}
