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

    private Board playerBoard;
    private Board opponentBoard;
    private BoardView opponentBoardView;
    private BoardView playerBoardView;
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

    private difficultyFragment difficultyFrag;
    private playFragment playFrag;

    private class DifficultySelectListener implements difficultyFragment.DifficultySelectListener{

        @Override
        public void difficultySelected(String difficulty){
            System.out.println("The difficulty was: "+difficulty);

            playerTurn = true;
            playerBoard = new Board(10);
            playerBoard.placeShips();
            opponentBoard = new Board(10);
            opponentBoard.placeShips();
            playerBoard.addBoardChangeListener(new BoardChangeListener());
            opponentBoard.addBoardChangeListener(new BoardChangeListener());


            /* modify when another strategy has been created */
            if(difficulty.equals("Hard")){
                strategy = new RandomStrategy(playerBoard);
            }
            else{
                strategy = new RandomStrategy(playerBoard);
            }

            startGame();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("onCreate called!!***************************************************");
        setContentView(R.layout.activity_main);
        difficultyFrag = new difficultyFragment();
        difficultyFrag.addDifficultyListener(new DifficultySelectListener());

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, difficultyFrag, difficultyFrag.getClass().getSimpleName()).commit();

        opponentContent = MediaPlayer.create(findViewById(R.id.fragment_frame).getContext(), R.raw.woohoo);
        opponentSad = MediaPlayer.create(findViewById(R.id.fragment_frame).getContext(), R.raw.doh2);
        opponentAngry = MediaPlayer.create(findViewById(R.id.fragment_frame).getContext(), R.raw.aaaahh);
        gameOver = MediaPlayer.create(findViewById(R.id.fragment_frame).getContext(), R.raw.about_time);
        v = (Vibrator) findViewById(R.id.fragment_frame).getContext().getSystemService(Context.VIBRATOR_SERVICE);
        
        thread = new Thread(){
            @Override
            public void run(){
                try{
                    while(!isInterrupted()){
                        Thread.sleep(100);
                        runOnUiThread(new Runnable(){
                            public void run(){
                                numShots.setText("Number of Shots: "+ opponentBoard.numOfShots());
                                if(!playerBoard.isGameOver()&&!opponentBoard.isGameOver()) {
                                    if (playerTurn) {
                                        if (!playerBoardView.hasBoardTouchListener()) {
                                            System.out.println("Added board touch listener******************************************");
                                            playerBoardView.addBoardTouchListener(new gameActivity.BoardTouchListener());
                                        }
                                        player.nextMove();
                                    } else {
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
////
//        thread.start();
//        playerViewThread.start();
//        opponentViewThread.start();

    }
    private void startGame() {
        System.out.println("start game called!*****************");

        startThreads();

        playFrag = new playFragment();
        moveToFragment(playFrag);
    }

    private void startThreads(){
        playerViewThread = new Thread(playerBoardView);
        opponentViewThread = new Thread(opponentBoardView);
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
        board.placeShips();
        boardView.removeAllBoardTouchListeners();
        boardView.invalidate();
    }

    private void moveToFragment(Fragment fragment){
        System.out.println("Move to Fragment called!! moving to: "+fragment.getClass().getSimpleName()+"************************");
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName()).addToBackStack(null).commit();
    }


    public BoardView getPlayerBoardView(){
        System.out.println("getPlayerBoardView called!!********************");
        playerBoardView = (BoardView) findViewById(R.id.playerBoardView);
        playerBoardView.setBoard(opponentBoard, true);
        player = new humanPlayer(opponentBoard, playerTurn, playerBoardView);
        return playerBoardView;
    }

    public Board getPlayerBoard(){
        return playerBoard;
    }

    public BoardView getOpponentBoardView(){
        System.out.println("getOpponentBoardView called!!!**********************");
        opponentBoardView = (BoardView) findViewById(R.id.opponentBoardView);
        opponentBoardView.setBoard(playerBoard, false);
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


    /**
     * BoardTouchListener to remove and add when button is clicked
     */
    private class BoardTouchListener implements BoardView.BoardTouchListener {
        @Override
        public void onTouch(int x, int y) {
            System.out.println("On touch called!! player turn:"+playerTurn);
            if (playerTurn) {
                if (!player.getBoard().at(x + 1, y + 1).isHit()) {
                    System.out.println("player took a shot!*********************");
                    player.hit(x + 1, y + 1);
                }
            }else {
                if (!opponent.getBoard().at(x + 1, y + 1).isHit()) {
                    System.out.println("Computer took a shot!*****************");
                    opponent.hit(x + 1, y + 1);
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
                playerTurn = !playerTurn;
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
}
