package com.pluslatestmemes.MainIssues;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jsibbold.zoomage.ZoomageView;
import com.pluslatestmemes.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ZoomingImage extends AppCompatActivity {
    private ZoomageView imaPasssed;
    private String imageUrl;
    private FloatingActionButton mDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zooming_image);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        mDownload = findViewById(R.id.float_download_zoomedimage);
        imaPasssed = findViewById(R.id.image_post_clicked);


        //getting the passed image
        imageUrl = getIntent().getExtras().getString("imageUrl");

        //setting the image now
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop();
        requestOptions.fitCenter();
        Glide.with(getApplicationContext()).setDefaultRequestOptions(requestOptions).load(imageUrl).into(imaPasssed);

        mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable draw = (BitmapDrawable) imaPasssed.getDrawable();
                Bitmap bitmap = draw.getBitmap();
                if (imaPasssed!=null) {

                    FileOutputStream outStream = null;
                    File sdCard = Environment.getExternalStorageDirectory();
                    File dir = new File(sdCard.getAbsolutePath() + "/Memes");
                    dir.mkdirs();
                    String fileName = String.format("%d.jpg", System.currentTimeMillis());
                    File outFile = new File(dir, fileName);

                    try {
                        outStream = new FileOutputStream(outFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(Uri.fromFile(dir));
                        sendBroadcast(intent);
                        Toast.makeText(getApplicationContext(), "Saved to gallery", Toast.LENGTH_SHORT).show();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else
                {
                    Toast.makeText(getApplicationContext(),"oops!:\nCannot download null image...!!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
