package edu.utep.cs.cs4330.battleship;

/**
 * Created by riosz on 3/3/2017.
 */

class Player {
    private Board opponentBoard;
    private BoardView playerView;
    private boolean allSunk;

    public Player(Board board, boolean turn, BoardView view){
        opponentBoard = board;
        allSunk = false;
        playerView = view;
    }

    public boolean hit(int x, int y){
        if(!opponentBoard.at(x, y).isHit()) {
            opponentBoard.hit(opponentBoard.at(x, y));
            return opponentBoard.at(x, y).isHitShip();
        }
        return false;
    }

    public void nextMove(){}
    public void nextMove(int x, int y){}

    public boolean getAllSunk(){
        return opponentBoard.isGameOver();
    }

    public Board getBoard(){
        return opponentBoard;
    }
}
