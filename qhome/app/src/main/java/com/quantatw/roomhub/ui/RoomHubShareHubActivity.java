package com.quantatw.roomhub.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.listener.ShareUserChangedListener;
import com.quantatw.roomhub.listener.UserFriendChangedListener;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.device.FriendData;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.account.UserSharedDataResPack;
import com.quantatw.sls.pack.device.AddDeviceUserReqPack;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by 95010915 on 2015/9/25.
 */
public class RoomHubShareHubActivity extends AbstractRoomHubActivity implements View.OnClickListener,RoomHubChangeListener,UserFriendChangedListener,ShareUserChangedListener{
    private static final String TAG = "RoomHubShareHubActivity";
    private static boolean DEBUG=true;

    private AccountManager mAccountMgr;
    private RoomHubManager mRoomHubMgr;
    private Context mContext;

    private TextView mTxtShareDesc;
    private ListView mLstShare;
    private ListView mLstExist;
    private Button mBtnAddPeople;
    private ImageView mBtnMyList;
    private ImageView mBtnCancel;

    private String mCurUuid;
    private ArrayList<FriendData> mFriendDataList;
    private SharePeopleAdapter mSharePeopleAdapter;
    private PeopleAdapter mPeopleAdapter;

    private ArrayList<FriendData> mShareFriendDataList=new ArrayList<FriendData>();

    private final static int MESSAGE_ADD_TO_SHARE   = 101;
    private final static int MESSAGE_DEL_SHARE      = 102;
    private final static int MESSAGE_UPDATE_UI      = 103;
    private final static int MESSAGE_REFRESH_FRIEND = 104;
    private final static int MESSAGE_REFRESH_SHARE_USER = 105;
    private final static int MESSAGE_UPDATE_NICK_NAME =106;
    private final static int MESSAGE_SHOW_ERROR     =107;
    private final static int MESSAGE_LAUNCH_DEVICE_LIST =108;
    private final static int MESSAGE_UPDATA_USERDATA_SHARE = 109;

    private int MAX_LISTITEM_COUNT;
    private int MAX_LISTITEM_COUNT_NO_ADD_PEOPLE;

    private boolean bBtnCancel=false;

    private int mMaxListViewCnt;

    private int deviceCategory;
    private int deviceType;
    enum TYPE{
        ADD_SHARE,
        REMOVE_SHARE
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int pos;
            final FriendData friend_data;
            Thread thread=null;
            switch (msg.what) {
                case MESSAGE_ADD_TO_SHARE:
                    pos=(int)msg.obj;
                    friend_data=mFriendDataList.get(pos);

                    showProgressDialog("", getString(R.string.processing_str));

                    thread = new Thread() {
                        @Override
                        public void run() {
                            int ret;
                            Log.d(TAG,"add to share uuid="+mCurUuid+" user_id="+friend_data.getUserId());
                            if(deviceCategory > 0)
                                ret=mAccountMgr.SaveUserDataShared(deviceCategory,deviceType,friend_data.getUserId(),"Add");
                            else
                                ret=mAccountMgr.AddDeviceUser(mCurUuid, friend_data.getUserId(), AddDeviceUserReqPack.ROLE_USER);
                            dismissProgressDialog();

                            if(ret != ErrorKey.Success){
                                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_ERROR, ret));
                            }
                            if(DEBUG)
                                Log.d(TAG,"add to share ret="+ret);
                        }
                    };
                    thread.start();
                    break;
                case MESSAGE_DEL_SHARE:
                    //pos=(int)msg.obj;
                    //friend_data=mShareFriendDataList.get(pos);
                    showProgressDialog("", getString(R.string.processing_str));
                    final String user_id=(String)msg.obj;
                    thread = new Thread() {
                        @Override
                        public void run() {
                            int ret;
                            Log.d(TAG,"delete share uuid="+mCurUuid+" user_id="+user_id);
                            if(deviceCategory > 0)
                                ret=mAccountMgr.SaveUserDataShared(deviceCategory,deviceType, user_id, "Del");
                            else
                                ret=mAccountMgr.DeleteDeviceUser(mCurUuid,user_id);
                            dismissProgressDialog();
                            if(DEBUG)
                                Log.d(TAG,"delete share ret="+ret);

                            if(ret != ErrorKey.Success){
                                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_ERROR, ret));
                            }
                        }
                    };
                    thread.start();

                    break;
                case MESSAGE_UPDATE_UI:
                    int type=msg.arg1;
                    friend_data=(FriendData)msg.obj;

                    if(type == TYPE.ADD_SHARE.ordinal()){
                        synchronized (mShareFriendDataList) {
                            mShareFriendDataList.add(friend_data);
                        }
                        if (mShareFriendDataList != null && mShareFriendDataList.size() > 0)
                            mLstShare.setVisibility(View.VISIBLE);

                        synchronized (mFriendDataList) {
                            mFriendDataList.remove(friend_data);
                        }
                        mSharePeopleAdapter.notifyDataSetChanged();
                        mPeopleAdapter.notifyDataSetChanged();

                        UpdateListView();
                    }else{
                        synchronized (mFriendDataList) {
                            mFriendDataList.add(friend_data);
                        }
                        synchronized (mShareFriendDataList) {
                            mShareFriendDataList.remove(friend_data);
                        }
                        mSharePeopleAdapter.notifyDataSetChanged();
                        mPeopleAdapter.notifyDataSetChanged();

                        UpdateListView();
                    }

                    break;
                case MESSAGE_REFRESH_FRIEND:
                    mPeopleAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_REFRESH_SHARE_USER:
                    UpdateShareUser((CloudDevice)msg.obj);
                    break;
                case MESSAGE_UPDATE_NICK_NAME:
                    ChangeNickName((FriendData)msg.obj);
                    break;
                case MESSAGE_SHOW_ERROR:
                    int ret=(int)msg.obj;
                    Toast.makeText(mContext, Utils.getErrorCodeString(getApplicationContext(),ret), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_LAUNCH_DEVICE_LIST:
                    launchDeviceList();
                    break;
                case MESSAGE_UPDATA_USERDATA_SHARE:
                    UpdateUserDataShared((UserSharedDataResPack)msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void ChangeNickName(FriendData friend_data){
        int first_visible;
        int last_visible;
        ListView lv;

        int idx=mFriendDataList.indexOf(friend_data);

        if(idx < 0) {
            idx=mShareFriendDataList.indexOf(friend_data);
            if(idx < 0) return;

            lv=mLstShare;
        }else {
            lv=mLstExist;
        }

        first_visible = lv.getFirstVisiblePosition();
        last_visible = lv.getLastVisiblePosition();
        if ((idx >= first_visible) && (idx <= last_visible)) {
            int pos = idx - first_visible;
            View v = lv.getChildAt(pos);
            if (v != null) {
                TextView tv_nick_name = (TextView) v.findViewById(R.id.txt_name);
                tv_nick_name.setText(friend_data.getNickName());
            }
        }
    }

    private void UpdateShareUser(CloudDevice device){
        Log.d(TAG,"UpdateShareUser type="+device.getType()+" targetUser="+device.getTagetUser());
        if(device.getType().equals("add")){
            synchronized (mFriendDataList){
                FriendData data;
                for(int i=0;i < mFriendDataList.size();i++){
                    data=mFriendDataList.get(i);
                    if(data.getUserId().equals(device.getTagetUser())){
                        Log.d(TAG,"UpdateShareUser MESSAGE_ADD_TO_SHARE");
                        Message msg=new Message();
                        msg.what=MESSAGE_UPDATE_UI;
                        msg.arg1=TYPE.ADD_SHARE.ordinal();
                        msg.obj=(Object)data;

                        mHandler.sendMessage(msg);
                    }
                }

            }
        }else{
            synchronized (mShareFriendDataList){
                FriendData data;
                for(int i=0;i < mShareFriendDataList.size();i++){
                    data=mShareFriendDataList.get(i);
                    if(data.getUserId().equals(device.getTagetUser())){
                        Log.d(TAG,"UpdateShareUser MESSAGE_DEL_SHARE");
                        Message msg=new Message();
                        msg.what=MESSAGE_UPDATE_UI;
                        msg.arg1=TYPE.REMOVE_SHARE.ordinal();
                        msg.obj=(Object)data;

                        mHandler.sendMessage(msg);
                    }
                }

            }
        }
    }

    private void UpdateUserDataShared(UserSharedDataResPack userSharedData){
        Log.d(TAG,"UpdateUserDataShared type="+userSharedData.getMethodType()+" share_user_id="+userSharedData.getSharedUserId()+" user_id="+userSharedData.getUserId());
        if(userSharedData.getMethodType().equals("Add")){
            synchronized (mFriendDataList){
                FriendData data;
                for(int i=0;i < mFriendDataList.size();i++){
                    data=mFriendDataList.get(i);
                    if(data.getUserId().equals(userSharedData.getSharedUserId())){
                        Log.d(TAG,"UpdateShareUser MESSAGE_ADD_TO_SHARE");
                        Message msg=new Message();
                        msg.what=MESSAGE_UPDATE_UI;
                        msg.arg1=TYPE.ADD_SHARE.ordinal();
                        msg.obj=(Object)data;

                        mHandler.sendMessage(msg);
                    }
                }

            }
        }else{
            synchronized (mShareFriendDataList){
                FriendData data;
                for(int i=0;i < mShareFriendDataList.size();i++){
                    data=mShareFriendDataList.get(i);
                    if(data.getUserId().equals(userSharedData.getSharedUserId())){
                        Log.d(TAG,"UpdateShareUser MESSAGE_DEL_SHARE");
                        Message msg=new Message();
                        msg.what=MESSAGE_UPDATE_UI;
                        msg.arg1=TYPE.REMOVE_SHARE.ordinal();
                        msg.obj=(Object)data;

                        mHandler.sendMessage(msg);
                    }
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_hub_share);

        mContext=this;
        getWindow().setBackgroundDrawableResource(R.drawable.background);

        LayoutInflater inflator = (LayoutInflater) this .getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.room_hub_share_header, null);

        ActionBar actionBar=getActionBar();
        actionBar.setCustomView(v);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        mBtnMyList=(ImageView)actionBar.getCustomView().findViewById(R.id.btn_people);
        mBtnMyList.setOnClickListener(this);

        mAccountMgr=getAccountManager();
        mAccountMgr.registerUserFriendChanged(this);
        mAccountMgr.registerShareUserChanged(this);
        mRoomHubMgr=getRoomHubManager();
        mRoomHubMgr.registerRoomHubChange(this);
        MAX_LISTITEM_COUNT =getResources().getInteger(R.integer.config_max_share_list_count);
        MAX_LISTITEM_COUNT_NO_ADD_PEOPLE=getResources().getInteger(R.integer.config_max_share_list_count_no_addpeople);

        deviceCategory = getIntent().getExtras().getInt(GlobalDef.KEY_DEVICE_CATEGORY);
        if(deviceCategory > 0) {
            deviceType = getIntent().getExtras().getInt(GlobalDef.KEY_DEVICE_TYPE);
            int titleResource = getHealthDeviceManager().getHealthDeviceTypeTitleResource(deviceType);
            if(titleResource > 0) {
                TextView title = (TextView)actionBar.getCustomView().findViewById(R.id.txt_title);
                title.setText(getString(R.string.share_device,getString(titleResource)));
            }
        }
    }

    @Override
    protected void onPause() {
        if(mShareFriendDataList !=null)
            mShareFriendDataList.clear();
        bBtnCancel=false;
        super.onPause();
    }

    @Override
    protected void onResume() {

        initLayout();
        super.onResume();
    }

    private void initLayout(){

        String dev_name=getIntent().getExtras().getString(RoomHubManager.KEY_DEV_NAME);
        mCurUuid=getIntent().getExtras().getString(RoomHubManager.KEY_UUID);

        mTxtShareDesc = (TextView) findViewById(R.id.txt_share_device_list);
        mTxtShareDesc.setText(String.format(getResources().getString(R.string.share_device_list), dev_name));

        mLstShare =  (ListView) findViewById(R.id.lst_share);
        mLstExist =  (ListView) findViewById(R.id.lst_exist);

        mFriendDataList=mAccountMgr.GetUserFriendList();
        //mDeviceUserList=mRoomHubMgr.GetDeviceUserList(mCurUuid);
        if(deviceCategory > 0)
            handleUserDataShared();
        else
            handleDeviceUserShared();

        mSharePeopleAdapter=new SharePeopleAdapter(this,mShareFriendDataList);
        mLstShare.setAdapter(mSharePeopleAdapter);

        mPeopleAdapter=new PeopleAdapter(this,mFriendDataList);
        mLstExist.setAdapter(mPeopleAdapter);

        UpdateListView();

        LinearLayout ll=(LinearLayout)findViewById(R.id.ll_add_people);
        ll.setVisibility(View.VISIBLE);
        mBtnAddPeople = (Button) findViewById(R.id.btn_add_people);
        mBtnAddPeople.setOnClickListener(this);

        mBtnCancel = (ImageView) findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(this);
    }

    private void handleUserDataShared(){
        mShareFriendDataList = mAccountMgr.GetUserDataSharedFriends(deviceCategory,deviceType);
        if((mShareFriendDataList != null) && (mFriendDataList != null)) {
            synchronized (mFriendDataList) {
                for (Iterator<FriendData> it = mFriendDataList.iterator(); it.hasNext(); ) {
                    FriendData friend_data = it.next();
                    for (FriendData share_friend_data : mShareFriendDataList) {
                        if (friend_data.getUserId().equalsIgnoreCase(share_friend_data.getUserId())) {
                            it.remove();
                            break;
                        }
                    }
                }
            }
        }
    }

    private void handleDeviceUserShared(){
        ArrayList<CloudDevice> device_user_list;
        device_user_list=mAccountMgr.GetDeviceUserList(mCurUuid);

        FriendData friend_data;
        if((device_user_list != null) && (mFriendDataList != null)) {
            for (int i = 0; i < device_user_list.size(); i++) {
                CloudDevice cloud_device = device_user_list.get(i);
                Log.d(TAG,"onCreate user_id="+cloud_device.getUser_id());
                for (int j = 0; j < mFriendDataList.size(); j++) {
                    friend_data = mFriendDataList.get(j);
                    if (cloud_device.getUser_id().equalsIgnoreCase(friend_data.getUserId()) &&
                            cloud_device.getRoleName().equals(RoomHubDef.ROLE_USER)) {
                        mShareFriendDataList.add(friend_data);
                        mFriendDataList.remove(j);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAccountMgr != null) {
            mAccountMgr.unRegisterUserFriendChanged(this);
            mAccountMgr.unRegisterShareUserChanged(this);
        }
        if(mRoomHubMgr != null){
            mRoomHubMgr.unRegisterRoomHubChange(this);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle bundle;
        switch (v.getId()){
            case R.id.btn_add_people:
                intent = new Intent();
                intent.setClass(this, EditPeopleActivity.class);
                bundle = new Bundle();
                bundle.putSerializable(RoomHubManager.KEY_CMD_TYPE, MyListActivity.CMD.ADD);
                bundle.putString(RoomHubManager.KEY_UUID, mCurUuid);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.btn_people:
                intent = new Intent();
                intent.setClass(this, MyListActivity.class);
                bundle = new Bundle();
                bundle.putString(RoomHubManager.KEY_UUID, mCurUuid);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.btn_cancel:
                LinearLayout ll=(LinearLayout) findViewById(R.id.ll_add_people);
                ll.setVisibility(View.GONE);
                bBtnCancel=true;
                UpdateListView();
                //setListViewHeightBasedOnChildren(mLstShare);
                //setListViewHeightBasedOnChildren(mLstExist);
                break;
        }
    }

    @Override
    public void addDevice(RoomHubData data) {

    }

    @Override
    public void removeDevice(RoomHubData data) {
        if(data!=null) {
            if(data.getUuid().equals(mCurUuid))
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        if(data != null) {
            if ((type == RoomHubManager.UPDATE_ONLINE_STATUS)) {
                if (data.getUuid().equals(mCurUuid) && (!data.IsOnLine())) {
                    mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
                }
            }
        }
    }
    /*
    @Override
    public void UpdateRoomHubDeviceSeq(MicroLocationData locationData) {

    }
    */
    @Override
    public void UpdateDeviceShareUser(CloudDevice device) {
        /*
        if(device!=null)
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_REFRESH_SHARE_USER, device));
        */
    }

    @Override
    public void UpgradeStatus(String uuid, boolean is_upgrade) {
        if(uuid.equals(mCurUuid) && (is_upgrade == true)){
            mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
    }

    private class SharePeopleAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<FriendData> mList;
        private LayoutInflater inflater = null;
        private FriendData mData;
        private class ViewHolder {
            TextView tv_name;
            ImageView btn_action;
        }

        public SharePeopleAdapter(Context context, ArrayList<FriendData> data) {
            mContext = context;
            mList = data;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if(mList == null) return 0;
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            if(mList == null) return null;
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(mList == null) return null;
            final ViewHolder holder;
            mData=mList.get(position);
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.room_hub_share_item, null);
                holder = new ViewHolder();
                holder.tv_name=(TextView) convertView.findViewById(R.id.txt_name);
                holder.btn_action=(ImageView) convertView.findViewById(R.id.btn_action);
                convertView.setTag(holder);
            }else
                holder = (ViewHolder)convertView.getTag();


            holder.tv_name.setText(mData.getNickName());
            holder.btn_action.setBackground(mContext.getResources().getDrawable(R.drawable.sharehub_del_btn_selector));

            holder.btn_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Delete Share user position="+position);
                    FriendData data=mList.get(position);
                    Log.d(TAG, "Delete Share user user_id="+data.getUserId());
                    mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_DEL_SHARE, data.getUserId()));
                }
            });

            return convertView;
        }
    }

    private class PeopleAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<FriendData> mList;
        private LayoutInflater inflater = null;
        private FriendData mData;

        private class ViewHolder {
            TextView tv_name;
            ImageView btn_action;
        }
        public PeopleAdapter(Context context, ArrayList<FriendData> data) {
            mContext = context;
            mList = data;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if(mList == null) return 0;

            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            if(mList == null) return null;

            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(mList == null) return null;

            final ViewHolder holder;
            mData=mList.get(position);
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.room_hub_share_item, null);
                holder = new ViewHolder();
                holder.tv_name=(TextView) convertView.findViewById(R.id.txt_name);
                holder.btn_action=(ImageView) convertView.findViewById(R.id.btn_action);
                convertView.setTag(holder);
            }else
                holder = (ViewHolder)convertView.getTag();


            holder.tv_name.setText(mData.getNickName());
            holder.btn_action.setBackground(mContext.getResources().getDrawable(R.drawable.sharehub_add_btn_selector));

            holder.btn_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"add Share user");
                    mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_ADD_TO_SHARE, position));
                }
            });

            return convertView;
        }
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }
        int item_max_height=0;

        int totalHeight = 0;

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
            if(i == 0) {
                item_max_height = totalHeight * mMaxListViewCnt;
            }
            if(totalHeight > item_max_height) {
                totalHeight = item_max_height;
                break;
            }
        }

        if(totalHeight <= item_max_height) {
            listView.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
            listView.requestLayout();
        }
    }

    private void UpdateListView(){
        int share_cnt=mLstShare.getAdapter().getCount();
        int friend_cnt=mLstExist.getAdapter().getCount();

        if(share_cnt <= 0) {
            mLstShare.setVisibility(View.GONE);
            if(friend_cnt > 0) {
                if(bBtnCancel)
                    mMaxListViewCnt=MAX_LISTITEM_COUNT_NO_ADD_PEOPLE*2;
                else
                    mMaxListViewCnt=MAX_LISTITEM_COUNT*2;

                setListViewHeightBasedOnChildren(mLstExist);
            }
        }else{
            mLstShare.setVisibility(View.VISIBLE);
            int max_listview_cnt;

            if(bBtnCancel){
                max_listview_cnt=MAX_LISTITEM_COUNT_NO_ADD_PEOPLE;
            }else{
                max_listview_cnt=MAX_LISTITEM_COUNT;
            }

            if(share_cnt <= max_listview_cnt){
                mMaxListViewCnt=share_cnt;
                setListViewHeightBasedOnChildren(mLstShare);
                mMaxListViewCnt=max_listview_cnt+(max_listview_cnt-share_cnt);
                setListViewHeightBasedOnChildren(mLstExist);
            }else{
                mMaxListViewCnt=max_listview_cnt;
                setListViewHeightBasedOnChildren(mLstShare);
                setListViewHeightBasedOnChildren(mLstExist);
            }

        }
    }

    @Override
    public void AddFriend(FriendData friend_data) {
        if(friend_data == null) return;
        synchronized (mFriendDataList) {
            mFriendDataList.add(friend_data);
        }

        mHandler.sendEmptyMessage(MESSAGE_REFRESH_FRIEND);
    }

    @Override
    public void UpdateFriend(FriendData friend_data) {
        if(friend_data == null) return;
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_NICK_NAME, friend_data));
    }

    @Override
    public void RemoveFriend(FriendData friend_data) {
        if(friend_data == null) return;
        synchronized (mFriendDataList) {
            mFriendDataList.remove(friend_data);
        }
        mHandler.sendEmptyMessage(MESSAGE_REFRESH_FRIEND);
    }

    @Override
    public void AddShareUser(CloudDevice device) {
        if(device!=null)
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_REFRESH_SHARE_USER, device));
    }

    @Override
    public void RemoveShareUser(CloudDevice device) {
        if(device!=null)
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_REFRESH_SHARE_USER, device));
    }

    @Override
    public void UserSharedData(UserSharedDataResPack userSharedData) {
        if(userSharedData!=null)
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATA_USERDATA_SHARE, userSharedData));
    }

}

