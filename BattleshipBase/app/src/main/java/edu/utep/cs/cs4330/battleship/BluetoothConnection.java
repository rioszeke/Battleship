package edu.utep.cs.cs4330.battleship;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by David on 4/18/2017.
 */

public class BluetoothConnection {

    private static final String TAG = "BATTLESHIP_TAG";
    private Handler mHandler; // handler that gets info from Bluetooth service
    Board opponentBoard;
    ConnectedThread game;
    gameActivityBT main;
    private ServerThread serverThread;
    private ClientThread clientThread;
//setters and getters
    BluetoothConnection(gameActivityBT main){
        this.main = main;
    }
    public BluetoothSocket getServerSocket(){
        return serverThread.getSocket();
    }
    public BluetoothSocket getClientSocket(){
        return clientThread.getSocket();
    }
    public void startServer(){
        serverThread = new ServerThread();
        serverThread.start();
    }
    public void startClient(BluetoothDevice clientDevice){
        clientThread = new ClientThread(clientDevice);
        clientThread.run();
    }
    public void write(String msg){
        game.write(msg.getBytes());
    }
    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                    /*Msg Parsing*/
                    if(incomingMessage.startsWith("SHIP:")){
                        //Code is 1.x, 2.y, 3.size,4.Name, 5.ishorizontal
                        String[] msg = incomingMessage.split("SHIP:");
                        for(int i =0;i<msg.length;i++) {
                            if(msg[i].equals("") || msg[i].equals(" ")){
                                continue;
                            }
                            System.out.println(""+i+":"+msg[i]);
                            String[] msgs = msg[i].split("&");
                            if (opponentBoard == null) {
                                opponentBoard = new Board(10);
                            }

                            main.setOpponentBoard(opponentBoard, Integer.parseInt(msgs[0]), Integer.parseInt(msgs[1]), Integer.parseInt(msgs[2]), msgs[3], Boolean.parseBoolean(msgs[4]));
                        }
                    }
                    else if(incomingMessage.startsWith("HIT:")){
                        //Code is 1.x, 2.y
                        String msg = incomingMessage.substring(4);
                        String[] msgs = msg.split("&");
                        main.nextMove(msgs);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }


        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }


        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
    private class ServerThread extends Thread{
        final String MY_NAME = "Battleship";
        final UUID MY_UUID
                = UUID.fromString("eeef63b7-b48b-4c10-8486-e78465a14f83");
        private BluetoothServerSocket mmServerSocket;
        private BluetoothSocket socket;
        private BluetoothAdapter bt;

        public ServerThread() {
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
                    game = new ConnectedThread(socket);
                    game.start();
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
    private class ClientThread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        final String MY_NAME = "Battleship";
        private BluetoothAdapter bt;

        final UUID MY_UUID
                = UUID.fromString("eeef63b7-b48b-4c10-8486-e78465a14f83");
        public ClientThread(BluetoothDevice device) {
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
            if(mmSocket != null) {
                main.changeTurn(false);
                game = new ConnectedThread(mmSocket);
                game.start();
            }
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
}
