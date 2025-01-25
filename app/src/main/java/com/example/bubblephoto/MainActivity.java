package com.example.bubblephoto;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private float buttonWidth;
    private float buttonHeight;
    public Map<Integer, String> whatsoprDict = new HashMap<>();
    public Map<Integer, Boolean> hasSlider = new HashMap<>();
    private ImageButton currentActiveButton = null;
    private Map<ImageButton, Integer> buttonInactiveDrawables = new HashMap<>();
    private SeekBar seekBar;
    private Bitmap mCurrentBitmap;
    private Bitmap mOriginalBitmap;

    boolean hasChanges = false;
    boolean changesApplied = false;

    private void initializeButtonMap() {
        whatsoprDict.put(R.id.button_cut, getString(R.string.button_cut_text));
        whatsoprDict.put(R.id.button_rotate, getString(R.string.button_rotate_text));
        whatsoprDict.put(R.id.button_brightness, getString(R.string.button_brightness_text));
        whatsoprDict.put(R.id.button_contrast, getString(R.string.button_contrast_text));
        whatsoprDict.put(R.id.button_temperature, getString(R.string.button_temperature_text));
        whatsoprDict.put(R.id.button_sharpness, getString(R.string.button_sharpness_text));
        whatsoprDict.put(R.id.button_exposure, getString(R.string.button_exposure_text));
        whatsoprDict.put(R.id.button_filters, getString(R.string.button_filters_text));
        whatsoprDict.put(R.id.button_blur, getString(R.string.button_blur_text));

        hasSlider.put(R.id.button_cut, false);
        hasSlider.put(R.id.button_rotate, true);
        hasSlider.put(R.id.button_brightness, true);
        hasSlider.put(R.id.button_contrast, true);
        hasSlider.put(R.id.button_temperature, true);
        hasSlider.put(R.id.button_sharpness, true);
        hasSlider.put(R.id.button_exposure, true);
        hasSlider.put(R.id.button_filters, false);
        hasSlider.put(R.id.button_blur, true);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initializeButtonMap();

        initializeButtonSizes();
        ReformatSizeElem();

        Intent sendImage = getIntent();
        Bitmap bitmapImage = sendImage.getParcelableExtra("BitmapImage");
        if (bitmapImage != null) {
            SetImage(bitmapImage);
        }

        String imageUriString = sendImage.getStringExtra("ImageUri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            loadImageFromUri(imageUri);
        }

        final TextView whatopr = findViewById(R.id.WhatsOperations);
        final Button button_cancel = findViewById(R.id.button_cancel);
        adjustTextSize(button_cancel, whatopr);
        button_cancel.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StartupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        final Button button_save = findViewById(R.id.button_save);
        adjustTextSize(button_save, whatopr);
        button_save.setEnabled(false);
        button_save.setTextColor(getResources().getColor(R.color.text_disable));

        button_save.setOnClickListener(v -> {
            if (!changesApplied) {
                mOriginalBitmap = mCurrentBitmap.copy(mCurrentBitmap.getConfig(), true);
                PhotoView photoView = findViewById(R.id.photo_view);
                photoView.setImageBitmap(mOriginalBitmap);

                if (currentActiveButton != null) {
                    Integer inactiveDrawable = buttonInactiveDrawables.get(currentActiveButton);
                    if (inactiveDrawable != null) {
                        currentActiveButton.setImageResource(inactiveDrawable);
                    }
                    currentActiveButton = null;
                    whatopr.setText("");
                    seekBar.setVisibility(View.INVISIBLE);
                    moveGuideline(0.79f);
                }

                changesApplied = true;
                button_save.setEnabled(true);
                button_save.setTextColor(getResources().getColor(R.color.text_default));
            } else {
                saveImage();
                hasChanges = false;
                changesApplied = false;
                button_save.setEnabled(false);
                button_save.setTextColor(getResources().getColor(R.color.text_disable));
            }
        });

        setupButtonListeners(whatopr);
        seekBar = findViewById(R.id.seekBar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeButtonSizes() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        buttonWidth = metrics.widthPixels * 0.20f;
        buttonHeight = buttonWidth;
    }

    private void ReformatSizeElem() {
        for (Map.Entry<Integer, String> entry : whatsoprDict.entrySet()) {
            ImageButton button = findViewById(entry.getKey());
            if (button != null) {
                applyButtonSize(button);
            }
        }

        HorizontalScrollView scrollView = findViewById(R.id.horizontalScrollView);
        if (scrollView != null) {
            ViewGroup.LayoutParams scrollParams = scrollView.getLayoutParams();
            scrollParams.height = (int) buttonHeight;
            scrollView.setLayoutParams(scrollParams);
        }
    }

    private void applyButtonSize(ImageButton button) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
        params.width = (int) buttonWidth;
        params.height = (int) buttonHeight;
        button.setLayoutParams(params);
    }

    private void setupButtonListeners(TextView whatopr) {
        setupButtonListener(R.id.button_cut, R.drawable.cut, R.drawable.cut_active, whatopr);
        setupButtonListener(R.id.button_rotate, R.drawable.rotate, R.drawable.rotate_active, whatopr);
        setupButtonListener(R.id.button_brightness, R.drawable.brightness, R.drawable.brightness_active, whatopr);
        setupButtonListener(R.id.button_contrast, R.drawable.contrast, R.drawable.contrast_active, whatopr);
        setupButtonListener(R.id.button_temperature, R.drawable.temperature, R.drawable.temperature_active, whatopr);
        setupButtonListener(R.id.button_sharpness, R.drawable.sharpness, R.drawable.sharpness_active, whatopr);
        setupButtonListener(R.id.button_exposure, R.drawable.exposure, R.drawable.exposure_active, whatopr);
        setupButtonListener(R.id.button_filters, R.drawable.filters, R.drawable.filters_active, whatopr);
        setupButtonListener(R.id.button_blur, R.drawable.blur, R.drawable.blur_active, whatopr);
    }

    private void setupButtonListener(int buttonId, int inactiveDrawable, int activeDrawable, TextView whatopr) {
        final ImageButton button = findViewById(buttonId);
        buttonInactiveDrawables.put(button, inactiveDrawable);

        button.setOnClickListener(v -> {
            String action = whatsoprDict.get(buttonId);

            if (currentActiveButton == button) {
                button.setImageResource(inactiveDrawable);
                currentActiveButton = null;
                whatopr.setText("");
                moveGuideline(0.79f);
                if (hasSlider.get(buttonId)) {
                    seekBar.setVisibility(View.INVISIBLE);
                }
                return;
            }

            if (currentActiveButton != null) {
                Integer previousInactiveDrawable = buttonInactiveDrawables.get(currentActiveButton);
                if (previousInactiveDrawable != null) {
                    currentActiveButton.setImageResource(previousInactiveDrawable);
                }
            }

            button.setImageResource(activeDrawable);
            currentActiveButton = button;
            whatopr.setText(action);

            if (hasSlider.get(buttonId)) {
                moveGuideline(0.75f);
                seekBar.setVisibility(View.VISIBLE);
            } else {
                moveGuideline(0.79f);
                seekBar.setVisibility(View.INVISIBLE);
            }

            applyButtonSize(button);

            if (buttonId == R.id.button_cut) {
                performCutOperation();
            } else if (buttonId == R.id.button_rotate) {
                performRotateOperation();
            } else if (buttonId == R.id.button_brightness) {
                performBrightnessOperation();
            } else if (buttonId == R.id.button_contrast) {
                performContrastOperation();
            } else if (buttonId == R.id.button_temperature) {
                performTemperatureOperation();
            } else if (buttonId == R.id.button_sharpness) {
                performSharpnessOperation();
            } else if (buttonId == R.id.button_exposure) {
                performExposureOperation();
            } else if (buttonId == R.id.button_filters) {
                performFiltersOperation();
            } else if (buttonId == R.id.button_blur) {
                performBlurOperation();
            }
        });
    }

    private void adjustTextSize(final Button button, final TextView textView) {
        button.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                button.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                String text = button.getText().toString();
                float textSize = button.getTextSize();
                int buttonWidth = button.getWidth() - button.getPaddingLeft() - button.getPaddingRight();

                Paint paint = new Paint();
                paint.set(button.getPaint());
                float textWidth = paint.measureText(text);

                while (textWidth > buttonWidth && textSize > 0) {
                    textSize--;
                    paint.setTextSize(textSize);
                    textWidth = paint.measureText(text);
                }

                button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }
        });
    }

    private void SetImage(Bitmap image) {
        PhotoView photoView = findViewById(R.id.photo_view);
        mOriginalBitmap = image;
        mCurrentBitmap = image.copy(image.getConfig(), true);
        photoView.setImageBitmap(mCurrentBitmap);
    }

    private void loadImageFromUri(Uri uri) {
        PhotoView photoView = findViewById(R.id.photo_view);
        try {
            mOriginalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            mCurrentBitmap = mOriginalBitmap.copy(mOriginalBitmap.getConfig(), true);
            photoView.setImageBitmap(mCurrentBitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableSaveButton() {
        final Button button_save = findViewById(R.id.button_save);
        hasChanges = true;
        changesApplied = false;
        button_save.setEnabled(true);
        button_save.setTextColor(getResources().getColor(R.color.text_enable));
    }

    private void saveImage() {
        if (mCurrentBitmap == null) {
            Toast.makeText(this, "Нет изображения для сохранения", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss",
                    java.util.Locale.getDefault()).format(new java.util.Date());
            String imageFileName = "BubblePhoto_" + timeStamp + ".jpg";

            String path = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_PICTURES).toString();
            java.io.File directory = new java.io.File(path + "/BubblePhoto");

            if (!directory.exists()) {
                directory.mkdirs();
            }

            java.io.File file = new java.io.File(directory, imageFileName);
            java.io.FileOutputStream fOut = new java.io.FileOutputStream(file);

            mCurrentBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(getContentResolver(),
                    file.getAbsolutePath(), file.getName(), file.getName());

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(file)));

            Toast.makeText(this, "Изображение сохранено в галерею", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this, StartupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при сохранении: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void moveGuideline(float percent) {
        androidx.constraintlayout.widget.Guideline guideline = findViewById(R.id.guideline_scrollStartTop);
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) guideline.getLayoutParams();
        params.guidePercent = percent;
        guideline.setLayoutParams(params);
    }

    private void performCutOperation() {
        // Implement cut operation
        //enableSaveButton();
    }

    private void performRotateOperation() {
        seekBar.setMax(360);
        seekBar.setMin(0);
        seekBar.setProgress(0);

        final PhotoView photoView = findViewById(R.id.photo_view);
        photoView.setRotation(0);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private float lastRotation = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress == 0 && mOriginalBitmap != null) {
                        mCurrentBitmap = mOriginalBitmap.copy(mOriginalBitmap.getConfig(), true);
                        photoView.setImageBitmap(mCurrentBitmap);
                        enableSaveButton();
                    } else {
                        mCurrentBitmap = ImageProcessor.rotateBitmap(mOriginalBitmap, progress);
                        photoView.setImageBitmap(mCurrentBitmap);
                        enableSaveButton();
                    }
                    lastRotation = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void performContrastOperation() {
        seekBar.setMax(150);
        seekBar.setMin(60);
        seekBar.setProgress(105);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mOriginalBitmap != null) {
                    float contrast = seekBar.getProgress() / 100f;
                    mCurrentBitmap = ImageProcessor.adjustContrast(mOriginalBitmap, contrast);
                    PhotoView photoView = findViewById(R.id.photo_view);
                    photoView.setImageBitmap(mCurrentBitmap);
                    enableSaveButton();
                }
            }
        });
    }

    private void performBrightnessOperation() {
        seekBar.setMax(200);
        seekBar.setProgress(100);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mOriginalBitmap != null) {
                    float brightness = (seekBar.getProgress() - 100) * 1.5f;
                    mCurrentBitmap = ImageProcessor.adjustBrightness(mOriginalBitmap, brightness);
                    PhotoView photoView = findViewById(R.id.photo_view);
                    photoView.setImageBitmap(mCurrentBitmap);
                    enableSaveButton();
                }
            }
        });
    }

    private void performTemperatureOperation() {
        seekBar.setMax(200);
        seekBar.setMin(50);
        seekBar.setProgress(125);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mOriginalBitmap != null) {
                    float temperature = (seekBar.getProgress() - 100) * 1.5f;
                    mCurrentBitmap = ImageProcessor.adjustTemperature(mOriginalBitmap, temperature);
                    PhotoView photoView = findViewById(R.id.photo_view);
                    photoView.setImageBitmap(mCurrentBitmap);
                    enableSaveButton();
                }
            }
        });
    }

    private void performSharpnessOperation() {
        seekBar.setMax(100);
        seekBar.setProgress(0);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mOriginalBitmap != null) {
                    float sharpness = seekBar.getProgress() / 100f * 5f;
                    mCurrentBitmap = ImageProcessor.adjustSharpness(mOriginalBitmap, sharpness);
                    PhotoView photoView = findViewById(R.id.photo_view);
                    photoView.setImageBitmap(mCurrentBitmap);
                    enableSaveButton();
                }
            }
        });
    }

    private void performExposureOperation() {
        seekBar.setMax(200);
        seekBar.setMin(30);
        seekBar.setProgress(115);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mOriginalBitmap != null) {
                    float exposure = seekBar.getProgress() - 100;
                    mCurrentBitmap = ImageProcessor.adjustExposure(mOriginalBitmap, exposure);
                    PhotoView photoView = findViewById(R.id.photo_view);
                    photoView.setImageBitmap(mCurrentBitmap);
                    enableSaveButton();
                }
            }
        });
    }

    private void performFiltersOperation() {
        // Implement filters operation
        //enableSaveButton();
    }

    private void performBlurOperation() {
        seekBar.setMax(100);
        seekBar.setProgress(0);

        DrawingView drawingView = findViewById(R.id.drawing_view);
        PhotoView photoView = findViewById(R.id.photo_view);
        drawingView.setDrawingEnabled(true);
        drawingView.setBrushSize(seekBar.getProgress());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    drawingView.setBrushSize(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        drawingView.setDrawingListener(new DrawingView.OnDrawingListener() {
            @Override
            public void onDrawingStart(float x, float y) {
                if (mCurrentBitmap != null) {
                    mCurrentBitmap = mCurrentBitmap.copy(mCurrentBitmap.getConfig(), true);
                    enableSaveButton();
                }
            }

            @Override
            public void onDrawing(float x, float y) {
                if (mCurrentBitmap != null) {
                    float viewWidth = photoView.getWidth();
                    float viewHeight = photoView.getHeight();
                    float imageWidth = mCurrentBitmap.getWidth();
                    float imageHeight = mCurrentBitmap.getHeight();

                    float scaleX = imageWidth / viewWidth;
                    float scaleY = imageHeight / viewHeight;

                    float imageX = x * scaleX;
                    float imageY = y * scaleY;

                    float brushSize = seekBar.getProgress() * scaleX;
                    float radius = 45 * scaleX;

                    mCurrentBitmap = ImageProcessor.applyLocalBlur(mCurrentBitmap, imageX, imageY, brushSize, radius);
                    photoView.setImageBitmap(mCurrentBitmap);
                }
            }

            @Override
            public void onDrawingEnd() {}
        });
    }
}