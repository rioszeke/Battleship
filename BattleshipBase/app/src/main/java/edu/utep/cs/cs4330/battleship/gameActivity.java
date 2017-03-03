package edu.utep.cs.cs4330.battleship;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.RadialGradient;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
    private Boolean turn;

    protected MediaPlayer placeHit;
    protected MediaPlayer shipHit;
    protected MediaPlayer shipSunk;
    protected MediaPlayer gameOver;
    protected Vibrator v;

    private Player player;
    private Player opponent;
    private Boolean playerTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerTurn = true;

        initializeBoard(playerBoard);
        initializeBoard(opponentBoard);

        numShots = (TextView)findViewById(R.id.numShots);
        opponentBoardView = (BoardView) findViewById(R.id.opponentBoardView);
        playerBoardView = (BoardView) findViewById(R.id.playerBoardView);

        //initializeBoardView(playerBoard, opponentBoardView);
        //initializeBoardView(opponentBoard, playerBoardView);
        opponentBoardView.setBoard(playerBoard);
        playerBoardView.setBoard(opponentBoard);

        placeHit = MediaPlayer.create(findViewById(R.id.activity_main).getContext(), R.raw.woohoo);
        shipHit = MediaPlayer.create(findViewById(R.id.activity_main).getContext(), R.raw.doh2);
        shipSunk = MediaPlayer.create(findViewById(R.id.activity_main).getContext(), R.raw.aaaahh);
        gameOver = MediaPlayer.create(findViewById(R.id.activity_main).getContext(), R.raw.about_time);
        v = (Vibrator) findViewById(R.id.activity_main).getContext().getSystemService(Context.VIBRATOR_SERVICE);

        player = new humanPlayer(opponentBoard, playerTurn, playerBoardView);
        opponent = new ComputerPlayer(playerBoard, !playerTurn, opponentBoardView);

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
                                if(playerTurn){
                                    if(!playerBoardView.hasBoardTouchListener()){
                                        playerBoardView.addBoardTouchListener(new gameActivity.BoardTouchListener());
                                    }
                                    player.nextMove();
                                }
                                else{
                                    if(!opponentBoardView.hasBoardTouchListener()){
                                        opponentBoardView.addBoardTouchListener(new gameActivity.BoardTouchListener());
                                    }
                                    opponent.nextMove();
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

    private void initializeBoard(Board board){
        board = new Board(10);
        board.addBoardChangeListener(new gameActivity.BoardChangeListener());
    }

    private void initializeBoardView(Board board, BoardView view){
        view.setBoard(board);
        view.addBoardTouchListener(new gameActivity.BoardTouchListener());
    }


    /**
     * BoardTouchListener to remove and add when button is clicked
     */
    private class BoardTouchListener implements BoardView.BoardTouchListener {
        @Override
        public void onTouch(int x, int y) {
            if (playerTurn) {
                if (!player.getBoard().at(x + 1, y + 1).isHit()) {
                    player.hit(x + 1, y + 1);
                } else {
                    if (!opponent.getBoard().at(x + 1, y + 1).isHit()) {
                        opponent.hit(x + 1, y + 1);
                    }
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
                    shipHit.start();
                    v.vibrate(100);
                }
            }
            else{
                playerTurn = !playerTurn;
                placeHit.start();
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
            gameOver.start();
            v.vibrate(1000);
        }

        /**
         * Plays sound and vibrates
         * @param ship
         */
        public void shipSunk(Battleship ship){
            shipSunk.start();
            v.vibrate(500);
        }
    }
}
