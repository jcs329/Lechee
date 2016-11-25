package org.linphone.tutorials;

import org.linphone.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class TutorialContainerFact {

	private LinearLayout tutorialLinear;
	private LinearLayout pageIndexLinear;
	private TutorialGalleryView scrollView;
	private static final String LOG_TAG = "TutorialGalleryView";

	private Button okBtn;
	private int mScreenWidth;
	private int mScreenHeight;
	private int o_mActiveFeature = 0;

	public TutorialContainerFact(int width, int height) {
		mScreenWidth = width;
		mScreenHeight = height;
	}

	public View createTutorialContainerView(Context context, int [] drawableRes, OnClickListener okBtnHandle) {
		
		LayoutInflater mInflater = LayoutInflater.from(context);
		View inflateView = mInflater.inflate(R.layout.custom_horizontal_scroll_view, null);
		scrollView = new TutorialGalleryView(context, drawableRes, this, mScreenWidth, mScreenHeight);
		tutorialLinear = (LinearLayout) inflateView.findViewById(R.id.tutorialLinear);
		pageIndexLinear = (LinearLayout) inflateView.findViewById(R.id.pageIndexLinear);

		okBtn = (Button) inflateView.findViewById(R.id.tutorialOkBtn);
		
		for(int i = 0 ; i < drawableRes.length ; i++) {
			
            ImageView indexBubble = new ImageView(context);
            if(i == 0){
            	indexBubble.setImageResource(R.drawable.circle_blue_16x16);
            	}
            else
            	indexBubble.setImageResource(R.drawable.circle_grey_16x16);
            //indexBubble.setScaleX(0.2f);
            //indexBubble.setScaleY(0.2f);
            indexBubble.setScaleType(ScaleType.FIT_CENTER);
            indexBubble.setBackgroundColor(Color.TRANSPARENT);
            indexBubble.setPadding(5, 0, 5, 0);
            pageIndexLinear.addView(indexBubble);
		}
		pageIndexLinear.setBackgroundColor(Color.TRANSPARENT);
		//tutorialLinear.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		tutorialLinear.addView(scrollView);
		
		okBtn.setOnClickListener(okBtnHandle);
		return inflateView;
	}
	
	public void scrollToIndex(int selected) {
        if( o_mActiveFeature == selected) {
            Log.d(LOG_TAG, "Stay at the page:" + selected);
            return;
        }
        Log.d(LOG_TAG, "page:" + o_mActiveFeature + " --> page:" + selected);

        ImageView o_selectedView = (ImageView) pageIndexLinear.getChildAt(o_mActiveFeature);
		ImageView selectedView = (ImageView) pageIndexLinear.getChildAt(selected);
		selectedView.setImageResource(R.drawable.circle_blue_16x16);
		o_selectedView.setImageResource(R.drawable.circle_grey_16x16);
		o_mActiveFeature = selected;
	}
}
