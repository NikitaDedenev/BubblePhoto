package com.example.bubblephoto;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {
    private Path drawPath;
    private Paint drawPaint;
    private boolean isDrawing = false;
    private float brushSize = 30;
    private OnDrawingListener drawingListener;

    public interface OnDrawingListener {
        void onDrawingStart(float x, float y);
        void onDrawing(float x, float y);
        void onDrawingEnd();
    }

    public DrawingView(Context context) {
        super(context);
        setupDrawing();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setBrushSize(float size) {
        brushSize = size;
        drawPaint.setStrokeWidth(size);
        invalidate();
    }

    public void setDrawingListener(OnDrawingListener listener) {
        this.drawingListener = listener;
    }

    public void setDrawingEnabled(boolean enabled) {
        isDrawing = enabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isDrawing) return false;

        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                if (drawingListener != null) {
                    drawingListener.onDrawingStart(touchX, touchY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                if (drawingListener != null) {
                    drawingListener.onDrawing(touchX, touchY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (drawingListener != null) {
                    drawingListener.onDrawingEnd();
                }
                drawPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isDrawing) {
            canvas.drawPath(drawPath, drawPaint);
        }
    }
}