package com.example.bubblephoto;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.Manifest;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class StartupActivity extends AppCompatActivity {

    final int REQUEST_CODE_STORAGE_PERMISSION = 0;
    final int pic_id = 0;
    final int CAMERA_PERMISSION_CODE = 0;
    final int PICK_IMAGE_REQUEST = 0;


    public void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camera_intent, pic_id);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            // Обработка результата с камеры
            if (requestCode == pic_id) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap photo = (Bitmap) extras.get("data");
                    if (photo != null) {
                        Intent sendImage = new Intent(StartupActivity.this, MainActivity.class);
                        sendImage.putExtra("BitmapImage", photo);
                        startActivity(sendImage);
                    } else {
                        Toast.makeText(this, "Error capturing image", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            // Обработка результата из проводника
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    // Grant temporary read permission for the URI (чтобы URI был доступен и в следующей активности)
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Intent sendImage = new Intent(StartupActivity.this, MainActivity.class);
                    sendImage.putExtra("ImageUri", imageUri.toString()); // Передаём строку URI
                    startActivity(sendImage);
                } else {
                    Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Result not OK or data is null", Toast.LENGTH_SHORT).show();
        }
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