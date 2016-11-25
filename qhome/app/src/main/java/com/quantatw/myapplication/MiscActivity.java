package com.quantatw.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class MiscActivity extends Activity{
    TextView textView;
    TextView textViewGuard;
    TextView textViewRtsp;
    CheckBox checkVoiceAssistant;
    CheckBox tcpVoiceAssistant;
    CheckBox debugVoiceAssistant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_misc);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = 800;
        params.width = 800;
        this.getWindow().setAttributes(params);

        textView = (TextView)findViewById(R.id.location_city);
        textView.setText(new CityPreference(this).getCity());

        textViewGuard = (TextView)findViewById(R.id.guard_phone_number);
        textViewGuard.setText(new CityPreference(this).getGuardPhoneNo());

        textViewRtsp = (TextView)findViewById(R.id.rtsp_ip);
        textViewRtsp.setText(new CityPreference(this).getRTSPip());

        /* YouJun, 2016/05/24, voice assistant <-- */
        checkVoiceAssistant = (CheckBox)findViewById(R.id.enable_voice_assistant);
        checkVoiceAssistant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                new CityPreference(MiscActivity.this).setVoiceAssistantEnabled(isChecked);
            }
        });
        checkVoiceAssistant.setChecked(new CityPreference(this).getVoiceAssistantEnabled());

        tcpVoiceAssistant = (CheckBox)findViewById(R.id.voice_assistant_tcp);
        tcpVoiceAssistant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                new CityPreference(MiscActivity.this).setVoiceAssistantTcpService(isChecked);
            }
        });
        tcpVoiceAssistant.setChecked(new CityPreference(this).getVoiceAssistantTcpService());

        debugVoiceAssistant = (CheckBox)findViewById(R.id.voice_assistant_debug);
        debugVoiceAssistant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                new CityPreference(MiscActivity.this).setVoiceAssistantDebug(isChecked);
            }
        });
        debugVoiceAssistant.setChecked(new CityPreference(this).getVoiceAssistantDebug());

        //((TextView)findViewById(R.id.keywordSensitivity)).setText(new CityPreference(this).getKeywordSensitivity());
        /* YouJun, 2016/05/24, voice assistant --> */

//        editText.setText(new CityPreference(this).getCity());
//        editText = (EditText) findViewById(R.id.city);
//        Log.i("Henry", "onCreate: " + new CityPreference(this).getCity());
//        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                boolean handled = false;
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    setCityPref(editText.getText().toString());
//                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                    handled = true;
//                }
//                return handled;
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        editText.setText(new CityPreference(this).getCity());
//        Log.i("Henry", "onResume: " + new CityPreference(this).getCity());
    }

    private void setCityPref(String city) {
        new CityPreference(this).setCity(city);
        textView.setText(city);
    }

    private void setGuardPhonePref(String phone) {
        new CityPreference(this).setGuardPhoneNo(phone);
        textViewGuard.setText(phone);
    }

    private void setRtspipPref(String rtspip) {
        new CityPreference(this).setRTSPip(rtspip);
        textViewGuard.setText(rtspip);
    }

    /* YouJun, 2016/05/24, voice assistant <-- */
    private void setKeywordSensitivity(String sensitivity) {
        new CityPreference(this).setKeywordSensitivity(sensitivity);
        //((TextView)findViewById(R.id.keywordSensitivity)).setText(sensitivity);

        MainActivity activity = (MainActivity)MainActivity.instance();
        if(activity != null) {
            Handler handler = activity.getHandler();
            if(handler != null) {
                Message msg = Message.obtain();
                msg.what = MainActivity.HANDLER_EVENT_KEYWORD_DETECT;
                msg.arg1 = 2;
                handler.sendMessage(msg);
            }
        }
    }
    /* YouJun, 2016/05/24, voice assistant --> */

    /** Called when the user touches the button */
    public void changeCity(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change city");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setCityPref(input.getText().toString());
            }
        });
        builder.show();
    }

    public void changeGuardPhone(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change guard phone");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setGuardPhonePref(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void changeRTSP(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change RTSP IP");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setRtspipPref(input.getText().toString());

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    /* YouJun, 2016/05/24, voice assistant <-- */
    public void changeKeywordSensitivity(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change sensitivity (20~45)");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("(low sensitivity) 20 ~ 45 (high sensitivity)");
        builder.setView(input);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String result = input.getText().toString();

                if(Integer.valueOf(result) > 45)
                    result = "45";
                else if(Integer.valueOf(result) < 20)
                    result = "20";

                setKeywordSensitivity(result);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    /* YouJun, 2016/05/24, voice assistant --> */
}
