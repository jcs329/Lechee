package org.linphone.tutorials;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class TutorialGalleryView extends HorizontalScrollView {
	private static final int SWIPE_MIN_DISTANCE = 5;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private static final String LOG_TAG = "TutorialGalleryView";

	private ArrayList<ImageView> mItems = null;
	private ArrayList<Integer> pointList = null;

	private GestureDetector mGestureDetector;
	private int mActiveFeature = 0;
	private int parentWidth = 0;
	private int parentHeight = 0;
	private Context mContext;
	private TutorialContainerFact mWrapper;
	private LinearLayout tutorialLinear;

	public TutorialGalleryView(Context context, int [] drawableRes, TutorialContainerFact wrapper, 
			int parentWidth, int parentHeight) {
		super(context);
		this.mContext = context;
		this.mWrapper = wrapper;
		this.mItems = new ArrayList<ImageView>();
		this.pointList = new ArrayList<Integer>();
		this.parentWidth = parentWidth;
		this.parentHeight = parentHeight;
		for(int i = 0 ; i < drawableRes.length ; i++){
			pointList.add(i*parentWidth);
		}
		setFeatureItem(drawableRes);
	}
	
	private void setFeatureItem(int [] drawableRes) {

		LayoutParams params = new LayoutParams(parentWidth, LayoutParams.MATCH_PARENT);

		tutorialLinear = new LinearLayout(getContext());
		tutorialLinear.setLayoutParams(params);
		tutorialLinear.setOrientation(LinearLayout.HORIZONTAL);

 		mGestureDetector = new GestureDetector(new MyGestureDetector());

		for(int i = 0 ; i < drawableRes.length; i++) {
			ImageView img = new ImageView(mContext);
            img.setImageResource(drawableRes[i]);
            img.setBackgroundColor(Color.WHITE);
            img.setLayoutParams(params);
            img.setScaleType(ScaleType.CENTER_INSIDE);

            tutorialLinear.addView(img);
            mItems.add(img);
        }

		addView(tutorialLinear);
		setHorizontalFadingEdgeEnabled(false);
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);
        setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				//If the user swipes
 				if (mGestureDetector.onTouchEvent(event)) {
 					return true;
 				}
 				else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL ){
 					
 					//pageScroll(HorizontalScrollView.FOCUS_RIGHT);
		   			Log.d(LOG_TAG, "onTouch page:" + mActiveFeature + " width:" + parentWidth);
		   			int width = v.getMeasuredWidth();
 					mActiveFeature = ((getScrollX() + (width/2))/width);
 					mWrapper.scrollToIndex(mActiveFeature);
 					smoothScrollTo(pointList.get(mActiveFeature), 0);
 					return true;
 				}
 				else{
 					return false;
 				}
 			}
 		});
	}
	
	public int getSelectedIndex() {
		return mActiveFeature;
	}
	
	public int getCount() {
		return mItems.size();
	}

	class MyGestureDetector extends SimpleOnGestureListener {
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 			try {
 				//right to left
  				if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					mActiveFeature = (mActiveFeature < (mItems.size() - 1))? mActiveFeature + 1:mItems.size() -1;
					Log.d(LOG_TAG,"onFling left page:" + mActiveFeature + " to:" + pointList.get(mActiveFeature));
					smoothScrollTo(pointList.get(mActiveFeature), 0);
 			        mWrapper.scrollToIndex(mActiveFeature);
 			        return true;
 				}
   				//left to right
 				else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					mActiveFeature = (mActiveFeature > 0)? mActiveFeature - 1:0;
					Log.d(LOG_TAG,"onFling right page:" + mActiveFeature + " to:" + pointList.get(mActiveFeature));
					smoothScrollTo(pointList.get(mActiveFeature), 0);
 			        mWrapper.scrollToIndex(mActiveFeature);
 			        return true;
				}
			} catch (Exception e) {
			        Log.e("Fling", "There was an error processing the Fling event:" + e.getMessage());
			}
			return false;
		}
	}

}
