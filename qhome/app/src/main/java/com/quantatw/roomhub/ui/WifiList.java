package com.quantatw.roomhub.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.ble.RoomHubBleDevice;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.GlobalDef;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by 95010915 on 2015/9/24.
 */
public class WifiList extends AbstractRoomHubActivity {

    private int mType = GlobalDef.TYPE_DEFAULT;
    private String mCurUuid;

    private ListView mWifiList;
    private ImageView mImgRefresh;

    private WifiManager mWifiManager;
    private ArrayList<String> mList; // ssid:level:type-psktype
    private List<ScanResult> mScanList;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                handleScanResult();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_wifi);
        Intent intent = getIntent();
        if(intent!=null){
            mType = intent.getIntExtra(GlobalDef.USE_TYPE,GlobalDef.TYPE_DEFAULT);
            mCurUuid = intent.getStringExtra(RoomHubManager.KEY_UUID);
        }

        if(mType == GlobalDef.TYPE_AP_TRANSFER){
            setTitle(R.string.change_wifi_now);
        }else{
            setTitle(R.string.wifi_list_title);
        }

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setLayoutParams(setMargin((int) getResources().getDimension(R.dimen.main_page_margin_bottom_reg_left),
                0,
                (int) getResources().getDimension(R.dimen.wifi_title_margin_top),
                (int) getResources().getDimension(R.dimen.wifi_title_margin_bottom),
                (LinearLayout.LayoutParams) txtTitle.getLayoutParams()));
        if(mType == GlobalDef.TYPE_AP_TRANSFER) {
            TextView subTitle = (TextView)findViewById(R.id.txtSubTitle);
            subTitle.setVisibility(View.VISIBLE);
            subTitle.setLayoutParams(setMargin((int) getResources().getDimension(R.dimen.main_page_margin_bottom_reg_left),
                    0,
                    (int) getResources().getDimension(R.dimen.wifi_title_margin_top),
                    0,
                    (LinearLayout.LayoutParams) subTitle.getLayoutParams()));
            txtTitle.setLayoutParams(setMargin((int) getResources().getDimension(R.dimen.main_page_margin_bottom_reg_left),
                    0,
                    0,
                    (int) getResources().getDimension(R.dimen.wifi_title_margin_bottom),
                    (LinearLayout.LayoutParams) txtTitle.getLayoutParams()));
            ArrayList<RoomHubBleDevice> list = intent.getParcelableArrayListExtra(GlobalDef.AP_TRANSFER_LIST);
            if(list != null && list.size()>0) {
                StringBuilder sb = new StringBuilder();
                for(Iterator iterator=list.iterator();iterator.hasNext();) {
                    RoomHubBleDevice roomHubBleDevice = (RoomHubBleDevice)iterator.next();
                    sb.append(roomHubBleDevice.getRoomHubName());
                    if(iterator.hasNext())
                        sb.append("/");
                }
                txtTitle.setText(sb.toString());
                if(Build.VERSION.SDK_INT >= 23)
                    txtTitle.setTextColor(getColor(R.color.color_white));
                else
                    txtTitle.setTextColor(getResources().getColor(R.color.color_white));
            }
        }

        TextView txtSearch = (TextView) findViewById(R.id.txtSearch);
        //txtSearch.setTextSize(getResources().getDimension(R.dimen.main_page_text_title_desc_size));
        txtSearch.setLayoutParams(setMargin((int) getResources().getDimension(R.dimen.main_page_margin_bottom_reg_left),
                0,
                0,
                0,
                (RelativeLayout.LayoutParams) txtSearch.getLayoutParams()));
        if(mType == GlobalDef.TYPE_AP_TRANSFER) {
            txtSearch.setText(R.string.change_wifi_subtitle);
            if(Build.VERSION.SDK_INT >= 23)
                txtSearch.setTextColor(getColor(R.color.btn_color));
            else
                txtSearch.setTextColor(getResources().getColor(R.color.btn_color));
        }

        mWifiList = (ListView) findViewById(R.id.lstWifi);
        /*
        mWifiList.setLayoutParams(setMargin((int) getResources().getDimension(R.dimen.main_page_margin_bottom_reg_left),
                0,
                0,
                0,
                (LinearLayout.LayoutParams) mWifiList.getLayoutParams()));
                */
        mImgRefresh = (ImageView) findViewById(R.id.imgRefresh);
        /*
        mImgRefresh.setLayoutParams(setMargin(0,
                (int) getResources().getDimension(R.dimen.main_page_margin_bottom_reg_left),
                0,
                0,
                (RelativeLayout.LayoutParams) mImgRefresh.getLayoutParams()));
                */
        mImgRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanWifi();
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, filter);

        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        mWifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String[] data = listView.getItemAtPosition(position).toString().split(":");

                Intent intent = new Intent(WifiList.this, WifiAPSetup.class);
                intent.putExtra(GlobalDef.USE_TYPE,mType);
                intent.putExtra(RoomHubManager.KEY_UUID,mCurUuid);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.addCategory(Intent.CATEGORY_HOME);
                Bundle bundle = new Bundle();
                bundle.putString(GlobalDef.WIFI_AP_SSID, data[0]);
                bundle.putInt(GlobalDef.WIFI_AP_LEVEL, Integer.parseInt(data[1]));
                int security = getSecurity(data[2]);
                GlobalDef.PskType pskType = getPskType(data[2]);
                GlobalDef.WPA_WAP2_SUB_TYPE subType = getSubType(data[2]);
                bundle.putInt(GlobalDef.WIFI_AP_SECURITY, security);
                bundle.putSerializable(GlobalDef.WIFI_AP_PSKTYPE, pskType);
                bundle.putSerializable(GlobalDef.WIFI_AP_PSKSUBTYPE, subType);
                intent.putExtras(bundle);
                if(mType == GlobalDef.TYPE_AP_TRANSFER)
                    startActivityForResult(intent,0);
                else
                    startActivity(intent);
                //System.exit(0);
            }
        });

        scanWifi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private LinearLayout.LayoutParams setMargin(int left, int right, int top, int bottom, LinearLayout.LayoutParams parms) {
        LinearLayout.LayoutParams marginParms = parms;
        marginParms.setMargins(left,top, right, bottom);

        return marginParms;
    }

    private RelativeLayout.LayoutParams setMargin(int left, int right, int top, int bottom, RelativeLayout.LayoutParams parms) {
        RelativeLayout.LayoutParams marginParms = parms;
        marginParms.setMargins(left,top, right, bottom);

        return marginParms;
    }

    private void handleScanResult() {
        mScanList = mWifiManager.getScanResults();
        if(mList == null) {
            mList = new ArrayList<String>();
        } else {
            mList.clear();
        }

        if(mScanList != null) {
            for (int i = mScanList.size() - 1; i >= 0; i--) {
                final ScanResult scanResult = mScanList.get(i);

                if (scanResult == null) {
                    continue;
                }

                // skip Frequency 5GHz
                if(scanResult.frequency > 5000)
                    continue;

                if (TextUtils.isEmpty(scanResult.SSID)) {
                    continue;
                }

                if(scanResult.SSID.contains(getString(R.string.config_onboardee_ssid_prefix))) {
                    continue;
                }

                String accessPoint = scanResult.SSID + ":" + (WifiManager.calculateSignalLevel(scanResult.level, 4)) + ":" + scanResult.capabilities + ":" + scanResult.level;
                mList.add(accessPoint);
            }
            mWifiList.setAdapter(new WifiItemAdapter(this, mList));
        } else {
            mWifiList.setAdapter(null);
        }
    }

    private int getSecurity(String capabilities) {
        if (capabilities.contains("WEP")) {
            return GlobalDef.SECURITY_WEP;
        } else if (capabilities.contains("PSK")) {
            return GlobalDef.SECURITY_PSK; // need to get psk type
        } else if (capabilities.contains("EAP")) {
            return GlobalDef.SECURITY_EAP;
        }
        return GlobalDef.SECURITY_NONE;
    }

    private GlobalDef.PskType getPskType(String capabilities) {
        boolean wpa = capabilities.contains("WPA-PSK");
        boolean wpa2 = capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return GlobalDef.PskType.WPA_WPA2;
        } else if (wpa2) {
            return GlobalDef.PskType.WPA2;
        } else if (wpa) {
            return GlobalDef.PskType.WPA;
        } else {
            return GlobalDef.PskType.UNKNOWN;
        }
    }

    private GlobalDef.WPA_WAP2_SUB_TYPE getSubType(String capabilities) {
        boolean wpa = capabilities.contains("WPA-PSK");
        boolean wpa2 = capabilities.contains("WPA2-PSK");
        if (wpa2 || wpa) {
            if(capabilities.contains("CCMP+TKIP"))
                return GlobalDef.WPA_WAP2_SUB_TYPE.AUTO;
            else if(capabilities.contains("CCMP"))
                return GlobalDef.WPA_WAP2_SUB_TYPE.CCMP;
            else if(capabilities.contains("TKIP"))
                return GlobalDef.WPA_WAP2_SUB_TYPE.TKIP;
            else
                return GlobalDef.WPA_WAP2_SUB_TYPE.UNKNOWN;
        } else {
            return GlobalDef.WPA_WAP2_SUB_TYPE.UNKNOWN;
        }
    }

    private void scanWifi() {
        if(mWifiManager.isWifiEnabled() == false) {
            mWifiManager.setWifiEnabled(true);
        }
        mWifiManager.startScan();
    }

    private class WifiItemAdapter extends BaseAdapter {
        private Context mContext;
        ArrayList<String> mList;
        private LayoutInflater inflater = null;
        private TextView txtWifiSSID;

        private static final int SSID_INDEX = 0;
        private static final int LEVEL_INDEX = 1;
        private static final int SECURITY_INDEX = 2;
        private static final int LEVEL_REAL_INDEX = 3;

        private static final int MIN_RSSI = -100;
        private static final int MAX_RSSI = -55;

        public WifiItemAdapter(Context context, ArrayList<String> data) {
            mContext = context;
            mList = data;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        private int calculateSignalLevel(int rssi, int numLevels) {
            if (rssi <= MIN_RSSI) {
                return 0;
            } else if (rssi >= MAX_RSSI) {
                return numLevels - 1;
            } else {
                int partitionSize = (MAX_RSSI - MIN_RSSI) / (numLevels - 1);
                return (rssi - MIN_RSSI) / partitionSize;
            }
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

        private void setData(View view, int pos) {
            txtWifiSSID = (TextView) view.findViewById(R.id.txtWifiSSID);

           // txtWifiSSID.setTextSize(getResources().getDimension(R.dimen.listview_item_size));
            String[] data = mList.get(pos).split(":");

            txtWifiSSID.setText(data[SSID_INDEX]);

            int level = Integer.parseInt(data[LEVEL_REAL_INDEX]);
            int wifi_icon_level = calculateSignalLevel(level, 3);
            ImageView imgWifiIcon = (ImageView) view.findViewById(R.id.img_wifi_icon);
            if(wifi_icon_level == 0) imgWifiIcon.setImageResource(R.drawable.icon_wifi_weak);
            if(wifi_icon_level == 1) imgWifiIcon.setImageResource(R.drawable.icon_wifi_okay);
            if(wifi_icon_level == 2) imgWifiIcon.setImageResource(R.drawable.icon_wifi_strong);

            ImageView imgWifilock = (ImageView) view.findViewById(R.id.img_wifi_lock);
            if(getSecurity(data[SECURITY_INDEX]) == GlobalDef.SECURITY_NONE) {
                imgWifilock.setVisibility(View.INVISIBLE);
            } else {
                imgWifilock.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null)
                view = inflater.inflate(R.layout.wifi_item, parent, false);

            setData(view, position);

            return view;
        }
    }
}
