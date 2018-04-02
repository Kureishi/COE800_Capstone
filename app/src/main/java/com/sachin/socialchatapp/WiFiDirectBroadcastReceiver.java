package com.sachin.socialchatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;            // create objects
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainActivity mActivity) {      // Constructor

        this.mManager = mManager;       // instantiate variables
        this.mChannel = mChannel;
        this.mActivity = mActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction(); // check current action

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {   // check to see if WiFi is enabled and notify appropriate activity

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);    // have value of Wifi in this state (on/off) or -1

            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {    // if WiFi is enabled
                Toast.makeText(context, "Wifi is ON", Toast.LENGTH_SHORT).show();   // indicate with a Toast
            }
            else {
                Toast.makeText(context, "Wifi is OFF", Toast.LENGTH_SHORT).show();   // indicate with a Toast if off
            }
        }
        else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) { // call WifiP2PManager.requestPeers() to get a list of current peers

            if(mManager != null) {                                              // if there is a device
                mManager.requestPeers(mChannel, mActivity.peerListListener);    // get the current device list
            }
        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) { // respond to new connections/disconnections

            if(mManager == null) {                                                  // if nothing change
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO); // if connections change

            if(networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);     // display connection info
            }
            else {
                mActivity.connectionStatus.setText("Device Disconnected");                      // if not connected, display message
            }
        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) { // respond to this device's WiFi state changing
            //do something
        }
    }
}
