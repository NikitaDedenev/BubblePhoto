package com.example.bubblephoto;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;

public class StartupActivity extends AppCompatActivity {

    public void openCamera()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Проверяем, доступно ли приложение камеры
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Создаем файл для сохранения изображения
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Если файл успешно создан
            if (imageFile != null) {
                // Получаем URI через FileProvider
                imageUri = FileProvider.getUriForFile(this, "com.yourpackage.fileprovider", imageFile);

                // Передаем URI камере, чтобы она сохранила изображение в этот файл
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_startup);

        final ImageButton but_camera = findViewById(R.id.button_camera);
        but_camera.setOnClickListener(v -> {
            //Open standard phone camera and take image
            //After passes the image to main_activity
            openCamera();
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


}