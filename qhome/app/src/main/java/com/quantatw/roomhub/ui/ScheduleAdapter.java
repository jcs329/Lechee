package com.quantatw.roomhub.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.device.Schedule;

import java.util.ArrayList;

/**
 * Created by 95011613 on 2015/10/2.
 */
public class ScheduleAdapter extends BaseAdapter {
    private static final String TAG = "ScheduleAdapter";
    private Context mContext;
    private Schedule schedule_data;
    private String mUuid;
    private ArrayList<Schedule> mSchedules;

    private int[] mWeekBtn = {R.id.btn_week_mon,R.id.btn_week_tue,R.id.btn_week_wed,
            R.id.btn_week_thu,R.id.btn_week_fri,R.id.btn_week_sat,R.id.btn_week_sun};

    private int[] mode_on_resId={R.drawable.icon_cooler_on,R.drawable.icon_dehimidity_on,
            R.drawable.icon_wind_on,R.drawable.icon_heater_on};
    private int[] mode_off_resId={R.drawable.icon_cooler_off,R.drawable.icon_dehimidity_off,
            R.drawable.icon_wind_off,R.drawable.icon_heater_off};

    private class ViewHolder {
        LinearLayout schedule_item_bg;
        LinearLayout mode_bg;
        ImageView btn_mode_icon;
        TextView tv_mode;
        TextView tv_temp;
        ImageView btn_switch_off;
        TextView tv_start_time;
        TextView tv_end_time;
        Button[] btn_week=new Button[mWeekBtn.length];
        ImageView btn_menu;
    }

    public ScheduleAdapter(Context context, String uuid,ArrayList<Schedule> schedules){
        mContext=context;
        mUuid=uuid;
        mSchedules=schedules;
    }

    public void setSchedule(ArrayList<Schedule> schedules){
        mSchedules=schedules;
    }
    @Override
    public int getCount() {
        if(mSchedules !=null) {
            return mSchedules.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(mSchedules != null)
            return mSchedules.get(position);

        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (mSchedules == null || mSchedules.size() <= 0) return null;
        Log.d(TAG, "getView position="+position);

        final ViewHolder holder;
        schedule_data = mSchedules.get(position);
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.schedule_item, null);
            holder = new ViewHolder();
            holder.schedule_item_bg=(LinearLayout) convertView.findViewById(R.id.ll_schedule_item);
            holder.mode_bg=(LinearLayout) convertView.findViewById(R.id.ll_mode);
            holder.btn_mode_icon = (ImageView) convertView.findViewById(R.id.mode_icon);
            holder.tv_mode = (TextView) convertView.findViewById(R.id.txt_mode);
            holder.tv_temp = (TextView) convertView.findViewById(R.id.txt_schedule_temp);
            holder.btn_switch_off = (ImageView) convertView.findViewById(R.id.btn_switch_off);
            holder.tv_start_time=(TextView) convertView.findViewById(R.id.txt_schedule_start);
            holder.tv_end_time=(TextView) convertView.findViewById(R.id.txt_schedule_end);
            for(int i=0;i<mWeekBtn.length;i++){
                holder.btn_week[i]=(Button)convertView.findViewById(mWeekBtn[i]);
            }

            holder.btn_menu=(ImageView) convertView.findViewById(R.id.schedule_btn_menu);

            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder)convertView.getTag();

        holder.btn_menu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //  schedule_data=mSchedules[position];
                ((ScheduleListActivity) mContext).OpenScheduleMenu(position, holder.btn_menu);
            }
        });

        switch (schedule_data.getType()){
            case 0:
            case ACDef.FUN_MODE_COOL:
                holder.btn_mode_icon.setBackground(mContext.getResources().getDrawable(R.drawable.icon_cooler_off));
                holder.tv_mode.setText(mContext.getResources().getString(R.string.cooler));
                holder.tv_temp.setVisibility(View.VISIBLE);
                break;
            case ACDef.FUN_MODE_DRY:
                holder.btn_mode_icon.setBackground(mContext.getResources().getDrawable(R.drawable.icon_dehimidity_off));
                holder.tv_mode.setText(mContext.getResources().getString(R.string.dehumidifier));
                holder.tv_temp.setVisibility(View.INVISIBLE);
                break;
            case ACDef.FUN_MODE_FAN:
                holder.btn_mode_icon.setBackground(mContext.getResources().getDrawable(R.drawable.icon_wind_off));
                holder.tv_mode.setText(mContext.getResources().getString(R.string.fan));
                holder.tv_temp.setVisibility(View.INVISIBLE);
                break;
            case ACDef.FUN_MODE_HEAT:
                holder.btn_mode_icon.setBackground(mContext.getResources().getDrawable(R.drawable.icon_heater_off));
                holder.tv_mode.setText(mContext.getResources().getString(R.string.heater));
                holder.tv_temp.setVisibility(View.VISIBLE);
                break;
        }

        holder.tv_temp.setText(String.valueOf((int)Utils.getTemp(mContext,schedule_data.getValue()) + "°"));
        holder.tv_start_time.setText(schedule_data.getStartTime());

        String[] splitTime1 = schedule_data.getStartTime().split(":");
        String[] splitTime2 = schedule_data.getEndTime().split(":");
        try {
            String splitTime3 = splitTime2[0].subSequence(splitTime2[0].length() - 2, splitTime2[0].length()).toString();
            int time1 = Integer.valueOf(splitTime1[0]) * 100 + Integer.valueOf(splitTime1[1]);
            int time2 = Integer.valueOf(splitTime3) * 100 + Integer.valueOf(splitTime2[1]);
            if (time1 > time2) {
                holder.tv_end_time.setText(mContext.getResources().getString(R.string.next_day) + " "
                        + splitTime3 + ":" + splitTime2[1]);
            }
//        else if(time1 == time2)
//            Toast.makeText(mContext, mContext.getResources().getString(R.string.ir_start_end_timesame), Toast.LENGTH_SHORT).show();
            else
                holder.tv_end_time.setText(schedule_data.getEndTime());
        }catch(Exception e){}

        holder.btn_switch_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                schedule_data = mSchedules.get(position);
                if (schedule_data.getEnable() == true) {
                    UpdateLayout(holder, false);
                    ((ScheduleListActivity) mContext).SwitchOnOff(mUuid, schedule_data, false);
                } else {
                    UpdateLayout(holder, true);
                    ((ScheduleListActivity) mContext).SwitchOnOff(mUuid, schedule_data, true);
                }
            }

        });
        UpdateLayout(holder,schedule_data.getEnable());
        return convertView;
    }

    private void UpdateLayout(ViewHolder holder,boolean is_on){
        Resources res=mContext.getResources();
        if(is_on == true) {
            holder.schedule_item_bg.setBackgroundColor(res.getColor(R.color.color_schedule_item_on));
            holder.tv_mode.setTextColor(res.getColor(R.color.color_white));
            holder.tv_temp.setTextColor(res.getColor(R.color.color_white));
            if(schedule_data.getType() <= 0)
                holder.btn_mode_icon.setBackground(res.getDrawable(mode_on_resId[0]));
            else
                holder.btn_mode_icon.setBackground(res.getDrawable(mode_on_resId[schedule_data.getType() - 1]));
            holder.tv_start_time.setTextColor(res.getColor(R.color.color_blue));
            holder.tv_end_time.setTextColor(res.getColor(R.color.color_blue));
            holder.btn_switch_off.setBackground(res.getDrawable(R.drawable.switch_on));
            holder.tv_temp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_temp,0,0,0);
        }else {
            holder.schedule_item_bg.setBackgroundColor(res.getColor(R.color.color_schedule_item_off));
            holder.tv_mode.setTextColor(res.getColor(R.color.color_schedule_txt_off));
            holder.tv_temp.setTextColor(res.getColor(R.color.color_schedule_txt_off));
            if(schedule_data.getType() <= 0)
                holder.btn_mode_icon.setBackground(res.getDrawable(mode_off_resId[0]));
            else
                holder.btn_mode_icon.setBackground(res.getDrawable(mode_off_resId[schedule_data.getType()-1]));
            holder.tv_start_time.setTextColor(res.getColor(R.color.color_schedule_txt_off));
            holder.tv_end_time.setTextColor(res.getColor(R.color.color_schedule_txt_off));
            holder.btn_switch_off.setBackground(res.getDrawable(R.drawable.switch_off));
            holder.tv_temp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_temp_off, 0, 0, 0);
        }

        boolean bflag=false;
        for(int i=0;i< holder.btn_week.length;i++) {
            bflag=false;
            int[] weeks=schedule_data.getWeek();
            if(weeks != null) {
                for (int j = 0; j < weeks.length; j++) {
                    if ((i + 1) == weeks[j]) {
                        if (is_on == true) {
                            holder.btn_week[i].setBackground(res.getDrawable(R.drawable.schedule_week_btn_on));
                            holder.btn_week[i].setTextColor(res.getColor(R.color.color_blue));
                        } else {
                            holder.btn_week[i].setBackground(res.getDrawable(R.drawable.schedule_week_btn_off));
                            holder.btn_week[i].setTextColor(res.getColor(R.color.color_schedule_txt_off));
                        }
                        bflag = true;
                        break;
                    }
                }
            }
            if(bflag == false){
                holder.btn_week[i].setBackgroundColor(res.getColor(android.R.color.transparent));
                holder.btn_week[i].setTextColor(res.getColor(R.color.color_schedule_txt_off));
            }
        }
    }

}
