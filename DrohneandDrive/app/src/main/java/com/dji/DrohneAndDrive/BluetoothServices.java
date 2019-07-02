package com.dji.DrohneAndDrive;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
Ein Service ,welcher im Hontergrund die Bluetooth Verbindung aufrechterhält
und das Senden und Empfangen von Daten, realisiert.
 */
public class BluetoothServices extends Service {

    private Intent eventIntent;

    //Event Name für den BroadVCst Reciever
    final String gpsDrone ="GPSdata";
    final String gpsCar ="GPSdataCar";

    private BluetoothAdapter mBluetoothAdapter;
    public static final String B_DEVICE = "MY DEVICE";
    public static final String B_UUID = "00001101-0000-1000-8000-00805f9b34fb";
// 00000000-0000-1000-8000-00805f9b34fb

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private ConnectBtThread mConnectThread;
    private static ConnectedBtThread mConnectedThread;

    private static Handler mHandler = null;
    public static int mState = STATE_NONE;
    public static String deviceName;
    public static BluetoothDevice sDevice = null;
    public Vector<Byte> packData = new Vector<>(2048);

//IBinder mIBinder = new LocalBinder();

    protected BroadcastReceiver steuerungReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            String msg="";
            Log.d("action",action);

            if (action.equals("car")){
                String s ="manualDirection("+intent.getStringExtra("Steering")+","+intent.getStringExtra("Direction")+")";
                sendData(s);
                Log.d("sendData",s);


            } else if(action.equals(gpsDrone)) {
                sendData(Constants.getGpsData);
            }else if(action.equals("enable")){
                sendData(Constants.startManualDrive);

            }else if(action.equals("disable")){
                sendData(Constants.stopManualDrive);
            }
        }
    };
    private void registerSteuerungReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(gpsDrone);
        filter.addAction("car");
        filter.addAction("enable");
        filter.addAction("disable");
        registerReceiver(steuerungReceiver, filter);
    }
    //Wird nach dem einlesen des Datenstroms ausgeführt vom auto zu msmartphone
    private void handleInputString(String input){
        Intent intent = new Intent(gpsCar);
        Intent statusIntent = new Intent("Status");


        boolean gps = Pattern.matches("sendLoca.*",input);
        boolean status = Pattern.matches("Status.*",input);
        if(gps){
            String pattern = "[0-9]+\\.[0-9]+";

            List<MatchResult> r = findMatches( pattern, input );
            double lat = Double.parseDouble(r.get(0).group());
            double longi =Double.parseDouble(r.get(1).group());
            intent.putExtra("Latitude",lat);
            intent.putExtra("Longitude",longi);
            Log.d("pattern",lat+"    "+longi);

            sendBroadcast(intent);

        }
        else if(status){
            statusIntent.putExtra("Status",input);
            sendBroadcast(statusIntent);
        }
        switch (input){
            case "A":
                break;
            case "B":break;

            default:break;
        }
    }

    static List<MatchResult> findMatches( String pattern, CharSequence s )
    {
        List<MatchResult> results = new ArrayList<MatchResult>();

        for ( Matcher m = Pattern.compile(pattern).matcher(s); m.find(); )

            results.add( m.toMatchResult() );

        return results;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerSteuerungReceiver();
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //mHandler = getApplication().getHandler();
        return mBinder;
    }

    public void toast(String mess) {
        Toast.makeText(this, mess, Toast.LENGTH_SHORT).show();
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        BluetoothServices getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothServices.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String deviceg = intent.getStringExtra("bluetooth_device");


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        connectToDevice(deviceg);

        return START_STICKY;
    }

    private synchronized void connectToDevice(String macAddress) {
        Log.d("conntest",macAddress);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
        Log.d("conntest",device.getName());
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectBtThread(device);
        toast("connecting");
        mConnectThread.start();
        setState(STATE_CONNECTING);

    }

    private void setState(int state) {
        mState = state;
        if (mHandler != null) {
            // mHandler.obtainMessage();
        }
    }

    public synchronized void stop() {
        setState(STATE_NONE);
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        stopSelf();
    }

    public void sendData(String message) {
        if (mConnectedThread != null) {
            mConnectedThread.write(message.getBytes());
           // toast("sent data");
        } else {
            Toast.makeText(BluetoothServices.this, "Failed to send data", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean stopService(Intent name) {
        setState(STATE_NONE);

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mBluetoothAdapter.cancelDiscovery();
        return super.stopService(name);
    }

    private class ConnectBtThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        public ConnectBtThread(BluetoothDevice device) {
            mDevice = device;
            BluetoothSocket socket = null;
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(B_UUID));
                sendBroadcast(new Intent(Constants.carIsConnected));
            } catch (IOException e) {
                toast("Connection failed");
                sendBroadcast(new Intent(Constants.carIsNotConnected));
            }
            mSocket = socket;

        }

        @Override
        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mSocket.connect();
                Log.d("service", "connect thread run method (connected)");
                SharedPreferences pre = getSharedPreferences("BT_NAME", 0);
                pre.edit().putString("bluetooth_connected", mDevice.getName()).apply();
                Log.d("service", "connected");

            } catch (IOException e) {

                try {
                    mSocket.close();
                    Log.d("service", "connect thread run method ( close function)");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
            //connected(mSocket);
            mConnectedThread = new ConnectedBtThread(mSocket);
            mConnectedThread.start();
        }

        public void cancel() {

            try {
                mSocket.close();
                Log.d("service", "connect thread cancel method");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedBtThread extends Thread {
        private final BluetoothSocket cSocket;
        private final InputStream inS;
        private final OutputStream outS;

        private byte[] buffer;

        public ConnectedBtThread(BluetoothSocket socket) {
            cSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inS = tmpIn;
            outS = tmpOut;
        }

        @Override
        public void run() {
            buffer = new byte[1024];
            int mByte;
            boolean infinity = true;
            while(infinity){
                try {
                    mByte = inS.read(buffer);
                    String incomingMessage = new String(buffer, 0, mByte);
                    Log.d("input", "InputStream: " + incomingMessage);
                    handleInputString(incomingMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d("service", "connected thread run method");

        }
        /*Je nach input sende eine Intent an die Brcastreciver
         */



        public void write(byte[] buff) {
            try {
                outS.write(buff);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void cancel() {
            try {
                cSocket.close();
                Log.d("service", "connected thread cancel method");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        this.stop();
        super.onDestroy();
        //unregisterReceiver(steuerungReceiver);
    }
}