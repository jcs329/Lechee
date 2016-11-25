package com.quantatw.myapplication.voiceAssistant;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.quantatw.myapplication.CityPreference;
import com.quantatw.myapplication.MainActivity;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

/**
 * Created by youjun on 2016/5/11.
 */
public class KeywordDetect implements RecognitionListener {

    private static final String LOGTAG = "KeywordDetect";

    private static final String KEYPHRASE = "ok sophia"; // okay sophia
    private static final String KWS_SEARCH = "wakeup";

    private float mSensitivity;

    public enum RecognizerState {
        INITIAL (0) {
            public String toString() {
                return "INITIAL";
            }
        },
        CREATING (1) {
            public String toString() {
                return "CREATING";
            }
        },
        CREATED (2) {
            public String toString() {
                return "CREATED";
            }
        },
        LISTENING (3) {
            public String toString() {
                return "LISTENING";
            }
        },
        STOPED (4) {
            public String toString() {
                return "STOPED";
            }
        },
        BEGINING_SPEECH (5) {
            public String toString() {
                return "BEGINING_SPEECH";
            }
        },
        PARTIAL_RESULT (6) {
            public String toString() {
                return "PARTIAL_RESULT";
            }
        },
        END_SPEECH (7) {
            public String toString() {
                return "END_SPEECH";
            }
        },
        ON_RESULT (8) {
            public String toString() {
                return "ON_RESULT";
            }
        },
        TIMEOUT (9) {
            public String toString() {
                return "TIMEOUT";
            }
        },
        ERROR (10){
            public String toString() {
                return "ERROR";
            }
        };

        private int value;

        private RecognizerState(int val) {
            this.value = val;
        }

        public int get() {
            return this.value;
        }
    }

    private Handler mHandler;
    private MainActivity mActivity;

    private SpeechRecognizer recognizer;
    private RecognizerState state = RecognizerState.INITIAL;

    private boolean startDetect;
    private Thread detectThread;

    private Runnable detectRunable = new Runnable() {
        @Override
        public void run() {
            synchronized(mActivity) {
                while(startDetect) {
                    try {
                        if(getState().get() == RecognizerState.ERROR.get()) {
                            Log.w(LOGTAG, "detectRunable - state error");
                            return;
                        }

                        if(getState().get() == RecognizerState.INITIAL.get()) {
                            Log.d(LOGTAG, "detectRunable - recognizer is in " + RecognizerState.INITIAL.toString() + ". create recognizer");

                            if(mHandler != null) {
                                Message msg = Message.obtain();
                                msg.what = MainActivity.HANDLER_EVENT_KEYWORD_DETECT;
                                msg.arg1 = 0;
                                msg.obj = "Create KeywordDetect";

                                mHandler.sendMessage(msg);
                            }

                            Thread.sleep(500);
                            continue;
                        }

                        if(getState().get() >= RecognizerState.CREATED.get()) {
                            Log.d(LOGTAG, "detectRunable - recognizer is created. start keywordDetect");

                            if(startDetect && start())
                                break;
                        }

                        Thread.sleep(500);
                    }
                    catch(Exception ex) {
                        Log.e(LOGTAG, "detectRunable occurs exception: " + ex.toString());
                    }
                }
            }
        }
    };

    public KeywordDetect(MainActivity activity) {
        Log.d(LOGTAG, "constructor");

        mActivity = activity;
        mHandler = mActivity.getHandler();
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        //Log.d(LOGTAG, "onPartialResult");

        state = RecognizerState.PARTIAL_RESULT;

        if(hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if(text.equals(KEYPHRASE)) {
            Log.i(LOGTAG, "onPartialResult - Keyword detected!!");

            destroy();
            mActivity.screenToggle(true);

            if(mHandler != null) {
                //Log.d(LOGTAG, "onPartialResult - Keyword detected!! - start speechRecognition");

                Message msg = Message.obtain();
                msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
                msg.arg1 = 0;
                msg.obj = "Start SpeechRecognition";

                mHandler.sendMessage(msg);
            }
        }
        else {
            //Log.d(LOGTAG, "onPartialResult - NOT Keyword");
            start();
        }
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        //Log.d(LOGTAG, "onResult");
        state = RecognizerState.ON_RESULT;
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(LOGTAG, "onBeginningOfSpeech");
        state = RecognizerState.BEGINING_SPEECH;
    }

    @Override
    public void onEndOfSpeech() {
        //Log.d(LOGTAG, "onEndOfSpeech");
        state = RecognizerState.END_SPEECH;
    }

    @Override
    public void onError(Exception error) {
        Log.e(LOGTAG, "onError: " + error.getMessage());

        state = RecognizerState.ERROR;
        destroy();
        //create();

        if(mHandler != null) {
            Message msg = new Message();
            msg.what = MainActivity.HANDLER_EVENT_KEYWORD_DETECT;
            msg.arg1 = 1;
            msg.obj = "Start KeywordDetect";

            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void onTimeout() {
        //Log.w(LOGTAG, "onTimeout");
        state = RecognizerState.TIMEOUT;
        start();
    }

    public RecognizerState getState() {
        //Log.d(LOGTAG, "getStatus - state: " + state);
        return state;
    }

    public void startKeywordDetect() {
        Log.d(LOGTAG, "startKeywordDetect");

        if(!new CityPreference(mActivity).getVoiceAssistantEnabled()) {
            Log.w(LOGTAG, "startKeywordDetect - voice assistant is disabled");
            return;
        }

        stopKeywordDetect();

        startDetect = true;
        detectThread = new Thread(detectRunable);
        detectThread.start();
    }

    public void stopKeywordDetect() {
        Log.d(LOGTAG, "stopKeywordDetect");

        /*if(detectThread == null) {
            Log.w(LOGTAG, "detectThread is null");
            return;
        }*/

        startDetect = false;
        stop(false);
        detectThread = null;

        //Log.d(LOGTAG, "detectThread - stopKeywordDetect return");
    }

    public void create(){
        Log.d(LOGTAG, "create");

        if(state == RecognizerState.CREATING) {
            Log.w(LOGTAG, "speechRecognizer is on CREATING state");
            return;
        }

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Log.i(LOGTAG, "speechRecognizer is creating");
                    state = RecognizerState.INITIAL;

                    Assets assets = new Assets(mActivity);
                    File assetDir = assets.syncAssets();

                    setupRecognizer(assetDir);
                }
                catch (IOException ex) {
                    return ex;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Exception ex) {
                if(ex != null) {
                    Log.e(LOGTAG, "failed to init recognizer(" + ex.toString() + ")");
                    state = RecognizerState.INITIAL;
                }
                else {
                    Log.i(LOGTAG, "speechRecognizer is created");
                    state = RecognizerState.CREATED;
                    //start();
                }
            }
        }.execute();
    }

    public void destroy() {
        //Log.d(LOGTAG, "destroy");

        if(recognizer == null) {
            Log.w(LOGTAG, "destroy - recognizer is null");
            return;
        }

        startDetect = false;
        detectThread = null;

        recognizer.cancel();
        recognizer.shutdown();
        recognizer = null;
        state = RecognizerState.INITIAL;

        Log.i(LOGTAG, "speechRecognizer is destroyed");
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        Log.i(LOGTAG, "setupRecognizer");

        state = RecognizerState.CREATING;

        String sensitivity = new CityPreference(mActivity).getKeywordSensitivity();
        String scientificStr = "1.0E-" + sensitivity;

        BigDecimal bigDecimal = new BigDecimal(scientificStr);
        mSensitivity = Float.parseFloat(bigDecimal.toPlainString());

        File modelsDir = new File(assetsDir, "models");
        recognizer = defaultSetup()
                .setAcousticModel(new File(modelsDir, "hmm/cmusphinx-en-us-ptm-5.2"))
                .setDictionary(new File(modelsDir, "dict/cmudict-en-us.dict"))
                .setKeywordThreshold(mSensitivity) //30
                .setBoolean("-allphone_ci", true)
                .getRecognizer();

        recognizer.addListener(this);
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
    }

    private boolean start() {
        Log.i(LOGTAG, "start");

        if(state.get() <= RecognizerState.CREATING.get()) {
            Log.w(LOGTAG, "start - wrong state of speechRecognizer");
            return false;
        }

        state = RecognizerState.LISTENING;
        recognizer.stop();

        return recognizer.startListening(KWS_SEARCH);
    }

    private void stop(boolean cancel) {
        Log.d(LOGTAG, "stop(cancel: " + cancel + ")");

        if(recognizer == null)
            return;

        if(cancel)
            recognizer.cancel();
        else
            recognizer.stop();

        state = RecognizerState.STOPED;

        //Log.d(LOGTAG, "stop finished");
    }
}
