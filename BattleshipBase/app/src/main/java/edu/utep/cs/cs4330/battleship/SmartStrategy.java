package edu.utep.cs.cs4330.battleship;

import java.util.Random;
import java.util.Stack;

/**
 * Created by David on 3/18/2017.
 */

public class SmartStrategy extends Strategy {
    private Board board;
    private int direction;
    private Stack<Place> hitStack;

    public SmartStrategy(Board board){
        super(board);
        this.board = board;
        direction = 0;
        hitStack = new <Place>Stack();
    }

    private int randomNumber(int min, int max){
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    @Override
    public Place getNextMove(){
        Place place = null;
        int x, y;

        while(place == null || place.isHit()) {
            while(!hitStack.empty() && (!hitStack.peek().isHitShip())){
                hitStack.pop();
            }
            if (hitStack.empty()) {
                x = randomNumber(1, board.size());
                y = randomNumber(1, board.size());
                place = board.at(x, y);
                System.out.println("attempting to select shot at: (" + place.getX() + ", " + place.getY() + ")");
                hitStack.add(place);
            }
            else{
                Place lastHit = hitStack.pop();
                Place nHit = null;
                if( direction == 0){
                    nHit = board.at(lastHit.getX(),lastHit.getY()-1);
                }
                else if( direction == 1 ){
                    nHit = board.at(lastHit.getX(),lastHit.getY()+1);
                }
                else if( direction == 2 ){
                    nHit = board.at(lastHit.getX()+1,lastHit.getY());
                }
                else if( direction == 3 ){
                    nHit = board.at(lastHit.getX()-1,lastHit.getY());
                }
                if(nHit != null && !nHit.isHit()){
                    hitStack.push(nHit);
                }
                if(direction != 3){
                    hitStack.push(lastHit);
                }
                place = nHit;
                direction = (direction + 1 )% 4;
            }
        }
        return place;
    }
}
