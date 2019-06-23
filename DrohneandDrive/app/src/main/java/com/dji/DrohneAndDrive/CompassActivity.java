package com.dji.DrohneAndDrive;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import static android.content.Context.SENSOR_SERVICE;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    Float compassRotation;

    public class CustomCompassView extends View {
        Paint compassPaint = new Paint();

        public CustomCompassView(Context context) {
            super(context);
            compassPaint.setColor(0xff00ff00);
            compassPaint.setStyle(Paint.Style.STROKE);
            //compassPaint.setWitdh(2);
            compassPaint.setAntiAlias(true);
        }


        protected void onDraw(Canvas canvas) {
            int width = getWidth();
            int height = getHeight();
            int centerWidth = width / 2;
            int centerHeight = height / 2;
            canvas.drawLine(centerWidth, 0, centerHeight, height, compassPaint);
            canvas.drawLine(0, centerWidth, width, centerHeight, compassPaint);

            if(compassRotation != null){
                canvas.rotate(-compassRotation*360/(2*(float)Math.PI),centerWidth,centerHeight);
                compassPaint.setColor(0xff0000ff);
                canvas.drawLine(centerWidth, -1000, centerWidth,+1000, compassPaint);
                canvas.drawLine(-1000, centerHeight,1000,centerHeight ,compassPaint);
                canvas.drawText("Car",centerWidth+5, centerHeight-10, compassPaint);
                compassPaint.setColor(0xff00ff00);
            }
        }
    }

    CustomCompassView compassView;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    protected void onCreate(Bundle savedInstanceState) {
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
                compassRotation = orientation[0]; // orientation contains: azimut, pitch and roll
            }
        }
    }
}
