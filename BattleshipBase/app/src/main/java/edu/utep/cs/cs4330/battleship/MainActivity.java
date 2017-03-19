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

public class MainActivity extends AppCompatActivity {


    private Board board;
    private BoardView boardView;
    private Thread boardViewThread;
    private Thread boardThread;
    private Thread thread;
    private TextView numShots;
    private MyDialogFragment promptFragment;

    protected MediaPlayer placeHit;

    protected MediaPlayer shipHit;

    protected MediaPlayer shipSunk;

    protected MediaPlayer gameOver;

    protected Vibrator v;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
//        numShots = (TextView)findViewById(R.id.numShots);
//
//        board = new Board(10);
//        board.placeShips();
//        board.addBoardChangeListener(new BoardChangeListener());
//
//        boardView = (BoardView) findViewById(R.id.boardView);
//        boardView.setBoard(board);
//        boardView.addBoardTouchListener(new BoardTouchListener());
//
//        placeHit = MediaPlayer.create(boardView.getContext(), R.raw.woohoo);
//        shipHit = MediaPlayer.create(boardView.getContext(), R.raw.doh2);
//        shipSunk = MediaPlayer.create(boardView.getContext(), R.raw.aaaahh);
//        gameOver = MediaPlayer.create(boardView.getContext(), R.raw.about_time);
//        v = (Vibrator) boardView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
//
//
//        boardViewThread = new Thread(boardView);
//
//        thread = new Thread(){
//            @Override
//            public void run(){
//                try{
//                    while(!isInterrupted()){
//                        Thread.sleep(100);
//                        runOnUiThread(new Runnable(){
//                            public void run(){
//                                numShots.setText("Number of Shots: "+ board.numOfShots());
//                            }
//                        });
//                    }
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
//            }
//        };
//        thread.start();
//        boardViewThread.start();
    }

    /**
     * Linked to "new" button, upon being clicked board will
     * be reset and new ships will be randomly placed
     * Creates fragment prompting player to end game and start another
     */
    public void newClicked(View view){
        if(!board.isGameOver()) {
            FragmentManager fm = getFragmentManager();
            promptFragment = new MyDialogFragment();
            promptFragment.show(fm, "sample fragment");
        }
        else{
            restartGame(board, boardView);
        }
    }

    /**
     * If yes button is clicked game is restarted
     * @param view
     */
    public void yesClicked(View view){
        restartGame(board, boardView);
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
        boardView.addBoardTouchListener(new BoardTouchListener());
        boardView.invalidate();
    }

    /**
     * BoardTouchListener to remove and add when button is clicked
     */
    private class BoardTouchListener implements BoardView.BoardTouchListener {
        @Override
        public void onTouch(int x, int y) {
            //toast(String.format("Touched: %d, %d", x, y));
            if(!board.at(x+1, y+1).isHit()) {
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
            boardView.invalidate();
            System.out.print("Board view invalidated**************************************************************************************");
        }

        /**
         * Clears listeners, plays sound and vibrates
         * @param numOfShots
         */
        public void gameOver(int numOfShots){
            boardView.removeAllBoardTouchListeners();
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
