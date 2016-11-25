package com.quantatw.myapplication;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.quantatw.myapplication.voiceAssistant.VoiceAssistant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by youjun on 2016/6/17.
 */
public class QHomeTcpService {

    private static final String LOGTAG = "QHomeTcpService";

    public static final int SERVER_PORT = 6666;

    private static ServerSocket mServerSocket;
    private Set<Socket> mClientList;

    private static volatile boolean mStartService;
    private boolean mRunning;

    private Thread mTcpThread;

    private Runnable TcpServerRun = new Runnable() {
        @Override
        public void run() {

            mRunning = true;

            try {
                if(!mServerSocket.isBound()) {
                    Log.w(LOGTAG, "server socket bind failed");

                    mServerSocket.setReuseAddress(true);
                    mServerSocket.bind(new InetSocketAddress(SERVER_PORT));
                    Log.d(LOGTAG, "TcpServerRun - bind on " + SERVER_PORT);
                }

                while(true) {
                    Socket client = mServerSocket.accept();

                    if(!mStartService) {
                        Log.w(LOGTAG, "force to stop TcpServerRun thread");
                        return;
                    }

                    if(!mClientList.add(client)) {
                        Log.d(LOGTAG, "TcpServerRun - remove " + client.getRemoteSocketAddress().toString());
                        mClientList.remove(client);
                        mClientList.add(client);
                    }
                    Log.d(LOGTAG, "TcpServerRun - " + client.getRemoteSocketAddress().toString() + " is accepted");

                    // handle message
                    new Thread(new ReqHandler(client)).start();
                }
            }
            catch(Exception ex) {
                Log.e(LOGTAG, "TcpServerRun - Exception: " + ex);

                /*try {
                    mServerSocket.close();
                }
                catch(Exception e) {}*/
            }

            mRunning = false;
        }
    };

    private class ReqHandler implements Runnable{

        private Socket mSocket;

        public ReqHandler(Socket socket) {
            this.mSocket = socket;
        }
        @Override
        public void run() {
            try {
                MainActivity mainActivity = MainActivity.instance();
                BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

                while(mStartService) {
                    String req = in.readLine();
                    Log.d(LOGTAG, "ReqHandler - receive " + req);

                    if(req.length() > 0) {
                        Handler handler = mainActivity==null?null:mainActivity.getHandler();
                        if(handler != null) {
                            Message msg = Message.obtain();
                            msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
                            msg.arg1 = -2;

                            handler.sendMessage(msg);
                        }

                        VoiceAssistant voiceAssistant = mainActivity==null?null:mainActivity.getVoiceAssistant();
                        if(voiceAssistant != null) {
                            voiceAssistant.commandHandler(req);
                        }

                        if(handler != null) {
                            Message msg = Message.obtain();
                            msg.what = MainActivity.HANDLER_EVENT_VOICE_ASSISTANT;
                            msg.arg1 = 0;
                            msg.arg2 = 0;

                            handler.sendMessage(msg);
                        }

                        break;
                    }
                }
            }
            catch(Exception ex) {
                Log.e(LOGTAG, "ReqHandler - Exception: " + ex);
            }
            finally {
                try {
                    mSocket.close();
                }
                catch(Exception e) {}

                mClientList.remove(mSocket);
            }
        }
    }

    static {
        try {
            mServerSocket = new ServerSocket();
            mServerSocket.setReuseAddress(true);
            mServerSocket.bind(new InetSocketAddress(SERVER_PORT));
            Log.d(LOGTAG, "TcpServerRun - bind on " + SERVER_PORT);
        }
        catch(Exception ex) {}
    }

    public QHomeTcpService(Activity activity) {
        Log.d(LOGTAG, "QHomeTcpService");

        mRunning = false;
        mStartService = false;
        mClientList = new HashSet<Socket>();
    }

    public boolean isRunning() { return mRunning; }

    public void startService() {
        Log.d(LOGTAG, "startService");

        //stopService();

        mStartService = true;
        mTcpThread = new Thread(TcpServerRun);
        mTcpThread.start();
    }

    public void stopService() {
        Log.d(LOGTAG, "stopService");

        mStartService = false;
        mTcpThread = null;

        for(Socket sock : mClientList) {
            try {
                sock.close();
            }
            catch (Exception ex) {
                Log.e(LOGTAG, "stopService - closeClient Exception: " + ex);
            }
        }

        /*try {
            mServerSocket.close();
            mServerSocket = null;
        }
        catch(Exception ex) {
            Log.e(LOGTAG, "stopService- closeServer Exception: " + ex);
        }*/
    }
}
