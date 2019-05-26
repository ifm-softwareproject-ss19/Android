package com.dji.DrohneAndDrive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private final String TITLE = "Drohne And Drive";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle(TITLE);
    }

    public void buttonPressed_Verbindungen(View view) {
        Intent intent = new Intent(this, VerbindungenActivity.class);
        startActivity(intent);
    }
    public void buttonPressed_Drohne(View view) {
        Intent intent = new Intent(this, DrohnenActivity.class);
        startActivity(intent);
    }
    public void buttonPressed_Auto(View view) {
        Intent intent = new Intent(this, AutoSteuerungActivity.class);
        startActivity(intent);
    }
    public void buttonPressed_AR(View view) {
        Intent intent = new Intent(this, ARealityActivity.class);
        startActivity(intent);
    }
    public void buttonPressed_Credits(View view) {
        Intent intent = new Intent(this, CreditsActivity.class);
        startActivity(intent);
    }


}