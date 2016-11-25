package com.quantatw.myapplication;

public class EventObject {
    
	public String  index;
	public String  actionName;
	public String  actionType;
	public String  actionDirection;
	public String  actionResult;
	public String  timestamp; 
    
    public EventObject(String index, String actionName, String actionType, String actionDirection, String actionResult, String timestamp) {
    	this.index = index;
    	this.actionName = actionName;
    	this.actionType = actionType;
    	this.actionDirection = actionDirection;
    	this.actionResult =actionResult;
    	this.timestamp = timestamp;
    }
}
