package com.quantatw.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by qic on 2016/5/4.
 */
public class CustomAdapter extends BaseAdapter {
    String [] result;
    String [] numbers;
    Context context;
    int [] imageId;
    private static LayoutInflater inflater=null;
    public CustomAdapter(ContactBookActivity mainActivity, String[] prgmNameList, String[] prgmNumberList, int[] prgmImages) {
        // TODO Auto-generated constructor stub
        result=prgmNameList;
        numbers=prgmNumberList;
        context=mainActivity;
        imageId=prgmImages;
        inflater = (LayoutInflater)context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
        TextView numberOrAddress;
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.contact_list, null);
        holder.tv=(TextView) rowView.findViewById(R.id.textView1);
        holder.numberOrAddress=(TextView) rowView.findViewById(R.id.textView2);
        holder.img=(ImageView) rowView.findViewById(R.id.imageView1);
        holder.tv.setText(result[position]);
        holder.numberOrAddress.setText(numbers[position]);
        holder.img.setImageResource(imageId[0]);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Toast.makeText(context, "You Clicked "+result[position], Toast.LENGTH_LONG).show();
                ContactBookActivity mainActivity = (ContactBookActivity) context;
                mainActivity.setContactIndex(position);
            }
        });

        /*android.widget.Button delContact = (android.widget.Button) rowView.findViewById(R.id.btnDelContact);
        delContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, "You Clicked "+result[position], Toast.LENGTH_LONG).show();
                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(context).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Are you sure to delete this item ?");
                alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                ContactBookActivity mainActivity = (ContactBookActivity) context;
                                mainActivity.deleteData(result[position]);
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });*/

        android.widget.ImageView delContactView = (android.widget.ImageView) rowView.findViewById(R.id.ivDelContact);
        delContactView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //actions for remove contact
                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(context).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Are you sure to delete this item ?");
                alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                ContactBookActivity mainActivity = (ContactBookActivity) context;
                                mainActivity.deleteData(result[position], position);
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }

        });
        return rowView;
    }

}
