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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        numShots = (TextView)findViewById(R.id.numShots);

        initializeBoard(playerBoard);
        initializeBoard(opponentBoard);

        opponentBoardView = (BoardView) findViewById(R.id.opponentBoardView);
        playerBoardView = (BoardView) findViewById(R.id.playerBoardView);

        opponentBoardView.setBoard(playerBoard);
        playerBoardView.setBoard(opponentBoard);

        placeHit = MediaPlayer.create(findViewById(R.id.activity_main).getContext(), R.raw.woohoo);
        shipHit = MediaPlayer.create(findViewById(R.id.activity_main).getContext(), R.raw.doh2);
        shipSunk = MediaPlayer.create(findViewById(R.id.activity_main).getContext(), R.raw.aaaahh);
        gameOver = MediaPlayer.create(findViewById(R.id.activity_main).getContext(), R.raw.about_time);
        v = (Vibrator) findViewById(R.id.activity_main).getContext().getSystemService(Context.VIBRATOR_SERVICE);

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


    /**
     * BoardTouchListener to remove and add when button is clicked
     */
    private class BoardTouchListener implements BoardView.BoardTouchListener {
        @Override
        public void onTouch(int x, int y, Player player) {
            if(!player.getBoard().at(x+1, y+1).isHit()) {
                board.hit(board.at(x + 1, y + 1));
                //boardView.invalidate();
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
