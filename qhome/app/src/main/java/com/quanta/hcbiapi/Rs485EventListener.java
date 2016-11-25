package com.quanta.hcbiapi;

/**
 * Created by qic on 2016/5/18.
 */
public interface Rs485EventListener {
    void onRs485EventReceived(Rs485Event event);
}
