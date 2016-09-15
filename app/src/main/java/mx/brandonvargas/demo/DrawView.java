package mx.brandonvargas.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by brandon on 8/09/16.
 */
public class DrawView extends View {

    Paint paint;
    float leftx = 0;
    float topy = 0;
    float rightx = 0;
    float bottomy = 0;
    public DrawView(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(leftx, topy, rightx, bottomy, paint);
    }

    public void newRectangle(float leftx,float topy,float rightx, float bottomy){
        this.leftx = leftx;
        this.topy = topy;
        this.rightx = rightx;
        this.bottomy = bottomy;
        invalidate();
    }

}
