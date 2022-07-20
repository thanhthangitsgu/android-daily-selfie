package com.example.daily_selfie;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageButton btn_doTake;
    static File filePhoto;
    static String filePath = "";
    static String fileName = "";
    static ListView list_view;
    ArrayList<Bitmap> listImage = new ArrayList<Bitmap>();
    ArrayList<String> nameImage = new ArrayList<String>();
    private static final long INTERVAL_TWO_MINUTES = 100L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createAlarm();
        setContentView(R.layout.activity_main);
        btn_doTake = findViewById(R.id.btn_doTake);
        list_view = findViewById(R.id.listImage);
        loadImage();
        btn_doTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intentView = new Intent(MainActivity.this, ViewImage.class).putExtra("image", listImage.get(i));
                File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String[] list = dir.list();
                intentView.putExtra("path", dir + "/" + nameImage.get(i));
                finish();
                startActivity(intentView);
            }
        });
        ImageButton btn_delete = findViewById(R.id.deleteImage);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, DeleteActivity.class));
            }
        });
    }

    //Lấy danh sách hình ảnh đã lưu
    private void loadImage() {
        listImage.clear();
        nameImage.clear();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String[] list = storageDir.list();
        for (int i = 0; i < list.length; i++) if(!list[i].contains("Deleted")) {
            Bitmap bitmap = bitmapEdit.setImageFromFilePath(storageDir + "/" + list[i]);
            listImage.add(bitmap);
            nameImage.add(list[i]);
        }
        ImageAdapter imageAdapter = new ImageAdapter(MainActivity.this, listImage, nameImage);
        list_view.setAdapter(imageAdapter);
    }

    //Lấy hình ảnh vừa chụp
    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
            return;
        }
        filePhoto = null;
        try {
            filePhoto = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (filePhoto != null) {
            Uri photoUri = FileProvider.getUriForFile(MainActivity.this, "com.example.daily_selfie.fileprovider", filePhoto);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            someActitvityResultLaucher.launch(intent);
        }
    }

    //Đăng ký cho ActivityResult, xử lí kết quả trả về
    ActivityResultLauncher<Intent> someActitvityResultLaucher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bitmap bitmap = bitmapEdit.setImageFromFilePath(filePath);
                        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        String[] list = storageDir.list();
                        listImage.add(bitmap);
                        nameImage.add(fileName);
                        ImageAdapter imageAdapter = new ImageAdapter(MainActivity.this, listImage, nameImage);
                        list_view.setAdapter(imageAdapter);
                        Toast.makeText(MainActivity.this, "Chụp thành công", Toast.LENGTH_SHORT).show();
                    } else{
                        File file =  new File(filePath);
                        file.delete();
                    }
                }
            });

    //Tạo file
    private File createImageFile() throws IOException {
        // Create an image file name
        fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + fileName + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        fileName = image.getName();
        filePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadImage();
    }
    private void createAlarm() {

        try {
            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + INTERVAL_TWO_MINUTES,
                    INTERVAL_TWO_MINUTES,
                    pendingIntent);
        }
        catch (Exception exception) {
            Log.d("ALARM", exception.getMessage().toString());
        }
    }
}