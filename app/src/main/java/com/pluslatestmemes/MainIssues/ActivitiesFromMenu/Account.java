package com.pluslatestmemes.MainIssues.ActivitiesFromMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pluslatestmemes.MainIssues.MainPanel;
import com.pluslatestmemes.MainIssues.ZoomingImage;
import com.pluslatestmemes.ProfileSettings;
import com.pluslatestmemes.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class Account extends AppCompatActivity {
    private CircleImageView mProfile;
    private TextView mUsername,mBio,mPhone;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private String user_id,status = "",message;
    private FloatingActionButton mChange;
    private AdView mAdView;
    private Uri imageUri;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
         auth = FirebaseAuth.getInstance();

         firebaseFirestore = FirebaseFirestore.getInstance();
         storageReference = FirebaseStorage.getInstance().getReference();
         progressDialog = new ProgressDialog(this);
         user_id = getIntent().getExtras().getString("user_id");

         mProfile = findViewById(R.id.image_profile_account);
         mUsername = findViewById(R.id.tv_username_account);
         mBio  = findViewById(R.id.tv_bio_account);
         mPhone  = findViewById(R.id.tv_phone_account);
         mChange = findViewById(R.id.float_pickimage_account);


        MobileAds.initialize(this,getString(R.string.appid));
        mAdView = findViewById(R.id.adView_account);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

         if (user_id.equals(auth.getCurrentUser().getUid())){
             mUsername.setEnabled(true);
             mBio.setEnabled(true);
             mChange.setVisibility(View.VISIBLE);

         }

         //getting the details
        gettingUserDeatails(user_id);
        //pickimg image

        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(Account.this);
            }
        });

        mUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status = "username";
                message = "Enter username of your choice";
                showDialog(status,message,user_id);

            }
        });

        mBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status = "status Bio";
                message = "Enter status of your choice";
                showDialog(status,message,user_id);

            }
        });

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(Account.this);
                dialog.setContentView(R.layout.dialog_item_owner_profile);
                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));

                final ImageView userProfile = (ImageView)dialog.findViewById(R.id.user_imag_profile_dialog);
                final TextView Username = (TextView)dialog.findViewById(R.id.tv_username_dialog);

                firebaseFirestore.collection("Users").document(user_id).collection("Profile")
                        .document(user_id).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    if (task.getResult().exists())
                                    {
                                        final String imageUrl = task.getResult().getString("imageUrl");
                                        final String username = task.getResult().getString("username");

                                        Username.setText(username);
                                        RequestOptions requestOptions = new RequestOptions();
                                        requestOptions.centerCrop();

                                        Glide.with(getApplicationContext()).applyDefaultRequestOptions(requestOptions)
                                                .load(imageUrl).into(userProfile);

                                        //IMAGE ONCLICK
                                        userProfile.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent i = new Intent(Account.this, ZoomingImage.class);
                                                i.putExtra("imageUrl",imageUrl);
                                                startActivity(i);
                                                dialog.dismiss();

                                            }
                                        });
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Account.this, "Something went wrong: \n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.show();
            }
        });
    }

    private void  gettingUserDeatails(String user_id){
        firebaseFirestore.collection("Users").document(user_id).collection("Profile")
                .document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        String imageUrl =task.getResult().getString("imageUrl");
                        String username = task.getResult().getString("username");
                        String bio = task.getResult().getString("bio");
                        String phone = auth.getCurrentUser().getPhoneNumber();

                        //setting the values
                        mUsername.setText(username);
                        mBio.setText(bio);
                        mPhone.setText(phone);
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.centerCrop();

                        Glide.with(getApplicationContext()).applyDefaultRequestOptions(requestOptions)
                                .load(imageUrl).into(mProfile);
                    }
                }
            }
        });
    }

    private void uploadingUserProfile(final String user_id, final String username){

        progressDialog.setTitle("Updating");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String randomImageName = username+ UUID.randomUUID().toString();
        final StorageReference reference = storageReference.child("ProfileImages").child(user_id).child(randomImageName+".jpg");

        reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl = uri.toString();
                        Map<String, Object> stringMap = new HashMap<>();
                        stringMap.put("imageUrl",imageUrl);

                        firebaseFirestore.collection("Users").document(user_id).collection("Profile")
                                .document(user_id).update(stringMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(Account.this, "Updated", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    gettingUserDeatails(user_id);

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(Account.this, "Something went wrong..!\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

    }

    private void showDialog(final String status, String message, final String user_id){
       AlertDialog.Builder builder = new AlertDialog.Builder(Account.this);
                builder.setTitle(status);
                builder.setMessage(message);
        final EditText editText = new EditText(this);
        builder.setView(editText);

        if (status.equals("username")){
            editText.setText(mUsername.getText().toString().trim());
        }else {
            editText.setText(mBio.getText().toString().trim());
        }
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                progressDialog.setTitle("Updating");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                String tochange = editText.getText().toString().trim();
                if (status.equals("username")){
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("username",tochange);
                    firebaseFirestore.collection("Users").document(user_id).collection("Profile")
                            .document(user_id).update(objectMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(Account.this, "Updated", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                gettingUserDeatails(user_id);
                            }
                        }
                    });
                }else {
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("bio",tochange);
                    firebaseFirestore.collection("Users").document(user_id).collection("Profile")
                            .document(user_id).update(objectMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(Account.this, "Updated", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                gettingUserDeatails(user_id);
                            }
                        }
                    });
                }
            }
        });
        builder.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                mProfile.setImageURI(imageUri);
                uploadingUserProfile(user_id,mUsername.getText().toString());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
