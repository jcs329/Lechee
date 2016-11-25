package org.linphone;
/*
DialerFragment.java
Copyright (C) 2012  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
import org.linphone.core.LinphoneCore;
import org.linphone.mediastream.Log;
import org.linphone.ui.AddressAware;
import org.linphone.ui.AddressText;
import org.linphone.ui.CallButton;
import org.linphone.ui.EraseButton;
import org.linphone.QuEws;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Sylvain Berfini
 */
public class DialerFragment extends Fragment {
	private static DialerFragment instance;
	private static boolean isCallTransferOngoing = false;
	
	public boolean mVisible;
	private boolean mTryAgain = false; // LiangBin add, 20150211
	private ArrayList<QueryItem> mStaffArr; // LiangBin add, 20150211
	private AddressText mAddress;
	private CallButton mCall;
	private ImageView mAddContact;
    private ImageButton btnLdapQuery;
    private String popUpContents[];
    private String mPhoneNo[];
    private String mMailAddr[];
    private int mSelPosition;
    private PopupWindow popupWindowDogs;
	private OnClickListener addContactListener, cancelListener, transferListener, queryLdapListener;
	private boolean shouldEmptyAddressField = true;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
		instance = this;
        View view = inflater.inflate(R.layout.dialer, container, false);
		
		mAddress = (AddressText) view.findViewById(R.id.Adress); 
		mAddress.setDialerFragment(this);
		
		EraseButton erase = (EraseButton) view.findViewById(R.id.Erase);
		erase.setAddressWidget(mAddress);
		
		btnLdapQuery = (ImageButton) view.findViewById(R.id.btnquery);
		
		mCall = (CallButton) view.findViewById(R.id.Call);
		mCall.setAddressWidget(mAddress);
		if (LinphoneActivity.isInstanciated() && LinphoneManager.getLc().getCallsNb() > 0) {
			if (isCallTransferOngoing) {
				mCall.setImageResource(R.drawable.transfer_call);
			} else {
				mCall.setImageResource(R.drawable.add_call);
			}
		} else {
			mCall.setImageResource(R.drawable.call);
		}
		
		AddressAware numpad = (AddressAware) view.findViewById(R.id.Dialer);
		if (numpad != null) {
			numpad.setAddressWidget(mAddress);
		}
		
		mAddContact = (ImageView) view.findViewById(R.id.addContact);
		
		addContactListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				LinphoneActivity.instance().displayContactsForEdition(mAddress.getText().toString());
			}
		};
		cancelListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				LinphoneActivity.instance().resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
			}
		};
		
		//<<< LiangBin add, for LDAP query
        queryLdapListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
            	String tt = mAddress.getText().toString(); // LiangBin add, 20150115
                if(tt.length() == 0) {
                	/*
                    Dialog dialog = new AlertDialog.Builder(getActivity())
                            .setTitle("Hint").setMessage("Please input contact's name") 
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() { 

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //SimpleDialogActivity.this.finish();
                                }
                            }).create();/**/
                    final EditText input = new EditText(getActivity());
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Need contact name:")
                            //.setMessage("Please input keyword")
                            .setView(input)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //input.getText();
                                    mAddress.setText(input.getText());
                                    if(input.getText().length() > 0)
                                    	btnLdapQuery.performClick();
                                }
                            }).setNegativeButton("Cancel", null)
                            .show();

                    //dialog.show(); 
                    return;
                }
                else {
                	//<<< LiangBin add, for EWS query
                	if(mTryAgain == false) {
	                    new Thread(new Runnable(){
	                        public void run(){
	                        	LinphonePreferences mPrefs = LinphonePreferences.instance();
	                            String id = mPrefs.getRmsid();//mStorage.getRMSUsername();
	                            String pw = mPrefs.getRmspw(); //mStorage.getRMSPassword();
	                            String server = "mail.quantatw.com";//getString(R.string.ews_server);
	                            String requestAbsoluteUrl = "https://mail.quantatw.com/EWS/Exchange.asmx";//getString(R.string.ews_requestabsoluteurl);
	                            String queryString = mAddress.getText().toString();//mEtDialPadInput.getText().toString();
	                            String result = QuEws.query(id,pw,server,requestAbsoluteUrl,queryString);
	                            android.util.Log.v("LOG_TAG","[EWS] query result="+ result );
	                            Bundle temp = new Bundle();
	                            //temp.putSerializable("ldap", 1);
	                            Message message = new Message();   
	                            temp.putString("ldap", result);
	                            message.setData(temp);
	                            mHandler.sendMessage(message);
	                            }
	                    }).start();/**/
                	}
                    //>>> LiangBin add, 20150210
                	else {
		                new Thread(new Runnable(){
		                    public void run(){
		                        String result = "";
		                        String addr = "http://sip.vccloud.quantatw.com/ios/test_ldap_query.php?id=hans";
		                        //<<< LiangBin add, Split keyword
		                        String tt = mAddress.getText().toString();
		                        if(tt !=null && tt.length() > 1) {
		                            String[] tKey = tt.split("[, +]+");
		                            addr = addr.replace("hans", tKey[0]);
		                            if(tKey.length == 2) {
		                                addr += "&sn=";
		                                addr += tKey[1];
		                            }
		                        }
		                        //>>> LiangBin add, 20150115
		                        try {
		                            URL url = new URL(addr);
		                            URLConnection con = url.openConnection();
		                            // activate the output
		                            con.setDoOutput(true);
		                            PrintStream ps = new PrintStream(con.getOutputStream());
		                            LinphonePreferences mPrefs = LinphonePreferences.instance();
		                            ps.print("firstKey=" + mPrefs.getRmsid());
		                            ps.print("&secondKey=" + mPrefs.getRmspw());
		                            //con.getInputStream();
		                            //ps.close();
		                            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "BIG5")); // LiangBin add, set BIG5 character, 20150116
		                            String strTemp = "";
		                            while (null != (strTemp = br.readLine())) {
		                                System.out.println(strTemp);
		                                result += strTemp;
		                            }
		                        } catch (Exception ex) {
		                            ex.printStackTrace();
		                        }
		
		                        //mHandler.sendEmptyMessage(1);
		                        Message message = new Message();
		                        //message.obj = result;
		                        Bundle temp = new Bundle();
		                        temp.putString("ldap", result);
		                        message.setData(temp);
		                        mHandler.sendMessage(message);
		                    }
		                }).start();
                	}
                }
                //popupWindowDogs.showAsDropDown(v, -5, 0);
                //LinphoneActivity.instance().displayContactsForEdition(mAddress.getText().toString());
            }
        };
		//>>> LiangBin add, 20150114
		
		transferListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				LinphoneCore lc = LinphoneManager.getLc();
				if (lc.getCurrentCall() == null) {
					return;
				}
				lc.transferCall(lc.getCurrentCall(), mAddress.getText().toString());
				isCallTransferOngoing = false;
				LinphoneActivity.instance().resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
			}
		};
		
		mAddContact.setEnabled(!(LinphoneActivity.isInstanciated() && LinphoneManager.getLc().getCallsNb() > 0));
		resetLayout(isCallTransferOngoing);
		
		if (getArguments() != null) {
			shouldEmptyAddressField = false;
			String number = getArguments().getString("SipUri");
			String displayName = getArguments().getString("DisplayName");
			String photo = getArguments().getString("PhotoUri");
			mAddress.setText(number);
			if (displayName != null) {
				mAddress.setDisplayedName(displayName);
			}
			if (photo != null) {
				mAddress.setPictureUri(Uri.parse(photo));
			}
		}
		
		return view;
    }

	//<<< LiangBin add, for LDAP query
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            //Bundle temp = msg.getData();
            String data = msg.getData().getString("ldap");
            if(data == null) return;
            if(data.length() == 0) {
                Toast.makeText(getActivity(), "No more data, please try it again.",Toast.LENGTH_LONG).show();
                //return;
            }
            else if(data == "500") {
                Toast.makeText(getActivity(), "result: 500, please try it again.",Toast.LENGTH_LONG).show();
                mTryAgain = true;
                btnLdapQuery.performClick();
                //return;
            }
            else {
            	List<String> dogsList = new ArrayList<String>();
            	if(mTryAgain == false) {
            		mStaffArr = parseEws(data);
            		int _size = mStaffArr.size();
            		if(_size > 30) {
            			mTryAgain = true;
                        btnLdapQuery.performClick();
                        return;
            		}
            		for(int i = 0; i < _size; i++) {
            			String staff = String.format("%s::%d", mStaffArr.get(i).queryName, i+1);
            			dogsList.add(staff);
            		}
            	} else {
                //List<String> dogsList = new ArrayList<String>();
                //dogsList.add("Akita Inu::1");
            		String[] AfterSplit = data.split(";");
            		int i = 1;
            		for(String token: AfterSplit){
            			String[] AgainSplit = token.split(",");
            			String staff = String.format("%s::%d", AgainSplit[0], i);
            			if( i == 1)
            				staff = String.format("%s::%d", AgainSplit[0].substring(4), i);
            			dogsList.add(staff);
            			i = i + 1;
            		}
            	}
                // convert to simple array
                popUpContents = new String[dogsList.size()];
                dogsList.toArray(popUpContents);

                // initialize pop up window
                popupWindowDogs = popupWindowDogs(data);
                popupWindowDogs.showAsDropDown(getView().findViewById(R.id.btnquery), -5, 0);
               // errorHandlerHere();//could be your toasts or any other error handling...
                mTryAgain = false;
            }
        }
    };

    public PopupWindow popupWindowDogs(String info) {

        // initialize a pop up window type
        final PopupWindow popupWindow = new PopupWindow(getActivity());

        // the drop down list is a list view
        final ListView listViewDogs = new ListView(getActivity());

        // set our adapter and pass our pop up window contents
        listViewDogs.setAdapter(dogsAdapter(popUpContents));
        /*
    	listViewDogs.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                //Toast.makeText(arg0.getContext(), ((TextView)arg1).getText(), Toast.LENGTH_SHORT).show();
            	PopupMenu popup = new PopupMenu(getActivity(), getView());
            	popup.getMenuInflater().inflate(R.menu.main, popup.getMenu());
            	popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						// TODO Auto-generated method stub
						if (item.getTitle().toString().equals("MVPN call")) {
                        	System.out.println("Hello");
                        	Toast.makeText(getActivity(), "Hello", Toast.LENGTH_SHORT).show();
                            //return true;
                        }
						return false;
					}
                });
            	popup.show();
                return false;
            }
        });/**/

        if(mTryAgain) {
        	// LDAP style
	        String[] AfterSplit = info.split(";");
	        this.mPhoneNo = new String[AfterSplit.length];
	        this.mMailAddr = new String[AfterSplit.length];
	        int i = 0;
	        for(String token: AfterSplit){
	            String[] AgainSplit = token.split(",");
	            if(AgainSplit.length > 2 && AgainSplit[2].length() > 5)
	            	this.mPhoneNo[i] = AgainSplit[2].substring(5);
	            if(AgainSplit.length > 3)
	            	this.mMailAddr[i] = AgainSplit[3];
	            i = i + 1;
	        }
        } 
        else {
         // EWS style
        	int len = mStaffArr.size();
            this.mPhoneNo = new String[len];
            this.mMailAddr = new String[len];
        	for(int i = 0; i < len; i++)
        	{
        		this.mPhoneNo[i] = mStaffArr.get(i).getMVPN();
        		this.mMailAddr[i] = mStaffArr.get(i).getSIPurl();
        	}
        }
        // set the item click listener
        listViewDogs.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
            	mSelPosition = position; // LiangBin add, 20150217
                //AddressText mAddress = (AddressText) view.findViewById(R.id.Adress);
            	final CharSequence[] items = {"MVPN call", "SIP call", "Save to contact"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(items, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int item) {
                    	 if(item == 0) {
                    		 mAddress.setText(mPhoneNo[mSelPosition]);
                    		 LinphoneManager.getInstance().newOutgoingCall(mAddress);
                    	 }
                    	 else if(item == 1) {
                    		 mAddress.setText(mMailAddr[mSelPosition].substring(0, mMailAddr[mSelPosition].indexOf('@')));
                    		 //mAddress.setText(mMailAddr[mSelPosition]);
                    		 LinphoneManager.getInstance().newOutgoingCall(mAddress);
                    	 }
                    	 else {
                    		 //Toast.makeText(getActivity().getApplicationContext(), "Save to contact not yet", Toast.LENGTH_SHORT).show();
                    		 popupWindow.dismiss();
                    		 LinphoneActivity.instance().addContact(null, mPhoneNo[mSelPosition]);
                    	 }
                     }
                 });
                 AlertDialog alert = builder.create();
                 alert.show();
                 
                //mAddress.setText(mPhoneNo[position]);
                if (position==0) {
                    //Intent news = new Intent(Menu.this ,RSSItem.class);
                    //startActivity(news);
                }else if (position==1) {
                    //Intent calendar = new Intent(Menu.this ,calender.class);
                    //startActivity(news);
                }}});

        // some other visual settings
        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // set the list view as pop up window content
        popupWindow.setContentView(listViewDogs);

        return popupWindow;
    }

    private ArrayAdapter<String> dogsAdapter(String dogsArray[]) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, dogsArray) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // setting the ID and text for every items in the list
                String item = getItem(position);
                String[] itemArr = item.split("::");
                String text = itemArr[0];
                String id = itemArr[1];

                // visual settings for the list item
                TextView listItem = new TextView(getActivity());

                listItem.setText(text+"\n"+mMailAddr[Integer.parseInt(id)-1]+"\n"+mPhoneNo[Integer.parseInt(id)-1]);
                listItem.setTag(id);
                listItem.setTextSize(22);
                listItem.setPadding(10, 10, 10, 10);
                //listItem.setTextColor(Color.WHITE);

                return listItem;
            }
        };

        return adapter;
    }
	//>>> LiangBin add, 20150114
    //<<< LiangBin add, parse EWS
    private ArrayList<QueryItem> parseEws(String results)
    {
        ArrayList<QueryItem> ret_queryItem = new ArrayList<QueryItem>();
        Boolean flag = false;
        String name="";
        String email="";
        String mvpn="";
        try{
	        XmlPullParserFactory pullfactory = XmlPullParserFactory.newInstance();
	        XmlPullParser pullparser = pullfactory.newPullParser();
	        pullparser.setInput( new StringReader (results) );
	        int eventType = pullparser.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	            switch(eventType){
	                case XmlPullParser.START_DOCUMENT:
	                    break;
	                case XmlPullParser.START_TAG:
	                    if(pullparser.getName().equals("t:Resolution")){
	                        flag=true;
	                        /* LIVEHD-Neil, 2014/02/24, ADD: initial the contact <-- */
	                        name="";
	                        email="";
	                        mvpn="";
	                        /* LIVEHD-Neil, 2014/02/24, ADD: initial the contact --> */
	                    }                       
	                    if(pullparser.getName().equals("t:Name") && flag){
	                        name=pullparser.nextText();
	                    }
	                    if(pullparser.getName().equals("t:EmailAddress") && flag){
	                        email=pullparser.nextText();
	                    }
	                    if(pullparser.getName().equals("t:Entry") && pullparser.getAttributeValue(0).equals("BusinessPhone") && flag){
	                        mvpn=pullparser.nextText();
	                    }
	                    break;
	                case XmlPullParser.END_TAG:
	                    if(pullparser.getName().equals("t:Resolution")){
	                        //System.out.println("add::name="+name+", email="+email+", mvpn="+mvpn);
	                        //datalist.add(new querydata(n,e,m));
	                        if(flag){
	                            //Log.v("LOG_TAG","[EWS]add to arryrlist::name="+ name + ";mail="+email+";mvpn="+mvpn);
	                            ret_queryItem.add(new QueryItem(name, null, email, mvpn));
	                            flag = false;
	                        }
	                    }
	                    break;
	                case XmlPullParser.END_DOCUMENT:
	                    break;
	            	}
	        	    eventType = pullparser.next();
                }
                
        	} catch(XmlPullParserException e) {
    		  e.printStackTrace();
            } catch(IOException e) {
    		  e.printStackTrace();
    	    }
        return ret_queryItem;
    }
    //>>> LiangBin add, 20150211
	/**
	 * @return null if not ready yet
	 */
	public static DialerFragment instance() { 
		return instance;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (LinphoneActivity.isInstanciated()) {
			LinphoneActivity.instance().selectMenu(FragmentsAvailable.DIALER);
			LinphoneActivity.instance().updateDialerFragment(this);
			LinphoneActivity.instance().showStatusBar();
		}
		
		if (shouldEmptyAddressField) {
			mAddress.setText("");
		} else {
			shouldEmptyAddressField = true;
		}
		resetLayout(isCallTransferOngoing);
	}
	
	public void resetLayout(boolean callTransfer) {
		isCallTransferOngoing = callTransfer;
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc == null) {
			return;
		}
		
		if (lc.getCallsNb() > 0) {
			if (isCallTransferOngoing) {
				mCall.setImageResource(R.drawable.transfer_call);
				mCall.setExternalClickListener(transferListener);
			} else {
				mCall.setImageResource(R.drawable.add_call);
				mCall.resetClickListener();
			}
			mAddContact.setEnabled(true);
			mAddContact.setImageResource(R.drawable.cancel);
			mAddContact.setOnClickListener(cancelListener);
		} else {
			mCall.setImageResource(R.drawable.call);
			mAddContact.setEnabled(true);
			mAddContact.setImageResource(R.drawable.add_contact);
			mAddContact.setOnClickListener(addContactListener);
			enableDisableAddContact();
		}
		
		btnLdapQuery.setOnClickListener(queryLdapListener);// LiangBin add, 20150114
	}
	
	public void enableDisableAddContact() {
		mAddContact.setEnabled(LinphoneManager.getLc().getCallsNb() > 0 || !mAddress.getText().toString().equals(""));	
	}
	
	public void displayTextInAddressBar(String numberOrSipAddress) {
		shouldEmptyAddressField = false;
		mAddress.setText(numberOrSipAddress);
	}
	
	public void newOutgoingCall(String numberOrSipAddress) {
		displayTextInAddressBar(numberOrSipAddress);
		LinphoneManager.getInstance().newOutgoingCall(mAddress);
	}
	
	public void newOutgoingCall(Intent intent) {
		if (intent != null && intent.getData() != null) {
			String scheme = intent.getData().getScheme();
			if (scheme.startsWith("imto")) {
				mAddress.setText("sip:" + intent.getData().getLastPathSegment());
			} else if (scheme.startsWith("call") || scheme.startsWith("sip")) {
				mAddress.setText(intent.getData().getSchemeSpecificPart());
			} else {
				Log.e("Unknown scheme: ",scheme);
				mAddress.setText(intent.getData().getSchemeSpecificPart());
			}
	
			mAddress.clearDisplayedName();
			intent.setData(null);
	
			LinphoneManager.getInstance().newOutgoingCall(mAddress);
		}
	}
	//<<<LiangBin add, QueryItem
	public class QueryItem {
		private String queryName;
		private String queryName_CH;
		private String SIPurl;
		private String MVPNnumber;
		
		public QueryItem(String q, String q_CH, String url, String number)
		{
			this.queryName    = q;
			this.queryName_CH = q_CH;
			this.SIPurl       = url;
			this.MVPNnumber   = number;
		}
		
		public String getName()
		{
			if(queryName != null && queryName.length() != 0)
				return	queryName;
			if(queryName_CH != null && queryName_CH.length() != 0)
				return	queryName_CH;
			
			return "";
		}
		
		public String getSIPurl()
		{
			if(SIPurl != null)
				return SIPurl;
			return "";
		}
		
		public String getMVPN()
		{
	        String[] mvpn_a=MVPNnumber.split("/|\\(|\\)|\\.|-|,| ");
	        String mvpn="";
	        for(String ele:mvpn_a){
	        	if(ele.matches("\\d{5}") && !ele.contentEquals("00000")){
	        		mvpn=ele;
	        		break;
	        	}
	        }
	        return mvpn;
		}
		public String getTotalMVPN()
		{
			if(MVPNnumber != null)
				return MVPNnumber;
			return "";
		}
	}
	//<<< LiangBin add, 20150211
}
