package com.quanta.hcbiapi;

/**
 * Created by qic on 2016/5/18.
 */
public enum Rs485Event {
    GasAlarm ("gas alarm"),
    DoorPhone ("door phone"),
    HelpAlarm ("help alarm"),
    DoorOpen ("door opened"),
    None ("No events");

    private String event;

    Rs485Event(String event) {
        this.event = event;
    }

    public String toString() {
        return event;
    }
}
