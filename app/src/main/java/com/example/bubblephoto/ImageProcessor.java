package com.example.bubblephoto;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

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
        float normalizedSharpness = sharpness / 100f;
        Bitmap adjustedBitmap = Bitmap.createBitmap(
                original.getWidth(),
                original.getHeight(),
                Bitmap.Config.ARGB_8888);

        int[] pixels = new int[original.getWidth() * original.getHeight()];
        original.getPixels(pixels, 0, original.getWidth(), 0, 0,
                original.getWidth(), original.getHeight());

        float center = 1.0f + (4.0f * normalizedSharpness);
        float outer = -normalizedSharpness;
        float[] kernel = {
                0,    outer, 0,
                outer, center, outer,
                0,    outer, 0
        };

        int width = original.getWidth();
        int height = original.getHeight();
        int[] result = new int[pixels.length];

        System.arraycopy(pixels, 0, result, 0, pixels.length);

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int index = y * width + x;

                float red = 0, green = 0, blue = 0;
                int alpha = pixels[index] & 0xff000000;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pos = (y + ky) * width + (x + kx);
                        float kernelValue = kernel[(ky + 1) * 3 + (kx + 1)];

                        red += ((pixels[pos] >> 16) & 0xff) * kernelValue;
                        green += ((pixels[pos] >> 8) & 0xff) * kernelValue;
                        blue += (pixels[pos] & 0xff) * kernelValue;
                    }
                }

                red = Math.min(Math.max(red, 0), 255);
                green = Math.min(Math.max(green, 0), 255);
                blue = Math.min(Math.max(blue, 0), 255);
                result[index] = alpha |
                        ((int)red << 16) |
                        ((int)green << 8) |
                        (int)blue;
            }
        }

        adjustedBitmap.setPixels(result, 0, width, 0, 0, width, height);
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

    public static Bitmap rotateBitmap(Bitmap original, float degrees) {
        if (original == null) return null;

        int width = original.getWidth();
        int height = original.getHeight();

        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int newWidth = (int) Math.floor(width * cos + height * sin);
        int newHeight = (int) Math.floor(height * cos + width * sin);

        float scale = Math.max(
                (float) width / Math.min(width, height),
                (float) height / Math.min(width, height)
        );

        scale *= Math.max(
                (float) newWidth / width,
                (float) newHeight / height
        );

        Bitmap rotatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(rotatedBitmap);

        canvas.translate(width / 2f, height / 2f);
        canvas.rotate(degrees);
        canvas.scale(scale, scale);
        canvas.translate(-width / 2f, -height / 2f);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        canvas.drawBitmap(original, 0, 0, paint);

        return rotatedBitmap;
    }
}