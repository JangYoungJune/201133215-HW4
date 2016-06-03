package com.example.lanco.hw4_bubble;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by Lanco on 2016-05-29.
 */
public class Bubble extends View {

    //bitmap image about bubble
    private Bitmap full;
    private Bitmap empty;

    private Paint mPaint;
    // bubble cell's data
    private boolean[] cell_state = new boolean[100]; // full or empty
    private float[] cell_x = new float[100]; // position x
    private float[] cell_y = new float[100]; // position y

    public Bubble(Context context) {
        super(context);
        init();
    }
    public Bubble(Context context, AttributeSet a) {
        super(context, a);
        init();
    }
    // make a paint and init the cell values;
    public void init()
    {
        mPaint = new Paint();
        Resources res = getResources();
        full = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.air_full),200,200,false);
        empty = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.air_empty),200,200,false);

        float x = -200;
        float y = 0;

        // init bubble cell - make grid space using for loop.
        for(int i=0;i<77;i++){
            x=x+200;
            if(x==1400){
                x = 0;
                y = y + 200;
            }
            cell_state[i]=false;
            cell_x[i] = x;
            cell_y[i] = y;
        }
    }

    //draw image by canvas using data
    protected void onDraw(Canvas canvas){
        Bitmap temp;
        for(int i=0;i<77;i++){

            if(cell_state[i]==false)
                temp = full; // false
            else
                temp=empty;
            canvas.drawBitmap(temp,cell_x[i],cell_y[i],mPaint);
        }
    }

    //when touching event occur, get x, y values and set data and call draw() using invalidate()
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            float x = event.getX();
            float y = event.getY();

            x = 200 * (int)(x/200);
            y = 200 * (int)(y/200);

            for(int i=0;i<77;i++){
                if(cell_x[i]==x && cell_y[i]==y){
                    if(cell_state[i]==false){
                        cell_state[i]=true;
                        invalidate( );//call draw()
                    }
                }
            }
        }
        return true;
    }

}
