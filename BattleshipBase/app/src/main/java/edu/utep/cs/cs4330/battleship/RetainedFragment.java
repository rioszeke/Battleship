package edu.utep.cs.cs4330.battleship;


import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by David on 3/19/2017.
 */

public class RetainedFragment extends Fragment {
    private Board playerBoard;
    private Board opponentBoard;
    private Player player;
    private Player opponent;
    private Boolean playerTurn;
    private Strategy strategy;
    private BoardView playerBoardView;
    private BoardView opponentBoardView;
    private Boolean sound;

    public Boolean getSound() {
        return sound;
    }

    public void setSound(Boolean sound) {
        this.sound = sound;
    }

    public BoardView getPlayerBoardView() {
        return playerBoardView;
    }

    public void setPlayerBoardView(BoardView playerBoardView) {
        this.playerBoardView = playerBoardView;
    }

    public BoardView getOpponentBoardView() {
        return opponentBoardView;
    }

    public void setOpponentBoardView(BoardView opponentBoardView) {
        this.opponentBoardView = opponentBoardView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setPlayerBoard(Board playerBoard){
        this.playerBoard = playerBoard;
    }

    public Board getPlayerBoard() {
        return playerBoard;
    }

    public Board getOpponentBoard() {
        return opponentBoard;
    }

    public void setOpponentBoard(Board opponentBoard) {
        this.opponentBoard = opponentBoard;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public Boolean getPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(Boolean playerTurn) {
        this.playerTurn = playerTurn;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
}
