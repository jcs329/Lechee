package com.quantatw.roomhub.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.ReminderData;
import com.quantatw.roomhub.utils.FailureCauseInfo;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.Utils;

/**
 * Created by erin on 12/25/15.
 */
public class ReminderDialog extends AlertDialog implements View.OnClickListener {
    private final String TAG=ReminderDialog.class.getSimpleName();
    private View mView;
    private Context mContext;
    private TextView titleTextView, messageTextView, suggestion1TextView, suggestion2TextView;
    private Button buttonOK, buttonLater, buttonCheck;

    private Messenger mServiceMessenger;
    private ReminderData mReminderData;
    private int mMessageTypeId;
    private String mCustomMessage;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(mServiceMessenger != null) {
                try {
                    mServiceMessenger.send(
                            Message.obtain(null, RoomHubService.MESSAGE_HANDLE_REMIND_MESSAGE_FINISH, mReminderData));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            super.handleMessage(msg);
        }
    };

    public ReminderDialog(Context context, int message_type_id, String message) {
        super(context);
        mMessageTypeId = message_type_id;
        mCustomMessage = message;
    }

    public ReminderDialog(Context context, Messenger serviceMessenger, ReminderData reminderData) {
        super(context);
        mServiceMessenger = serviceMessenger;
        mReminderData = reminderData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = getContext();
        mView = getLayoutInflater().inflate(R.layout.custom_reminder_dialog, null);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setView(mView);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        super.onCreate(savedInstanceState);

        titleTextView = (TextView)mView.findViewById(R.id.txtTitle);
        messageTextView = (TextView)mView.findViewById(R.id.txtMessage);
        suggestion1TextView = (TextView)mView.findViewById(R.id.txtSuggestion1);
        suggestion2TextView = (TextView)mView.findViewById(R.id.txtSuggestion2);
        buttonOK = (Button)mView.findViewById(R.id.ok_i_know);
        buttonLater = (Button)mView.findViewById(R.id.btn_wait);
        buttonCheck = (Button)mView.findViewById(R.id.btn_handle);
    }

    @Override
    public void onClick(View v) {
        FailureCauseInfo.ButtonAction buttonAction = (FailureCauseInfo.ButtonAction)v.getTag();
        Log.d(TAG,"onClick: buttonAction="+buttonAction);

        FailureCauseInfo failureCauseInfo = mReminderData.getFailureCauseInfo(mContext);

        Log.d(TAG,"onClick: reminderData="+mReminderData
                        +"\n,messageId="+mReminderData.getMessageId()
                        +"\n,message extra index="+mReminderData.getMessage_extraIndex()
                        +"\n,failureCauseInfo"+failureCauseInfo
                        +"\n,getActionButton1Message="+failureCauseInfo.getActionButton1Message()
                        +"\n,getActionButton2Message="+failureCauseInfo.getActionButton2Message()
                        +"\n,getActionButton3Message="+failureCauseInfo.getActionButton3Message()
        );
        /*

        FailureCauseInfo.ButtonAction buttonAction = null;

        switch(v.getId()) {
            case R.id.ok_i_know:
                buttonAction = failureCauseInfo.getActionButton1Message();
                break;
            case R.id.btn_wait:
                buttonAction = failureCauseInfo.getActionButton2Message();
                break;
            case R.id.btn_handle:
                buttonAction = failureCauseInfo.getActionButton3Message();
                break;
            default:
                break;
        }
        */
        
        try {
            if(buttonAction == null) {
                mServiceMessenger.send(Message.obtain(null,
                        FailureCauseInfo.LaunchActionType.DO_NOTHING, null));
            }
            else {
                int launchActionType = buttonAction.getLaunchActionType();
                Log.d(TAG,"onClick: launchActionType="+launchActionType);
                if (launchActionType >= FailureCauseInfo.LaunchActionType.LAUNCH_CUSTOM) {
                    Message replyMessage = buttonAction.getReplyMessage();
                    Log.d(TAG,"onClick: replyMessage="+replyMessage);
                    replyMessage.sendToTarget();
                } else {
                    mServiceMessenger.send(Message.obtain(null, launchActionType, mReminderData));
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        dismiss();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshLayout();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    private void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span)
    {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                TextView textView = (TextView)view;
                Spanned spanned = (Spanned)textView.getText();
                String url = spanned.subSequence(spanned.getSpanStart(this), spanned.getSpanEnd(this)).toString();
                Log.d(TAG, "onClick!!!" + url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(url));
                mContext.startActivity(intent);
                dismiss();
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    private void setTextViewHTML(TextView text, String html)
    {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        text.setText(strBuilder);
    }

    private void refreshLayout() {
        if(!TextUtils.isEmpty(mCustomMessage) && Utils.isGcmMessageId(mMessageTypeId)) {
            // Title
            String title = mContext.getString(R.string.gcm_message_title);
            title = mContext.getString(Utils.getGcmTitle(mMessageTypeId));
            titleTextView.setText(title);

            // Message
            // Linkify the message
            setTextViewHTML(messageTextView, mCustomMessage);
            messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
            suggestion1TextView.setVisibility(View.GONE);
            suggestion2TextView.setVisibility(View.GONE);
            buttonLater.setVisibility(View.GONE);
            buttonCheck.setVisibility(View.GONE);
            buttonOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            return;
        }

        int displayButtonCount = 0;
        FailureCauseInfo failureCauseInfo = mReminderData.getFailureCauseInfo(mContext);

        Log.d(TAG,"refreshLayout: reminderData="+mReminderData
                        +"\n,messageId="+mReminderData.getMessageId()
                        +"\n,message extra index="+mReminderData.getMessage_extraIndex()
                        +"\n,failureCauseInfo"+failureCauseInfo
                        +"\n,getActionButton1Message="+failureCauseInfo.getActionButton1Message()
                        +"\n,getActionButton2Message="+failureCauseInfo.getActionButton2Message()
                        +"\n,getActionButton3Message="+failureCauseInfo.getActionButton3Message()
        );

//        String title = failureCauseInfo.getLevelString(mContext, failureCauseInfo.getLevel());
        String title = failureCauseInfo.getCategory().getTitle(mContext);
        String message = mReminderData.getSimpleMessage();
        if (TextUtils.isEmpty(message))
            message = failureCauseInfo.getCause();
        String suggestion1 = null;
        if(failureCauseInfo.getSuggestion1() > 0)
            suggestion1 = failureCauseInfo.getSuggestion1String(mContext);
        String suggestion2 = null;
        if(failureCauseInfo.getSuggestion2() > 0)
            suggestion2 = failureCauseInfo.getSuggestion2String(mContext);

        // Title
        titleTextView.setText(title);

        // Message(Cause)
        messageTextView.setText(message);

        // Suggestion1
        if(!TextUtils.isEmpty(suggestion1))
            suggestion1TextView.setText(suggestion1);
        else
            suggestion1TextView.setVisibility(View.GONE);

        // Suggestion2
        if(!TextUtils.isEmpty(suggestion2))
            suggestion2TextView.setText(suggestion2);
        else
            suggestion2TextView.setVisibility(View.GONE);

        final FailureCauseInfo.ButtonAction button1Action =
                failureCauseInfo.getActionButton1Message();
        if(button1Action == null) {
            buttonOK.setVisibility(View.GONE);
        }
        else {
            if(button1Action.getButtonType() == FailureCauseInfo.FailureButton.BUTTON_CUSTOM) {
                String customLabel = button1Action.getCustomButtonLabel();
                buttonOK.setText(customLabel);
            }
            buttonOK.setTag(button1Action);
            buttonOK.setOnClickListener(this);
            displayButtonCount++;
        }

        // only show wifi
        final FailureCauseInfo.ButtonAction button2Action =
                failureCauseInfo.getActionButton2Message();
        if(button2Action == null) {
            buttonLater.setVisibility(View.GONE);
        }
        else if(button2Action.getLaunchActionType() == FailureCauseInfo.LaunchActionType.LAUNCH_WIFI) {
            String customLabel = button2Action.getCustomButtonLabel();
            buttonLater.setText(customLabel);
            buttonLater.setTag(button2Action);
            buttonLater.setOnClickListener(this);
            displayButtonCount++;
        }
        else {
            buttonLater.setVisibility(View.GONE);
//            if(button2Action.getButtonType() == FailureCauseInfo.FailureButton.BUTTON_CUSTOM) {
//                String customLabel = button2Action.getCustomButtonLabel();
//                buttonLater.setText(customLabel);
//            }
//            buttonLater.setTag(button2Action);
//            buttonLater.setOnClickListener(this);
//            displayButtonCount++;
        }

        final FailureCauseInfo.ButtonAction button3Action =
                failureCauseInfo.getActionButton3Message();
        if(button3Action == null) {
            buttonCheck.setVisibility(View.GONE);
        }
        else {
            if(button3Action.getButtonType() == FailureCauseInfo.FailureButton.BUTTON_CUSTOM) {
                String customLabel = button3Action.getCustomButtonLabel();
                buttonCheck.setText(customLabel);
            }
            buttonCheck.setTag(button3Action);
            buttonCheck.setOnClickListener(this);
            displayButtonCount++;
        }

        // display OK button if there are no any specific button
        if(displayButtonCount == 0) {
            buttonOK.setVisibility(View.VISIBLE);
            buttonOK.setOnClickListener(this);
        }
    }
}
