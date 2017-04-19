package edu.utep.cs.cs4330.battleship;

import java.io.IOException;
import java.util.logging.Handler;

import static android.R.attr.port;
import static edu.utep.cs.cs4330.battleship.R.id.playerBoardView;

/**
 * Created by riosz on 4/16/2017.
 */

public class BluetoothPlayer extends Player {
    private Board opponentBoard;
    private boolean turn;
    private BoardView playerBoardView;

    public BluetoothPlayer(Board board, boolean turn, BoardView view){
        super(board, turn, view);
        opponentBoard = board;
        this.turn = turn;
        playerBoardView = view;
    }


    /** do something with bluetooth messages here **/
    @Override
    public void nextMove(){
//        Place nextHit = strategy.getNextMove();
//        System.out.println("Computer generated shot ("+(nextHit.getX()-1)+", "+(nextHit.getY()-1));
//        playerBoardView.notifyBoardTouch(nextHit.getX()-1, nextHit.getY()-1);
    }
    public void nextMove(int x, int y){
        playerBoardView.notifyBoardTouch(x,y);
    }
}
