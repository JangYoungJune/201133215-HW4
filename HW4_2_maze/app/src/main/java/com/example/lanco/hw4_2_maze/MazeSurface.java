package com.example.lanco.hw4_2_maze;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Lanco on 2016-06-02.
 */
public class MazeSurface extends SurfaceView implements SurfaceHolder.Callback {

    Canvas cacheCanvas;
    Bitmap backBuffer;
    int width, height, clientHeight;
    Paint paint;
    Context context;
    SurfaceHolder mHolder;

    //image bitmap
    private Bitmap wall;
    private Bitmap start;
    private Bitmap finish;

    // bit size about one maze block
    private int r_cell;
    private int c_cell;

    // maze size
    private int row = 15;
    private int col = 15;

    // array to save maze data
    private char[][] cell = new char[row][col];
    private int[][] cell_x = new int[row][col];
    private int[][] cell_y = new int[row][col];

    // distinct the character is first movement.
    private int first_s = 0;

    //constructor
    public MazeSurface(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MazeSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    // init function - get holder
    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    //surfaceCreated() : identify cacheCanvas and make maze using randomize Prim's algorithm
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        width = getWidth();
        height = getHeight();
        cacheCanvas = new Canvas();
        backBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        cacheCanvas.setBitmap(backBuffer);
        cacheCanvas.drawColor(Color.WHITE);

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        Resources res = getResources();
        r_cell = (int) (width / row);
        c_cell = (int) (height / col);
        wall = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.wall), r_cell, c_cell, false);
        start = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.ghost), r_cell, c_cell, false);
        finish = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.finish), r_cell, c_cell, false);

        //init all maze cell - wall
        for (int x = 0; x < col; x++)
            for (int y = 0; y < row; y++)
                cell[x][y] = 'W';

        //decide random - start point
        Point start = new Point((int) (Math.random() * row), (int) (Math.random() * col), null);
        cell[start.r][start.c] = 'S';

        // set find value about nearby start cell
        ArrayList<Point> find = new ArrayList<Point>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if ((x == 0 && y == 0) || (x != 0 && y != 0))
                    continue;
                try {
                    if (cell[start.r + x][start.c + y] == 'R')
                        continue;
                } catch (Exception e) {
                    continue;
                }
                find.add(new Point(start.r + x, start.c + y, start));
            }
        }

        Point finish = null;

        while (!find.isEmpty()) {
            // pick random cell
            Point cu = find.remove((int) (Math.random() * find.size()));
            Point op = cu.opposite();
            try {
                //if both cell and its opposite are walls
                if (cell[cu.r][cu.c] == 'W') {
                    if (cell[op.r][op.c] == 'W') {
                        //open path between cells
                        cell[cu.r][cu.c] = 'R';
                        cell[op.r][op.c] = 'R';

                        //store last cell - set finish point
                        finish = op;

                        //add nearby find cell to make a road - iterate
                        for (int x = -1; x <= 1; x++) {
                            for (int y = -1; y <= 1; y++) {
                                if (x == 0 && y == 0 || x != 0 && y != 0)
                                    continue;
                                try {
                                    if (cell[op.r + x][op.c + y] == '□')
                                        continue;
                                } catch (Exception e) {
                                    continue;
                                }
                                find.add(new Point(op.r + x, op.c + y, op));
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }

            // if loop is ended, mark end
            if (find.isEmpty())
                cell[finish.r][finish.c] = 'F';
        }
        draw();
    }

    //static class using Maze generator
    static class Point {
        Integer r;
        Integer c;
        Point parent;

        public Point(int x, int y, Point p) {
            r = x;
            c = y;
            parent = p;
        }

        public Point opposite() {
            if (this.r.compareTo(parent.r) != 0)
                return new Point(this.r + this.r.compareTo(parent.r), this.c, this);
            if (this.c.compareTo(parent.c) != 0)
                return new Point(this.r, this.c + this.c.compareTo(parent.c), this);
            return null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    // stare last, and current event action x,y value
    int lastX, lastY, currX, currY;
    // to control touch event - when someone do not touch charactor, it doesn't control.
    boolean isStart = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int action = event.getAction();
        // if the touch event is first
        if (first_s == 0) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (findCell((int) event.getX(), (int) event.getY()) == 'S') {
                        isStart = true;
                        lastX = (int) event.getX();
                        lastY = (int) event.getY();
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (!isStart) break;
                    // when touching the wall cell - initial map
                    if (findCell((int) event.getX(), (int) event.getY()) == 'W') {
                        cacheCanvas.drawColor(Color.WHITE);
                        surfaceCreated(mHolder);
                        isStart = false;
                        break;
                    }
                    // when touching the finish cell - game clear and initial map
                    if (findCell((int) event.getX(), (int) event.getY()) == 'F') {
                        cacheCanvas.drawColor(Color.WHITE);
                        surfaceCreated(mHolder);
                        Toast.makeText(getContext(), "Maze clear!", Toast.LENGTH_SHORT).show();
                        isStart = false;
                        break;
                    }

                    currX = (int) event.getX();
                    currY = (int) event.getY();
                    cacheCanvas.drawLine(lastX, lastY, currX, currY, paint);
                    lastX = currX;
                    lastY = currY;
                    break;

                case MotionEvent.ACTION_UP:
                    if (!isStart) break;
                    cacheCanvas.drawColor(Color.WHITE);
                    first_s=1;
                    isStart=false;
                    cacheCanvas.drawBitmap(start, event.getX() - (r_cell / 2), event.getY() - (c_cell / 2), paint);
                    break;

            }
        }
        if(first_s==1){
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if ((event.getX()>=currX-(r_cell/2) && event.getX()<=currX+(r_cell/2)) && (event.getY()>=currY-(c_cell/2)&&event.getY()<=currY+(c_cell/2))) {
                        isStart = true;
                        lastX = (int) event.getX();
                        lastY = (int) event.getY();
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (!isStart) break;
                    // when touching the wall cell - initial map
                    if (findCell((int) event.getX(), (int) event.getY()) == 'W') {
                        cacheCanvas.drawColor(Color.WHITE);
                        surfaceCreated(mHolder);
                        isStart = false;
                        first_s=0;
                        break;
                    }
                    // when touching the finish cell - game clear and initial map
                    if (findCell((int) event.getX(), (int) event.getY()) == 'F') {
                        cacheCanvas.drawColor(Color.WHITE);
                        surfaceCreated(mHolder);
                        Toast.makeText(getContext(), "Maze clear!", Toast.LENGTH_SHORT).show();
                        isStart = false;
                        first_s=0;
                        break;
                    }

                    currX = (int) event.getX();
                    currY = (int) event.getY();
                    cacheCanvas.drawLine(lastX, lastY, currX, currY, paint);
                    lastX = currX;
                    lastY = currY;
                    break;

                case MotionEvent.ACTION_UP:
                    if (!isStart) break;
                    cacheCanvas.drawColor(Color.WHITE);
                    first_s=1;
                    isStart=false;
                    cacheCanvas.drawBitmap(start, event.getX() - (r_cell / 2), event.getY() - (c_cell / 2), paint);
                    break;

            }
        }

        draw(); // call surface function
        return true;
    }

    public char findCell(int get_x, int get_y) {
        int x = get_x / r_cell;
        int y = get_y / c_cell;

        return cell[x][y];
    }

    protected void draw() {
        if (clientHeight == 0) {
            clientHeight = getClientHeight();
            height = clientHeight;
            backBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            cacheCanvas.setBitmap(backBuffer);
            cacheCanvas.drawColor(Color.WHITE);
        }
        Canvas canvas = null;
        try {
            canvas = mHolder.lockCanvas(null);//draw screen buffer drawn back buffer
            for (int x = 0; x < row; x++) {
                for (int y = 0; y < col; y++) {
                    cell_x[x][y] = x * r_cell;
                    cell_y[x][y] = y * c_cell;
                    switch (cell[x][y]) {
                        case 'S':
                            if (first_s == 0)
                                cacheCanvas.drawBitmap(start, x * r_cell, y * c_cell, paint);
                            break;
                        case 'F':
                            cacheCanvas.drawBitmap(finish, x * r_cell, y * c_cell, paint);
                            break;
                        case 'W':
                            cacheCanvas.drawBitmap(wall, x * r_cell, y * c_cell, paint);
                            break;
                        default:
                            break;
                    }
                }
            }

            canvas.drawBitmap(backBuffer, 0, 0, paint);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (mHolder != null) mHolder.unlockCanvasAndPost(canvas);
        }
    }

    private int getClientHeight() {
        Rect rect = new Rect();
        Window window = ((Activity) context).getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentViewTop - statusBarHeight;
        return ((Activity) context).getWindowManager().getDefaultDisplay().
                getHeight() - statusBarHeight - titleBarHeight;
    }

}
