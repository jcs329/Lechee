package com.quantatw.roomhub.ui.bpm;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.health.data.HealthData;

/**
 * Created by erin on 5/16/16.
 */
public class BPMHistoryUsageFragment extends BPMHistoryFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Toast.makeText(getActivity(), R.string.coming_soon,Toast.LENGTH_SHORT).show();
//        return inflater.inflate(R.layout.frag_bpm_history_usage, container, false);
        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((BPMHistoryActivity)context).onTabSelected(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void refreshUpdate(int type, HealthData device) {
    }

    protected void refreshAdd(HealthData device) {
    }

    protected void refreshRemove(HealthData device) {
    }
}
