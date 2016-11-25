package com.quantatw.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

public class CameraViewActivity extends Activity {
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Timer _continueMonitorOn;
    private boolean _need_monitor = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);

        /*Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });*/
        Bundle b = getIntent().getExtras();
        boolean auto_ans = true;
        if(b != null)
            auto_ans = b.getBoolean("auto_answer");

        final android.media.MediaPlayer ringTone = new android.media.MediaPlayer();
        try{
            ringTone.setDataSource("/data/data/com.quantatw.myapplication/files/ringback.wav");
            ringTone.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            ringTone.prepare();
            ringTone.setLooping(true);
            if(auto_ans == false) ringTone.start();
        }
        catch(java.io.IOException e){
        }

        ImageView  screen_cap = (ImageView) findViewById(R.id.button_capture);
        screen_cap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //MainActivity.instance().turnofSpeaker();
                mCamera.takePicture(null, null, mPicture);
                //android.widget.Toast.makeText(CameraViewActivity.this, "Take a snapshot ..", android.widget.Toast.LENGTH_SHORT).show();
            }

        });

        final ImageView  answer_door = (ImageView) findViewById(R.id.answer_door);
        answer_door.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                ringTone.release();
                //ringTone = null;
                MainActivity.instance().answerDoorCall(); // 0x02
                MainActivity.instance().turnonSpeaker();
                answer_door.setVisibility(View.GONE);
            }

        });

        ImageView  close_preview = (ImageView) findViewById(R.id.close_preview);
        close_preview.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if(ringTone != null) {
                    ringTone.release(); // LiangBin add, 20160705
                    //ringTone = null;
                }
                MainActivity.instance().turnoffSpeaker();
                _need_monitor = false;
                _continueMonitorOn.cancel();
                MainActivity.instance().stopDoorRing(); // 0x03
                MainActivity.instance().hangupDoorCall(); // 0x78
                MainActivity.instance().resetSophia(); // LiangBin add, 20160622
                finish();
            }

        });
        //((android.view.ViewGroup)captureButton.getParent()).removeView(captureButton);
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.layoutCam);
        ((android.view.ViewGroup)layout.getParent()).removeView(layout);
        preview.addView(layout/*captureButton*/);

        if(auto_ans == true) {
            answer_door.setVisibility(View.GONE);
            MainActivity.instance().answerDoorCall(); // 0x02
            MainActivity.instance().turnonSpeaker(); // LiangBin add, 20160527, 20160603
        }
        _continueMonitorOn = new Timer();
        _continueMonitorOn.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                if(_need_monitor)
                    MainActivity.instance().restartDoorMonitor(); // 0x79
            }
        }, 2000, 18000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            MainActivity.instance().turnoffSpeaker();
            _need_monitor = false;
            _continueMonitorOn.cancel();
            MainActivity.instance().stopDoorRing(); // 0x03
            MainActivity.instance().hangupDoorCall(); // 0x78
            MainActivity.instance().resetSophia(); // LiangBin add, 20160622
            finish();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == 0/*REQUEST_CODE*/) {
            if (resultCode == RESULT_OK) {
                // A contact was picked.  Here we will just display it
                // to the user.
                //startActivity(new Intent(Intent.ACTION_VIEW, data));
                Bundle file = data.getExtras();
                //android.widget.Toast.makeText(CameraViewActivity.this, "show a snapshot .."
                //        + file.getString("DATA"), android.widget.Toast.LENGTH_SHORT).show();
                //<<< LiangBin add, JPG preview
                final android.app.Dialog dialog = new android.app.Dialog(CameraViewActivity.this);
                dialog.setContentView(R.layout.custom_image_preview);
                dialog.setTitle("Path: " + file.getString("DATA"));

                android.widget.TextView text = (android.widget.TextView) dialog.findViewById(R.id.text);
                text.setText("Your Text");
                ImageView image = (ImageView) dialog.findViewById(R.id.image);
                android.graphics.Bitmap myJPG = BitmapFactory.decodeFile(file.getString("DATA"));
                image.setImageBitmap(myJPG);

                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                Button dialogButton2 = (Button) dialog.findViewById(R.id.dialogButtonGallery);
                dialogButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.setClass(CameraViewActivity.this, PhotoGalleryActivity.class);
                        //startActivity(intent);
                        startActivityForResult(intent, 0);
                        dialog.dismiss();
                    }
                });
                dialog.show();
                //>>> LiangBin add, 20160705
            }
        }
    }

    @Override
    protected void onNewIntent (Intent intent) {
        //Log.d(TAG, "onNewIntent()");
        setIntent(intent);

        Bundle b = getIntent().getExtras();
        boolean auto_ans = true;
        if(b != null)
            auto_ans = b.getBoolean("auto_answer");

        final android.media.MediaPlayer ringTone = new android.media.MediaPlayer();
        try{
            ringTone.setDataSource("/data/data/com.quantatw.myapplication/files/ringback.wav");
            ringTone.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            ringTone.prepare();
            ringTone.setLooping(true);
            if(auto_ans == false) ringTone.start();
        }
        catch(java.io.IOException e){
        }
        if(auto_ans == true) {
            ImageView  answer_door = (ImageView) findViewById(R.id.answer_door);
            answer_door.setVisibility(View.GONE);
            MainActivity.instance().answerDoorCall(); // 0x02
            MainActivity.instance().turnonSpeaker(); // LiangBin add, 20160527, 20160603
        }
        super.onNewIntent(intent);
    }

    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length).copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(bmp);
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setTextSize(14);
                p.setColor(Color.GREEN);
                String _t = pictureFile.getName().substring(4).replace("_", " ");
                // format: 2016/07/18 15:54:00
                StringBuffer t2 = new StringBuffer(_t.substring(0, _t.length()-4));
                t2.insert(t2.length()-2, ":");
                t2.insert(t2.length()-5, ":");
                t2.insert(4, "/");
                t2.insert(7, "/");
                canvas.drawText(t2.toString(), bmp.getWidth()/5*4, bmp.getHeight()-15, p);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                //fos.write(data);
                fos.flush();
                fos.close();
                refreshGallery(pictureFile);
                //<<< LiangBin add, JPG preview
                final android.app.Dialog dialog = new android.app.Dialog(CameraViewActivity.this);
                dialog.setContentView(R.layout.custom_image_preview);
                dialog.setTitle("Path: /sdcard/Pictures/MyCameraApp/" + pictureFile.getName());

                android.widget.TextView text = (android.widget.TextView) dialog.findViewById(R.id.text);
                text.setText("Your Text");
                ImageView image = (ImageView) dialog.findViewById(R.id.image);
                android.graphics.Bitmap myJPG = BitmapFactory.decodeFile(pictureFile.getPath());
                image.setImageBitmap(myJPG);

                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                Button dialogButton2 = (Button) dialog.findViewById(R.id.dialogButtonGallery);
                dialogButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.setClass(CameraViewActivity.this, PhotoGalleryActivity.class);
                        startActivity(intent);
                        //startActivityForResult(intent, 0);
                        //dialog.dismiss();
                    }
                });
                final String _fileName = pictureFile.getName();
                Button dialogButton3 = (Button) dialog.findViewById(R.id.dialogButtonFullScreen);
                dialogButton3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.setClass(CameraViewActivity.this, PhotoViewerActivity.class);
                        intent.putExtra("DATA", "/sdcard/Pictures/MyCameraApp/" + _fileName);
                        startActivity(intent);
                        //startActivityForResult(intent, 0);
                        //dialog.dismiss();
                    }
                });
                dialog.show();
                //>>> LiangBin add, 20160705
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                //Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(android.net.Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }
}
