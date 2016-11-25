package com.quantatw.roomhub.manager.health.bpm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceView;
import com.quantatw.roomhub.ui.AboutHealthcareActivity;
import com.quantatw.roomhub.ui.RoomHubApplication;
import com.quantatw.roomhub.ui.bpm.BPMGuideActivity;
import com.quantatw.roomhub.ui.bpm.BPMHistoryActivity;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.sls.pack.healthcare.BPMDataInfo;

/**
 * Created by erin on 5/17/16.
 */
public class BPMViewController extends HealthDeviceView {

    private class ViewHolder {
        ImageView device_image;
        TextView device_name;
        ImageView device_menu;
        View contentLayout;
        TextView remind_message;
        TextView systolic;
        TextView diastolic;
        TextView heart_rate;
        Button tour_guide;
    }

    BPMViewController(Context context, BPMManager bpmManager) {
        super(context,bpmManager);
    }

    public View bindView(final HealthData healthData, final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.healthcare_bpm_item, null);
            holder = new ViewHolder();
            holder.device_image = (ImageView)convertView.findViewById(R.id.healthcare_dev_image);
            holder.device_name = (TextView)convertView.findViewById(R.id.healthcare_dev_name);
            holder.device_menu = (ImageView)convertView.findViewById(R.id.btn_menu);
            holder.contentLayout = convertView.findViewById(R.id.bpm_content_layout);
            holder.remind_message = (TextView) convertView.findViewById(R.id.bpm_remind_message);
            holder.systolic = (TextView)convertView.findViewById(R.id.bpm_systolic);
            holder.diastolic = (TextView)convertView.findViewById(R.id.bpm_diastolic);
            holder.heart_rate = (TextView)convertView.findViewById(R.id.bpm_heart_rate);
            holder.tour_guide = (Button)convertView.findViewById(R.id.btn_bpm_guide);
            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder)convertView.getTag();

        final BPMData bpmData = (BPMData)healthData;
//        ArrayList<BPMDataInfo> history = bpmData.getHistoryList();
        final BPMDataInfo latestData = bpmData.getLastHistory();

        holder.device_name.setText(bpmData.getDeviceName());
        if(bpmData.IsOwner() && latestData != null) {
            holder.device_menu.setVisibility(View.VISIBLE);
            holder.device_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMenu(bpmData);
                }
            });
        }
        else {
            // shared cards
            holder.device_menu.setVisibility(View.GONE);
        }

//        log("bindView uuid="+bpmData.getUuid()+",deviceName:"+bpmData.getDeviceName()+",getLastHistory="+bpmData.getLastHistory());
//        log("bindView "+bpmData.getRoleName()+","+bpmData.getOwnerId());
        if(latestData != null) {
            holder.contentLayout.setVisibility(View.VISIBLE);
            holder.tour_guide.setVisibility(View.GONE);
            String remind_message;
            int days = bpmData.getDaysBeforeCheck();
            if(days <= 0) // today
                remind_message = mContext.getString(R.string.bp_today);
            else {
                remind_message = mContext.getString(R.string.bp_remind_msg,days);
            }
            holder.remind_message.setText(remind_message);
            BPMUtils.setSystolicValue(latestData.getMaxBloodPressure(), holder.systolic);
            BPMUtils.setDialstolicValue(latestData.getMinBloodPressure(), holder.diastolic);
            BPMUtils.setHeartRateValue(latestData.getHeartRate(), holder.heart_rate);
        }
        else {
            // show tour guide
            holder.remind_message.setText(mContext.getString(R.string.bpm_first_use_string));
            holder.contentLayout.setVisibility(View.GONE);
            holder.tour_guide.setVisibility(View.VISIBLE);
        }

        if(holder.contentLayout.getVisibility() == View.VISIBLE) {
            holder.contentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext,BPMHistoryActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(GlobalDef.BP_USERID_MESSAGE,healthData.getFriendData().getUserId());
                    intent.putExtra(GlobalDef.BP_UUID_MESSAGE,healthData.getUuid());
                    mContext.startActivity(intent);
                }
            });
        }

        if(holder.tour_guide.getVisibility() == View.VISIBLE) {
            holder.tour_guide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext,BPMGuideActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });
        }

        return convertView;
    }

    public void onItemClick(HealthData healthData) {
    }

    protected void manageDevice(HealthData healthData) {
        Intent intent = new Intent(mContext, AboutHealthcareActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putParcelable(GlobalDef.KEY_DEVICE_DATA, healthData);
        intent.putExtras(bundle);
        mContext.startActivity(intent);
    }

    private void log(String msg) {
        Log.d("HealthDeviceManager","[BPMViewController] "+msg);
    }
}
