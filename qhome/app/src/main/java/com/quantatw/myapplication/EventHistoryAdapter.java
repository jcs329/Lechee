package com.quantatw.myapplication;

import java.util.ArrayList;

import com.quantatw.myapplication.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

class EventHistoryAdapter extends BaseAdapter
{
    private LayoutInflater myInflater;
    private ArrayList<EventObject> eventList;

    public EventHistoryAdapter(Context c, ArrayList<EventObject> eventList)
    {
        myInflater = LayoutInflater.from(c);
        this.eventList = eventList;
    }
	
    public void setAdapterList(ArrayList<EventObject> list)
    {
    	this.eventList = eventList;
    }
   
    public void addEvent(EventObject e)
    {
        this.eventList.add(e);
    }

    public void clearAdapterList()
    {
    	eventList.removeAll(eventList);
    }

    @Override
	public int getCount() {
		return eventList.size();
	}

    @Override
    public EventObject getItem(int position) {
       return eventList.get(getCount() - position - 1);
    }
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		EventListTag imTag;
        /*
        if(convertView == null)
        {
            convertView = myInflater.inflate(R.layout.event_listview, null);
            
            imTag = new EventListTag(
            		(TextView) convertView.findViewById(
                            R.id.eventIndex),
                    (TextView) convertView.findViewById(
                            R.id.eventActionName),
                    (TextView) convertView.findViewById(
                        R.id.eventType),
                    (TextView) convertView.findViewById(
                        R.id.eventDirection),
                    (TextView) convertView.findViewById(
                        R.id.eventActionResult),   
                    (TextView)convertView.findViewById(
	                    R.id.eventTime)
                    );
            
            convertView.setTag(imTag);
        }
        else{
        	imTag = (EventListTag) convertView.getTag();
        }
        
        imTag.index.setText(eventList.get(getCount() - position - 1).index);
        imTag.actionName.setText(eventList.get(getCount() - position - 1).actionName);
        imTag.actionType.setText(eventList.get(getCount() - position - 1).actionType);
        imTag.actionDirection.setText(eventList.get(getCount() - position - 1).actionDirection);
        imTag.actionResult.setText(eventList.get(getCount() - position - 1).actionResult);
        imTag.timestamp.setText(eventList.get(getCount() - position - 1).timestamp);
*/
        return convertView;
	}
}

class EventListTag
{
    TextView  index;
    TextView  actionName;
    TextView  actionType;
    TextView  actionDirection;
    TextView  actionResult;
    TextView  timestamp;

    public EventListTag (TextView index, TextView actionName, TextView actionType, TextView actionDirection, TextView actionResult, TextView timestamp)
    {
        this.index = index;
        this.actionName = actionName;
        this.actionType = actionType;
        this.actionDirection = actionDirection;
        this.actionResult = actionResult;
        this.timestamp = timestamp;
    }
}

