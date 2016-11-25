package com.quantatw.myapplication.information_delivery;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import android.util.Log;

import com.quantatw.myapplication.information_delivery.Package;
import com.quantatw.myapplication.R;
import com.quantatw.myapplication.information_delivery.fileio.*;
import com.quantatw.myapplication.information_delivery.view.*;
import com.quantatw.myapplication.information_delivery.sqlite.DBHelper;
/**
 * Created by root on 2016/5/20.
 */
 
public class infoViewCustomList extends ArrayAdapter<String>{

    private final Activity context;
	private final String[] id;
	private final String[] mailtotext;
	private final String[] mailfromtext;
    private final String[] infotext;
    private final String[] imageId;
	private final String[] unread;
	private final String[] URL;
	
	final String TAG = "infoViewListView";
	
    public ImageLoader imageLoader;
    public infoViewCustomList(Activity context,String[] id,
                      String[] web, String[] imageId, String[] mailto,String[] mailfrom,String[] unread, String[] URL) {
        super(context, R.layout.fragment_info_listview, web);
        this.context = context;
		this.id=id;
		this.mailtotext=mailto;
        this.infotext = web;
        this.imageId = imageId;
		this.mailfromtext=mailfrom;
		this.unread=unread;
		this.URL=URL;
        imageLoader=new ImageLoader(context.getApplicationContext());
    }
	
    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.fragment_info_listview, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.listtext);
		TextView txtMailTo = (TextView) rowView.findViewById(R.id.listtextmailto);
		
        ImageView imageView = (ImageView) rowView.findViewById(R.id.listimage);
		String receive_time=infotext[position];
		
		//receive_time=receive_time.substring(0,10) + "   " +receive_time.substring(11,19);
		
        txtTitle.setText(" "+receive_time);
		txtMailTo.setText(" To: " +mailtotext[position]);
		
		if(unread[position].equals("1")){
		TextView txtRead = (TextView) rowView.findViewById(R.id.listread);
		txtRead.setVisibility(View.VISIBLE);
		}	
        
		//imageView.setImageResource(imageId[position]);
        imageLoader.QuickDisplayImage(imageId[position], imageView);
		
        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				/* change database */
				DBHelper dbhelper = new DBHelper(context);
				dbhelper.update(id[position]);
				
                Intent intent = new Intent(getContext(), Package.class);
				
				Bundle bundle = new Bundle();
				
				bundle.putString("mailto",mailtotext[position]);
				bundle.putString("mailfrom",mailfromtext[position]);
				bundle.putString("mailtoimage",URL[position]);

				intent.putExtras(bundle);

                getContext().startActivity(intent);

				
                ((Activity)getContext()).overridePendingTransition(0, 0);
            }
        });
		
		
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				/* change database */
				DBHelper dbhelper = new DBHelper(context);
				dbhelper.update(id[position]);
				
                Intent intent = new Intent(getContext(), Package.class);
				
				Bundle bundle = new Bundle();
				
				bundle.putString("mailto",mailtotext[position]);
				bundle.putString("mailfrom",mailfromtext[position]);
				bundle.putString("mailtoimage",URL[position]);

				intent.putExtras(bundle);

                getContext().startActivity(intent);
				
                ((Activity)getContext()).overridePendingTransition(0, 0);
            }
        });
		
        return rowView;
    }
}
