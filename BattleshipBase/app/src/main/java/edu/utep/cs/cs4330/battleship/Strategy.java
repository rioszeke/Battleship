package edu.utep.cs.cs4330.battleship;

/**
 * Created by riosz on 3/3/2017.
 */

class Strategy {

    private Board board;
    public Strategy(Board board){
        this.board = board;
    }

    public Place getNextMove(){
        return new Place(0, 0, board);
    }
}
