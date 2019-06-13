package com.dji.DrohneAndDrive;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.simulator.InitializationData;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class DrohnenActivity extends AppCompatActivity {

    private final float verticalJoyControlMaxSpeed = 0.4f; //max 0.2 meter die sekunde
    private final float yawJoyControlMaxSpeed = 45; // Geschwindigkeit in Grad
    private final float pitchJoyControlMaxSpeed = 0.4f;
    private final float rollJoyControlMaxSpeed = 0.4f;

    private float mPitch;
    private float mRoll;
    private float mYaw;
    private float mThrottle;

    private static BaseProduct mProduct;

    private OnScreenJoystick mScreenJoystickRight;
    private OnScreenJoystick mScreenJoystickLeft;
    private SwitchCompat switchJoystick;

    private Timer mSendVirtualStickDataTimer;//Drohne
    private Timer mSendCarDataTimer; //Car
    private SendVirtualStickDataTask mSendVirtualStickDataTask;
    private SendCarDataTask mSendCarData;
    private TextView gpsTextView;

    private double longitude,latitude,atitude;
    private FlightController mFlightController;

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setAtitude(double atitude) {
        this.atitude = atitude;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MApplication map =(MApplication)getApplicationContext();

        if(!map.isRegisterAp()){
        Intent intent = new Intent(this, RegisterAppActivity.class);
        startService(intent);
        }
        //Register BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(RegisterAppActivity.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
        setContentView(R.layout.activity_drohnen);
        initUI();
    }
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }


    class SendVirtualStickDataTask extends TimerTask {
        @Override
        public void run() {
            Log.d("Datatask","controllerstatuss="+ mFlightController);
            if (mFlightController != null) {
                mFlightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                mPitch, mRoll, mYaw, mThrottle
                        ), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {

                            }
                        }
                );
                //uptadeKoordinates();
            }
        }
    }
    class SendCarDataTask extends TimerTask {
        @Override
        public void run() {

            //Sende Daten ans Auto
        }
    }
    private void onProductConnectionChange()
    {
        initFlightController();
    }


    private void initFlightController() {

        Aircraft aircraft = getAircraftInstance();
        if (aircraft == null || !aircraft.isConnected()) {
           // showToast("Disconnected");
            mFlightController = null;
            return;
        } else {
            mFlightController = aircraft.getFlightController();
            //Pitch = vorne und hinten , roll links rechts, throttle hoch runter, yaw drehen
            mFlightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);// in meter pro secunde
            mFlightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
            mFlightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
            mFlightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
            mFlightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null){

                    }else
                    {

                    }
                }
            });

        }
        if (mFlightController != null) {
            mFlightController.setStateCallback(new FlightControllerState.Callback() {

                @Override
                public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
                    latitude = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                    longitude = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                    atitude = djiFlightControllerCurrentState.getAircraftLocation().getAltitude();
                    updateDroneLocation();
                }
            });
        }
    }

    private void updateDroneLocation() {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String lon = String.format("%.2f", latitude);
                String lat = String.format("%.2f", longitude);
                String att = String.format("%.2f", atitude);
                gpsTextView.setText("GPS(Drohne): Lat: "+ lat + " Long: " + lon + " Att: "+ att);
            }
        });



    }

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }
    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) return null;
        return (Aircraft) getProductInstance();
    }
    public static synchronized BaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getProduct();
        }
        return mProduct;
    }

    private void initUI() {
        mScreenJoystickRight = (OnScreenJoystick)findViewById(R.id.directionJoystickRight);
        mScreenJoystickLeft = (OnScreenJoystick)findViewById(R.id.directionJoystickLeft);
        switchJoystick =(SwitchCompat)findViewById(R.id.DrohneOrCarJoystickSwitch);
        gpsTextView = (TextView)findViewById(R.id.gps_text_view);
        stickJoystickToDrone();

        switchJoystick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    switchJoystick.setText("Car");
                    stickJoystickToCar();
                }else{
                    switchJoystick.setText("Drohne");
                    stickJoystickToDrone();
                }
            }
        });
    }

    private void stickJoystickToDrone(){
        mScreenJoystickRight.setJoystickListener(new OnScreenJoystickListener(){
            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if(Math.abs(pX) < 0.2 ){
                    pX = 0;
                }

                if(Math.abs(pY) < 0.2 ){
                    pY = 0;
                }

                Log.d("Right","Drohne px: "+ pX + "  py: " +pY);
                mPitch = (float)(pitchJoyControlMaxSpeed * pX);

                mRoll = (float)(rollJoyControlMaxSpeed * pY);

                if (null == mSendVirtualStickDataTimer) {
                    mSendVirtualStickDataTask = new SendVirtualStickDataTask();
                    mSendVirtualStickDataTimer = new Timer();
                    mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 100, 200);
                }
            }
        });

        mScreenJoystickLeft.setJoystickListener(new OnScreenJoystickListener() {
            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if(Math.abs(pX) < 0.2 ){
                    pX = 0;
                }

                if(Math.abs(pY) < 0.2 ){
                    pY = 0;
                }

                Log.d("Left","Drohne   px: "+ pX + "  py: " +pY);
                mYaw = (float)(yawJoyControlMaxSpeed * pX);
                mThrottle = (float)(verticalJoyControlMaxSpeed * pY);

                if (null == mSendVirtualStickDataTimer) {
                    mSendVirtualStickDataTask = new SendVirtualStickDataTask();
                    mSendVirtualStickDataTimer = new Timer();
                    mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 0, 200);
                }

            }
        });
    }
    private void stickJoystickToCar(){
        mScreenJoystickRight.setJoystickListener(new OnScreenJoystickListener(){
            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if(Math.abs(pX) < 0.5 ){
                    pX = 0;
                }

                if(Math.abs(pY) < 1.1 ){
                    pY = 0;
                }

                Log.d("Right","Car   px: "+ pX + "  py: " +pY);

                if (null == mSendCarDataTimer) {
                    mSendCarData = new SendCarDataTask();
                    mSendCarDataTimer = new Timer();
                    mSendCarDataTimer.schedule(mSendCarData, 0, 200);
                }
            }
        });

        mScreenJoystickLeft.setJoystickListener(new OnScreenJoystickListener() {
            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if(Math.abs(pX) < 1.1 ){//Deaktivire links rechts
                    pX = 0;
                }

                if(Math.abs(pY) < 0.5 ){
                    pY = 0;
                }

                Log.d("Left","Car   px: "+ pX + "  py: " +pY);

                if (null == mSendCarDataTimer) {
                    mSendCarData = new SendCarDataTask();
                    mSendCarDataTimer = new Timer();
                    mSendCarDataTimer.schedule(mSendCarData, 0, 200);
                }

            }
        });
    }
}
