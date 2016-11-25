package com.quantatw.roomhub.ui;

import android.content.Context;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.quantatw.myapplication.R;

/**
 * Created by 95010915 on 2015/9/30.
 */
public class ImageEditTextWidget extends LinearLayout {
    Context mContext;
    ImageView imgMain;
    EditText txtMain;

    public ImageEditTextWidget(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public ImageEditTextWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.image_edittext, this);
        imgMain = (ImageView) view.findViewById(R.id.imgMain);
        txtMain = (EditText) view.findViewById(R.id.txtMain);
    //    txtMain.setTextSize(getResources().getDimension(R.dimen.main_page_button_text_size));
    }

    public void setEditImage(int imgId) {
        imgMain.setImageResource(imgId);
    }

    public void setEditHint(int resId) {
        txtMain.setHint(resId);
    }

    public String getText() {
        return txtMain.getText().toString();
    }

    public void setText(String text) {
        txtMain.setText(text);
    }

    public void setEditInputType(int inputType) {
        txtMain.setInputType(inputType);
    }

    public void setTransformationMethod(TransformationMethod method) {
        txtMain.setTransformationMethod(method);
    }
}
