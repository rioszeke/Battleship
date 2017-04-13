package edu.utep.cs.cs4330.battleship;

import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
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

public class gameActivityBT extends AppCompatActivity {

    private Board playerBoard;
    private Board opponentBoard;
    private BoardView opponentBoardView;
    private BoardView playerBoardView;
    private SelectShipsView shipsView;
    private PlacedShipsView placedShipsView;

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
    private ArrayAdapter<String> pairList;

    private Strategy strategy;
    private Battleship shipSelected;

    private bluetoothSetUpFragment btSetFrag;
    private placeShipsBTFragment placeShipsFrag;
    private waitingFragment waitingFrag;
    private RetainedFragment mRetainedFragment;

    private BluetoothAdapter bt;
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
//            btSetFrag = new bluetoothSetUpFragment();
//            placeShipsFrag = new placeShipsBTFragment();
            waitingFrag = new waitingFragment();
            moveToFragment(waitingFrag, false);

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

    }

    public void enableClicked(View view){
        Log.d("enableClicked", "Enable was clicked!");
        if(!isBTEnabled()){
            Log.d("enabledClicked", "about to enable!");
            bt.enable();

        }else{
            bt.disable();
        }
    }
    public void settingsClicked(View view){
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
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
                        if(!gameStarted && !opponentReady){
                            while(!opponentReady){
                            }
                            /** Send player's ship information here David **/
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        }
//                        while(!isInterrupted()){
//                            Thread.sleep(100);
//                            runOnUiThread(new Runnable(){
//                                public void run(){
//                                    numShots.setText("Number of Shots: "+ opponentBoard.numOfShots());
//                                    if(!playerBoard.isGameOver()&&!opponentBoard.isGameOver() && gameStarted) {
//                                        if (playerTurn) {
//                                            System.out.println("players turn");
//                                            if (!playerBoardView.hasBoardTouchListener()) {
//                                                playerBoardView.addBoardTouchListener(new BoardTouchListener());
//                                            }
//                                            player.nextMove();
//                                        }
//                                        if(!playerTurn){
//                                            System.out.println("opponents turn");
//                                            try{
//                                                thread.sleep(100);
//                                            }catch(Exception e){
//                                                System.out.println(e);
//                                            }
//                                            if (!opponentBoardView.hasBoardTouchListener()) {
//                                                opponentBoardView.addBoardTouchListener(new gameActivity.BoardTouchListener());
//                                            }
//                                            System.out.println("opponent making next move");
//                                            opponent.nextMove();
//                                        }
//                                    }
//                                }
//                            });
//                        }

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
//    public void setPlayerBoardView(){
//        playerBoardView = (BoardView) findViewById(R.id.playerBoardView);
//        playerBoardView.setBoard(opponentBoard, false);
//        mRetainedFragment.setPlayerBoardView(playerBoardView);
//        //        if(player == null) {
//        player = new humanPlayer(opponentBoard, playerTurn, playerBoardView);
//        mRetainedFragment.setPlayer(player);
//        //        }
//    }
//
//    public void setOpponentBoardView(){
//        opponentBoardView = (BoardView) findViewById(R.id.opponentBoardView);
//        opponentBoardView.setBoard(playerBoard, true);
//        mRetainedFragment.setOpponentBoardView(opponentBoardView);
//        //        if(opponent == null) {
//        if (strategy != null) {
//            opponent = new ComputerPlayer(playerBoard, !playerTurn, opponentBoardView, strategy);
//        } else {
//            opponent = new humanPlayer(playerBoard, !playerTurn, opponentBoardView);
//        }
//        mRetainedFragment.setOpponent(opponent);
//        //        }
//    }
//
//    public void setShotsView(){
//        numShots = (TextView) findViewById(R.id.numShots);
//    }


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
        public void listItemSelected(int position, long id){
            /** David implement here
             *  When a list item is clicked from the list of pairs this method sends information
             *  about the item that was clicked
             */
        }
    }

    /**
     * BoardTouchListener to remove and add when button is clicked
     */
    private class BoardTouchListener implements BoardView.BoardTouchListener {
        @Override
        public void onTouch(int x, int y) {
            if (playerTurn) {
                if (!player.getBoard().at(x + 1, y + 1).isHit()) {
                    if(!player.hit(x + 1, y + 1)){
                        playerTurn = !playerTurn;
                        mRetainedFragment.setPlayerTurn(playerTurn);
                    }
                    player.hit(x+1, y+1);
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


}
