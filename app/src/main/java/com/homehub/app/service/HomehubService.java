package com.homehub.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;

public class HomehubService extends Service {
    public static final int Event_Connected = 1;

    private static final int SSDP_PORT = 1900;
    private static final String SSDP_ADDRESS = "239.255.255.250";

    private final IBinder mBinder = new HomehubBinder();
    private final HubHandler mHubHandler = new HubHandler();
    private boolean mKeepRunning = true;
    private HomehubCallback mCallback;
    private String mHomehubLocation;

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

            DatagramSocket ssdpSocket = null;
            try {
                ssdpSocket = new DatagramSocket();
                ssdpSocket.setSoTimeout(5000);
                byte[] mSearchBytes = String.format("M-SEARCH * HTTP/1.1\r\n" +
                        "HOST: %s:%d\r\n" +
                        "MAN: \"ssdp:discover\"\r\n" +
                        "MX: 1\r\n" +
                        "ST: urn:aya-home-org:service:homehub:1\r\n" +
                        "\r\n", SSDP_ADDRESS, SSDP_PORT).getBytes();
                DatagramPacket mSearchPacket = new DatagramPacket(mSearchBytes, 0, mSearchBytes.length, InetAddress.getByName(SSDP_ADDRESS), SSDP_PORT);
                byte[] buf = new byte[1024];
                DatagramPacket ssdpPacket = new DatagramPacket(buf, buf.length);
                String homehubLocation = null;
                while (mKeepRunning) {
                    if (homehubLocation != null) {
                        if (!homehubLocation.equals(mHomehubLocation) && mCallback != null) {
                            mHomehubLocation = homehubLocation;
                            mCallback.onHomehubEvent(Event_Connected);
                        } else {
                            Thread.sleep(6000);
                            continue;
                        }
                    }
                    ssdpSocket.send(mSearchPacket);
                    try {
                        ssdpSocket.receive(ssdpPacket);
                    } catch (SocketTimeoutException se) {
                        continue;
                    }
                    String resp = new String(ssdpPacket.getData(), 0, ssdpPacket.getLength());
                    if (resp.startsWith("HTTP/1.1 200 OK\r\n")) {
                        int locationStartIdx = resp.indexOf("LOCATION: ") + "LOCATION: ".length();
                        int locationEndIdx = resp.indexOf("\r\n", locationStartIdx);
                        homehubLocation = resp.substring(locationStartIdx, locationEndIdx);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (ssdpSocket != null && !ssdpSocket.isClosed())
                    ssdpSocket.close();
            }
        }
    }

    public class HomehubBinder extends Binder {
        public HomehubService getService() {
            return HomehubService.this;
        }
    }

    public interface HomehubCallback {
        void onHomehubEvent(int HomehubService_Event);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setCallback(HomehubCallback callBack) {
        mCallback = callBack;
        mHomehubLocation = null;
    }

    public String getLocation() { return mHomehubLocation; }
}
