package com.dji.DrohneAndDrive;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;


public class AutoSteuerungActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    BluetoothServices mService;
    boolean mBound = false;
    final String gps ="GPSdata";
    String latDrohne=""; String longiDrohne="";

    private static final String TAG = "AutosteuerungActivity";

    BluetoothAdapter mBluetoothAdapter;
    Button btnEnableDisable_Discoverable;

    BluetoothConnectionService mBluetoothConnection;

    Button btnONOFF;
    Button btnStartConnection;
    Button btnStartManualDrive;
    Button btnStartAutomaticDrive;
    TextView textViewInfo;

    private String carStateDriveModus = Constants.carstateNone;
    private String carStateArrival = "";
    private String carStateConnection = Constants.carIsNotConnected;
    private String bluetoothState = "OFF";


    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        //unregisterReceiver(mBroadcastReceiver1);
        // unregisterReceiver(mBroadcastReceiver2);
        // unregisterReceiver(mBroadcastReceiver3);
        // unregisterReceiver(mBroadcastReceiver4);
        //mBluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_steuerung);
        btnONOFF = (Button) findViewById(R.id.btnONOFF);
        mBTDevices = new ArrayList<>();

        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
        btnStartManualDrive = (Button) findViewById(R.id.btnStartManualDrive);
        btnStartAutomaticDrive = (Button) findViewById(R.id.btnStartAutomaticDrive);
        textViewInfo = (TextView) findViewById(R.id.textViewInfo);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("data",MODE_PRIVATE);
        bluetoothState = prefs.getString("bluetooth", "unknown");
        carStateConnection= prefs.getString("conn", "unknown");
        carStateDriveModus= prefs.getString("driveMode", "unknown");
        Log.d("prefs create",bluetoothState+"  "+carStateConnection+"  "+carStateDriveModus);
        setTextViewInfo();

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);
        //Broadcasts wenn die Textview aktualisiert werden muss
        IntentFilter filterTxtView = new IntentFilter();
        filterTxtView.addAction(Constants.txtInfoActionfilter);
        filterTxtView.addAction(gps);
        registerReceiver(BroadcastReceiverTextViewInfo, filterTxtView);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();




        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                enableDisableBT();
            }
        });

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothState.equals("ON") && carStateConnection.equals(Constants.carIsNotConnected)) {
                    startConnection();
                    setTextViewInfo();
                } else {
                    Log.d(TAG, "bluetooth state = " + bluetoothState + "  carStateConnection= " + carStateConnection);
                }
            }
        });

        btnStartManualDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!carStateDriveModus.equals(Constants.carstateManual)&&carStateConnection.equals(Constants.carIsConnected)){
                    mService.sendData(Constants.startManualDrive);
                    carStateDriveModus=Constants.carstateManual;
                    setTextViewInfo();
                }

            }
        });

        btnStartAutomaticDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!carStateDriveModus.equals(Constants.carstateAutomatic)&&carStateConnection.equals(Constants.carIsConnected)){
                    mService.sendData("toDo");
                    carStateDriveModus=Constants.carstateAutomatic;
                    setTextViewInfo();
                }

            }
        });

    }
    private void setTextViewInfo() {
        String value="";
        String n= "\n";
        String header="Status"+n+n;
        String bluetooth="Bluetooth: "+bluetoothState+n;
        String connection ="Connection: "+carStateConnection+n;
        String driveModus ="Drive-Modus:"+ carStateDriveModus+n;
        String drohne = "Drohne GPS: Long: " + longiDrohne+ "  Lati: "+latDrohne+n;
        value = header+bluetooth+connection+driveModus+drohne;
        textViewInfo.setText(value);
        SharedPreferences prefs = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("bluetooth", bluetoothState);
        editor.putString("conn", carStateConnection);
        editor.putString("driveMode", carStateDriveModus);
        editor.commit();
    }

    //create method for starting connection
//***remember the conncction will fail and app will crash if you haven't paired first
    public void startConnection() {
        //startBTConnection(mBTDevice,MY_UUID_INSECURE);
        Intent intent = new Intent(this, BluetoothServices.class);
        intent.putExtra("bluetooth_device", Constants.mac_Car); // Mac einfuegen
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        startService(intent);
        carStateConnection = Constants.carIsConnected;
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BluetoothServices.LocalBinder binder = (BluetoothServices.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onResume(){
        super.onResume();
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("data",MODE_PRIVATE);
        bluetoothState = prefs.getString("bluetooth", "unknown");
        carStateConnection= prefs.getString("conn", "unknown");
        carStateDriveModus= prefs.getString("driveMode", "unknown");
        Log.d("prefs resume",bluetoothState+"  "+carStateConnection+"  "+carStateDriveModus);

    }

    @Override
    protected void onPause(){
        super.onPause();
        SharedPreferences prefs = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("bluetooth", bluetoothState);
        editor.putString("conn", carStateConnection);
        editor.putString("driveMode", carStateDriveModus);
        editor.commit();
        Log.d("prefs pause",bluetoothState+"  "+carStateConnection+"  "+carStateDriveModus);

    }
    @Override
    protected void onStop() {

        try {
            super.onStop();
            unbindService(connection);
            mBound = false;
            SharedPreferences prefs = getSharedPreferences("data", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("bluetooth", bluetoothState);
            editor.putString("conn", carStateConnection);
            editor.putString("driveMode", carStateDriveModus);
            editor.commit();
            Log.d("prefs stop",bluetoothState+"  "+carStateConnection+"  "+carStateDriveModus);
        } catch (IllegalArgumentException e) {

        }

    }


    /**
     * starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        mBluetoothConnection.startClient(device, uuid);
    }


    public void enableDisableBT() {
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }

    }


    public void btnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, intentFilter);

    }

    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if (!mBluetoothAdapter.isDiscovering()) {

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     * <p>
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();

            mBTDevice = mBTDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(AutoSteuerungActivity.this);
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver BroadcastReceiverTextViewInfo = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(Constants.txtInfoActionfilter)){
                setTextViewInfo();

            }else if(action.equals(gps)) {
                double lo =intent.getDoubleExtra("Longitude",6.6);
                double la = intent.getDoubleExtra("Latitude",0.0);
                longiDrohne = String.format("%.4f", lo);
                latDrohne  = String.format("%.4f", la);
                setTextViewInfo();
            }
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        bluetoothState = "OFF";
                        carStateConnection = Constants.carIsNotConnected;
                        carStateDriveModus = Constants.carstateNone;
                        setTextViewInfo();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        bluetoothState = "ON";
                        setTextViewInfo();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };
    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };
    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };
    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

}