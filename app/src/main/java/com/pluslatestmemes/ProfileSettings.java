package com.pluslatestmemes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pluslatestmemes.MainIssues.MainPanel;
import com.pluslatestmemes.database.DatabaseHelper;
import com.pluslatestmemes.database.User;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSettings extends AppCompatActivity {
    private Button mProceed;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private EditText mUsername;
    private ProgressDialog progressDialog;
    private StorageReference storageReference;
    private CircleImageView mProfileImage;
    private CheckBox checkBox;
    private TextView mAgreePolicy;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user_id = auth.getCurrentUser().getUid();
        progressDialog = new ProgressDialog(this);


        gettingUserDeatails(user_id);

        //views
        mUsername = findViewById(R.id.edt_username_profile_settings);
        mProfileImage = findViewById(R.id.image_profile_settings);
        mProceed = findViewById(R.id.btn_proceed_profile_settings);
        mAgreePolicy = findViewById(R.id.tv_agree);
        checkBox = findViewById(R.id.check_agree);

        mAgreePolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        //picking image
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(ProfileSettings.this);
            }
        });

        mProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = mUsername.getText().toString().trim();
                if (TextUtils.isEmpty(username)){
                    mUsername.setError("Enter username..!");
                    mUsername.findFocus();
                }else {

                    firebaseFirestore.collection("Users").document(user_id).collection("Profile")
                            .document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                if (!task.getResult().exists()){
                                    if (imageUri !=null){
                                    if (checkBox.isChecked()){
                                        progressDialog.setCancelable(false);
                                        progressDialog.setTitle("Creating..");
                                        progressDialog.setMessage("Creating profile please wait...");
                                        progressDialog.show();
                                        uploadingUserProfile(user_id,username);
                                    }else {
                                        Toast.makeText(ProfileSettings.this, "Please confirm if you agree with our privacy policy...!", Toast.LENGTH_SHORT).show();
                                    }

                                }else {
                                    Toast.makeText(ProfileSettings.this, "Pick profile image..!", Toast.LENGTH_SHORT).show();
                                }

                                }else {
                                    updatingChanges(user_id);
                                }
                            }
                        }
                    });

                }
            }
        });
    }

    private void uploadingUserProfile(final String user_id, final String username){

        String randomImageName = username+ UUID.randomUUID().toString();
        final StorageReference reference = storageReference.child("ProfileImages").child(user_id).child(randomImageName+".jpg");

        reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl = uri.toString();
                        Map<String, String> stringMap = new HashMap<>();
                        stringMap.put("username",username);
                        stringMap.put("bio","Hey there am using 1000+ memes");
                        stringMap.put("imageUrl",imageUrl);

                        firebaseFirestore.collection("Users").document(user_id).collection("Profile")
                                .document(user_id).set(stringMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(ProfileSettings.this, "Profile Successfully created.", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    addingUserToDb(user_id);
                                    finish();
                                    startActivity(new Intent(ProfileSettings.this, MainPanel.class));

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(ProfileSettings.this, "Something went wrong..!\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

    }






    @Override
    protected void onStart() {
        super.onStart();


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                mProfileImage.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void addingUserToDb(String user_id){
        User user = new User();
        user.setUser_id(user_id);
        DatabaseHelper databaseHelper = new DatabaseHelper(ProfileSettings.this);
        if (!databaseHelper.checkUser(user_id)){
            databaseHelper.addUser(user);
        }
    }
    private void showDialog(){
        final Dialog dialog = new Dialog(ProfileSettings.this);
        dialog.setContentView(R.layout.dialog_agreement);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView mTerms =dialog.findViewById(R.id.tv_terms);

        mTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://cleminternational.000webhostapp.com/clemourls/clemourls/PlusMemes.html");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        dialog.show();
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

                        //setting the values
                        mUsername.setText(username);
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.centerCrop();

                        Glide.with(getApplicationContext()).applyDefaultRequestOptions(requestOptions)
                                .load(imageUrl).into(mProfileImage);
                    }
                }
            }
        });
    }

    private void updatinguaernam(String user_id){
        Map<String ,Object> objectMap = new HashMap<>();
        objectMap.put("username",mUsername.getText().toString().trim());
        firebaseFirestore.collection("Users").document(user_id).collection("Profile")
                .document(user_id).update(objectMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    finish();
                    startActivity(new Intent(ProfileSettings.this, MainPanel.class));
                }else {
                    Toast.makeText(ProfileSettings.this,
                            "Something went wrong..!\n"+task.getException()
                                    .getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void updatingwithImage(final String user_id){
        String randomImageName = mUsername.getText().toString().trim()+ UUID.randomUUID().toString();
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
                                    progressDialog.dismiss();
                                    finish();
                                    startActivity(new Intent(ProfileSettings.this, MainPanel.class));

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(ProfileSettings.this, "Something went wrong..!\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

    private void updatingChanges(String user_id){
        if (imageUri !=null){
            updatingwithImage(user_id);
        }else {
            updatinguaernam(user_id);
        }
    }
}
