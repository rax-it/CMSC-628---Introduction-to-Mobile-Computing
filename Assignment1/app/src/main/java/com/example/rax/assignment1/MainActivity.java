package com.example.rax.assignment1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final long TIME_DIFFERENCE = 120000;

    Button buttonMonitor;
    Button buttonShow;
    protected TextView textViewActivity;
    MyService myService;
    boolean mBound = false;
    private String userActivity;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private int countButtonShowPress = 0;
    private Timer mTimer;
    private pollToDisplay asyncPoll = new pollToDisplay();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonMonitor = (Button) findViewById(R.id.buttonMonitor);
        buttonMonitor.setOnClickListener(this);

        buttonShow = (Button) findViewById(R.id.buttonShow);
        buttonShow.setOnClickListener(this);

        textViewActivity = (TextView) findViewById(R.id.textViewActivity);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void bindMethod(View v) {
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unBindMethod(View v) {
        unbindService(mConnection);
        mBound = false;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            myService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void writeToSDFile(String s) {
        if (isExternalStorageWritable()) {

            File dir = new File(Environment.getExternalStorageDirectory() + "/UserActivity");
            dir.mkdirs();
            File file = new File(dir, "UserActivity.txt");

            try {
                FileOutputStream mFileOutputStream = new FileOutputStream(file, true);
                PrintWriter pWriter = new PrintWriter(mFileOutputStream);
                if(s!=null)
                    pWriter.println(s);
                pWriter.flush();
                pWriter.close();
                mFileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Log.d("WRITE TO FILE", "writeToSDFile: " + "External media not mounted, cannot write to file.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    public void startDisplaying() {
//        mTimer = new Timer(true);
//        mTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        writeToSDFile();
//                        String s = textViewActivity.getText().toString();
//                        textViewActivity.setText(s+"\n"+myService.getCalculatedActivity());
//                    }
//                });
//            }
//        }, 0, TIME_DIFFERENCE);
//    }
//
//    public void stopDisplaying() {
//        textViewActivity.setText(null);
//        mTimer.cancel();
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.buttonMonitor:
                bindMethod(v);
//                if (mBound)
                    Toast.makeText(getBaseContext(),"Service started", Toast.LENGTH_LONG).show();
                break;

            case R.id.buttonShow:

                // First time the button is clicked
                if (mBound && countButtonShowPress == 0) {
                    myService.StartMonitoring();
//                    new java.util.Timer().schedule(
//                            new java.util.TimerTask() {
//                                @Override
//                                public void run() {
//                                    asyncPoll.execute();
//                                }
//                            },
//                            2100
//                    );
                    asyncPoll.execute();
                    buttonShow.setText("Stop Displaying Activity");
                    countButtonShowPress++;
                }

                else if (mBound && countButtonShowPress %2 == 0) {
                    asyncPoll = new pollToDisplay();
                    asyncPoll.execute();
                    buttonShow.setText("Stop Displaying Activity");
                    countButtonShowPress++;
                }

                else if (mBound && countButtonShowPress %2 == 1) {
                    // Clearing textview and cancelling polling task
                    asyncPoll.cancel(true);
                    textViewActivity.setText(null);
                    buttonShow.setText("Start Displaying Activity");
                    countButtonShowPress++;
                }
                break;
        }
    }

    private class pollToDisplay extends AsyncTask {
        int i=0;
        @Override
        protected String doInBackground(Object[] params) {
            mTimer = new Timer(true);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
//                    writeToSDFile();
                    publishProgress();
                }
            }, 0, TIME_DIFFERENCE);
            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            Log.d("ASYNC", "run: " + i++);
            String s = myService.getCalculatedActivity();
            writeToSDFile(s);
            Log.d("Main", "onProgressUpdate: "+s);
            textViewActivity.setText(textViewActivity.getText().toString()+"\n" + s);
        }

        @Override
        protected void onCancelled() {
            textViewActivity.setText(null);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

//         ATTENTION: This was auto-generated to implement the App Indexing API.
//         See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Main Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.example.rax.assignment1/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(client, viewAction);
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Main Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.example.rax.assignment1/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(client, viewAction);
//        client.disconnect();
//    }
}
