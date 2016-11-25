package com.quantatw.sls.key;

public enum ReasonType {
    UNKNOWN,
    ALLJOYN,         //session lost
    CLOUD,           //MQTT 10200
    LOGOUT,
    NETWORKCHANGE,
    USERSHARE,       //MQTT 10600
}
