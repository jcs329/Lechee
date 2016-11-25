package com.quantatw.myapplication.information_delivery;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.MainActivity;
import com.quantatw.myapplication.R;
import com.quantatw.myapplication.information_delivery.infoViewCustomList;
import com.quantatw.myapplication.information_delivery.sqlite.DBHelper;

public class infoView extends Fragment implements OnClickListener,OnLongClickListener{
    RelativeLayout infoItems[] = new RelativeLayout[4];
    //Button[] buttons = new Button[4];
    TextView perunread,commperunread,foodperunread,countryperunread;
    final String TAG = "infoView1";
    public DBHelper dbhelper = null;
    ListView infolistview;
    static boolean allowRefrash = false;
    static View Vid = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "fragment create");
        View view = inflater.inflate(R.layout.fragment_info_view, container, false);

        return view;
    }

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		int personunread;
		infolistview = (ListView) getActivity().findViewById(R.id.infolistvliew);
        String[] id,lists,urls,mailto,mailfrom,unread,URL;
        infoViewCustomList adapter;
        dbhelper = new DBHelper(this.getActivity());
		
        super.onActivityCreated(savedInstanceState);
        infoItems[0] =(RelativeLayout) getActivity().findViewById(R.id.button1);
        infoItems[1] =(RelativeLayout) getActivity().findViewById(R.id.button2);
        infoItems[2] =(RelativeLayout) getActivity().findViewById(R.id.button3);
        infoItems[3] =(RelativeLayout) getActivity().findViewById(R.id.button4);

        for (RelativeLayout infoItem : infoItems){
                infoItem.setOnClickListener(this);
        }

        for (RelativeLayout infoItem : infoItems){
            infoItem.setOnLongClickListener(this);
        }

        perunread = (TextView) getActivity().findViewById(R.id.infounreadperson);
        perunread.setTextSize(32);
        personunread=dbhelper.getTablePartialUnreadCount("infoperson");
        if (personunread != 0)
            perunread.setText(Integer.toString(personunread));
        commperunread = (TextView) getActivity().findViewById(R.id.infounreadcomm);
        commperunread.setTextSize(32);

        foodperunread = (TextView) getActivity().findViewById(R.id.infounreadfood);
        foodperunread.setTextSize(32);

        countryperunread = (TextView) getActivity().findViewById(R.id.infounreadcountry);
        countryperunread.setTextSize(32);

    }


    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG,"start android.intent.action.CART");
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CART");
        broadcastManager.registerReceiver(perInfo,intentFilter);
    }
    @Override
    public void onStop(){
        Log.d(TAG,"onStop android.intent.action.CART");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(perInfo);
        super.onStop();
    }

    private final BroadcastReceiver perInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "fragment BroadcastReceiver");
            if (intent.getAction().equals("android.intent.action.CART")) {
                //String infocomm = intent.getStringExtra("infocomm");
                String infoperson = intent.getStringExtra("infoperson");
                //String infofood = intent.getStringExtra("infofood");
                //String infocountry = intent.getStringExtra("infocountry");
                //if (Integer.parseInt(infocomm) != 0)
                //    commperunread.setText(infocomm);
                if (Integer.parseInt(infoperson) != 0)
                    perunread.setText(infoperson);
                //if (Integer.parseInt(infofood) != 0)
                 //   foodperunread.setText(infofood);
               // if (Integer.parseInt(infocountry) != 0)
                 //   countryperunread.setText(infocountry);
                if (Vid != null)
                    refeshonclick(Vid);
            }
        }
    };

    @Override
    public void onClick(View V){
        /*SQL output*/
        Vid = V;
        infolistview = (ListView) getActivity().findViewById(R.id.infolistvliew);
        ArrayAdapter listAdapter;
        String[] id,lists,urls,mailto,mailfrom,unread,URL;
        infoViewCustomList adapter;
        dbhelper = new DBHelper(this.getActivity());
        Log.d(TAG, "fragment onclick");
        switch  (V.getId()){
            case R.id.button1:
                commperunread.setText("");
                Toast.makeText(getActivity(), "Community", Toast.LENGTH_SHORT).show();
                infolistview.setAdapter(null);
                break;
            case R.id.button2:
                int personunread=dbhelper.getTablePartialUnreadCount("infoperson");
                if (personunread != 0)
                    perunread.setText(Integer.toString(personunread));
                else
                    perunread.setText("");
                Toast.makeText(getActivity(), "Personal", Toast.LENGTH_SHORT).show();
                id =  dbhelper.get2DimData(MainActivity.infoPerson).get(0).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(0).size()]);
                lists =  dbhelper.get2DimData(MainActivity.infoPerson).get(1).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(1).size()]);
                urls = dbhelper.get2DimData(MainActivity.infoPerson).get(2).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(2).size()]);
				mailto = dbhelper.get2DimData(MainActivity.infoPerson).get(3).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(3).size()]);
                mailfrom = dbhelper.get2DimData(MainActivity.infoPerson).get(4).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(4).size()]);
                unread = dbhelper.get2DimData(MainActivity.infoPerson).get(5).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(5).size()]);
                URL = dbhelper.get2DimData(MainActivity.infoPerson).get(6).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(6).size()]);
                adapter = new infoViewCustomList(getActivity(),id,lists,urls,mailto,mailfrom,unread,URL);
                infolistview.setAdapter(adapter);
                break;
            case R.id.button3:
                foodperunread.setText("");
                Toast.makeText(getActivity(), "Food", Toast.LENGTH_SHORT).show();
                infolistview.setAdapter(null);
                break;
            case R.id.button4:
                countryperunread.setText("");
                Toast.makeText(getActivity(), "Local Area", Toast.LENGTH_SHORT).show();
                infolistview.setAdapter(null);
                break;

        }
        dbhelper.close();
    }

    @Override
    public boolean onLongClick(View V){
      Log.d(TAG,"fragment LongClick");

        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (allowRefrash){
            Log.d("Wei","allowRefrash");
            allowRefrash = false;
            infoView fr = new infoView();
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fraglinear, fr);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            if (Vid != null)
                refeshonclick(Vid);
        }else
            Log.d("Wei","notallowRefrash");
    }

    public void setallowRefresh(boolean Refresh){
        this.allowRefrash = Refresh;
    }

    public void refeshonclick(View V){
        infolistview = (ListView) getActivity().findViewById(R.id.infolistvliew);
        ArrayAdapter listAdapter;
        String[] id,lists,urls,mailto,mailfrom,unread,URL;
        infoViewCustomList adapter;
        dbhelper = new DBHelper(this.getActivity());
        Log.d(TAG, "fragment allowRefrash");
        switch  (V.getId()){
            case R.id.button1:
                commperunread.setText("");
                infolistview.setAdapter(null);
                break;
            case R.id.button2:
                //perunread.setText("");
                int personunread=dbhelper.getTablePartialUnreadCount("infoperson");
                if (personunread != 0)
                    perunread.setText(Integer.toString(personunread));
                else
                    perunread.setText("");
                id =  dbhelper.get2DimData(MainActivity.infoPerson).get(0).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(0).size()]);
                lists =  dbhelper.get2DimData(MainActivity.infoPerson).get(1).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(1).size()]);
                urls = dbhelper.get2DimData(MainActivity.infoPerson).get(2).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(2).size()]);
                mailto = dbhelper.get2DimData(MainActivity.infoPerson).get(3).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(3).size()]);
                mailfrom = dbhelper.get2DimData(MainActivity.infoPerson).get(4).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(4).size()]);
                unread = dbhelper.get2DimData(MainActivity.infoPerson).get(5).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(5).size()]);
                URL = dbhelper.get2DimData(MainActivity.infoPerson).get(6).toArray(new String[dbhelper.get2DimData(MainActivity.infoPerson).get(6).size()]);
                adapter = new infoViewCustomList(getActivity(),id,lists,urls,mailto,mailfrom,unread,URL);
                infolistview.setAdapter(adapter);
                break;
            case R.id.button3:
                foodperunread.setText("");
                infolistview.setAdapter(null);
                break;
            case R.id.button4:
                countryperunread.setText("");
                infolistview.setAdapter(null);
                break;

        }
        dbhelper.close();
    }
}

