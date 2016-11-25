package com.quantatw.myapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PhotoViewerActivity extends Activity {
    private boolean zoomOut = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        Bundle file = getIntent().getExtras();
        android.graphics.Bitmap myJPG = BitmapFactory.decodeFile(file.getString("DATA"));
        //image.setImageBitmap(myJPG);
        final ImageView imageView  = (ImageView)findViewById(R.id.imageView1);
        imageView.setImageBitmap(myJPG);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(zoomOut) {
                    //Toast.makeText(getApplicationContext(), "NORMAL SIZE!", Toast.LENGTH_LONG).show();
                    //imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    //imageView.setAdjustViewBounds(true);
                    //zoomOut =false;
                }else{
                    //Toast.makeText(getApplicationContext(), "FULLSCREEN!", Toast.LENGTH_LONG).show();
                    //imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    //zoomOut = true;
                }
            }
        });
    }
}
