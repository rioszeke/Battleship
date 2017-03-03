package edu.utep.cs.cs4330.battleship;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * A special view class to display a battleship board as a2D grid.
 *
 * @see Board
 */
public class BoardView extends View implements Runnable{

    /** Listeners to be notified upon board touches. */
    private final List<BoardTouchListener> listeners = new ArrayList<>();

    /** Board background color. */
    private final int boardColor = Color.rgb(102, 163, 255);

    /**
     * Sounds to play during various states of the game
     */
//    protected MediaPlayer placeHit = MediaPlayer.create(this.getContext(), R.raw.woohoo);
//
//    protected MediaPlayer shipHit = MediaPlayer.create(this.getContext(), R.raw.doh2);
//
//    protected MediaPlayer shipSunk = MediaPlayer.create(this.getContext(), R.raw.aaaahh);
//
//    protected MediaPlayer gameOver = MediaPlayer.create(this.getContext(), R.raw.about_time);

    /** Vibrates phone, requires app permission */
    protected Vibrator v = (Vibrator) this.getContext().getSystemService(Context.VIBRATOR_SERVICE);


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

    /** Board to be displayed by this view. */
    private Board board;

    /** Size of the board. */
    private int boardSize;


    /** Callback interface to listen for board touches. */
    public interface BoardTouchListener {

        /**
         * Called when a place of the board is touched.
         * The coordinate of the touched place is provided.
         *
         * @param x 0-based column index of the touched place
         * @param y 0-based row index of the touched place
         */
        void onTouch(int x, int y, Board board);
    }

    /** Create a new board view to be run in the given context. */
    public BoardView(Context context) {
        super(context);
    }

    /** Create a new board view with the given attribute set. */
    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Create a new board view with the given attribute set and style. */
    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /** Set the board to to be displayed by this view. */
    public void setBoard(Board board) {
        this.board = board;
        this.boardSize = board.size();
        //board.addBoardChangeListener(new MainActivity.BoardChangeListener());
        //board.placeShips();
    }

    /**
     * Debugging method to print board and status of places
     */
    private void printBoard(){
        int i = 0;
        for(Place place: board.places()){
            String hit = "";
            if(place.isHitShip()){
                hit = "SHIP";
            }
            else{
                if(place.isHit()) {
                    hit = "!hit";
                }
            }
            System.out.print("["+place.getX()+","+place.getY()+":"+hit+"]");
            i++;
            if(i == board.size()){
                i = 0;
                System.out.println("");
            }
        }
    }

    /**
     * Overridden here to detect a board touch. When the board is
     * touched, the corresponding place is identified,
     * and registered listeners are notified.
     *
     * @see BoardTouchListener
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                int xy = locatePlace(event.getX(), event.getY());
                if (xy >= 0 && xy/100 < boardSize && xy%100 < boardSize) {
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

    /**
     * Class is made runnable so that it
     * can run separately on thread
     */
    @Override
    public void run(){
        while(true){
            try{
                Thread.sleep(1);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            //this.invalidate();
        }
    }

    /** Overridden here to draw a 2-D representation of the board. */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGrid(canvas);
        if(board != null) {
            drawPlaces(canvas);
        }
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
        for(Place place: board.places()){
            RectF rect = createRectF(place.getX()-1, place.getY()-1);
            if(place.isHitShip()){
                paint.setColor(Color.RED);
                canvas.drawRoundRect(rect, 10, 10, paint);
            }
            else{
                if(place.isHit()){
                    paint.setColor(Color.BLUE);
                    canvas.drawRoundRect(rect, 10, 10, paint);
                }
                else{
                    paint.setColor(Color.GREEN);
                    canvas.drawRoundRect(rect, 10, 10, paint);
                }
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
    public void addBoardTouchListener(BoardTouchListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /** Unregister the given listener. */
    public void removeBoardTouchListener(BoardTouchListener listener) {
        listeners.remove(listener);
    }

    public void removeAllBoardTouchListeners(){
        listeners.clear();
    }

    /** Notify all registered listeners. */
    protected void notifyBoardTouch(int x, int y,Board board) {
        for (BoardTouchListener listener: listeners) {
            listener.onTouch(x, y, board);
        }
    }

//    /**
//     * Nested class implements BoardChangeListener interface
//     * Observes and reports changes to the board
//     */
//    private class BoardChangeListener implements Board.BoardChangeListener{
//
//        /** will force onDraw to be called again with
//         * invalidate().
//         * @param place
//         * @param numOfShots
//         */
//        public void hit(Place place, int numOfShots){
//            if(place.hasShip()){
//                if(!place.ship().isSunk()) {
//                    shipHit.start();
//                    v.vibrate(100);
//                }
//            }
//            else{
//
//                placeHit.start();
//            }
//            invalidate();
//        }
//
//        /**
//         * Clears listeners, plays sound and vibrates
//         * @param numOfShots
//         */
//        public void gameOver(int numOfShots){
//            listeners.clear();
//            gameOver.start();
//            v.vibrate(1000);
//        }
//
//        /**
//         * Plays sound and vibrates
//         * @param ship
//         */
//        public void shipSunk(Battleship ship){
//            shipSunk.start();
//            v.vibrate(500);
//        }
//    }
}
