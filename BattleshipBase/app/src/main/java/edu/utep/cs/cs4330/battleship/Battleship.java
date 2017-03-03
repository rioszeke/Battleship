package edu.utep.cs.cs4330.battleship;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by riosz on 2/10/2017.
 */

class Battleship {
    private final String name;

    private final int size;

    private final List<Place> places;

    public Battleship(String name, int size){
        this.name = name;
        this.size = size;
        places = new ArrayList<Place>(size);
    }

    /** Gets name of ship **/
    public String name(){
        return name;
    }

    /** Gets number of Place objects this ship occupies **/
    public int size(){
        return size;
    }

    /** returns place that contains X and Y coordinates of the head of the ship */
    public Place head(){
        return places.get(0);
    }

    /** returns place that contains X and Y coordinates of the tail of the ship */
    public Place tail(){
        return places.get(places.size()-1);
    }

    /** returns whether the orientation of ship is horizontal */
    public boolean isHorizontal(){
        return head().getY() == tail().getY();
    }

    /** returns whether the orientation of ship is vertical */
    public boolean isVertical(){
        return !isHorizontal();
    }

    /** returns the iterable list of Places ship is contained to */
    public Iterable<Place> places(){
        return places;
    }

    /** returns whether the ship has been sunk */
    public boolean isSunk(){
        if(places.isEmpty()){
            //ship cant be sunk if empty
            return false;
        }

        //Checks each place in list for hit
        for(Place place : places){
            if(!place.isHit()){
                return false;
            }
        }
        return true;
    }

    /** Add place to list of places
     * checks whether place already exists in list
     * @param place
     */
    public void addPlace(Place place){
        if(!places.contains(place)){
            places.add(place);
        }
        if(!place.hasShip()){
            place.placeShip(this);
        }
    }

    /** Remove specified list from list of places **/
    public void removePlace(Place place){
        places.remove(place);
    }

    /** Checks if ship contains places
     * meaning if has been placed on board
     * @return
     */
    public boolean isDeployed(){
        return !places.isEmpty();
    }

    /** provides a clean list of places **/
    public void removePlaces(){
        List<Place> copies = new ArrayList<>(places);
        for(Place p : copies){
            p.reset();
        }
    }
}
