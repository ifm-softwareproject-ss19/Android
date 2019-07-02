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

    final String requestGpsCar = "requestGPSCar";
    final String gpsCar= "GPSCar";

    Location phoneLoc = new Location("service Provider");
    Location carLoc = new Location("service Provider");

    double phoneLongitude;
    double phoneLatitude;
    double updateLati = 0;
    double updateLongi = 0;

    double newLati = 92; // impossible carGPS to check if Data was send
    double newLongi = 182;
    float heading = 0;
    float bearing = 0;
    long timer;     //

    CustomCompassView compassView;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    protected BroadcastReceiver compassReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("action", action);

            if (action.equals(gpsCar)) {
                newLati = intent.getDoubleExtra(Constants.carLatitude,91);
                System.out.println("latitude: " + newLati);

                newLongi = intent.getDoubleExtra(Constants.carLongitude,181);
                System.out.println("longitude " + newLongi);
            }

        }
    };

    private void registerCompassReceiver() {
        //register the Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(gpsCar);
        registerReceiver(compassReceiver, filter);
    }



    final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //is called when the Phone Location is changed
            phoneLongitude = location.getLongitude();
            phoneLatitude = location.getLatitude();
            phoneLoc.setLongitude(phoneLongitude);
            phoneLoc.setLatitude(phoneLatitude);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            // not used
        }

        @Override
        public void onProviderEnabled(String s) {
            //not used
        }

        @Override
        public void onProviderDisabled(String s) {
            //not used
        }
    };

    public void setLocs(double lati, double longi) {
        //setting location data and determining bearing
        carLoc.setLongitude(longi);
        carLoc.setLatitude(lati);

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
            Log.d("Compass",newLati+"   "+newLongi+" phone:  "+phoneLatitude+"    "+phoneLatitude);

            timer = SystemClock.elapsedRealtime();

            if (timer + 0.5 < SystemClock.elapsedRealtime()|| firstRun) {
                Intent compassGps = new Intent();
                compassGps.setAction(requestGpsCar);
                sendBroadcast(compassGps); // requesting GpsData

                if(newLati < 92|| newLongi < 182){ //only updating Data when new Location Data was send

                    //updating last Car Location Data
                    updateLongi = newLongi;
                    updateLati = newLati;
                    signalError = false; // after a getting a signal remove "no Signal"

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


            //correting Bearing Heading and CompassRoatation Degrees
            if (bearing < 0) {
                bearing = bearing + 360;
            }

            compassRotation = normalizeDegree(heading);
            if (compassRotation != null) {
                if (compassRotation < 0) {
                    compassRotation = compassRotation + 360;
                }




            }



            if(!signalError){

                float rotation = compassRotation - (float)compassOrientation; // the final Rotation of the Drawing
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
        registerCompassReceiver(); // registering Receiver for Bluetooth CarGpsData

        timer = SystemClock.elapsedRealtime();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // asking for necessary Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 125);
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);




        compassView = new CustomCompassView(this);
        setContentView(compassView);    // Register the sensor listeners
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE); // sensors to determine North
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    protected void onResume() {
        super.onResume();       //is called when Activity is Resumed
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        super.onPause();        //is called when Activity is Paused
        mSensorManager.unregisterListener(this); // unregister sensors when they're not used
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not used
    }

    float[] mGravity;
    float[] mGeomagnetic;

    public void onSensorChanged(SensorEvent event) {
        // is called when the sensor get new signals
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
                compassOrientation = Math.toDegrees(orientation[0]); //CompassOrientation is the Rotation of the phone
            }
        }

        
            GeomagneticField geoField = new GeomagneticField(Double.valueOf(phoneLoc.getLatitude()).floatValue(), Double
                    .valueOf(phoneLoc.getLongitude()).floatValue(),
                    Double.valueOf(phoneLoc.getAltitude()).floatValue(),
                    System.currentTimeMillis());
            heading = geoField.getDeclination(); // getting the heading

            compassView.invalidate(); // calls onDraw of compassView
        }


    public void onDestroy() {
        mSensorManager.unregisterListener(this);
        super.onDestroy();

    }
}
