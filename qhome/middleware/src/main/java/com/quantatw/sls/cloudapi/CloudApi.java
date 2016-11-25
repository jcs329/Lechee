package com.quantatw.sls.cloudapi;


import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.Schedule;
import com.quantatw.sls.json.ExangeJson;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.LanguageType;
import com.quantatw.sls.pack.Weather.CityListResPack;
import com.quantatw.sls.pack.Weather.TownListResPack;
import com.quantatw.sls.pack.Weather.WeatherDataResPack;
import com.quantatw.sls.pack.account.AccountInfoCheckReqPack;
import com.quantatw.sls.pack.account.AccountLoginReqPack;
import com.quantatw.sls.pack.account.AccountNotificationTokenReqPack;
import com.quantatw.sls.pack.account.AccountRegisterReqPack;
import com.quantatw.sls.pack.account.AccountResPack;
import com.quantatw.sls.pack.account.AddUserFriendReqPack;
import com.quantatw.sls.pack.account.ChangePasswordReqPack;
import com.quantatw.sls.pack.account.FBAccountReqPack;
import com.quantatw.sls.pack.account.ForgetPasswordReqPack;
import com.quantatw.sls.pack.account.GetUserFriendListResPack;
import com.quantatw.sls.pack.account.JwtPayloadResPack;
import com.quantatw.sls.pack.account.ModifyNickNameReqPack;
import com.quantatw.sls.pack.account.RemoveUserFriendReqPack;
import com.quantatw.sls.pack.account.SendAuthorizeEmailReqPack;
import com.quantatw.sls.pack.account.UpdateUserProfileReqPack;
import com.quantatw.sls.pack.account.UserDataSharedReqPack;
import com.quantatw.sls.pack.base.BaseReqPack;
import com.quantatw.sls.pack.base.BaseResPack;
import com.quantatw.sls.pack.device.AddCloudDeviceResPack;
import com.quantatw.sls.pack.device.AddDeviceReqPack;
import com.quantatw.sls.pack.device.AddDeviceUserReqPack;
import com.quantatw.sls.pack.device.DeleteDeviceReqPack;
import com.quantatw.sls.pack.device.DeviceUserReqPack;
import com.quantatw.sls.pack.device.DeviceUserResPack;
import com.quantatw.sls.pack.device.GetAllDeviceSettingResPack;
import com.quantatw.sls.pack.device.GetCloudDeviceStatusResPack;
import com.quantatw.sls.pack.device.GetCloudDevicesResPack;
import com.quantatw.sls.pack.device.GetDeviceSettingResPack;
import com.quantatw.sls.pack.device.GetDeviceStatusReqPack;
import com.quantatw.sls.pack.device.GetDevicesReqPack;
import com.quantatw.sls.pack.device.GetIRDeviceAbilityResPack;
import com.quantatw.sls.pack.device.GetIRDeviceStatusResPack;
import com.quantatw.sls.pack.device.GetScheduleListResPack;
import com.quantatw.sls.pack.device.IRCommandReqPack;
import com.quantatw.sls.pack.device.IRDeviceScheduleReqPack;
import com.quantatw.sls.pack.device.ModifyDeviceNameReqPack;
import com.quantatw.sls.pack.device.UpdateDeviceOnlineStatusReqPack;
import com.quantatw.sls.pack.device.UpdateDeviceOnlineStatusResPack;
import com.quantatw.sls.pack.homeAppliance.BaseHomeApplianceResPack;
import com.quantatw.sls.pack.homeAppliance.CommandAcReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandBulbReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandRemoteControlReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandResPack;
import com.quantatw.sls.pack.homeAppliance.CommonReqPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitAcCommandPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitAcResPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitRemoteControlCommandPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitRemoteControlResPack;
import com.quantatw.sls.pack.homeAppliance.GetAbilityLimitReqPack;
import com.quantatw.sls.pack.homeAppliance.GetAcAssetInfoCommandPack;
import com.quantatw.sls.pack.homeAppliance.GetAcAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.GetAirPurifierAssetInfoCommandPack;
import com.quantatw.sls.pack.homeAppliance.GetAirPurifierAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.GetBulbAssetInfoCommandPack;
import com.quantatw.sls.pack.homeAppliance.GetBulbAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.GetDeviceInfoCommandPack;
import com.quantatw.sls.pack.homeAppliance.GetDeviceInfoResPack;
import com.quantatw.sls.pack.homeAppliance.GetFanAssetInfoCommandPack;
import com.quantatw.sls.pack.homeAppliance.GetFanAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.GetPMAssetInfoCommandPack;
import com.quantatw.sls.pack.homeAppliance.GetPMAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.GetTVAssetInfoCommandPack;
import com.quantatw.sls.pack.homeAppliance.GetTVAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.ScheduleReqPack;
import com.quantatw.sls.pack.homeAppliance.SetFailRecoverReqPack;
import com.quantatw.sls.pack.homeAppliance.UpgradeReqPack;
import com.quantatw.sls.pack.roomhub.VersionCheckUpdateReqPack;
import com.quantatw.sls.pack.roomhub.VersionCheckUpdateResPack;
import com.quantatw.sls.pack.roomhub.ir.IRACAutoScanResPack;
import com.quantatw.sls.pack.roomhub.ir.IRACKeyDataResPack;
import com.quantatw.sls.pack.roomhub.ir.IRBrandAndModelDataResPack;
import com.quantatw.sls.pack.roomhub.ir.IRBrandListResPack;
import com.quantatw.sls.pack.roomhub.ir.IRCodeNumByKeywordResPack;
import com.quantatw.sls.pack.roomhub.ir.IRCodeNumListResPack;
import com.quantatw.sls.pack.roomhub.ir.IRModeResPack;
import com.quantatw.sls.pack.roomhub.ir.IRModelListResPack;
import com.quantatw.sls.pack.roomhub.ir.IRModelResPack;
import com.quantatw.sls.pack.roomhub.sensor.SensorDataResPack;

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
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;

public class CloudApi {
    static CloudApi instance = null;

    // For Authorize type http request use
    private static AccountResPack mLoginedAccountResPack = null;
    private static String mClient_id = null;

    public static String CloudAddress = "http://10.0.8.101";
    public static String API_USERREGISTER = "/V1/User/Register";
    public static String API_USERLOGIN = "/V1/User/Login/1";
    public static String API_FBUSERLOGIN = "/V1/User/Login/2";
    public static String API_ADDDEVICE = "/V1/Device";
    public static String API_UPDATE_USER_PROFILE = "/V1/User/UserProfile";
    public static String API_FORGET_PASSWORD = "/V1/User/ForgetPassword";

    private static final int HTTP_MAX_RETRY_COUNT = 3;

    protected Gson gson;

    public static CloudApi getInstance(String cloudAddress, String clientId)
    {
        if(instance == null)
        {
            instance = new CloudApi();
            if(cloudAddress != null) {
                instance.CloudAddress = cloudAddress;
            }
            if(clientId != null) {
                instance.mClient_id = clientId;
            }
        }

        return instance;
    }

    public static CloudApi getInstance() {
        return instance;
    }

    private CloudApi() {
        gson = new GsonBuilder().create();
    }

    protected String PostBaseReq(String URL, String in) {
        return PostBaseReq(URL, in, false);
    }

    protected String PostBaseReq(String URL, String in, boolean Authorize) {
        return PostBaseReq(URL, in, Authorize, LanguageType.EN);
    }

    protected String PostBaseReq(String URL, String in, boolean Authorize, String language) {
        String data = in;
        boolean mRetry = false;
        int mRetryCnt = 0;

        // List<NameValuePair> nvpList = new ArrayList<NameValuePair>(1);
        // BasicNameValuePair bnvp = new BasicNameValuePair("ReqData", data);
        // nvpList.add(bnvp);

        HttpParams params = new BasicHttpParams();

        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpConnectionParams.setConnectionTimeout(params, 30000);
        HttpConnectionParams.setSoTimeout(params, 20000);
        SchemeRegistry sr = new SchemeRegistry();
        sr.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        // Register scheme for https
        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
        sr.register(new Scheme("https", socketFactory, 443));

        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, sr);

        HttpClient httpClient = new DefaultHttpClient(cm, params);

        HttpPost httpPost = null;
        try {
            URL = URL.replaceAll(" ", "%20");
            httpPost = new HttpPost(new URI(CloudAddress + URL));

            Log.d("CloudAPI", "URL: " + CloudAddress + URL);

        } catch (URISyntaxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return null;
        }
        if(language != null) {
            httpPost.setHeader("Accept-Language", language);
        } else {
            httpPost.setHeader("Accept-Language", "en");
        }

        httpPost.setHeader("User-Agent", "CloudAPI");
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");

        if(Authorize == true) {
            if(mLoginedAccountResPack == null || mLoginedAccountResPack.getToken() == null) {
                return null;
            }
            httpPost.setHeader("Authorization", mLoginedAccountResPack.getTokenType() + " " + mLoginedAccountResPack.getToken());
            httpPost.setHeader("clientId", mClient_id);
        }

        try {
            // httpPost.setEntity(new UrlEncodedFormEntity(nvpList, "utf-8"));
            // httpPost.setRequestEntity(new StringRequestEntity(data));
            httpPost.setEntity(new StringEntity(data, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        HttpResponse httpResp = null;
        do {
            try {
                mRetry = true;
                mRetryCnt++;

                httpResp = httpClient.execute(httpPost);
                mRetry = false;
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                //return null;
            } catch (IOException e) {
                // TODO Auto-generated catch block

                e.printStackTrace();
                //return null;
            }

            if(mRetry && mRetryCnt >= HTTP_MAX_RETRY_COUNT) {
                return null;
            }
        } while(mRetry);
        // for( Header header : httpResp.getAllHeaders()) {
        // // cookie = header.getValue();
        // System.out.println( header);
        // }

        String resp = null;
        try {
            resp = EntityUtils.toString(httpResp.getEntity());
            // System.out.println("BASE64 " + resp);
            Log.d("CloudAPI", "Resp: " + resp);

            if (resp != null) {
                Log.d("CloudAPI", "StatusCode: " + httpResp.getStatusLine().getStatusCode());
                // if(resp.contains("503 Service Unavailable"))
                // return null;
                if (httpResp.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
                    return null;

            }

        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            return null;
        }
        Log.d("CloudAPI", "Resp: " + resp);

        return resp;
    }

    protected String GetBaseReq(String URL) {
        return GetBaseReq(URL, false, LanguageType.EN);
    }

    protected String GetBaseReq(String URL, boolean Authorize) {
       return GetBaseReq(URL, Authorize, LanguageType.EN);
    }
    protected String GetBaseReq(String URL, boolean Authorize, String language) {
        boolean mRetry = false;
        int mRetryCnt = 0;
        // List<NameValuePair> nvpList = new ArrayList<NameValuePair>(1);
        // BasicNameValuePair bnvp = new BasicNameValuePair("ReqData", data);
        // nvpList.add(bnvp);

        HttpParams params = new BasicHttpParams();

        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpConnectionParams.setConnectionTimeout(params, 30000);
        HttpConnectionParams.setSoTimeout(params, 20000);
        SchemeRegistry sr = new SchemeRegistry();
        sr.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        // Register scheme for https
        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
        sr.register(new Scheme("https", socketFactory, 443));

        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, sr);

        HttpClient httpClient = new DefaultHttpClient(cm, params);

        HttpGet httpGet = null;
        try {
            URL = URL.replaceAll(" ", "%20");
            httpGet = new HttpGet(new URI(CloudAddress + URL));
            Log.d("CloudAPI", "URL: " + CloudAddress + URL);

        } catch (URISyntaxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return null;
        }
        if(language != null) {
            httpGet.setHeader("Accept-Language", language);
        } else {
            httpGet.setHeader("Accept-Language", "en");
        }

        httpGet.setHeader("User-Agent", "CloudAPI");
        httpGet.setHeader("Content-Type", "application/json");

        if(Authorize == true) {
            if(mLoginedAccountResPack == null || mLoginedAccountResPack.getToken() == null) {
                return null;
            }
            httpGet.setHeader("Authorization", mLoginedAccountResPack.getTokenType() + " " + mLoginedAccountResPack.getToken());
            httpGet.setHeader("clientId", mClient_id);
        }

        HttpResponse httpResp = null;
        do {
            try {
                mRetry = true;
                mRetryCnt++;

                httpResp = httpClient.execute(httpGet);
                mRetry = false;
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                //return null;
            } catch (IOException e) {
                // TODO Auto-generated catch block

                e.printStackTrace();
                //return null;
            }

            if(mRetry && mRetryCnt >= HTTP_MAX_RETRY_COUNT) {
                return null;
            }
        } while(mRetry);
        // for( Header header : httpResp.getAllHeaders()) {
        // // cookie = header.getValue();
        // System.out.println( header);
        // }

        String resp = null;
        try {
            resp = EntityUtils.toString(httpResp.getEntity());
            // System.out.println("BASE64 " + resp);
            Log.d("CloudAPI", "Resp: " + resp);

            if (resp != null) {

                // if(resp.contains("503 Service Unavailable"))

                Log.d("CloudAPI", "StatusCode: " + httpResp.getStatusLine().getStatusCode());
                if (httpResp.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
                    return null;
            }

        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            return null;
        }
        Log.d("CloudAPI", "Resp: " + resp);

        return resp;
    }

    protected String DeleteBaseReq(String URL) {
        boolean mRetry = false;
        int mRetryCnt = 0;
        // List<NameValuePair> nvpList = new ArrayList<NameValuePair>(1);
        // BasicNameValuePair bnvp = new BasicNameValuePair("ReqData", data);
        // nvpList.add(bnvp);

        HttpParams params = new BasicHttpParams();

        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpConnectionParams.setConnectionTimeout(params, 30000);
        HttpConnectionParams.setSoTimeout(params, 20000);
        SchemeRegistry sr = new SchemeRegistry();
        sr.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, sr);

        HttpClient httpClient = new DefaultHttpClient(cm, params);

        HttpDelete httpDelete = null;
        try {

            httpDelete = new HttpDelete(new URI(CloudAddress + URL));
            Log.d("CloudAPI", "URL: " + CloudAddress + URL);

        } catch (URISyntaxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return null;
        }
        httpDelete.setHeader("Accept-Language", "en");

        httpDelete.setHeader("User-Agent", "CloudAPI");

        HttpResponse httpResp = null;
        do {
            try {
                mRetry = true;
                mRetryCnt++;

                httpResp = httpClient.execute(httpDelete);
                mRetry = false;
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                //return null;
            } catch (IOException e) {
                // TODO Auto-generated catch block

                e.printStackTrace();
                //return null;
            }

            if(mRetry && mRetryCnt >= HTTP_MAX_RETRY_COUNT) {
                return null;
            }
        } while (mRetry);
        // for( Header header : httpResp.getAllHeaders()) {
        // // cookie = header.getValue();
        // System.out.println( header);
        // }

        String resp = null;
        try {
            resp = EntityUtils.toString(httpResp.getEntity());
            // System.out.println("BASE64 " + resp);
            Log.d("CloudAPI", "Resp: " + resp);

            if (resp != null) {

                Log.d("CloudAPI", "StatusCode: " + httpResp.getStatusLine().getStatusCode());
                // if(resp.contains("503 Service Unavailable"))
                // return null;
                if (httpResp.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
                    return null;

            }

        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            return null;
        }
        Log.d("CloudAPI", "Resp: " + resp);

        return resp;
    }

    // User ----------------------------------------------------------------------------------
    public AccountResPack UserRegisterREQ(AccountRegisterReqPack _AccountRegisterReqPack, String language) {

        String data = gson.toJson(_AccountRegisterReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(API_USERREGISTER, data, false, language);

        if (ret == null) {

            AccountResPack resPack = new AccountResPack();
            resPack.setStatus_code(ErrorKey.ConnectionError);

            mLoginedAccountResPack = null;
            return resPack;
        } else {
            Log.d("CloudAPI", "BaseReq Return: " + ret);
            AccountResPack resPack;
            try {
                resPack = gson.fromJson(ret, AccountResPack.class);

            } catch (Exception e) {
                // TODO: handle exception
                resPack = new AccountResPack();
                resPack.setStatus_code(ErrorKey.JsonError);
            }

            mLoginedAccountResPack = resPack;
            return resPack;

        }
    }

    public AccountResPack UserLoginREQ(AccountLoginReqPack _AccountLoginReqPack) {

        String data = gson.toJson(_AccountLoginReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(API_USERLOGIN, data);

        ExangeJson<AccountResPack> mExangeJson = new ExangeJson<AccountResPack>();

        AccountResPack resPack = new AccountResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        mLoginedAccountResPack = resPack;
        return resPack;
        // if (ret == null) {
        //
        // AccountResPack resPack = new AccountResPack();
        // resPack.setStatus_code(ErrorKey.ConnectionError);
        // return resPack;
        // } else {
        // Log.d("CloudAPI","BaseReq Return: "+ ret);
        // AccountResPack resPack;
        // try {
        // resPack = gson.fromJson(ret, AccountResPack.class);
        //
        // } catch (Exception e) {
        // // TODO: handle exception
        // resPack = new AccountResPack();
        // resPack.setStatus_code(ErrorKey.JsonError);
        // }
        //
        // return resPack;
        //
        // }
    }

    public AccountResPack FBUserLoginREQ(FBAccountReqPack _FBAccountReqPack) {

        String data = gson.toJson(_FBAccountReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(API_FBUSERLOGIN, data);

        ExangeJson<AccountResPack> mExangeJson = new ExangeJson<AccountResPack>();

        AccountResPack resPack = new AccountResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        mLoginedAccountResPack = resPack;
        return resPack;

        // if (ret == null) {
        //
        // AccountResPack resPack = new AccountResPack();
        // resPack.setStatus_code(ErrorKey.ConnectionError);
        // return resPack;
        // } else {
        // Log.d("CloudAPI","BaseReq Return: "+ ret);
        // AccountResPack resPack;
        // try {
        // resPack = gson.fromJson(ret, AccountResPack.class);
        //
        // } catch (Exception e) {
        // // TODO: handle exception
        // resPack = new AccountResPack();
        // resPack.setStatus_code(ErrorKey.JsonError);
        // }
        //
        // return resPack;
        //
        // }
    }
    // TODO: http://10.0.8.101/V1/UserDoc

    public BaseResPack ForgetPasswordREQ(ForgetPasswordReqPack reqPack, String language) {
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(API_FORGET_PASSWORD, data, false, language);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }
    public BaseResPack ChangePasswordREQ(ChangePasswordReqPack reqPack) {

        String data = "/V1/ChangePassword/User/" + reqPack.getOldPassword() + "/" + reqPack.getNewPassword();

        String ret = GetBaseReq(data, true);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    public BaseResPack UpdateUserProfile(UpdateUserProfileReqPack userProfileReqPack) {
        String data = gson.toJson(userProfileReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(API_UPDATE_USER_PROFILE, data, true);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    public BaseResPack SendAuthorizeEmail(SendAuthorizeEmailReqPack sendAuthorizeEmailReqPack) {
        final  String API_SEND_AUTHORIZE_EMAIL = "/V1/User/SendAuthorizeEmail";

        String data = gson.toJson(sendAuthorizeEmailReqPack);

        String ret = PostBaseReq(API_SEND_AUTHORIZE_EMAIL, data, false);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    public BaseResPack CheckAccountOrEmailExist(AccountInfoCheckReqPack accountInfoCheckReqPack) {
        String data = gson.toJson(accountInfoCheckReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq("/V1/User/CheckExists", data, false);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    public BaseResPack SetNotificationTokenREQ(AccountNotificationTokenReqPack reqPack, String language) {
        String data = gson.toJson(reqPack);

        String ret = PostBaseReq("/V1/Device/NotificationToken", data, true, language);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    public JwtPayloadResPack GetJwtPayloadREQ() {
        JwtPayloadResPack resPack = null;

        if(mLoginedAccountResPack != null && mLoginedAccountResPack.getToken() != null) {
            String jwt[] = mLoginedAccountResPack.getToken().split("\\.");
            String jwtStr = null;

            if( jwt[1] != null ) {
                byte[] tmp2 = Base64.decode(jwt[1], Base64.DEFAULT);
                try {
                    jwtStr = new String(tmp2, "UTF-8");

                    if(jwtStr != null) {
                        resPack = new JwtPayloadResPack();
                        ExangeJson<JwtPayloadResPack> mExangeJson = new ExangeJson<JwtPayloadResPack>();
                        resPack = mExangeJson.Exe(jwtStr, resPack);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.getStackTrace();
                }
            }
        }

        return resPack;
    }

    public GetUserFriendListResPack GetUserFriendListREQ() {
        // Get User Friend List - /V1/User/Friend
        String data = "/V1/User/Friend";

        String ret = GetBaseReq(data, true);

        ExangeJson<GetUserFriendListResPack> mExangeJson = new ExangeJson<GetUserFriendListResPack>();

        GetUserFriendListResPack resPack = new GetUserFriendListResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    public BaseResPack AddUserFriend(AddUserFriendReqPack _AddUserFriendReqPack) {
        final String API_ADDUSERFRIEND = "/V1/User/Friend/AddFriend";
        // Add User Friend
        String data = gson.toJson(_AddUserFriendReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(API_ADDUSERFRIEND, data, true);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    public BaseResPack RemoveUserFriend(RemoveUserFriendReqPack _RemoveUserFriendReqPack) {
        final String API_REMOVEUSERFRIEND = "/V1/User/Friend/DelFriend";
        // Remove User Friend
        String data = gson.toJson(_RemoveUserFriendReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(API_REMOVEUSERFRIEND, data, true);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    public BaseResPack modifyNickName(ModifyNickNameReqPack _ModifyNickNameReqPack) {
        final String API_UPDATEUSERFRIEND = "/V1/User/Friend/UpdFriend";
        // Update user friend nickname
        String data = gson.toJson(_ModifyNickNameReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(API_UPDATEUSERFRIEND, data, true);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }
    // User ----------------------------------------------------------------------------------

    // Device --------------------------------------------------------------------------------
    public AddCloudDeviceResPack AddDeviceREQ(AddDeviceReqPack _AddDeviceReqPack) {
        // Add device / Change device owner
        String data = gson.toJson(_AddDeviceReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(API_ADDDEVICE, data, true);

        ExangeJson<AddCloudDeviceResPack> mExangeJson = new ExangeJson<AddCloudDeviceResPack>();

        AddCloudDeviceResPack resPack = new AddCloudDeviceResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;

        // if (ret == null) {
        //
        // AddDeviceResPack resPack = new AddDeviceResPack();
        // resPack.setStatus_code(ErrorKey.ConnectionError);
        // return resPack;
        // } else {
        // Log.d("CloudAPI","BaseReq Return: "+ ret);
        // AddDeviceResPack resPack;
        // try {
        // resPack = gson.fromJson(ret, AddDeviceResPack.class);
        //
        // } catch (Exception e) {
        // // TODO: handle exception
        // resPack = new AddDeviceResPack();
        // resPack.setStatus_code(ErrorKey.JsonError);
        // }
        //
        // return resPack;
        //
        // }
    }

    public GetCloudDeviceStatusResPack GetDeviceStatusREQ(GetDeviceStatusReqPack _GetDeviceStatusReqPack) {
        // Get device
        String data = "/V1/Device/" + _GetDeviceStatusReqPack.getUuid();
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, true);

        ExangeJson<GetCloudDeviceStatusResPack> mExangeJson = new ExangeJson<GetCloudDeviceStatusResPack>();

        GetCloudDeviceStatusResPack resPack = new GetCloudDeviceStatusResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;

        // if (ret == null) {
        //
        // GetDeviceStatusResPack resPack = new GetDeviceStatusResPack();
        // resPack.setStatus_code(ErrorKey.ConnectionError);
        // return resPack;
        // } else {
        // Log.d("CloudAPI","BaseReq Return: "+ ret);
        // GetDeviceStatusResPack resPack;
        // try {
        // resPack = gson.fromJson(ret, GetDeviceStatusResPack.class);
        //
        // } catch (Exception e) {
        // // TODO: handle exception
        // resPack = new GetDeviceStatusResPack();
        // resPack.setStatus_code(ErrorKey.JsonError);
        // }
        //
        // return resPack;
        //
        // }
    }

    public BaseResPack AddDeviceUserREQ(String uuid, AddDeviceUserReqPack addDeviceUserReqPack) {
        // Add Device User - // /V1/Device/{uuid}/AddUser
        String urlPath = "/V1/Device/" + uuid + "/AddUser";
        String data = gson.toJson(addDeviceUserReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, true);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    public BaseResPack DeleteDeviceUserREQ(String uuid, DeviceUserReqPack deleteDeviceUserReqPack) {
        // Delete Device User - // /V1/Device/{uuid}/DelUser
        String urlPath = "/V1/Device/" + uuid + "/DelUser";
        String data = gson.toJson(deleteDeviceUserReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, true);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    // Get Device Users
    public GetCloudDevicesResPack GetDeviceUsersREQ(String uuid) {
        // Get Device Users - /V1/Device/{uuid}/UserList
        String data = "/V1/Device/" + uuid + "/UserList";
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, true);

        ExangeJson<GetCloudDevicesResPack> mExangeJson = new ExangeJson<GetCloudDevicesResPack>();

        GetCloudDevicesResPack resPack = new GetCloudDevicesResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }


    public GetCloudDevicesResPack GetDevicesREQ(GetDevicesReqPack _GetDevicesReqPack) {
        // Get devices
        String data = "/V1/Devices";
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, true);

        ExangeJson<GetCloudDevicesResPack> mExangeJson = new ExangeJson<GetCloudDevicesResPack>();

        GetCloudDevicesResPack resPack = new GetCloudDevicesResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;

        // if (ret == null) {
        //
        // GetDevicesResPack resPack = new GetDevicesResPack();
        // resPack.setStatus_code(ErrorKey.ConnectionError);
        // return resPack;
        // } else {
        // Log.d("CloudAPI","BaseReq Return: "+ ret);
        // GetDevicesResPack resPack;
        // try {
        // resPack = gson.fromJson(ret, GetDevicesResPack.class);
        //
        // } catch (Exception e) {
        // // TODO: handle exception
        // resPack = new GetDevicesResPack();
        // resPack.setStatus_code(ErrorKey.JsonError);
        // }
        //
        // return resPack;
        //
        // }
    }

    public BaseResPack ModifyDeviceNameREQ(String uuid, ModifyDeviceNameReqPack reqPack) {
        // Update device name - /V1/Device/{uuid}/DeviceName/{deviceName}

	// device re-name on Cloud, change API from "Update device name" to "Remote update device name"
        //String data = "/V1/Device/" + reqPack.getUuid() + "/DeviceName/" + reqPack.getName();
        String urlPath = "/V1/Device/" + uuid + "/name";
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, true);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;


    }

    public BaseResPack ModifyDeviceName(String uuid, ModifyDeviceNameReqPack reqPack) {
        // Update device name - /V1/Device/{uuid}/DeviceName/

        String urlPath = "/V1/Device/" +uuid + "/DeviceName";
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, true);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    public BaseResPack DeleteDeviceREQ(DeleteDeviceReqPack _DeleteDeviceReqPack) {
        // Remove device
        // TODO: deviceType
        String data = "/V1/Device/" + _DeleteDeviceReqPack.getUuid() + "/Del";
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, true);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;

        // if (ret == null) {
        //
        // BaseResPack resPack = new BaseResPack();
        // resPack.setStatus_code(ErrorKey.ConnectionError);
        // return resPack;
        // } else {
        // Log.d("CloudAPI","BaseReq Return: "+ ret);
        // BaseResPack resPack;
        // try {
        // resPack = gson.fromJson(ret, BaseResPack.class);
        //
        // } catch (Exception e) {
        // // TODO: handle exception
        // resPack = new BaseResPack();
        // resPack.setStatus_code(ErrorKey.JsonError);
        // }
        //
        // return resPack;
        //
        // }
    }

    public UpdateDeviceOnlineStatusResPack UpdateDeviceStatusREQ(
            UpdateDeviceOnlineStatusReqPack _UpdateDeviceOnlineStatusReqPack) {
        // Update Device Online Status
        String data = "/V1/Device/" + _UpdateDeviceOnlineStatusReqPack.getUuid()
                + "/UpdateOnlineStatus/";
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, true);

        ExangeJson<UpdateDeviceOnlineStatusResPack> mExangeJson = new ExangeJson<UpdateDeviceOnlineStatusResPack>();

        UpdateDeviceOnlineStatusResPack resPack = new UpdateDeviceOnlineStatusResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;

        // if (ret == null) {
        //
        // UpdateDeviceOnlineStatusResPack resPack = new
        // UpdateDeviceOnlineStatusResPack();
        // resPack.setStatus_code(ErrorKey.ConnectionError);
        // return resPack;
        // } else {
        // Log.d("CloudAPI","BaseReq Return: "+ ret);
        // UpdateDeviceOnlineStatusResPack resPack;
        // try {
        // resPack = gson.fromJson(ret, UpdateDeviceOnlineStatusResPack.class);
        //
        // } catch (Exception e) {
        // // TODO: handle exception
        // resPack = new UpdateDeviceOnlineStatusResPack();
        // resPack.setStatus_code(ErrorKey.JsonError);
        // }
        //
        // return resPack;
        //
        // }
    }

    // Get remote device setting / all schedule
    public GetAllDeviceSettingResPack GetDeviceSetting(String uuid) {
        // Get Device Setting - // /V1/Device/{uuid}/DeviceSetting
        String data = "/V1/Device/" + uuid + "/DeviceSetting";
        Log.d("CloudAPI", "GetDeviceSetting:"+data);

        GetAllDeviceSettingResPack getAllDeviceSettingResPack = new GetAllDeviceSettingResPack();

        String ret = GetBaseReq(data, true);

        // Get deviceType, deviceSetting
        ExangeJson<GetDeviceSettingResPack> mExangeJson = new ExangeJson<GetDeviceSettingResPack>();

        GetDeviceSettingResPack resPack = new GetDeviceSettingResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        getAllDeviceSettingResPack.setUuid(resPack.getUuid());
        getAllDeviceSettingResPack.setDeviceType(resPack.getDeviceType());

        if(resPack.getStatus_code() == 0) {
            // Get schedule list
            ExangeJson<GetScheduleListResPack> mExangeScheduleListJson = new ExangeJson<GetScheduleListResPack>();

            GetScheduleListResPack getScheduleListResPack = new GetScheduleListResPack();

            getScheduleListResPack = mExangeScheduleListJson.Exe(resPack.getDeviceSetting(), getScheduleListResPack);

            if (getScheduleListResPack != null)
            {
                if (getAllDeviceSettingResPack.getStatus_code() == 0) {
                    getAllDeviceSettingResPack.setScheduleList(getScheduleListResPack.getScheduleList());
                }
                if(getAllDeviceSettingResPack.getScheduleList() == null)
                    getAllDeviceSettingResPack.setScheduleList(new ArrayList<Schedule>());
                getAllDeviceSettingResPack.setStatus_code(getScheduleListResPack.getStatus_code());
                getAllDeviceSettingResPack.setMessgae(getScheduleListResPack.getMessgae());
            }
            else {
                getScheduleListResPack = new GetScheduleListResPack();
                getAllDeviceSettingResPack.setScheduleList(new ArrayList<Schedule>());
            }
        }
        else {
            getAllDeviceSettingResPack.setStatus_code(resPack.getStatus_code());
            getAllDeviceSettingResPack.setMessgae(resPack.getMessgae());

        }

        return getAllDeviceSettingResPack;
    }

    // Get remote device ability
    /**
     * @deprecated
     * See GetAcAbilityLimit, GetFanAbilityLimit
     */
    @Deprecated
    public GetIRDeviceAbilityResPack GetIRDeviceAbility(String uuid) {
        // Get IR Device Ability - // /V1/Device/IRCmd/{uuid}/Ability

        return new GetIRDeviceAbilityResPack();

        /*
        String data = "/V1/Device/IRCmd/" + uuid + "/Ability";
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<GetIRDeviceAbilityResPack> mExangeJson = new ExangeJson<GetIRDeviceAbilityResPack>();

        GetIRDeviceAbilityResPack resPack = new GetIRDeviceAbilityResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
        */
    }

    // Get remote device info
    public GetIRDeviceStatusResPack GetIRDeviceStatus(String uuid) {
        // Get IR Device Status - // /V1/Device/IRCmd/{uuid}
        String data = "/V1/Device/IRCmd/" + uuid ;
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, true);

        ExangeJson<GetIRDeviceStatusResPack> mExangeJson = new ExangeJson<GetIRDeviceStatusResPack>();

        GetIRDeviceStatusResPack resPack = new GetIRDeviceStatusResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    // control remote device
    public BaseResPack SendIRCommand(String uuid, IRCommandReqPack irCommandReqPack) {
        // Send IR Command - // /V1/Device/IRCmd/{uuid}/Command
        String urlPath = "/V1/Device/IRCmd/" + uuid + "/Command";
        String data = gson.toJson(irCommandReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    // set schedule to remote device
    public BaseResPack SendIRDeviceSchedule(String uuid, IRDeviceScheduleReqPack irDeviceScheduleReqPack) {
        // Send IR Device Schedule - // /V1/Device/IRCmd/{uuid}/Schedule
        String urlPath = "/V1/Device/IRCmd/" + uuid + "/Schedule";
        String data = gson.toJson(irDeviceScheduleReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, true);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    // Send DeviceAsset IR command - AC
    public CommandResPack SendDeviceAssetsIRCommand_Ac(String roomHubUuid, CommandAcReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND = "206";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<CommandResPack> mExangeJson = new ExangeJson<CommandResPack>();

        CommandResPack resPack = new CommandResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    // Send DeviceAsset IR command - FAN,AirPurifier,TV
    public CommandResPack SendDeviceAssetsIRCommand_RemoteControl(String roomHubUuid, CommandRemoteControlReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND = "206";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<CommandResPack> mExangeJson = new ExangeJson<CommandResPack>();

        CommandResPack resPack = new CommandResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    // Send DeviceAsset IR command - Bulb
    public CommandResPack SendDeviceAssetsIRCommand_Bulb(String roomHubUuid, CommandBulbReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND = "206";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<CommandResPack> mExangeJson = new ExangeJson<CommandResPack>();

        CommandResPack resPack = new CommandResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    // Send DeviceAsset IR command  - Get device information
    public GetDeviceInfoResPack GetDeviceInfo(String roomHubUuid) {
        final String REMOTE_COMMAND_GET_ASSET_INFO = "207";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND_GET_ASSET_INFO;
        String data = gson.toJson(new BaseReqPack());
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath,data , false);

        ExangeJson<GetDeviceInfoCommandPack> mExangeJson = new ExangeJson<GetDeviceInfoCommandPack>();
        GetDeviceInfoCommandPack commandPack = new GetDeviceInfoCommandPack();
        commandPack = mExangeJson.Exe(ret, commandPack);

        if(commandPack != null) {
            commandPack.getData().setStatus_code(commandPack.getStatus_code());
            commandPack.getData().setMessgae(commandPack.getMessgae());

            return commandPack.getData();
        }

        return null;
    }

    // Get Ac Asset Info
    public GetAcAssetInfoResPack GetAcAssetInfo(String roomHubUuid, CommonReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND_GET_ASSET_INFO = "208";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND_GET_ASSET_INFO;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<GetAcAssetInfoCommandPack> mExangeJson = new ExangeJson<GetAcAssetInfoCommandPack>();
        GetAcAssetInfoCommandPack commandPack = new GetAcAssetInfoCommandPack();
        commandPack = mExangeJson.Exe(ret, commandPack);

        if(commandPack != null) {
            commandPack.getData().setStatus_code(commandPack.getStatus_code());
            commandPack.getData().setMessgae(commandPack.getMessgae());

            return commandPack.getData();
        }

        return null;
    }

    // Get Fan Asset Info
    public GetFanAssetInfoResPack GetFanAssetInfo(String roomHubUuid, CommonReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND_GET_ASSET_INFO = "208";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND_GET_ASSET_INFO;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<GetFanAssetInfoCommandPack> mExangeJson = new ExangeJson<GetFanAssetInfoCommandPack>();
        GetFanAssetInfoCommandPack commandPack = new GetFanAssetInfoCommandPack();
        try {
            commandPack = mExangeJson.Exe(ret, commandPack);
        } catch (Exception e) {
            return null;
        }

        if(commandPack != null) {
            commandPack.getData().setStatus_code(commandPack.getStatus_code());
            commandPack.getData().setMessgae(commandPack.getMessgae());

            return commandPack.getData();
        }

        return null;
    }

    // Get TV Asset Info
    public GetTVAssetInfoResPack GetTVAssetInfo(String roomHubUuid, CommonReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND_GET_ASSET_INFO = "208";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND_GET_ASSET_INFO;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<GetTVAssetInfoCommandPack> mExangeJson = new ExangeJson<GetTVAssetInfoCommandPack>();
        GetTVAssetInfoCommandPack commandPack = new GetTVAssetInfoCommandPack();
        try {
            commandPack = mExangeJson.Exe(ret, commandPack);
        } catch (Exception e) {
            return null;
        }

        if(commandPack != null) {
            commandPack.getData().setStatus_code(commandPack.getStatus_code());
            commandPack.getData().setMessgae(commandPack.getMessgae());

            return commandPack.getData();
        }

        return null;
    }

    // Get PM2.5 Asset Info
    public GetPMAssetInfoResPack GetPMAssetInfo(String roomHubUuid, CommonReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND_GET_ASSET_INFO = "208";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND_GET_ASSET_INFO;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<GetPMAssetInfoCommandPack> mExangeJson = new ExangeJson<GetPMAssetInfoCommandPack>();
        GetPMAssetInfoCommandPack commandPack = new GetPMAssetInfoCommandPack();
        try {
            commandPack = mExangeJson.Exe(ret, commandPack);
        } catch (Exception e) {
            return null;
        }

        if(commandPack != null) {
            commandPack.getData().setStatus_code(commandPack.getStatus_code());
            commandPack.getData().setMessgae(commandPack.getMessgae());

            return commandPack.getData();
        }

        return null;
    }

    // Get AirPurifier Asset Info
    public GetAirPurifierAssetInfoResPack GetAirPurifierAssetInfo(String roomHubUuid, CommonReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND_GET_ASSET_INFO = "208";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND_GET_ASSET_INFO;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<GetAirPurifierAssetInfoCommandPack> mExangeJson = new ExangeJson<GetAirPurifierAssetInfoCommandPack>();
        GetAirPurifierAssetInfoCommandPack commandPack = new GetAirPurifierAssetInfoCommandPack();
        try {
            commandPack = mExangeJson.Exe(ret, commandPack);
        } catch (Exception e) {
            return null;
        }

        if(commandPack != null) {
            commandPack.getData().setStatus_code(commandPack.getStatus_code());
            commandPack.getData().setMessgae(commandPack.getMessgae());

            return commandPack.getData();
        }

        return null;
    }

    // Get Bulb Asset Info
    public GetBulbAssetInfoResPack GetBulbAssetInfo(String roomHubUuid, CommonReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND_GET_ASSET_INFO = "208";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND_GET_ASSET_INFO;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<GetBulbAssetInfoCommandPack> mExangeJson = new ExangeJson<GetBulbAssetInfoCommandPack>();
        GetBulbAssetInfoCommandPack commandPack = new GetBulbAssetInfoCommandPack();
        try {
            commandPack = mExangeJson.Exe(ret, commandPack);
        } catch (Exception e) {
            return null;
        }

        if(commandPack != null) {
            commandPack.getData().setStatus_code(commandPack.getStatus_code());
            commandPack.getData().setMessgae(commandPack.getMessgae());

            return commandPack.getData();
        }

        return null;
    }

    // Send DeviceAsset IR command - Bulb
    public CommandResPack ModifySchedule_Bulb(String roomHubUuid, ScheduleReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND = "209";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<CommandResPack> mExangeJson = new ExangeJson<CommandResPack>();

        CommandResPack resPack = new CommandResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    // Get Ac Ability
    public GetAbilityLimitAcResPack GetAcAbilityLimit(String roomHubUuid, GetAbilityLimitReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND_GET_ABILITY = "210";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND_GET_ABILITY;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<GetAbilityLimitAcCommandPack> mExangeJson = new ExangeJson<GetAbilityLimitAcCommandPack>();
        GetAbilityLimitAcCommandPack commandPack = new GetAbilityLimitAcCommandPack();
        commandPack = mExangeJson.Exe(ret, commandPack);

        if(commandPack != null) {
            commandPack.getData().setStatus_code(commandPack.getStatus_code());
            commandPack.getData().setMessgae(commandPack.getMessgae());

            return commandPack.getData();
        }

        return null;
    }

    // Get Remote Control Ability - assetType = 1 (FAN),4 (Air purifier),6 (TV)
    public GetAbilityLimitRemoteControlResPack GetRemoteControlAbilityLimit(String roomHubUuid, GetAbilityLimitReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND_GET_ABILITY = "210";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND_GET_ABILITY;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<GetAbilityLimitRemoteControlCommandPack> mExangeJson = new ExangeJson<GetAbilityLimitRemoteControlCommandPack>();
        GetAbilityLimitRemoteControlCommandPack commandPack = new GetAbilityLimitRemoteControlCommandPack();
        commandPack = mExangeJson.Exe(ret, commandPack);

        if(commandPack != null) {
            commandPack.getData().setStatus_code(commandPack.getStatus_code());
            commandPack.getData().setMessgae(commandPack.getMessgae());

            return commandPack.getData();
        }

        return null;
    }

    // Send DeviceAsset IR command - Reboot hub
    public CommandResPack SendDeviceAssetsIRCommand_RebootRoomHub(String roomHubUuid) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND = "211";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND;
        String data = gson.toJson(new BaseReqPack());
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath,data , false);

        ExangeJson<CommandResPack> mExangeJson = new ExangeJson<CommandResPack>();

        CommandResPack resPack = new CommandResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    // Send DeviceAsset IR command - Fail recover
    public CommandResPack SendDeviceAssetsIRCommand_AcFailRecover(String roomHubUuid, SetFailRecoverReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND = "212";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<CommandResPack> mExangeJson = new ExangeJson<CommandResPack>();

        CommandResPack resPack = new CommandResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    // Send DeviceAsset IR command - Start upgrade
    public BaseHomeApplianceResPack SendDeviceAssetsIRCommand_StartUpgrade(String roomHubUuid, UpgradeReqPack reqPack) {
        // Send DeviceAsset IR command - // /V1/Device/IRCmd/{uuid}/Cmd/{cmd}
        final String REMOTE_COMMAND = "214";
        String urlPath = "/V1/Device/IRCmd/" + roomHubUuid + "/Cmd/" + REMOTE_COMMAND;
        String data = gson.toJson(reqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, false);

        ExangeJson<BaseHomeApplianceResPack> mExangeJson = new ExangeJson<BaseHomeApplianceResPack>();

        BaseHomeApplianceResPack resPack = new BaseHomeApplianceResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    // TODO: http://10.0.8.101/V1/DeviceDoc
    // Device --------------------------------------------------------------------------------

    // IR ------------------------------------------------------------------------------------
    @Deprecated
    public IRCodeNumListResPack GetCodeNumByLearningSearch(String signature) {
        String data = "/V1/IR/LearningSearch/" + signature; // /V1/IR/LearningSearch/{s1}
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRCodeNumListResPack> mExangeJson = new ExangeJson<IRCodeNumListResPack>();

        IRCodeNumListResPack resPack = new IRCodeNumListResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    /**
     *
     * Get Code Num and IR Data via S0 & S3
     *
     * @param deviceId (see IrDeviceType)
     *   0	AIR CONDITIONER
     *   1	All TV
     *   11	STREAMING /MEDIA PLAYER
     *   12	SAT/CLB/DVR/DTV/DVBT/IPTV/PVR
     *   68	FAN/FAN HEATER/AIR PURIFIER uto
     * @param s0
     * @param s1
     * @param s3
     * @return
     */
    public IRCodeNumListResPack ACIrLearning(int deviceId, int s0, int s1, String s3) {
//        String data = "/V1/IR/acIrLearning/" + String.valueOf(s1) + "/" + s3; // /V1/IR/acIrLearning/{s0}/{s3}
        // /V1/IR/acIrLearning/{deviceId}/{s0}/{s1}/{s3}/{misRate}
        String data = "/V1/IR/acIrLearning/" +
                String.valueOf(deviceId)+
                "/" +
                String.valueOf(s0) +
                "/" +
                String.valueOf(s1) +
                "/" +
                s3 +
                "/" +
                Integer.toString(15);
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRCodeNumListResPack> mExangeJson = new ExangeJson<IRCodeNumListResPack>();

        IRCodeNumListResPack resPack = new IRCodeNumListResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    public IRCodeNumListResPack AVIrLearning(int deviceId, int s0, int s1, String s3) {
//        String data = "/V1/IR/acIrLearning/" + String.valueOf(s1) + "/" + s3; // /V1/IR/acIrLearning/{s0}/{s3}
        // /V1/IR/AV/IrLearning/{deviceId}/{s0}/{s1}/{s3}/{misRate}/{region}
        String data = "/V1/IR//AV/IrLearning/" +
                String.valueOf(deviceId)+
                "/" +
                String.valueOf(s0) +
                "/" +
                String.valueOf(s1) +
                "/" +
                s3 +
                "/" +
                Integer.toString(15)+
                "/ASIA";
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRCodeNumListResPack> mExangeJson = new ExangeJson<IRCodeNumListResPack>();

        IRCodeNumListResPack resPack = new IRCodeNumListResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    /**
     *
     * http://10.0.8.101/V1/IRDoc Get Brand List
     *
     * @param deviceId  0   AIR CONDITIONER
     *                  1   All TV
     *                  11  STREAMING /MEDIA PLAYER
     *                  12  SAT/CLB/DVR/DTV/DVBT/IPTV/PVR
     * @param language  tw / cn / en
     * @return
     */
    public IRBrandListResPack GetIRBrandList(int deviceId, String language) {
        String data = "/V1/IR/" + String.valueOf(deviceId) + "/Brand"; // /V1/IR/{deviceId}/Brand
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false, language);

        ExangeJson<IRBrandListResPack> mExangeJson = new ExangeJson<IRBrandListResPack>();

        IRBrandListResPack resPack = new IRBrandListResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    public IRBrandListResPack GetAVBrandList(int deviceId, String language) {
        ///V1/IR/AV/{deviceId}/Brand/{region}
        String data = "/V1/IR/AV/" + String.valueOf(deviceId) + "/Brand/ASIA";
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false, language);

        ExangeJson<IRBrandListResPack> mExangeJson = new ExangeJson<IRBrandListResPack>();

        IRBrandListResPack resPack = new IRBrandListResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    public IRModelResPack GetIRFirstModel(int deviceId, int brandId) {
        String data = "/V1/IR/" + String.valueOf(deviceId) + "/Brand/" + String.valueOf(brandId) + "/Model"; // /V1/IR/{deviceId}/Brand/{brandId}/Model
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRModelResPack> mExangeJson = new ExangeJson<IRModelResPack>();

        IRModelResPack resPack = new IRModelResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    public IRModelResPack GetAVFirstModel(int deviceId, int brandId) {
        String data = "/V1/IR/AV" + String.valueOf(deviceId) + "/Brand/" + String.valueOf(brandId) + "/Model/ASIA";
        ///V1/IR/AV/{deviceId}/Brand/{brandId}/Model/{region}
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRModelResPack> mExangeJson = new ExangeJson<IRModelResPack>();

        IRModelResPack resPack = new IRModelResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    /**
     *
     * http://10.0.8.101/V1/IRDoc Get Model List
     *
     * @param deviceId  0   AIR CONDITIONER
     *                  1   All TV
     *                  11  STREAMING /MEDIA PLAYER
     *                  12  SAT/CLB/DVR/DTV/DVBT/IPTV/PVR
     * @param brandId   BrandId
     * @return
     */
    public IRModelListResPack GetIRModelList(int deviceId, int brandId) {
        String data = "/V1/IR/" + String.valueOf(deviceId) + "/Brand/" + String.valueOf(brandId) + "/ModelList"; // /V1/IR/{deviceId}/Brand/{brandId}/Model
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRModelListResPack> mExangeJson = new ExangeJson<IRModelListResPack>();

        IRModelListResPack resPack = new IRModelListResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    public IRModelListResPack GetAVModelList(int deviceId, int brandId) {
        // /V1/IR/AV/{deviceId}/Brand/{brandId}/ModelList/{region}
        String data = "/V1/IR/AV/" + String.valueOf(deviceId) + "/Brand/" + String.valueOf(brandId) + "/ModelList/ASIA"; // /V1/IR/{deviceId}/Brand/{brandId}/Model
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRModelListResPack> mExangeJson = new ExangeJson<IRModelListResPack>();

        IRModelListResPack resPack = new IRModelListResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    public IRACAutoScanResPack ACAutoScan(int deviceId) {
        ///V1/IR/{deviceId}/ACAutoScan
        String data = "/V1/IR/"+String.valueOf(deviceId)+"/ACAutoScan";
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRACAutoScanResPack> mExangeJson = new ExangeJson<IRACAutoScanResPack>();

        IRACAutoScanResPack resPack = new IRACAutoScanResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    public IRACAutoScanResPack AVAutoScan(int deviceId) {
        // /V1/IR/AV/{deviceId}/AutoScan/{region}
        String data = "/V1/IR/AV/"+String.valueOf(deviceId)+"/AutoScan/ASIA";
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRACAutoScanResPack> mExangeJson = new ExangeJson<IRACAutoScanResPack>();

        IRACAutoScanResPack resPack = new IRACAutoScanResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    public IRACKeyDataResPack GetACKeyData(int codeNum) {
        String data = "/V1/IR/ACKeyData/" + String.valueOf(codeNum); // /V1/IR/ACKeyData/{codeNum}
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRACKeyDataResPack> mExangeJson = new ExangeJson<IRACKeyDataResPack>();

        IRACKeyDataResPack resPack = new IRACKeyDataResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    public IRACKeyDataResPack GetAVKeyData(int codeNum) {
        String data = "/V1/IR/AV/KeyData/" + String.valueOf(codeNum)+"/ASIA";
        // /V1/IR/AV/KeyData/{codeNum}/{region}
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRACKeyDataResPack> mExangeJson = new ExangeJson<IRACKeyDataResPack>();

        IRACKeyDataResPack resPack = new IRACKeyDataResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    public IRBrandAndModelDataResPack GetBrandAndModelData(int deviceId,int codeNum, String language) {
        ///V1/IR/AC/{deviceId}/BrandFromCodeNum/{codeNum}
        String data = "/V1/IR/AC/" + String.valueOf(deviceId) + "/BrandFromCodeNum/" + String.valueOf(codeNum);
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false, language);

        ExangeJson<IRBrandAndModelDataResPack> mExangeJson = new ExangeJson<IRBrandAndModelDataResPack>();

        IRBrandAndModelDataResPack resPack = new IRBrandAndModelDataResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    public IRModeResPack GetIRModes(int codeNum) {
        String data = "/V1/IR/Brand/" + String.valueOf(codeNum) + "/Modes"; // /V1/IR/Brand/{codeNum}/Modes
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRModeResPack> mExangeJson = new ExangeJson<IRModeResPack>();

        IRModeResPack resPack = new IRModeResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    /**
     *
     * http://10.0.8.101/V1/IRDoc Get CodeNum by keyword
     *
     * @param deviceId  IrDeviceType.AIR_CONDITIONER, IrDeviceType.TV, IrDeviceType.STREAMING_MEDIA_PLAYER, IrDeviceType.SAT_CLB_DVR_DTV_DVBT_IPTV_PVR
     *                  0   AIR CONDITIONER
     *                  1   All TV
     *                  11  STREAMING /MEDIA PLAYER
     *                  12  SAT/CLB/DVR/DTV/DVBT/IPTV/PVR
     * @param keyword   Keyword
     * @return
     */
    public IRCodeNumByKeywordResPack GetCodeNumByKeyword(int deviceId, String keyword) {
        String data = "/V1/IR/getCodeNumByKeyword/" + String.valueOf(deviceId) + "/" + keyword; // /V1/IR/getCodeNumByKeyword/{deviceId}/{keyword}
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRCodeNumByKeywordResPack> mExangeJson = new ExangeJson<IRCodeNumByKeywordResPack>();

        IRCodeNumByKeywordResPack resPack = new IRCodeNumByKeywordResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    public IRCodeNumByKeywordResPack GetAVCodeNumByKeyword(int deviceId, String keyword) {
        /// V1/IR/AV/getCodeNumByKeyword/{deviceId}/{keyword}/{region}
        String data = "/V1/IR/AV/getCodeNumByKeyword/" + String.valueOf(deviceId) + "/" + keyword+"/ASIA";
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<IRCodeNumByKeywordResPack> mExangeJson = new ExangeJson<IRCodeNumByKeywordResPack>();

        IRCodeNumByKeywordResPack resPack = new IRCodeNumByKeywordResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }
    // IR ------------------------------------------------------------------------------------

    // Sensor --------------------------------------------------------------------------------

    /**
     *
     * @param uuid Device UUID
     * @param sensorType Temperature / Humidity / AirQuality / LightLux...etc
     * @param queryDate For example: Oct 16 => "10-16"
     * @return
     */
    public SensorDataResPack GetSensorDailyData(String uuid, String sensorType, String queryDate) {
        String data = "/V1/Device/" + uuid + "/SensorData/" + sensorType + "/GetList/" + queryDate; // /V1/Device/{uuid}/SensorData/{sensorDataType}/GetList/{queryDate}
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, true);

        ExangeJson<SensorDataResPack> mExangeJson = new ExangeJson<SensorDataResPack>();

        SensorDataResPack resPack = new SensorDataResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }

    // Sensor --------------------------------------------------------------------------------

    // Weather -------------------------------------------------------------------------------
    public CityListResPack GetCityListREQ(String language) {
        String data = "/V1/City/";  // /V1/City
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false, language);

        ExangeJson<CityListResPack> mExangeJson = new ExangeJson<CityListResPack>();

        CityListResPack resPack = new CityListResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    public TownListResPack GetTownListREQ(int cityId, String language) {
        String data = "/V1/City/" + String.valueOf(cityId);    // /V1/City/{cityId}
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false, language);

        ExangeJson<TownListResPack> mExangeJson = new ExangeJson<TownListResPack>();

        TownListResPack resPack = new TownListResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    public WeatherDataResPack GetWeatherDataREQ(String townId, String language) {
        String data = "/V1/City/" + String.valueOf(townId) + "/Weather";    // /V1/City/{townId}/Weather
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false, language);

        ExangeJson<WeatherDataResPack> mExangeJson = new ExangeJson<WeatherDataResPack>();

        WeatherDataResPack resPack = new WeatherDataResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;

    }
    // Weather -------------------------------------------------------------------------------

    // RoomHub Version -----------------------------------------------------------------------
    public VersionCheckUpdateResPack CheckVersion(VersionCheckUpdateReqPack versionCheckUpdateReqPack) {
        final  String API_VERSION_CHECK = "/V1/DriverVersion/H60firmware/chkUpdateVersion";

        String data = gson.toJson(versionCheckUpdateReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(API_VERSION_CHECK, data);

        ExangeJson<VersionCheckUpdateResPack> mExangeJson = new ExangeJson<VersionCheckUpdateResPack>();

        VersionCheckUpdateResPack resPack = new VersionCheckUpdateResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }
    // RoomHub Version -----------------------------------------------------------------------

    // App Latest Version -----------------------------------------------------------------------
    public VersionCheckUpdateResPack AppCheckVersion() {
        ///V1/DriverVersion/{driverName}/LastVersion
        final String driverName = "H60Android";
        final  String data = "/V1/DriverVersion/"+driverName+"/LastVersion";
        Log.d("CloudAPI", data);

        String ret = GetBaseReq(data, false);

        ExangeJson<VersionCheckUpdateResPack> mExangeJson = new ExangeJson<VersionCheckUpdateResPack>();

        VersionCheckUpdateResPack resPack = new VersionCheckUpdateResPack();

        resPack = mExangeJson.Exe(ret, resPack);
        return resPack;
    }
    // App Latest Version -----------------------------------------------------------------------

    // BPM History ------------------------------------------------------------------------------
    public String getBPMLastData(String uuid) {
        // /V1/Device/{uuid}/SensorData/Sphygmometer
        String data = "/V1/Device/" + uuid + "/SensorData/Sphygmometer";
        Log.d("CloudAPI", data);

        return GetBaseReq(data, true);
    }

    public String getBPMDataList(String uuid, int days) {
        // /V1/Device/{uuid}/SensorData/Sphygmometer/History/{day}
        String data = "/V1/Device/" + uuid + "/SensorData/Sphygmometer/History/" + Integer.toString(days);
        Log.d("CloudAPI", data);

        return GetBaseReq(data, true);
    }

    public String getBPMLastDataByUserId(String userId) {
        // /V1/User/{userId}/Sphygmometer
        String data = "/V1/User/" + userId + "/Sphygmometer";
        Log.d("CloudAPI", data);

        return GetBaseReq(data, true);
    }

    public String getBPMDataListByUserId(String userId, int days) {
        // /V1/User/{sharedUserId}/Sphygmometer/History/{day}
        String data = "/V1/User/" + userId + "/Sphygmometer/History/" + Integer.toString(days);
        Log.d("CloudAPI", data);

        return GetBaseReq(data, true);
    }

    // BPM History ------------------------------------------------------------------------------

    //Save Device DefaultUser
    public BaseResPack saveDeviceDefaultUser(String uuid,DeviceUserReqPack deviceDefaultUserReqPack) {
        String urlPath = "/V1/Device/" +uuid + "/DefaultUser";
        String data = gson.toJson(deviceDefaultUserReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, true);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    //Get Default User
    public DeviceUserResPack getDeviceDefaultUser(String uuid) {
        // /V1/Device/{uuid}/DefaultUser
        String data = "/V1/Device/" + uuid + "/DefaultUser";

        String ret = GetBaseReq(data, true);

        ExangeJson<DeviceUserResPack> mExangeJson = new ExangeJson<DeviceUserResPack>();

        DeviceUserResPack resPack = new DeviceUserResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    //Save userdata shared users
    public BaseResPack saveUserDataShared(UserDataSharedReqPack userDataSharedReqPack) {
        String urlPath = "/V1/User/UserDataShared";
        String data = gson.toJson(userDataSharedReqPack);
        Log.d("CloudAPI", data);

        String ret = PostBaseReq(urlPath, data, true);

        ExangeJson<BaseResPack> mExangeJson = new ExangeJson<BaseResPack>();

        BaseResPack resPack = new BaseResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    //Get shared userdata list
    public GetUserFriendListResPack getSharedUserDataList(int deviceType) {
        int convertType = DeviceTypeConvertApi.ConvertType_AppToCloud(
                new DeviceTypeConvertApi.AppDeviceCategoryType(DeviceTypeConvertApi.CATEGORY.HEALTH, deviceType));

        if(convertType == DeviceTypeConvertApi.TYPE_NOT_FOUND)
            return null;

        String data = "/V1/User/UserDataShared/" + String.valueOf(convertType);

        String ret = GetBaseReq(data, true);

        ExangeJson<GetUserFriendListResPack> mExangeJson = new ExangeJson<GetUserFriendListResPack>();

        GetUserFriendListResPack resPack = new GetUserFriendListResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }

    //Get userdata shared friends
    public GetUserFriendListResPack getUserDataSharedFriends(int deviceType) {
        String data = "/V1/User/Friend/" + String.valueOf(deviceType);

        String ret = GetBaseReq(data, true);

        ExangeJson<GetUserFriendListResPack> mExangeJson = new ExangeJson<GetUserFriendListResPack>();

        GetUserFriendListResPack resPack = new GetUserFriendListResPack();

        resPack = mExangeJson.Exe(ret, resPack);

        return resPack;
    }
 }
