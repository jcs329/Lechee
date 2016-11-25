package com.quantatw.roomhub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.quantatw.myapplication.R;

/**
 * Created by erin on 10/7/15.
 */
public class IRConfigActivity extends AbstractRoomHubActivity {
    public static final String GET_DATA_ROOM_HUB_DEVICE_UUID = "ROOM_HUB_DEVICE_UUID";
    public static final String GET_DATA_IR_CODE_NUM = "IR_CODE_NUMBER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_ir_code_num);

        final String uuid = getIntent().getStringExtra(GET_DATA_ROOM_HUB_DEVICE_UUID);

        final EditText codeNumEdit = (EditText)findViewById(R.id.editIRCodeNum);

        Button btn = (Button)findViewById(R.id.configIRBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(codeNumEdit.getText() != null && !codeNumEdit.getText().toString().isEmpty()) {
                    // send code number back
                    Intent intent = new Intent();
                    intent.putExtra(GET_DATA_ROOM_HUB_DEVICE_UUID, uuid);
                    intent.putExtra(GET_DATA_IR_CODE_NUM, codeNumEdit.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
