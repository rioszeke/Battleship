package edu.utep.cs.cs4330.battleship;

import java.util.Random;

/**
 * Created by riosz on 3/3/2017.
 */

class RandomStrategy extends Strategy {
    private Board board;

    public RandomStrategy(Board board){
        super(board);
        this.board = board;
    }

    private int randomNumber(int min, int max){
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    @Override
    public Place getNextMove(){
        Place place;
        int x , y;
            do {
                x = randomNumber(1, board.size());
                y = randomNumber(1, board.size());
                place = board.at(x, y);
                System.out.println("attempting to select shot at: (" + place.getX() + ", " + place.getY() + ")");
            } while (place.isHit());
        return place;
    }
}
