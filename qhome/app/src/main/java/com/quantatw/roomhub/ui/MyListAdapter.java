package com.quantatw.roomhub.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.sls.device.FriendData;

import java.util.ArrayList;

/**
 * Created by 95011613 on 2015/10/2.
 */
public class MyListAdapter extends BaseAdapter {
    private static final String TAG = "MyListAdapter";
    private Context mContext;
    private ArrayList<FriendData> mFriendDataList;
    private FriendData mFriendData;

    private class ViewHolder {
        TextView tv_nick_name;
        Button btn_rename;
        ImageView btn_del;
    }

    public MyListAdapter(Context context, ArrayList<FriendData> friend_list){
        mContext=context;
        mFriendDataList=friend_list;
    }

    @Override
    public int getCount() {
        return mFriendDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFriendDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (mFriendDataList == null || mFriendDataList.size() <= 0) return null;
        Log.d(TAG, "getView position="+position);

        final ViewHolder holder;
        mFriendData = mFriendDataList.get(position);
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.my_list_item, null);
            holder = new ViewHolder();
            holder.tv_nick_name = (TextView) convertView.findViewById(R.id.txt_people_nick_name);
            holder.btn_rename = (Button) convertView.findViewById(R.id.btn_people_rename);
            holder.btn_del = (ImageView) convertView.findViewById(R.id.btn_del);

            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder)convertView.getTag();


        holder.btn_rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendData friend_data=mFriendDataList.get(position);
                ((MyListActivity) mContext).OpenEditPeople(MyListActivity.CMD.EDIT, friend_data);
            }
        });

        holder.btn_del.setVisibility(View.VISIBLE);
        holder.btn_del.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FriendData friend_data=mFriendDataList.get(position);
                ((MyListActivity) mContext).DeletePeople( friend_data);
            }
        });

        Log.d(TAG, "getView nick_name=" + mFriendData.getNickName());
        holder.tv_nick_name.setText(mFriendData.getNickName());

        return convertView;
    }
}
