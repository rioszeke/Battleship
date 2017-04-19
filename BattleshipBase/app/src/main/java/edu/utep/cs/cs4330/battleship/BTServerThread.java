package edu.utep.cs.cs4330.battleship;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by David on 4/16/2017.
 */

public class BTServerThread extends Thread{
    final String MY_NAME = "Battleship";
    final UUID MY_UUID
            = UUID.fromString("eeef63b7-b48b-4c10-8486-e78465a14f83");
    public BTGame game;
    private BluetoothServerSocket mmServerSocket;
    private BluetoothSocket socket;
    private BluetoothAdapter bt;

        public BTServerThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            bt = BluetoothAdapter.getDefaultAdapter();
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bt.listenUsingRfcommWithServiceRecord(MY_NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }
        public synchronized BluetoothSocket getSocket(){
            return socket;
        }

        public void run() {
            socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                    Log.d(TAG,"Server running ");
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
//                    game = new BTGame();
//                    game.start();
                    Log.d(TAG,"Connection accepted");
                    try {
                        mmServerSocket.close();
                    }
                    catch(IOException e){}
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

