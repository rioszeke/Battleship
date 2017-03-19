package edu.utep.cs.cs4330.battleship;

/**
 * Created by riosz on 3/3/2017.
 */

class humanPlayer extends Player {
    private Board opponentBoard;
    private boolean turn;
    private BoardView playerView;
    private boolean allSunk;

    public humanPlayer(Board board, boolean turn, BoardView view){
        super(board, turn, view);
        opponentBoard = board;
        this.turn = turn;
        playerView = view;
        allSunk = false;
    }
}
