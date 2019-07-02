package com.dji.DrohneAndDrive;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;


public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    Float compassRotation;
    double compassOrientation;
    LocationManager lm;

    boolean firstRun = true;
    boolean signalError = false;
    boolean tester = true;

    final String requestGpsCar = "requestGPSCar";
    final String gpsCar= "GPSCar";

    Location userLoc = new Location("service Provider");

    Location carLoc = new Location("service Provider");
    double phoneLongitude;
    double phoneLatitude;

    double updateLati = 0;
    double updateLongi = 0;

    double newLati = 92; // impossible carGPS to check data
    double newLongi = 182;
    float heading = 0;
    float bearing = 0;
    long timer;

    CustomCompassView compassView;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

  //   a method to test GPSDAta
    public void testgpsData(){
        Intent test = new Intent();
        test.setAction("GPSCar");
        test.putExtra("compassLatitude", 52.06526171883914);
        test.putExtra("compassLongitude",9.109505649501275);
        sendBroadcast(test);
    }

    protected BroadcastReceiver compassReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("action", action);

            if (action.equals(gpsCar)) {


                newLati = intent.getDoubleExtra("compassLatitude",92);
                System.out.println("latitude: " + newLati);

                newLongi = intent.getDoubleExtra("compassLongitude",182);
                System.out.println("longitude " + newLongi);
            }

        }
    };

    private void registerCompassReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(gpsCar);
        registerReceiver(compassReceiver, filter);
    }



    final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {


                phoneLongitude = location.getLongitude();
                phoneLatitude = location.getLatitude();
                if(tester){
                   testgpsData();
                    tester = false;
                }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    public void setLocs(double lati, double longi) {

        carLoc.setLongitude(longi);
        carLoc.setLatitude(lati);
        Location phoneLoc = new Location("service Provider");

        phoneLoc.setLongitude(phoneLongitude);
        phoneLoc.setLatitude(phoneLatitude);
        bearing = phoneLoc.bearingTo(carLoc);



    }




    private float normalizeDegree(float value){
        if(value >= 0.0f && value <= 180.0f){
            return value;
        }else{
            return 180 + (180 + value);
        }
    }

    public class CustomCompassView extends View {
        Paint compassPaint = new Paint();

        public CustomCompassView(Context context) {
            super(context);
            compassPaint.setColor(0xff00ff00);
            compassPaint.setStyle(Paint.Style.STROKE);
            compassPaint.setAntiAlias(true);
        }


        protected void onDraw(Canvas canvas) {

            timer = SystemClock.elapsedRealtime();

        //GPSTEST    System.out.println("Latitude"+newLati);

            if (timer + 0.5 < SystemClock.elapsedRealtime()|| firstRun) {
                Intent compassGps = new Intent();
                compassGps.setAction(requestGpsCar);
                sendBroadcast(compassGps);

                if(newLati < 92|| newLongi < 182){
                    updateLati = newLati;
                    updateLongi = newLongi;

                    signalError = false;

                } else if(firstRun){
                    signalError = true;
                }
                firstRun = false;
                setLocs(updateLati, updateLongi);

           }

            heading = (bearing - heading) * -1;




            int width = getWidth();
            int height = getHeight();
            int centerWidth = width / 2;
            int centerHeight = height / 2;



            if (bearing < 0) {
                bearing = bearing + 360;
            }


            compassRotation = normalizeDegree(heading);
            //System.out.println("Rotation:"+compassRotation);
            //System.out.println("Orientation:"+compassOrientation);
            if (compassRotation != null) {
                if (compassRotation < 0) {
                    compassRotation = compassRotation + 360;
                }




            }



            if(!signalError){

                float rotation = compassRotation - (float)compassOrientation;
              //  System.out.println("True Rotation:" + rotation);
                canvas.rotate(rotation, centerWidth, centerHeight);
                canvas.save();
                signalError = false;
                compassPaint.setColor(Color.RED);
                compassPaint.setStyle(Paint.Style.STROKE);
                compassPaint.setStrokeWidth(2);

                Path arrow = new Path();
                arrow.moveTo(centerWidth, centerHeight - 320);
                arrow.lineTo(centerWidth + 150, centerHeight);
                arrow.lineTo(centerWidth - 150, centerHeight);
                arrow.close();

                canvas.drawPath(arrow, compassPaint);
                compassPaint.setTextSize(110);
                canvas.drawText("Car", centerWidth - 75, centerHeight + 110, compassPaint);


                canvas.restore();
            }else{


                compassPaint.setColor(Color.RED);
                compassPaint.setStrokeWidth(2);
                compassPaint.setTextSize(110);
                canvas.drawText("No Signal", centerWidth-75, centerHeight ,compassPaint);
            }

        }


    }



    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        registerCompassReceiver();

        timer = SystemClock.elapsedRealtime();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 125);
            return;
        }
       // testgpsData(); // GPSTEST
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);




        compassView = new CustomCompassView(this);
        setContentView(compassView);    // Register the sensor listeners
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    float[] mGravity;
    float[] mGeomagnetic;

    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float[] R= new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
               // System.out.println("orientationSensor:"+orientation[0]);
                compassOrientation = Math.toDegrees(orientation[0]);
              //  System.out.println("OrientationSensor:"+compassOrientation);
            }
        }

        
            GeomagneticField geoField = new GeomagneticField(Double.valueOf(userLoc.getLatitude()).floatValue(), Double
                    .valueOf(userLoc.getLongitude()).floatValue(),
                    Double.valueOf(userLoc.getAltitude()).floatValue(),
                    System.currentTimeMillis());
            heading = geoField.getDeclination();





            compassView.invalidate();
        }


    public void onDestroy() {
        mSensorManager.unregisterListener(this);
        super.onDestroy();

    }
}
