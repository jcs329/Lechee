package com.quantatw.roomhub.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.AssetChangeListener;
import com.quantatw.roomhub.listener.ScheduleChangeListener;
import com.quantatw.roomhub.manager.asset.manager.ACData;
import com.quantatw.roomhub.manager.asset.manager.ACManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.Schedule;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.roomhub.NextScheduleResPack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by 95010915 on 2015/9/24.
 */
public class ScheduleListActivity extends AbstractRoomHubActivity implements AdapterView.OnItemClickListener,OnClickListener,
        AssetChangeListener,ScheduleChangeListener {
    private static final String TAG = "ScheduleListActivity";
    private static boolean DEBUG=true;

    private Context mContext;

    private ACManager mACMgr;

    private ScheduleAdapter mAdapter;

    private GridView list_gv;
    private Button mBtnAdd;
    private Button mBtnHistory;

    protected static final int MENU1=0;
    protected static final int MENU2=MENU1+1;

    private ACData mData;
    private ArrayList<Schedule> mSchedules;

    private final int MESSAGE_UPDATE_SCHEDULE       = 100;
    private final int MESSAGE_UPDATE_ALL_SCHEDULE   = 101;
    private final int MESSAGE_DELETE_SCHEDULE       = 102;
    private final int MESSAGE_LAUNCH_DEVICE_LIST    = 103;

    private boolean isHome=true;

    public enum CMD{
        ADD,
        EDIT
    }
    class sortByTime implements Comparator<Schedule>{
        String[] splitTime1,splitTime2;
        int time1, time2;
        @Override
        public int compare(Schedule lhs, Schedule rhs) {
                splitTime1 = lhs.getStartTime().split(":");
                splitTime2 = rhs.getStartTime().split(":");
            try {
                time1 = Integer.valueOf(splitTime1[0]) * 100 + Integer.valueOf(splitTime1[1]);
                time2 = Integer.valueOf(splitTime2[0]) * 100 + Integer.valueOf(splitTime2[1]);
                if (time1 > time2)
                    return 1;
                else if (time1 < time2)
                    return -1;
                else
                    return 0;
            }catch (ParseException e) {
                e.printStackTrace();
            return 0;}
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int idx;
            switch(msg.what) {
                case MESSAGE_UPDATE_SCHEDULE: {
                    String uuid=msg.getData().getString(ACManager.KEY_UUID);

                    if(mData.getRoomHubUuid().equals(uuid)){
                        Schedule schedule= (Schedule) msg.getData().getSerializable(ACManager.KEY_CMD_VALUE);
                        idx = getIdxByScheduleIdx(schedule.getIndex());
                        synchronized (mSchedules) {
                            if (idx < 0) {
                                mSchedules.add(schedule);
                            } else {
                                mSchedules.set(idx, schedule);
                            }
                            Collections.sort(mSchedules, new sortByTime());
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                }
                case MESSAGE_UPDATE_ALL_SCHEDULE:{
                    String uuid=msg.getData().getString(ACManager.KEY_UUID);
                    if(mData.getRoomHubUuid().equals(uuid)){
                        mSchedules= (ArrayList<Schedule>) msg.getData().getSerializable(ACManager.KEY_CMD_VALUE);
                        Log.v(TAG, "======sorttime");
                        Collections.sort(mSchedules, new sortByTime());
                        mAdapter.setSchedule(mSchedules);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                }
                case MESSAGE_DELETE_SCHEDULE:
                    /*
                    idx=(int)msg.obj;

                    Schedule schedule=getScheduleByIdx(idx);
                    if(schedule != null){
                        mSchedules.remove(schedule);
                    }
                    */
                    mAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_LAUNCH_DEVICE_LIST:
                    launchDeviceList();
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    @Override
    public void addDevice(int asset_type, Object data) {

    }

    @Override
    public void removeDevice(int asset_type, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.AC) && (data != null)) {
            if (((ACData)data).getAssetUuid().equals(mData.getAssetUuid()))
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
    }

    @Override
    public void UpdateAssetData(int asset_type, Object data) {
    }

    @Override
    public void UpdatePageStatus(int asset_type, boolean enabled, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.AC) && (data != null)) {
            if(((ACData)data).getAssetUuid().equals(mData.getAssetUuid()) && (enabled == false)){
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void onCommandResult(int asset_type, String uuid, int result) {

    }

    @Override
    public void UpdateSchedule(String uuid,Schedule schedule) {
        if(schedule != null) {
            Message msg=new Message();
            msg.what=MESSAGE_UPDATE_SCHEDULE;
            Bundle bundle=new Bundle();
            bundle.putString(ACManager.KEY_UUID,uuid);
            bundle.putSerializable(ACManager.KEY_CMD_VALUE, schedule);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void UpdateAllSchedule(String uuid,ArrayList<Schedule> schedule_lst) {
        if(schedule_lst != null){
            Message msg=new Message();
            msg.what=MESSAGE_UPDATE_ALL_SCHEDULE;
            Bundle bundle=new Bundle();
            bundle.putString(ACManager.KEY_UUID,uuid);
            bundle.putSerializable(ACManager.KEY_CMD_VALUE, schedule_lst);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void NextSchedule(NextScheduleResPack resPack) {

    }

    @Override
    public void DeleteSchedule(int idx) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_DELETE_SCHEDULE,idx));
    }

    @Override
    public void onCommandResult(String uuid,int result) {
        if(DEBUG)
            Log.d(TAG,"onCommandResult uuid="+uuid+" result="+result);
        if(result < ErrorKey.Success) {
            Toast.makeText(this, Utils.getErrorCodeString(this, result),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_list);
        mContext = this;
        getWindow().setBackgroundDrawableResource(R.color.color_very_dark_blue);
        mACMgr=(ACManager)getRoomHubManager().getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.AC);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        /*
        if(isHome==true)
            ledOnOff(mData.getUuid(),true);
        else
            isHome=true;
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        mACMgr.registerAssetsChange(this);
        mACMgr.registerScheduleChange(this);

        String uuid=getIntent().getStringExtra(RoomHubManager.KEY_UUID);
        mData=mACMgr.getCurrentACDataByUuid(uuid);
        if(mData == null) {
            finish();
            return;
        }
        getActionBar().setTitle(mData.getRoomHubData().getName());
        //UpdateBackgroundImage(mData.getFunctionMode());
        mSchedules=mACMgr.getAllSchedules(uuid);
        if(DEBUG)
            Log.d(TAG, "onStart uuid=" + uuid);

        mBtnAdd=(Button)findViewById(R.id.btn_add_schedule);
        mBtnAdd.setOnClickListener(this);

        mBtnHistory=(Button)findViewById(R.id.btn_history);
        mBtnHistory.setOnClickListener(this);

        mAdapter=new ScheduleAdapter(this, uuid,mSchedules);
        list_gv= (GridView) findViewById(R.id.schedule_list);
        list_gv.setAdapter(mAdapter);
        list_gv.setOnItemClickListener(this);
        if(mSchedules != null)
            Collections.sort(mSchedules, new sortByTime());

        Button btn_schedule=(Button)findViewById(R.id.btn_schedule);
        btn_schedule.setSelected(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*
        if(isHome){
            ledOnOff(mData.getUuid(),false);
        }
        */
    }

    @Override
    protected void onPause() {
        super.onPause();

        mACMgr.unRegisterAssetsChange(this);
        mACMgr.unRegisterScheduleChange(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void OpenScheduleMenu(int position,View v){
        final int pos=position;
        final RoomHubMenuDialog menu = new RoomHubMenuDialog(this, mData.getRoomHubData(), getResources().getStringArray(R.array.schedule_menu));
        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Schedule schedule = mSchedules.get(pos);
                switch (position) {
                    case MENU1:
                        Intent intent = new Intent();
                        intent.setClass(mContext, ScheduleSettingActivity.class);

                        Bundle bundle = new Bundle();
                        bundle.putSerializable(RoomHubManager.KEY_CMD_TYPE, CMD.EDIT);
                        //bundle.putParcelable(RoomHubManager.KEY_ROOMHUB_DATA, mData);
                        bundle.putString(RoomHubManager.KEY_UUID, mData.getRoomHubUuid());
                        bundle.putParcelable(RoomHubManager.KEY_CMD_VALUE, schedule);
                        //bundle.putInt(RoomHubManager.KEY_CMD_VALUE, pos);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        menu.dismiss();
                        break;
                    case MENU2:
                        mACMgr.RemoveSchedule(mData.getRoomHubUuid(), schedule.getIndex());
                        menu.dismiss();
                        break;
                }
            }
        });
    }

    public void SwitchOnOff(String uuid,Schedule schedule,boolean is_on){
        schedule.setEnable(is_on);
        mACMgr.ModifySchedule(uuid, schedule);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_add_schedule:
                isHome=false;
                Intent intent = new Intent();
                intent.setClass(mContext, ScheduleSettingActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(RoomHubManager.KEY_CMD_TYPE, CMD.ADD);
                //bundle.putParcelable(RoomHubManager.KEY_ROOMHUB_DATA, mData);
                bundle.putString(RoomHubManager.KEY_UUID, mData.getRoomHubUuid());
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.btn_history:
                Toast.makeText(mContext, mContext.getResources().getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private int getIdxByScheduleIdx(int idx){
        if(mSchedules == null) return -1;

        synchronized (mSchedules){
            for(int i=0 ;i < mSchedules.size();i++){

                if(mSchedules.get(i).getIndex() == idx)
                    return i;
            }
        }

        return -1;
    }
}
