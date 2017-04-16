package edu.utep.cs.cs4330.battleship;

import java.io.IOException;
import java.util.logging.Handler;

import static android.R.attr.port;

/**
 * Created by riosz on 4/16/2017.
 */

public class BluetoothPlayer extends Player {
    private Board opponentBoard;
    private boolean turn;
    private BoardView playerView;
    private boolean allSunk;

    public BluetoothPlayer(Board board, boolean turn, BoardView view){
        super(board, turn, view);
        opponentBoard = board;
        this.turn = turn;
        playerView = view;
        allSunk = false;
    }

    /** connect to bluetooth message server here **/
    public void connectToBTServer(){
        new Thread(new Runnable(){
            public void run() {
                if (turn) {
                    try {
//                    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
//                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
//            handler.post(() -> showToast(socket != null ? "Connected." : "Failed to connect!"));
            }}).start();
    }

    /** do something with bluetooth messages here **/
    @Override
    public void nextMove(){
        new Thread(new Runnable() {
            public void run() {
                while(turn){
                    //do something to receive messsage
                }
            }
        }).start();
    }
}
