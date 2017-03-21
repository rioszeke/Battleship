package edu.utep.cs.cs4330.battleship;

/**
 * Place object to be contained in Board
 * Ships may be placed on place and place knows:
 * Whether it's occupied, hit, or empty
 */

class Place {
    private final int x;

    private final int y;

    private boolean isHit;

    private Battleship ship;

    private final Board board;

    public Place(int x, int y, Board board){
        this.x = x;
        this.y = y;
        this.board = board;
    }
    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public boolean isHit(){
        return isHit;
    }

    public boolean isHitShip(){
        return isHit && !isEmpty();
    }


    public void hit(){
        isHit = true;
        board.hit(this);
    }

    public boolean hasShip(){
        return ship != null;
    }

    public boolean isEmpty(){
        return ship == null;
    }

    public void placeShip(Battleship ship){
        this.ship = ship;
        ship.addPlace(this);
    }

    public Battleship ship(){
        return ship;
    }

    public void reset(){
        isHit = false;
        if(ship != null){
            ship.removePlace(this);
            ship = null;
        }
    }
}
