package com.quantatw.roomhub.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.quantatw.myapplication.R;

/**
 * Created by erin on 10/14/15.
 */
public class IRLearningFailFragment extends Fragment {
    private FragmentActivity mParent;
    private IRLearningFailFragment mInstance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_ir_learning_fail, container, false);
        View bottomLayout = (View)view.findViewById(R.id.bottomLayout);
        Button autoScanButton = (Button)bottomLayout.findViewById(R.id.btnAutoScan);
        autoScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((IRLearningActivity)mParent).autoScan(mInstance);
            }
        });

        Button retryButton = (Button)bottomLayout.findViewById(R.id.btnRetry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((IRLearningActivity)mParent).doRetry(mInstance);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //((IRLearningActivity)mParent).blinkLed();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mParent = (FragmentActivity)context;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
