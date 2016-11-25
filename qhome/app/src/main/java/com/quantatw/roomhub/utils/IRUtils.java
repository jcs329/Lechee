package com.quantatw.roomhub.utils;

import com.quantatw.sls.alljoyn.RoomHubInterface;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by cherry on 2016/6/22.
 */
public class IRUtils {
    public static int getIRS1(byte[] data) {
        byte[] target = new byte[2];
        target[1] = data[2];
        target[0] = data[3];
        ByteBuffer converter = ByteBuffer.wrap(target);
        converter.order(ByteOrder.nativeOrder());
        return converter.getShort(0);
    }

    public static int getIRS0(byte[] data) {
        byte[] target = new byte[2];
        target[1] = data[0];
        target[0] = data[1];
        ByteBuffer converter = ByteBuffer.wrap(target);
        converter.order(ByteOrder.nativeOrder());
        return converter.getShort(0);
    }

    public static String getIRS3(byte[] data) {
        byte[] target = new byte[16];
        byte[] source = new byte[16];

        System.arraycopy(data, 6, source, 0, 16);
        for (int i = 0, j = 0; i < target.length; i++, j++) {
            target[i] = source[j];
        }

        return byteArrayToHex(target);
    }

    private static String byteArrayToHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }

    public static RoomHubInterface.irData_y[] hexStringToByteArray(String s) {
        int len = s.length();
        RoomHubInterface.irData_y[] data = new RoomHubInterface.irData_y[len / 2];
        for (int i = 0; i < len; i += 2) {
            RoomHubInterface.irData_y bdata = new RoomHubInterface.irData_y();
            bdata.data = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
            data[i/2] = bdata;
        }
        return data;
    }

    public static byte[] irLearningResult_hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] getIRDataFromResPack(RoomHubInterface.irData_y[] data) {
        byte[] result = new byte[data.length];
        for(int i=0;i<data.length;i++) {
            result[i]=data[i].data;
        }
        return result;
    }

}
