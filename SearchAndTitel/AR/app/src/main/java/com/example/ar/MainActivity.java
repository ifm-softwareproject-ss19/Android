package com.example.ar;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView mTextMessage;
    Intent connect = new Intent(this, ConnectionActivity.class);

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        mTextMessage = findViewById(R.id.message);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
    // all button functions of the main menu
    public void button_connect(View v){
        setContentView(R.layout.activity_connect);
        startActivity(connect);
    }

    public void button_credits(View v){
        setContentView(R.layout.activity_credits);

    }

    public void button_car(View v){
        setContentView(R.layout.activity_car);

    }

    public void button_drone(View v){
        setContentView(R.layout.activity_drone);

    }

    public void button_ar(View v){
        setContentView(R.layout.activity_ar);

    }

    public void button_mainmenu(View v){
        setContentView(R.layout.activity_main);
    }


}
