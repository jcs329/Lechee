package com.quantatw.roomhub.listener;

import com.quantatw.roomhub.manager.MicroLocationData;
import com.radiusnetworks.ibeacon.IBeacon;

import java.util.ArrayList;
/**
 * Created by jungle on 2015/10/7.
 */
public interface MicroLocationSequenceChangeListener {
    public void onSequenceChange(ArrayList<MicroLocationData> sequenceList);
    public void onServiceIBeaconSequenceChange(ArrayList<IBeacon> beaconsServiceList);
}
