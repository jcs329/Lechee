package com.quantatw.roomhub.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.ir.ApIRParingInfo;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;

import java.util.ArrayList;

/**
 * Created by erin on 2/1/16.
 */
public class IRModelListActivity extends AbstractRoomHubActivity implements AdapterView.OnItemClickListener {

    private Context mContext;
    private String mUuid;
    private String mAssetUuid;
    private int mAssetType;
    IRModelListAdapter irModelListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ir_setting_modellist);

        mContext = this;

        mUuid = getIntent().getStringExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID);
        mAssetUuid = getIntent().getStringExtra(IRSettingDataValues.KEY_ELECTRIC_UUID);
        mAssetType = getIntent().getIntExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, DeviceTypeConvertApi.TYPE_ROOMHUB.AC);
        String brandName = getIntent().getStringExtra(IRSettingDataValues.KEY_DATA_IR_BRAND_NAME);
        ArrayList<ApIRParingInfo> modelList = getIntent().getParcelableArrayListExtra(IRSettingDataValues.KEY_DATA_IR_PARING_INFO);

        TextView brandTextView = (TextView)findViewById(R.id.ir_brandname);
        if (mAssetType != DeviceTypeConvertApi.TYPE_ROOMHUB.FAN)
            brandTextView.setText(brandName+"\n"+getString(R.string.ir_search_model));
        else
            brandTextView.setText(brandName+"\n"+getString(R.string.ir_search_electric_model));

        ListView modelListView = (ListView)findViewById(R.id.ir_modellist);
        irModelListAdapter = new IRModelListAdapter(modelList);
        modelListView.setAdapter(irModelListAdapter);
        modelListView.setOnItemClickListener(this);

        final View ir_learning_view = findViewById(R.id.ir_learning_layout);

        if(mAssetType == DeviceTypeConvertApi.TYPE_ROOMHUB.AC) {
            ir_learning_view.setVisibility(View.VISIBLE);
        }else {
            ir_learning_view.setVisibility(View.GONE);
        }


        ImageView ir_learning_cancel = (ImageView)findViewById(R.id.ir_learning_cancel);
        ir_learning_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ir_learning_view.setVisibility(View.GONE);
            }
        });

        /* IR Learning */
        Button ir_learning = (Button)findViewById(R.id.ir_learning_btn);
        ir_learning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, IRLearningActivity.class);
                intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, mUuid);
                intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, mAssetType);
                intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_UUID, mAssetUuid);
                startActivityForResult(intent, IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ArrayList<ApIRParingInfo> list = new ArrayList<>();
        list.add((ApIRParingInfo)irModelListAdapter.getItem(position));
        launchIRParingActivity(list);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID,mUuid);
        intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE,mAssetType);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        setResult(resultCode,data);
//        finish();
        if(resultCode == RESULT_OK && requestCode == IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE) {
            setResult(resultCode,data);
            finish();
        }
    }

    private void launchIRParingActivity(ArrayList<ApIRParingInfo> paringInfo) {
        Intent intent = new Intent(mContext, IRPairingActivity.class);
        intent.putExtra(IRSettingDataValues.KEY_DATA_IR_SETTING_MODE, IRSettingDataValues.IR_SETTING_MODE_GET_LIST);
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, mUuid);
        intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_UUID,mAssetUuid);
        intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE,mAssetType);
        intent.putExtra(IRSettingDataValues.KEY_DATA_IR_PARING_INFO, paringInfo);
        startActivityForResult(intent, IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE);
    }

    private class IRModelListAdapter extends BaseAdapter {
        private ArrayList<ApIRParingInfo> mList;

        private class ViewHolder {
            TextView modelText;
        }

        public IRModelListAdapter(ArrayList<ApIRParingInfo> list) {
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.ir_brand_list_item, parent, false);
                holder = new ViewHolder();
                holder.modelText = (TextView)convertView.findViewById(R.id.brand);
                convertView.setTag(holder);
            }
            else
                holder = (ViewHolder)convertView.getTag();

            final ApIRParingInfo item = (ApIRParingInfo)getItem(position);
            holder.modelText.setText(item.getRemoteModelNum());

            // make fan type model list show the model name, not the remoter name. if there is no model name
            // show the remoter name
            if (mAssetType != DeviceTypeConvertApi.TYPE_ROOMHUB.FAN || item.getDevModelNumber().equals("")) {
                holder.modelText.setText(item.getRemoteModelNum());
            } else {
                holder.modelText.setText(item.getDevModelNumber());
            }

            return convertView;
        }
    };

}
