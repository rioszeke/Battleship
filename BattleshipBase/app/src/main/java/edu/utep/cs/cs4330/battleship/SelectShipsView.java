package edu.utep.cs.cs4330.battleship;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by riosz on 3/5/2017.
 */

public class SelectShipsView extends View {
    /** Listeners to be notified upon board touches. */
    private final List<ShipSelectListener> listeners = new ArrayList<>();

    /** Board background color. */
    private final int boardColor = Color.rgb(102, 163, 255);

    private Board board;

    private Battleship selectedShip;

    /** Board background paint. */
    private final Paint boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    {
        boardPaint.setColor(boardColor);
    }

    /** Board grid line color. */
    private final int boardLineColor = Color.WHITE;

    /** Board grid line paint. */
    private final Paint boardLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    {
        boardLinePaint.setColor(boardLineColor);
        boardLinePaint.setStrokeWidth(2);
    }

    /** Size of the board. */
    private final int boardSize = 5;

    /** Callback interface to listen for board touches. */
    public interface ShipSelectListener {

        void onSelect(Battleship ship);
    }

    /** Create a new board view to be run in the given context. */
    public SelectShipsView(Context context) {
        super(context);
    }

    /** Create a new board view with the given attribute set. */
    public SelectShipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Create a new board view with the given attribute set and style. */
    public SelectShipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setBoard(Board board){
        this.board = board;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                int xy = locatePlace(event.getX(), event.getY());
                if (xy >= 0 && xy / 100 < boardSize && xy % 100 < boardSize) {
                    System.out.println("Place touched at: ("+xy/100+", "+xy%100+")**********************");
                    if(xy/100 < getShip(xy%100).size()){
                        notifyShipSelect(getShip(xy%100));
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    private Battleship getShip(int i){
        int k = 0;
        for(Battleship battleship : board.getDefaultShips()){
            if(k == i){
                return battleship;
            }
            k++;
        }
        return null;
    }
    /** Overridden here to draw a 2-D representation of the board. */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGrid(canvas);
        drawPlaces(canvas);
    }

    /**
     * Draw all the places of the board.
     * Blue = miss
     * Red = hit
     * Green = !hit
     */
    private void drawPlaces(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        int i = 0;
        for(Battleship battleship : board.getDefaultShips()) {
            if(!battleship.isDeployed()) {
                if(selectedShip != null) {
                    if (!battleship.name().equals(selectedShip.name())) {
                        for (int j = 0; j < battleship.size(); j++) {
                            RectF rect = createRectF(j, i);
                            paint.setColor(Color.RED);
                            canvas.drawRoundRect(rect, 10, 10, paint);
                        }
                    } else {
                        for (int j = 0; j < battleship.size(); j++) {
                            RectF rect = createRectF(j, i);
                            paint.setColor(Color.GREEN);
                            canvas.drawRoundRect(rect, 10, 10, paint);
                        }
                    }
                }else{
                    for (int j = 0; j < battleship.size(); j++) {
                        RectF rect = createRectF(j, i);
                        paint.setColor(Color.RED);
                        canvas.drawRoundRect(rect, 10, 10, paint);
                    }
                }
            }
            i++;
        }
    }

    /**
     * Creates RectF object to fit size
     * and affected coordinates of
     * affected square.
     * @param x
     * @param y
     * @return
     */
    private RectF createRectF(int x, int y){
        if(x*lineGap() == maxCoord() || y*lineGap() == maxCoord()){
            return null;
        }
        float left = x * lineGap()+5;
        float top = y * lineGap()+5;
        float right = left + (lineGap()-10);
        float bottom = top + (lineGap()-10);
        return new RectF(left, top, right, bottom);
    }

    /** Draw horizontal and vertical lines. */
    private void drawGrid(Canvas canvas) {
        final float maxCoord = maxCoord();
        final float placeSize = lineGap();
        int k = 0;
        for(Battleship battleship : board.getDefaultShips()){
            System.out.println("drawing rectangle from (0, "+(k*placeSize)+") -> ("+((battleship.size()-1)*placeSize)+", "+(k*placeSize)+")");
            canvas.drawRect(0, k*placeSize, battleship.size() * placeSize, (k+1)*placeSize, boardPaint);
            k++;
        }
//        for (int i = 0; i < numOfLines(); i++) {
//            float xy = i * placeSize;
//            canvas.drawLine(0, xy, maxCoord, xy, boardLinePaint); // horizontal line
//            canvas.drawLine(xy, 0, xy, maxCoord, boardLinePaint); // vertical line
//        }
    }

    /** Calculate the gap between two horizontal/vertical lines. */
    private float lineGap() {
        return Math.min(getMeasuredWidth(), getMeasuredHeight()) / (float) boardSize;
    }

    /** Calculate the number of horizontal/vertical lines. */
    private int numOfLines() {
        return boardSize + 1;
    }

    /** Calculate the maximum screen coordinate. */
    protected float maxCoord() {
        return lineGap() * (numOfLines() - 1);
    }

    /**
     * Given screen coordinates, locate the corresponding place in the board
     * and return its coordinates; return -1 if the screen coordinates
     * don't correspond to any place in the board.
     * The returned coordinates are encoded as <code>x*100 + y</code>.
     */
    private int locatePlace(float x, float y) {
        if (x <= maxCoord() && y <= maxCoord()) {
            final float placeSize = lineGap();
            int ix = (int) (x / placeSize);
            int iy = (int) (y / placeSize);
            return ix * 100 + iy;
        }
        return -1;
    }
    /** Register the given listener. */
    public void addShipSelectListener(ShipSelectListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public boolean hasShipSelectListener(){
        return !listeners.isEmpty();
    }

    /** Unregister the given listener. */
    public void removeShipSelectListener(ShipSelectListener listener) {
        listeners.remove(listener);
    }

    public void removeAllShipPlaceListeners(){
        listeners.clear();
    }


    /** Notify all registered listeners. */
    protected void notifyShipSelect(Battleship ship) {
        for (ShipSelectListener listener: listeners) {
            listener.onSelect(ship);
        }
        selectedShip = ship;
        invalidate();
    }
}
