package com.quantatw.sls.listener;

import com.quantatw.sls.device.Schedule;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.pack.roomhub.AcOnOffStatusResPack;
import com.quantatw.sls.pack.roomhub.DeleteScheduleResPack;
import com.quantatw.sls.pack.roomhub.DeviceFirmwareUpdateStateResPack;
import com.quantatw.sls.pack.roomhub.DeviceInfoChangeResPack;
import com.quantatw.sls.pack.roomhub.LearningResultResPack;
import com.quantatw.sls.pack.roomhub.NameChangeResPack;
import com.quantatw.sls.pack.roomhub.NextScheduleResPack;
import com.quantatw.sls.pack.roomhub.RoomHubDataResPack;
import com.quantatw.sls.pack.roomhub.UpdateScheduleResPack;

import java.util.ArrayList;

public interface  RoomHubSignalListener {
    void RoomHubDataUpdate(RoomHubDataResPack dataResPack, SourceType sourceType);
    void RoomHubLearningResultUpdate(LearningResultResPack learningResultResPack);
    void RoomHubDeviceInfoChangeUpdate(DeviceInfoChangeResPack deviceInfoChangeResPack, SourceType sourceType);
    void RoomHubNameChangeUpdate(NameChangeResPack nameResPack);
    void RoomHubSyncTime();
    void RoomHubAcOnOffStatusUpdate(AcOnOffStatusResPack resPack);
    void RoomHubUpdateSchedule(UpdateScheduleResPack resPack);
    void RoomHubUpdateAllSchedule(String uuid,ArrayList<Schedule> schedule_lst);
    void RoomHubDeleteSchedule(DeleteScheduleResPack resPack);
    void RoomHubNextSchedule(NextScheduleResPack resPack);
    void RoomHubOTAUpgradeStateChangeUpdate(DeviceFirmwareUpdateStateResPack resPack);
}
