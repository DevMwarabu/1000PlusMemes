package com.pluslatestmemes.MainIssues;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pluslatestmemes.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Upload extends AppCompatActivity {
    private ImageView mBack,mPicker,mSelected;
    private EditText mPostWritten;
    private Button mSubmit;
    private AdView mAdView;
    private TextView mPostWrittenToPosts;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private Uri imageUri;
    private StorageReference storageReference;
    private String user_id,memeToPosts;
    private int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user_id = auth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);

        mPicker = findViewById(R.id.image_imagePicker);
        mSelected = findViewById(R.id.image_imageSelected);
        mPostWritten = findViewById(R.id.edt_whatson_new_post);
        mSubmit = findViewById(R.id.btn_share_meme_newpost);


        MobileAds.initialize(this,getString(R.string.appid));
        mAdView = findViewById(R.id.adView_newpost);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(Upload.this);
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String randomName = UUID.randomUUID().toString();
                final String meme = mPostWritten.getText().toString().trim();
                //checking values
                if (imageUri == null && TextUtils.isEmpty(meme)){
                    Toast.makeText(Upload.this, "Set something to post..!", Toast.LENGTH_SHORT).show();
                }else if (!TextUtils.isEmpty(meme) && imageUri != null) {
                    Toast.makeText(Upload.this, "Pick only one thing either type your creative or post image", Toast.LENGTH_SHORT).show();
                }else {
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle("Posting");
                    progressDialog.setMessage("Posting your meme please wait..");
                    progressDialog.show();

                    if (TextUtils.isEmpty(meme) && imageUri!=null){
                        firebaseFirestore.collection("Users").document(user_id).collection("Profile")
                                .document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    if (task.getResult().exists()){
                                        memeToPosts = "NoText";
                                        final StorageReference reference = storageReference.child("PostImages").
                                                child(randomName+user_id+".jpg");
                                        reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String imageUrl = uri.toString();
                                                        newPost(memeToPosts,imageUrl);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }else if (!TextUtils.isEmpty(meme) && imageUri == null){
                        firebaseFirestore.collection("Users").document(user_id).collection("Profile")
                                .document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    if (task.getResult().exists()){
                                        String imageUrl = "NoImage";
                                        newPost(meme,imageUrl);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }



    private void newPost(String meme,String imageUrl){
        Map<String,Object> objectMap = new HashMap<>();
        objectMap.put("meme",meme);
        objectMap.put("user_id",user_id);
        objectMap.put("imageUrl",imageUrl);
        objectMap.put("timeStamp", FieldValue.serverTimestamp());
        firebaseFirestore.collection("AllPosts")
                .add(objectMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if (task.isSuccessful()){
                    Toast.makeText(Upload.this, "Meme sent lets all lough...", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Upload.this,MainPanel.class));
                    finish();
                }else {
                    Toast.makeText(Upload.this, "Something went wrong...!"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                mSelected.setImageURI(imageUri);
                mSelected.setVisibility(View.VISIBLE);
                mPostWritten.setVisibility(View.GONE);
                mPostWritten.setText("");
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Upload.this,MainPanel.class));
        finish();
        super.onBackPressed();

    }
}
