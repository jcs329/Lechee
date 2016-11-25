package com.quantatw.roomhub.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import com.quantatw.myapplication.R;

/**
 * Created by erin on 11/11/15.
 */
public class CustomProgressDialog extends ProgressDialog {
    public CustomProgressDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        setProgressDrawable(getContext().getResources().getDrawable(R.drawable.custom_progressbar));
        super.onCreate(savedInstanceState);
    }
}
