package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.sls.object.IRCodeNumData;

import java.util.ArrayList;

/**
 * Created by erin on 10/13/15.
 */
public class IRLearningResultsFragment extends Fragment implements AdapterView.OnItemClickListener{
    private FragmentActivity mParent;
    private IRLearningResultsFragment mInstance;
    private Context mContext;

    private ListView mListView;
    private IRLearningResultAdapter mAdapter;
    private ArrayList<IRCodeNumData> mList;

    private final int MESSAGE_CHECK_IR_DATA_DONE = 100;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_CHECK_IR_DATA_DONE:
                    IRCodeNumData data = (IRCodeNumData)msg.obj;
                    displayAlertDialog(data);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;
        if(getArguments()!=null) {
            mList = getArguments().getParcelableArrayList(IRSettingDataValues.KEY_IR_LEARNING_RESULTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_ir_learning_results, container, false);

        mListView = (ListView)view.findViewById(R.id.resultList);
        mAdapter = new IRLearningResultAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        View bottomLayout = (View)view.findViewById(R.id.bottomLayout);
        Button autoScanButton = (Button)bottomLayout.findViewById(R.id.btnAutoScan);
        autoScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        Button retryButton = (Button)bottomLayout.findViewById(R.id.btnRetry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((IRLearningActivity)mParent).doRetry(mInstance);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mParent = (FragmentActivity)context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final IRCodeNumData currentData = mList.get(position);

        //((IRLearningActivity)mParent).setLed();
        Thread thread = new Thread() {
            @Override
            public void run() {
                boolean ret = ((IRLearningActivity)mParent).checkIRData(currentData);
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_CHECK_IR_DATA_DONE, currentData));
            }
        };
        thread.start();

    }

    private void displayAlertDialog(final IRCodeNumData currentData) {
        final Dialog dialog = new Dialog(getActivity(),R.style.IRLearningConfirmDialog);
        dialog.setContentView(R.layout.simple_confirm_dialog);

        ((TextView)dialog.findViewById(R.id.message)).setText(getString(R.string.ir_learning_confirm));
        ((Button)dialog.findViewById(R.id.yesBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((IRLearningActivity) mParent).finishIRLearning(currentData);
                dialog.dismiss();
            }
        });
        ((Button)dialog.findViewById(R.id.noBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private class IRLearningResultAdapter extends BaseAdapter {

        private class ViewHolder {
            TextView brandText;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.ir_learning_result_list_item, parent, false);
                holder = new ViewHolder();
                holder.brandText = (TextView)convertView.findViewById(R.id.brand);
                convertView.setTag(holder);
            }
            else
                holder = (ViewHolder)convertView.getTag();

            final IRCodeNumData item = (IRCodeNumData)getItem(position);
            holder.brandText.setText(Integer.toString(item.getCodeNum()));

            return convertView;
        }
    };
}
