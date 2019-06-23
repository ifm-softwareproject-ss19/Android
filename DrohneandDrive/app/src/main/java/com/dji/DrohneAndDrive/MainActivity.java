package com.dji.DrohneAndDrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private final String TITLE = "Drone And Drive";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle(TITLE);
    }
    public void buttonPressed_Drohne(View view) {
        Intent intent = new Intent(this, DrohnenActivity.class);
        startActivity(intent);
    }
    public void buttonPressed_Auto(View view) {
        Intent intent = new Intent(this, AutoSteuerungActivity.class);
        startActivity(intent);
    }
    public void buttonPressed_Compass(View view) {
        Intent intent = new Intent(this, CompassActivity.class);
        startActivity(intent);
    }


}