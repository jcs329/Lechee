package com.quantatw.roomhub.ui.bpm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
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
import com.quantatw.roomhub.ui.AbstractRoomHubActivity;
import com.quantatw.roomhub.ui.LicenseActivity;
import com.quantatw.roomhub.ui.MainActivity;
import com.quantatw.roomhub.ui.RoomHubViewFilpper;
import com.quantatw.roomhub.utils.Utils;


public class BPMGuideActivity extends AbstractRoomHubActivity implements View.OnClickListener,RoomHubViewFilpper.OnViewFlipperListener{
    private final String TAG=BPMGuideActivity.class.getSimpleName();
    private Context mContext;

    private RoomHubViewFilpper viewFlipper;

    private int[] mImgGuideResId={R.drawable.img_bpm_step_1,R.drawable.img_bpm_step_2,R.drawable.img_bpm_step_3,R.drawable.img_bpm_step_4,R.drawable.img_bpm_step_5};
    private int[] mTxtDescResId={R.string.bpm_guide_step1_desc,R.string.bpm_guide_step2_desc,R.string.bpm_guide_step3_desc,R.string.bpm_guide_step4_desc,R.string.bpm_guide_step5_desc};
    private int[] mTxtStepResId={R.string.bpm_guide_step1,R.string.bpm_guide_step2,R.string.bpm_guide_step3,R.string.bpm_guide_step4,R.string.bpm_guide_step5};
    private int[] mImgPageResId={R.drawable.bpm_loading_01,R.drawable.bpm_loading_02,R.drawable.bpm_loading_03,R.drawable.bpm_loading_04,R.drawable.bpm_loading_05};

    private int mPage=0;

    private LruCache<Integer, Bitmap> mMemoryCache;

    private TextView mTextStep;
    private ImageView mImageProgress;
    private Button mConfirmBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.guide_controller_flipper);

        mContext = this;

        getViews();
    }


    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getViews() {
        mTextStep = (TextView)findViewById(R.id.txt_bpm_guide_step);
        mImageProgress = (ImageView)findViewById(R.id.image_bpm_guide_progress);
        mConfirmBtn = (Button)findViewById(R.id.btn_bpm_guide_skip_comfirm);
        mConfirmBtn.setOnClickListener(this);
        viewFlipper = (RoomHubViewFilpper) findViewById(R.id.body_flipper);
        viewFlipper.setLongClickable(true);
        viewFlipper.setClickable(true);
        viewFlipper.setOnViewFlipperListener(this);
        viewFlipper.addView(createView());
    }

    private View createView() {

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View v = (View) layoutInflater.inflate(R.layout.activity_bpm_guide, null);
        v.setLongClickable(true);
        // mCurView.setClickable(true);
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        refreshTitleDesc();
        initLayout(v);

        return v;
    }

    private void refreshTitleDesc() {
        mTextStep.setText(mTxtStepResId[mPage]);
        mImageProgress.setBackgroundResource(mImgPageResId[mPage]);

        if(mPage == 4) {
            mConfirmBtn.setText(R.string.start_button_name);
            mConfirmBtn.setBackgroundResource(R.drawable.salmon_background_selector);
        }
        else {
            mConfirmBtn.setText(R.string.skip_guide);
            if(Build.VERSION.SDK_INT>=23)
                mConfirmBtn.setBackgroundColor(ContextCompat.getColor(this,R.color.white_mask));
            else
                mConfirmBtn.setBackgroundColor(getResources().getColor(R.color.white_mask));
        }
    }

    private void enableNavigationView(ImageView imageView, boolean enable) {
        if(enable) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setOnClickListener(this);
        }
        else {
            imageView.setVisibility(View.GONE);
            imageView.setOnClickListener(null);
        }
    }

    private void initLayout(View v){

        TextView textStepDesc = (TextView)v.findViewById(R.id.txt_bpm_guide_step_desc);
        ImageView imageGuide = (ImageView)v.findViewById(R.id.image_guide);
        ImageView imageRight = (ImageView)v.findViewById(R.id.image_rightBtn);
        ImageView imageLeft = (ImageView)v.findViewById(R.id.image_leftBtn);

        textStepDesc.setText(mTxtDescResId[mPage]);

        imageGuide.setBackgroundResource(mImgGuideResId[mPage]);
        enableNavigationView(imageLeft, mPage==0?false:true);
        enableNavigationView(imageRight, mPage==4?false:true);

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
        if(++mPage > 4) {
            mPage = 4;
            return null;
        }

        return createView();
    }

    @Override
    public View getPreviousView() {
        if(--mPage < 0) {
            mPage = 0;
            return null;
        }

        return createView();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_bpm_guide_skip_comfirm){
            finish();
        }
        else if(v.getId()==R.id.image_rightBtn) {
            viewFlipper.flingToNext();
        }
        else if(v.getId()==R.id.image_leftBtn) {
            viewFlipper.flingToPrevious();
        }
    }
}
