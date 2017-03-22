package edu.utep.cs.cs4330.battleship;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class gameActivity extends AppCompatActivity {

    private boolean gameStarted = false;
    private Board playerBoard;
    private Board opponentBoard;
    private BoardView opponentBoardView;
    private BoardView playerBoardView;
    private SelectShipsView shipsView;
    private PlacedShipsView placedShipsView;
    private Thread playerViewThread;
    private Thread opponentViewThread;
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

    private Strategy strategy;
    private Battleship shipSelected;

    private difficultyFragment difficultyFrag;
    private playFragment playFrag;
    private RetainedFragment mRetainedFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        opponentContent = MediaPlayer.create(findViewById(R.id.fragment_frame).getContext(), R.raw.woohoo);
        opponentSad = MediaPlayer.create(findViewById(R.id.fragment_frame).getContext(), R.raw.doh2);
        opponentAngry = MediaPlayer.create(findViewById(R.id.fragment_frame).getContext(), R.raw.aaaahh);
        gameOver = MediaPlayer.create(findViewById(R.id.fragment_frame).getContext(), R.raw.about_time);
        v = (Vibrator) findViewById(R.id.fragment_frame).getContext().getSystemService(Context.VIBRATOR_SERVICE);
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
            difficultyFrag = new difficultyFragment();
            difficultyFrag.addButtonListener(new ButtonSelectListener());
            mRetainedFragment.setPlayerTurn(playerTurn);
            mRetainedFragment.setPlayerBoard(playerBoard);
            mRetainedFragment.setSound(sound);
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_frame, difficultyFrag, difficultyFrag.getClass().getSimpleName()).commit();


        }
        else{
            playerBoard = mRetainedFragment.getPlayerBoard();
            opponentBoard = mRetainedFragment.getOpponentBoard();
            player = mRetainedFragment.getPlayer();
            opponent = mRetainedFragment.getOpponent();
            playerTurn = mRetainedFragment.getPlayerTurn();
            strategy = mRetainedFragment.getStrategy();
            sound = mRetainedFragment.getSound();
            if(!playerBoard.hasBoardChangeListener()) {
                playerBoard.addBoardChangeListener(new BoardChangeListener());
            }
            if(!opponentBoard.hasBoardChangeListener()) {
                opponentBoard.addBoardChangeListener(new BoardChangeListener());
            }
            playerBoardView = mRetainedFragment.getPlayerBoardView();
            playerBoardView.setBoard(opponentBoard, true);
            opponentBoardView = mRetainedFragment.getOpponentBoardView();
            opponentBoardView.setBoard(playerBoard, true);
            gameStarted = true;
            startThreads();
            playFrag = new playFragment();
            this.setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            moveToFragment(playFrag);
        }
    }

    private void startGame() {
        opponentBoard = new Board(10);
        opponentBoard.placeShips();
        mRetainedFragment.setOpponentBoard(opponentBoard);
        if(!playerBoard.hasBoardChangeListener()) {
            playerBoard.addBoardChangeListener(new BoardChangeListener());
        }
        if(!opponentBoard.hasBoardChangeListener()) {
            opponentBoard.addBoardChangeListener(new BoardChangeListener());
        }
        startThreads();
        gameStarted = true;
        playFrag = new playFragment();
        this.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        moveToFragment(playFrag);
    }

    private void startThreads(){
        playerViewThread = new Thread(playerBoardView);
        opponentViewThread = new Thread(opponentBoardView);
        thread = new Thread(){
            @Override
            public void run(){
                try{
                    while(!isInterrupted()){
                        Thread.sleep(100);
                        runOnUiThread(new Runnable(){
                            public void run(){
                                numShots.setText("Number of Shots: "+ opponentBoard.numOfShots());
                                if(!playerBoard.isGameOver()&&!opponentBoard.isGameOver() && gameStarted) {
                                    if (playerTurn) {
                                        System.out.println("players turn");
                                        if (!playerBoardView.hasBoardTouchListener()) {
                                            playerBoardView.addBoardTouchListener(new gameActivity.BoardTouchListener());
                                        }
                                        player.nextMove();
                                    }
                                    if(!playerTurn){
                                        System.out.println("opponents turn");
                                        try{
                                            thread.sleep(800);
                                        }catch(Exception e){
                                            System.out.println(e);
                                        }
                                        if (!opponentBoardView.hasBoardTouchListener()) {
                                            opponentBoardView.addBoardTouchListener(new gameActivity.BoardTouchListener());
                                        }
                                        System.out.println("opponent making next move");
                                        opponent.nextMove();
                                    }
                                }
                            }
                        });
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        thread.start();
//        playerViewThread.start();
//        opponentViewThread.start();
    }

    /**
     * Linked to "new" button, upon being clicked board will
     * be reset and new ships will be randomly placed
     * Creates fragment prompting player to end game and start another
     */
    public void newClicked(View view){
        if(!playerBoard.isGameOver()&&!opponentBoard.isGameOver()) {
            FragmentManager fm = getFragmentManager();
            promptFragment = new MyDialogFragment();
            promptFragment.show(fm, "sample fragment");
        }
        else{
            resetGame(playerBoard, opponentBoardView);
            resetGame(opponentBoard, playerBoardView);
            gameStarted = false;
            this.setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            moveToFragment(difficultyFrag);

        }
    }

    @Override
    public void onBackPressed(){
        if(gameStarted) {
            FragmentManager fm = getFragmentManager();
            promptFragment = new MyDialogFragment();
            promptFragment.show(fm, "sample fragment");

        }
    }


    /**
     * If yes button is clicked game is restarted
     * @param view
     */
    public void yesClicked(View view){
        resetGame(playerBoard, opponentBoardView);
        resetGame(opponentBoard, playerBoardView);
        gameStarted = false;
        difficultyFrag = new difficultyFragment();
        difficultyFrag.addButtonListener(new ButtonSelectListener());
        this.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        moveToFragment(difficultyFrag);
        promptFragment.dismiss();


    }

    /**
     * If no button is clickin from view
     * @param view
     */
    public void noClicked(View view){
        promptFragment.dismiss();
    }

    /**
     * Board is reset, ships are re-placed, boardTouchListeners
     * are removed and added. onDraw is forced to be called to
     * reflect new state of game
     * @param board
     * @param boardView
     */
    private void resetGame(Board board, BoardView boardView){
        board.reset();
        boardView.removeAllBoardTouchListeners();
//        boardView.invalidate();
    }

    private void moveToFragment(Fragment fragment){
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName())/*.addToBackStack(null)*/.commit();
    }

    public SelectShipsView getSelectShipsView(){
        shipsView = (SelectShipsView) findViewById(R.id.shipsToPlace);
        shipsView.setBoard(playerBoard);
        shipsView.addShipSelectListener(new shipSelectListener());
        return shipsView;
    }

    public PlacedShipsView getPlacedShipsView(){
        placedShipsView = (PlacedShipsView) findViewById(R.id.placedShipsView);
        placedShipsView.setBoard(playerBoard);
        placedShipsView.addPlacedListener(new PlacedListener());
        return placedShipsView;
    }

    public BoardView getPlayerBoardView(){
        playerBoardView = (BoardView) findViewById(R.id.playerBoardView);
        playerBoardView.setBoard(opponentBoard, false);
        mRetainedFragment.setPlayerBoardView(playerBoardView);
//        if(player == null) {
            player = new humanPlayer(opponentBoard, playerTurn, playerBoardView);
            mRetainedFragment.setPlayer(player);
//        }
        return playerBoardView;
    }

    public BoardView getOpponentBoardView(){
        opponentBoardView = (BoardView) findViewById(R.id.opponentBoardView);
        opponentBoardView.setBoard(playerBoard, true);
        mRetainedFragment.setOpponentBoardView(opponentBoardView);
//        if(opponent == null) {
            if (strategy != null) {
                opponent = new ComputerPlayer(playerBoard, !playerTurn, opponentBoardView, strategy);
            } else {
                opponent = new humanPlayer(playerBoard, !playerTurn, opponentBoardView);
            }
            mRetainedFragment.setOpponent(opponent);
//        }
        return opponentBoardView;
    }

    public TextView getShotsView(){
        numShots = (TextView) findViewById(R.id.numShots);

        return numShots;
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

    protected void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }


    private class ButtonSelectListener implements difficultyFragment.ButtonSelectListener{

        @Override
        public void ButtonSelected(String button){

             /* modify when another strategy has been created */
            if(button.equals("Difficult")){
                strategy = new SmartStrategy(playerBoard);
                toast("Difficult strategy selected!");
            }
            if(button.equals("Easy")){
                toast("Easy strategy selected!");
                strategy = new RandomStrategy(playerBoard);
            }

            if(button.equals("Done")&& playerBoard.defaultShipsDeployed() && strategy != null){
                toast("Game started!");
                startGame();
            }
            mRetainedFragment.setStrategy(strategy);
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