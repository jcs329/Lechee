package com.quantatw.roomhub.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceController;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceManager;

import java.util.ArrayList;

/**
 * Created by erin on 5/17/16.
 */
public class HealthDeviceAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<HealthData> mList;
    private HealthDeviceManager healthDeviceManager;

    HealthDeviceAdapter(Context context, ArrayList<HealthData> list) {
        mContext = context;
        mList = list;
        healthDeviceManager = ((RoomHubApplication)mContext.getApplicationContext()).getHealthDeviceManager();
    }

    @Override
    public int getCount() {
        if(mList == null)
            return 0;
        return mList.size();
    }

    @Override
    public HealthData getItem(int position) {
        if(mList == null)
            return null;
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HealthData healthData = getItem(position);
        HealthDeviceController healthDeviceController = healthDeviceManager.getDeviceManager(healthData.getType());
        return healthDeviceController.getViewController().bindView(healthData,position,convertView,parent);
    }
}
