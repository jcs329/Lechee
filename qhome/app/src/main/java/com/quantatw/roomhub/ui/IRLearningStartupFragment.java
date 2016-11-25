package com.quantatw.roomhub.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.IRController;

/**
 * Created by erin on 10/13/15.
 */
public class IRLearningStartupFragment extends Fragment {
    private FragmentActivity mParent;
    private boolean mSearchDone = false;

    private ImageView mAimImage, mSearchImage, mConnectImage;

    private final IRController.OnSignalLearningCallback mOnSignalLearningCallback = new IRController.OnSignalLearningCallback() {
        @Override
        public void onSignalLearning() {
            mHandler.sendEmptyMessage(MESSAGE_LEARNING_SIGNAL);
        }

        @Override
        public void onFound() {
            mHandler.sendEmptyMessage(MESSAGE_LEARNING_FOUND);
        }
    };

    private final int MESSAGE_LEARNING_SIGNAL = 100;
    private final int MESSAGE_LEARNING_FOUND = 200;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_LEARNING_FOUND:
                    notifySearchingDone();
                    break;
                case MESSAGE_LEARNING_SIGNAL:
                    notifySearchingOngoing();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_ir_learning_startup, container, false);
        mAimImage = (ImageView)view.findViewById(R.id.currentAction_aim);
        mSearchImage = (ImageView)view.findViewById(R.id.currentAction_search);
        mConnectImage = (ImageView)view.findViewById(R.id.currentAction_connect);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        notifySearchingStart();
        ((IRLearningActivity)mParent).startupIRLearning(mOnSignalLearningCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mParent = (FragmentActivity)context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void notifySearchingStart() {
        mSearchDone = false;
        mAimImage.setVisibility(View.VISIBLE);
        mSearchImage.setVisibility(View.INVISIBLE);
        mConnectImage.setVisibility(View.INVISIBLE);
    }

    private void notifySearchingOngoing() {
        mSearchDone = false;
        mAimImage.setVisibility(View.INVISIBLE);
        mSearchImage.setVisibility(View.VISIBLE);
        mConnectImage.setVisibility(View.INVISIBLE);
    }

    private void notifySearchingDone() {
        mSearchDone = true;
        mAimImage.setVisibility(View.INVISIBLE);
        mSearchImage.setVisibility(View.INVISIBLE);
        mConnectImage.setVisibility(View.VISIBLE);
    }
}
