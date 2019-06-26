package com.dji.DrohneAndDrive;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
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

import static android.content.Context.SENSOR_SERVICE;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    SystemClock clock;
    Float compassRotation;
    Float compassOrientation;
    LocationManager lm;

    Location userLoc = new Location("service Provider");;

    Location carLoc = new Location("service Provider");
    double phoneLongitude;
    double phoneLatitude;
    float heading = 0;
    float bearing = 0;
    long timer;


    public void setLocs(float lati, float longi) {

        /*
        final LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                phoneLongitude = location.getLongitude();
                phoneLatitude = location.getLatitude();
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


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 125);
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
*/

        carLoc.setLongitude(longi);
        carLoc.setLatitude(lati);
        Location phoneLoc = new Location("service Provider");

        phoneLoc.setLongitude(phoneLongitude);
        phoneLoc.setLatitude(phoneLatitude);
        bearing = phoneLoc.bearingTo(carLoc);



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

            if (timer + 1 < clock.elapsedRealtime()) {
                float newLati = 10; // here carGPS necessary
                float newLongi = 20;
                setLocs(newLati, newLongi);


                GeomagneticField geoField = new GeomagneticField(Double.valueOf(userLoc.getLatitude()).floatValue(), Double
                        .valueOf(userLoc.getLongitude()).floatValue(),
                        Double.valueOf(userLoc.getAltitude()).floatValue(),
                        System.currentTimeMillis());
                heading = geoField.getDeclination();
                System.out.println(heading);
            }
            timer = clock.elapsedRealtime();
            int width = getWidth();
            int height = getHeight();
            int centerWidth = width / 2;
            int centerHeight = height / 2;



            if (bearing < 0) {
                bearing = bearing + 360;
            }



            if (compassRotation != null) {
                if (compassRotation < 0) {
                    compassRotation = compassRotation + 360;
                }
                float rotation = compassRotation+compassOrientation;

                canvas.rotate(rotation, centerWidth, centerHeight);
                System.out.println(compassRotation);
            }
            compassPaint.setColor(Color.RED);
            compassPaint.setStyle(Paint.Style.STROKE);
            compassPaint.setStrokeWidth(2);
            Path arrow = new Path();
            arrow.moveTo(centerWidth, centerHeight + 10);
            arrow.lineTo(centerWidth + 30, centerHeight - 30);
            arrow.lineTo(centerWidth - 30, centerHeight - 30);
            arrow.close();
            canvas.drawPath(arrow, compassPaint);
            // canvas.drawLine(1000, centerHeight,1000, centerHeight ,compassPaint);
            canvas.drawText("Car", centerWidth, centerHeight - 40, compassPaint);
        }


    }

    CustomCompassView compassView;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    protected void onCreate(Bundle savedInstanceState) {

        final LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if(location != null) {

                    compassRotation = bearing - heading;
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
        //userLoc = lm.;
        if(userLoc != null){
            phoneLongitude = userLoc.getLongitude();
            phoneLatitude = userLoc.getLatitude();
        }


        super.onCreate(savedInstanceState);
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
                compassOrientation = orientation[0];

            }
            compassView.invalidate();
        }
    }

    public void onDestroy() {
        mSensorManager.unregisterListener(this);
        super.onDestroy();

    }
}
