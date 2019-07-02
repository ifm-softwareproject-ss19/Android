package com.dji.DrohneAndDrive;

public class Constants {
    //
    public static final String getGpsData="getGpsData()";
    public static final String automaticDrive=" automaticDrive(Gps gpsData)";
    public static final String startGpsData = "startGpsData(int interval);";
    public static final String stopGpsData = "stopGpsData()";
    public static final String  startManualDrive = "startManualDrive()";
    public static final String stopManualDrive  = "stopManualDrive()";
    public static final String emergencyStop = "emergencyStop()";
    public static final String continueDriving = "continueDriving();";
    public static final String  manualDirection =" manualDirection(Drive direction1, Steering direction2);";
    public static final String mac_Car ="B8:27:EB:63:BD:8F";
    public static final String DriveFRONT ="Front";
    public static final String DriveBACK ="Back";
    public static final String DriveSTOP ="Stop";
    public static final String SteerFORWARD ="Forward";
    public static final String SteerRight ="Right";
    public static final String SteerLeft ="Left";

    public static final float verticalJoyControlMaxSpeed = 0.4f; //max 0.2 meter die sekunde
    public static final float yawJoyControlMaxSpeed = 45; // Geschwindigkeit in Grad
    public static final float pitchJoyControlMaxSpeed = 0.4f;
    public static final float rollJoyControlMaxSpeed = 0.4f;

    public static final String carstateAutomatic ="Automatic Drive";
    public static final String carstateManual ="Manual Drive";
    public static final String carstateNone ="None";
    public static final String carIsConnected ="Car is connected";
    public static final String carIsNotConnected ="Car is not connected ";
    public static final String carstateDriving ="Car is driving";
    public static final String carstateArrived ="arrived at destination";

    public static final String txtInfoActionfilter ="refreshInfo";

    public static final String carLatitude ="carLatitude";
    public static final String carLongitude ="carLongigtude";

}

