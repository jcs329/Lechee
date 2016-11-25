package com.quantatw.roomhub.gcm;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.PreferenceEditor;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.pack.account.AccountNotificationTokenReqPack;
import com.quantatw.sls.pack.base.BaseResPack;

import java.io.IOException;

/**
 * Created by erin on 11/5/15.
 */
public class GcmController {
    private final String TAG=GcmController.class.getSimpleName();
    private MiddlewareApi mMiddlewareApi;
    private final Context mContext;
    private static GcmController mInstance;

    private final String[] TOPICS = {"global"};

    private String mInstanceId;
    private String mGcmToken;

    public GcmController(Context context, MiddlewareApi api) {
        mContext = context;
        mMiddlewareApi = api;
        mInstance = this;
    }

    public static GcmController getInstance() {
        return mInstance;
    }

    public void register() {
        if(Utils.isGcmRegistered(mContext) == true) {
            Log.d(TAG,"GCM Token retrieved and sent to server!");
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // [START register_for_gcm]
                    // Initially this call goes out to the network to retrieve the token, subsequent calls
                    // are local.
                    // [START get_token]
                    Log.d(TAG, "register ");
                    InstanceID instanceID = InstanceID.getInstance(mContext);
                    mGcmToken = instanceID.getToken(mContext.getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    // [END get_token]
                    mInstanceId = instanceID.getId();
                    Log.i(TAG, "GCM instance Id: " + mInstanceId);
                    Log.i(TAG, "GCM Sender Id: " + mContext.getResources().getString(R.string.gcm_defaultSenderId));
                    Log.i(TAG, "GCM Registration Token: " + mGcmToken);

                    // TODO: Implement this method to send any registration to your app's servers.
                    sendRegistrationToServer();

                    // Subscribe to topic channels
                    Log.d(TAG, "subscribeTopics ");
                    subscribeTopics();

                    // You should store a boolean that indicates whether the generated token has been
                    // sent to your server. If the boolean is false, send the token to your server,
                    // otherwise your server should have already received the token.
                    saveRegistrationPrefs(true);
                    //sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
                    // [END register_for_gcm]
                } catch (Exception e) {
                    Log.d(TAG, "Failed to complete token refresh", e);
                    // If an exception happens while fetching the new token or updating our registration data
                    // on a third-party server, this ensures that we'll attempt the update at a later time.
                    saveRegistrationPrefs(false);
                    //sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
                }
                // Notify UI that registration has completed, so the progress indicator can be hidden.
                //Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
                //LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
                return null;
            }
        }.execute();
    }

    public void unregister() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Log.d(TAG, "unregister deleteToken");
                    InstanceID.getInstance(mContext).deleteToken(mContext.getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE);

                    unSubscribeTopics();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                saveRegistrationPrefs(false);
                return null;
            }
        }.execute();

    }

    private void saveRegistrationPrefs(boolean registration) {
        Utils.setGcmRegistration(mContext, registration);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     */
    private void sendRegistrationToServer() {
        // Add custom implementation, as needed.
//        AccountNotificationTokenReqPack reqPack = new AccountNotificationTokenReqPack();
////        reqPack.setUuid(mContext.getString(R.string.gcm_defaultSenderId));
//        reqPack.setUuid(getClientId());
//        reqPack.setAppName("H60");
//        reqPack.setDeviceType("Android");
//        reqPack.setToken(mGcmToken);
//        BaseResPack resPack = mMiddlewareApi.setNotificationTokenREQ(reqPack);
//        Log.d(TAG, "sendRegistrationToServer status=" + resPack.getStatus_code() + ", msg=" + resPack.getMessgae());
    }

    private String getClientId() {
        String clientId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        clientId = "Android-" + clientId.replaceAll(":", "");

        return clientId;
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics() throws IOException {
        Log.d(TAG, "subscribeTopics token="+mGcmToken);
        GcmPubSub pubSub = GcmPubSub.getInstance(mContext);
        for (String topic : TOPICS) {
            pubSub.subscribe(mGcmToken, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

    private void unSubscribeTopics() throws IOException {
        Log.d(TAG, "unSubscribeTopics token="+mGcmToken);
        GcmPubSub pubSub = GcmPubSub.getInstance(mContext);
        for (String topic : TOPICS) {
            pubSub.unsubscribe(mGcmToken, "/topics/" + topic);
        }
    }

}
