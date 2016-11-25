package com.quantatw.roomhub.manager;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.listener.AccountLoginStateListener;
import com.quantatw.roomhub.listener.ShareUserChangedListener;
import com.quantatw.roomhub.listener.UserFriendChangedListener;
import com.quantatw.roomhub.utils.PreferenceEditor;
import com.quantatw.roomhub.utils.RoomHubFailureCause;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.cloudapi.CloudApi;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.device.FriendData;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.LanguageType;
import com.quantatw.sls.listener.ShareUserListener;
import com.quantatw.sls.listener.UserFriendListener;
import com.quantatw.sls.pack.account.AccountInfoCheckReqPack;
import com.quantatw.sls.pack.account.AccountLoginReqPack;
import com.quantatw.sls.pack.account.AccountRegisterReqPack;
import com.quantatw.sls.pack.account.AccountResPack;
import com.quantatw.sls.pack.account.AddUserFriendReqPack;
import com.quantatw.sls.pack.account.ChangePasswordReqPack;
import com.quantatw.sls.pack.account.ForgetPasswordReqPack;
import com.quantatw.sls.pack.account.GetUserFriendListResPack;
import com.quantatw.sls.pack.account.ModifyNickNameReqPack;
import com.quantatw.sls.pack.account.RemoveUserFriendReqPack;
import com.quantatw.sls.pack.account.SendAuthorizeEmailReqPack;
import com.quantatw.sls.pack.account.UserDataSharedReqPack;
import com.quantatw.sls.pack.account.UserFriendResPack;
import com.quantatw.sls.pack.account.UserSharedDataResPack;
import com.quantatw.sls.pack.base.BaseResPack;
import com.quantatw.sls.pack.device.AddDeviceUserReqPack;
import com.quantatw.sls.pack.device.DeviceUserReqPack;
import com.quantatw.sls.pack.device.GetCloudDevicesResPack;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;

/**
 * Created by 95010915 on 2015/9/22.
 */
public class AccountManager extends BaseManager implements UserFriendListener,ShareUserListener{
    private static final String TAG = "AccountManager";

    private static final String PREFERENCE_NAME = "ACCOUNT_INFO";

    private static final String PREFERENCE_LOGIN_RECORD = "LoginRecord";
    private static final String PREFERENCE_USERNAME = "UserName";
    private static final String PREFERENCE_USERID = "UserId";
    private static final String PREFERENCE_NICKNAME = "NickName";
    private static final String PREFERENCE_PWD = "Password";

    private static final String TOKEN_TITLE_USERNAME = "userName";
    private static final String TOKEN_TITLE_USERID = "userId";
    private static final String TOKEN_TITLE_EMAIL = "email";

    private MiddlewareApi mApi;
    private String mToken;
    private  String mUserName; // Nick Name
    private String mUserId;
    private  String mEmail; // Email
    private boolean mLogin = false;

    private PreferenceEditor mPref;

    private LinkedHashSet<AccountLoginStateListener> mLoginStateListenerList = null;

    private final int MESSAGE_SKIP_LOGIN = 100;
    private final int MESSAGE_UPDATE_TOKEN_PERIODICALLY = 500;

    private final int MESSAGE_GET_FRIEND_LIST = 600;

    private HandlerThread mBackgroundThread;
    private BackgroundHandler mBackgroundHandler;
    private ArrayList<FriendData> mFrinedList=new ArrayList<FriendData>();
    private LinkedHashSet<UserFriendChangedListener> mUserFriendListener = null;
    private LinkedHashSet<ShareUserChangedListener> mShareUserListener = null;

    @Override
    public void addShareUser(CloudDevice device) {
        if(device == null) return;

        String owner=device.getOwnerName();

        Log.d(TAG,"addShareUser owner=" + owner);
        if(owner.equalsIgnoreCase(mUserName)){
            if(mShareUserListener != null){
                synchronized (mShareUserListener) {
                    for (Iterator<ShareUserChangedListener> it = mShareUserListener.iterator(); it.hasNext(); ) {
                        ShareUserChangedListener listener = it.next();
                        listener.AddShareUser(device);
                    }
                }
            }
        }
    }

    @Override
    public void removeShareUser(CloudDevice device) {
        if(device == null) return;

        String owner=device.getOwnerName();
        Log.d(TAG,"removeShareUser owner=" + owner);
        if(owner.equalsIgnoreCase(mUserName)) {
            if(mShareUserListener != null){
                synchronized (mShareUserListener) {
                    for (Iterator<ShareUserChangedListener> it = mShareUserListener.iterator(); it.hasNext(); ) {
                        ShareUserChangedListener listener = it.next();
                        listener.RemoveShareUser(device);
                    }
                }
            }
        }
    }

    @Override
    public void UserSharedData(UserSharedDataResPack userSharedData) {
        if(userSharedData != null){
            //if(mUserId.equalsIgnoreCase(userSharedData.getUserId())) {
                if(mShareUserListener != null){
                    synchronized (mShareUserListener) {
                        for (Iterator<ShareUserChangedListener> it = mShareUserListener.iterator(); it.hasNext(); ) {
                            ShareUserChangedListener listener = it.next();
                            listener.UserSharedData(userSharedData);
                        }
                    }
                }
            //}
        }
    }

    private android.os.Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_SKIP_LOGIN:
                    notifySkipLogin();
                    break;
                case MESSAGE_UPDATE_TOKEN_PERIODICALLY:
                    removeMessages(MESSAGE_UPDATE_TOKEN_PERIODICALLY);
                    updateToken((int)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void addFriend(UserFriendResPack friend) {
        Log.d(TAG,"addFriend");

        if(getFriendDataByUserId(friend.getUserId()) == null){
            synchronized (mFrinedList) {
                FriendData data = new FriendData();
                data.setNickName(friend.getNickName());
                data.setUserId(friend.getUserId());
                data.setUserAccount(friend.getUserAccount());

                mFrinedList.add(data);

                if(mUserFriendListener != null) {
                    for (UserFriendChangedListener listener : mUserFriendListener) {
                        listener.AddFriend(data);
                    }
                }
            }
        }
    }

    @Override
    public void updateFriend(UserFriendResPack friend) {
        Log.d(TAG,"updateFriend");
        FriendData data=getFriendDataByUserId(friend.getUserId());
        if(data != null){
            data.setNickName(friend.getNickName());
            if(mUserFriendListener != null) {
                for (UserFriendChangedListener listener : mUserFriendListener) {
                    listener.UpdateFriend(data);
                }
            }
        }
    }

    @Override
    public void removeFriend(UserFriendResPack friend) {
        Log.d(TAG,"removeFriend");

        FriendData data=getFriendDataByUserId(friend.getUserId());
        if(data != null){
          //  FriendData friend_data=new FriendData();
            //friend_data.setUserId(data.getUserId());
            if(mUserFriendListener != null) {
                for (UserFriendChangedListener listener : mUserFriendListener) {
                    listener.RemoveFriend(data);
                }
            }
            synchronized (mFrinedList) {
                mFrinedList.remove(data);
            }
        }
    }

    public FriendData getFriendDataByUserId(String user_id){
        if((mFrinedList == null) || (mFrinedList.size() <= 0 ))
            return null;

        String userId;
        synchronized (mFrinedList){
            for (Iterator<FriendData> it = mFrinedList.iterator();it.hasNext();) {
                FriendData data = it.next();
                userId=data.getUserId();
                if (userId.equalsIgnoreCase(user_id)) {
                    return data;
                }
            }
        }

        return null;
    }

    private final class BackgroundHandler extends Handler {
        public BackgroundHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_GET_FRIEND_LIST:
                    GetFriendList();
                    break;
            }
        }
    }

    private void notifySkipLogin() {
        if(mLoginStateListenerList != null) {
            for (AccountLoginStateListener listener : mLoginStateListenerList) {
                listener.onSkipLogin();
            }
        }
    }

    private void setLoginRecord(boolean login) {
        mPref.setStringValue(PREFERENCE_LOGIN_RECORD, String.valueOf(login));
    }

    private boolean hasLoginBefore() {
        String val = mPref.getStringValue(PREFERENCE_LOGIN_RECORD);
        if(val != null && (val.equalsIgnoreCase("true"))) {
            return true;
        }
        return false;
    }

    public AccountManager(Context context, MiddlewareApi api) {
        super(context,BaseManager.ACCOUNT_MANAGER);
        mApi = api;
        mPref = new PreferenceEditor(context, PREFERENCE_NAME);
        mLoginStateListenerList = new LinkedHashSet<AccountLoginStateListener>();

        mUserFriendListener =new LinkedHashSet<UserFriendChangedListener>();

        mShareUserListener =new LinkedHashSet<ShareUserChangedListener>();

        mBackgroundThread=new HandlerThread("AccountManager");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());

        mApi.registerUserFrinedListener(this);
        mApi.registerShareUserListener(this);
    }

    public void registerForLoginState(AccountLoginStateListener deviceListener) {
        synchronized(mLoginStateListenerList) {
            mLoginStateListenerList.add(deviceListener);
        }
    }

    public void unRegisterForLoginState(AccountLoginStateListener deviceListener) {
        synchronized (mLoginStateListenerList) {
            mLoginStateListenerList.remove(deviceListener);
        }
    }

    public int createNewAccount(String username, String pass, String email) {
        String locale = Locale.getDefault().getLanguage();
        AccountRegisterReqPack pack = new AccountRegisterReqPack();
        pack.setUserAccount(username);
        pack.setUserPw(pass);
        pack.setEmail(email);
        if(locale.equals("zh"))
            locale = LanguageType.TW;
        AccountResPack res = mApi.userRegister(pack, locale);
        /*
        if(res.getStatus_code() == GlobalDef.STATUS_CODE_SUCCESS) {
            setLoginInfomation(res.getToken(), username, pass);
        }
        */
        return res.getStatus_code();
    }

    private void setLoginInfomation(String token, String name, String pass) {
        mLogin = true;

        mToken = token;
        parseToken();
        // Write shared preference
        setLoginInfo(name, pass);

        if(mLoginStateListenerList != null) {
            for (AccountLoginStateListener listener : mLoginStateListenerList) {
                listener.onLogin();
            }
        }
    }

    public int Login(String name, String pass, boolean clearLoginInfo) {
        AccountLoginReqPack pack = new AccountLoginReqPack();
        pack.setUserAccount(name);
        pack.setUserPw(pass);

        mHandler.removeMessages(MESSAGE_UPDATE_TOKEN_PERIODICALLY);
        AccountResPack res = mApi.userLogin(pack, false);
        if(res.getStatus_code() == ErrorKey.Success) {
            setLoginInfomation(res.getToken(), name, pass);
            setLoginRecord(true);
            int nextDueTime = res.getExpireIn()/2;
            //int nextDueTime = 60/2;
            Log.d(TAG,"udpateToken next due time: "+nextDueTime);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_UPDATE_TOKEN_PERIODICALLY, nextDueTime),nextDueTime*1000);
            mBackgroundHandler.sendEmptyMessage(MESSAGE_GET_FRIEND_LIST);
        } else {
            mLogin = false;
            if(clearLoginInfo == true)
                setLoginInfo("", "");
        }
        return res.getStatus_code();
    }

    private void updateToken(int updateTime) {
        AccountLoginReqPack pack = new AccountLoginReqPack();
        pack.setUserAccount(getCurrentAccount());
        pack.setUserPw(getCurrentAccountPass());

        AccountResPack res = mApi.userLogin(pack, true);
        int nextDueTime = 0;
        if(res.getStatus_code() == ErrorKey.Success) {
            nextDueTime = res.getExpireIn()/2;
            //nextDueTime = 60/2;
        }
        else {
            if((updateTime/2) <= 0) {
                ReminderData reminderData = new ReminderData();
                reminderData.setMessageId(RoomHubFailureCause.ID.H60Failure_Account_001);
                reminderData.setSimpleMessage(mContext.getString(R.string.fail_msg_account_001));
                sendReminderMessage(reminderData);

                updateTime = 30 * 60; //30 mins
                //updateTime = 60;    //test 1 min
            }
            nextDueTime = updateTime/2;
        }
        Log.d(TAG, "udpateToken next due time: " + nextDueTime);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_UPDATE_TOKEN_PERIODICALLY, nextDueTime), nextDueTime * 1000);
    }

    private byte[] decodeBase64(String string) {
        int rem = string.length()%4;
        String ending="";
        if(rem > 0) {
            int num = 4 - rem;
            for(int i=0;i<num;i++)
                ending = ending+"=";
        }

        String newString = string.replace('-', '+');
        newString = newString.replace("_","/")+ending;
        return Base64.decode(newString, Base64.DEFAULT);
    }

    private void parseToken() {
        if(TextUtils.isEmpty(mToken))
            return;

        String[] temp = mToken.split("\\.");
//        byte[] tmp2 = Base64.decode(temp[1], Base64.DEFAULT);
        byte[] tmp2 = decodeBase64(temp[1]);
        try {
            String str1 = new String(tmp2, "UTF-8");
            temp = str1.split(",");
            for(int i = 0; i < temp.length; i++) {
                if(temp[i].contains(TOKEN_TITLE_USERNAME)) {
                    String[] temp2 = temp[i].split(":");
                    mUserName = temp2[1].replaceAll("\"","");
                } else if(temp[i].contains(TOKEN_TITLE_USERID)) {
                    String[] temp2 = temp[i].split(":");
                    mUserId = temp2[1].replaceAll("\"","");
                } else if(temp[i].contains(TOKEN_TITLE_EMAIL)) {
                    String[] temp2 = temp[i].split(":");
                    mEmail = temp2[1].replaceAll("\"","");
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.getStackTrace();
        }
    }

    public void Logout() {
        mLogin = false;
        mApi.userLogout();
        setLoginInfo("", "");
        mUserName = "";

        if(mLoginStateListenerList != null) {
            for (AccountLoginStateListener listener : mLoginStateListenerList) {
                listener.onLogout();
            }
        }
    }

    public boolean loginBefore() {
        return hasLoginBefore();
    }

    public boolean couldAutoLogin() {
        //boolean hasLogin = hasLoginBefore();
        //if(!hasLogin) return false;

        String value = mPref.getStringValue(PREFERENCE_USERNAME);
        if(TextUtils.isEmpty(value))
            return false;
        else
            return true;
    }

    public int autoLogin() {
        String userName, pwd;
        userName = mPref.getStringValue(PREFERENCE_USERNAME);
        pwd = mPref.getStringValue(PREFERENCE_PWD);
        return Login(userName, pwd, false);
    }

    public boolean isLogin() {
        if(!TextUtils.isEmpty(mPref.getStringValue(PREFERENCE_USERNAME)) &&
                !TextUtils.isEmpty(mPref.getStringValue(PREFERENCE_PWD)))
            return true;
        return false;
    }

    private void setLoginInfo(String userName, String pwd) {
        mPref.setStringValue(PREFERENCE_USERNAME, userName);
        mPref.setStringValue(PREFERENCE_PWD, pwd);
        if(userName.length() != 0 && pwd.length() != 0) {
            mPref.setStringValue(PREFERENCE_USERID, mUserId);
        }
    }

    public void skipLogin() {
        mLogin = false;
        mHandler.sendEmptyMessage(MESSAGE_SKIP_LOGIN);
    }

    public int modifyProfile(String newName) {
        int ret = ErrorKey.Success;

        mUserName = newName;
        return ret;
    }

    public int changePassword(String oldPass, String newPass) {
        ChangePasswordReqPack req = new ChangePasswordReqPack();
        req.setOldPassword(oldPass);
        req.setNewPassword(newPass);
        BaseResPack pack = mApi.changePassword(req);

        return pack.getStatus_code();
    }

    public String getCurrentAccountName() {
        return mUserName;
    }

    public String getCurrentAccount() {
        return mPref.getStringValue(PREFERENCE_USERNAME);
    }

    public String getCurrentAccountPass() {
        return mPref.getStringValue(PREFERENCE_PWD);
    }

    public String getCurrentEmail() {
        return mEmail;
    }

    private String getUserIdFromPreference() {
        mUserId = mPref.getStringValue(PREFERENCE_USERID);
        return mUserId;
    }

    public String getUserId() {
        if(mUserId != null && mUserId.length() > 0)
            return mUserId;
        else {
            return getUserIdFromPreference();
        }
    }

    public int checkUserName(String username) {
        AccountInfoCheckReqPack req = new AccountInfoCheckReqPack();
        req.setType(AccountInfoCheckReqPack.TYPE_USER_ACCOUNT);
        req.setUserAccount(username);
        BaseResPack pack = mApi.CheckAccountOrEmailExist(req);

        return pack.getStatus_code();
    }

    public int checkEmail(String email) {
        AccountInfoCheckReqPack req = new AccountInfoCheckReqPack();
        req.setType(AccountInfoCheckReqPack.TYPE_EMAIL);
        req.setEmail(email);
        BaseResPack pack = mApi.CheckAccountOrEmailExist(req);

        return pack.getStatus_code();
    }

    public int forgetPass(String email) {
        String locale = Locale.getDefault().getLanguage();
        ForgetPasswordReqPack reqPack = new ForgetPasswordReqPack();
        reqPack.setEmail(email);
        if(locale.equals("zh"))
            locale = LanguageType.TW;
        BaseResPack pack = mApi.forgetPassword(reqPack, locale);

        return pack.getStatus_code();
    }

    public int sendAuthorizeEmail(String userAccount) {
        Log.d(TAG, "sendAuthorizeEmail user account=" + userAccount);
        SendAuthorizeEmailReqPack sendAuthorizeEmailReqPack = new SendAuthorizeEmailReqPack();
        sendAuthorizeEmailReqPack.setUserAccount(userAccount);
        BaseResPack resPack = mApi.SendAuthorizeEmail(sendAuthorizeEmailReqPack);
        Log.d(TAG,"sendAuthorizeEmail status code="+resPack.getStatus_code());
        return resPack.getStatus_code();
    }

    private void GetFriendList(){
        GetUserFriendListResPack res_pack=mApi.GetUserFriendListREQ();

        if(res_pack != null){
            synchronized(mFrinedList) {
                mFrinedList = res_pack.getList();
            }
        }
    }

    public ArrayList<FriendData> GetUserFriendList(){
        ArrayList<FriendData> friend_list;
        synchronized(mFrinedList)
        {
            friend_list = (ArrayList<FriendData>) mFrinedList.clone();
        }
        return friend_list;
    }

    public int AddUserFriend(String account,String nick_name){
        int ret;

        AddUserFriendReqPack addUserFriendReqPack=new AddUserFriendReqPack();
        addUserFriendReqPack.setUserAccount(account);
        addUserFriendReqPack.setNickName(nick_name);

        BaseResPack resPack= mApi.AddUserFriend(addUserFriendReqPack);

        ret=resPack.getStatus_code();

        return ret;
    }

    public int RemoveUserFriend(String account){
        int ret;

        RemoveUserFriendReqPack removeUserFriendReqPack=new RemoveUserFriendReqPack();
        removeUserFriendReqPack.setUserAccount(account);

        BaseResPack resPack= mApi.RemoveUserFriend(removeUserFriendReqPack);

        ret=resPack.getStatus_code();

        return ret;
    }

    public int modifyFrinedNickName(String account,String nick_name){
        int ret;

        ModifyNickNameReqPack ReqPack=new ModifyNickNameReqPack();
        ReqPack.setUserAccount(account);
        ReqPack.setNickName(nick_name);

        BaseResPack resPack= mApi.modifyUserFriendNickName(ReqPack);

        ret=resPack.getStatus_code();

        return ret;
    }

    public void registerUserFriendChanged(UserFriendChangedListener userfriendListener) {
        synchronized(mUserFriendListener) {
            mUserFriendListener.add(userfriendListener);
        }
    }

    public void unRegisterUserFriendChanged(UserFriendChangedListener userfriendListener) {
        synchronized (mUserFriendListener) {
            mUserFriendListener.remove(userfriendListener);
        }
    }

    /* User share */
    public void registerShareUserChanged(ShareUserChangedListener shareUserListener) {
        synchronized(mShareUserListener) {
            mShareUserListener.add(shareUserListener);
        }
    }

    public void unRegisterShareUserChanged(ShareUserChangedListener shareUserListener) {
        synchronized (mShareUserListener) {
            mShareUserListener.remove(shareUserListener);
        }
    }

    public ArrayList<CloudDevice> GetDeviceUserList(String uuid){
        GetCloudDevicesResPack res_pack = mApi.GetDeviceUsersREQ(uuid);
        if((res_pack != null) && (res_pack.getStatus_code() == ErrorKey.Success)){
            return res_pack.getDevices();
        }

        return null;
    }

    public int AddDeviceUser(String uuid,String user_id,String role_name){
        int ret;

        AddDeviceUserReqPack add_user=new AddDeviceUserReqPack();
        add_user.setUserId(user_id);
        add_user.setRoleName(role_name);

        BaseResPack resPack=mApi.AddDeviceUserREQ(uuid, add_user);

        ret=resPack.getStatus_code();

        return ret;
    }

    public int DeleteDeviceUser(String uuid,String user_id){
        int ret;

        DeviceUserReqPack del_user=new DeviceUserReqPack();
        del_user.setUserId(user_id);

        BaseResPack resPack=mApi.DeleteDeviceUserREQ(uuid, del_user);

        ret=resPack.getStatus_code();

        return ret;
    }

    public int SaveUserDataShared(int category,int device_type,String user_id,String type){
        int ret = 0;

        int convertType = DeviceTypeConvertApi.ConvertType_AppToCloud(
                new DeviceTypeConvertApi.AppDeviceCategoryType(category, device_type));

        if(convertType != DeviceTypeConvertApi.TYPE_NOT_FOUND) {
            UserDataSharedReqPack userDataSharedReqPack = new UserDataSharedReqPack();
            userDataSharedReqPack.setDeviceType(convertType);
            userDataSharedReqPack.setUserId(user_id);
            userDataSharedReqPack.setType(type);

            BaseResPack resPack = CloudApi.getInstance().saveUserDataShared(userDataSharedReqPack);

            ret = resPack.getStatus_code();
        }
        return ret;
    }

    public ArrayList<FriendData> GetUserDataSharedFriends(int category,int device_type){
        int convertType = DeviceTypeConvertApi.ConvertType_AppToCloud(
                new DeviceTypeConvertApi.AppDeviceCategoryType(category, device_type));
        if(convertType != DeviceTypeConvertApi.TYPE_NOT_FOUND) {
            GetUserFriendListResPack resPack = CloudApi.getInstance().getUserDataSharedFriends(convertType);
            if (resPack != null) {
                ArrayList<FriendData> list = resPack.getList();
                if(list != null) {
                    for(Iterator iterator=list.iterator();iterator.hasNext();) {
                        FriendData friendData = (FriendData)iterator.next();
                        if(friendData.getShared() == false)
                            iterator.remove();
                    }
                }
                return list;
            }
        }
        return null;
    }

    public void onDestory(){

        mApi.unregisterUserFriendListener(this);
        mApi.unregisterShareUserListener(this);
        if (mBackgroundThread != null ) {
            mBackgroundThread.quit();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }
    }

    @Override
    public void startup() {

    }

    @Override
    public void terminate() {

    }
	
}

