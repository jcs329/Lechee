package com.quantatw.roomhub.manager.health.manager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.ui.RoomHubApplication;
import com.quantatw.roomhub.ui.RoomHubShareHubActivity;
import com.quantatw.roomhub.ui.ShareHubInfoActivity;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.key.ErrorKey;

/**
 * Created by erin on 5/17/16.
 */
public class HealthDeviceView {
    protected Context mContext;

    private ProgressDialog mProgressDialog;
    protected HealthDeviceController mHealthDeviceController; //manager

    protected class MenuDialog extends AlertDialog.Builder {
        private static final String TAG = "HealthDeviceMenuDialog";
        private Context mContext;
        private HealthData mHealthData;
        private ArrayAdapter<String> listAdapter;
        private AlertDialog mAlert;

        private ListView lv;

        public MenuDialog(Context context,HealthData healthData, String[] menu_list){
            super(context);
            mContext=context;
            mHealthData = healthData;

            View dialog_layout = LayoutInflater.from(context).inflate(R.layout.roomhub_menu_dialog, null);
            setView(dialog_layout);

            Log.d(TAG, "HealthDeviceMenuDialog uuid=" + mHealthData.getUuid());

            lv= (ListView) dialog_layout.findViewById(R.id.popup_listview);

            listAdapter = new ArrayAdapter(mContext,R.layout.roomhub_menu_item,menu_list);
            lv.setDividerHeight(1);
            lv.setAdapter(listAdapter);


            mAlert=create();
            mAlert.setView(dialog_layout,0,0,0,0);
            mAlert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            mAlert.show();
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
            lv.setOnItemClickListener(listener);
        }
        public void dismiss(){
            mAlert.dismiss();
        }
    }

    protected String makeUpStringWithDeviceTitle(String resourceName, int titleResourceId) {
        int id = mContext.getResources().getIdentifier(resourceName,"string",mContext.getPackageName());
        if(id == R.string.share_healthdevice ||
                id == R.string.share_device_info ||
                id == R.string.share_leave_device) {
            return mContext.getResources().getString(id,
                    mContext.getString(titleResourceId));
        }
        else
            return mContext.getResources().getString(id);
    }

    // Override if necessary
    protected String[] getMenuPolicy(HealthData healthData) {
        // pre-defined menu: Share,Manage
        String[] menu_array = mContext.getResources().getStringArray(R.array.healthdevice_login_menu);

        HealthDeviceManager healthDeviceManager = ((RoomHubApplication)mContext.getApplicationContext()).getHealthDeviceManager();
        int titleResource = healthDeviceManager.getHealthDeviceTypeTitleResource(healthData.getType());
        if(titleResource > 0) {
            String[] menuResource = mContext.getResources().getStringArray(R.array.healthdevice_login_menu);
            menu_array  = new String[menuResource.length];
            for(int i=0;i<menuResource.length;i++) {
                menu_array[i] = makeUpStringWithDeviceTitle(menuResource[i],titleResource);
            }
        }
        return menu_array;
    }

    // Override if manager has different policy to load menu
    // Override this need to also override 'menuClick'
    protected String[] getMenuPolicyWithPermissions(HealthData healthData) {
        if(HealthDeviceManager.NEW_BPM_SHARE_STYLE) {
            // only Owner can show menu
            final boolean isOwner = healthData.IsOwner();
            String[] menu_array = null;

            HealthDeviceManager healthDeviceManager = ((RoomHubApplication) mContext.getApplicationContext()).getHealthDeviceManager();
            int titleResource = healthDeviceManager.getHealthDeviceTypeTitleResource(healthData.getType());

            if (isOwner) {
                String[] menuResource = mContext.getResources().getStringArray(R.array.healthdevice_share_menu);
                menu_array = new String[menuResource.length];
                for (int i = 0; i < menuResource.length; i++) {
                    menu_array[i] = makeUpStringWithDeviceTitle(menuResource[i], titleResource);
                }
            }

            return menu_array;
        }
        else {
            com.quantatw.roomhub.manager.AccountManager accountManager = ((RoomHubApplication) mContext.getApplicationContext()).getAccountManager();
            final boolean isLogin = accountManager.isLogin();
            final boolean isOwner = healthData.IsOwner();
            final boolean isFriend = healthData.IsFriend();
            String[] menu_array;

            HealthDeviceManager healthDeviceManager = ((RoomHubApplication) mContext.getApplicationContext()).getHealthDeviceManager();
            int titleResource = healthDeviceManager.getHealthDeviceTypeTitleResource(healthData.getType());

            if (isLogin) {
                if (isOwner) {
                    String[] menuResource = mContext.getResources().getStringArray(R.array.healthdevice_login_menu);
                    menu_array = new String[menuResource.length];
                    for (int i = 0; i < menuResource.length; i++) {
                        menu_array[i] = makeUpStringWithDeviceTitle(menuResource[i], titleResource);
                    }
                } else {
                    if (isFriend) {
                        String[] menuResource = mContext.getResources().getStringArray(R.array.healthdevice_login_not_owner);
                        menu_array = new String[menuResource.length];
                        for (int i = 0; i < menuResource.length; i++) {
                            menu_array[i] = makeUpStringWithDeviceTitle(menuResource[i], titleResource);
                        }
                    } else {
                        Toast.makeText(mContext, R.string.insufficient_permissions, Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }
            } else {
                if (isOwner) {
                    menu_array = mContext.getResources().getStringArray(R.array.healthdevice_menu);
                } else {
                    Toast.makeText(mContext, R.string.insufficient_permissions, Toast.LENGTH_SHORT).show();
                    return null;
                }
            }
            return menu_array;
        }
    }

    // Override if manager wants to have different style menu
    protected void openMenu(final HealthData healthData) {
//        String[] menu_array = getMenuPolicy(healthData);
        String[] menu_array = getMenuPolicyWithPermissions(healthData);

        final MenuDialog menu = new MenuDialog(mContext, healthData, menu_array);

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                menuClick(position, healthData);
                menu.dismiss();
            }
        });
    }

    // Override if manager has different policy to load menu
    protected void menuClick(int position, HealthData healthData) {
        com.quantatw.roomhub.manager.AccountManager accountManager = ((RoomHubApplication)mContext.getApplicationContext()).getAccountManager();
        final boolean isLogin=accountManager.isLogin();
        final boolean isOwner=healthData.IsOwner();
        final boolean isFriend=healthData.IsFriend();

        if(isLogin){
            if(isOwner)
                OwnerItemClick(position,healthData);
            else {
                if(isFriend)
                    NoOwnerItemClick(position, healthData);
            }
        }else {
            if(isOwner)
                NoLoginItemClick(position, healthData);
        }
    }

    // Override this since each manager has different manage page
    protected void manageDevice(HealthData healthData) {
        Toast.makeText(mContext,"Manage uuid="+healthData.getUuid(),Toast.LENGTH_SHORT).show();
    }

    private void OwnerItemClick(int position, HealthData healthData) {
        final int MENU_SHARE = 0;
        final int MENU_MANAGE = 1;

        switch (position) {
            case MENU_SHARE:
                Intent intent = new Intent(mContext, RoomHubShareHubActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putInt(GlobalDef.KEY_DEVICE_CATEGORY, DeviceTypeConvertApi.CATEGORY.HEALTH);
                bundle.putInt(GlobalDef.KEY_DEVICE_TYPE, healthData.getType());
                bundle.putString(RoomHubManager.KEY_UUID, healthData.getUuid());
                bundle.putString(RoomHubManager.KEY_DEV_NAME, healthData.getDeviceName());
                intent.putExtras(bundle);
                mContext.startActivity(intent);
                break;
            case MENU_MANAGE:
                manageDevice(healthData);
                break;
        }
    }

    private void NoOwnerItemClick(int position, HealthData healthData) {
        final int MENU_SHARE = 0;
        final int MENU_LEAVE = 1;

        switch (position) {
            case MENU_SHARE:
                Intent intent = new Intent(mContext, ShareHubInfoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putString(RoomHubManager.KEY_UUID, healthData.getUuid());
                bundle.putString("owner_name", healthData.getOwnerId());
                bundle.putInt(GlobalDef.KEY_DEVICE_TYPE,healthData.getType());
                intent.putExtras(bundle);
                mContext.startActivity(intent);
                break;
            case MENU_LEAVE:
                LeaveDeviceDialog(healthData);
                break;
        }
    }

    private void NoLoginItemClick(int position, HealthData healthData) {
        final int MENU_MANAGE = 0;

        switch (position) {
            case MENU_MANAGE:
                break;
        }
    }

    private void LeaveDeviceDialog(HealthData healthData){
        final String str_uuid=healthData.getUuid();

        final Context context = mContext.getApplicationContext();
        final Dialog dialog = new Dialog(mContext,R.style.CustomDialog);

        HealthDeviceManager healthDeviceManager = ((RoomHubApplication)mContext.getApplicationContext()).getHealthDeviceManager();
        int titleResource = healthDeviceManager.getHealthDeviceTypeTitleResource(healthData.getType());

        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(mContext.getString(R.string.share_leave_device_prompt_message,mContext.getString(titleResource)));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setMessage(mContext.getString(R.string.processing_str));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                mProgressDialog.show();
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        com.quantatw.roomhub.manager.AccountManager accountManager =
                                ((RoomHubApplication)context).getAccountManager();

                        int ret = accountManager.DeleteDeviceUser(str_uuid, accountManager.getUserId());
                        HealthDeviceManager.traceLog("LeaveDeviceDialog ret=" + ret);
                        mProgressDialog.dismiss();
                        if (ret != ErrorKey.Success) {
                            Toast.makeText(mContext, Utils.getErrorCodeString(mContext,ret), Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                thread.start();
            }
        });


        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public HealthDeviceView(Context context, HealthDeviceController healthDeviceController) {
        mContext = context;
        mHealthDeviceController = healthDeviceController;
    }

    public View bindView(HealthData healthData, int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void onItemClick(HealthData healthData) {
    }
}
