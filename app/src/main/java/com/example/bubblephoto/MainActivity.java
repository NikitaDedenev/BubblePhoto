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




    public Map<Integer, String> whatsoprDict = new HashMap<Integer, String>();
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
        //whatsoprDict.put(R.id.button_AI, getString(R.string.button_AI_text));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initializeButtonMap();
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


        final ImageButton but_cut = findViewById(R.id.button_cut);
        but_cut.setOnClickListener(v -> {
            String action = whatsoprDict.get(R.id.button_cut);
            whatopr.setText(action);
        });
        final ImageButton but_rotate = findViewById(R.id.button_rotate);
        but_rotate.setOnClickListener(v -> {
            String action = whatsoprDict.get(R.id.button_rotate);
            whatopr.setText(action);
        });
        final ImageButton but_brightness = findViewById(R.id.button_brightness);
        but_brightness.setOnClickListener(v -> {
            String action = whatsoprDict.get(R.id.button_brightness);
            whatopr.setText(action);
        });
        final ImageButton but_contrast = findViewById(R.id.button_contrast);
        but_contrast.setOnClickListener(v -> {
            String action = whatsoprDict.get(R.id.button_contrast);
            whatopr.setText(action);
        });
        final ImageButton but_temperature = findViewById(R.id.button_temperature);
        but_temperature.setOnClickListener(v -> {
            String action = whatsoprDict.get(R.id.button_temperature);
            whatopr.setText(action);
        });
        final ImageButton but_sharpness = findViewById(R.id.button_sharpness);
        but_sharpness.setOnClickListener(v -> {
            String action = whatsoprDict.get(R.id.button_sharpness);
            whatopr.setText(action);
        });
        final ImageButton but_exposure = findViewById(R.id.button_exposure);
        but_exposure.setOnClickListener(v -> {
            String action = whatsoprDict.get(R.id.button_exposure);
            whatopr.setText(action);
        });
        final ImageButton but_filters = findViewById(R.id.button_filters);
        but_filters.setOnClickListener(v -> {
            String action = whatsoprDict.get(R.id.button_filters);
            whatopr.setText(action);
        });
        final ImageButton but_blur = findViewById(R.id.button_blur);
        but_blur.setOnClickListener(v -> {
            String action = whatsoprDict.get(R.id.button_blur);
            whatopr.setText(action);
        });
        /*
        final ImageButton but_AI = findViewById(R.id.button_AI);
        but_AI.setOnClickListener(v -> {
            String action = whatsoprDict.get(R.id.button_AI);
            whatopr.setText(action);
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

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float widthPixels = metrics.widthPixels;
        float sizeButtons = (float) (widthPixels * 0.20);
        float marginButtons = (float) (widthPixels * 0.027);

        int index = 0;
        for (Map.Entry<Integer, String> entry : whatsoprDict.entrySet()) {
            ImageButton button = findViewById(entry.getKey());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
            params.width = (int) sizeButtons;
            params.height = (int) sizeButtons;

            if (index < whatsoprDict.size() - 2) {
                params.setMarginEnd((int) marginButtons);
            }
            if (index == whatsoprDict.size()){
                params.setMarginEnd(0);
            }

            button.setLayoutParams(params);
            index++;
        }

        HorizontalScrollView scrollView = findViewById(R.id.horizontalScrollView);
        ViewGroup.LayoutParams scrollParams = scrollView.getLayoutParams();
        scrollParams.height = (int) sizeButtons;
        scrollView.setLayoutParams(scrollParams);
    }
}