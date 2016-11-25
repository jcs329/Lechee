package com.quantatw.myapplication;

import java.io.File;
import java.util.ArrayList;

import com.bumptech.glide.Glide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotoGalleryActivity extends Activity {
    private ArrayList<String> images;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        GridView gallery = (GridView) findViewById(R.id.galleryGridView);
        gallery.setAdapter(new ImageAdapter(this));
        gallery.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                /*
                if (null != images && !images.isEmpty())
                    Toast.makeText(
                            getApplicationContext(),
                            "position " + position + " " + images.get(position),
                            300).show();
                ;
                */
                /*
                if (null != images && !images.isEmpty()) {
                    android.content.Intent intent = new android.content.Intent();
                    intent.putExtra("DATA", images.get(position));
                    setResult(RESULT_OK, intent);
                    finish();
                }
                */
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.setClass(PhotoGalleryActivity.this, PhotoViewerActivity.class);
                intent.putExtra("DATA", images.get(position));
                startActivity(intent);

            }
        });
    }

    private class ImageAdapter extends BaseAdapter {

        /** The context. */
        private Activity context;

        /**
         * Instantiates a new image adapter.
         *
         * @param localContext
         *            the local context
         */
        public ImageAdapter(Activity localContext) {
            context = localContext;
            images = getAllShownImagesPath(context);
        }

        public int getCount() {
            return images.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ImageView picturesView;
            if (convertView == null) {
                picturesView = new ImageView(context);
                picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                picturesView
                        .setLayoutParams(new GridView.LayoutParams(270, 270));

            } else {
                picturesView = (ImageView) convertView;
            }

            Glide.with(context).load(images.get(position))
                    //.placeholder(R.drawable.ic_launcher).centerCrop()
                    .into(picturesView);

            return picturesView;
        }

        private ArrayList<String> getAllShownImagesPath(Activity activity) {
            File f = new File("/sdcard/Pictures/MyCameraApp");
            File file[] = f.listFiles();
            Log.d("Files", "Size: "+ file.length);
            ArrayList<String> listOfAllImages = new ArrayList<String>();
            for (int i=0; i < file.length; i++)
            {
                Log.d("Files", "FileName:" + file[i].getName());
                listOfAllImages.add("/sdcard/Pictures/MyCameraApp/" + file[i].getName());
            }
            return listOfAllImages;
        }
    }

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    //    getMenuInflater().inflate(R.menu.main, menu);
    //    return true;
    //}

    //@Override
    //public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    //    int id = item.getItemId();
    //    if (id == R.id.action_settings) {
    //        return true;
    //    }
    //    return super.onOptionsItemSelected(item);
    //}
}
