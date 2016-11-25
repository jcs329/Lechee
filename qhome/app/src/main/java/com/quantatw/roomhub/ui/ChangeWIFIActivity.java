package com.quantatw.roomhub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.GlobalDef;

public class ChangeWIFIActivity extends AbstractRoomHubActivity implements View.OnClickListener{

    private String mCurUuid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_change);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        mCurUuid = getIntent().getStringExtra(RoomHubManager.KEY_UUID);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_confirm:
                Intent intent = new Intent(this, WifiList.class);
                intent.putExtra(RoomHubManager.KEY_UUID, mCurUuid);
                intent.putExtra(GlobalDef.USE_TYPE,GlobalDef.TYPE_AP_TRANSFER);
                startActivity(intent);
                break;
            case R.id.btn_cancel:
                break;
        }
        finish();
    }
}
