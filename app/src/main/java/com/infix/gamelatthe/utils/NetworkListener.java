package com.infix.gamelatthe.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class NetworkListener {
    private boolean isRegistered = false;
    private boolean currentNetwork;
    private List<Observer> observers = new ArrayList<>();
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private final Context context;

    public NetworkListener(Context applicationContext) {
        context = applicationContext;
        currentNetwork = NetworkUtils.isNetworkAvailable(applicationContext);
    }

    //-	Lỗi kết nối mạng kết nối đến Firebase dịch vụ: (Rẽ nhánh tại bất kỳ bước nào
    // có tương tác mạng như 6.1.5, 6.1.9 hoặc 6.2.3)
    public void registerNetworkChange() {
        if (isRegistered) return;

        //6.5.1 Lớp tiện ích kiểm tra trạng thái mạng
        // của hệ thống chủ động phát hiện thiết bị mất tín hiệu Internet (Wi-Fi/4G bị ngắt đột ngột)
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                updateObservers(true);
                currentNetwork = true;
                Log.d("NetworkListener", Thread.currentThread().getName() + true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                //6.5.2 Hệ thống lập tức gọi các đối tượng quan sát để thông báo trạng
                // thái là false. Các đối tượng quán sát dựa vào đó để quyết
                // định gọi hàm liên quan đến mạng hay không.
                updateObservers(false);
                currentNetwork = false;
                Log.d("NetworkListener", Thread.currentThread().getName() + false);
            }
        };

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);

        isRegistered = true;
    }

    public void unregisterNetworkChange() {
        if (!isRegistered) return;
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            isRegistered = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void register(Observer observer) {
        if (observer == null) return;
        if (!isRegistered)
            registerNetworkChange();
        observer.onUpdateNetworkValid(currentNetwork);
        observers.add(observer);
    }

    public void remove(Observer observer) {
        if (observer == null) return;
        observers.remove(observer);
        if (observers.isEmpty())
            unregisterNetworkChange();
    }

    private void updateObservers(boolean isNetworkValid) {
        for (Observer ob : observers) {
            ob.onUpdateNetworkValid(isNetworkValid);
        }
    }
}
