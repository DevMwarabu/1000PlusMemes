package com.pluslatestmemes.MainIssues.ActivitiesFromMenu;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.pluslatestmemes.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MemeGenerator extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private Button mGenerate,mPickImage,mSetText;
    private ImageView mImagePicked;
    private LinearLayout mLinearGenerator;
    private EditText mMemeText,mMemeTextBottom;
    private Uri imageUri = null;
    private AdView mAdView;
    private TextView mMemeCarrier,mMemeCarrierBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_generator);

        mLinearGenerator = findViewById(R.id.linear_meme_generator);
        mGenerate = findViewById(R.id.btn_generate_meme);
        mImagePicked = findViewById(R.id.image_meme_generator);
        mMemeText = findViewById(R.id.edt_meme_text_generator);
        mPickImage = findViewById(R.id.btn_pickimage_meme);
        mMemeCarrierBottom = findViewById(R.id.tv_meme_carrier_bottom);
        mMemeTextBottom = findViewById(R.id.edt_meme_text_bottom_generator);
        mMemeCarrier = findViewById(R.id.tv_meme_carrier);
        mSetText = findViewById(R.id.btn_set_meme);

        mGenerate.setVisibility(View.GONE);
        mMemeText.setVisibility(View.GONE);
        mMemeTextBottom.setVisibility(View.GONE);
        mSetText.setVisibility(View.GONE);


        MobileAds.initialize(this,getString(R.string.appid));
        mAdView = findViewById(R.id.adView_generator);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MemeGenerator.this);

            }
        });

        mSetText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mMemeText.getText().toString().trim()) && TextUtils.isEmpty(mMemeTextBottom.getText().toString().trim())) {
                    Toast.makeText(MemeGenerator.this, "Enter something.", Toast.LENGTH_SHORT).show();
            }else {
                    mMemeCarrier.setText(mMemeText.getText().toString().trim());
                    mMemeCarrierBottom.setText(mMemeTextBottom.getText().toString().trim());

                    mGenerate.setVisibility(View.VISIBLE);
                }

            }
        });

        mGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              if (imageUri == null){
                    Toast.makeText(MemeGenerator.this, "Pick image..!", Toast.LENGTH_SHORT).show();
                }else{
                    File file = saveBitMap(MemeGenerator.this ,mLinearGenerator);
                    if (file != null) {
                        Toast.makeText(MemeGenerator.this, "Meme saved to gallery.", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                        Log.i("TAG", "Drawing saved to the gallery!");
                    } else {
                        Toast.makeText(MemeGenerator.this, "Oops! Meme could not be generated.", Toast.LENGTH_SHORT).show();
                        Log.i("TAG", "Oops! Image could not be saved. Try again");
                    }
                }

            }
        });
    }

    private File saveBitMap(Context context, View drawView){
        File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Generated Memes");
        if (!pictureFileDir.exists()) {
            boolean isDirectoryCreated = pictureFileDir.mkdirs();
            if(!isDirectoryCreated)
                Log.i("ATG", "Can't create directory to save the image");
            return null;
        }
        String filename = pictureFileDir.getPath() +File.separator+ System.currentTimeMillis()+".jpg";
        File pictureFile = new File(filename);
        Bitmap bitmap =getBitmapFromView(drawView);
        try {
            pictureFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
            oStream.flush();
            oStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TAG", "There was an issue saving the image.");
        }
        scanGallery( context,pictureFile.getAbsolutePath());
        return pictureFile;
    }

    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
    // used for scanning gallery
    private void scanGallery(Context cntx, String path) {
        try {
            MediaScannerConnection.scanFile(cntx, new String[] { path },null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                mImagePicked.setImageURI(imageUri);
                if (TextUtils.isEmpty(mMemeText.getText().toString().trim())){
                    mGenerate.setVisibility(View.GONE);
                    mMemeText.setVisibility(View.VISIBLE);
                    mSetText.setVisibility(View.VISIBLE);
                    mMemeTextBottom.setVisibility(View.VISIBLE);
                }else {
                    mGenerate.setVisibility(View.VISIBLE);
                    mMemeText.setVisibility(View.VISIBLE);
                    mSetText.setVisibility(View.VISIBLE);
                    mMemeTextBottom.setVisibility(View.VISIBLE);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
