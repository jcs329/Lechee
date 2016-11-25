package com.quantatw.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;

import java.io.File;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SnapshotActivity extends Activity {
    private String mfilename="";
    private Camera mCamera;
    private String TAG = "ledebug";
    private String saveName="abc.jpg";
    private File sourceFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SurfaceView surface = new SurfaceView(this);
        mCamera = Camera.open(0);
        try {
            mCamera.setPreviewDisplay(surface.getHolder());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mCamera.startPreview();
        mCamera.takePicture(null, null, mPicture);
        finish();
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.flush();
                fos.close();
                refreshGallery(pictureFile);
//                mUtil.telbot_msg(MainActivity.deviceID+" photo "+saveName);
//                new ImageUploader().execute();
                new ImageUploader_MQTT().execute();

            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            } finally {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }
    };

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyMMddHHmm").format(new Date());
        File mediaFile;
        saveName = "IMG_" + timeStamp + RandomN() +".jpg";
        mfilename = mediaStorageDir.getPath() + File.separator + saveName;
        mediaFile = new File(mfilename);

        return mediaFile;
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(android.net.Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    private String RandomN() {
        String[] randomHard = new String[20];
        int randomWordIndex = (int) (Math.random() * 19);
        for (int j = 0; j < 20; j++) {
            int randomWordNum = (int) (Math.random() * 10);
            char randomLetter = (char) (Math.random() * 26 + 'a');
            if (randomWordNum % 2 == 0) {
                randomHard[j] = randomWordNum + "";
            } else {
                randomHard[j] = String.valueOf(randomLetter);
            }
        }
        return randomHard[randomWordIndex];
    }

    private class ImageUploader_MQTT extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String result = "OK";
            sourceFile = new File(mfilename);
            if (!sourceFile.isFile()) {
            } else {
                    MainActivity.lastTopicID = "IMG/"+mUtil.getRandomHexString(15);
                    mUtil.telbot_msg(MainActivity.deviceID+" photo2 "+MainActivity.lastTopicID + " "+saveName);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                mUtil.telbot_sendImage(sourceFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ImageUploader extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            // TODO Auto-generated method stub
            String result = "";
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1000 * 1000;


            FileOutputStream fos = null;
            String fileName = saveName;
            File sourceFile = new File(mfilename);

            if (!sourceFile.isFile()) {
                result = "Source File not exist!";
            } else {
                try {

                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL("http://192.168.0.100/upload.php");

                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    conn.setRequestProperty("file", fileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""
                            + fileName + "\"" + lineEnd);
                    dos.writeBytes("Content-Type:image/jpg" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }
                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                    int response = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();
                    InputStream inputStream = null;
                    inputStream = conn.getInputStream();

                    return result;
                } catch (Exception e) {
                    return null;
                }
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
        }

    }



}
