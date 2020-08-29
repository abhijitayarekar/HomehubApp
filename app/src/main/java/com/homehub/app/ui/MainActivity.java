package com.homehub.app.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.homehub.app.service.HomehubService;

public class MainActivity extends AppCompatActivity implements HomehubService.HomehubCallback {
    public static final String EXTRA_MESSAGE = "homehub.message";

    private static final String S_HOMEHUB_SERVICE_STATE = "homehub.service.state";

    HomehubService mHomehubService;
    int mBindState = HomehubService.BindState_UnBinded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null)
            mBindState = savedInstanceState.getInt(S_HOMEHUB_SERVICE_STATE);

        if (mBindState == HomehubService.BindState_UnBinded) {
            System.out.println("Binding homehub service.");
            Intent intent = new Intent(this, HomehubService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
            mBindState = HomehubService.BindState_Binding;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(S_HOMEHUB_SERVICE_STATE, mBindState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBindState > HomehubService.BindState_UnBinded)
            unbindService(connection);

        mBindState = HomehubService.BindState_UnBinded;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HomehubService.HomehubBinder binder = (HomehubService.HomehubBinder) service;
            mHomehubService = binder.getService();
            mHomehubService.setCallback(MainActivity.this);
            mBindState = HomehubService.BindState_Binded;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mHomehubService.setCallback(null);
            mBindState = HomehubService.BindState_UnBinded;
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