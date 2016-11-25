package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager;
import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.ui.bpm.BPMHistoryActivity;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 95010915 on 2015/9/24.
 */
public class RoomHubMainPage extends AbstractRoomHubActivity {//implements AdapterView.OnItemClickListener,View.OnClickListener,RoomHubChangeListener,ElectricChangeListener,AssetChangeListener{
    private static final String TAG = "RoomHubMainPageNew";
    private static boolean DEBUG=true;

    private Context mContext;
    private AccountManager mAccountMgr;
    private RoomHubManager mRoomHubMgr;
    /*
    private ACManager mACMgr;
    private FANManager mFANMgr;
    */
    private RoomHubAdapter mAdapter;
    String[] drawer_menu;

    View view1;
    View view2;

    protected static final int MENU1=0;
    protected static final int MENU2=MENU1+1;
    protected static final int MENU3=MENU2+1;

    protected static final int TAB_APPLIANCE = 1;
    protected static final int TAB_HEALTH = 2;

    AppliancesTabFrag mainFrag;

    private DrawerLayout layDrawer;
    private ListView lstDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private TextView txtDrawAccount;
    private TextView txtDrawEmail;

    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_hub_main_page);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.x = -500;
        params.height = 800;
        params.width = 600;
        mContext = this;

        view1 = (View) findViewById(R.id.view_appliance);
        view2 = (View) findViewById(R.id.view_health);
        txtDrawAccount = (TextView) findViewById(R.id.txt_drawer_name);
        txtDrawEmail = (TextView) findViewById(R.id.txt_drawer_email);

        view1.setVisibility(View.VISIBLE);
        view2.setVisibility(View.INVISIBLE);
        FragmentManager fragMgr = getSupportFragmentManager();
        mainFrag = new AppliancesTabFrag();
        fragMgr.beginTransaction()
                .replace(R.id.frameLayout, mainFrag)
                .commit();

//        getWindow().setBackgroundDrawableResource(R.drawable.background);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        initActionBar();
        initDrawer();
        initDrawerList();
        OpenMenu(this, R.menu.menu_settings);

        mAccountMgr=getAccountManager();
        mRoomHubMgr=getRoomHubManager();
        /*
        mACMgr=getACManager();
        mFANMgr=getFANManager();
        */
        /* show GCM messagee when click on notification from status bar */
        if(getIntent() != null) {
            int gcmMessageTypeId = getIntent().getIntExtra(GlobalDef.GCM_MESSAGE_TYPE_ID,0);
            String gcmCustomMessage = getIntent().getStringExtra(GlobalDef.GCM_MESSAGE);
            if(!TextUtils.isEmpty(gcmCustomMessage)) {
                ReminderDialog dialog = new ReminderDialog(this, gcmMessageTypeId, gcmCustomMessage);
                dialog.show();
            }
            String lauchBPMUuid = getIntent().getStringExtra(GlobalDef.BP_UUID_MESSAGE);
            String launchBPMUserId = getIntent().getStringExtra(GlobalDef.BP_USERID_MESSAGE);
            if(mAccountMgr.isLogin() && (!TextUtils.isEmpty(lauchBPMUuid) || !TextUtils.isEmpty(launchBPMUserId))) {
                Intent intent = new Intent(mContext,BPMHistoryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(GlobalDef.BP_UUID_MESSAGE,lauchBPMUuid);
                intent.putExtra(GlobalDef.BP_USERID_MESSAGE,launchBPMUserId);
                mContext.startActivity(intent);
            }
        }

        /* show restore mobile dialog for Android M (SDK > 23) */
        if (Utils.hasPromptDisableMobileData(mContext)) {
            Utils.setPromptDisableMobileData(this, false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    promptRestoreMobileDialog();
                }
            },1000);
        }

    }

    public void ApplianceTab(View view){
        TabChoose(TAB_APPLIANCE);
        view1.setVisibility(View.VISIBLE);
        view2.setVisibility(View.INVISIBLE);
        FragmentManager fragMgr = getSupportFragmentManager();
        mainFrag = new AppliancesTabFrag();
        fragMgr.beginTransaction()
                .replace(R.id.frameLayout, mainFrag)
                .commit();
    }

    public void HealthCareTab(View view){
        TabChoose(TAB_HEALTH);
        view1.setVisibility(View.INVISIBLE);
        view2.setVisibility(View.VISIBLE);
        FragmentManager fragMgr = getSupportFragmentManager();
        HealthcareTabFrag mainFragHealth = new HealthcareTabFrag();
        fragMgr.beginTransaction()
                .replace(R.id.frameLayout, mainFragHealth)
                .commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void selectItem(int position) {
        Intent intent;
        switch (position) {
            case 0:
                intent = new Intent(mContext, EditProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                break;
            case 1:
                intent = new Intent(mContext, NotificationCenterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case 2:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.config_feedback_url).toLowerCase()));
                startActivity(intent);
                break;
            case 3:
                intent = new Intent(mContext, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            default:
                return;
        }

        lstDrawer.setItemChecked(position, true);
        setTitle(drawer_menu[position]);
        layDrawer.closeDrawers();
    }
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
//        getActionBar().setTitle(mTitle);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void initActionBar(){
//        getActionBar().setDisplayHomeAsUpEnabled(true);
//        getActionBar().setHomeButtonEnabled(true);
    }
    private void initDrawer(){
        layDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        lstDrawer = (ListView) findViewById(R.id.left_drawer);
        //layDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mTitle = mDrawerTitle = getTitle();
        drawerToggle = new ActionBarDrawerToggle(
                this,
                layDrawer,
                R.drawable.menu,
                R.string.drawer_open,
                R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(mTitle);
//                getActionBar().setTitle(mDrawerTitle);
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                txtDrawAccount.setText(mAccountMgr.getCurrentAccountName());
                txtDrawEmail.setText(mAccountMgr.getCurrentEmail());
//                getActionBar().setTitle(mDrawerTitle);
            }

        };
        drawerToggle.syncState();
        layDrawer.setDrawerListener(drawerToggle);
    }

    private int[] image = {
            R.drawable.icon_account2,
            R.drawable.icon_msg,
            R.drawable.icon_info2,
            R.drawable.icon_setting
    };

    private void initDrawerList(){
//        drawer_menu = this.getResources().getStringArray(R.array.drawer_menu);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawer_menu);
//        lstDrawer.setAdapter(adapter);
//        lstDrawer.setOnItemClickListener(new DrawerItemClickListener());

        drawer_menu = this.getResources().getStringArray(R.array.drawer_menu);

        //List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
        List<HashMap<String,String>> lstData = new ArrayList<HashMap<String,String>>();
        for (int i = 0; i < image.length; i++) {
            HashMap<String, String> mapValue = new HashMap<String, String>();
            mapValue.put("icon", Integer.toString(image[i]));
            mapValue.put("title", drawer_menu[i]);
            lstData.add(mapValue);
        }
        SimpleAdapter adapter = new SimpleAdapter(this,
            lstData, R.layout.drawer_list_item,
            new String[]{"icon", "title"}, new int[]{R.id.imgIcon, R.id.txtItem});
            lstDrawer.setAdapter(adapter);

        lstDrawer.setOnItemClickListener(new DrawerItemClickListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
//        getActionBar().setTitle(mDrawerTitle);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //dismissProgressDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void OpenRoomHubListMenu(int index,View v){
        mainFrag.OpenRoomHubListMenu(index,v);
    }

    public void LaunchAddElectric(int pos,RoomHubData roomhub_data){
        mainFrag.LaunchAddElectric(pos,roomhub_data);
    }

    public void LaunchElectricActivity(int pos,int type,RoomHubData roomhub_data){
        mainFrag.LaunchElectricActivity(pos,type,roomhub_data);
    }

/*
    public int getPMStatus(int pos, int type, RoomHubData roomhub_data) {
        return mainFrag.getPMStatus(pos, type, roomhub_data);
    }
*/
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
//            if((System.currentTimeMillis()-exitTime) > 2000){
//                Toast.makeText(this, R.string.exit_device, Toast.LENGTH_SHORT).show();
//                exitTime = System.currentTimeMillis();
//            }
//            else{
//                ((RoomHubApplication)getApplicationContext()).onTerminate();
//                System.exit(0);
//            }
//
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
/*
    public boolean isPowerOn(int pos, RoomHubData roomhub_data, int type){
        boolean tmp = mainFrag.isPowerOn(pos,roomhub_data,type);
        return tmp;
    }
*/
    /*
    public void SelectIR(int index) {
        Log.d(TAG, "SelectIR index=" + index);
        RoomHubData data = getRoomHubDataByIdx(index);
        String uuid = data.getUuid();

        if(data.IsUpgrade()) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.device_upgrade_not_operate), Toast.LENGTH_SHORT).show();
        }else if(!data.isOnLine()) {
            Toast.makeText(mContext,mContext.getResources().getString(R.string.device_offline_not_operate),Toast.LENGTH_SHORT).show();
        }else if((!data.IsOwner()) && (!data.IsFriend())) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.insufficient_permissions), Toast.LENGTH_SHORT).show();
        }else{
            if(mRoomHubMgr.IsAlljoyn(uuid)){
                ((RoomHubMainPage) mContext).configIRSetting(uuid);
            }else
                Toast.makeText(mContext,mContext.getResources().getString(R.string.roomhub_warning_msg),Toast.LENGTH_SHORT).show();
        }
    }

    public void configIRSetting(String uuid) {
        Intent intent = new Intent(mContext,IRSettingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID,uuid);
        startActivity(intent);
    }

    public void configIRSetting(int index) {
        Log.d(TAG, "configIRSettings index=" + index);
        RoomHubData devData = getRoomHubDataByIdx(index);
        String uuid = devData.getUuid();
        Intent intent = new Intent(mContext,IRSettingActivity.class);
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID,uuid);
        startActivityForResult(intent, IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE);
    }

    public boolean isIRPair(int index) {
        RoomHubData devData = getRoomHubDataByIdx(index);
        if(devData != null) {
            return mRoomHubMgr.IsIRPair(devData.getUuid());
        }

        return false;
    }
    */
/*
    public boolean isAlljoyn(int index) {
        RoomHubData devData = getRoomHubDataByIdx(index);
        if(devData != null) {
            return mRoomHubMgr.IsAlljoyn(devData.getUuid());
        }
        return false;
    }

    public boolean isAlljoyn(String uuid) {
        RoomHubData devData = getRoomHubDataByUuid(uuid);
        if(devData != null) {
            return mRoomHubMgr.IsAlljoyn(devData.getUuid());
        }
        return false;
    }

    public boolean isSchedule(int index){
        RoomHubData devData = getRoomHubDataByIdx(index);
        if(devData != null) {
            ArrayList<Schedule> schedules=mRoomHubMgr.getAllSchedules(devData.getUuid());
            if((schedules != null) && (schedules.size() > 0))
                return true;
        }
        return false;
    }

    public boolean isShareList(int index){
        RoomHubData data = getRoomHubDataByIdx(index);
        if(data != null) {
            if(data.getShareCnt() > 0)
                return true;
        }
        return false;
    }
*/

    public void CloseAllDevices(){
        mainFrag.CloseAllDevices();
    }

    private void promptRestoreMobileDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.onboarding_done_restore_mobile));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setText(getString(R.string.enable_mobile_data));
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setPromptDisableMobileData(mContext, false);
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(
                        new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                startActivity(intent);
                dialog.dismiss();
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setText(getString(R.string.continue_use_wifi));
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
