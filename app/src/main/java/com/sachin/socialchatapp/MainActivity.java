package com.sachin.socialchatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btnOnOff, btnDiscover, btnSend; // Wifi enable/disable, discover nearby available peers, to send message
    ListView listView;      // available peers shown
    TextView read_msg_box, connectionStatus;    // received message shown, Connection Status shown
    EditText writeMsg;                          // send message in EditText

    WifiManager wifiManager;            // object of WifiManager -> enable/disable WiFi
    WifiP2pManager mManager;            // objects used in BroadcastReceiver (manage WiFi peer-to-peer connectivity)
    WifiP2pManager.Channel mChannel;    // channel connects application to WiFi P2P framework (most P2P operations require Channel as argument)

    BroadcastReceiver mReceiver;        // BroadcastReceiver object
    IntentFilter mIntentFilter;         // to use intents

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();     // where peers appear
    String[] deviceNameArray;                                       // show device name in list view
    WifiP2pDevice[] deviceArray;                                    // use array to connect a device

    static final int MESSAGE_READ = 1;                              // indicator of read message (since switch case take in int)

    ServerClass serverClass;                                        // create objects for three inner classes
    ClientClass clientClass;
    SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialWork();
        exqListener();
    }

    Handler handler = new Handler(new Handler.Callback() {      // create object for handler (for read message)
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);     // read into first position of byte array
                    read_msg_box.setText(tempMsg);                                // display in read message box on screen
                    break;
            }
            return true;
        }
    });

    private void exqListener() {                                    // execute listener methods (when buttons are clicked)
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(wifiManager.isWifiEnabled()) {   // check WiFi status
                    wifiManager.setWifiEnabled(false);  // if WiFi on, set it to false (since already enabled)
                    btnOnOff.setText("ON");     // set text of button after WiFi off to indicate next press turn "ON"
                }
                else {
                    wifiManager.setWifiEnabled(true);   // if off, then enable it
                    btnOnOff.setText("OFF");     // set text of button after WiFi on to indicate next press turn "OFF"
                }
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {  // listen for when discovery process initiated
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Discovery Started");      // if discovered started successful
                    }

                    @Override
                    public void onFailure(int i) {
                        connectionStatus.setText("Discovery Starting Failed");  // if discovered started not successful
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {     // when an item pressed
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final WifiP2pDevice device = deviceArray[i];        // for each device
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;        // assign the config as their address

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show(); // display with device connected to
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Not Connected " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {                 // when "send" button pressed
            @Override
            public void onClick(View view) {
                String msg = writeMsg.getText().toString();                     // from write message box
                sendReceive.write(msg.getBytes());
            }
        });
    }

    private void initialWork() {

        btnOnOff = (Button)findViewById(R.id.onOff);    // Initialize variables to id-components
        btnDiscover = (Button)findViewById(R.id.discover);
        btnSend = (Button)findViewById(R.id.sendButton);

        listView = (ListView)findViewById(R.id.peerListView);

        read_msg_box = (TextView)findViewById(R.id.readMsg);
        connectionStatus = (TextView)findViewById(R.id.connectionStatus);

        writeMsg = (EditText)findViewById(R.id.writeMsg);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE); // used for WiFi services

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE); // used for BroadcastReceiver
        mChannel = mManager.initialize(this, getMainLooper(), null); // initialize P2P channel

        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);  // initialize BroadcastReceiver constructor

        mIntentFilter = new IntentFilter();                                             // initialize intent filter
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);          // add intents
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {  // inform whether peerList changed or not
            if(!peerList.getDeviceList().equals(peers)) {           // if current list not same as old list
                peers.clear();                                      // clear old/previous list
                peers.addAll(peerList.getDeviceList());             // add the devices in the new/current list

                deviceNameArray = new String[peerList.getDeviceList().size()];  // initialize deviceNameArray
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];   // initialize deviceArray

                int index = 0;
                for (WifiP2pDevice device: peerList.getDeviceList()) {  // iterations depend on number of devices in peerList (all)
                    deviceNameArray[index] = device.deviceName;         // get name of the device
                    deviceArray[index] = device;                        // get the device
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray); // have the device names in a list view layout
                listView.setAdapter(adapter);   // put the above list of devices in the listview
            }

            if(peers.size() == 0) {                     // if no device found (0 devices)

                Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_SHORT).show();  // display toast message
                return;
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {            // the connection info between devices
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;    // owner's address

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {       // if formed group and also owner
                connectionStatus.setText("Host");                           // indicate device is a server/host

                serverClass = new ServerClass();
                serverClass.start();                                        // start the Server Class
            }
            else if(wifiP2pInfo.groupFormed) {                              // if only formed group
                connectionStatus.setText("Client");                         // indicate device is a client

                clientClass = new ClientClass(groupOwnerAddress);           // InetAddress is that of group owner
                clientClass.start();                                        // start Client class
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter); // register the receiver with specified intents
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);  // unregister receiver
    }

    public class ServerClass extends Thread {   // create inner class for Server Class
        Socket socket;                          // standard socket
        ServerSocket serverSocket;              // server socket

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);  // server socket connected to port 8888
                socket = serverSocket.accept();             // accept connections/messages to this server
                sendReceive = new SendReceive(socket);      // initiate transmission between two end of port
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendReceive extends Thread {              // create send/receive thread
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt) {
            socket=skt;                                     // initialize socket
            try {
                inputStream = socket.getInputStream();      // instantiate streams
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];                 // to store messages in
            int bytes;

            while (socket != null) {                        // while there are still sockets, ready to listen to message
                try {
                    bytes = inputStream.read(buffer);       // read from the buffer
                    if (bytes > 0) {                        // have something in the message
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget(); // message.what, arg1, arg2, object
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {                   // for sending message
            try {
                outputStream.write(bytes);                  // send to output stream
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientClass extends Thread {               // inner class for Client Class
        Socket socket;                                      // standard socket
        String hostAdd;                                     // string for host address

        public ClientClass(InetAddress hostAddress) {
            hostAdd = hostAddress.getHostAddress();         // initialize hostAdd with device host address through constructor
            socket = new Socket();                          // initialize socket
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888), 500);  // connect host address to port 8888 (same on server) with timeout 500
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * Android System notifies us about events of WiFi using Broadcast
 *
 * INTENTS:
 * WIFI_P2P_STATE_CHANGE_ACTION -> indicates whether WiFi P2P is enabled/disabled
 * WIFI_P2P_PEERS_CHANGE_ACTION -> indicates that the available peer list has changed (when peer list changed, get new list using this intent)
 * WIFI_P2P_CONNECTION_CHANGED_ACTION -> indicates the state of WiFi P2P connectivity has changed
 * WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> indicates this device's configuration details have changed (ex. if name change)
 * */