package com.quantatw.roomhub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quantatw.myapplication.R;

/**
 * Created by 95010915 on 2015/9/24.
 */
public class SetupWifiActivity extends AbstractRoomHubActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_wifi);

        Button btnLogout = (Button) findViewById(R.id.btnLogout);
        /*btnLogout.setTextSize(getResources().getDimension(R.dimen.main_page_button_text_size));
        btnLogout.setLayoutParams(setMargin(0,
                (int) getResources().getDimension(R.dimen.wifi_logout_margin),
                (int) getResources().getDimension(R.dimen.wifi_logout_margin), 0,
                (RelativeLayout.LayoutParams) btnLogout.getLayoutParams()));
        */
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        TextView txtTitle, txtDesc;
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        /*txtTitle.setTextSize(getResources().getDimension(R.dimen.main_page_text_main_title_size));
        txtTitle.setLayoutParams(setMargin(0,
                (int) getResources().getDimension(R.dimen.wifi_logout_margin),
                (int) getResources().getDimension(R.dimen.wifi_logout_margin), 0,
                (RelativeLayout.LayoutParams) txtTitle.getLayoutParams()));
*/
        txtDesc = (TextView) findViewById(R.id.txtDesc);
        //txtDesc.setTextSize(getResources().getDimension(R.dimen.main_page_text_title_desc_size));

        ImageView imgWifi = (ImageView) findViewById(R.id.imgWifi);
        imgWifi.setLayoutParams(setMargin(0, 0,
                (int) getResources().getDimension(R.dimen.wifi_setup_image_margin),
                (int) getResources().getDimension(R.dimen.wifi_setup_image_margin),
                (RelativeLayout.LayoutParams) imgWifi.getLayoutParams()));

        Button btnSearch = (Button) findViewById(R.id.btnSearchWifi);
        //btnSearch.setTextSize(getResources().getDimension(R.dimen.main_page_button_text_size));
        btnSearch.setLayoutParams(setMargin(0, 0, 0,
                (int) getResources().getDimension(R.dimen.wifi_setup_image_margin),
                (RelativeLayout.LayoutParams) btnSearch.getLayoutParams()));
        btnSearch.getLayoutParams().height = (int) getResources().getDimension(R.dimen.btn_height);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupWifiActivity.this, WifiList.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                //System.exit(0);
            }
        });
    }

    private RelativeLayout.LayoutParams setMargin(int left, int right, int top, int bottom, RelativeLayout.LayoutParams parms) {
        RelativeLayout.LayoutParams marginParms = parms;
        marginParms.setMargins(left, top, right, bottom);

        return marginParms;
    }

    private LinearLayout.LayoutParams setMargin(int left, int right, int top, int bottom, LinearLayout.LayoutParams parms) {
        LinearLayout.LayoutParams marginParms = parms;
        marginParms.setMargins(left, top, right, bottom);

        return marginParms;
    }
}
