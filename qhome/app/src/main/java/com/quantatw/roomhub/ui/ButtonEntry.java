package com.quantatw.roomhub.ui;

public class ButtonEntry {
    int testButton1Key;
    int testButton2Key;
    int actionResID;
    int button1ResID = 0;
    int button2ResID = 0;
    boolean button1Selected = false;
    boolean button2Selected = false;
    boolean enable = true;

    public ButtonEntry(int testButton1Key,int testButton2Key, int actionResID, int button1ResID, int button2ResID, boolean enable) {
        this.testButton1Key = testButton1Key;
        this.testButton2Key = testButton2Key;
        this.actionResID = actionResID;
        this.button1ResID = button1ResID;
        this.button2ResID = button2ResID;
        this.enable = enable;
    }

    public ButtonEntry(int testButton1Key, int actionResID, int button1ResID, boolean enable) {
        this.testButton1Key = testButton1Key;
        this.actionResID = actionResID;
        this.button1ResID = button1ResID;
        this.enable = enable;
    }

}