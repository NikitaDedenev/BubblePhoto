package com.example.bubblephoto;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.BlurMaskFilter;

public class ImageProcessor {

    public static Bitmap adjustContrast(Bitmap original, float contrast) {
        if (original == null) return null;

        Bitmap adjustedBitmap = Bitmap.createBitmap(
                original.getWidth(),
                original.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(adjustedBitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        float scale = contrast;
        if (scale < 0.2f) scale = 0.2f;

        float translate = (-.5f * scale + .5f) * 255f;
        if (contrast < 1.0f) {
            translate = (127.5f * (1 - scale));
        }

        cm.set(new float[] {
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        });

        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(original, 0, 0, paint);

        return adjustedBitmap;
    }

    public static Bitmap adjustBrightness(Bitmap original, float brightness) {
        if (original == null) return null;

        Bitmap adjustedBitmap = Bitmap.createBitmap(
                original.getWidth(),
                original.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(adjustedBitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        cm.set(new float[] {
                1, 0, 0, 0, brightness,
                0, 1, 0, 0, brightness,
                0, 0, 1, 0, brightness,
                0, 0, 0, 1, 0
        });

        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(original, 0, 0, paint);

        return adjustedBitmap;
    }

    public static Bitmap adjustTemperature(Bitmap original, float temperature) {
        if (original == null) return null;

        Bitmap adjustedBitmap = Bitmap.createBitmap(
                original.getWidth(),
                original.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(adjustedBitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        float scale = (temperature - 100f) / 1000f;

        cm.set(new float[] {
                1 + scale, 0, 0, 0, 0,
                0, 1, 0, 0, 0,
                0, 0, 1 - scale, 0, 0,
                0, 0, 0, 1, 0
        });

        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(original, 0, 0, paint);

        return adjustedBitmap;
    }

    public static Bitmap adjustSharpness(Bitmap original, float sharpness) {
        if (original == null) return null;

        Bitmap adjustedBitmap = Bitmap.createBitmap(
                original.getWidth(),
                original.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(adjustedBitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        float normalizedSharpness = sharpness / 10f;

        float[] matrix = {
                1 + normalizedSharpness, -normalizedSharpness/2, -normalizedSharpness/2, 0, 0,
                -normalizedSharpness/2, 1 + normalizedSharpness, -normalizedSharpness/2, 0, 0,
                -normalizedSharpness/2, -normalizedSharpness/2, 1 + normalizedSharpness, 0, 0,
                0, 0, 0, 1, 0
        };

        cm.set(matrix);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(original, 0, 0, paint);

        return adjustedBitmap;
    }

    public static Bitmap adjustExposure(Bitmap original, float exposure) {
        if (original == null) return null;

        Bitmap adjustedBitmap = Bitmap.createBitmap(
                original.getWidth(),
                original.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(adjustedBitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        float scaleFactor = 1.0f + (exposure / 100f);

        cm.set(new float[] {
                scaleFactor, 0, 0, 0, 0,
                0, scaleFactor, 0, 0, 0,
                0, 0, scaleFactor, 0, 0,
                0, 0, 0, 1, 0
        });

        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(original, 0, 0, paint);

        return adjustedBitmap;
    }

    public static Bitmap applyLocalBlur(Bitmap bitmap, float x, float y, float brushSize, float blurRadius) {
        if (bitmap == null) return null;

        int left = Math.max(0, (int)(x - brushSize/2));
        int top = Math.max(0, (int)(y - brushSize/2));
        int right = Math.min(bitmap.getWidth(), (int)(x + brushSize/2));
        int bottom = Math.min(bitmap.getHeight(), (int)(y + brushSize/2));

        if (right <= left || bottom <= top) {
            return bitmap;
        }

        Bitmap section = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top);

        Paint blurPaint = new Paint();
        blurPaint.setAntiAlias(true);
        blurPaint.setMaskFilter(new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL));

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(section, left, top, blurPaint);

        section.recycle();
        return bitmap;
    }
}