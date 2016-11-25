package com.quantatw.myapplication.information_delivery;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
 
import android.content.Context;  
import android.content.res.Resources; 
import android.content.Intent; 
import android.graphics.Bitmap;  
import android.graphics.BitmapFactory;  
import android.graphics.drawable.BitmapDrawable;  


import android.view.View;   
import android.widget.LinearLayout;    
import com.quantatw.myapplication.R;
import com.quantatw.myapplication.information_delivery.fileio.ImageLoader;
import com.quantatw.myapplication.information_delivery.view.SmoothImageView;
import com.quantatw.myapplication.information_delivery.view.SquareCenterImageView;


public class Package extends Activity {
	
	private LinearLayout devicesView;
    private  LinearLayout loadingView;
	public ImageLoader imageLoader;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar);
        super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();  
		String mailto = bundle.getString("mailto");
		String mailfrom = bundle.getString("mailfrom");
		final String mailtoimage = bundle.getString("mailtoimage");
		
		imageLoader=new ImageLoader(this.getApplicationContext());

        setContentView( R.layout.activity_package);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = 700;
        params.width = 600;
		

        this.getWindow().setAttributes(params);
         
		TextView txtMailTo = (TextView) findViewById(R.id.moretext1);
		TextView txtMailFrom = (TextView) findViewById(R.id.moretext2);
		TextView txtMailNote = (TextView) findViewById(R.id.moretext3);
        //MyZoomImageView imageView = (MyZoomImageView) findViewById(R.id.moreimage);
		ImageView imageView = (ImageView) findViewById(R.id.moreimage);
		
		
		txtMailTo.setText(mailto);
		txtMailFrom.setText(mailfrom);
		txtMailNote.setText("Status: Waitting");
		imageView.setVisibility(View.VISIBLE);
		imageLoader.DisplayImage(mailtoimage, imageView);
		
		final SquareCenterImageView imageView1 = new SquareCenterImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageLoader.DisplayImage(mailtoimage,imageView1);

		infoView infoView = new infoView();
		infoView.setallowRefresh(true);
		
	}


	
	
	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing()) {
			overridePendingTransition(0, 0);
		}
	}	

	
}

