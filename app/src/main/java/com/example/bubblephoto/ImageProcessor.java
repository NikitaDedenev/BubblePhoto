package com.example.bubblephoto;

import android.graphics.Bitmap;
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

        Bitmap adjustedBitmap = Bitmap.createBitmap(
                original.getWidth(),
                original.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(adjustedBitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();

        float[] sharpenMatrix = {
                0, -sharpness, 0,
                -sharpness, 1 + (4 * sharpness), -sharpness,
                0, -sharpness, 0
        };

        cm.set(new float[] {
                sharpenMatrix[4], sharpenMatrix[3], sharpenMatrix[3], 0, 0,
                sharpenMatrix[1], sharpenMatrix[4], sharpenMatrix[1], 0, 0,
                sharpenMatrix[1], sharpenMatrix[1], sharpenMatrix[4], 0, 0,
                0, 0, 0, 1, 0
        });

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

    public static Bitmap adjustBlur(Bitmap original, float radius) {
        if (original == null) return null;

        Bitmap adjustedBitmap = original.copy(original.getConfig(), true);

        int width = adjustedBitmap.getWidth();
        int height = adjustedBitmap.getHeight();
        int[] pixels = new int[width * height];

        adjustedBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int r = (int) Math.ceil(radius);
        int iterations = 2;

        for (int i = 0; i < iterations; i++) {
            for (int y = 0; y < height; y++) {
                for (int x = r; x < width - r; x++) {
                    int red = 0, green = 0, blue = 0, alpha = 0;
                    for (int ix = x - r; ix <= x + r; ix++) {
                        int pixel = pixels[y * width + ix];
                        alpha += (pixel >> 24) & 0xff;
                        red += (pixel >> 16) & 0xff;
                        green += (pixel >> 8) & 0xff;
                        blue += pixel & 0xff;
                    }
                    int count = r * 2 + 1;
                    pixels[y * width + x] = ((alpha / count) << 24) |
                            ((red / count) << 16) |
                            ((green / count) << 8) |
                            (blue / count);
                }
            }

            for (int x = 0; x < width; x++) {
                for (int y = r; y < height - r; y++) {
                    int red = 0, green = 0, blue = 0, alpha = 0;
                    for (int iy = y - r; iy <= y + r; iy++) {
                        int pixel = pixels[iy * width + x];
                        alpha += (pixel >> 24) & 0xff;
                        red += (pixel >> 16) & 0xff;
                        green += (pixel >> 8) & 0xff;
                        blue += pixel & 0xff;
                    }
                    int count = r * 2 + 1;
                    pixels[y * width + x] = ((alpha / count) << 24) |
                            ((red / count) << 16) |
                            ((green / count) << 8) |
                            (blue / count);
                }
            }
        }

        adjustedBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return adjustedBitmap;
    }
}