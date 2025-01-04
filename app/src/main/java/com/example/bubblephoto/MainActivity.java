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
import com.github.chrisbanes.photoview.PhotoView;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.opengl.GLSurfaceView;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;

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

    private GLSurfaceView mEffectView;
    private Effect mEffect;
    private EffectContext mEffectContext;
    private SeekBar seekBar;

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

        // Инициализация размеров кнопок
        initializeButtonSizes();

        // Установка размеров кнопок
        ReformatSizeElem();

        Intent sendImage = getIntent();
        Bitmap bitmapImage = sendImage.getParcelableExtra("BitmapImage");
        if (bitmapImage != null) {
            SetImage(bitmapImage);
        }

        mEffectView = new GLSurfaceView(this);

        String imageUriString = sendImage.getStringExtra("ImageUri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            loadImageFromUri(imageUri);
        }

        final TextView whatopr = findViewById(R.id.WhatsOperations);
        final Button but_cancel = findViewById(R.id.button_cancel);
        adjustTextSize(but_cancel, whatopr);
        but_cancel.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StartupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });


        final Button but_save = findViewById(R.id.button_save);
        adjustTextSize(but_save, whatopr);

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
        buttonHeight = buttonWidth; // Если высота и ширина одинаковы
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
        buttonInactiveDrawables.put(button, inactiveDrawable); // Сохраняем неактивное изображение для кнопки

        button.setOnClickListener(v -> {
            String action = whatsoprDict.get(buttonId);

            // Если текущая кнопка уже активна, сделайте её неактивной
            if (currentActiveButton == button) {
                button.setImageResource(inactiveDrawable);
                currentActiveButton = null;
                whatopr.setText("");

                // Скрываем SeekBar и возвращаем Guideline в исходное положение
                moveGuideline(0.79f);
                if (hasSlider.get(buttonId)) {
                    seekBar.setVisibility(View.INVISIBLE);
                }
                return;
            }

            // Сброс предыдущей активной кнопки
            if (currentActiveButton != null) {
                Integer previousInactiveDrawable = buttonInactiveDrawables.get(currentActiveButton);
                if (previousInactiveDrawable != null) {
                    currentActiveButton.setImageResource(previousInactiveDrawable);
                }
            }

            // Установка текущей кнопки как активной
            button.setImageResource(activeDrawable);
            currentActiveButton = button;
            whatopr.setText(action);

            if (hasSlider.get(buttonId)) {
                moveGuideline(0.75f);
                seekBar.setVisibility(View.VISIBLE);
            }
            else {
                moveGuideline(0.79f);
                seekBar.setVisibility(View.INVISIBLE);
            }

            // Применение размеров
            applyButtonSize(button);

            // Вызов функции, связанной с кнопкой
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
            } else {
                throw new IllegalStateException("Unexpected value: " + buttonId);
            }
        });
    }

    private void adjustTextSize(final Button button, final TextView textView) {
        button.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                button.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                String text = button.getText().toString();
                float textSize = button.getTextSize(); // Получаем текущий размер текста в пикселях
                int buttonWidth = button.getWidth() - button.getPaddingLeft() - button.getPaddingRight();

                // Создаем Paint для измерения текста
                Paint paint = new Paint();
                paint.set(button.getPaint());

                // Измеряем ширину текста
                float textWidth = paint.measureText(text);

                // Уменьшаем размер текста, пока он не поместится в кнопку
                while (textWidth > buttonWidth && textSize > 0) {
                    textSize--;
                    paint.setTextSize(textSize);
                    textWidth = paint.measureText(text);
                }

                // Устанавливаем новый размер текста
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

            }
        });
    }

    private void SetImage(Bitmap image) {
        PhotoView photoView = findViewById(R.id.photo_view);
        photoView.setImageBitmap(image);
    }

    private void loadImageFromUri(Uri uri) {
        PhotoView photoView = findViewById(R.id.photo_view);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            photoView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveGuideline(float percent) {
        androidx.constraintlayout.widget.Guideline guideline = findViewById(R.id.guideline_scrollStartTop);
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) guideline.getLayoutParams();
        params.guidePercent = percent; // например, 0.75f
        guideline.setLayoutParams(params);
    }


    private void performCutOperation() {

    }

    private void performRotateOperation() {

    }

    private void performBrightnessOperation() {

    }

    private void performContrastOperation() {

    }

    private void performTemperatureOperation() {

    }

    private void performSharpnessOperation() {

    }

    private void performExposureOperation() {

    }

    private void performFiltersOperation() {

    }

    private void performBlurOperation() {

    }

}