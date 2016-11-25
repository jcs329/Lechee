package com.quantatw.myapplication.voiceAssistant;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.quantatw.myapplication.CityPreference;
import com.quantatw.myapplication.MainActivity;
import com.quantatw.myapplication.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by youjun on 2016/5/11.
 */
public class SpeechRecognition implements TextToSpeech.OnInitListener {

    private static final String LOGTAG = "SpeechRecognition";

    private static final String KEYWORD = "heysophia";
    private VoiceAssistant.RecognitionStage mStage;

    private static final int MAX_RESULTS = 10;
    public static final int MAX_ERROR_TIMES = 3;

    private static final int TTS_EVENT_START = 0;
    private static final int TTS_EVENT_DONE  = 1;
    private static final int TTS_EVENT_ERROR = -1;

    private static final int TTS_STATE_TRANSFORM = 1;

    private static final String UTTERANCE_SPEAK = "TTS_SPEAK";
    private static final String UTTERANCE_TRANSFORM = "TTS_WAV";
    private static final String UTTERANCE_TRANSFORM_TARGET = UTTERANCE_TRANSFORM + "_TARGET";
    private static final String UTTERANCE_TRANSFORM_QUERY = UTTERANCE_TRANSFORM + "_QUERY";

    //private MainActivity mActivity;
    //private Handler mHandler;

    private Handler wcvHandler;
    private WordsCmpByVoice wcvThread;

    private final String TARGET_WAV;
    private final String QUERY_WAV;
    private final String TARGET_HASH;

    protected static List<VoiceAssistant.CommandHandler> COMMAND_ACTION = new ArrayList<VoiceAssistant.CommandHandler>();

    private int commandHandling;
    private Map<Integer, String> mCandidate;
    private Map<Integer, String> mPossibility;

    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;

    static private Object mTTSMutex = new Object();

    private TextToSpeech mTTS;
    private boolean mTTSinitialed;
    private boolean bStopTTS;
    private boolean mEndAfterSpeak;

    private int mNoMatched;
    private int mErrorTimes;

    private Runnable restartRun;

    class WordsCmpByVoice extends Thread {
        private Looper myLooper;

        private List<String> targetList = null;
        private List<String> queryList = null;
        private Iterator targetIter = null;
        private Iterator queryIter = null;

        private int     priority;
        private boolean bDontCompare;

        private HashMap<String, String> mTargetHash = new HashMap<String, String>();
        private HashMap<String, String> mQueryHash = new HashMap<String, String>();

        private String mQuery;
        private String mTarget;

        public WordsCmpByVoice(List<String> target, List<String> query, boolean bDontComp) {
            Log.d(LOGTAG, "WordsCmpByVoice - constructor");

            priority = -1;
            bDontCompare = bDontComp;

            if(target != null) {
                targetList = target;
                targetIter = targetList.iterator();
            }

            if(query != null) {
                queryList = query;
                queryIter = queryList.iterator();
            }

            mTargetHash.clear();
            mQueryHash.clear();
        }

        private boolean doRound() {
            //Log.d(LOGTAG, "WordsCmpByVoice - doRound");

            boolean bTextToFile = false;
            while(targetIter.hasNext()) {
                if(bStopTTS) {
                    Log.d(LOGTAG, "WordsCmpByVoice - doRound - force stop");
                    return false;
                }

                if(!bDontCompare)
                    queryIter = queryList.iterator(); priority = -1;// reset

                mTarget = (String)targetIter.next();

                if(!mTargetHash.containsKey(mTarget)) {
                    textToFile(mTarget, true);
                    bTextToFile = true;
                    break;
                }
                else {
                    if(bDontCompare)
                        continue;

                    while(queryIter.hasNext()) {
                        if(bStopTTS) {
                            Log.d(LOGTAG, "WordsCmpByVoice - doRound - force stop");
                            return false;
                        }

                        priority++;
                        mQuery = queryIter.next().toString();

                        if(!filteredByDigit(mQuery)) {
                            if(mTarget.length() == mQuery.length()) {
                                if(!mQueryHash.containsKey(mQuery)) {
                                    textToFile(mQuery, false);
                                    bTextToFile = true;
                                    break;
                                }
                                else {
                                    if(mTargetHash.get(mTarget) != null && mQueryHash.get(mQuery) != null) {
                                        if(mTargetHash.get(mTarget).equals(mQueryHash.get(mQuery))) {
                                            if(!isMapContainsValue(mCandidate, mTarget)) {
                                                //Log.d(LOGTAG, "WordsCmpByVoice - doRound - add to mCandidate(priori: " + priority + ")");
                                                mCandidate.put(new Integer(priority), mTarget);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if(bTextToFile) {
                        break;
                    }
                }
            }

            return bTextToFile;
        }

        public void run() {
            Log.d(LOGTAG, "WordsCmpByVoice - run(" + Thread.currentThread().getId() + ")");

            if(targetList == null) {
                Log.w(LOGTAG, "WordsCmpByVoice - run - targetList is null");
                return;
            }

            // prepare hash table of targets
            if(fileExist(TARGET_HASH)) {
                for(String t : targetList) {
                    if(bStopTTS) {
                        Log.d(LOGTAG, "WordsCmpByVoice - run - force stop");
                        return;
                    }

                    String hash = getHashCodeByName(TARGET_HASH, t);
                    if(hash != null) {
                        mTargetHash.put(t, hash);
                    }
                }
            }

            if(bDontCompare && targetList.size() == mTargetHash.size()) {
                return;
            }

            while(!mTTSinitialed) {
                Log.d(LOGTAG, "WordsCmpByVoice - tts is not ready. wait 500ms");

                if(bStopTTS) {
                    Log.w(LOGTAG, "WordsCmpByVoice - run - force stop");
                    return;
                }

                try {
                    Thread.sleep(500);
                }
                catch(Exception e){
                    Log.e(LOGTAG, "WordsCmpByVoice - exception: " + e.toString());
                }
            }

            Looper.prepare();
            myLooper = Looper.myLooper();

            wcvHandler = new Handler(myLooper) {
                public void handleMessage(Message msg) {
                    if(msg.obj == null) {
                        return;
                    }

                    switch(msg.arg1) {
                        case TTS_STATE_TRANSFORM: // UTTERANCE_TRANSFORM
                            if(msg.obj.toString().contains(UTTERANCE_TRANSFORM)) {
                                //Log.d(LOGTAG, "WordsCmpByVoice - handleMessage - UTTERANCE_TRANSFORM");

                                if(msg.arg2 == TTS_EVENT_START) {
                                    //Log.d(LOGTAG, "WordsCmpByVoice - handleMessage - UTTERANCE_TRANSFORM - onStart");
                                }
                                else if(msg.arg2 == TTS_EVENT_DONE) {
                                    //Log.d(LOGTAG, "WordsCmpByVoice - handleMessage - UTTERANCE_TRANSFORM - onDone");

                                    boolean bMatched = false;
                                    if(msg.obj.toString().equals(UTTERANCE_TRANSFORM_TARGET)) {
                                        String hashCode = convertWavFileToHash(TARGET_WAV+mTarget, true);

                                        if(hashCode != null) {
                                            mTargetHash.put(mTarget, hashCode);
                                        }
                                        fileDelete(TARGET_WAV+mTarget);

                                        if(bDontCompare) bMatched = true;
                                    }
                                    else if(msg.obj.toString().equals(UTTERANCE_TRANSFORM_QUERY)) {
                                        String hashCode = convertWavFileToHash(QUERY_WAV+mQuery, false);

                                        if(hashCode != null) {
                                            mQueryHash.put(mQuery, hashCode);

                                            if(mTargetHash.get(mTarget) != null && mQueryHash.get(mQuery) != null) {
                                                if(mTargetHash.get(mTarget).contentEquals(mQueryHash.get(mQuery))) {
                                                    if(!isMapContainsValue(mCandidate, mTarget)) {
                                                        //Log.d(LOGTAG, "WordsCmpByVoice - run - Matched - add to mCandidate(priori: " + priority + ")");
                                                        mCandidate.put(new Integer(priority), mTarget);
                                                    }
                                                    bMatched = true;
                                                }
                                            }
                                        }
                                    }

                                    boolean bTextToFile = false;
                                    while(!bMatched && queryIter.hasNext()) {
                                        if(bStopTTS) {
                                            Log.d(LOGTAG, "WordsCmpByVoice - run - force stop");
                                            return;
                                        }

                                        priority++;
                                        mQuery = queryIter.next().toString();

                                        if(!filteredByDigit(mQuery)) {
                                            if(mTarget.length() == mQuery.length()) {
                                                if(!mQueryHash.containsKey(mQuery)) {
                                                    textToFile(mQuery, false);
                                                    bTextToFile = true;
                                                    break;
                                                }
                                                else {
                                                    if(mTargetHash.get(mTarget) != null && mQueryHash.get(mQuery) != null) {
                                                        if(mTargetHash.get(mTarget).equals(mQueryHash.get(mQuery))) {
                                                            if(!isMapContainsValue(mCandidate, mTarget)) {
                                                                //Log.d(LOGTAG, "WordsCmpByVoice - run - Matched - add to mCandidate(priori: " + priority + ")");
                                                                mCandidate.put(new Integer(priority), mTarget);
                                                            }

                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if(!bTextToFile)
                                        bTextToFile = doRound();

                                    if(!bTextToFile) {
                                        Log.w(LOGTAG, "WordsCmpByVoice - run - No matched!!!");
                                        wcvHandler.getLooper().quit();
                                    }
                                }
                                else if(msg.arg2 == TTS_EVENT_ERROR) {
                                    Log.d(LOGTAG, "WordsCmpByVoice - handleMessage - UTTERANCE_TRANSFORM - onError");

                                    if(msg.obj.toString().equals(UTTERANCE_TRANSFORM_QUERY)) {
                                        if(wcvHandler != null) {
                                            Message message = Message.obtain();
                                            message.arg1 = TTS_STATE_TRANSFORM;
                                            message.arg2 = TTS_EVENT_DONE;
                                            message.obj  = UTTERANCE_TRANSFORM;
                                            wcvHandler.sendMessage(message);
                                        }
                                    }
                                    else if(msg.obj.toString().equals(UTTERANCE_TRANSFORM_TARGET)) {
                                        if(!doRound()) {
                                            Log.w(LOGTAG, "WordsCmpByVoice - onError - No matched!!!");
                                            wcvHandler.getLooper().quit();
                                        }
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }

                    super.handleMessage(msg);
                }
            };

            if(!doRound()) {
                Log.w(LOGTAG, "WordsCmpByVoice - No target");
                return;
            }

            Log.d(LOGTAG, "WordsCmpByVoice - handleMessage - looper START");

            Looper.loop();

            Log.d(LOGTAG, "WordsCmpByVoice - looper STOP");
            return;
        }

        private boolean textToFile(String text, boolean bIsTarget) {
            //Log.d(LOGTAG, "textToFile - text: " + text + ", bIsTarget: " + bIsTarget);

            if(mTTSinitialed == false || mTTS == null) {
                Log.e(LOGTAG, "textToFile - TextToSpeech is not initialized");
                return false;
            }

            String filename;
            if(bIsTarget)
                filename = TARGET_WAV+text;
            else
                filename = QUERY_WAV+text;

            HashMap<String, String> params = new HashMap();
            if(bIsTarget)
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_TRANSFORM_TARGET);
            else
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_TRANSFORM_QUERY);

            mTTS.synthesizeToFile(text, params, filename);
            return true;
        }

        private boolean writeStrinToFile(String sourceFile, String line) {
            //Log.d(LOGTAG, "writeStrinToFile - sourceFile: " + sourceFile + ", line: " + line);

            if(!fileExist(sourceFile)) {
                File file = new File(TARGET_HASH);
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                catch(Exception e) {
                    Log.e(LOGTAG, "writeStrinToFile - create " + sourceFile + " occurs exception: " + e.toString());
                    return false;
                }
            }

            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(TARGET_HASH, true));
                writer.append(line);
            }
            catch(Exception e) {
                Log.e(LOGTAG, "writeStrinToFile - exception: " + e.toString());
                return false;
            }
            finally {
                if(writer != null) {
                    try {
                        writer.close();
                    }
                    catch(Exception e) {
                        Log.e(LOGTAG, "writeStrinToFile - close writer occurs exception: " + e.toString());
                    }
                }
            }

            return true;
        }

        private String getHashCodeByName(String sourceFile, String name) {
            //Log.d(LOGTAG, "getHashCodeByName - name: " + name + ", sourceFile: " + sourceFile);

            if(!fileExist(sourceFile)) {
                Log.d(LOGTAG, "getHashCodeByName - " + sourceFile + " not exist");
                return null;
            }

            String hashcode = null;
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));

                String line;
                while((line = br.readLine()) != null) {
                    String[] param = line.split(" ");

                    if(param[0].equals(name)) {
                        hashcode = param[1];
                        break;
                    }
                }
            }
            catch(Exception e) {
                Log.e(LOGTAG, "getHashCodeByName - readLine occurs exception: " + e.toString());
            }
            finally {
                if(br != null) {
                    try {
                        br.close();
                    }
                    catch (Exception e) {
                        Log.e(LOGTAG, "getHashCodeByName - close bufferedReader occurs exception: " + e.toString());
                    }
                }
            }

            return hashcode;
        }

        private String convertWavFileToHash(String sourceFile, boolean bIsTarget) {
            //Log.d(LOGTAG, "convertWavFileToHash - sourceFile: " + sourceFile + ", bIsTarget: " + bIsTarget);

            if(!fileExist(sourceFile)) {
                Log.d(LOGTAG, "convertWavFileToHash - " + sourceFile + " not exist");
                return null;
            }

            String hashCode = null;
            if(bIsTarget) {
                String param[] = sourceFile.split("tWav_");

                if((hashCode = fileToMD5(sourceFile)) == null) {
                    Log.w(LOGTAG, "convertWavFileToHash(bIsTarget) - hashCode create failed");
                    return null;
                }

                String line = param[1] + " " + hashCode + "\n";
                if(!writeStrinToFile(TARGET_HASH, line)) {
                    Log.w(LOGTAG, "convertWavFileToHash(bIsTarget) - writeStrinToFile failed");
                    return null;
                }
            }
            else {
                hashCode = fileToMD5(sourceFile);
            }

            return hashCode;
        }

        private String fileToMD5(String filePath) {
            //Log.d(LOGTAG, "fileToMD5 - filePath: " + filePath);

            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(filePath); // Create an FileInputStream instance according to the filepath
                byte[] buffer = new byte[1024]; // The buffer to read the file
                MessageDigest digest = MessageDigest.getInstance("MD5"); // Get a MD5 instance

                int numRead = 0; // Record how many bytes have been read
                while(numRead != -1) {
                    numRead = inputStream.read(buffer);
                    if (numRead > 0)
                        digest.update(buffer, 0, numRead); // Update the digest
                }

                byte [] md5Bytes = digest.digest(); // Complete the hash computing
                return convertHashToString(md5Bytes); // Call the function to convert to hex digits
            }
            catch(Exception e) {
                Log.e(LOGTAG, "fileToMD5 occurs exception: " + e.toString());
                return null;
            }
            finally {
                if(inputStream != null) {
                    try {
                        inputStream.close(); // Close the InputStream
                    }
                    catch (Exception e) {
                        Log.e(LOGTAG, "fileToMD5 - close file failed: " + e.toString());
                    }
                }
            }
        }

        private String convertHashToString(byte[] hashBytes) {
            //Log.d(LOGTAG, "convertHashToString");

            String returnVal = "";
            for(int i = 0; i < hashBytes.length; i++) {
                returnVal += Integer.toString(( hashBytes[i] & 0xff) + 0x100, 16).substring(1);
            }
            return returnVal.toLowerCase();
        }
    }

    public SpeechRecognition() {
        Log.d(LOGTAG, "constructor");

        //mActivity = (MainActivity)activity;
        //mHandler = mActivity.getHandler();
        mNoMatched = 0;
        mErrorTimes = 0;
        mSpeechRecognizer = null;
        mStage = VoiceAssistant.RecognitionStage.KEYWORD;

        mCandidate   = new IdentityHashMap<Integer, String>();
        mPossibility = new IdentityHashMap<Integer, String>();

        final MainActivity mainActivity = MainActivity.instance();
        restartRun = new Runnable() {
            @Override
            public void run() {
                Log.w(LOGTAG, "restartSpeechRecognition");

                VoiceAssistant voiceAssistant = mainActivity != null ? mainActivity.getVoiceAssistant() : null;
                if(voiceAssistant != null) {
                    voiceAssistant.restartSpeechRecognition();
                }
            }
        };

        File appDir = mainActivity.getExternalFilesDir(null);
        TARGET_WAV = appDir.toString() + "/tWav_";
        QUERY_WAV = appDir.toString() + "/qWav_";
        TARGET_HASH = appDir.toString() + "/targetHash";
    }

    //-----------------------TextToSpeech-----------------------
    @Override
    public void onInit(int status) {
        Log.d(LOGTAG, "TextToSpeech - onInit");

        if(status == TextToSpeech.SUCCESS) {
            if(mTTS == null) {
                Log.w(LOGTAG, "TextToSpeech - mTTS is null");
                return;
            }

            MainActivity mainActivity = MainActivity.instance();

            int result;
            if(mainActivity.getString(R.string.voice_assistant_nationality).contentEquals("Taiwanese"))
                result = mTTS.setLanguage(Locale.TAIWAN);
            else
                result = mTTS.setLanguage(Locale.US);

            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(LOGTAG, "TextToSpeech - language is NOT supported");
                mTTSinitialed = false;
                return;
            }

            mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Message msg = Message.obtain();
                    if(utteranceId.equals(UTTERANCE_SPEAK)) {
                        //Log.d(LOGTAG, "UtteranceProgressListener - onStart - UTTERANCE_SPEAK");
                    }
                    else if(utteranceId.equals(UTTERANCE_TRANSFORM_TARGET)) {
                        //Log.d(LOGTAG, "UtteranceProgressListener - onStart - UTTERANCE_TRANSFORM_TARGET");

                        if(wcvHandler != null) {
                            msg.arg1 = TTS_STATE_TRANSFORM;
                            msg.arg2 = TTS_EVENT_START;
                            msg.obj  = UTTERANCE_TRANSFORM_TARGET;
                            wcvHandler.sendMessage(msg);
                        }
                    }
                    else if(utteranceId.equals(UTTERANCE_TRANSFORM_QUERY)) {
                        //Log.d(LOGTAG, "UtteranceProgressListener - onStart - UTTERANCE_TRANSFORM_QUERY");

                        if(wcvHandler != null) {
                            msg.arg1 = TTS_STATE_TRANSFORM;
                            msg.arg2 = TTS_EVENT_START;
                            msg.obj  = UTTERANCE_TRANSFORM_QUERY;
                            wcvHandler.sendMessage(msg);
                        }
                    }
                }

                @Override
                public void onDone(String utteranceId) {
                    Message msg = Message.obtain();
                    if(utteranceId.equals(UTTERANCE_SPEAK)) {
                        //Log.d(LOGTAG, "UtteranceProgressListener - onDone - UTTERANCE_SPEAK(" + mEndAfterSpeak + ")");

                        MainActivity mainActivity = MainActivity.instance();
                        VoiceAssistant voiceAssistant = mainActivity != null ? mainActivity.getVoiceAssistant() : null;
                        if(voiceAssistant != null)
                            voiceAssistant.textToSpeechOnFinish(mEndAfterSpeak);

                        mEndAfterSpeak = false;

                        //Log.d(LOGTAG, "UTTERANCE_SPEAK - onDone - before notify");
                        synchronized(mTTSMutex) {
                            mTTSMutex.notifyAll();
                        }
                        //Log.d(LOGTAG, "UTTERANCE_SPEAK - onDone - after notify");
                    }
                    else if(utteranceId.equals(UTTERANCE_TRANSFORM_TARGET)) {
                        //Log.d(LOGTAG, "UtteranceProgressListener - onDone - UTTERANCE_TRANSFORM_TARGET");
                        if(wcvHandler != null) {
                            msg.arg1 = TTS_STATE_TRANSFORM;
                            msg.arg2 = TTS_EVENT_DONE;
                            msg.obj  = UTTERANCE_TRANSFORM_TARGET;
                            wcvHandler.sendMessage(msg);
                        }
                    }
                    else if(utteranceId.equals(UTTERANCE_TRANSFORM_QUERY)) {
                        //Log.d(LOGTAG, "UtteranceProgressListener - onDone - UTTERANCE_TRANSFORM_QUERY");
                        if(wcvHandler != null) {
                            msg.arg1 = TTS_STATE_TRANSFORM;
                            msg.arg2 = TTS_EVENT_DONE;
                            msg.obj  = UTTERANCE_TRANSFORM_QUERY;
                            wcvHandler.sendMessage(msg);
                        }
                    }
                }

                @Override
                public void onError(String utteranceId) {
                    Message msg = Message.obtain();
                    if(utteranceId.equals(UTTERANCE_SPEAK)) {
                        //Log.d(LOGTAG, "UtteranceProgressListener - onError - UTTERANCE_SPEAK(" + mEndAfterSpeak + ")");

                        MainActivity mainActivity = MainActivity.instance();
                        VoiceAssistant voiceAssistant = mainActivity != null ? mainActivity.getVoiceAssistant() : null;
                        if(voiceAssistant != null)
                            voiceAssistant.textToSpeechOnFinish(mEndAfterSpeak);

                        mEndAfterSpeak = false;

                        //Log.d(LOGTAG, "UTTERANCE_SPEAK - onError - before notify");
                        synchronized(mTTSMutex) {
                            mTTSMutex.notifyAll();
                        }
                        //Log.d(LOGTAG, "UTTERANCE_SPEAK - onError - after notify");
                    }
                    else if(utteranceId.equals(UTTERANCE_TRANSFORM_TARGET)) {
                        //Log.d(LOGTAG, "UtteranceProgressListener - onError - UTTERANCE_TRANSFORM_TARGET");
                        if(wcvHandler != null) {
                            msg.arg1 = TTS_STATE_TRANSFORM;
                            msg.arg2 = TTS_EVENT_ERROR;
                            msg.obj  = UTTERANCE_TRANSFORM_TARGET;
                            wcvHandler.sendMessage(msg);
                        }
                    }
                    else if(utteranceId.equals(UTTERANCE_TRANSFORM_QUERY)) {
                        //Log.d(LOGTAG, "UtteranceProgressListener - onError - UTTERANCE_TRANSFORM_QUERY");
                        if(wcvHandler != null) {
                            msg.arg1 = TTS_STATE_TRANSFORM;
                            msg.arg2 = TTS_EVENT_ERROR;
                            msg.obj  = UTTERANCE_TRANSFORM_QUERY;
                            wcvHandler.sendMessage(msg);
                        }
                    }
                }
            });

            mTTSinitialed = true;
            Log.d(LOGTAG, "onInit - TextToSpeech initialized");
        }
        else {
            Log.e(LOGTAG, "onInit - initialize TextToSpeech failed");
            mTTSinitialed = false;
        }
    }

    public int speakOut(String text, boolean bEnd) {
        //Log.d(LOGTAG, "speakOut - text: " + text + ", bEnd: " + bEnd);

        if(mTTSinitialed == false || mTTS == null) {
            Log.e(LOGTAG, "speakOut - TextToSpeech is not initialized");

            MainActivity mainActivity = MainActivity.instance();
            VoiceAssistant voiceAssistant = mainActivity != null ? mainActivity.getVoiceAssistant() : null;
            if(voiceAssistant != null)
                voiceAssistant.textToSpeechOnFinish(bEnd);

            return -1;
        }

        mEndAfterSpeak = bEnd;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_SPEAK);

        int ret = mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, params);

        //Log.d(LOGTAG, "speakOut - before mTTSMutex.wait()");
        synchronized(mTTSMutex) {
            try {
                mTTSMutex.wait();
            }
            catch(Exception ex) {
                Log.d(LOGTAG, "speakOut - mTTSMutex.wait occur exception: " + ex);
            }
        }
        //Log.d(LOGTAG, "speakOut - after mTTSMutex.wait()");

        return ret;
    }

    protected void stopTextToSpeech() {
        Log.d(LOGTAG, "stopTextToSpeech");

        synchronized(mTTSMutex) {
            mTTSMutex.notifyAll();
        }

        if(mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
            mTTS = null;
            mTTSinitialed = false;
        }

        bStopTTS = true;
        if(wcvHandler != null) {
            wcvHandler.getLooper().quit();
            wcvHandler = null;
        }

        wcvThread = null;
    }
    //-----------------------TextToSpeech-----------------------

    public void registerCommand(VoiceAssistant.ICommandHandler handler, String main, String second) {
        if(main.length() == 0) {
            Log.w(LOGTAG, "registerCommand - main command can't be empty");
            return;
        }

        int noSecond = -1;
        int registered = -1;

        for(int i = 0; i < COMMAND_ACTION.size(); i++) {
            VoiceAssistant.CommandHandler  cmdHandler = (VoiceAssistant.CommandHandler)COMMAND_ACTION.get(i);

            if(noSecond == -1 && cmdHandler.commandSecond.length() == 0) {
                noSecond = i;
            }

            if(cmdHandler.commandMain.contentEquals(main) && cmdHandler.commandSecond.contentEquals(second))
                registered = i;
        }

        VoiceAssistant.CommandHandler cmdHandler = new VoiceAssistant.CommandHandler(handler, main, second);
        if(registered != -1) {
            //Log.w(LOGTAG, "registerCommand - already registered");

            COMMAND_ACTION.set(registered, cmdHandler);
            return;
        }

        if(second.length() > 0) {
            if(noSecond == -1)
                COMMAND_ACTION.add(cmdHandler);
            else if(noSecond == 0)
                COMMAND_ACTION.add(0, cmdHandler);
            else
                COMMAND_ACTION.add(noSecond-1, cmdHandler);

        }
        else {
            COMMAND_ACTION.add(cmdHandler);
        }

        Log.d(LOGTAG, "registerCommand - (main: " + main + ", second: " + second + ")");
    }

    public VoiceAssistant.RecognitionStage getStage() {
        return mStage;
    }

    public void startListening(boolean bForceAssistant) {
        Log.d(LOGTAG, "startListening - bForceAssistant: " + bForceAssistant);

        MainActivity mainActivity = MainActivity.instance();
        if(!SpeechRecognizer.isRecognitionAvailable(mainActivity)) {
            Log.w(LOGTAG, "speech recognition service is not available on the system");
            return;
        }

        mEndAfterSpeak = false;

        mCandidate.clear();
        mPossibility.clear();

        if(bForceAssistant)
            mStage = VoiceAssistant.RecognitionStage.ASSISTANT;

        stopListening();
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mainActivity);
        registerListener();

        //stopTextToSpeech();
        if(mTTS == null)
            mTTS = new TextToSpeech(mainActivity, this);
        //mTTSinitialed = false;
        bStopTTS = false;

        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mainActivity.getPackageName());
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_RESULTS);

        if(mStage == VoiceAssistant.RecognitionStage.KEYWORD)
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        mSpeechRecognizerIntent = null;
    }

    public void destroy() {
        Log.d(LOGTAG, "destroy");

        mStage = VoiceAssistant.RecognitionStage.KEYWORD;
        mNoMatched = 0;
        mErrorTimes = 0;

        stopListening();
        stopTextToSpeech();
    }

    public void prepareTargets(List<String> targets) {
        Log.d(LOGTAG, "prepareTargets - size of targets: " +targets.size());

        stopTextToSpeech();

        MainActivity mainActivity = MainActivity.instance();
        mTTS = new TextToSpeech(mainActivity, this);
        mTTSinitialed = false;
        bStopTTS = false;

        if(wcvThread == null) {
            Log.d(LOGTAG, "prepareTargets - create WordsCmpByVoice");
            wcvThread = new WordsCmpByVoice(targets, null, true);
            wcvThread.start();
        }
    }

    public Map<Integer, String> getCandidate() {
        //Log.d(LOGTAG, "getCandidate");
        return mCandidate;
    }

    public Map<Integer, String> getPossibility() {
        //Log.d(LOGTAG, "getPossibility");
        return mPossibility;
    }

    public void recognizeWordsByNumber(List<String> targets, List<String> query) {
        //Log.d(LOGTAG, "recognizeWordsByNumber(target:" + targets + ", query: " + query + ")");

        int weight = 0;
        for(String target: targets) {
            weight = 0;

            for(String q : query) {
                if(!filteredByDigit(q)) {
                    //Log.d(LOGTAG, "recognizeWordsByNumber - continue");
                    continue;
                }

                if(q.contentEquals(target)) {
                    Log.d(LOGTAG, "recognizeWordsByNumber - matched!!!");

                    if(!isMapContainsValue(mCandidate, target))
                        mCandidate.put(new Integer(weight), target);

                    break;
                }

                weight++;
            }
        }
    }

    public void recognizeWordsByString(List<String> targets, List<String> query) {
        //Log.d(LOGTAG, "recognizeWordsByString(targets: " + targets + "query: " + query);

        for(String target : targets) {
            String words = target.replaceAll("\\s+", "").replaceAll("-", "").replaceAll("_", "");
            double threshold = Math.ceil(words.length() / 2);

            char[] wordsArray = words.toCharArray();
            ArrayList<HashMap<Integer, String>> wordsList = new ArrayList<HashMap<Integer, String>>(wordsArray.length);

            boolean haveCandidate = false;
            for(int i = 0; i < wordsArray.length; i++) {
                int weight = 0;
                HashMap<Integer, String> wordsHash = new HashMap<Integer, String>();

                for(String q : query) {
                    if(filteredByDigit(q)) {
                        //Log.d(LOGTAG, "recognizeWordsByString - continue");
                        continue;
                    }

                    if(words.length() != q.length()) {
                        //Log.d(LOGTAG, "recognizeWordsByString - different length");
                        continue;
                    }

                    haveCandidate = true;

                    char[] qArray = q.toCharArray();
                    if(!wordsHash.containsValue(String.valueOf(qArray[i]))) {
                        wordsHash.put(weight, String.valueOf(qArray[i]));
                        weight++;
                    }
                }

                if(!haveCandidate) {
                    //Log.d(LOGTAG, "recognizeWordsByString - no candidates");
                    break;
                }

                wordsList.add(wordsHash);
            }

            if(wordsList.size() == 0) {
                Log.w(LOGTAG, "recognizeWordsByString - can not create wordsList");
                continue;
            }

            String matchedStr = "";
            for(int i = 0; i < wordsArray.length; i++) {
                HashMap<Integer, String> charHash = wordsList.get(i);

                Iterator<Map.Entry<Integer, String>> iterator = charHash.entrySet().iterator();
                while(iterator.hasNext()) {
                    Map.Entry<Integer, String> next = (Map.Entry<Integer, String>) iterator.next();

                    if(next.getValue().equalsIgnoreCase(String.valueOf(wordsArray[i]))) {
                        matchedStr += String.valueOf(next.getKey()) + ",";
                        break;
                    }
                }
            }

            if(!matchedStr.contains(",")) {
                //Log.w(LOGTAG, "recognizeWordsByString - NO MATCHED!!");
                continue;
            }

            int priority = 0;
            for(String s : matchedStr.split(",")) {
                priority += Integer.valueOf(s);
            }
            priority += (wordsArray.length - matchedStr.split(",").length) * MAX_RESULTS;

            if(matchedStr.split(",").length != wordsArray.length && matchedStr.split(",").length >= threshold) {
                Log.d(LOGTAG, "recognizeWordsByString - partial matched");

                if(!isMapContainsValue(mPossibility, target))
                    mPossibility.put(new Integer(priority), target);
            }
            else if(matchedStr.split(",").length == wordsArray.length) {
                Log.d(LOGTAG, "recognizeWordsByString - matched");

                if(!isMapContainsValue(mCandidate, target))
                    mCandidate.put(new Integer(priority), target);
            }
        }
    }

    // solve Homophone case
    public void recongizeWordsByVoice(List<String> targets, List<String> query) {
        //Log.d(LOGTAG, "recongizeWordsByVoice(targets: " + targets + ", query: " + query + ")");

        new AsyncTask<List, Void, List>() {
            @Override
            protected void onPreExecute() {
                //Log.d(LOGTAG, "recongizeWordsByVoice - onPreExecute");
                return;
            }
            @Override
            protected List doInBackground(List... params) {
                //Log.d(LOGTAG, "recongizeWordsByVoice - doInBackground");

                if(params[0].size() <= 0 || params[1].size() <= 0) {
                    Log.w(LOGTAG, "recongizeWordsByVoice - doInBackground - Invalid params");
                    return null;
                }

                List<String> targetList = params[0];
                for(int i = 0; i < targetList.size(); i++) {
                    targetList.set(i, targetList.get(i).replaceAll("\\s+", "").replaceAll("-", "").replaceAll("_", ""));
                }

                List<String> queryList = params[1];
                if(mTTSinitialed && wcvThread == null) {
                    Log.d(LOGTAG, "recongizeWordsByVoice - doInBackground - create WordsCmpByVoice");
                    wcvThread = new WordsCmpByVoice(targetList, queryList, false);
                    wcvThread.start();
                }

                if(wcvThread != null) {
                    try {
                        Log.d(LOGTAG, "recongizeWordsByVoice - doInBackground - before join wcvThread");
                        wcvThread.join();
                        Log.d(LOGTAG, "recongizeWordsByVoice - doInBackground - after join wcvThread");
                    }
                    catch(Exception e) {
                        Log.e(LOGTAG, "recongizeWordsByVoice - doInBackground - join wcvThread error: " + e.toString());
                    }
                    wcvThread = null;
                }

                return queryList;
            }

            @Override
            protected void onPostExecute(List qlist) {
                //Log.d(LOGTAG, "recongizeWordsByVoice - onPostExecute");

                //Log.d(LOGTAG, "recongizeWordsByVoice - before mTTSMutex.notifyAll");
                synchronized(mTTSMutex) {
                    mTTSMutex.notifyAll();
                }
                //Log.d(LOGTAG, "recongizeWordsByVoice - after mTTSMutex.notifyAll");

                // clean files
                if(qlist != null) {
                    for (String query : (List<String>) qlist) {
                        Log.d(LOGTAG, "recongizeWordsByVoice - clean - query: " + query);
                        fileDelete(QUERY_WAV + query);
                    }
                }

                return;
            }
        }.execute(targets, query);

        //Log.d(LOGTAG, "recongizeWordsByVoice - before mTTSMutex.wait");
        synchronized(mTTSMutex) {
            try {
                mTTSMutex.wait();
            }
            catch(Exception ex) {
                Log.d(LOGTAG, "recongizeWordsByVoice - mTTSMutex.wait occurs exception: " + ex.toString());
            }
        }
        //Log.d(LOGTAG, "recongizeWordsByVoice - after mTTSMutex.wait");
    }

    private void registerListener() {
        Log.d(LOGTAG, "registerListener");

        if(mSpeechRecognizer != null) {
            mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {

                @Override
                public void onReadyForSpeech(Bundle params) {
                    //Log.d(LOGTAG, "SpeechRecognizer - onReadyForSpeech");

                    if(mStage == VoiceAssistant.RecognitionStage.ASSISTANT) {
                        try {
                            Thread.sleep(300);
                        }
                        catch(Exception ex) {
                            Log.e(LOGTAG, "onReadyForSpeech - sleep() occurs exception: " + ex.toString());
                        }

                        MainActivity mainActivity = MainActivity.instance();
                        Handler handler = mainActivity==null?null:mainActivity.getHandler();

                        if(handler != null) {
                            Log.d(LOGTAG, "onReadyForSpeech - Start VoiceAssistant");

                            Message msg = Message.obtain();
                            msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
                            msg.arg1 = 1;
                            msg.obj = "Start VoiceAssistant";

                            handler.sendMessage(msg);
                        }
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    //Log.d(LOGTAG, "SpeechRecognizer - onResults(" + Thread.currentThread().getId() + ")");

                    MainActivity mainActivity = MainActivity.instance();
                    Handler handler = mainActivity==null?null:mainActivity.getHandler();

                    if(handler != null) {
                        //Log.d(LOGTAG, "onResults - stop restart timer");
                        handler.removeCallbacks(restartRun);
                    }

                    VoiceAssistant voiceAssistant = mainActivity != null ? mainActivity.getVoiceAssistant() : null;
                    commandHandling = Integer.MAX_VALUE;

                    final ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                    // debug---------------------------------------------------
                    Object[] txt = matches.toArray();
                    for(int i = 0; i < matches.size(); i++)
                        Log.d(LOGTAG, "SpeechRecognizer - results: " + txt[i]);
                    //---------------------------------------------------------

                    if(new CityPreference(mainActivity).getVoiceAssistantDebug()
                            && mStage == VoiceAssistant.RecognitionStage.ASSISTANT) {
                        //Log.d(LOGTAG, "SpeechRecognizer - onResults - debug mode");
                        voiceAssistant.createDialog(matches, 1, null, null);
                    }

                    if(mStage == VoiceAssistant.RecognitionStage.KEYWORD) {
                        //Log.d(LOGTAG, "SpeechRecognizer - onResults - KEYWORD STAGE");

                        for(String req : matches) {
                            String trimReq = req.replaceAll("\\s+", "").replaceAll("-", "").replaceAll("_", "").toLowerCase();

                            if(trimReq.contains(KEYWORD)) {
                                Log.d(LOGTAG, "SpeechRecognizer - KEYWORD matched!!!");

                                mStage = VoiceAssistant.RecognitionStage.ASSISTANT;

                                if(mainActivity != null)
                                    mainActivity.screenToggle(true);

                                break;
                            }
                        }

                        stopListening();

                        if(handler != null) {
                            Log.d(LOGTAG, "onResults - Start SpeechRecognition");

                            Message msg = Message.obtain();
                            msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
                            msg.arg1 = 0;
                            msg.arg2 = 0;
                            msg.obj = "Start SpeechRecognition";

                            handler.sendMessage(msg);
                        }
                    }
                    else if(mStage == VoiceAssistant.RecognitionStage.ASSISTANT) {
                        Log.d(LOGTAG, "SpeechRecognizer - onResults - ASSISTANT STAGE");

                        String userInput = "";
                        String byebye = mainActivity!=null?(mainActivity.getString(R.string.voice_assistant_goodbye)):"";
                        int matchedIdx = Integer.MAX_VALUE;

                        for(String req : matches) {
                            String trimReq = req.replaceAll("\\s+", "").replaceAll("-", "").replaceAll("_", "").toLowerCase();

                            if(trimReq.contentEquals(byebye.replaceAll("\\s+", "").toLowerCase())) {
                                Log.w(LOGTAG, "SpeechRecognizer - onResults - force restart");

                                if(voiceAssistant != null) {
                                    voiceAssistant.speechRecognitionOnResults(-1);
                                }
                                return;
                            }

                            for(int i = 0; i < COMMAND_ACTION.size(); i++) {
                                String main = COMMAND_ACTION.get(i).commandMain.replaceAll("\\s+", "").replaceAll("-", "").replaceAll("_", "").toLowerCase();
                                String second = COMMAND_ACTION.get(i).commandSecond.replaceAll("\\s+", "").replaceAll("-", "").replaceAll("_", "").toLowerCase();

                                if(trimReq.contains(main)) {
                                    if(second.length() > 0 && trimReq.contains(second)) {
                                        matchedIdx = i;
                                        break;
                                    }
                                    else if(second.length() == 0) {
                                        matchedIdx = i;
                                        break;
                                    }
                                }
                            }

                            if(commandHandling > matchedIdx) {
                                commandHandling = matchedIdx;
                                userInput = req;
                            }
                        }

                        if(commandHandling == Integer.MAX_VALUE) {
                            Log.w(LOGTAG, "SpeechRecognizer - onResults: NO matched command");

                            //mStage = VoiceAssistant.RecognitionStage.KEYWORD;
                            if(voiceAssistant != null) {
                                voiceAssistant.speechRecognitionOnResults(++mNoMatched);
                            }

                            return;
                        }

                        mNoMatched = 0;

                        if (voiceAssistant != null)
                            voiceAssistant.createDialog(userInput, 1);

                        final List<String> trimFiltered = new ArrayList<String>();
                        for(String req : matches) {
                            String trimReq = req.replaceAll("\\s+", "").replaceAll("-", "").replaceAll("_", "").toLowerCase();
                            String main = COMMAND_ACTION.get(commandHandling).commandMain.replaceAll("\\s+", "").replaceAll("-", "").replaceAll("_", "").toLowerCase();

                            int idx = trimReq.indexOf(main);
                            if(idx != -1) {
                                String target = "";

                                if(idx + main.length() < trimReq.length())
                                    target = trimReq.substring(idx + main.length());

                                if(target.length() > 0 && !trimFiltered.contains(target)) {
                                    trimFiltered.add(target);
                                }
                            }
                        }

                        VoiceAssistant.CommandHandler cmdHandler = COMMAND_ACTION.get(commandHandling);
                        final VoiceAssistant.ICommandHandler IcmdHandler = cmdHandler.handler;
                        if(IcmdHandler != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(LOGTAG, "onResults - commandHandler");
                                    IcmdHandler.commandHandler(COMMAND_ACTION.get(commandHandling).commandMain, COMMAND_ACTION.get(commandHandling).commandSecond, trimFiltered);
                                }
                            }).start();
                        }

                        stopListening();
                        mStage = VoiceAssistant.RecognitionStage.KEYWORD;

                        if(voiceAssistant != null)
                            voiceAssistant.speechRecognitionOnResults(0);
                    }
                }

                @Override
                public void onError(int error) {
                    Log.w(LOGTAG, "SpeechRecognizer - onError");

                    MainActivity mainActivity = MainActivity.instance();
                    Handler handler = mainActivity==null?null:mainActivity.getHandler();

                    if(handler != null) {
                        //Log.d(LOGTAG, "onError - stop restart timer");
                        handler.removeCallbacks(restartRun);
                    }

                    if(mStage == VoiceAssistant.RecognitionStage.ASSISTANT)
                        mErrorTimes++;
                    else
                        mErrorTimes = 0;

                    stopListening();

                    VoiceAssistant voiceAssistant = mainActivity!= null ? mainActivity.getVoiceAssistant() : null;
                    if(voiceAssistant != null)
                        voiceAssistant.speechRecognitionOnError(mStage, error, mErrorTimes);
                }

                @Override
                public void onEndOfSpeech() {
                    //Log.d(LOGTAG, "SpeechRecognizer - onEndOfSpeech");

                    MainActivity mainActivity = MainActivity.instance();
                    Handler handler = mainActivity==null?null:mainActivity.getHandler();

                    if(handler != null) {
                        //Log.d(LOGTAG, "onEndOfSpeech - start timer to restart voiceAssistant");
                        handler.postDelayed(restartRun, 5000);
                    }
                }

                /*-------------------- No implemented --------------------*/
                @Override
                public void onBeginningOfSpeech() {
                    Log.d(LOGTAG, "SpeechRecognizer - onBeginningOfSpeech");
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    //Log.d(LOGTAG, "SpeechRecognizer - onPartialResults");
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    //Log.d(LOGTAG, "SpeechRecognizer - onEvent");
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    //Log.d(LOGTAG, "SpeechRecognizer - onBufferReceived");
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    //Log.d(LOGTAG, "SpeechRecognizer - onRmsChanged");
                }
            });
        }
    }

    public void stopListening() {
        Log.d(LOGTAG, "stopListening");

        if(mSpeechRecognizer != null) {
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }
    }

    private boolean filteredByDigit(String input) {
        //Log.d(LOGTAG, "filteredByDigit - input: " + input);

        String regex = "\\d+";
        if(input.matches(regex)) {
            Log.d(LOGTAG, "filteredByDigit - is all digital");
            return true;
        }

        Log.d(LOGTAG, "filteredByDigit - is NOT digital");
        return false;
    }

    private boolean filteredByCharacter(String input) {
        //Log.d(LOGTAG, "filteredByCharacter - input: " + input);

        String regex = "\\D+";

        if(input.matches(regex)) {
            Log.d(LOGTAG, "filteredByCharacter - is all character");
            return true;
        }

        Log.d(LOGTAG, "filteredByCharacter - is NOT character");
        return false;
    }

    private Boolean isMapContainsValue(Map<Integer, String> map, String value) {
        //Log.d(LOGTAG, "isMapContainsValue - map: " + map + ", value: " + value);

        for(Map.Entry<Integer, String> entry : map.entrySet()) {
            String str = entry.getValue();

            if(str.contentEquals(value)) {
                return true;
            }
        }

        return false;
    }

    private boolean fileExist(String filename) {
        //Log.d(LOGTAG, "fileExist - filename: " + filename);

        File f = new File(filename);
        if(f.exists() && !f.isDirectory()) {
            Log.d(LOGTAG, "fileExist - " + filename + " exist");
            return true;
        }

        Log.d(LOGTAG, "fileExist - " + filename + " not existed");
        return false;
    }

    private void fileDelete(String filename) {
        //Log.d(LOGTAG, "fileDelete - filename: " + filename);

        if(fileExist(filename)) {
            try {
                File qFile = new File(filename);
                qFile.delete();
            }
            catch(Exception e) {
                Log.e(LOGTAG, "fileDelete - delete " + filename + " failed!");
            }
        }
    }
}
