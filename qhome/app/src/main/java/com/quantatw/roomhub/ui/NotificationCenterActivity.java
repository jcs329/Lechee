package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.BaseManager;
import com.quantatw.roomhub.manager.ReminderData;
import com.quantatw.roomhub.manager.RoomHubDBHelper;
import com.quantatw.roomhub.utils.FailureCauseInfo;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by erin on 1/6/16.
 */
public class NotificationCenterActivity extends AbstractRoomHubActivity {
    private final String TAG=NotificationCenterActivity.class.getSimpleName();
    private final int MESSAGE_GET_DATA = 100;

    private final int MESSAGE_DELETE = 200;
    private final int MESSAGE_DELETE_ALL = 201;

    private Context mContext;
    private GridView mHistoryGridView;
    private Button mClearButton;
    private HistoryCursorAdapter mHistoryCursorAdapter;
    private ProgressDialog mProgressDialog;

    private final int COLUMN_ID = 0;
    private final int COLUMN_UUID = 1;
    private final int COLUMN_MESSAGE_ID = 2;
    private final int COLUMN_MESSAGE = 3;
    private final int COLUMN_SENDER_ID = 4;
    private final int COLUMN_TIMESTAMP = 5;
    private final int COLUMN_EXPIRE_TIMESTAMP= 6;
    private final int COLUMN_BUTTON1_TYPE = 7;
    private final int COLUMN_BUTTON1_LABEL = 8;
    private final int COLUMN_BUTTON1_HANDLEID = 9;
    private final int COLUMN_BUTTON2_TYPE = 10;
    private final int COLUMN_BUTTON2_LABEL = 11;
    private final int COLUMN_BUTTON2_HANDLEID = 12;
    private final int COLUMN_BUTTON3_TYPE = 13;
    private final int COLUMN_BUTTON3_LABEL = 14;
    private final int COLUMN_BUTTON3_HANDLEID = 15;
    private final int COLUMN_MINOR_MESSAGE = 16;
    private final int COLUMN_SUGGESTION = 17;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_GET_DATA:
                    removeMessages(MESSAGE_GET_DATA);
                    showProgressDialog(true);
                    getData();
                    showProgressDialog(false);
                    break;
                case MESSAGE_DELETE:
                    deleteHistory((int) msg.obj);
                    Toast.makeText(mContext,getString(R.string.notification_delete_toast),Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_DELETE_ALL:
                    deleteAllHistory();
                    enableClearButton(false);
                    Toast.makeText(mContext,getString(R.string.notification_delete_toast),Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    private void getData() {
        Cursor cursor = getRoomHubDBHelper().notificationQuery(getRoomHubDBHelper().getReadableDatabase());
        mHistoryCursorAdapter.changeCursor(cursor);
        if(cursor != null && cursor.getCount() > 0) {
            enableClearButton(true);
        }
        else
            enableClearButton(false);
    }

    private void enableClearButton(boolean enable) {
        if(enable)
            mClearButton.setVisibility(View.VISIBLE);
        else
            mClearButton.setVisibility(View.GONE);
    }

    private void deleteHistory(final int index) {
        getRoomHubDBHelper().notificationDelete(getRoomHubDBHelper().getWritableDatabase(), index);
    }

    private void deleteAllHistory() {
        getRoomHubDBHelper().notificationDeleteAll(getRoomHubDBHelper().getWritableDatabase());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);

        mContext = this;
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.process_str));

        mHistoryGridView = (GridView)findViewById(R.id.history_list);
        mHistoryCursorAdapter = new HistoryCursorAdapter(null);
        mHistoryGridView.setAdapter(mHistoryCursorAdapter);

        mClearButton = (Button)findViewById(R.id.btnClearAll);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mHandler.sendEmptyMessage(MESSAGE_GET_DATA);
        IntentFilter intentFilter = new IntentFilter(RoomHubDBHelper.ACTION_NOTIFICATOIN_CONTENT_CHANGED);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showConfirmDialog() {
        final Dialog dialog = new Dialog(mContext,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.notification_delete_all_confirm));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MESSAGE_DELETE_ALL);
                dialog.dismiss();
            }
        });
        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showProgressDialog(boolean show) {
        if(show) {
            if(!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }
        else {
            mProgressDialog.dismiss();
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(RoomHubDBHelper.ACTION_NOTIFICATOIN_CONTENT_CHANGED)) {
                mHandler.sendEmptyMessageDelayed(MESSAGE_GET_DATA, 1000);
            }
        }
    };

    private class HistoryCursorAdapter extends CursorAdapter {
        private class ViewHolder {
            ReminderData reminderData;
            ImageView icon;
            TextView title;
            TextView message;
            TextView suggestion;
            TextView timestamp;
            View button1, button2, button3;

            public ButtonViewHolder getButtonViewHolder(View button) {
                ButtonViewHolder buttonViewHolder = new ButtonViewHolder();
                buttonViewHolder.parentHolder = this;
                buttonViewHolder.icon = (ImageView) button.findViewById(R.id.notify_btn_icon);
                buttonViewHolder.label = (TextView) button.findViewById(R.id.notify_btn_label);
                return buttonViewHolder;
            }

            public void hideButtonView(View button) {
                button.setVisibility(View.GONE);
            }

            public void disableButtonView(View button) {
                ButtonViewHolder buttonViewHolder = (ButtonViewHolder) button.getTag();
                buttonViewHolder.label.setTextColor(getResources().getColor(R.color.color_pinkish_grey));
                button.setEnabled(false);
                buttonViewHolder.icon.setClickable(false);
                buttonViewHolder.label.setClickable(false);
                buttonViewHolder.icon.setOnClickListener(null);
                buttonViewHolder.label.setOnClickListener(null);
            }

            public void enableButtonView(View button, int index) {
                ButtonViewHolder buttonViewHolder = (ButtonViewHolder) button.getTag();
                buttonViewHolder.label.setTextColor(getResources().getColor(R.color.color_white));
                button.setEnabled(true);
                buttonViewHolder.icon.setClickable(true);
                buttonViewHolder.label.setClickable(true);
                buttonViewHolder.icon.setOnClickListener(onClickListener);
                buttonViewHolder.label.setOnClickListener(onClickListener);
                buttonViewHolder.index = index;
            }

            private View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View parent = (View) v.getParent();
                    ButtonViewHolder buttonViewHolder = (ButtonViewHolder) parent.getTag();
                    String uuid = buttonViewHolder.parentHolder.reminderData.getUuid();
                    int index = buttonViewHolder.index;
                    log("onClick: index=" + index + ",label=" + buttonViewHolder.label.getText().toString()
                            + ", uuid="+uuid);
//                    FailureCauseInfo.ButtonAction buttonAction = buttonViewHolder.buttonAction;
//                    if(buttonAction != null) {
//                        log("onClick: getLaunchActionType=" + buttonAction.getLaunchActionType());
//                        replyToTarget(reminderData.getSenderId(),
//                                uuid,
//                                buttonAction.getLaunchActionType());
//                    }
//                    else
                    {
                        log("onClick: Delete pos=" + buttonViewHolder.index);
                        showConfirmDialog(buttonViewHolder.index);
                    }
                }
            };

            private void showConfirmDialog(final int index) {
                final Dialog dialog = new Dialog(mContext,R.style.CustomDialog);

                dialog.setContentView(R.layout.custom_dialog);

                TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
                txt_msg.setText(getString(R.string.notification_delete_confirm));

                Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
                btn_yes.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_DELETE, index));
                        dialog.dismiss();
                    }
                });
                Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
                btn_no.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        }

        private class ButtonViewHolder {
            ViewHolder parentHolder;
            ImageView icon;
            TextView label;
            int index;
            FailureCauseInfo.ButtonAction buttonAction;
        }

        public HistoryCursorAdapter(Cursor cursor) {
            super(mContext, cursor, 0);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if(view == null || cursor == null)
                return;

            if(view.getTag() == null)
                setView(view);
            bindItem((ViewHolder) view.getTag(), createFromCursor(cursor), cursor.getInt(COLUMN_ID));
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.notification_item_row, null);
            setView(view);
            return view;
        }

        private void replyToTarget(int senderId, String uuid, int message_id) {
            Handler targetHandler = null;
            log("replyToTarget senderId="+senderId+",uuid="+uuid+",message_id="+message_id);
            switch(senderId) {
                case BaseManager.ROOMHUB_MANAGER:
                    targetHandler = getRoomHubManager().getFailureHandler();
                    break;
                case BaseManager.ACCOUNT_MANAGER:
                    targetHandler = getAccountManager().getFailureHandler();
                    break;
                case BaseManager.OTA_MANAGER:
                    targetHandler = getOTAManager().getFailureHandler();
                    break;
                case BaseManager.ACNOTICE_MANAGER:
                    targetHandler = getACNoticeManager().getFailureHandler();
                    break;
                /*
                case BaseManager.AC_MANAGER:
                    targetHandler = getACManager().getFailureHandler();
                    break;
                */
                // send back to Service Handler
                case BaseManager.NETWORK_MONITOR:
                default:
                    break;
            }

            if(targetHandler != null) {
                targetHandler.sendMessage(targetHandler.obtainMessage(message_id, uuid));
            }
            else {
                ((RoomHubApplication)getApplication()).sendToServiceHandler(message_id);
            }
        }

        private void setView(View view) {
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView)view.findViewById(R.id.notify_icon);
            viewHolder.title = (TextView)view.findViewById(R.id.notify_title);
            viewHolder.message = (TextView)view.findViewById(R.id.notify_message);
            viewHolder.suggestion = (TextView)view.findViewById(R.id.notify_suggestion);
            viewHolder.timestamp = (TextView)view.findViewById(R.id.notify_timestamp);
            viewHolder.button1 = view.findViewById(R.id.notify_btn1).findViewById(R.id.notify_btn_layout);
            ButtonViewHolder buttonViewHolder1 = viewHolder.getButtonViewHolder(viewHolder.button1);
            buttonViewHolder1.icon.setBackground(getResources().getDrawable(R.drawable.btn_take_action));
            buttonViewHolder1.label.setText(getString(R.string.notification_btn_handle));
            viewHolder.button1.setTag(buttonViewHolder1);
            viewHolder.button2 = view.findViewById(R.id.notify_btn2).findViewById(R.id.notify_btn_layout);;
            ButtonViewHolder buttonViewHolder2 = viewHolder.getButtonViewHolder(viewHolder.button2);
            buttonViewHolder2.icon.setBackground(getResources().getDrawable(R.drawable.btn_remindmelater));
            buttonViewHolder2.label.setText(getString(R.string.notification_btn_later));
            viewHolder.button2.setTag(buttonViewHolder2);
            viewHolder.button3 = view.findViewById(R.id.notify_btn3).findViewById(R.id.notify_btn_layout);;
            ButtonViewHolder buttonViewHolder3 = viewHolder.getButtonViewHolder(viewHolder.button3);
            buttonViewHolder3.icon.setBackground(getResources().getDrawable(R.drawable.btn_deletemsg));
            buttonViewHolder3.label.setText(getString(R.string.notification_btn_delete));
            viewHolder.button3.setTag(buttonViewHolder3);
            view.setTag(viewHolder);
        }

        private ReminderData createFromCursor(Cursor cursor) {
            ReminderData reminderData = new ReminderData();

            int id = cursor.getInt(COLUMN_ID);
            String uuid = cursor.getString(COLUMN_UUID);
            int message_id = cursor.getInt(COLUMN_MESSAGE_ID);
            String message = cursor.getString(COLUMN_MESSAGE);
            int sender_id = cursor.getInt(COLUMN_SENDER_ID);
            Long time = cursor.getLong(COLUMN_TIMESTAMP);
            Long expire = cursor.getLong(COLUMN_EXPIRE_TIMESTAMP);
            int button1_type = cursor.getInt(COLUMN_BUTTON1_TYPE);
            String button1_label = cursor.getString(COLUMN_BUTTON1_LABEL);
            int button1_handleId = cursor.getInt(COLUMN_BUTTON1_HANDLEID);
            int button2_type = cursor.getInt(COLUMN_BUTTON2_TYPE);
            String button2_label = cursor.getString(COLUMN_BUTTON2_LABEL);
            int button2_handleId = cursor.getInt(COLUMN_BUTTON2_HANDLEID);
            int button3_type = cursor.getInt(COLUMN_BUTTON3_TYPE);
            String button3_label = cursor.getString(COLUMN_BUTTON3_LABEL);
            int button3_handleId = cursor.getInt(COLUMN_BUTTON3_HANDLEID);

            /*
            Log.d(TAG, "id=" + cursor.getInt(COLUMN_ID)
                            + ",uuid=" + cursor.getString(COLUMN_UUID)
                            + ",message_id=" + cursor.getInt(COLUMN_MESSAGE_ID)
                            + ",message=" + cursor.getString(COLUMN_MESSAGE)
                            + ",sender_id=" + cursor.getInt(COLUMN_SENDER_ID)
                            + ",time=" + cursor.getLong(COLUMN_TIMESTAMP)
                            + ",expire=" + cursor.getLong(COLUMN_EXPIRE_TIMESTAMP)
                            + ",button1_type=" + cursor.getInt(COLUMN_BUTTON1_TYPE)
                            + ",button1_label=" + cursor.getString(COLUMN_BUTTON1_LABEL)
                            + ",button1_handleId=" + cursor.getInt(COLUMN_BUTTON1_HANDLEID)
                            + ",button2_type=" + cursor.getInt(COLUMN_BUTTON2_TYPE)
                            + ",button2_label=" + cursor.getString(COLUMN_BUTTON2_LABEL)
                            + ",button2_handleId=" + cursor.getInt(COLUMN_BUTTON2_HANDLEID)
                            + ",button3_type=" + cursor.getInt(COLUMN_BUTTON3_TYPE)
                            + ",button3_label=" + cursor.getString(COLUMN_BUTTON3_LABEL)
                            + ",button3_handleId=" + cursor.getInt(COLUMN_BUTTON3_HANDLEID)
            );
            */

            reminderData.setUuid(uuid);
            reminderData.setMessageId(message_id);
            reminderData.setSimpleMessage(message);
            reminderData.setSenderId(sender_id);
            reminderData.setTimestamp(time);

            if(Utils.isGcmMessageId(message_id)) {
                return reminderData;
            }

            boolean hasButton = ((button1_handleId|button2_handleId|button3_handleId)>0);

            FailureCauseInfo failureCauseInfo = reminderData.getFailureCauseInfo(mContext);
            if(hasButton) {
                int type[] = { button1_type, button2_type, button3_type};
                String label[] = {button1_label, button2_label, button3_label};
                int handleId[] = {button1_handleId, button2_handleId, button3_handleId};

                for(int i=0;i<3;i++) {
                    if(handleId[i] > 0) {
                        FailureCauseInfo.ButtonAction buttonAction = new FailureCauseInfo.ButtonAction();
                        buttonAction.setButtonType(type[i]);
                        buttonAction.setCustomButtonLabel(label[i]);
                        buttonAction.setLaunchActionType(handleId[i]);
                        if(i == 0)
                            failureCauseInfo.setActionButton1Message(buttonAction);
                        else if(i == 1)
                            failureCauseInfo.setActionButton2Message(buttonAction);
                         else if(i == 2)
                            failureCauseInfo.setActionButton3Message(buttonAction);
                    }
                }
            }

            return reminderData;
        }

        private void gcm_bindItem(ViewHolder viewHolder, ReminderData reminderData, int index) {
            viewHolder.reminderData = reminderData;

            viewHolder.icon.setBackgroundResource(R.drawable.icon_promotion);
            viewHolder.title.setText(getString(Utils.getGcmTitle(reminderData.getMessageId())));

            viewHolder.message.setMovementMethod(LinkMovementMethod.getInstance());
            viewHolder.message.setText(Html.fromHtml(reminderData.getSimpleMessage()));
            viewHolder.suggestion.setVisibility(View.GONE);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String timestamp = dateFormat.format(new Date(reminderData.getTimestamp()));
            viewHolder.timestamp.setText(timestamp);

            viewHolder.hideButtonView(viewHolder.button1);
            viewHolder.hideButtonView(viewHolder.button2);
            viewHolder.enableButtonView(viewHolder.button3, index);
        }

        private void bindItem(ViewHolder viewHolder, ReminderData reminderData, int index) {
            if(Utils.isGcmMessageId(reminderData.getMessageId())) {
                gcm_bindItem(viewHolder, reminderData, index);
                return;
            }

            FailureCauseInfo failureCauseInfo = reminderData.getFailureCauseInfo(mContext);

            viewHolder.reminderData = reminderData;

            int iconRes = failureCauseInfo.getCategory().getIcon_res();
            if(iconRes > 0)
                viewHolder.icon.setBackground(getResources().getDrawable(iconRes));
            viewHolder.title.setText(failureCauseInfo.getCategory().getTitle(mContext));
            viewHolder.message.setText(reminderData.getSimpleMessage());
            viewHolder.suggestion.setVisibility(View.VISIBLE);
            viewHolder.suggestion.setText(failureCauseInfo.getSuggestion1String(mContext));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String timestamp = dateFormat.format(new Date(reminderData.getTimestamp()));
            viewHolder.timestamp.setText(timestamp);

            int buttons = getButtons(failureCauseInfo);
            if(!failureCauseInfo.hasButtons() || buttons==0) { // only show Delete button
//                viewHolder.disableButtonView(viewHolder.button1);
//                viewHolder.disableButtonView(viewHolder.button2);
                viewHolder.hideButtonView(viewHolder.button1);
                viewHolder.hideButtonView(viewHolder.button2);
                viewHolder.enableButtonView(viewHolder.button3, index);
            } else {

                /* check button */
                viewHolder.hideButtonView(viewHolder.button1);
                /*
                FailureCauseInfo.ButtonAction buttonAction3 =
                        failureCauseInfo.getActionButton3Message();
                if (buttonAction3 != null) {
                    if(buttonAction3.getLaunchActionType()== FailureCauseInfo.LaunchActionType.DO_NOTHING) {
                        viewHolder.disableButtonView(viewHolder.button1);
                    }
                    else {
                        viewHolder.enableButtonView(viewHolder.button1, index);
                        ButtonViewHolder buttonViewHolder = (ButtonViewHolder) viewHolder.button1.getTag();
                        buttonViewHolder.buttonAction = buttonAction3;
                        //buttonViewHolder.icon.setBackground();
                        if (!TextUtils.isEmpty(buttonAction3.getCustomButtonLabel()))
                            buttonViewHolder.label.setText(buttonAction3.getCustomButtonLabel());
                    }
                } else {
                    viewHolder.disableButtonView(viewHolder.button1);
                }
                */

                /* notify later button */
                viewHolder.hideButtonView(viewHolder.button2);
                /*
                FailureCauseInfo.ButtonAction buttonAction2 =
                        failureCauseInfo.getActionButton2Message();
                if (buttonAction2 != null) {
                    if(buttonAction2.getLaunchActionType()== FailureCauseInfo.LaunchActionType.DO_NOTHING) {
                        viewHolder.disableButtonView(viewHolder.button2);
                    }
                    else {
                        viewHolder.enableButtonView(viewHolder.button2, index);
                        ButtonViewHolder buttonViewHolder = (ButtonViewHolder) viewHolder.button2.getTag();
                        buttonViewHolder.buttonAction = buttonAction2;
                        //buttonViewHolder.icon.setBackground();
                        if (!TextUtils.isEmpty(buttonAction2.getCustomButtonLabel()))
                            buttonViewHolder.label.setText(buttonAction2.getCustomButtonLabel());
                    }
                } else
                    viewHolder.disableButtonView(viewHolder.button2);
                    */

                /* delete button */
                FailureCauseInfo.ButtonAction buttonAction1 =
                        failureCauseInfo.getActionButton1Message();
                if (buttonAction1 != null &&
                        buttonAction1.getLaunchActionType()>= FailureCauseInfo.LaunchActionType.LAUNCH_CUSTOM) {
                    viewHolder.enableButtonView(viewHolder.button3, index);
                    ButtonViewHolder buttonViewHolder = (ButtonViewHolder) viewHolder.button3.getTag();
                    buttonViewHolder.buttonAction = buttonAction1;
                    //buttonViewHolder.icon.setBackground();
                    if (!TextUtils.isEmpty(buttonAction1.getCustomButtonLabel()))
                        buttonViewHolder.label.setText(buttonAction1.getCustomButtonLabel());
                } else {
                    // enable Delete button
                    viewHolder.enableButtonView(viewHolder.button3, index);
                }

            }
        }

    }

    private int getButtons(FailureCauseInfo failureCauseInfo) {
        int button = 0;

        for(int i=0;i<3;i++) {
            FailureCauseInfo.ButtonAction buttonAction = null;

            if(i == 0) {
                buttonAction =
                        failureCauseInfo.getActionButton1Message();
            }
            else if(i == 1) {
                buttonAction =
                        failureCauseInfo.getActionButton2Message();
            }
            else {
                buttonAction =
                        failureCauseInfo.getActionButton3Message();
            }
            if(buttonAction != null &&
                    buttonAction.getLaunchActionType() >= FailureCauseInfo.LaunchActionType.LAUNCH_CUSTOM) {
                button++;
            }
        }

        return button;
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
