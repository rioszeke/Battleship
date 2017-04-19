package edu.utep.cs.cs4330.battleship;

import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import static android.content.ContentValues.TAG;

public class gameActivityBT extends AppCompatActivity {

    private Board playerBoard;
    private Board opponentBoard;
    private BoardView opponentBoardView;
    private BoardView playerBoardView;
    private SelectShipsView shipsView;
    private PlacedShipsView placedShipsView;
    private BluetoothConnection btConn;
    private Thread thread;
    private TextView numShots;
    private MyDialogFragment promptFragment;
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
    private Strategy strategy;
    private Battleship shipSelected;
    private bluetoothSetUpFragment btSetFrag;
    private placeShipsBTFragment placeShipsFrag;
    private playFragmentBT playFrag;
    private RetainedFragment mRetainedFragment;
    private BluetoothAdapter bt;
    private BluetoothSocket socket;
    private boolean connectWithPlayer = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //What to do when activity is started
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_bt);
        btConn = new BluetoothConnection(this);
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
            moveToFragment(btSetFrag, false);
        }

    }
    public void nextMove(String[] msgs){
        opponent.nextMove(Integer.parseInt(msgs[0]),Integer.parseInt(msgs[1]));
    }
    public void changeTurn(Boolean turn){
        playerTurn = turn;
    }
    public void startShipPlacement(){
        placeShipsFrag = new placeShipsBTFragment();
        moveToFragment(placeShipsFrag, false);
    }

    public void setOpponentBoard(Board opBoard, int x, int y, int size, String name, boolean isHorizontal){
        opponentBoard = opBoard;
        Battleship ship = new Battleship(name, size);
        Place head = opponentBoard.at(x, y);
        opponentBoard.placeShip(ship, head.getX(), head.getY(), isHorizontal);
    }

    private void resetGame(Board board, BoardView boardView){
        board.reset();
        boardView.removeAllBoardTouchListeners();
    }
    /**
     * If yes button is clicked game is restarted
     * @param view
     */
    public void yesClicked(View view){
        resetGame(playerBoard, opponentBoardView);
        resetGame(opponentBoard, playerBoardView);
        gameStarted = false;
        startShipPlacement();
        promptFragment.dismiss();
    }

    /**
     * If no button is clickin from view
     * @param view
     */
    public void noClicked(View view){
        promptFragment.dismiss();
    }
//when new button is clicked
    public void newClicked(View view){
        //if game is not over you need to ask if they are sure
        if(!playerBoard.isGameOver()&&!opponentBoard.isGameOver()) {
            FragmentManager fm = getFragmentManager();
            promptFragment = new MyDialogFragment();
            promptFragment.show(fm, "sample fragment");
        }
        else{
            //otherwise just do it
            resetGame(playerBoard, opponentBoardView);
            resetGame(opponentBoard, playerBoardView);
            gameStarted = false;
            startShipPlacement();
        }
    }
    //when done button is clicked inside the setup fragment
    public void doneClicked(View view){
        mRetainedFragment.setPlayerBoard(playerBoard);
        //if ships aren't all deployed don't do anything
        if(playerBoard.defaultShipsDeployed()){
            playerReady = true;
            //move to waiting fragment until board is received
            waitingFragment watingFrag = new waitingFragment();
            moveToFragment(watingFrag, true);
            thread = new Thread(){
                @Override
                public void run(){
                    try{
                        if(!gameStarted){
                            /** Send player's ship information here **/
                            //Send each ship to the opponent
                            Iterable<Battleship> shipSend = playerBoard.ships();
                            for(Battleship ship : shipSend) {
                                String x = Integer.toString(ship.head().getX());
                                String y = Integer.toString(ship.head().getY());
                                String size = Integer.toString(ship.size());
                                String shipName = ship.name();
                                String isHori = Boolean.toString(ship.isHorizontal());
                                //the protocol for the ship to be sent
                                String msg = "SHIP:"+x+"&"+y+"&"+size+"&"+shipName+"&"+isHori;
                                btConn.write(msg);
                            }
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
    //move from a fragment to another one
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
//initialize placedShips fragment view
    public void setSelectShipsView(){
        shipsView = (SelectShipsView) findViewById(R.id.shipsToPlaceBT);
        shipsView.setBoard(playerBoard);
        shipsView.addShipSelectListener(new shipSelectListener());
    }
//initialize placedShips fragment view
    public void setPlacedShipsView(){
        placedShipsView = (PlacedShipsView) findViewById(R.id.placedShipsViewBT);
        placedShipsView.setBoard(playerBoard);
        placedShipsView.addPlacedListener(new PlacedListener());
    }

    /* playerview needs board to observe */
    public void setPlayerBoardView(){
        playerBoardView = (BoardView) findViewById(R.id.playerBoardView);
        playerBoardView.setBoard(opponentBoard, false);
        mRetainedFragment.setPlayerBoardView(playerBoardView);
        //        if(player == null) {
        player = new humanPlayer(opponentBoard, playerTurn, playerBoardView);
        mRetainedFragment.setPlayer(player);
        //        }
    }
//boardview need a board to observe
    public void setOpponentBoardView(){
        opponentBoardView = (BoardView) findViewById(R.id.opponentBoardView);
        opponentBoardView.setBoard(playerBoard, true);
        mRetainedFragment.setOpponentBoardView(opponentBoardView);
        opponent = new BluetoothPlayer(playerBoard, !playerTurn, opponentBoardView);
        mRetainedFragment.setOpponent(opponent);
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
                    btConn.startServer();
                    //client part, server failed
                    btConn.startClient(clientDevice);
                    socket = btConn.getServerSocket();
                    if(socket ==null){
                        socket = btConn.getClientSocket();
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
                    btConn.write(msg);
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
        public void hit(Place place, int numOfShots) {
            if (place.hasShip()) {
                if (!place.ship().isSunk()) {
                    if (playerTurn && sound) {
                        opponentSad.start();
                    } else if (sound) {
                        opponentContent.start();
                    }
                    v.vibrate(100);
                }
            } else {
                if (playerTurn && sound) {
                    opponentContent.start();
                } else if (sound) {
                    opponentSad.start();
                }
            }

            runOnUiThread(new Runnable() {
                public void run() {
                    opponentBoardView.invalidate();
                    playerBoardView.invalidate();
                }
            });
        }
        /**
         * Clears listeners, plays sound and vibrates
         * @param numOfShots
         */
        public void gameOver(int numOfShots){
            playerBoardView.removeAllBoardTouchListeners();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (playerBoard.isGameOver()) {
                        toast("Opponent won!");
                    } else {
                        toast("Player won!");
                    }
                }
            });
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
            runOnUiThread(new Runnable() {
                public void run() {
                    placedShipsView.invalidate();
                    shipsView.invalidate();
                }});
        }
    }


}
