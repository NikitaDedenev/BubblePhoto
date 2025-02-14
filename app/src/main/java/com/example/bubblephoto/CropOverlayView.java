package com.example.bubblephoto;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CropOverlayView extends View {
    private RectF cropRect;
    private Paint borderPaint;
    private Paint overlayPaint;
    private Paint cornerPaint;
    private int selectedCorner = -1;
    private PointF[] corners = new PointF[4];
    private OnCropChangeListener cropChangeListener;
    private static final float TOUCH_THRESHOLD = 60f;
    private float cornerSize = 50f;
    private boolean isDragging = false;
    private float lastTouchX;
    private float lastTouchY;

    private void init() {
        cropRect = new RectF();

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setColor(Color.WHITE);

        overlayPaint = new Paint();
        overlayPaint.setStyle(Paint.Style.FILL);
        overlayPaint.setColor(Color.TRANSPARENT);

        cornerPaint = new Paint();
        cornerPaint.setStyle(Paint.Style.FILL);
        cornerPaint.setColor(Color.WHITE);

        for (int i = 0; i < 4; i++) {
            corners[i] = new PointF();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        resetCropRect();
    }

    public void resetCropRect() {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        float padding = Math.min(getWidth(), getHeight()) * 0.1f;
        cropRect.set(
                padding,
                padding,
                getWidth() - padding,
                getHeight() - padding
        );
        updateCornerPositions();
        invalidate();
    }



    public interface OnCropChangeListener {
        void onCropChange(RectF rect);
    }

    public CropOverlayView(Context context) {
        super(context);
        init();
    }

    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }



    private void updateCornerPositions() {
        corners[0].set(cropRect.left, cropRect.top);
        corners[1].set(cropRect.right, cropRect.top);
        corners[2].set(cropRect.right, cropRect.bottom);
        corners[3].set(cropRect.left, cropRect.bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Path path = new Path();
        path.addRect(cropRect, Path.Direction.CW);
        path.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
        canvas.drawPath(path, overlayPaint);

        float cellWidth = cropRect.width() / 3;
        float cellHeight = cropRect.height() / 3;

        for (int i = 1; i < 3; i++) {
            float x = cropRect.left + cellWidth * i;
            canvas.drawLine(x, cropRect.top, x, cropRect.bottom, borderPaint);
        }

        for (int i = 1; i < 3; i++) {
            float y = cropRect.top + cellHeight * i;
            canvas.drawLine(cropRect.left, y, cropRect.right, y, borderPaint);
        }

        canvas.drawRect(cropRect, borderPaint);

        for (PointF corner : corners) {
            canvas.drawCircle(corner.x, corner.y, cornerSize/2, cornerPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                selectedCorner = findSelectedCorner(x, y);
                if (selectedCorner != -1) {
                    return true;
                }
                if (cropRect.contains(x, y)) {
                    isDragging = true;
                    lastTouchX = x;
                    lastTouchY = y;
                    return true;
                }
                return false;

            case MotionEvent.ACTION_MOVE:
                if (selectedCorner != -1) {
                    moveCorner(selectedCorner, x, y);
                    if (cropChangeListener != null) {
                        cropChangeListener.onCropChange(cropRect);
                    }
                    invalidate();
                    return true;
                } else if (isDragging) {
                    // Перемещаем всю рамку
                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;

                    // Проверяем границы
                    if (cropRect.left + dx >= 0 && cropRect.right + dx <= getWidth()) {
                        cropRect.left += dx;
                        cropRect.right += dx;
                    }
                    if (cropRect.top + dy >= 0 && cropRect.bottom + dy <= getHeight()) {
                        cropRect.top += dy;
                        cropRect.bottom += dy;
                    }

                    updateCornerPositions();
                    lastTouchX = x;
                    lastTouchY = y;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                selectedCorner = -1;
                isDragging = false;
                break;
        }

        return super.onTouchEvent(event);
    }

    private int findSelectedCorner(float x, float y) {
        for (int i = 0; i < corners.length; i++) {
            if (Math.hypot(x - corners[i].x, y - corners[i].y) < TOUCH_THRESHOLD) {
                return i;
            }
        }
        return -1;
    }

    private void moveCorner(int corner, float x, float y) {
        x = Math.max(0, Math.min(x, getWidth()));
        y = Math.max(0, Math.min(y, getHeight()));

        switch (corner) {
            case 0:
                cropRect.left = Math.min(x, cropRect.right - cornerSize);
                cropRect.top = Math.min(y, cropRect.bottom - cornerSize);
                break;
            case 1:
                cropRect.right = Math.max(x, cropRect.left + cornerSize);
                cropRect.top = Math.min(y, cropRect.bottom - cornerSize);
                break;
            case 2:
                cropRect.right = Math.max(x, cropRect.left + cornerSize);
                cropRect.bottom = Math.max(y, cropRect.top + cornerSize);
                break;
            case 3:
                cropRect.left = Math.min(x, cropRect.right - cornerSize);
                cropRect.bottom = Math.max(y, cropRect.top + cornerSize);
                break;
        }
        updateCornerPositions();
    }

    public RectF getCropRect() {
        return new RectF(cropRect);
    }

    public void setOnCropChangeListener(OnCropChangeListener listener) {
        this.cropChangeListener = listener;
    }
}