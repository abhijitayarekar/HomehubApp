package com.homehub;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

public class HomehubService extends Service {
    private final IBinder mBinder = new HomehubBinder();
    private final HubHandler mHubHandler = new HubHandler();
    private boolean mKeepRunning = true;
    private HomehubCallback mCallback;

    @Override
    public void onDestroy() {
        super.onDestroy();
        mKeepRunning = false;
        try {
            mHubHandler.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mKeepRunning = true;
        mHubHandler.start();
    }

    private class HubHandler extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (mCallback != null)
                mCallback.onHomehubConnected();

            while (mKeepRunning) {
                try {
                    Thread.yield();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class HomehubBinder extends Binder {
        public HomehubService getService() {
            return HomehubService.this;
        }
    }

    public interface HomehubCallback {
        void onHomehubConnected();
        void onHomehubEvent();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setCallback(HomehubCallback callBack) {
        mCallback = callBack;
    }
}
