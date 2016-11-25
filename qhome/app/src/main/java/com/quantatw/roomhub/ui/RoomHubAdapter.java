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
import com.quantatw.roomhub.manager.asset.manager.AssetInfoData;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetData;
import com.quantatw.roomhub.manager.asset.manager.PMData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.AQIApi;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.key.ErrorKey;

import java.util.ArrayList;

/**
 * Created by 95011613 on 2015/10/2.
 */
public class RoomHubAdapter extends BaseAdapter {
    private static final String TAG = "RoomHubAdapter";
    private Context mContext;
    private ArrayList<RoomHubData> mDataList;
    private static LayoutInflater inflater=null;
    private RoomHubData data;

    private static final int AIR_QUALITY_GOOD   =   0;
    private static final int AIR_QUALITY_NORMAL =   1;
    private static final int AIR_QUALITY_DANGER =   2;

    private class ViewHolder {
        TextView tv_devname;
        TextView tv_temp;
        TextView tv_hum;
        ImageView btn_menu;
        ImageView btn_funmode_icon;

        LinearLayout ll_electric;
        LinearLayout ll_add_electric;
        LinearLayout ll_msg;

        LinearLayout ll_electric_list;

        Button btn_add_electric;
        TextView txt_msg;

        public ElectricBtnViewHolder getElectricBtnViewHolder(View electric_item_view) {
            ElectricBtnViewHolder buttonViewHolder = new ElectricBtnViewHolder();
            buttonViewHolder.parentHolder = this;
            buttonViewHolder.icon = (ImageView) electric_item_view.findViewById(R.id.btn_electric);
            buttonViewHolder.pmStatus = (ImageView) electric_item_view.findViewById(R.id.pm_status);
            buttonViewHolder.ConnStatus = (ImageView) electric_item_view.findViewById(R.id.conn_status);
            buttonViewHolder.pmStatusTxt = (TextView) electric_item_view.findViewById(R.id.pm_status_text);
            buttonViewHolder.label = (TextView) electric_item_view.findViewById(R.id.txt_electric);
            electric_item_view.setEnabled(true);
            buttonViewHolder.icon.setClickable(true);
            buttonViewHolder.label.setClickable(true);
            buttonViewHolder.icon.setOnClickListener(onClickListener);
            buttonViewHolder.label.setOnClickListener(onClickListener);
            return buttonViewHolder;
        }

        public void updateElectricBtnView(View button, int pos,int index,int type) {
            ElectricBtnViewHolder buttonViewHolder = (ElectricBtnViewHolder) button.getTag();
            buttonViewHolder.index=index;
            buttonViewHolder.type=type;
            buttonViewHolder.pos=pos;
        }

        private View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parent = (View) v.getParent();
                ElectricBtnViewHolder buttonViewHolder = (ElectricBtnViewHolder) parent.getTag();
                if(buttonViewHolder.ConnStatus.getVisibility() == View.INVISIBLE)
                    ((RoomHubMainPage) mContext).LaunchElectricActivity(buttonViewHolder.pos,buttonViewHolder.type ,null);
                else
                    Toast.makeText(mContext, R.string.appliance_offline, Toast.LENGTH_SHORT).show();
            }
        };
    }

    public class ElectricBtnViewHolder {
        ViewHolder parentHolder;
        ImageView icon;
        ImageView pmStatus;
        ImageView ConnStatus;
        TextView pmStatusTxt;
        TextView label;
        int index;
        int type;
        int pos; //roomhub devices list
    }

    public RoomHubAdapter(Context context, ArrayList<RoomHubData> data_list){
        mContext=context;
        mDataList=data_list;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (mDataList == null || mDataList.size() <= 0) return null;
        Log.d(TAG, "getView position=" + position);
        final ViewHolder holder;
        data = mDataList.get(position);
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.room_hub_item, null);
            holder = new ViewHolder();

            holder.tv_devname = (TextView) convertView.findViewById(R.id.txt_devname);
            holder.tv_temp = (TextView) convertView.findViewById(R.id.txt_sensor_temp);
            holder.tv_hum = (TextView) convertView.findViewById(R.id.txt_sensor_hum);
            holder.btn_menu=(ImageView) convertView.findViewById(R.id.btn_menu);
            //holder.btn_funmode_icon=(ImageView)convertView.findViewById(R.id.mode_icon);
            holder.btn_funmode_icon=(ImageView)convertView.findViewById(R.id.mode_icon2);

            holder.ll_add_electric = (LinearLayout) convertView.findViewById(R.id.ll_add_electric);
            holder.btn_add_electric = (Button) convertView.findViewById(R.id.btn_add_electric);
            holder.ll_msg = (LinearLayout) convertView.findViewById(R.id.ll_msg);
            holder.txt_msg = (TextView) convertView.findViewById(R.id.txt_msg);

            holder.ll_electric = (LinearLayout) convertView.findViewById(R.id.ll_electric);
            holder.ll_electric_list = (LinearLayout) convertView.findViewById(R.id.ll_electric_list);

            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder)convertView.getTag();

        holder.btn_menu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((RoomHubMainPage) mContext).OpenRoomHubListMenu(position, holder.btn_menu);
            }
        });

        Log.d(TAG, "getView dev_name=" + data.getName() + " uuid=" + data.getUuid());
        holder.tv_devname.setText(data.getName());


        Resources res=mContext.getResources();
        boolean is_show_msg=false;

        if(data.IsUpgrade() == true) {
            Log.d(TAG, "getView is upgrade uuid="+data.getUuid());
            holder.btn_funmode_icon.setBackground(res.getDrawable(R.drawable.icon_renew));
            holder.ll_add_electric.setVisibility(View.GONE);
            holder.ll_electric.setVisibility(View.GONE);
            holder.ll_msg.setVisibility(View.VISIBLE);
            holder.txt_msg.setText(R.string.device_upgrade);
            is_show_msg=true;
        }else if(data.IsOnLine() == false) {
            Log.d(TAG, "getView is offline uuid="+data.getUuid());
            holder.tv_temp.setText("--°");
            holder.tv_hum.setText("--%");

            holder.btn_funmode_icon.setBackground(res.getDrawable(R.drawable.icon_disconnected));

            holder.ll_add_electric.setVisibility(View.GONE);
            holder.ll_electric.setVisibility(View.GONE);
            holder.ll_msg.setVisibility(View.VISIBLE);
            holder.txt_msg.setText(R.string.device_offline);
            is_show_msg=true;
        }else {
            Log.d(TAG, "getView uuid=" + data.getUuid());
            double sensor_temp=data.getSensorTemp();
            double sensor_hum=data.getSensorHumidity();
            if(sensor_temp == ErrorKey.SENSOR_TEMPERATURE_INVALID)
                holder.tv_temp.setText("--°");
            else
                holder.tv_temp.setText(String.valueOf((int) Utils.getTemp(mContext,sensor_temp)) + "°");

            if(sensor_hum == ErrorKey.SENSOR_HUMIDITY_INVALID)
                holder.tv_hum.setText("--%");
            else
                holder.tv_hum.setText(String.valueOf((int) sensor_hum) + "%");

            holder.btn_funmode_icon.setBackground(null);
            holder.ll_msg.setVisibility(View.GONE);
        }

        if(!is_show_msg) {
            UpdateElectricBtnList(convertView,data,position);
        }

        return convertView;
    }

    private View CreateElectricBtn(View v,int position,int idx,AssetInfoData data){
        Log.d(TAG, "CreateElectricBtn electric uuid=" + data.getAssetUuid() + " type=" + data.getAssetType() + " position=" + position);
        ViewHolder holder= (ViewHolder) v.getTag();

        LinearLayout electric_btn_lst=(LinearLayout) v.findViewById(R.id.ll_electric_list);
        View item_view=electric_btn_lst.getChildAt(idx);
        View each_item_view;
        if(item_view == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View electric_item_view = inflater.inflate(R.layout.room_hub_electric_item, null);

            if(electric_item_view != null) {
                item_view = electric_item_view.findViewById(R.id.ll_electric_item);
                each_item_view = electric_item_view.findViewById(R.id.electric_device);
                //if(idx != 0)
                    //item_view.setPadding(20, 0, 0, 0);
                Log.d(TAG, "CreateElectricBtn electric getElectricBtnViewHolder");
                //btnViewHolder = holder.getElectricBtnViewHolder(item_view, position,idx, data.getType());
                ElectricBtnViewHolder btnViewHolder = holder.getElectricBtnViewHolder(item_view);

                item_view.setTag(btnViewHolder);
                each_item_view.setTag(btnViewHolder);

                electric_btn_lst.addView(electric_item_view);
            }
        }

        holder.updateElectricBtnView(item_view,position,idx,data.getAssetType());

        return item_view;
    }

    public void UpdateElectricBtnList(View v, final RoomHubData data,int position){
        LinearLayout ll_electric = (LinearLayout) v.findViewById(R.id.ll_electric);
        LinearLayout ll_add_electric = (LinearLayout) v.findViewById(R.id.ll_add_electric);

        ArrayList<AssetInfoData> asset_list = data.getAssetListNoSameType();
        int electric_cnt=asset_list.size();
        Log.d(TAG, "UpdateElectricBtnItem electric uuid=" + data.getUuid()+" position="+position+" electric_cnt="+electric_cnt);
        if (electric_cnt > 0) {

            ll_electric.setVisibility(View.VISIBLE);
            ll_add_electric.setVisibility(View.GONE);
            LinearLayout electric_btn_lst=(LinearLayout) v.findViewById(R.id.ll_electric_list);

            int child_cnt=electric_btn_lst.getChildCount();

            if(child_cnt > electric_cnt) {
                electric_btn_lst.removeViews(electric_cnt,(child_cnt-electric_cnt));
            }

            for (int i = 0; i < electric_cnt; i++) {
                BaseAssetData asset_data = (BaseAssetData)asset_list.get(i);
                Log.d(TAG, "UpdateElectricBtnItem asset_type="+asset_data.getAssetType()+" electric uuid=" + asset_data.getAssetUuid()+" online_status="+asset_data.getOnlineStatus());
                View item_view = CreateElectricBtn(v, position,i,asset_data);

                if (item_view != null) {
                    ElectricBtnViewHolder btnViewHolder = (ElectricBtnViewHolder) item_view.getTag();
                    btnViewHolder.ConnStatus.setBackground(null);
                    btnViewHolder.pmStatus.setVisibility(View.INVISIBLE);
                    btnViewHolder.pmStatusTxt.setVisibility(View.INVISIBLE);

                    btnViewHolder.label.setText(asset_data.getAssetName());
                    btnViewHolder.icon.setBackground(mContext.getResources().getDrawable(asset_data.getAssetIcon()));
                    if(asset_data.getAssetType() != DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) {
                        if (asset_data.getOnlineStatus() == AssetDef.ONLINE_STATUS_OFFLINE) {
                            btnViewHolder.ConnStatus.setVisibility(View.VISIBLE);
                            btnViewHolder.ConnStatus.setBackground(mContext.getResources().getDrawable(R.drawable.icon_bt_status_off));
                        } else {
                            btnViewHolder.ConnStatus.setVisibility(View.INVISIBLE);
                        }
                    }else{
                        if (!data.isAnyBulbOnline()) {
                            btnViewHolder.ConnStatus.setVisibility(View.VISIBLE);
                            btnViewHolder.ConnStatus.setBackground(mContext.getResources().getDrawable(R.drawable.icon_bt_status_off));
                        } else {
                            btnViewHolder.ConnStatus.setVisibility(View.INVISIBLE);
                        }
                    }

                    if(asset_data.getAssetType() == DeviceTypeConvertApi.TYPE_ROOMHUB.PM25){
                        if(asset_data.getOnlineStatus() == AssetDef.ONLINE_STATUS_OFFLINE){
                            btnViewHolder.pmStatus.setVisibility(View.INVISIBLE);
                            btnViewHolder.pmStatusTxt.setVisibility(View.INVISIBLE);
                        }else {
                            btnViewHolder.pmStatus.setVisibility(View.VISIBLE);
                            btnViewHolder.pmStatusTxt.setVisibility(View.VISIBLE);
                            PMData pm_data=(PMData)asset_data;
                            //int pmAirQuality = ((RoomHubMainPage) mContext).getPMStatus(position,RoomHubDef.ELECTRIC_PARTICULATE_MATTER ,null);
                            Log.d(TAG,"getView pm25 value="+pm_data.getValue());
                            AQIApi.AQI_CATEGORY catetory=AQIApi.getAQICategoryByPM25Value(pm_data.getValue());

                            if(AQIApi.AQI_CATEGORY.GOOD == catetory) {
                                btnViewHolder.pmStatus.setBackground(mContext.getResources().getDrawable(R.drawable.lable_status_good));
                                btnViewHolder.pmStatusTxt.setText(R.string.air_quality_good);
                            }
                            else if(AQIApi.AQI_CATEGORY.NORMAL == catetory) {
                                btnViewHolder.pmStatus.setBackground(mContext.getResources().getDrawable(R.drawable.lable_status_normal));
                                btnViewHolder.pmStatusTxt.setText(R.string.air_quality_normal);
                            }
                            else if(AQIApi.AQI_CATEGORY.DANGER == catetory) {
                                btnViewHolder.pmStatus.setBackground(mContext.getResources().getDrawable(R.drawable.lable_status_bad));
                                btnViewHolder.pmStatusTxt.setText(R.string.air_quality_danger);
                            }
                        }
                    }
                }
            }
        }else {
            Button btn_add_electric = (Button) v.findViewById(R.id.btn_add_electric);
            ll_electric.setVisibility(View.GONE);

            if (data.IsOwner()) {
                ll_add_electric.setVisibility(View.VISIBLE);

                btn_add_electric.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((RoomHubMainPage) mContext).LaunchAddElectric(0, data);
                    }
                });
            } else
                ll_add_electric.setVisibility(View.INVISIBLE);
        }
    }
}