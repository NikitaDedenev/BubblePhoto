package com.example.bubblephoto;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.Manifest;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class StartupActivity extends AppCompatActivity {

    final int REQUEST_CODE_STORAGE_PERMISSION = 0;
    final int pic_id = 0;
    final int CAMERA_PERMISSION_CODE = 0;
    final int PICK_IMAGE_REQUEST = 0;
    private Uri imageUri; // URI для сохранения изображения

    public void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            // Создайте файл для хранения изображения
            File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "captured_image.jpg");
            imageUri = FileProvider.getUriForFile(this, "com.example.bubblephoto.fileprovider", imageFile); // замените на ваш package name

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // передаем URI файла
            startActivityForResult(cameraIntent, pic_id);
        }
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK) {
                // Обработка результата с камеры
                if (requestCode == pic_id) {
                    Intent sendImage = new Intent(StartupActivity.this, MainActivity.class);
                    sendImage.putExtra("ImageUri", imageUri.toString()); // передаем URI
                    sendImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // разрешение на чтение URI
                    startActivity(sendImage);
                }

                // Обработка результата из проводника
                if (requestCode == PICK_IMAGE_REQUEST) {
                    Uri imageUri = data.getData();
                    if (imageUri != null) {
                        // Grant temporary read permission for the URI
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        Intent sendImage = new Intent(StartupActivity.this, MainActivity.class);
                        sendImage.putExtra("ImageUri", imageUri.toString()); // Передаём строку URI
                        startActivity(sendImage);
                    }
                }
            }
        }
    }

    // Метод для сохранения Bitmap в файл
    private File saveBitmapToFile(Bitmap bitmap, String fileName) throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, fileName);

        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }

        return imageFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_startup);
        final ImageButton but_camera = findViewById(R.id.button_camera);
        but_camera.setOnClickListener(v -> {
            openCamera();
        });
        final ImageButton but_gallery = findViewById(R.id.button_gallery);
        but_gallery.setOnClickListener(v -> {
            openGallery();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}