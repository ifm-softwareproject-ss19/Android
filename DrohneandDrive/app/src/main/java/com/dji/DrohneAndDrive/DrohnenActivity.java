package com.dji.DrohneAndDrive;


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

    private final float verticalJoyControlMaxSpeed = 0.2f; //max 0.2 meter die sekunde
    private final float yawJoyControlMaxSpeed = 15; // Geschwindigkeit in Grad
    private final float pitchJoyControlMaxSpeed = 0.2f;
    private final float rollJoyControlMaxSpeed = 0.2f;

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
       // IntentFilter filter = new IntentFilter();
       // filter.addAction(DJIDemoApplication.FLAG_CONNECTION_CHANGE);
        //registerReceiver(mReceiver, filter);
        setContentView(R.layout.activity_drohnen);
        initUI();
    }


    class SendVirtualStickDataTask extends TimerTask {
        @Override
        public void run() {

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


    private void initFlightController() {

        Aircraft aircraft = DrohnenActivity.getAircraftInstance();
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
            /*
            mFlightController.getSimulator().setStateCallback(new SimulatorState.Callback() {
                @Override
                public void onUpdate(final SimulatorState stateData) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            String yaw = String.format("%.2f", stateData.getYaw());
                            String pitch = String.format("%.2f", stateData.getPitch());
                            String roll = String.format("%.2f", stateData.getRoll());
                            String positionX = String.format("%.2f", stateData.getPositionX());
                            String positionY = String.format("%.2f", stateData.getPositionY());
                            String positionZ = String.format("%.2f", stateData.getPositionZ());

                            mTextView.setText("Yaw : " + yaw + ", Pitch : " + pitch + ", Roll : " + roll + "\n" + ", PosX : " + positionX +
                                    ", PosY : " + positionY +
                                    ", PosZ : " + positionZ);
                        }
                    });

                }
            }); */
        }
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
        stickJoystickToDrone();
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
