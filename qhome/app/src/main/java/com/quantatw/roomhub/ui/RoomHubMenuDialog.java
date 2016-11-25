package com.quantatw.roomhub.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;

public class RoomHubMenuDialog extends AlertDialog.Builder {//implements AdapterView.OnItemClickListener {
    private static final String TAG = "RoomHubMenuDialog";
    private RoomHubData mData;
    private Context mContext;
    private ArrayAdapter<String> listAdapter;
    private AlertDialog mAlert;

    private ListView lv;

    public RoomHubMenuDialog(Context context,RoomHubData data,String[] menu_list){
        super(context);
        mContext=context;
        mData=data;

        View dialog_layout = LayoutInflater.from(context).inflate(R.layout.roomhub_menu_dialog, null);
        setView(dialog_layout);

        Log.d(TAG, "RoomHubMenuDialog uuid=" + mData.getUuid());

        lv= (ListView) dialog_layout.findViewById(R.id.popup_listview);

        listAdapter = new ArrayAdapter(mContext,R.layout.roomhub_menu_item,menu_list);
        lv.setDividerHeight(1);
        lv.setAdapter(listAdapter);


        mAlert=create();
        mAlert.setView(dialog_layout,0,0,0,0);
        mAlert.show();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
        lv.setOnItemClickListener(listener);
    }
    public void dismiss(){
        mAlert.dismiss();
    }
    /*
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case MENU_RENAME:
                Log.d(TAG, "RoomHubMenuDialog onItemClick uuid=" + mData.getUuid());
                Intent intent = new Intent(mContext, RenameActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(RoomHubManager.KEY_UUID,mData.getUuid());
                bundle.putString(RoomHubManager.KEY_DEV_NAME, mData.getName());

                intent.putExtras(bundle);

                mContext.startActivity(intent);
                mAlert.dismiss();
                break;
            case MENU_PAIR:
                if(((RoomHubMainPage)mContext).isAlljoyn(mData.getUuid()))
                    ((RoomHubMainPage)mContext).configIRSetting(mData.getUuid());
                else
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.roomhub_warning_msg), Toast.LENGTH_SHORT).show();
                mAlert.dismiss();
                break;
            case MENU_DELETE:
                ((RoomHubMainPage)mContext).DeleteDevice(mData.getUuid());
                mAlert.dismiss();
                break;
        }
    }
    */
}
