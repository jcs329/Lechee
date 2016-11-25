package com.quantatw.roomhub.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;

import java.util.ArrayList;

/**
 * Created by 95011613 on 2015/10/2.
 */
public class ElectricAdapter extends BaseAdapter {
    private static final String TAG = "ElectricAdapter";
    private Context mContext;

    private ElectricMgrActivity.ContentList mDataList;
    private int mDeviceCategory;
    private static LayoutInflater inflater=null;

    private final int BUTTON_PAIR = 0;
    private final int BUTTON_DEL  = 1;
    private final int BUTTON_RENAME  = 2;

    private class ViewHolder {
        ImageView electric_icon;
        TextView tv_models;
        TextView dev_name;
        TextView account;
        View btn_pair,btn_del,btn_rename;

        public ButtonViewHolder getButtonViewHolder(View button,int type) {
            ButtonViewHolder buttonViewHolder = new ButtonViewHolder();
            buttonViewHolder.parentHolder = this;
            buttonViewHolder.icon = (ImageView) button.findViewById(R.id.notify_btn_icon);
            buttonViewHolder.label = (TextView) button.findViewById(R.id.notify_btn_label);
            buttonViewHolder.type=type;
            return buttonViewHolder;
        }

        public void disableButtonView(View button) {
            ButtonViewHolder buttonViewHolder = (ButtonViewHolder) button.getTag();
            buttonViewHolder.label.setTextColor(mContext.getResources().getColor(R.color.color_pinkish_grey));
            button.setEnabled(false);
            buttonViewHolder.icon.setClickable(false);
            buttonViewHolder.label.setClickable(false);
            buttonViewHolder.icon.setOnClickListener(null);
            buttonViewHolder.label.setOnClickListener(null);
        }

        public void enableButtonView(View button, int index) {
            ButtonViewHolder buttonViewHolder = (ButtonViewHolder) button.getTag();
            buttonViewHolder.label.setTextColor(mContext.getResources().getColor(R.color.color_white));
            button.setEnabled(true);
            buttonViewHolder.icon.setClickable(true);
            buttonViewHolder.label.setClickable(true);
            buttonViewHolder.icon.setOnClickListener(onClickListener);
            buttonViewHolder.label.setOnClickListener(onClickListener);
            buttonViewHolder.index = index;
        }

        public void setIndex(View button,int index){
            ButtonViewHolder buttonViewHolder = (ButtonViewHolder) button.getTag();
            buttonViewHolder.index=index;
        }

        private View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parent = (View) v.getParent();
                ButtonViewHolder buttonViewHolder = (ButtonViewHolder) parent.getTag();

                if(buttonViewHolder.type == BUTTON_PAIR){
                    ((ElectricMgrActivity) mContext).RePairing(buttonViewHolder.index);
                }else if(buttonViewHolder.type == BUTTON_RENAME) {
                    ((ElectricMgrActivity) mContext).Rename(buttonViewHolder.index);
                }else{
                    ((ElectricMgrActivity) mContext).DeleteElectric(buttonViewHolder.index);
                }
            }
        };
    }

    private class ButtonViewHolder {
        ViewHolder parentHolder;
        ImageView icon;
        TextView label;
        int index;
        int type;
    }

    public ElectricAdapter(Context context, int category, ElectricMgrActivity.ContentList data_list){
        mContext=context;
        mDeviceCategory = category;
        mDataList=data_list;
    }

    @Override
    public int getCount() {
        return mDataList.getList().size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (mDataList == null || mDataList.getList().size() <= 0) return null;
        Log.d(TAG, "getView position=" + position);

        final ViewHolder holder;
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.electric_item, null);
            holder = new ViewHolder();

            holder.electric_icon = (ImageView) convertView.findViewById(R.id.electric_icon);
            holder.tv_models = (TextView) convertView.findViewById(R.id.txt_electric_models);
            holder.dev_name = (TextView) convertView.findViewById(R.id.txt_dev_name);
            holder.account = (TextView) convertView.findViewById(R.id.txt_account);

            holder.btn_pair = convertView.findViewById(R.id.btn_pair).findViewById(R.id.notify_btn_layout);
            ButtonViewHolder buttonViewHolder1 = holder.getButtonViewHolder(holder.btn_pair,BUTTON_PAIR);
            buttonViewHolder1.icon.setBackground(mContext.getResources().getDrawable(R.drawable.icon_repairing));
            if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.ROOMHUB)
                buttonViewHolder1.label.setText(mContext.getResources().getString(R.string.ir_repairing));
            else
                buttonViewHolder1.label.setText(mContext.getResources().getString(R.string.reset_default_user));
            holder.btn_pair.setTag(buttonViewHolder1);
            holder.enableButtonView(holder.btn_pair, position);

            holder.btn_del = convertView.findViewById(R.id.btn_del).findViewById(R.id.notify_btn_layout);
            ButtonViewHolder buttonViewHolder2 = holder.getButtonViewHolder(holder.btn_del,BUTTON_DEL);
            buttonViewHolder2.icon.setBackground(mContext.getResources().getDrawable(R.drawable.btn_deletemsg));
            buttonViewHolder2.label.setText(mContext.getResources().getString(R.string.del_electric));
            holder.btn_del.setTag(buttonViewHolder2);
            holder.enableButtonView(holder.btn_del, position);

            holder.btn_rename = convertView.findViewById(R.id.btn_rename).findViewById(R.id.notify_btn_layout);
            ButtonViewHolder buttonViewHolder3 = holder.getButtonViewHolder(holder.btn_rename,BUTTON_RENAME);
            buttonViewHolder3.icon.setBackground(mContext.getResources().getDrawable(R.drawable.btn_rename));
            buttonViewHolder3.label.setText(mContext.getResources().getString(R.string.rename));
            holder.btn_rename.setTag(buttonViewHolder3);
            holder.enableButtonView(holder.btn_rename, position);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
            holder.setIndex(holder.btn_pair, position);
            holder.setIndex(holder.btn_del, position);
            holder.setIndex(holder.btn_rename, position);
        }
        holder.btn_del.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((ElectricMgrActivity) mContext).DeleteElectric(position);
            }
        });

        Object item = mDataList.getItem(position);
        int drawableResource = ((ElectricMgrActivity)mContext).getDrawbleResourceByType(item);
        if(drawableResource > 0)
            holder.electric_icon.setBackground(mContext.getResources().getDrawable(drawableResource));

        int connection_type=((ElectricMgrActivity)mContext).getConnectionType(item);
        if((connection_type == AssetDef.CONNECTION_TYPE_BT) || (connection_type < 0))
            holder.btn_pair.setVisibility(View.INVISIBLE);
        else
            holder.btn_pair.setVisibility(View.VISIBLE);

        String str_models=((ElectricMgrActivity)mContext).getModelsByType(item);
        String defaultUser =  ((ElectricMgrActivity)mContext).getDefaultUser(item);
        if(mDeviceCategory != DeviceTypeConvertApi.CATEGORY.ROOMHUB) {
            holder.btn_pair.setVisibility(View.VISIBLE);
            holder.dev_name.setVisibility(View.VISIBLE);
            holder.dev_name.setText(((HealthData) item).getDeviceName());
            holder.account.setVisibility(View.VISIBLE);
            holder.btn_rename.setVisibility(View.VISIBLE);
            holder.tv_models.setText(mContext.getResources().getString(R.string.model) + ":" + str_models);
            holder.account.setText(mContext.getResources().getString(R.string.bpm_default_name)+":"+defaultUser);
        }
        else {
            holder.btn_rename.clearAnimation();
            holder.dev_name.setVisibility(View.GONE);
            holder.account.setVisibility(View.GONE);
            holder.btn_rename.setVisibility(View.GONE);

            holder.tv_models.setText(str_models);
        }

        return convertView;
    }
}
