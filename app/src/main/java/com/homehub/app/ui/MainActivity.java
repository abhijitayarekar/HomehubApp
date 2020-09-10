package com.homehub.app.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.homehub.app.service.HomehubService;

public class MainActivity extends AppCompatActivity implements HomehubService.HomehubCallback {
    HomehubService mHomehubService;
    TextView mConnectionStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mConnectionStatusView = findViewById(R.id.textViewConnection);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(connection);
        if (mHomehubService != null)
            mHomehubService.setCallback(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, HomehubService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        mConnectionStatusView.setText(R.string.looking_for_homehub);
        mConnectionStatusView.setBackgroundColor(0xFF6D00);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HomehubService.HomehubBinder binder = (HomehubService.HomehubBinder) service;
            mHomehubService = binder.getService();
            mHomehubService.setCallback(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mHomehubService.setCallback(null);
        }
    };

    @Override
    public void onHomehubEvent(int event) {
        if (event == HomehubService.Event_Connected) {
            mConnectionStatusView.setText(mHomehubService.getLocation());
            mConnectionStatusView.setBackgroundColor(0xFF64DD17);
        }
    }
}