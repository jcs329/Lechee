package com.quantatw.roomhub.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceController;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceManager;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;

import java.util.ArrayList;

import static com.quantatw.sls.api.DeviceTypeConvertApi.CATEGORY;

/**
 * Created by erin on 5/24/16.
 */
/* 20160524
* TODO: This Activity and AddEletricActivity could be combined together in the future
 */
public class AddHealthcareActivity extends AbstractRoomHubActivity {

    private HealthDeviceManager mHealthDeviceManager;

    private GridView mContentGridView;
    private ContentAdapter mContentAdapter;
    private ArrayList<ContentItem> mContentList;

    private boolean isSupport(int type) {
        ArrayList<Integer> typeList = mHealthDeviceManager.getSupportTypeNumbers();
        for(int val:typeList) {
            if(val == type)
                return true;
        }
        return false;
    }

    private class ContentItem {
        int category;
        int deviceType;
        int resTitle;
        int resDrawable;
        ContentItem(int category, int deviceType, int title, int drawable) {
            this.category = category;
            this.deviceType = deviceType;
            this.resTitle = title;
            this.resDrawable = drawable;
        }
    }

    private class ViewHolder {
        ImageView imageView;
        TextView titleTextView;
    }

    private class ContentAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<ContentItem> mList;

        ContentAdapter(Context context, ArrayList<ContentItem> list) {
            mContext = context;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.asset_grid_item, null);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView)convertView.findViewById(R.id.asset_image);
                viewHolder.titleTextView = (TextView)convertView.findViewById(R.id.asset_title);
                convertView.setTag(viewHolder);
            }
            else
                viewHolder = (ViewHolder)convertView.getTag();

            final ContentItem contentItem = (ContentItem)getItem(position);
            viewHolder.imageView.setBackgroundResource(contentItem.resDrawable);
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!isSupport(contentItem.deviceType)) {
                        Toast.makeText(mContext,R.string.coming_soon,Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(!Utils.isAllowToAddHealthcareDevice(mContext)) {
                        return;
                    }

                    final ContentItem contentItem = (ContentItem) getItem(position);
                    final int category = contentItem.category;
                    final int deviceType = contentItem.deviceType;
                    HealthDeviceController health_device=mHealthDeviceManager.getDeviceManager(deviceType);
                    health_device.startBLEPairing();
                }
            });
            viewHolder.titleTextView.setText(contentItem.resTitle);

            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_assets);

        mHealthDeviceManager = getHealthDeviceManager();

        View view = findViewById(R.id.electric_list);
        mContentGridView = (GridView)view.findViewById(R.id.assets_content);
        mContentList = obtainContentList();
        mContentAdapter = new ContentAdapter(this, mContentList);
        mContentGridView.setAdapter(mContentAdapter);

        /*
         * acquire and held wakelock here and release in onDestroy
         * no need to consider power consumption for temporary
          */
        Utils.acquireIRPairingWakeLock(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private ArrayList<ContentItem> obtainContentList() {
        ArrayList<Integer> typeList = mHealthDeviceManager.getSupportTypeNumbers();
        ArrayList<ContentItem> list = new ArrayList<>();

        for(int type: typeList) {
            HealthDeviceController healthDeviceController = mHealthDeviceManager.getDeviceManager(type);
            if(healthDeviceController != null) {
                list.add(
                        new ContentItem(
                                CATEGORY.HEALTH,
                                type,
                                healthDeviceController.getTitleStringResourceId(),
                                healthDeviceController.getDrawableResourceId()));
            }
        }

        // for temporary UI
        {
            list.add(
                    new ContentItem(
                            CATEGORY.HEALTH,
                            DeviceTypeConvertApi.TYPE_HEALTH.WEIGHT,
                            R.string.bathroom_scales,
                            R.drawable.btn_add_weight_disable));
        }

        return list;
    }

}
