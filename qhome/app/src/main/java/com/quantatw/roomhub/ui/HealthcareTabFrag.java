package com.quantatw.roomhub.ui;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceChangeListener;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceUpdateType;
import com.quantatw.roomhub.manager.health.listener.ShareHealthDataListener;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceController;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceManager;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.listener.ShareUserListener;
import com.quantatw.sls.pack.account.UserSharedDataResPack;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by erin on 5/16/16.
 */
public class HealthcareTabFrag extends Fragment implements AdapterView.OnItemClickListener,
        HealthDeviceChangeListener,ShareHealthDataListener {
    private final String TAG=HealthcareTabFrag.class.getSimpleName();

    private HealthDeviceManager mHealthDeviceManager;
    private ArrayList<HealthData> mHealthDataList;
    private HealthDeviceAdapter mHealthDeviceAdapter;

    private View contentLayout, addDeviceLayout;
    private GridView devlist_gv;

    private HandlerThread mBackgroundThread;
    private BackgroundHandler mBackgroundHandler;

    private final int MESSAGE_ADD_DEVICE = 101;
    private final int MESSAGE_REMOVE_DEVICE = 102;
    private final int MESSAGE_UPDATE_DEVICE = 103;
    private final int MESSAGE_GET_LAST_DATA = 104;
    private final int MESSAGE_ADD_SHARE_HEALTHDATA = 105;
    private final int MESSAGE_REMOVE_SHARE_HEALTHDATA = 106;

    public static final String KEY_DEVICE= "device";
    public static final String KEY_UPDATE_CONTENT_TYPE = "update_type";

    private final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            switch(msg.what) {
                case MESSAGE_ADD_DEVICE:
                case MESSAGE_REMOVE_DEVICE: {
                    log("--- "+(msg.what==MESSAGE_ADD_DEVICE?"MESSAGE_ADD_DEVICE":"MESSAGE_REMOVE_DEVICE"+" ---"));
                    final HealthData healthData = (HealthData) msg.obj;
                    final int message = msg.what;
                    new Handler(getActivity().getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refreshList(message, healthData);
                        }
                    },0);
                    if(msg.what == MESSAGE_ADD_DEVICE)
                        sendEmptyMessageDelayed(MESSAGE_GET_LAST_DATA,1000);
                }
                    break;
                case MESSAGE_GET_LAST_DATA: {
                    log("--- MESSAGE_GET_LAST_DATA ---");
                    new Handler(getActivity().getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            invalidateViews();
                            switchViewLayout();
                        }
                    },0);
                }
                    break;
                case MESSAGE_ADD_SHARE_HEALTHDATA:
                case MESSAGE_REMOVE_SHARE_HEALTHDATA:
                case MESSAGE_UPDATE_DEVICE: {
                    final HealthData healthData = msg.getData().getParcelable(KEY_DEVICE);
                    final int updateType = msg.getData().getInt(KEY_UPDATE_CONTENT_TYPE);
                    new Handler(getActivity().getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateData(updateType, healthData);
                        }
                    },1000);
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            updateData(updateType, healthData);
//                        }
//                    });
                }
                    break;
            }
        }
    }

    private void refreshList(int message, HealthData healthData) {
        HealthData data = getHealthDataByUuid(healthData.getUuid());
        log("refreshList enter");
        if(data != null) {
            if(message == MESSAGE_REMOVE_DEVICE) {
                log("remove device");
                synchronized (mHealthDataList) {
                    mHealthDataList.remove(data);
                }
            }
        }
        else {
            if(message == MESSAGE_ADD_DEVICE) {
                log("add device");
                log(healthData.getRoleName()+"/"+healthData.getOwnerId());
                synchronized (mHealthDataList) {
                    mHealthDataList.add(healthData);
                }
            }
        }

        Collections.sort(mHealthDataList);
        invalidateViews();
        switchViewLayout();
    }

    private void updateData(int updateType, HealthData healthData) {
        // TODO: update item
        log("updateData enter updateType="+updateType);
        invalidateViews();
     }

    private void invalidateViews() {
        mHealthDeviceAdapter.notifyDataSetChanged();
        devlist_gv.invalidateViews();
    }

    private HealthData getHealthDataByUuid(String uuid) {
        for(HealthData healthData: mHealthDataList) {
            if(healthData.getUuid().equals(uuid))
                return healthData;
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBackgroundThread=new HandlerThread("MainHealthcareFragment");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());

        mHealthDeviceManager = ((RoomHubApplication)getActivity().getApplicationContext()).getHealthDeviceManager();

        mHealthDeviceManager.registerHealthDeviceChangeListener(this);
        mHealthDeviceManager.registerShareHealthDataListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_main_healthcare, container, false);

        contentLayout = view.findViewById(R.id.ll_devlist);
        devlist_gv= (GridView) contentLayout.findViewById(R.id.healthcare_devlist);
        devlist_gv.setOnItemClickListener(this);

        addDeviceLayout = view.findViewById(R.id.ll_add_healthcare_dev);
        ImageView addDeviceBtn = (ImageView)addDeviceLayout.findViewById(R.id.btn_add_healthcare_dev);
        addDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AccountManager accountManager = ((RoomHubApplication)getActivity().getApplicationContext()).getAccountManager();
                if (!accountManager.isLogin()) {
                    Utils.ShowLoginActivity(getActivity(), RoomHubMainPage.class);
                    return;
                }

                if(Utils.isAllowToAddHealthcareDevice(getActivity())) {
                    Intent intent = new Intent(getActivity(), AddHealthcareActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        mHealthDataList = mHealthDeviceManager.getAllHealthDeviceList();
        mHealthDataList = mHealthDeviceManager.getSharedHealthDataList(DeviceTypeConvertApi.TYPE_HEALTH.BPM);
        mHealthDeviceAdapter = new HealthDeviceAdapter(getActivity(), mHealthDataList);
        devlist_gv.setAdapter(mHealthDeviceAdapter);
        switchViewLayout();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHealthDeviceManager.unregisterHealthDeviceChangeListener(this);
        mHealthDeviceManager.unregisterShareHealthDataListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HealthData healthData = mHealthDeviceAdapter.getItem(position);
        HealthDeviceController healthDeviceController = mHealthDeviceManager.getDeviceManager(healthData.getType());
        healthDeviceController.getViewController().onItemClick(healthData);
    }

    @Override
    public void updateDevice(int type, HealthData device) {
        Message msg = new Message();
        msg.what = MESSAGE_UPDATE_DEVICE;
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_DEVICE, device);
        bundle.putInt(KEY_UPDATE_CONTENT_TYPE, type);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    @Override
    public void addDeivce(HealthData device) {
        if(!HealthDeviceManager.NEW_BPM_SHARE_STYLE) {
            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_ADD_DEVICE, device));
        }
    }

    @Override
    public void removeDevice(HealthData device) {
        if(!HealthDeviceManager.NEW_BPM_SHARE_STYLE) {
            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_REMOVE_DEVICE, device));
        }
    }

    @Override
    public void updateHealthData(HealthData healthData) {
    }

    @Override
    public void addShareHealthData(HealthData healthData) {
        mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_ADD_SHARE_HEALTHDATA,healthData));
    }

    @Override
    public void removeHealthData(HealthData healthData) {
        mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_REMOVE_SHARE_HEALTHDATA,healthData));
    }

    private void switchViewLayout() {
        if(mHealthDataList != null && mHealthDataList.size() > 0) {
            contentLayout.setVisibility(View.VISIBLE);
            addDeviceLayout.setVisibility(View.GONE);
        }
        else {
            contentLayout.setVisibility(View.GONE);
            addDeviceLayout.setVisibility(View.VISIBLE);
        }
    }

    private void log(String msg) {
        mHealthDeviceManager.traceLog(TAG,msg);
    }
}
