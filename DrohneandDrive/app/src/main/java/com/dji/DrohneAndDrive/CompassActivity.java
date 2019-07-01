package com.dji.DrohneAndDrive;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
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
import android.widget.Button;

import static android.content.Context.SENSOR_SERVICE;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    SystemClock clock;
    Float compassRotation;
    double compassOrientation;
    LocationManager lm;

    boolean firstrun = true;
    boolean signalError = false;

    Location userLoc = new Location("service Provider");;

    Location carLoc = new Location("service Provider");
    double phoneLongitude;
    double phoneLatitude;

    float updateLati = 0;
    float updateLongi = 0;

    float newLati = 92; // impossible carGPS to check data
    float newLongi = 182;
    float heading = 0;
    float bearing = 0;
    long timer;

    CustomCompassView compassView;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    public void testgpsData(){
        Intent test = new Intent();
        test.setAction("compassGpsData");
        test.putExtra("compassLatitude",11.f);
        test.putExtra("compassLongitude",11.f);
        sendBroadcast(test);
    }

    protected BroadcastReceiver compassReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String msg = "";
            Log.d("action", action);

            if (action.equals("compassGpsData")) {


                newLati = intent.getFloatExtra("compassLatitude",92);
                System.out.println("latitude: " + newLati);

                newLongi = intent.getFloatExtra("compassLongitude",182);
                System.out.println("latitude: " + newLongi);
            }

        }
    };

    private void registerCompassReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("compassGpsData");
        registerReceiver(compassReceiver, filter);
    }



    final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if(location != null) {

                phoneLongitude = location.getLongitude();
                phoneLatitude = location.getLatitude();
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

    public void setLocs(float lati, float longi) {

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

            timer = clock.elapsedRealtime();

        //GPSTEST    System.out.println("Latitude"+newLati);

            if (timer + 0.5 < clock.elapsedRealtime()|| firstrun) {
                Intent compassGps = new Intent();
                compassGps.setAction("compassGps");
                sendBroadcast(compassGps);

                if(newLati < 92|| newLongi < 182){
                    updateLati = newLati;
                    updateLongi = newLongi;



                } else if(firstrun){
                    signalError = true;
                }
                firstrun = false;
                setLocs(updateLati, updateLongi);
                System.out.println(signalError);
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
            System.out.println("Rotation:"+compassRotation);
            System.out.println("Orientation:"+compassOrientation);
            if (compassRotation != null) {
                if (compassRotation < 0) {
                    compassRotation = compassRotation + 360;
                }




            }



            if(!signalError){

                float rotation = compassRotation - (float)compassOrientation;
                System.out.println("True Rotation:" + rotation);
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
                compassPaint.setTextSize(30);
                canvas.drawText("No Signal", centerWidth, centerHeight ,compassPaint);
            }

        }


    }



    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        registerCompassReceiver();
        testgpsData(); // GPSTEST
        timer = clock.elapsedRealtime(); // maybe better solution?
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 125);
            return;
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 125);
            return;
        }

        if(userLoc != null){
            phoneLongitude = userLoc.getLongitude();
            phoneLatitude = userLoc.getLatitude();
        }



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
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
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
