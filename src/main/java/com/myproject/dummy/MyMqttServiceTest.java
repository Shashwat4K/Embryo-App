package com.myproject.dummy;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.myproject.dummy.MyMqttService.MyBinder;



public class MyMqttServiceTest extends AppCompatActivity {
    private TextView msg;
    MyMqttService mServer ;
    boolean mBounded;
    Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testing_page);
       // mServer.initMqttClient();
        msg = findViewById(R.id.rec_msg);
        button = findViewById(R.id.publish);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // msg.setText();

                mServer.publish(getString(R.string.mqtt_topic),"OFF");
            }
        });
        /*   msg = findViewById(R.id.rec_msg);
        msg.setText(services.getMessage());
        */
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent mIntent = new Intent(MyMqttServiceTest.this, MyMqttService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
           Toast.makeText(MyMqttServiceTest.this, "Service is disconnected", Toast.LENGTH_SHORT).show();
            mBounded = false;
            mServer = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
           Toast.makeText(MyMqttServiceTest.this, "Service is connected",Toast.LENGTH_SHORT).show();
            mBounded = true;
            MyBinder mLocalBinder = (MyBinder)service;
            mServer = mLocalBinder.getService();
        }
    };
    @Override
    protected void onStop() {
        super.onStop();
        if(mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    };
}