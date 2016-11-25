package com.quantatw.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ContactBookActivity extends Activity {
    ListView lv;
    Context context;
    ArrayList prgmName;
    private Handler mHandler = new Handler();
    public static int [] prgmImages={R.drawable.contact_icon,R.drawable.contact_icon,R.drawable.contact_icon,R.drawable.contact_icon,R.drawable.contact_icon,R.drawable.contact_icon,R.drawable.contact_icon,R.drawable.contact_icon,R.drawable.contact_icon};
    public static String [] prgmNameList={"Let Us C","c++","JAVA","Jsp","Microsoft .Net","Android","PHP","Jquery","JavaScript"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_book);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = 800;
        params.width = 1000;

        this.getWindow().setAttributes(params);


        context=this;

        String contacts = MainActivity.instance().getAllContactText();
        List<String> staff = new ArrayList<String>();
        List<String> numberOrAddress = new ArrayList<String>();

        String[] afterSplit = contacts.split(";");
        for(int i = 0; i < afterSplit.length; i++) {
            String[] staff_ = afterSplit[i].split(",");
            if(staff_[0].length() > 0) {
                staff.add(staff_[0]);
                numberOrAddress.add(staff_[1]);
            }
            //Toast.makeText(ContactBookActivity.this, staff[0], Toast.LENGTH_LONG).show();
        }

        /*Button addContact = (Button) findViewById(R.id.btnAddContact);
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayContactDialog("New contact", "Please input name (e.g. lechee.Lai) and number..", false);
            }
        });*/
        ImageView addContactView = (ImageView) findViewById(R.id.ivAddContact);
        addContactView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //actions for add contact
                displayContactDialog("New contact", "Please input name and number.", false);
            }

        });

        lv=(ListView) findViewById(R.id.listView);
        if(staff.size() > 0)
            lv.setAdapter(new CustomAdapter(this,
                    staff.toArray(new String[0]), numberOrAddress.toArray(new String[0]),prgmImages));

    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    public void addData(String name1, String name2, String number) {
        MainActivity.instance().addContact(name1, name2, number);
        MainActivity.instance().getContacts();
        String contacts = MainActivity.instance().getAllContactText();
        List<String> staff = new ArrayList<String>();
        List<String> numberOrAddress = new ArrayList<String>();

        String[] afterSplit = contacts.split(";");
        for(int i = 0; i < afterSplit.length; i++) {
            String[] staff_ = afterSplit[i].split(",");
            if(staff_[0].length() > 0) {
                staff.add(staff_[0]);
                numberOrAddress.add(staff_[1]);
            }
        }
        if(name2.length() > 0)
            staff.add(name1 + " " + name2);
        else
            staff.add(name1);
        numberOrAddress.add(number);

        lv.setAdapter(new CustomAdapter(this, staff.toArray(new String[0]), numberOrAddress.toArray(new String[0]),prgmImages));
    }

    public void deleteData(String oldStaff, int idx) {
        MainActivity.instance().deleteContact(oldStaff);
        MainActivity.instance().getContacts();
        String contacts = MainActivity.instance().getAllContactText();
        List<String> staff = new ArrayList<String>();
        List<String> numberOrAddress = new ArrayList<String>();

        String[] afterSplit = contacts.split(";");
        for(int i = 0; i < afterSplit.length; i++) {
            String[] staff_ = afterSplit[i].split(",");
            staff.add(staff_[0]);
            numberOrAddress.add(staff_[1]);
        }
        staff.remove(idx);
        numberOrAddress.remove(idx);

        lv.setAdapter(new CustomAdapter(this, staff.toArray(new String[0]), numberOrAddress.toArray(new String[0]),prgmImages));
    }

    public void setContactIndex(int index) {
        MainActivity.instance().setContactIndex(index);
        finish();
    }

    public void displayContactDialog(final String title,final String msg, final boolean needBack) {
        mHandler.post(new Runnable() {
            @Override
            public void run(){
                AlertDialog.Builder builder = new AlertDialog.Builder(ContactBookActivity.this);
                builder.setTitle(title);
                builder.setMessage(msg);
                android.widget.LinearLayout layout = new android.widget.LinearLayout(ContactBookActivity.this.context);
                layout.setOrientation(android.widget.LinearLayout.VERTICAL);
                final EditText input = new EditText(ContactBookActivity.this);
                final EditText input2 = new EditText(ContactBookActivity.this);
                input.setHint("First name and last name (e.g. lechee lai) ..");
                input2.setHint("Phone number or address ..");
                layout.addView(input);
                layout.addView(input2);
                input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                builder.setView(layout);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
				      /*
					  Intent intent = new Intent(InCallActivity.this, LinphoneActivity.class);
				      intent.putExtra("AddCall", true);
				      intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				      startActivity(intent);
				      */
                    }
                });
                if(needBack) {
                    builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //dialog.dismiss();
                            //pauseOrResumeCall();
                        }
                    });
                } else {
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                }

                final Dialog msgDialog = builder.create();
                msgDialog.show();
                ((AlertDialog) msgDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String name = input.getText().toString();
                        String number = input2.getText().toString();
                        String[] myName = name.split("\\.|\\s+");
                        if(myName.length > 0 && number.length() > 0) {
                            //if(myName.length == 2)
                            //    MainActivity.instance().addContact(myName[0], myName[1], number);
                            //if(myName.length == 1)
                            //    MainActivity.instance().addContact(myName[0], "", number);
                            addData(myName[0], myName.length > 1 ? myName[1]:"", number);
                            msgDialog.dismiss();
                        }
                    }
                });
            }
        });
    }
}
