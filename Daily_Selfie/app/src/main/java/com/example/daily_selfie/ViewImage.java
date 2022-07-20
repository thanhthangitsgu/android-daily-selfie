package com.example.daily_selfie;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

public class ViewImage extends AppCompatActivity {
    //@RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String[] item = {"Xóa"};
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        PhotoView photoView = findViewById(R.id.image_view);
        Intent intent = getIntent();
        if(intent.getExtras() != null){
            Bundle extras = intent.getExtras();
            String path = (String) extras.get("path");
            photoView.setImageBitmap(bitmapEdit.setImageFromFilePath(path,600, 500));
            String[] split = path.split("/");
            String fileName = split[split.length-1];
            File del = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File dest = new File(del,"Deleted");
            if(!dest.exists()) dest.mkdir();
            File dest_2 = new File(dest, fileName);
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ViewImage.this);
                    alert.setTitle("Tùy chọn");
                    alert.setItems(item, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(i==0){
                                File source = new File(path);
                                source.renameTo(dest_2);
                                Toast.makeText(ViewImage.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(ViewImage.this, MainActivity.class));
                            }
                        }
                    });
                    alert.show();
                }
            });
        }
    }
}