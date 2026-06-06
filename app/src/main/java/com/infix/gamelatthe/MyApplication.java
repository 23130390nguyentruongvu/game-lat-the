package com.infix.gamelatthe;

import android.app.Application;

import com.infix.gamelatthe.utils.NetworkListener;
import com.infix.gamelatthe.utils.Observer;

public class MyApplication extends Application {
    private NetworkListener networkListener;

    @Override
    public void onCreate() {
        super.onCreate();
        networkListener = new NetworkListener(this.getApplicationContext());
        networkListener.registerNetworkChange();
    }

    public void registerObserver(Observer observer) {
        networkListener.register(observer);
    }

    public void removeObserver(Observer observer) {
        networkListener.remove(observer);
    }
}
