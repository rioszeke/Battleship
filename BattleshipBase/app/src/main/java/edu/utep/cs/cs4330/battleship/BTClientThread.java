package edu.utep.cs.cs4330.battleship;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by David on 4/16/2017.
 */

public class BTClientThread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    final String MY_NAME = "Battleship";
    private BluetoothAdapter bt;
    public BTGame game;

    final UUID MY_UUID
            = UUID.fromString("eeef63b7-b48b-4c10-8486-e78465a14f83");
    public BTClientThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;
        bt = BluetoothAdapter.getDefaultAdapter();

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }
    public synchronized BluetoothSocket getSocket(){
        return mmSocket;
    }
    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        bt.cancelDiscovery();
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
            Log.d(TAG,"Connected to "+mmSocket.getRemoteDevice());
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
//        if(mmSocket != null) {
//             game = new BTServerThread(mmSocket);
//            game.start();
//        }
        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
//        manageMyConnectedSocket(mmSocket);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

}
