package com.example.myfirstapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.homehub.HomehubService;

public class MainActivity extends AppCompatActivity implements HomehubService.HomehubCallback {
    public static final String EXTRA_MESSAGE = "homehub.message";

    private static final String HOMEHUB_SERVICE_STATE = "homehub.service.state";

    HomehubService mHomehubService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            if (!savedInstanceState.getBoolean(HOMEHUB_SERVICE_STATE)) {
                Intent intent = new Intent(this, HomehubService.class);
                bindService(intent, connection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(HOMEHUB_SERVICE_STATE, mBound);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound)
            unbindService(connection);
        mBound = false;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HomehubService.HomehubBinder binder = (HomehubService.HomehubBinder) service;
            mHomehubService = binder.getService();
            mHomehubService.setCallback(MainActivity.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mHomehubService.setCallback(null);
            mBound = false;
        }
    };

    @Override
    public void onHomehubConnected() {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "Hub Connected");
        startActivity(intent);
    }

    @Override
    public void onHomehubEvent() {
    }
}