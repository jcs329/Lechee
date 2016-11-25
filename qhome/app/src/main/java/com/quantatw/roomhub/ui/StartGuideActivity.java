package com.quantatw.roomhub.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.utils.Utils;


public class StartGuideActivity extends AbstractRoomHubActivity implements View.OnClickListener,RoomHubViewFilpper.OnViewFlipperListener{
    private final String TAG=StartGuideActivity.class.getSimpleName();
    private Context mContext;

    private RoomHubViewFilpper viewFlipper;

    private int[] mImgGuideResId={R.drawable.i_m_g_0001,R.drawable.img_0002,R.drawable.img_0003,R.drawable.i_m_g_0004};
    private int[] mTxtDescResId={R.string.start_guide_page_1,R.string.start_guide_page_2,R.string.start_guide_page_3,R.string.start_guide_page_4};
    private int[] mImgPageResId={R.drawable.start_guide_loading_01,R.drawable.start_guide_loading_02,R.drawable.start_guide_loading_03,R.drawable.start_guide_loading_04};

    private int mPage=0;

    private LruCache<Integer, Bitmap> mMemoryCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.room_hub_controller_flipper);

        mContext = this;

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        /*
        Bitmap bmp= BitmapFactory.decodeResource(getResources(), mImgGuideResId[0]);

        bmp_width=bmp.getWidth();
        bmp_height=bmp.getHeight();
        */
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!Utils.isShowWelcome(this)){
            viewFlipper = (RoomHubViewFilpper) findViewById(R.id.body_flipper);
            viewFlipper.setLongClickable(true);
            viewFlipper.setClickable(true);
            viewFlipper.setOnViewFlipperListener(this);
            View v=createView();
            viewFlipper.addView(v, 0);
        }else{
            LaunchMainActivity();
        }
    }

    private View createView() {

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View v = (View) layoutInflater.inflate(R.layout.start_guide_activity, null);
        v.setLongClickable(true);
        // mCurView.setClickable(true);
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        initLayout(v);

        return v;
    }

    private void initLayout(View v){

        DisplayMetrics dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        float height = 0;
        Bitmap bitmap = mMemoryCache.get(mPage);
        if(bitmap == null) {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), mImgGuideResId[mPage]);
            int bmp_width = bmp.getWidth();
            int bmp_height = bmp.getHeight();

            height = (float) (dm.heightPixels * 0.75);
            float scaleWidth = (float) dm.widthPixels / (float) bmp_width;
            float scaleHeight = height / (float) bmp_height;

            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp_width, bmp_height, matrix, true);
            mMemoryCache.put(mPage,bitmap);
            if(bmp != null)
                bmp.recycle();
        }
        else
            height = bitmap.getHeight();

        ImageView img_guide=(ImageView)v.findViewById(R.id.img_guide);
        RelativeLayout.LayoutParams parms=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) height);
        img_guide.setLayoutParams(parms);
        img_guide.setImageBitmap(bitmap);

        LinearLayout ll_bottom=(LinearLayout)v.findViewById(R.id.ll_desc);
        RelativeLayout.LayoutParams ll_parms=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (dm.heightPixels-height));
        int margin= (int) (getResources().getDimension(R.dimen.start_guide_margin)/dm.density);
        ll_parms.setMargins(margin,0,margin,0);
        ll_parms.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ll_bottom.setLayoutParams(ll_parms);

        Button btn_skip=(Button)v.findViewById(R.id.btn_skip);
        btn_skip.setOnClickListener(this);

        TextView txt_desc=(TextView)v.findViewById(R.id.txt_desc);
        txt_desc.setText(mTxtDescResId[mPage]);

        ImageView load_page=(ImageView)v.findViewById(R.id.btn_page);
        load_page.setImageResource(mImgPageResId[mPage]);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMemoryCache != null) {
            for (int i = 0; i < mMemoryCache.size(); i++) {
                Bitmap bitmap = mMemoryCache.get(i);
                if (bitmap != null) {
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            }
        }
    }

    @Override
    public View getNextView() {
        mPage++;
        if(mPage == 4) {
            Intent intent = new Intent(this,LicenseActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            finish();
        }else{
            return createView();
        }
        return null;
    }

    @Override
    public View getPreviousView() {

        if(mPage >= 0) {
            if((mPage-1) < 0)
                return null;
            else {
                mPage--;
                return createView();
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_skip){
            Intent intent = new Intent(this,LicenseActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            finish();
        }
    }

    private void LaunchMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        finish();
    }
}
