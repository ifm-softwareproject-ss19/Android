package com.dji.DrohneAndDrive;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class CarBackroundService extends Service {
    //Event Name für den BroadVCst Reciever
    final String eventUP="UP";
    final String eventDown="DOWN";
    final String eventRight= "RIGHT";
    final String eventLeft= "LEFT";
    final String eventNotihng= "NOTHING";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected BroadcastReceiver steuerungReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            String msg="";
            Log.d("SteuuerungRecuever",action+"  " + intent.getFloatExtra("Speed",0.0f));
            if (action.equals(eventUP)){

            } else if(action.equals(eventDown)){

            } else if(action.equals(eventLeft)){

            } else if(action.equals(eventRight)){

            } else{

            }
        }
    };
    private void registerSteuerungReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(eventDown);
        filter.addAction(eventUP);
        filter.addAction(eventLeft);
        filter.addAction(eventRight);
        filter.addAction(eventNotihng);
        registerReceiver(steuerungReceiver, filter);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerSteuerungReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(steuerungReceiver);

    }
}
