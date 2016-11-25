package com.quantatw.myapplication.allseen;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.quantatw.myapplication.LampsPageActivity;
import com.quantatw.myapplication.R;

import org.allseen.lsf.sdk.ColorItem;
import org.allseen.lsf.sdk.Lamp;

/**
 * Created by youjun on 2016/5/3.
 */
public class LampsTableFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private LSFLightingController lsfController;
    private TableLayout lampTable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("LampsTableFragment", "onCreateView");

        View view =inflater.inflate(R.layout.lamps_table_fragment, container, false);
        lampTable = (TableLayout)view.findViewById(R.id.lampsTable);

        return view;
    }

    @Override
    public void onClick(View view) {
        //Log.d("LampsTableFragment", "onClick - id: " + view.getId() + ", tag: " + view.getTag().toString());

        int clickID = view.getId();
        if(clickID == R.id.buttonPower) {
            if(lsfController != null)
                lsfController.togglePower(view.getTag().toString());
        }
        else if(clickID == R.id.lampRowLayout) {
            ((LampsPageActivity)getActivity()).updateInfoFields(view.getTag().toString(), true);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        //Log.d("LampsTableFragment", "onTouch");

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(view.getId() == R.id.lampRowLayout) {
                    ((LampsPageActivity)getActivity()).updateInfoFields(view.getTag().toString(), true);
                }
                break;
        }
        return false;
    }

    public void setLSFController(LSFLightingController lsf) {
        lsfController = lsf;
        //Log.d("LampsTableFragment", "setLSFController - lsfController: " + lsfController);
    }

    public void addLamps(Lamp[] lamps) {
        Log.d("LampsTableFragment", "addLamps");

        for(Lamp lamp : lamps)
            addLamp(lamp);
    }

    public void addLamp(ColorItem item) {
        Log.d("LampsTableFragment", "addLamp(" + item.getName() + ")");

        if(getActivity() == null) {
            Log.w("LampsTableFragment", "addLamp - activity is NOT CREATED");
            return;
        }

        if(item != null) {
            //Log.d("LampsTableFragment", "addLamp - itemID: " + item.getId() + ", Uniformity: " + item.getUniformity().power + ", isOn: " + item.isOn() +
            //        ", name: " + item.getName() + ", tag: " + item.getTag());

            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);

            String itemID = item.getId();
            TableRow tableRow = (TableRow)lampTable.findViewWithTag(itemID);

            if(tableRow == null) {
                Log.d("LampsTableFragment", "addLamp - tableRow is NULL");

                tableRow = new TableRow(getActivity());
                tableRow.setTag(itemID);

                inflater.inflate(R.layout.lamps_row, tableRow);

                ImageButton powerButton = (ImageButton) tableRow.findViewById(R.id.buttonPower);
                powerButton.setTag(itemID);
                powerButton.setBackgroundResource(item.getUniformity().power ? (item.isOn() ? R.drawable.allseen_power_button_on : R.drawable.allseen_power_button_off) : R.drawable.allseen_power_button_mix);
                powerButton.setOnClickListener(this);

                TextView lampText = ((TextView) tableRow.findViewById(R.id.textLamp));
                lampText.setText(item.getName());
                lampText.setTag(itemID);

                RelativeLayout lampLayout = ((RelativeLayout)tableRow.findViewById(R.id.lampRowLayout));
                lampLayout.setOnTouchListener(this);
                lampLayout.setTag(itemID);

                //Log.d("LampsTableFragment", "addLamp - lampTable childCount: " + lampTable.getChildCount());
                lampTable.addView(tableRow, lampTable.getChildCount());
            }
            else {
                ((ImageButton)tableRow.findViewById(R.id.buttonPower)).setBackgroundResource(item.getUniformity().power ?
                        (item.isOn() ? R.drawable.allseen_power_button_on : R.drawable.allseen_power_button_off) : R.drawable.allseen_power_button_mix);
                ((TextView) tableRow.findViewById(R.id.textLamp)).setText(item.getName());
            }
        }
    }

    public void removeLamp(Lamp lamp) {
        Log.d("LampsTableFragment", "removeLamp(" + lamp.getName() + ")");

        final TableRow row = (TableRow)lampTable.findViewWithTag(lamp.getId());
        if(row != null) {
            lampTable.removeView(row);
            lampTable.postInvalidate();
        }
    }
}
