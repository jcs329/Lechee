package com.quantatw.roomhub.ui;

/**
 * Created by 95011613 on 2015/10/20.
 */
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class RoomHubGestureListener extends SimpleOnGestureListener{

    private OnFlingListener mOnFlingListener;
    private static final int SWIPE_THRESHOLD = 100;

    @Override
    public final boolean onFling(final MotionEvent e1, final MotionEvent e2,
                                 final float speedX, final float speedY) {
        if (mOnFlingListener == null) {
            return super.onFling(e1, e2, speedX, speedY);
        }

        float XFrom = e1.getX();
        float XTo = e2.getX();
        float YFrom = e1.getY();
        float YTo = e2.getY();
        if (XFrom - XTo > SWIPE_THRESHOLD) {
            mOnFlingListener.flingToNext();
        }else if (XTo - XFrom > SWIPE_THRESHOLD){
            mOnFlingListener.flingToPrevious();
        }else
            return false;

        return true;
    }

    public interface OnFlingListener {
        void flingToNext();

        void flingToPrevious();
    }

    public OnFlingListener getOnFlingListener() {
        return mOnFlingListener;
    }

    public void setOnFlingListener(OnFlingListener mOnFlingListener) {
        this.mOnFlingListener = mOnFlingListener;
    }
}
