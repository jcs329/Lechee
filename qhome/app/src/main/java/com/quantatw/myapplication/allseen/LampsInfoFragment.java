package com.quantatw.myapplication.allseen;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;

import com.quantatw.myapplication.LampsPageActivity;
import com.quantatw.myapplication.R;

import org.allseen.lsf.sdk.Color;
import org.allseen.lsf.sdk.Lamp;
import org.allseen.lsf.sdk.LampCapabilities;
import org.allseen.lsf.sdk.LampParameters;
import org.allseen.lsf.sdk.LampStateUniformity;

/**
 * Created by youjun on 2016/5/3.
 */
public class LampsInfoFragment extends Fragment  implements OnSeekBarChangeListener, OnClickListener {

    private LSFLightingController lsfController;

    private TableLayout lampInfo;
    private SeekBar brightnessSeekBar;
    private SeekBar hueSeekBar;
    private SeekBar saturationSeekBar;
    private SeekBar tempSeekBar;

    private LampCapabilities capability;
    private int viewColorTempMin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("LampsInfoFragment", "onCreateView");

        View view = inflater.inflate(R.layout.lamps_info_fragment, container, false);
        lampInfo = (TableLayout)view.findViewById(R.id.lampInfoTable);

        brightnessSeekBar = (SeekBar)lampInfo.findViewById(R.id.sliderBrightness);
        brightnessSeekBar.setEnabled(false);
        brightnessSeekBar.setSaveEnabled(false);
        brightnessSeekBar.setOnSeekBarChangeListener(this);
        lampInfo.findViewById(R.id.controlBrightness).setOnClickListener(this);

        hueSeekBar = (SeekBar)lampInfo.findViewById(R.id.sliderHue);
        hueSeekBar.setMax(360);
        hueSeekBar.setEnabled(false);
        hueSeekBar.setSaveEnabled(false);
        hueSeekBar.setOnSeekBarChangeListener(this);
        lampInfo.findViewById(R.id.controlHue).setOnClickListener(this);

        saturationSeekBar = (SeekBar)lampInfo.findViewById(R.id.sliderSaturation);
        saturationSeekBar.setEnabled(false);
        saturationSeekBar.setSaveEnabled(false);
        saturationSeekBar.setOnSeekBarChangeListener(this);
        lampInfo.findViewById(R.id.controlSaturation).setOnClickListener(this);

        tempSeekBar = (SeekBar)lampInfo.findViewById(R.id.sliderColorTemp);
        tempSeekBar.setEnabled(false);
        tempSeekBar.setSaveEnabled(false);
        tempSeekBar.setOnSeekBarChangeListener(this);
        lampInfo.findViewById(R.id.controlColorTemp).setOnClickListener(this);

        capability = new LampCapabilities();

        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //Log.d("LampsInfoFragment", "onProgressChanged");
        // Currently nothing to do
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //Log.d("LampsInfoFragment", "onStartTrackingTouch");
        // Currently nothing to do
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //Log.d("LampsInfoFragment", "onStopTrackingTouch");

        setField(seekBar);
        if(seekBar.getId() == R.id.sliderSaturation) {
            saturationCheck();
        }

        setSeekBarValues(seekBar);
    }

    @Override
    public void onClick(View view) {
        //Log.d("LampsInfoFragment", "onClick");

        int viewID = view.getId();
        if(viewID == R.id.controlBrightness) {
            if(capability.dimmable <= LampCapabilities.NONE) {
                ((LampsPageActivity)getActivity()).showToast(getResources().getString(R.string.no_support_dimmable));
            }
        }
        else if(viewID == R.id.controlHue) {
            if(capability.color <= LampCapabilities.NONE) {
                ((LampsPageActivity)getActivity()).showToast(getResources().getString(R.string.no_support_color));
            }
            else if(saturationSeekBar.getProgress() == 0) {
                ((LampsPageActivity)getActivity()).showToast(getResources().getString(R.string.saturation_disable_hue));
            }
        }
        else if(viewID == R.id.controlSaturation) {
            if(capability.color <= LampCapabilities.NONE) {
                ((LampsPageActivity)getActivity()).showToast(getResources().getString(R.string.no_support_color));
            }
        }
        else if(viewID == R.id.controlColorTemp) {
            if(capability.temp <= LampCapabilities.NONE) {
                ((LampsPageActivity)getActivity()).showToast(getResources().getString(R.string.no_support_temp));
            }
            else if(saturationSeekBar.getProgress() == saturationSeekBar.getMax()) {
                ((LampsPageActivity)getActivity()).showToast(getResources().getString(R.string.saturation_disable_temp));
            }
        }
    }

    public void setLSFController(LSFLightingController lsf) {
        lsfController = lsf;
        //Log.d("LampsInfoFragment", "setLSFController - lsfController: " + lsfController);
    }

    public void updateInfoFields(Lamp lamp, Boolean forceNoShow) {
        Log.d("LampsInfoFragment", "updateInfoFields - lamp: " + lamp.getName() + ", forceNoShow: " + forceNoShow);

        if(getActivity() == null) {
            Log.w("LampsInfoFragment", "updateInfoFields - activity is NOT CREATED");
            return;
        }

        if(lsfController == null) {
            Log.w("LampsInfoFragment", "updateInfoFields - NO lsfController");
            return;
        }

        if(lamp.getName().contains("Loading lamp info")) {
            Log.w("LampsInfoFragment", "updateInfoFields - lamp is loading");

            if(forceNoShow)
                lampInfo.setVisibility(View.INVISIBLE);
            return;
        }

        viewColorTempMin = lsfController.getColorTempMin(lamp);
        tempSeekBar.setMax(lsfController.getColorTempSpan(lamp));

        brightnessSeekBar.setTag(lamp.getId());
        hueSeekBar.setTag(lamp.getId());
        saturationSeekBar.setTag(lamp.getId());
        tempSeekBar.setTag(lamp.getId());

        setCapability(lamp.getCapability());

        Color color = lamp.getState().getColor();
        LampStateUniformity uniformity = lamp.getUniformity();

        setBrightness(color.getBrightness(), uniformity.brightness);
        setHue(color.getHue(), uniformity.hue);
        setSaturation(color.getSaturation(), uniformity.saturation);
        setColorTemp(color.getColorTemperature(), uniformity.colorTemp);

        LampParameters lampParams = lamp.getParameters();
        setTextViewValue((View)lampInfo, R.id.lampInfoTextLumens, String.valueOf(lampParams.getLumens()), 0);
        setTextViewValue((View)lampInfo, R.id.lampInfoTextEnergy, String.valueOf(lampParams.getEnergyUsageMilliwatts()), R.string.units_mw);

        lampInfo.setVisibility(View.VISIBLE);
    }

    public void setCapability(LampCapabilities capability) {
        Log.d("LampsInfoFragment", "setCapability");

        this.capability = capability;

        // dimmable
        if(capability.dimmable >= LampCapabilities.SOME) {
            brightnessSeekBar.setEnabled(true);
        }
        else {
            brightnessSeekBar.setEnabled(false);
            setTextViewValue((View)lampInfo, R.id.textBrightness, getResources().getString(R.string.na), 0);
        }

        // color support
        if(capability.color >= LampCapabilities.SOME) {
            hueSeekBar.setEnabled(true);
            saturationSeekBar.setEnabled(true);
        }
        else {
            hueSeekBar.setEnabled(false);
            saturationSeekBar.setEnabled(false);
            setTextViewValue((View)lampInfo, R.id.textHue, getResources().getString(R.string.na), 0);
            setTextViewValue((View)lampInfo, R.id.textSaturation, getResources().getString(R.string.na), 0);
        }

        // temperature support
        if(capability.temp >= LampCapabilities.SOME) {
            tempSeekBar.setEnabled(true);
        }
        else {
            tempSeekBar.setEnabled(false);
            setTextViewValue((View)lampInfo, R.id.textColorTemp, getResources().getString(R.string.na), 0);
        }

        saturationCheck();
    }

    private void setBrightness(int viewBrightness, boolean uniformBrightness) {
        Log.d("LampsInfoFragment", "setBrightness(brightneww: " + viewBrightness + ", uniform: " + uniformBrightness + ")");

        if(capability.dimmable >= LampCapabilities.SOME) {
            brightnessSeekBar.setProgress(viewBrightness);
            brightnessSeekBar.setThumb(getActivity().getResources().getDrawable(uniformBrightness ? R.drawable.allseen_slider_thumb_normal : R.drawable.allseen_slider_thumb_midstate));

            setTextViewValue((View)lampInfo, R.id.textBrightness, String.valueOf(viewBrightness), R.string.units_percent);
        }
    }

    private void setHue(int viewHue, boolean uniformHue) {
        Log.d("LampsInfoFragment", "setHue(hue: " + viewHue + ", uniform: " + uniformHue + ")");

        if (capability.color >= LampCapabilities.SOME) {
            hueSeekBar.setProgress(viewHue);
            hueSeekBar.setThumb(getActivity().getResources().getDrawable(uniformHue ? R.drawable.allseen_slider_thumb_normal : R.drawable.allseen_slider_thumb_midstate));

            setTextViewValue((View)lampInfo, R.id.textHue, String.valueOf(viewHue), R.string.units_degrees);
        }
    }

    private void setSaturation(int viewSaturation, boolean uniformSaturation) {
        Log.d("LampsInfoFragment", "setSaturation(saturation: " + viewSaturation + ", uniform: " + uniformSaturation + ")");

        if (capability.color >= LampCapabilities.SOME) {
            saturationSeekBar.setProgress(viewSaturation);
            saturationSeekBar.setThumb(getActivity().getResources().getDrawable(uniformSaturation ? R.drawable.allseen_slider_thumb_normal : R.drawable.allseen_slider_thumb_midstate));

            setTextViewValue((View)lampInfo, R.id.textSaturation, String.valueOf(viewSaturation), R.string.units_percent);
            saturationCheck();
        }
    }

    private void setColorTemp(int viewColorTemp, boolean uniformColorTemp) {
        Log.d("LampsInfoFragment", "setColorTemp(color: " + viewColorTemp + ", uniform: " + uniformColorTemp + ")");

        if (capability.temp >= LampCapabilities.SOME) {
            tempSeekBar.setProgress(viewColorTemp - viewColorTempMin);
            tempSeekBar.setThumb(getActivity().getResources().getDrawable(uniformColorTemp ? R.drawable.allseen_slider_thumb_normal : R.drawable.allseen_slider_thumb_midstate));

            setTextViewValue((View)lampInfo, R.id.textColorTemp, String.valueOf(viewColorTemp), R.string.units_kelvin);
        }
    }

    private void saturationCheck() {
        Log.d("LampsInfoFragment", "saturationCheck");

        if(saturationSeekBar.getProgress() == 0) {
            hueSeekBar.setEnabled(false);
        }
        else if(capability.color >= LampCapabilities.SOME) {
            hueSeekBar.setEnabled(true);
        }

        if(saturationSeekBar.getProgress() == saturationSeekBar.getMax()) {
            tempSeekBar.setEnabled(false);
        }
        else if (capability.temp >= LampCapabilities.SOME) {
            tempSeekBar.setEnabled(true);
        }
    }

    private void setField(SeekBar seekBar) {
        Log.d("LampsInfoFragment", "setField");

        if(lsfController == null) {
            Log.w("LampsInfoFragment", "setField - No lsfController");
            return;
        }

        int seekBarID = seekBar.getId();
        if(seekBarID == R.id.sliderBrightness) {
            lsfController.setBrightness(seekBar.getTag().toString(), seekBar.getProgress());
        }
        else if(seekBarID == R.id.sliderHue) {
            lsfController.setHue(seekBar.getTag().toString(), seekBar.getProgress());
        }
        else if(seekBarID == R.id.sliderSaturation) {
            lsfController.setSaturation(seekBar.getTag().toString(), seekBar.getProgress());
        }
        else if(seekBarID == R.id.sliderColorTemp) {
            lsfController.setColorTemp(seekBar.getTag().toString(), seekBar.getProgress() + viewColorTempMin);
        }
    }

    private void setSeekBarValues(SeekBar seekBar) {
        Log.d("LampsInfoFragment", "setSeekBarValues");

        int seekBarID = seekBar.getId();
        int progress = seekBar.getProgress();

        if(seekBarID == R.id.sliderBrightness) {
            setTextViewValue((View)lampInfo, R.id.textBrightness, String.valueOf(progress), R.string.units_percent);
        }
        else if(seekBarID == R.id.sliderHue) {
            setTextViewValue((View)lampInfo, R.id.textHue, String.valueOf(progress), R.string.units_degrees);
        }
        else if(seekBarID == R.id.sliderSaturation) {
            setTextViewValue((View)lampInfo, R.id.textSaturation, String.valueOf(progress), R.string.units_percent);
        }
        else if(seekBarID == R.id.sliderColorTemp) {
            setTextViewValue((View)lampInfo, R.id.textColorTemp, String.valueOf(progress+viewColorTempMin), R.string.units_kelvin);
        }
    }

    private void setTextViewValue(View parent, int viewID, String value, int unitsID) {
        Log.d("LampsInfoFragment", "setTextViewValue");

        TextView textView = (TextView)parent.findViewById(viewID);
        if(textView != null) {
            String text;
            if(value != null) {
                if(unitsID > 0) {
                    text = value + getResources().getString(unitsID);
                }
                else {
                    text = value;
                }
            }
            else {
                text = "";
            }
            textView.setText(text);
        }
    }
}
