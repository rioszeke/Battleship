package edu.utep.cs.cs4330.battleship;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.RadialGradient;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
    private Thread boardThread;
    private Thread thread;
    private TextView numShots;
    private MyDialogFragment promptFragment;

    protected MediaPlayer opponentContent;
    protected MediaPlayer opponentSad;
    protected MediaPlayer opponentAngry;
    protected MediaPlayer gameOver;
    protected Vibrator v;

    private Player player;
    private Player opponent;
    private Boolean playerTurn;

    private Strategy strategy;
    private Battleship shipSelected;

    private difficultyFragment difficultyFrag;
    private playFragment playFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        opponentContent = MediaPlayer.create(findViewById(R.id.fragment_frame).getContext(), R.raw.woohoo);
        opponentSad = MediaPlayer.create(findViewById(R.id.fragment_frame).getContext(), R.raw.doh2);
        opponentAngry = MediaPlayer.create(findViewById(R.id.fragment_frame).getContext(), R.raw.aaaahh);
        gameOver = MediaPlayer.create(findViewById(R.id.fragment_frame).getContext(), R.raw.about_time);
        v = (Vibrator) findViewById(R.id.fragment_frame).getContext().getSystemService(Context.VIBRATOR_SERVICE);

        playerTurn = true;
        playerBoard = new Board(10);

        difficultyFrag = new difficultyFragment();
        difficultyFrag.addButtonListener(new ButtonSelectListener());

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, difficultyFrag, difficultyFrag.getClass().getSimpleName()).commit();

    }
    private void startGame() {
        System.out.println("start game called!*****************");
        opponentBoard = new Board(10);
        opponentBoard.placeShips();
        if(!playerBoard.hasBoardChangeListener()) {
            playerBoard.addBoardChangeListener(new BoardChangeListener());
        }
        if(!opponentBoard.hasBoardChangeListener()) {
            opponentBoard.addBoardChangeListener(new BoardChangeListener());
        }
        startThreads();
        gameStarted = true;
        playFrag = new playFragment();
        moveToFragment(playFrag);
    }

    private void startThreads(){
        System.out.println("Threads started!!!");
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
                                        if (!playerBoardView.hasBoardTouchListener()) {
                                            System.out.println("Added board touch listener******************************************");
                                            playerBoardView.addBoardTouchListener(new gameActivity.BoardTouchListener());
                                        }
                                        System.out.println("Player.nextMove called!!********************");
                                        player.nextMove();
                                    }
                                    if(!playerTurn){
                                        if (!opponentBoardView.hasBoardTouchListener()) {
                                            opponentBoardView.addBoardTouchListener(new gameActivity.BoardTouchListener());
                                        }
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
        playerViewThread.start();
        opponentViewThread.start();
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
            restartGame(playerBoard, opponentBoardView);
            restartGame(opponentBoard, playerBoardView);
        }
    }

    /**
     * If yes button is clicked game is restarted
     * @param view
     */
    public void yesClicked(View view){
        restartGame(playerBoard, opponentBoardView);
        restartGame(opponentBoard, playerBoardView);
        gameStarted = false;
        moveToFragment(difficultyFrag);
        promptFragment.dismiss();
    }

    /**
     * If no button is clicked, game resumes
     * fragment is dismissed from view
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
    private void restartGame(Board board, BoardView boardView){
        board.reset();
        boardView.removeAllBoardTouchListeners();
        boardView.invalidate();
    }

    private void moveToFragment(Fragment fragment){
        System.out.println("Move to Fragment called!! moving to: "+fragment.getClass().getSimpleName()+"************************");
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName()).addToBackStack(null).commit();
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
        System.out.println("getPlayerBoardView called!!********************");
        playerBoardView = (BoardView) findViewById(R.id.playerBoardView);
        playerBoardView.setBoard(opponentBoard, false);
        player = new humanPlayer(opponentBoard, playerTurn, playerBoardView);
        return playerBoardView;
    }

    public Board getPlayerBoard(){
        return playerBoard;
    }

    public BoardView getOpponentBoardView(){
        System.out.println("getOpponentBoardView called!!!**********************");
        opponentBoardView = (BoardView) findViewById(R.id.opponentBoardView);
        opponentBoardView.setBoard(playerBoard, true);
        if(strategy != null) {
            opponent = new ComputerPlayer(playerBoard, !playerTurn, opponentBoardView, strategy);
        }
        else{
            opponent = new humanPlayer(playerBoard, !playerTurn, opponentBoardView);
        }

        return opponentBoardView;
    }

    public Board getOpponentBoard(){
        return opponentBoard;
    }

    public TextView getShotsView(){
        System.out.println("getShotsView called!!*******************************");
        numShots = (TextView) findViewById(R.id.numShots);

        return numShots;
    }


    private class ButtonSelectListener implements difficultyFragment.ButtonSelectListener{

        @Override
        public void ButtonSelected(String button){

             /* modify when another strategy has been created */
            if(button.equals("Difficult")){
                strategy = new RandomStrategy(playerBoard);
            }
            if(button.equals("Easy")){
                strategy = new RandomStrategy(playerBoard);
            }

            if(button.equals("Done")&& playerBoard.defaultShipsDeployed() && strategy != null){
                startGame();
            }
        }
    }


    /**
     * BoardTouchListener to remove and add when button is clicked
     */
    private class BoardTouchListener implements BoardView.BoardTouchListener {
        @Override
        public void onTouch(int x, int y) {
            System.out.println("On touch called!! player's turn:"+playerTurn);
            if (playerTurn) {
                if (!player.getBoard().at(x + 1, y + 1).isHit()) {
                    System.out.println("player took a shot!*********************");
                    if(!player.hit(x + 1, y + 1)){
                        playerTurn = !playerTurn;
                        System.out.println("Player turn switched from: "+ !playerTurn+" to: "+playerTurn);
                    }
                    player.hit(x+1, y+1);
                }
            }
            if(!playerTurn){
                if (!opponent.getBoard().at(x + 1, y + 1).isHit()) {
                    System.out.println("Computer took a shot!*****************");
                    if(!opponent.hit(x + 1, y + 1)){
                        playerTurn = !playerTurn;
                        System.out.println("Player turn switched from: "+ !playerTurn+" to: "+playerTurn);
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
            System.out.println("hit called by: "+playerTurn+" at: "+(place.getX()-1)+", "+(place.getY()-1));
            if(place.hasShip()){
                if(!place.ship().isSunk()) {
                    if(playerTurn){
                        opponentSad.start();
                    }
                    else{
                        opponentContent.start();
                    }
                    v.vibrate(100);
                }
            }
            else{
                if(playerTurn){
                    opponentContent.start();
                }else{
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
            System.out.println("**************GAME OVER!!!!!!!!**********************");
            gameOver.start();
            v.vibrate(1000);
        }

        /**
         * Plays sound and vibrates
         * @param ship
         */
        public void shipSunk(Battleship ship){
            if(playerTurn) {
                opponentAngry.start();
            }
            else{
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
            System.out.println("Ship selected! name: "+shipSelected.name()+"******************************");
        }
    }

    protected class PlacedListener implements PlacedShipsView.PlacedListener{

        @Override
        public void onTouch(int x, int y){
            //going to reorient or replace
            if(shipSelected == null) {
                if (playerBoard.at(x + 1, y + 1).hasShip()) {
                    //if head of ship is touched reorient
                    if(playerBoard.at(x + 1, y + 1).ship().head().getX() == x + 1 && playerBoard.at(x + 1, y + 1).ship().head().getY() == y + 1) {
                        reorientShip(playerBoard.at(x + 1, y + 1).ship());
                    }else {
                        shipSelected = playerBoard.at(x+1, y+1).ship();
                    }
                }//else do nothing

            }else{//going to place ship that has been selected
                //if ship is already on the board so replace
                if(shipSelected.isDeployed()) {
                    //Battleship ship = shipSelected;
                    System.out.println("ship attempting to replace ship: " + shipSelected.name() + "************************************");
                    //Place head = ship.head();
                    Boolean wasHorizontal = shipSelected.isHorizontal();
                    shipSelected.removePlaces();
                    if (playerBoard.placeShip(shipSelected, x + 1, y + 1, wasHorizontal)) {
                        placedShipsView.invalidate();
                        shipsView.invalidate();
                        shipSelected = null;
                    }
                }else{
                    //place ship that has been selected
                    if(playerBoard.placeShip(shipSelected, x+1, y+1, true)){
                        placedShipsView.invalidate();
                        shipsView.invalidate();
                        shipSelected = null;
                    }
                }
            }
        }

        private boolean reorientShip(Battleship ship){
            System.out.println("ship attempting to reorient: " + ship.name() + "************************************");
            Place head = ship.head();
            Boolean wasHorizontal = ship.isHorizontal();
            ship.removePlaces();
            //if ship was placed successfully
            if (playerBoard.placeShip(ship, head.getX(), head.getY(), !wasHorizontal)) {
                placedShipsView.invalidate();
                shipsView.invalidate();
                return true;
            } else {//if ship was not able to be placed successfully
                playerBoard.placeShip(ship, head.getX(), head.getY(), wasHorizontal);
                return false;
            }
        }
    }




}
