package com.dji.DrohneAndDrive;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DrohnenActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MApplication map =(MApplication)getApplicationContext();

        if(!map.isRegisterAp()){
        Intent intent = new Intent(this, RegisterAppActivity.class);
        startService(intent);
        }
        setContentView(R.layout.activity_drohnen);
    }
}
