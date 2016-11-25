package com.quantatw.roomhub.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ViewFlipper;
import com.quantatw.roomhub.ui.RoomHubGestureListener.OnFlingListener;
import com.quantatw.myapplication.R;

public class RoomHubViewFilpper extends ViewFlipper implements OnFlingListener {
    private GestureDetector mGestureDetector = null;

    private OnViewFlipperListener mOnViewFlipperListener = null;

    public RoomHubViewFilpper(Context context) {
        super(context);
    }

    public RoomHubViewFilpper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnViewFlipperListener(OnViewFlipperListener mOnViewFlipperListener) {
        this.mOnViewFlipperListener = mOnViewFlipperListener;
        RoomHubGestureListener myGestureListener = new RoomHubGestureListener();

        myGestureListener.setOnFlingListener(this);
        mGestureDetector = new GestureDetector(myGestureListener);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(mGestureDetector.onTouchEvent(ev)){
            ev.setAction(MotionEvent.ACTION_CANCEL);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mGestureDetector.onTouchEvent(ev);
    }

    @Override
    public void flingToNext() {
        if (null != mOnViewFlipperListener) {
            View next_view=mOnViewFlipperListener.getNextView();
            if(next_view != null) {
                int childCnt = getChildCount();
                if (childCnt > 1) {
                    removeViewAt(1);
                }
                addView(next_view, 0);
                if (0 != childCnt) {
                    setInAnimation(getContext(), R.anim.push_left_in);
                    setOutAnimation(getContext(), R.anim.push_left_out);
                    setDisplayedChild(0);
                }
            }
        }
    }

    @Override
    public void flingToPrevious() {
        if (null != mOnViewFlipperListener) {
            View prev_view=mOnViewFlipperListener.getPreviousView();
            if(prev_view != null) {
                int childCnt = getChildCount();
                if (childCnt > 1) {
                    removeViewAt(1);
                }
                addView(prev_view, 0);
                if (0 != childCnt) {
                    setInAnimation(getContext(), R.anim.push_right_in);
                    setOutAnimation(getContext(), R.anim.push_right_out);
                    setDisplayedChild(0);
                }
            }
        }
    }

    public interface OnViewFlipperListener {
        View getNextView();
        View getPreviousView();
    }
}
