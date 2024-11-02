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
import android.widget.ImageView;
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



    /*
    public Map<Integer, String> whatsoprDict = new HashMap<Integer, String>();
    private void initializeButtonMap() {
        whatsoprDict.put(R.id.button_cut, "Cut");
        whatsoprDict.put(R.id.button_rotate, "Rotate");
        whatsoprDict.put(R.id.button_brightness, "Brightness");
        whatsoprDict.put(R.id.button_contrast, "Contrast");
        whatsoprDict.put(R.id.button_temperature, "Temperature");
        whatsoprDict.put(R.id.button_sharpness, "Sharpness");
        whatsoprDict.put(R.id.button_exposure, "Exposure");
        whatsoprDict.put(R.id.button_filters, "Filters");
        whatsoprDict.put(R.id.button_blur, "Blur");
        whatsoprDict.put(R.id.button_AI, "AI");
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ReformatSizeElem();
        Intent sendImage = getIntent();

        //initializeButtonMap();
        // Проверка, был ли передан Bitmap (из камеры)
        Bitmap bitmapImage = sendImage.getParcelableExtra("BitmapImage");
        if (bitmapImage != null) {
            SetImage(bitmapImage);
        }

        // Проверка, был ли передан URI изображения (из проводника)
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

        /*
        final ImageButton but_cut = findViewById(R.id.button_cut);
        but_cut.setOnClickListener(v -> {
            whatopr.setText("");
        });
         */

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
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

    private void ReformatSizeElem()
    {
        int[] buttonIds = {
                R.id.button_cut,
                R.id.button_rotate,
                R.id.button_brightness,
                R.id.button_contrast,
                R.id.button_temperature,
                R.id.button_sharpness,
                R.id.button_exposure,
                R.id.button_filters,
                R.id.button_blur,
                R.id.button_AI
        };

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float widthPixels = metrics.widthPixels;
        float sizeButtons = (float) (widthPixels * 0.20);
        float marginButtons = (float) (widthPixels * 0.027);
        for (int i = 0; i < buttonIds.length; i++) {
            ImageButton button = findViewById(buttonIds[i]);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
            params.width = (int) sizeButtons;
            params.height = (int) sizeButtons;

            // Устанавливаем отступы
            if (i < buttonIds.length - 1) {
                params.setMarginEnd((int) marginButtons);
            } else {
                params.setMarginEnd(0);
            }

            button.setLayoutParams(params);
        }

        HorizontalScrollView scrollView = findViewById(R.id.horizontalScrollView);
        ViewGroup.LayoutParams scrollParams = scrollView.getLayoutParams();
        scrollParams.height = (int) sizeButtons;
        scrollView.setLayoutParams(scrollParams);

    }
}