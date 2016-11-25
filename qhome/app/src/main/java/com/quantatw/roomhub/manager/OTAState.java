package com.quantatw.roomhub.manager;

/**
 * Created by erin on 1/18/16.
 */
public enum OTAState {
    IDLE(0),
    ADD(1),
    GET_VERSION(2),
    READY(3),
    VERIFY(4),
    VERIFY_DONE(5),
    UPGRADE(6),
    UPGRADE_DONE(7);

    private OTAState(int value) {
        this.value = value;
    }

    private int value;

    public int getValue() {
        return value;
    }
}
