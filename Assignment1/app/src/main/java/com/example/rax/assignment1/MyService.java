package com.example.rax.assignment1;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class MyService extends Service implements SensorEventListener, LocationListener {
    private final long ACC_EVENT_TIME_DIFF = 1000;
    private final int MAX_EVENT_COUNT = 120;
    private final IBinder mBinder = new LocalBinder();

    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Sensor mAccSensor;
    private Sensor mGyroSensor;
    private float accX = 0, accY = 0, accZ = 0;
    private Handler myHandler;
    private float gravity[] = new float[3];
    private float linear_acceleration[] = new float[3];
    public String GlobalVar;
    private String calculatedActivity = "";
    String s;
    Queue<String> userActivityQueue = new LinkedList<String>();
    StringBuffer activityBuffer = new StringBuffer();
    boolean sent = true;

    private float accArrX[] = new float[120];
    private float accArrY[] = new float[120];
    private float accArrZ[] = new float[120];
    private long eventTime[] = new long[120];
    long lastEventTime = 0;
    int eventCount = 0;

    public MyService() {
        myHandler = new Handler();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        gravity[0] = (float) 9.81;
        gravity[1] = (float) 9.81;
        gravity[2] = (float) 9.81;
        return mBinder;
    }

    @Override
    public void onLocationChanged(Location location) {
//        double mLatitude = location.getLatitude();
//        double mLogitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    public void StartMonitoring() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Registering Sensor event listeners
        mSensorManager.registerListener(this, mAccSensor, SensorManager.SENSOR_DELAY_FASTEST);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getCalculatedActivity(){
        Log.d("RETURN", "getCalculatedActivity: "+activityBuffer.toString());
//        return calculatedActivity;
        sent = true;
        return activityBuffer.toString();
    }

    public void calculateActivity() {
//        Log.d("System time long", "calculateActivity: "+System.currentTimeMillis());
//        Log.d("System time", "calculateActivity: " + getTime(System.currentTimeMillis()));
        int sleeping = 0, sitting = 0, walking = 0;
        String startTime = getTime(eventTime[0]);
        String endTime = getTime(eventTime[MAX_EVENT_COUNT-1]);

        for(int index = 0; index < MAX_EVENT_COUNT; index++){
            // abs(Z) is greater than 7 so considering phone is sleeping
//            Log.d("CALCULATE", "calculateActivity: X "+accArrX[index]+" Y "+accArrY[index]+" Z "+accArrZ[index]);
            if(Math.abs(accArrX[index]) < 2 && Math.abs(accArrY[index]) < 3 && Math.abs(accArrZ[index]) > 7) {
                sleeping++;
            }
            // abs(Y) is greater than 7 so considering phone is sitting
            else if(Math.abs(accArrX[index]) < 2 && Math.abs(accArrY[index]) > 7 && Math.abs(accArrZ[index]) < 2) {
                sitting++;
            }
            else if(Math.abs(accArrX[index]) < 2 && Math.abs(accArrY[index]) > 7 && Math.abs(accArrZ[index]) >=2){
                walking++;
            }
        }

        if (sleeping > sitting && sleeping > walking)
            calculatedActivity = startTime+" - "+ endTime +" Sleeping zzzzz ";
        else if (sitting > sleeping && sitting > walking)
            calculatedActivity = startTime+" - "+ endTime +" Standing / Sitting! ";
//        + sleeping +" Stand " + sitting + " Walk "+walking;
        else if (walking > sleeping && walking > sitting)
            calculatedActivity = startTime+" - "+ endTime +" Walking / Running ";
        else
            calculatedActivity = startTime+" - "+ endTime +"Unknown";

        Log.d("CALCULATED activity", "calculateActivity: "+calculatedActivity);
//        userActivityQueue.add(calculatedActivity);
        if (sent == true){
            activityBuffer = new StringBuffer();
            activityBuffer.append(calculatedActivity);
            sent = false;
        }
        else if(sent == false){
            activityBuffer.append("\n");
            activityBuffer.append(calculatedActivity);
        }
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                long currEventTime = event.timestamp/1000000;
//                long currEventTime = (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;
                // Accumulating data from the event at every 1 sec for a total of 120 events per session
                if((currEventTime-lastEventTime) >= ACC_EVENT_TIME_DIFF && eventCount < MAX_EVENT_COUNT){
                    accArrX[eventCount] = event.values[0];
                    accArrY[eventCount] = event.values[1];
                    accArrZ[eventCount] = event.values[2];
                    eventTime[eventCount] = currEventTime;
                    lastEventTime = currEventTime;
                    eventCount++;
                }
                // Calculate activity from the data gathered and reset flags
                else if(eventCount == MAX_EVENT_COUNT){
                    calculateActivity();
                    eventCount = 0;
                    lastEventTime = 0;
                }

                break;

            case Sensor.TYPE_GYROSCOPE:

                break;
        }
    }

    public String getTime(long time){
        try {
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(time);
            String date = DateFormat.format("HH:mm", cal).toString();
            SimpleDateFormat Hour24SDF = new SimpleDateFormat("HH:mm");
            SimpleDateFormat Hour12SDF = new SimpleDateFormat("hh:mm a");
            Date _24HourDt = Hour24SDF.parse(date);
            return Hour12SDF.format(_24HourDt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}