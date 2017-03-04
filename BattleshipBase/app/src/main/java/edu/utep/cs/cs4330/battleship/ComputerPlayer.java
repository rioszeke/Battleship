package edu.utep.cs.cs4330.battleship;

/**
 * Created by riosz on 3/3/2017.
 */

class ComputerPlayer extends Player {
    private Board opponentBoard;
    private boolean turn;
    private BoardView playerBoardView;
    private Strategy strategy;

    public ComputerPlayer(Board board, boolean turn, BoardView view){
        super(board, turn, view);
        opponentBoard = board;
        this.turn = turn;
        playerBoardView = view;
        strategy = new RandomStrategy(board);
    }

    public void setStrategy(Strategy strategy){
        this.strategy = strategy;
    }

    @Override
    public void nextMove(){
        Place nextHit = strategy.getNextMove();
        playerBoardView.notifyBoardTouch(nextHit.getX()-1, nextHit.getY()-1);
    }
}
