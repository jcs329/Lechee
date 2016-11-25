package com.quantatw.roomhub.ui.bpm;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.health.bpm.BPMUtils;
import com.quantatw.sls.pack.healthcare.BPMDataInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 95011613 on 2016/3/17.
 */
public class BPMDataInfoAdapter extends BaseAdapter {
    private final String TAG=BPMDataInfoAdapter.class.getSimpleName();
    private Context mContext;

    private ArrayList<BPMDataInfo> mDataList;

    private BPMDataInfo mData;

    private class ViewHolder {
        TextView txt_date;
        TextView txt_time;
        TextView txt_max_bp;
        TextView txt_min_bp;
        TextView txt_heart_rate;
    }

    public BPMDataInfoAdapter(Context context, ArrayList<BPMDataInfo> data_list){
        mContext=context;
        mDataList=data_list;
    }

    @Override
    public int getCount() {
        if(mDataList == null)
            return 0;
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        if(mDataList == null)
            return null;
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (mDataList == null || mDataList.size() <= 0) return null;

        final ViewHolder holder;
        mData = mDataList.get(position);
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.blood_pressure_history_item, null);
            holder = new ViewHolder();

            holder.txt_date = (TextView) convertView.findViewById(R.id.txt_date);
            holder.txt_time = (TextView) convertView.findViewById(R.id.txt_time);
            holder.txt_max_bp = (TextView) convertView.findViewById(R.id.txt_max_bp);
            holder.txt_min_bp = (TextView) convertView.findViewById(R.id.txt_min_bp);
            holder.txt_heart_rate = (TextView) convertView.findViewById(R.id.txt_hear_rate);

            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder)convertView.getTag();


        holder.txt_date.setText(BPMUtils.getDateString(mData.getMeasureDate(),"yyyy/MM/dd"));
        holder.txt_time.setText(BPMUtils.getDateString(mData.getMeasureDate(),"HH:mm"));

        BPMUtils.setSystolicValue(mData.getMaxBloodPressure(), holder.txt_max_bp);
        BPMUtils.setDialstolicValue(mData.getMinBloodPressure(), holder.txt_min_bp);
        BPMUtils.setHeartRateValue(mData.getHeartRate(), holder.txt_heart_rate);
        return convertView;
    }
}
