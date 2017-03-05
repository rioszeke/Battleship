package edu.utep.cs.cs4330.battleship;

/**
 * Created by riosz on 3/3/2017.
 */

class ComputerPlayer extends Player {
    private Board opponentBoard;
    private boolean turn;
    private BoardView playerBoardView;
    private Strategy strategy;

    public ComputerPlayer(Board board, boolean turn, BoardView view, Strategy strategy){
        super(board, turn, view);
        opponentBoard = board;
        this.turn = turn;
        playerBoardView = view;
        this.strategy = strategy;
    }

    public ComputerPlayer(Board board, boolean turn, BoardView view){
        super(board, turn, view);
        opponentBoard = board;
        this.turn = turn;
        playerBoardView = view;
    }

    public void setStrategy(Strategy strategy){
        this.strategy = strategy;
    }

    @Override
    public void nextMove(){
        Place nextHit = strategy.getNextMove();
        if(playerBoardView == null){
            System.out.println("According to player computer playerBoardView is null********************************************************");
        }
        playerBoardView.notifyBoardTouch(nextHit.getX()-1, nextHit.getY()-1);
    }
}
