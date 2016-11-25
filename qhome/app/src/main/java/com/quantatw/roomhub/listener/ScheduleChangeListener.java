package com.quantatw.roomhub.listener;

import com.quantatw.sls.device.Schedule;
import com.quantatw.sls.pack.roomhub.NextScheduleResPack;

import java.util.ArrayList;

/**
 * Created by 95010915 on 2015/9/23.
 */
public interface ScheduleChangeListener {
    /*
    public void addDevice(ACData data);
    public void removeDevice(ACData data);
    public void UpdateACData(ACData data);
    public void UpdatePageStatus(boolean enabled,ACData data);
    */
    public void UpdateSchedule(String uuid, Schedule schedule);
    public void UpdateAllSchedule(String uuid, ArrayList<Schedule> schedule_lst);
    public void NextSchedule(NextScheduleResPack resPack);
    public void DeleteSchedule(int idx);
    public void onCommandResult(String uuid, int result);

}
