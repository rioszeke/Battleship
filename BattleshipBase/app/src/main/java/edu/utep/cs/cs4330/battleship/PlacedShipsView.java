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

class PlacedShipsView extends View{

    /** Callback interface to listen for board touches. */
    public interface PlacedListener {

        /**
         * Called when a place of the board is touched.
         * The coordinate of the touched place is provided.
         *
         * @param x 0-based column index of the touched place
         * @param y 0-based row index of the touched place
         */
        void onTouch(int x, int y);
    }

    /** Listeners to be notified upon board touches. */
    private final List<PlacedListener> listeners = new ArrayList<>();

    /** Board background color. */
    private final int boardColor = Color.rgb(102, 163, 255);

    private Board board;

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
    private final int boardSize = 10;

    /** Create a new board view to be run in the given context. */
    public PlacedShipsView(Context context) {
        super(context);
    }

    /** Create a new board view with the given attribute set. */
    public PlacedShipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Create a new board view with the given attribute set and style. */
    public PlacedShipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBoard(Board board){
        this.board = board;
    }

    /**
     * Overridden here to detect a board touch. When the board is
     * touched, the corresponding place is identified,
     * and registered listeners are notified.
     *
     * @see PlacedListener
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                int xy = locatePlace(event.getX(), event.getY());
                if (xy >= 0) {
                    notifyBoardTouch(xy / 100, xy % 100);
                }
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    /** Overridden here to draw a 2-D representation of the board. */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGrid(canvas);
        drawPlaces(canvas);
    }

    private void drawPlaces(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        for (Place place : board.places()) {
            RectF rect = createRectF(place.getX() - 1, place.getY() - 1);
            if (place.hasShip()) {
                paint.setColor(Color.RED);
                canvas.drawRoundRect(rect, 10, 10, paint);
            }
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
        canvas.drawRect(0, 0, maxCoord, maxCoord, boardPaint);
        for (int i = 0; i < numOfLines(); i++) {
            float xy = i * placeSize;
            canvas.drawLine(0, xy, maxCoord, xy, boardLinePaint); // horizontal line
            canvas.drawLine(xy, 0, xy, maxCoord, boardLinePaint); // vertical line
        }
    }

    /** Calculate the gap between two horizontal/vertical lines. */
    protected float lineGap() {
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
    public void addPlacedListener(PlacedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /** Unregister the given listener. */
    public void removeBoardTouchListener(PlacedListener listener) {
        listeners.remove(listener);
    }

    /** Notify all registered listeners. */
    private void notifyBoardTouch(int x, int y) {
        for (PlacedListener listener: listeners) {
            listener.onTouch(x, y);
        }
    }
}
