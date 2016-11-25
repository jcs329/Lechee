package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.PreferenceEditor;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.object.CityData;
import com.quantatw.sls.object.TownData;
import com.quantatw.sls.pack.Weather.CityListResPack;
import com.quantatw.sls.pack.Weather.TownListResPack;

import java.util.ArrayList;

/**
 * Created by 95010915 on 2015/10/15.
 */
public class CityChoiceActivity extends AbstractRoomHubActivity {
    LinearLayout mMainLayout, mSearchLayout;

    public final static String TAG = "CityChoiceActivity";

    private final static int LIST_TYPE_CITY = 0;
    private final static int LIST_TYPE_TOWN = 1;

    private int mListType = LIST_TYPE_CITY;
    private int mCityId;
    private Context mContext;

    private RoomHubManager mRoomHubMangager;

    ListView lstList;
    private ArrayList<RegisterDevice> mRegisterDevices;

    private ArrayList<String> mMacStringList;
    private Button mBtnStartNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.citychoice);

        mRoomHubMangager = getRoomHubManager();

        mBtnStartNow = (Button) findViewById(R.id.btnStartNow);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        getData(bundle);

        TextView txtMenuTitle = (TextView) findViewById(R.id.txtTitle);
        txtMenuTitle.setText(getString(R.string.menu_title));

        mContext = this;

        LoadCityData();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.releaseOnBoardingWakeLock();
    }

    private void getData(Bundle bundle) {
        mRegisterDevices = (ArrayList< RegisterDevice>)bundle.getSerializable("DeviceList");
        mMacStringList = bundle.getStringArrayList("DeviceMacList");
    }

    private void LoadCityData() {
        lstList = (ListView) findViewById(R.id.listList);

        mListType = LIST_TYPE_CITY;
        mBtnStartNow.setVisibility(View.INVISIBLE);
        mCityId = -1;
        TextView txtTitle = (TextView) findViewById(R.id.txtCityChoiceTitle);
        txtTitle.setText(getString(R.string.location_title));
        CityListResPack ret = mRoomHubMangager.getCityList(getString(R.string.lang_str));
        if(ret.getStatus_code() == ErrorKey.Success && ret.getCityList().size() > 0) {
            ArrayList<String> cityList = new ArrayList<String>();
            ArrayList<CityData> cityListOrg = ret.getCityList();
            for(int i = 0; i < cityListOrg.size(); i++) {
                cityList.add(cityListOrg.get(i).getCityId() + ":" + cityListOrg.get(i).getCityName());
            }
            lstList.setAdapter(new ItemCityAdapter(mContext, LIST_TYPE_CITY, cityList));
            lstList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView listView = (ListView) parent;
                    String[] data = listView.getItemAtPosition(position).toString().split(":");
                    if(mListType == LIST_TYPE_CITY) {
                        mCityId = Integer.parseInt(data[0]);

                        TextView txtTitle = (TextView) findViewById(R.id.txtCityChoiceTitle);
                        txtTitle.setText(data[1] + " ,  " + getString(R.string.location_title2));
                        TownListResPack ret = mRoomHubMangager.getTownList(mCityId, getString(R.string.lang_str));
                        if(ret.getStatus_code() == ErrorKey.Success) {
                            mListType = LIST_TYPE_TOWN;
                            ArrayList<String> townList = new ArrayList<String>();
                            ArrayList<TownData> townListOrg = ret.getTownList();
                            for(int i = 0; i < townListOrg.size(); i++) {
                                townList.add(townListOrg.get(i).getTownId() + ":" + townListOrg.get(i).getTownName());
                            }
                            lstList.setAdapter(new ItemCityAdapter(mContext, LIST_TYPE_TOWN, townList));
                        } else {
                            Toast.makeText(mContext, Utils.getErrorCodeString(mContext, ret.getStatus_code()), Toast.LENGTH_SHORT).show();
                        }
                    } else { // Town
                        mBtnStartNow.setEnabled(true);
                        mBtnStartNow.setTag(data[0]);
                        TextView txtTitle = (TextView) findViewById(R.id.txtCityChoiceTitle);
                        String[] tmp = txtTitle.getText().toString().split(",");
                        txtTitle.setText(tmp[0] + ", " + data[1]);
                        mBtnStartNow.setText(getString(R.string.start_button_name));
                        mBtnStartNow.setVisibility(View.VISIBLE);
                        mBtnStartNow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String townId = (String) v.getTag();
                                Message msg = new Message();
                                msg.what = MSG_DO_REG;
                                Bundle data = new Bundle();
                                data.putString("town_id", townId);
                                msg.setData(data);
                                mRegHandler.sendMessage(msg);
                            }
                        });
                    }
                }
            });
        } else {
            Toast.makeText(this, Utils.getErrorCodeString(this, ret.getStatus_code()), Toast.LENGTH_SHORT).show();
            mBtnStartNow.setEnabled(true);
            mBtnStartNow.setText(getString(R.string.reload_city));
            mBtnStartNow.setVisibility(View.VISIBLE);
            mBtnStartNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = new Message();
                    msg.what = MSG_RELOADCITY;
                    mRegHandler.sendMessage(msg);
                }
            });
        }
    }

    private final static int MSG_DO_REG = 1020001;
    private final static int MSG_DO_NEXT_ACTIVITY = 1020002;
    private final static int MSG_SHOW_ERROR_MSG =  1020003;
    private final static int MSG_RELOADCITY = 1020004;
    private Handler mRegHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RELOADCITY:
                    LoadCityData();
                    break;
                case MSG_DO_REG:
                    showProgressDialog("", getString(R.string.process_str));
                    final String town_key = msg.getData().getString("town_id");
                    new Thread() {
                        public void run() {
                            // for debug dump:
                            Log.d(TAG,"prepare regDeviceToCloud: mListRoomHub size="+mRegisterDevices.size()+",mMacStringList size="+mMacStringList.size());
                            if(mRegisterDevices.size() != mMacStringList.size()) {
                                for(RegisterDevice registerDevice: mRegisterDevices) {
                                    Log.d(TAG,"mRegisterDevices "+registerDevice.getName()+","+registerDevice.getUuid());
                                }
                                for(String macString:mMacStringList) {
                                    Log.d(TAG,"mMacString "+macString);
                                }
                            }

                            int count = 0;
                            for (int i = 0; i < mRegisterDevices.size(); i++) {
                                int ret = -1;
                                try {
                                    ret = mRoomHubMangager.regDeviceToCloud(mRegisterDevices.get(i).getUuid(), mRegisterDevices.get(i).getName(),
                                            mRegisterDevices.get(i).getVersion(),town_key, mMacStringList.get(i));
                                } finally {
                                    if (ret != ErrorKey.Success && (ret != ErrorKey.DeviceHasOwnerAlready)) {
                                        Log.d(TAG,"regDeviceToCloud failed, error_code="+ret);
                                        count++;
                                        Message msgErr = new Message();
                                        msgErr.what = MSG_SHOW_ERROR_MSG;
                                        Bundle data = new Bundle();
                                        data.putInt("index", i);
                                        data.putInt("error_code", ret);
                                        msgErr.setData(data);
                                        mRegHandler.sendMessage(msgErr);
                                    }
                                }
                            }
                            setPrvision();
                            if(count == 0) {
                                Message msgSend = new Message();
                                msgSend.what = MSG_DO_NEXT_ACTIVITY;
                                mRegHandler.sendMessage(msgSend);
                            }
                        }
                    }.start();
                    break;
                case MSG_DO_NEXT_ACTIVITY:
                    dismissProgressDialog(10, DIALOG_SHOW_NOTHING, ErrorKey.Success);

                    Intent intent = new Intent(CityChoiceActivity.this, RoomHubMainPage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                    break;
                case MSG_SHOW_ERROR_MSG:
                    dismissProgressDialog();

                    int index = msg.getData().getInt("index");
                    int ret = msg.getData().getInt("error_code");
                    showRegErrorDialog(index, ret);
                    //Toast.makeText(mContext, Utils.getErrorCodeString(mContext, ret), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void showRegErrorDialog(int index, int errorCode) {
        RegisterDevice registerDevice = mRegisterDevices.get(index);
        String errorString = Utils.getErrorCodeString(mContext, errorCode);
        String msg = getString(R.string.onboarding_reg_to_cloud_fail,registerDevice.getName());
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(msg);

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setVisibility(View.GONE);

        dialog.show();

    }

    private static final int PROVISION_SET = 1;
    private void setPrvision() {
        PreferenceEditor pref = new PreferenceEditor(this, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        pref.setIntValue(GlobalDef.ROOMHUB_SETTINGS_PROVISION, PROVISION_SET);
    }

    private class ItemCityAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<String> mList;
        private LayoutInflater inflater = null;
        private int mType = LIST_TYPE_CITY;

        public ItemCityAdapter(Context context, int type, ArrayList<String> data) {
            mContext = context;
            mList = data;
            mType = type;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            View view = convertView;
            if (view == null)
                view = inflater.inflate(R.layout.city_choice_item, null);

            TextView txtName = (TextView) view.findViewById(R.id.txtName);
            String[] tmpArray = mList.get(position).split(":");
            txtName.setText(tmpArray[1]);

            return view;
        }
    }

    @Override
    public void onBackPressed() {
        if(mListType == LIST_TYPE_TOWN) {
            LoadCityData();
        } else {
            finish();
        }
    }
}
