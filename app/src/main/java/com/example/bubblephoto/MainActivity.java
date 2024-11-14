package com.example.bubblephoto;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import com.github.chrisbanes.photoview.PhotoView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private float buttonWidth;
    private float buttonHeight;
    public Map<Integer, String> whatsoprDict = new HashMap<>();
    private ImageButton currentActiveButton = null;
    private Map<ImageButton, Integer> buttonInactiveDrawables = new HashMap<>();

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

        String imageUriString = sendImage.getStringExtra("ImageUri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            loadImageFromUri(imageUri);
        }

        final TextView whatopr = findViewById(R.id.WhatsOperations);
        final Button but_cancel = findViewById(R.id.button_cancel);
        but_cancel.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StartupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        setupButtonListeners(whatopr);

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
                whatopr.setText(""); // Очистить текст, если кнопка деактивирована
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

            // Применение размеров
            applyButtonSize(button);
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
}