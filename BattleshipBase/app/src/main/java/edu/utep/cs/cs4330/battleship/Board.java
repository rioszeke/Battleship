package edu.utep.cs.cs4330.battleship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A game board consisting of <code>size</code> * <code>size</code> places
 * where battleships can be placed. A place of the board is denoted
 * by a pair of 0-based indices (x, y), where x is a column index
 * and y is a row index. A place of the board can be shot at, resulting
 * in either a hit or miss.
 */
public class Board /*implements Runnable*/{

    /**
     * Size of this board. This board has
     * <code>size*size </code> places.
     */
    private final int size;
/** this is a comment*/
    /**
     * Number of shots taken at this Board
     */
    private int numOfShots;

    /**
     * Iterable List of place objects this
     * Board contains
     */
    private final List<Place> places;


    /**
     * List of default ships to be deployed including:
     * Aircraft carrier size 5
     * Battleship size 4
     * Frigate size 3
     * Submarine size 3
     * Minesweeper size 2
     */
    private final List<Battleship> DEFAULT_SHIPS;

    /**
     * Contains list of ships that occupy board
     */
    private List<Battleship> ships;

    /**
     * List of BoardChangeListeners currently listening to this board
     */
    private final List<BoardChangeListener> listeners;

    /**
     * Random object to place ships
     */
    private final static Random random = new Random();

    /**
     * Creates default board based on given size
     * @param size
     */
    public Board(int size){
        this.size = size;
        numOfShots = 0;
        places = new ArrayList<Place>(size * size);
        for(int x = 1; x <= size; x++){
            for(int y = 1; y <= size; y++){
                places.add(new Place(x, y, this));
            }
        }
        this.ships = new ArrayList<>();

        DEFAULT_SHIPS = new ArrayList<>();
        createDefaultShips();
        for(Battleship ship : DEFAULT_SHIPS){
            this.ships.add(ship);
        }
        listeners = new ArrayList<BoardChangeListener>();
    }


    /**
     * Resets the number of shots
     * and clears the individual places
     */
    public void reset(){
        numOfShots = 0;
        for(Place place : places){
            place.reset();
        }
    }

    /**
     * Returns Iterable list of places
     * contained by the board
     * @return Iterable<Place>
     */
    public Iterable<Place> places(){
        return places;
    }

    /**
     * Returns Iterable list of Battleships
     * currently placed on the board
     * @return Iterable<Battleship>
     */
    public Iterable<Battleship> ships(){
        return ships;
    }


    /**
     * Creates the standard ships and adds them to
     * DEFAULT_SHIPS
     */
    private void createDefaultShips(){
        DEFAULT_SHIPS.add(new Battleship("Aircraft carrier", 5));
        DEFAULT_SHIPS.add(new Battleship("Battleship", 4));
        DEFAULT_SHIPS.add(new Battleship("Frigate", 3));
        DEFAULT_SHIPS.add(new Battleship("Submarine", 3));
        DEFAULT_SHIPS.add(new Battleship("Minesweeper", 2));
    }

    /**
     * returns ship based on its name
     * @param name
     * @return Battleship
     */
    public Battleship ship(String name){
        for(Battleship ship : ships){
            if(ship.name().equals(name)){
                return ship;
            }
        }
        return null;
    }

    /**
     * Places the list of ships in a random orientation and order
     */
    public void placeShips(){
        int size  = size();
        for(Battleship ship : ships()){
            int x = 0;
            int y = 0;
            boolean dir = false;
            do{
                x = random.nextInt(size) + 1;
                y = random.nextInt(size) + 1;
                dir = random.nextBoolean();
            } while(!placeShip(ship, x, y, dir));
        }
    }

    /**
     * Attempts to place the specified ship according to
     * the given x and y coordinates and direction
     */
    public boolean placeShip(Battleship ship, int x, int y, boolean dir/*true if horizontal*/){
        int len = ship.size();
        if(dir
            && (0 < x)&&(x + len - 1 <= this.size)
            && !isPlacementOccuppied(ship, x, y, dir)){
            for(int i = x; i < x+len; i++){
                at(i, y).placeShip(ship);
            }
            return true;
        }
        if(!dir
              &&(0 < y) && (y+len-1 <= this.size)
              && !isPlacementOccuppied(ship,x, y, dir)){
            for(int i = y; i < y+len; i++){
                at(x, i).placeShip(ship);
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if potential placement of ship is already occuppied
     *
     * @param ship
     * @param x
     * @param y
     * @param dir
     * @return
     */
    private boolean isPlacementOccuppied(Battleship ship, int x, int y, boolean dir){
        int len = ship.size();
        if(dir){
            for(int i = x; i < x+len; i++){
                if(!at(i, y).isEmpty()){
                    return true;
                }
            }
        }
        else{
            for(int i = y; i < y+len; i++){
                if(!at(x,i).isEmpty()){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns place that has specified X and Y coordinates respectively
     * @param x
     * @param y
     * @return Place
     */
    public Place at(int x, int y){
        for(Place p: places){
            if(p.getX() == x && p.getY() == y){
                return p;
            }
        }
        return null;
    }


    /**
     *  Return the size of this board.
     */
    public int size() {
        return size;
    }

    /**
     * Return the number of shots made on board
     */
    public int numOfShots(){ return numOfShots;}

    /** Will check each ship if it is sunk **/
    public boolean isGameOver(){
        for(Battleship ship: ships){
            if(!ship.isSunk()){
                return false;
            }
        }
        return true;
    }

    /**
     * this is a cyclic method, if board is hit
     * place.hit() will be called which also calls this method
     * This ensures a link between board and place
     * NumShots will increase regardless if place
     * has been previously hit
     */
    public void hit(Place place){
        if(place == null){
//            System.out.println("Place is null method: hit(Place place) Class: Board");
        }
        //System.out.println("Hitting place [x: "+place.getX()+"] [y: "+place.getY()+"]");
        if(!place.isHit()){
            place.hit();
            return;
        }
        numOfShots++;
        notifyHit(place, numOfShots);

        if(!place.isEmpty()){
            if(place.ship().isSunk()){
                notifyShipSunk(place.ship());
                if(isGameOver()){
                    notifyGameOver(numOfShots);
                }
            }
        }
    }

    /**
     * adds BoardChangeListener to iterative list of Listeners
     */
    public void addBoardChangeListener(BoardChangeListener listener){
        if(!listeners.contains(listener)){
            listeners.add(listener);
        }
    }

    /**
     * Removes specified BoardChangeListener
     */
    public void removeBoardChangeListener(BoardChangeListener listener){
        listeners.remove(listener);
    }

    /**
     * Notifies BoardChangeListener that a place has been hit
     */
    private void notifyHit(Place place, int numOfShots){
        //System.out.println("I have notified the board of hit at: "+place.getX()+", "+place.getY());
        for(BoardChangeListener listener : listeners){
            listener.hit(place, numOfShots);
        }
    }

    /**
     * Notifies BoardChangeListener that a ship has been sunk
     * For extra credit sound feature will be added
     */
    private void notifyShipSunk(Battleship ship){
        for(BoardChangeListener listener : listeners){
            listener.shipSunk(ship);
        }
    }

    /**
     * Notifies BoardChangeListener
     * that the game has ended
     */
    private void notifyGameOver(int numOfShots){
        for(BoardChangeListener listener : listeners){
            listener.gameOver(numOfShots);
        }
    }

    /**
     * interface to be implemented which links this board to the listener
     */
    protected interface BoardChangeListener{

        void hit(Place place, int numOfShots);

        void gameOver(int numOfShots);

        void shipSunk(Battleship ship);
    }
}
