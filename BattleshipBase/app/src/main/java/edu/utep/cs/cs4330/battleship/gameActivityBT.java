package edu.utep.cs.cs4330.battleship;

import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class gameActivityBT extends AppCompatActivity {

    private Board playerBoard;
    private Board opponentBoard;
    private BoardView opponentBoardView;
    private BoardView playerBoardView;
    private SelectShipsView shipsView;
    private PlacedShipsView placedShipsView;
    private ServerThread serverThread;
    private ClientThread clientThread;
    private Thread thread;
    private TextView numShots;
    private MyDialogFragment promptFragment;
    public ConnectedThread game;
    protected MediaPlayer opponentContent;
    protected MediaPlayer opponentSad;
    protected MediaPlayer opponentAngry;
    protected MediaPlayer gameOver;
    protected Vibrator v;
    static private boolean sound;
    private Player player;
    private Player opponent;
    private Boolean playerTurn;
    private boolean gameStarted = false;
    private boolean opponentReady = false;
    private boolean playerReady = false;
    private ArrayAdapter<String> pairList;

    private Strategy strategy;
    private Battleship shipSelected;
    private bluetoothSetUpFragment btSetFrag;
    private placeShipsBTFragment placeShipsFrag;
    private waitingFragment waitingFrag;
    private playFragmentBT playFrag;
    private RetainedFragment mRetainedFragment;
    private BluetoothAdapter bt;
    private BluetoothSocket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_bt);
        bt = BluetoothAdapter.getDefaultAdapter();
        opponentContent = MediaPlayer.create(findViewById(R.id.fragment_frame_bt).getContext(), R.raw.woohoo);
        opponentSad = MediaPlayer.create(findViewById(R.id.fragment_frame_bt).getContext(), R.raw.doh2);
        opponentAngry = MediaPlayer.create(findViewById(R.id.fragment_frame_bt).getContext(), R.raw.aaaahh);
        gameOver = MediaPlayer.create(findViewById(R.id.fragment_frame_bt).getContext(), R.raw.about_time);
        v = (Vibrator) findViewById(R.id.fragment_frame_bt).getContext().getSystemService(Context.VIBRATOR_SERVICE);
        sound = true;
        FragmentManager fm = getFragmentManager();
        mRetainedFragment = (RetainedFragment) fm.findFragmentByTag("RetainedFragment");
        this.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // create the fragment and data the first time
        if (mRetainedFragment == null) {
            playerTurn = true;
            playerBoard = new Board(10);
            // add the fragment
            mRetainedFragment = new RetainedFragment();
            fm.beginTransaction().add(mRetainedFragment, "RetainedFragment").commit();
            // load data from a data source or perform any calculation
            mRetainedFragment.setPlayerTurn(playerTurn);
            mRetainedFragment.setPlayerBoard(playerBoard);
            mRetainedFragment.setSound(sound);
            btSetFrag = new bluetoothSetUpFragment();
            btSetFrag.addListItemSelectListener(new listItemSelectListener());
//            placeShipsFrag = new placeShipsBTFragment();
//            waitingFrag = new waitingFragment();
            moveToFragment(btSetFrag, false);

        }
//        else{
//            playerBoard = mRetainedFragment.getPlayerBoard();
//            opponentBoard = mRetainedFragment.getOpponentBoard();
//            player = mRetainedFragment.getPlayer();
//            opponent = mRetainedFragment.getOpponent();
//            playerTurn = mRetainedFragment.getPlayerTurn();
//            strategy = mRetainedFragment.getStrategy();
//            sound = mRetainedFragment.getSound();
//            if(!playerBoard.hasBoardChangeListener()) {
//                playerBoard.addBoardChangeListener(new gameActivity.BoardChangeListener());
//            }
//            if(!opponentBoard.hasBoardChangeListener()) {
//                opponentBoard.addBoardChangeListener(new gameActivity.BoardChangeListener());
//            }
//            playerBoardView = mRetainedFragment.getPlayerBoardView();
//            playerBoardView.setBoard(opponentBoard, true);
//            opponentBoardView = mRetainedFragment.getOpponentBoardView();
//            opponentBoardView.setBoard(playerBoard, true);
//            gameStarted = true;
//            startThreads();
//            playFrag = new playFragment();
//            this.setRequestedOrientation(
//                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//            moveToFragment(playFrag, false);
//        }

//        playerViewThread.start();
//        opponentViewThread.start();
    }


    public void startShipPlacement(){
        placeShipsFrag = new placeShipsBTFragment();
        moveToFragment(placeShipsFrag, false);
    }
    /** These methods are used to receive various types of input in order to
     *  set local board as identical to opponent's board David
     */
    public void setOpponentBoard(Iterable<Battleship> ships){
        for(Battleship ship : ships){
            opponentBoard.placeShip(ship, ship.head().getX(), ship.head().getY(), ship.isHorizontal());
        }
    }

    public void setOpponentBoard(Battleship ship){
        opponentBoard.placeShip(ship, ship.head().getX(), ship.head().getY(), ship.isHorizontal());
    }

    public void setOpponentBoard(int x, int y, int size, String name, boolean isHorizontal){
        Battleship ship = new Battleship(name, size);
        Place head = opponentBoard.at(x, y);
        opponentBoard.placeShip(ship, head.getX(), head.getY(), isHorizontal);
    }

    /** further implementation needed here to send info
     *  to other player David
     */
    public Iterable<Battleship> sendPlayerShips(){
        return playerBoard.ships();
    }

    public void doneClicked(View view){
        mRetainedFragment.setPlayerBoard(playerBoard);
        if(playerBoard.defaultShipsDeployed()){
            playerReady = true;
            waitingFragment watingFrag = new waitingFragment();
            moveToFragment(watingFrag, true);
            thread = new Thread(){
                @Override
                public void run(){
                    try{
                        if(!gameStarted){
                            /** Send player's ship information here David **/
//                            public void setOpponentBoard(int x, int y, int size, String name, boolean isHorizontal){
//                            public Iterable<Battleship> ships(){
//                                return ships;
//                            }
                            Iterable<Battleship> shipSend = playerBoard.ships();
                            for(Battleship ship : shipSend) {
                                String x = Integer.toString(ship.head().getX());
                                String y = Integer.toString(ship.head().getY());
                                String size = Integer.toString(ship.size());
                                String shipName = ship.name();
                                String isHori = Boolean.toString(ship.isHorizontal());
                                String msg = "SHIP:"+x+"&"+y+"&"+size+"&"+shipName+"&"+isHori;
                                game.write(msg.getBytes());
                            }
//                            playFrag = new playFragment();
//                            moveToFragment(playFrag, true);
                        }

                        while(!isInterrupted()){
                            if(opponentBoard != null) {
                                if(playFrag == null) {
                                    playFrag = new playFragmentBT();
                                    moveToFragment(playFrag, true);
                                    gameStarted = true;
                                    if (!playerBoard.hasBoardChangeListener()) {
                                        playerBoard.addBoardChangeListener(new BoardChangeListener());
                                    }
                                    if (!opponentBoard.hasBoardChangeListener()) {
                                        opponentBoard.addBoardChangeListener(new BoardChangeListener());
                                    }
                                }
                                Thread.sleep(100);
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        numShots.setText("Number of Shots: " + opponentBoard.numOfShots());
                                        if (!playerBoard.isGameOver() && !opponentBoard.isGameOver() && gameStarted) {
                                            if (playerTurn) {
                                                if (!playerBoardView.hasBoardTouchListener()) {
                                                    playerBoardView.addBoardTouchListener(new gameActivityBT.BoardTouchListener());
                                                }
                                            }
                                            if (!playerTurn) {
                                                try {
                                                    thread.sleep(100);
                                                } catch (Exception e) {
                                                    System.out.println(e);
                                                }
                                                if (!opponentBoardView.hasBoardTouchListener()) {
                                                    opponentBoardView.addBoardTouchListener(new gameActivityBT.BoardTouchListener());
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
    }

    private Boolean isBTEnabled(){
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        return bt != null && bt.isEnabled();
    }

    protected void moveToFragment(Fragment fragment, Boolean addToBackStack){
        if(addToBackStack) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_frame_bt, fragment, fragment.getClass().getSimpleName()).addToBackStack(null).commit();
        }
        else{
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_frame_bt, fragment, fragment.getClass().getSimpleName()).commit();
        }
    }

    protected void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void setSelectShipsView(){
        shipsView = (SelectShipsView) findViewById(R.id.shipsToPlaceBT);
        shipsView.setBoard(playerBoard);
        shipsView.addShipSelectListener(new shipSelectListener());
    }

    public void setPlacedShipsView(){
        placedShipsView = (PlacedShipsView) findViewById(R.id.placedShipsViewBT);
        placedShipsView.setBoard(playerBoard);
        placedShipsView.addPlacedListener(new PlacedListener());
    }

    /* to implement later */
    public void setPlayerBoardView(){
        playerBoardView = (BoardView) findViewById(R.id.playerBoardView);
        playerBoardView.setBoard(opponentBoard, false);
        mRetainedFragment.setPlayerBoardView(playerBoardView);
        //        if(player == null) {
        player = new humanPlayer(opponentBoard, playerTurn, playerBoardView);
        mRetainedFragment.setPlayer(player);
        //        }
    }
//
    public void setOpponentBoardView(){
        opponentBoardView = (BoardView) findViewById(R.id.opponentBoardView);
        opponentBoardView.setBoard(playerBoard, true);
        mRetainedFragment.setOpponentBoardView(opponentBoardView);
        //        if(opponent == null) {
        opponent = new BluetoothPlayer(playerBoard, !playerTurn, opponentBoardView);
        mRetainedFragment.setOpponent(opponent);
        //        }
    }
//
    public void setShotsView(){
        numShots = (TextView) findViewById(R.id.numShots);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);

        if(sound){
            menu.getItem(0).setTitle("Sound on");
        }
        else{
            menu.getItem(0).setTitle("Sound off");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sound:
                if (item.getTitle().equals("Sound off")) {
                    sound = true;
                    item.setTitle("Sound on");
                    mRetainedFragment.setSound(sound);

                } else {
                    sound = false;
                    item.setTitle("Sound off");
                    mRetainedFragment.setSound(sound);

                }
                return true;
            default:
                return true;
        }
    }
    /** Listeners begin **/

    private class listItemSelectListener implements bluetoothSetUpFragment.listItemSelectListener{
        @Override
        public void listItemSelected(int position, long id) {
            /** David implement here
             *  When a list item is clicked from the list of pairs this method sends information
             *  about the item that was clicked
             */
            String deviceName = btSetFrag.getPairs().get(position);
            Set<BluetoothDevice> devices = bt.getBondedDevices();
            for (BluetoothDevice device : devices) {
                if (device.getName().equals(deviceName)) {
                    Log.d(TAG, deviceName);
                    final BluetoothDevice clientDevice = device;
                    serverThread = new ServerThread();
                    serverThread.start();
                    //client part, server failed
                    clientThread = new ClientThread(clientDevice);
                    clientThread.run();
                    socket = serverThread.getSocket();
                    if(socket ==null){
                        socket = clientThread.getSocket();
                    }
                    startShipPlacement();
                }
            }
        }
    }

    /**
     * BoardTouchListener to remove and add when button is clicked
     */
    private class BoardTouchListener implements BoardView.BoardTouchListener {
        @Override
        public void onTouch(int x, int y) {
            Log.d(TAG, "onTouch: ");
            if (playerTurn) {
                if (!player.getBoard().at(x + 1, y + 1).isHit()) {
                    if(!player.hit(x + 1, y + 1)){
                        playerTurn = !playerTurn;
                        mRetainedFragment.setPlayerTurn(playerTurn);
                    }
                    Log.d(TAG, "onTouch: ");
                    String msg = "HIT:"+x+"&"+y;
                    game.write(msg.getBytes());
                }
            }else{
                if (!opponent.getBoard().at(x + 1, y + 1).isHit()) {
                    if(!opponent.hit(x + 1, y + 1)){
                        playerTurn = !playerTurn;
                        mRetainedFragment.setPlayerTurn(playerTurn);
                    }
                    opponent.hit(x+1, y+1);

                }
            }
        }
    }

    /**
     * Nested class implements BoardChangeListener interface
     * Observes and reports changes to the board
     */
    protected class BoardChangeListener implements Board.BoardChangeListener{

        /** will force onDraw to be called again with
         * invalidate().
         * @param place
         * @param numOfShots
         */
        public void hit(Place place, int numOfShots){
            if(place.hasShip()){
                if(!place.ship().isSunk()) {
                    if(playerTurn && sound){
                        opponentSad.start();
                    }
                    else if(sound){
                        opponentContent.start();
                    }
                    v.vibrate(100);
                }
            }
            else{
                if(playerTurn && sound){
                    opponentContent.start();
                }else if (sound){
                    opponentSad.start();
                }
            }
            opponentBoardView.invalidate();
            playerBoardView.invalidate();
        }

        /**
         * Clears listeners, plays sound and vibrates
         * @param numOfShots
         */
        public void gameOver(int numOfShots){
            playerBoardView.removeAllBoardTouchListeners();
            if(playerBoard.isGameOver()){
                toast("Opponent won!");
            }else{
                toast("Player won!");
            }
            if(sound) {
                gameOver.start();
            }
            v.vibrate(1000);
        }

        /**
         * Plays sound and vibrates
         * @param ship
         */
        public void shipSunk(Battleship ship){
            if(playerTurn && sound) {
                opponentAngry.start();
            }
            else if (sound){
                opponentContent.start();
            }
            v.vibrate(500);
        }
    }

    protected class shipSelectListener implements SelectShipsView.ShipSelectListener{

        @Override
        public void onSelect(Battleship ship){
            shipSelected = ship;
            if(shipSelected.isDeployed()){
                shipSelected.removePlaces();
                placedShipsView.invalidate();
                shipsView.invalidate();
            }
        }
    }

    protected class PlacedListener implements PlacedShipsView.PlacedListener {

        @Override
        public void onTouch(int x, int y) {
            //going to reorient or replace
            if (shipSelected == null) {
                if (playerBoard.at(x + 1, y + 1).hasShip()) {
                    //if head of ship is touched reorient
                    if (playerBoard.at(x + 1, y + 1).ship().head().getX() == x + 1
                            && playerBoard.at(x + 1, y + 1).ship().head().getY() == y + 1) {
                        reorientShip(playerBoard.at(x + 1, y + 1).ship());
                    } else {
                        shipSelected = playerBoard.at(x + 1, y + 1).ship();
                    }
                }//else do nothing

            } else {//going to place ship that has been selected
                //if ship is already on the board replace
                if (shipSelected.isDeployed()) {
                    replaceShip(shipSelected, x, y);
                } else {
                    //place ship that has been selected
                    if (playerBoard.placeShip(shipSelected, x + 1, y + 1, true)) {
                        refreshView();
                        shipSelected = null;
                    }
                }
            }
        }

        private boolean reorientShip(Battleship ship) {
            Place head = ship.head();
            boolean wasHorizontal = ship.isHorizontal();
            ship.removePlaces();
            //if ship was placed successfully
            if (playerBoard.placeShip(ship, head.getX(), head.getY(), !wasHorizontal)) {
                refreshView();
                return true;
            } else {//if ship was not able to be placed successfully
                playerBoard.placeShip(ship, head.getX(), head.getY(), wasHorizontal);
                return false;
            }
        }

        private boolean replaceShip(Battleship ship, int x, int y) {
            Place head = ship.head();
            boolean wasHorizontal = ship.isHorizontal();
            ship.removePlaces();
            if (playerBoard.placeShip(ship, x + 1, y + 1, wasHorizontal)) {
                refreshView();
                shipSelected = null;
                return true;
            } else {
                playerBoard.placeShip(ship, head.getX(), head.getY(), wasHorizontal);
                return false;
            }

        }
        protected void refreshView() {
            placedShipsView.invalidate();
            shipsView.invalidate();
        }
    }

    private static final String TAG = "BATTLESHIP_TAG";
    private Handler mHandler; // handler that gets info from Bluetooth service

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
                // Read from the InputStrea
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

                            setOpponentBoard(Integer.parseInt(msgs[0]), Integer.parseInt(msgs[1]), Integer.parseInt(msgs[2]), msgs[3], Boolean.parseBoolean(msgs[4]));
                        }
                    }
                    else if(incomingMessage.startsWith("HIT:")){
                        //Code is 1.x, 2.y
                        String msg = incomingMessage.substring(4);
                        String[] msgs = msg.split("&");
                        opponent.nextMove(Integer.parseInt(msgs[0]),Integer.parseInt(msgs[1]));
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
                playerTurn = false;
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
