package com.quantatw.myapplication.voiceAssistant;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.quantatw.myapplication.CityPreference;
import com.quantatw.myapplication.MainActivity;
import com.quantatw.myapplication.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by youjun on 2016/5/13.
 */
public class VoiceAssistant extends DialogFragment {

    public enum RecognitionStage {
        KEYWORD (0) {
            public String toString() {
                return "KEYWORD";
            }
        },
        ASSISTANT (1) {
            public String toString() {
                return "ASSISTANT";
            }
        };

        private int value;

        private RecognitionStage(int val) {
            this.value = val;
        }

        public int get() {
            return this.value;
        }
    }

    protected final static String LOGTAG = "VoiceAssistant";

    private static List<ICommandHandler> mRegisteredHandler = new ArrayList<ICommandHandler>();

    //private MainActivity mActivity;
    //private Handler      mHandler;

    private LayoutInflater mInflater;
    private ListView mListView;
    private VoiceAssistantAdapter mArrayAdapter;

    private SpeechRecognition mSpeechRecognition;

    private boolean mRestartAfterSpeak;

    public interface ICommandHandler {
        List<VoiceCommand>  getCommandTable();
        boolean commandHandler(String main, String second, List<String> result);
    }

    public static class VoiceCommand {
        protected String commandMain;
        protected String commandSecond;

        public VoiceCommand(String cmdMain, String cmdSecond) {
            //Log.d(LOGTAG, "VoiceCommand - constructor");

            commandMain = cmdMain;
            commandSecond = cmdSecond;
        }
    }

    public static class CommandHandler extends VoiceCommand {
        protected VoiceAssistant.ICommandHandler handler;

        public CommandHandler(VoiceAssistant.ICommandHandler handler, String main, String second) {
            super(main, second);
            this.handler = handler;
            //Log.d(LOGTAG, "CommandAction - constructor");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOGTAG, "onCreateView");

        MainActivity mainActivity = MainActivity.instance();
        if(mainActivity != null)
            mainActivity.stopScreenSaveTimer();

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        mInflater = inflater;
        View view = inflater.inflate(R.layout.assistant_welcome, null);

        mListView = (ListView)view.findViewById(R.id.assistant_list);
        mArrayAdapter = new VoiceAssistantAdapter();
        mListView.setAdapter(mArrayAdapter);

        return view;
    }

    @Override
    public void onStart() {
        Log.d(LOGTAG, "onStart");
        super.onStart();

        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.height = 640;
        params.width = 1000;
        getDialog().getWindow().setAttributes(params);
    }

    @Override
    public void onStop() {
        Log.d(LOGTAG, "onStop");
        super.onStop();

        if(mSpeechRecognition != null)
            mSpeechRecognition.destroy();

        MainActivity mainActivity = MainActivity.instance();
        Handler handler = mainActivity==null?null:mainActivity.getHandler();

        if(mainActivity != null)
            mainActivity.startScreenSaveTimer();

        if(handler != null) {
            Log.d(LOGTAG, "onStop - start KeywordDetect");

            Message msg = Message.obtain();
            //msg.what = MainActivity.HANDLER_EVENT_KEYWORD_DETECT;
            //msg.arg1 = 1;
            //msg.obj = "Start KeywordDetect";
            msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
            msg.arg1 = 0;
            msg.arg2 = 0;

            handler.sendMessage(msg);
        }
    }

    public void create() {
        Log.d(LOGTAG, "setActivity");

        mRestartAfterSpeak = false;
        //mActivity = activity;
        //mHandler = mActivity.getHandler();
        mSpeechRecognition = new SpeechRecognition();
    }

    public void destroy() {
        Log.d(LOGTAG, "destroy");

        if(mSpeechRecognition != null)
            mSpeechRecognition.destroy();

        mRegisteredHandler.clear();

        //mActivity = null;
        //mHandler = null;
        mArrayAdapter = null;
    }

    public static void register(ICommandHandler handler) {
        Log.d(LOGTAG, "register");

        if(!mRegisteredHandler.contains(handler))
            mRegisteredHandler.add(handler);
    }
    public static void unregister(ICommandHandler handler) {
        Log.d(LOGTAG, "unregister");
        mRegisteredHandler.remove(handler);
    }

    //public void startSpeechRecognition() {
    public void startSpeechRecognition(boolean bForceAssistant) {
        //Log.d(LOGTAG, "startSpeechRecognition");
        Log.d(LOGTAG, "startSpeechRecognition - bForceAssistant: " + bForceAssistant);

        /*if(this.getDialog() != null && !mRestartAfterSpeak) {
            Log.d(LOGTAG, "startSpeechRecognition - dismiss dialog");
            this.dismissAllowingStateLoss();
        }*/

        mRestartAfterSpeak = false;
        registerCommand();

        if(mSpeechRecognition != null) {
            if(bForceAssistant || mSpeechRecognition.getStage() == RecognitionStage.ASSISTANT) {
                ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE);
                //toneGenerator.startTone(ToneGenerator.TONE_SUP_RADIO_ACK);
            }
            //mSpeechRecognition.startListening(stage);
            mSpeechRecognition.startListening(bForceAssistant);
        }
    }

    public void stopSpeechRecognition(boolean bDestroy) {
        Log.d(LOGTAG, "stopSpeechRecognition(" + bDestroy + ")");

        if(mSpeechRecognition != null) {
            if(bDestroy)
                mSpeechRecognition.destroy();
            else
                mSpeechRecognition.stopListening();
        }
    }

    public void stopTextToSpeech() {
        if(mSpeechRecognition != null) {
            mSpeechRecognition.stopTextToSpeech();
        }
    }

    public void restartSpeechRecognition() {
        Log.d(LOGTAG, "restartSpeechRecognition");

        if(getDialog() != null) {
            dismissAllowingStateLoss();
        }
        else {
            if(mSpeechRecognition != null)
                mSpeechRecognition.destroy();

            MainActivity mainActivity = MainActivity.instance();
            Handler handler = mainActivity==null?null:mainActivity.getHandler();

            if(handler != null) {
                Log.d(LOGTAG, "restartSpeechRecognition - start KeywordDetect");

                Message msg = Message.obtain();
                msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
                msg.arg1 = 0;
                msg.arg2 = 0;

                handler.sendMessage(msg);
            }
        }
    }

    public void startWelcome() {
        Log.d(LOGTAG, "startWelcome");

        //ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
        //toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE);
        //toneGenerator.startTone(ToneGenerator.TONE_SUP_RADIO_ACK);

        MainActivity mainActivity = MainActivity.instance();
        Handler handler = mainActivity==null?null:mainActivity.getHandler();

        if(handler != null)
            handler.removeMessages(MainActivity.HANDLER_EVENT_VOICE_ASSISTANT);

        if(!this.isAdded() && mainActivity != null) {
            //this.show(mActivity.getFragmentManager(), "VoiceAssistant");
            FragmentTransaction ft = mainActivity.getFragmentManager().beginTransaction();
            ft.add(this, "VoiceAssistant");
            ft.commitAllowingStateLoss();
        }
    }

    public void speakOut(String text, boolean bEnd) {
        //Log.d(LOGTAG, "speakOut - text: " + text + ", bEnd: " + bEnd);

        if(mSpeechRecognition != null)
            mSpeechRecognition.speakOut(text, bEnd);
    }

    public void createDialog(String text, int bIsMe) {
        Log.d(LOGTAG, "createDialog - text: " + text + ", bIsMe: " + bIsMe);

        MainActivity mainActivity = MainActivity.instance();
        Handler handler = mainActivity==null?null:mainActivity.getHandler();

        if(handler != null) {
            Message msg = Message.obtain();
            msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
            msg.arg1 = 2;
            msg.arg2 = bIsMe;
            msg.obj = text;

            handler.sendMessage(msg);
        }
    }

    public void createDialog(List<String> dialogs, int bIsMe, Object listener, String callback) {
        Log.d(LOGTAG, "createDialog - text: " + dialogs + ", callback: " + callback + ", bIsClean: " + bIsMe);

        if(mArrayAdapter != null)
            mArrayAdapter.setListener(listener, callback);

        MainActivity mainActivity = MainActivity.instance();
        Handler handler = mainActivity==null?null:mainActivity.getHandler();

        if(handler != null) {
            Message msg = Message.obtain();
            msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
            msg.arg1 = 2;
            msg.arg2 = bIsMe;
            msg.obj = dialogs;

            handler.sendMessage(msg);
        }
    }

    public void updateDialog(List<String> dialogs, int isMe) {
        Log.d(LOGTAG, "notifyDialogChanged - dialogs: " + dialogs + ", isMe: " + isMe);

        if(mArrayAdapter == null)
            return;

        boolean debugMode = new CityPreference(MainActivity.instance()).getVoiceAssistantDebug();

        if(dialogs.size() == 1) {
            if(!debugMode || isMe != 0) {
                VoiceAssistantAdapter.DialogContent dialogContent = mArrayAdapter.new DialogContent(dialogs.get(0), isMe);
                mArrayAdapter.mDialogList.add(dialogContent);
            }
        }
        else {
            mArrayAdapter.mDialogList.clear();

            if(!debugMode || isMe != 0) {
                for(String dialog : dialogs) {
                    VoiceAssistantAdapter.DialogContent dialogContent = mArrayAdapter.new DialogContent(dialog, isMe);
                    mArrayAdapter.mDialogList.add(dialogContent);
                }
            }
        }

        mArrayAdapter.notifyDataSetChanged();
    }


    public Map<Integer, String> getCandidate() {
        //Log.d(LOGTAG, "getCandidate");

        if(mSpeechRecognition != null)
            return mSpeechRecognition.getCandidate();

        return null;
    }

    public Map<Integer, String> getPossibility() {
        //Log.d(LOGTAG, "getPossibility");

        if(mSpeechRecognition != null)
            return mSpeechRecognition.getPossibility();

        return null;
    }

    public void recognizeWordsByNumber(List<String> targets, List<String> query) {
        //Log.d(LOGTAG, "recognizeWordsByNumber(target:" + targets + ", query: " + query + ")");

        if(mSpeechRecognition != null)
            mSpeechRecognition.recognizeWordsByNumber(targets, query);
    }

    public void recognizeWordsByString(List<String> targets, List<String> query) {
        //Log.d(LOGTAG, "recognizeWordsByString(targets: " + targets + "query: " + query);

        if(mSpeechRecognition != null)
            mSpeechRecognition.recognizeWordsByString(targets, query);
    }

    public void recongizeWordsByVoice(List<String> targets, List<String> query) {
        //Log.d(LOGTAG, "recongizeWordsByVoice(target: " + targets + ", query: " + query + ")");

        if(mSpeechRecognition != null)
            mSpeechRecognition.recongizeWordsByVoice(targets, query);
    }

    public void prepareTargets(List<String> targets) {
        //Log.d(LOGTAG, "prepareTargets - size of targets: " +targets.size());

        if(mSpeechRecognition != null)
            mSpeechRecognition.prepareTargets(targets);
    }

    // blocked function
    public void commandHandler(String cmd) {
        Log.d(LOGTAG, "commandHandler - cmd: " + cmd);

        registerCommand();

        int commandHandling = -1;
        String trimCmd = cmd.replaceAll("\\s+", "").replaceAll("-", "").replaceAll("_", "").toLowerCase();

        for(int i = 0; i < SpeechRecognition.COMMAND_ACTION.size(); i++) {
            String main = SpeechRecognition.COMMAND_ACTION.get(i).commandMain.replaceAll("\\s+", "").replaceAll("-", "").replaceAll("_", "").toLowerCase();
            String second = SpeechRecognition.COMMAND_ACTION.get(i).commandSecond.replaceAll("\\s+", "").replaceAll("-", "").replaceAll("_", "").toLowerCase();

            if(trimCmd.contains(main)) {
                if(second.length() > 0 && trimCmd.contains(second)) {
                    commandHandling = i;
                    break;
                }
                else if(second.length() == 0) {
                    commandHandling = i;
                    break;
                }
            }
        }

        if(commandHandling == -1) {
            Log.w(LOGTAG, "commandHandler - NO matched command");
            return;
        }

        final List<String> trimFiltered = new ArrayList<String>();
        String main = SpeechRecognition.COMMAND_ACTION.get(commandHandling).commandMain.replaceAll("\\s+", "").replaceAll("-", "").replaceAll("_", "").toLowerCase();

        int idx = trimCmd.indexOf(main);
        if(idx != -1) {
            String remain = "";
            if(idx + main.length() < trimCmd.length())
                remain = trimCmd.substring(idx + main.length());

            if(remain.length() > 0 && !trimFiltered.contains(remain)) {
                trimFiltered.add(remain);
            }
        }

        CommandHandler cmdHandler = SpeechRecognition.COMMAND_ACTION.get(commandHandling);
        ICommandHandler IcmdHandler = cmdHandler.handler;
        if(IcmdHandler != null) {
            Log.d(LOGTAG, "commandHandler - IcmdHandler is not null");
            IcmdHandler.commandHandler(SpeechRecognition.COMMAND_ACTION.get(commandHandling).commandMain, SpeechRecognition.COMMAND_ACTION.get(commandHandling).commandSecond, trimFiltered);
        }
    }

    protected void speechRecognitionOnResults(int noMatched) {
        Log.d(LOGTAG, "speechRecognitionOnResults - noMatched: " + noMatched);

        MainActivity mainActivity = MainActivity.instance();
        Handler handler = mainActivity==null?null:mainActivity.getHandler();

        if(noMatched == -1) {
            if(mSpeechRecognition != null) {
                mRestartAfterSpeak = false;
                createDialog(mainActivity.getString(R.string.voice_assistant_goodbye), 0);
                mSpeechRecognition.speakOut(mainActivity.getString(R.string.voice_assistant_goodbye), true);
            }
        }
        else if(noMatched > 0) {
            mRestartAfterSpeak = noMatched < SpeechRecognition.MAX_ERROR_TIMES ? true : false;

            if(mRestartAfterSpeak) {
                createDialog(mainActivity.getString(R.string.voice_assistant_error_cmd1), 0);
                if(mSpeechRecognition != null)
                    mSpeechRecognition.speakOut(mainActivity.getString(R.string.voice_assistant_error_cmd1), true);
            }
            else {
                createDialog(mainActivity.getString(R.string.voice_assistant_error_cmd2), 0);
                if(mSpeechRecognition != null)
                    mSpeechRecognition.speakOut(mainActivity.getString(R.string.voice_assistant_error_cmd2), true);
            }
        }

        if(getDialog() != null) {
            if(handler != null) {
                Log.d(LOGTAG, "speechRecognitionOnResults - start timer to stop voiceAssistant");

                Message msg = Message.obtain();
                msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
                msg.arg1 = -1;
                msg.obj = "Stop VoiceAssistant";

                handler.sendMessageDelayed(msg, 60000);
            }
        }
    }

    //protected void speechRecognitionOnError(int error, int errorTimes) {
    protected void speechRecognitionOnError(RecognitionStage stage, int error, int errorTimes) {
        //Log.d(LOGTAG, "speechRecognitionOnError(err: " + error + ", times: " + errorTimes + ")");
        Log.d(LOGTAG, "speechRecognitionOnError(stage: " + stage + ", err: " + error + ", times: " + errorTimes + ")");

        boolean bEnd = true;
        String message = "", msgTTS = "";

        MainActivity mainActivity = MainActivity.instance();
        Handler handler = mainActivity==null?null:mainActivity.getHandler();

        switch(error) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                msgTTS = mainActivity.getString(R.string.voice_assistant_error_client);
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Other client side errors";
                msgTTS = mainActivity.getString(R.string.voice_assistant_error_client);
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                msgTTS = mainActivity.getString(R.string.voice_assistant_error_client);
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Other network related errors";
                msgTTS = mainActivity.getString(R.string.voice_assistant_error_network);
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network operation timed out";
                msgTTS = mainActivity.getString(R.string.voice_assistant_error_network);
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No recognition result matched";
                msgTTS = mainActivity.getString(R.string.voice_assistant_error_noResult);
                bEnd = false;
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                msgTTS = mainActivity.getString(R.string.voice_assistant_error_client);
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Server sends error status";
                msgTTS = mainActivity.getString(R.string.voice_assistant_error_server);
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                msgTTS = mainActivity.getString(R.string.voice_assistant_error_noInput);;
                bEnd = false;
                break;
            default:
                message = "Unkonw error";
                break;
        }

        if(stage == RecognitionStage.KEYWORD) {
            if(error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                    && error != SpeechRecognizer.ERROR_NETWORK)
                mSpeechRecognition.speakOut(msgTTS, false);

            if(handler != null) {
                Log.d(LOGTAG, "speechRecognitionOnError - Start SpeechRecognition");

                Message msg = Message.obtain();
                msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
                msg.arg1 = 0;
                msg.arg2 = 0;
                msg.obj = "Start SpeechRecognition";

                handler.sendMessage(msg);
            }
            return;
        }

        bEnd = bEnd || (errorTimes >= SpeechRecognition.MAX_ERROR_TIMES ? true : false);

        mRestartAfterSpeak = !bEnd;

        createDialog(msgTTS, 0);
        mSpeechRecognition.speakOut(msgTTS, bEnd);

        if(bEnd) {
            if(handler != null) {
                Log.d(LOGTAG, "speechRecognitionOnError - start timer to stop voiceAssistant");

                Message msg = Message.obtain();
                msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
                msg.arg1 = -1;
                msg.obj = "Stop VoiceAssistant";

                handler.sendMessageDelayed(msg, 10000);
            }
        }
    }

    protected void textToSpeechOnFinish(boolean end) {
        Log.d(LOGTAG, "textToSpeechOnFinish(" + end + ")");

        MainActivity mainActivity = MainActivity.instance();
        Handler handler = mainActivity==null?null:mainActivity.getHandler();

        if(mRestartAfterSpeak) {
            if(handler != null) {
                Log.d(LOGTAG, "textToSpeechOnFinish - Start SpeechRecognition");

                Message msg = Message.obtain();
                msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
                msg.arg1 = 0;
                msg.arg2 = 0;
                msg.obj = "Start SpeechRecognition";

                handler.sendMessage(msg);
            }
        }
        else {
            if(end && getDialog() != null) {
                if(handler != null) {
                    Log.d(LOGTAG, "textToSpeechOnFinish - start timer to stop voiceAssistant");

                    Message msg = Message.obtain();
                    msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
                    msg.arg1 = -1;
                    msg.obj = "Stop VoiceAssistant";

                    handler.sendMessageDelayed(msg, 500);
                }
            }
        }
    }

    private void registerCommand() {
        //Log.d(LOGTAG, "registerCommand");

        for(Iterator iter1 = mRegisteredHandler.iterator(); iter1.hasNext();) {
            ICommandHandler Ihandler = (ICommandHandler)iter1.next();

            List<VoiceCommand> voiceCmdList = Ihandler.getCommandTable();
            for(Iterator iter2 = voiceCmdList.iterator(); iter2.hasNext();) {
                VoiceCommand voiceCmd = (VoiceCommand)iter2.next();
                if(mSpeechRecognition != null)
                    mSpeechRecognition.registerCommand(Ihandler, voiceCmd.commandMain, voiceCmd.commandSecond);
            }
        }
    }

    public class VoiceAssistantAdapter extends BaseAdapter {

        protected Object mListener;
        protected String mCallback;
        protected List<DialogContent> mDialogList;

        protected class DialogContent {
            protected int isMe;
            protected String content;

            public DialogContent(String text, int me) {
                //Log.d(LOGTAG, "DialogList - constructor");

                content = text;
                isMe = me;
            }
        }

        public VoiceAssistantAdapter() {
            Log.d(LOGTAG, "VoiceAssistantAdapter - constructor");

            mListener = null;
            mCallback = "";
            mDialogList = new ArrayList<DialogContent>();

            MainActivity mainActivity = MainActivity.instance();
            if(new CityPreference(mainActivity).getVoiceAssistantDebug() == false) {
                DialogContent dialogCont = new DialogContent(mainActivity.getString(R.string.voice_assistant_title), 0);
                mDialogList.add(dialogCont);
            }
        }

        protected void setListener(Object obj, String callback) {
            //Log.d(LOGTAG, "VoiceAssistantAdapter - setListener - callback: " + callback);

            mListener = obj;
            mCallback = callback;
        }

        @Override
        public int getCount() {
            //Log.d(LOGTAG, "VoiceAssistantAdapter - getCount: " + mDialogList.size());
            return mDialogList.size();
        }

        @Override
        public String getItem(int position) {
            //Log.d(LOGTAG, "VoiceAssistantAdapter - getItem: " + mDialogList.get(position).content);
            return mDialogList.get(position).content;
        }

        @Override
        public long getItemId(int position) {
            //Log.d(LOGTAG, "VoiceAssistantAdapter - getItemId: " + position);
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Log.d(LOGTAG, "VoiceAssistantAdapter - getView(p: " + position + "), mList: " + mDialogList.get(position).content);

            View view;
            ViewHolder viewHolder;

            {
                TextView textView;
                if(mDialogList.get(position).isMe == 0) {
                    view = mInflater.inflate(R.layout.assistant_list_me, parent, false);
                    textView = (TextView)view.findViewById(R.id.assistant_dialog);
                }
                else {
                    view = mInflater.inflate(R.layout.assistant_list_you, parent, false);
                    textView = (TextView)view.findViewById(R.id.assistant_dialog);
                }

                viewHolder = new ViewHolder(textView);
                view.setTag(viewHolder);
            }

            viewHolder.dialog.setText(getItem(position));

            if(mListener != null && mCallback.length() > 0) {
                //Log.d(LOGTAG, "VoiceAssistantAdapter - add click listener");

                viewHolder.dialog.setClickable(true);
                viewHolder.dialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Class[] paramType = { String.class };
                            Method action = mListener.getClass().getMethod(mCallback, paramType);

                            if(action != null) {
                                Object[] param = { getItem(position) };
                                action.invoke(mListener, param);
                            }
                        }
                        catch(Exception ex) {
                            Log.d(LOGTAG, "VoiceAssistantAdapter - call to " + mCallback + " occurs exception: " + ex.toString());
                        }

                        dismissAllowingStateLoss();
                    }
                });
            }

            return view;
        }
    }

    class ViewHolder {
        protected TextView dialog;

        public ViewHolder(TextView text) {
            dialog = text;
        }
    }
}
