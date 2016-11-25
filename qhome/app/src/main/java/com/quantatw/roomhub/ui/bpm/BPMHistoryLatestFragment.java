package com.quantatw.roomhub.ui.bpm;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.health.bpm.BPMData;
import com.quantatw.roomhub.manager.health.bpm.BPMManager;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceManager;
import com.quantatw.roomhub.ui.RoomHubApplication;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.sls.pack.healthcare.BPMDataInfo;

import java.util.ArrayList;

/**
 * Created by erin on 5/16/16.
 */
public class BPMHistoryLatestFragment extends BPMHistoryFragment{

    private HealthDeviceManager mHealthDeviceManager;
    private String mCurrentUuid;
    private BPMData mBPMData;
    private GridView mHistoryGv;
    private BPMDataInfoAdapter mBPAdapter;
    private ProgressDialog mProgressDialog;
    private ArrayList<BPMDataInfo> mHistoryList;

    private final int MESSAGE_GET_LIST = 100;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_GET_LIST:
                    if(mProgressDialog == null)
                        mProgressDialog = ProgressDialog.show(getActivity(),"",getString(R.string.process_str),true);
                    else
                        mProgressDialog.show();

                    new Thread() {
                        @Override
                        public void run() {
                            if(HealthDeviceManager.NEW_BPM_SHARE_STYLE) {
                                final BPMManager bpmManager = (BPMManager) mHealthDeviceManager.getDeviceManager(mBPMData.getType());
                                mHistoryList = bpmManager.getHistoryList(mBPMData);
                            }
                            else {
                                final BPMData bpmData = (BPMData) mHealthDeviceManager.getHealthDataByUuid(mCurrentUuid);
                                final BPMManager bpmManager = (BPMManager) mHealthDeviceManager.getDeviceManager(bpmData.getType());
                                mHistoryList = bpmManager.getHistoryList(bpmData);
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshUI();
                                }
                            });
                        }
                    }.start();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void refreshUI() {
        mBPAdapter = new BPMDataInfoAdapter(getActivity(), mHistoryList);
        mHistoryGv.setAdapter(mBPAdapter);
        mProgressDialog.dismiss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHealthDeviceManager = ((RoomHubApplication)getActivity().getApplication()).getHealthDeviceManager();

        Bundle data = getArguments();
        if(data != null) {
            mCurrentUuid = data.getString(GlobalDef.BP_UUID_MESSAGE);
            if(HealthDeviceManager.NEW_BPM_SHARE_STYLE)
                mBPMData = data.getParcelable(GlobalDef.BP_DATA_MESSAGE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_bpm_history_latest, container, false);

        mHistoryGv =  (GridView) view.findViewById(R.id.bp_history_lst);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((BPMHistoryActivity)context).onTabSelected(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(MESSAGE_GET_LIST);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void refreshUpdate(int type, HealthData device) {
        mHealthDeviceManager.traceLog("updateDevice", "update type=" + type);
        mHandler.sendEmptyMessage(MESSAGE_GET_LIST);
    }

    protected void refreshAdd(HealthData device) {
    }

    protected void refreshRemove(HealthData device) {
    }

}
