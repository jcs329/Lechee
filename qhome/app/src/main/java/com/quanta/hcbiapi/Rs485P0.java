package com.quanta.hcbiapi;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by qic on 2016/4/28.
 */
public class Rs485P0 extends Tty{

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final String TAG = "Rs485P0";
    private Timer mTimer;
    private final byte[] CMD_QUERY = new byte[]{0x0f, 0x30, 0x30, 0x36, 0x46, 0x0d};
    private final String CMD_QUERY_GPI = "$016\r";
    private final byte[] REP_FIRE_ALARM = new byte[]{0x0f, 0x36, 0x31, 0x37, 0x36, 0x0d};
    private final byte[] REP_DOOR_PHONE = new byte[]{0x0f, 0x30, 0x37, 0x37, 0x36, 0x0d};
    private final byte[] REP_OK = new byte[]{0x0f, 0x30, 0x30, 0x36, 0x46, 0x0d};
    Adam4055.Level gpo_state[];
    private static boolean query_gpi = true;
    private final int REP_DOOR_CLOSED = 1; //GPI[0] = 1
    private final int REP_HELP_ALARM = 4; //GPI[2] = 1

//    public enum Rs485Event {
//        GasAlarm ("gas alarm"),
//        DoorPhone ("door phone");
//
//        private String event;
//
//        Rs485Event(String event) {
//            this.event = event;
//        }
//
//        public String toString() {
//            return event;
//        }
//
//    }

    private static Rs485P0 ourInstance;
    static {
        try {
            ourInstance = new Rs485P0();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static List<Rs485EventListener> rs485EventListenerList = new ArrayList<Rs485EventListener>();
    private boolean bHelpAlarm = true;
    private boolean bDoorOpened = true;

//    public static interface Rs485EventListener {
//        void onRs485EventReceived(Rs485Event event);
//    }

    public static void addRs485EventListener(Rs485EventListener listener) {
        if (!rs485EventListenerList.contains(listener)) {
            rs485EventListenerList.add(listener);
        }
    }
    public static void removeRs485Listener(Rs485EventListener listener) {
        rs485EventListenerList.remove(listener);
    }

    public static Rs485P0 getInstance() {
        return ourInstance;
    }

    private Rs485P0() throws IOException {
        super("/dev/ttyUSB0", BaudRate.BPS9600, 0);
        gpo_state = new Adam4055.Level[8];
        for(int i = 0; i < gpo_state.length; i++) {
            gpo_state[i] = Adam4055.Level.LOW;
        }
        
        new Thread(new Runnable() {
            byte[] data = new byte[1];
            byte[] buffer = new byte[16];
            int position = 0;
            Rs485Event ev;
            public void run() {
                while(true) {
                    String str = "";
                    try {
                        read(data);
                        buffer[position] = data[0];
                        position++;
                        ev = Rs485Event.None;
                        if ((data[0] == 0x0d) || (position == 15)) {
                            byte[] reply = Arrays.copyOfRange(buffer, 0, position);
                            position = 0;
                            Log.i(TAG, "run: RS485 reply: " + bytesToHex(reply));

                            if (bytesToHex(reply).equals(bytesToHex(REP_FIRE_ALARM))) {
                                ev = Rs485Event.GasAlarm;
                            } else if (bytesToHex(reply).equals(bytesToHex(REP_DOOR_PHONE))) {
                                ev = Rs485Event.DoorPhone;
                            } else if (reply[0] == 0x21) {
                                byte gpi_data[] = new byte[]{reply[3], reply[4]};
                                str = new String(gpi_data);

                                int gpi_value = Integer.parseInt(str, 16);
                                if (((gpi_value & REP_DOOR_CLOSED) == REP_DOOR_CLOSED)) {
                                    bDoorOpened = false;
                                } else {
                                    //Only trigger a "door was opened" event if a door was closed last time
                                    if (!bDoorOpened) {
                                        ev = Rs485Event.DoorOpen;
                                    }
                                    bDoorOpened = true;
                                }

                                if ((gpi_value & REP_HELP_ALARM) == REP_HELP_ALARM) {
                                    //Only trigger a help event if last query is not help On
                                    if (!bHelpAlarm) {
                                        ev = Rs485Event.HelpAlarm;
                                    }
                                    bHelpAlarm = true;
                                } else {
                                    bHelpAlarm = false;
                                }

                            }
                            if (ev != Rs485Event.None) {
                                Log.i(TAG, "run: Event: [" + ev.toString() + "] is triggered");
                                for (Rs485EventListener l : rs485EventListenerList) {
                                    l.onRs485EventReceived(ev);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NumberFormatException nfe) {
                        Log.d(TAG, "NumberFormatException: string:" + str);
                    }
                }
            }
        }).start();
    }

    @Override
    public void write(byte[] buf) throws IOException {
        super.write(buf);
    }

    @Override
    protected int read(byte[] buf) throws IOException {
        return super.read(buf);
    }

    public void startPollingRS485() {

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                try {
                    if (query_gpi)
                        write(CMD_QUERY_GPI.getBytes());
                    else
                        write(CMD_QUERY);

                    query_gpi = !query_gpi;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        if(mTimer == null) {
            mTimer = new Timer("Query RS485 scheduler");
            mTimer.schedule(task, 1000, 1000);
        }
    }

    public void stopPollingRS485() {
        if(mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void setGpo(Adam4055.Gpo gpo, Adam4055.Level level) {

        gpo_state[gpo.getVal()] = level;

        int all_gpo = 0;

        for(int i = 0; i < gpo_state.length; i++)
        {
            all_gpo |= gpo_state[i].getVal()  << i;
        }

        String str = "#0100"+ String.format("%02x", all_gpo) + "\r";

        str = str.toUpperCase();
        try {
            write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
