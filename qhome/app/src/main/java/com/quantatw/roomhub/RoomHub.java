package com.quantatw.roomhub;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.quantatw.myapplication.R;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.device.Schedule;
import com.quantatw.sls.key.ReasonType;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.listener.RoomHubDeviceListener;
import com.quantatw.sls.listener.RoomHubSignalListener;
import com.quantatw.sls.pack.device.GetScheduleListResPack;
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

import static com.quantatw.sls.api.DeviceTypeConvertApi.CATEGORY;

public class RoomHub extends Activity implements RoomHubDeviceListener,RoomHubSignalListener {
    // TODO: read this from config
    private static final String MQTT_SERVER =  "tcp://10.0.8.101:1883";
    private static final String CLOUD_SERVER =  "http://10.0.8.101";

    private Context mContext = null;
    private MiddlewareApi mMiddlewareApi = null;
    private RoomHubDeviceListener mRoomHubDeviceListener = new RoomHubDeviceListener() {
        @Override
        public void addDevice(RoomHubDevice device, ReasonType reason) {

        }

        @Override
        public void removeDevice(RoomHubDevice device, ReasonType reason) {

        }

        @Override
        public void updateDevice(RoomHubDevice device) {

        }

        @Override
        public void switchNetwork(boolean connected) {

        }
    };
    private RoomHubDeviceListener mRoomHubDeviceListener2 = new RoomHubDeviceListener() {
        @Override
        public void addDevice(RoomHubDevice device, ReasonType reason) {

        }

        @Override
        public void removeDevice(RoomHubDevice device, ReasonType reason) {

        }

        @Override
        public void updateDevice(RoomHubDevice device) {
            
        }

        @Override
        public void switchNetwork(boolean connected) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_hub);
        mContext = getApplication().getApplicationContext();

        mMiddlewareApi= MiddlewareApi.getInstance(mContext, new MiddlewareApi.ApiConfig(MQTT_SERVER, CLOUD_SERVER));

        mMiddlewareApi.registerRoomHubDeviceListener(CATEGORY.ROOMHUB,mRoomHubDeviceListener);
        mMiddlewareApi.registerRoomHubDeviceListener(CATEGORY.ROOMHUB,mRoomHubDeviceListener2);
        mMiddlewareApi.registerRoomHubDeviceListener(CATEGORY.ROOMHUB,this);
        mMiddlewareApi.registerRoomHubSignalListener(CATEGORY.ROOMHUB,this);

        // get device list form cloud
        //ArrayList<CloudDevice> devices = mMiddlewareApi.getAllDeviceList().getDevices(); // Cloud devices
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_room_hub, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMiddlewareApi.unregisterRoomHubDeviceListener(CATEGORY.ROOMHUB,mRoomHubDeviceListener);
        mMiddlewareApi.unregisterRoomHubDeviceListener(CATEGORY.ROOMHUB,mRoomHubDeviceListener2);
        mMiddlewareApi.unregisterRoomHubSignalListener(this);
    }

    @Override
    public void RoomHubDataUpdate(RoomHubDataResPack dataResPack, SourceType sourceType) {

    }

    @Override
    public void RoomHubLearningResultUpdate(LearningResultResPack learningResultResPack) {

    }

    @Override
    public void RoomHubDeviceInfoChangeUpdate(DeviceInfoChangeResPack deviceInfoChangeResPack, SourceType sourceType) {

    }

    @Override
    public void RoomHubNameChangeUpdate(NameChangeResPack nameResPack) {
        
    }

    @Override
    public void RoomHubSyncTime() {
        
    }

    @Override
    public void RoomHubAcOnOffStatusUpdate(AcOnOffStatusResPack resPack) {
        
    }

    @Override
    public void RoomHubUpdateSchedule(UpdateScheduleResPack resPack) {

    }

    @Override
    public void RoomHubUpdateAllSchedule(String uuid,ArrayList<Schedule> schedule_lst) {

    }

    @Override
    public void RoomHubDeleteSchedule(DeleteScheduleResPack resPack) {

    }

    @Override
    public void RoomHubNextSchedule(NextScheduleResPack resPack) {

    }

    @Override
    public void RoomHubOTAUpgradeStateChangeUpdate(DeviceFirmwareUpdateStateResPack resPack) {

    }

    @Override
    public void addDevice(RoomHubDevice device, ReasonType reason) {

    }

    @Override
    public void removeDevice(RoomHubDevice device, ReasonType reason) {

    }

    @Override
    public void updateDevice(RoomHubDevice device) {

    }

    @Override
    public void switchNetwork(boolean connected) {

    }
}
