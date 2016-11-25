package com.quantatw.roomhub.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.BulbData;
import com.quantatw.roomhub.utils.AssetDef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulbGroupAdapter extends BaseAdapter {
    private Context mContext;
    private List<BulbData> mList;
    private LayoutInflater mInflater;
    private Map<String,Integer> statusMap; //-1,0,1
    protected static int STATUS_DISABLE = -1;
    protected static int STATUS_UNCHECKED = 0;
    protected static int STATUS_CHECKED = 1;

    public BulbGroupAdapter(Context context, List<BulbData> list) {
        this.mInflater = LayoutInflater.from(context);
        mContext = context;
        mList = list;
        statusMap = new HashMap<String, Integer>();
        initDate();
    }
    public void setList(List<BulbData> list){
        mList = list;
    }
    private void initDate()
    {
        for(int i=0;i<mList.size();i++)
        {
            statusMap.put(mList.get(i).getAssetUuid(), STATUS_UNCHECKED);
        }
    }
    @Override
    public int getCount() {
        if(mList == null)
            return 0;
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
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

        ViewHolder holder;
        if (convertView == null) {
            holder=new ViewHolder();
            convertView = mInflater.inflate(R.layout.bulb_group_controller_list_item, null);
            holder.ll=(LinearLayout)convertView.findViewById(R.id.ll);
            holder.img = (ImageView)convertView.findViewById(R.id.img);
            holder.bulbname = (TextView)convertView.findViewById(R.id.item_tv);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.item_cb);
            convertView.setTag(holder);

        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        String uuid = mList.get(position).getAssetUuid();
        if(mList.get(position).getOnlineStatus() == AssetDef.ONLINE_STATUS_OFFLINE){
            statusMap.put(uuid,STATUS_DISABLE);
            holder.img.setImageResource(R.drawable.icon_warning);
        }else {
            if (mList.get(position).getPower() == 0) {
                holder.img.setImageResource(R.drawable.btn_blub_off);
            } else {
                holder.img.setImageResource(R.drawable.btn_blub_on);
            }
        }
        holder.bulbname.setText(mList.get(position).getName());
        holder.checkBox.setChecked(statusMap.get(uuid) == STATUS_CHECKED);
        if(holder.checkBox.isChecked()){
            holder.ll.setBackgroundColor(mContext.getResources().getColor(R.color.color_list_item_bg));
        }else {
            holder.ll.setBackgroundColor(Color.TRANSPARENT);
        }


        return convertView;
    }
    public Map<String,Integer> getStatusMap() {
        return statusMap;
    }

    public final class ViewHolder{
        public LinearLayout ll;
        public ImageView img;
        public TextView bulbname;
        public CheckBox checkBox;
    }
}
