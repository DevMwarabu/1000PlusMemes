package com.pluslatestmemes.MainIssues;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.pluslatestmemes.MainIssues.ActivitiesFromMenu.Account;
import com.pluslatestmemes.ProfileSettings;
import com.pluslatestmemes.R;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import me.grantland.widget.AutofitTextView;

public class PostingAdapter extends RecyclerView.Adapter<PostingAdapter.ViewHolder> {
    private Context context,applicationContext;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private List<PostModelAdapter> postModelAdapters;
    private Activity activity;

    public PostingAdapter(List<PostModelAdapter> postModelAdapters) {
        this.postModelAdapters = postModelAdapters;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_display, parent, false);
        applicationContext = parent.getContext().getApplicationContext();
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final String user_id_poster = postModelAdapters.get(position).getUser_id();
        final String meme = postModelAdapters.get(position).getMeme();
        final String imageUrlPosted = postModelAdapters.get(position).getImageUrl();
        final Date timeStamp = postModelAdapters.get(position).getTimeStamp();
        final String documentid =  postModelAdapters.get(position).PostId;
        String current_user_id = auth.getCurrentUser().getUid();


        if (context == activity){

            firebaseFirestore.collection("Users").document(user_id_poster).collection("Profile")
                    .document(user_id_poster).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if (task.getResult().exists()){
                            final String username = task.getResult().getString("username");

                            holder.mUserProfiles.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //sending data kwa uko
                                    Intent i =  new Intent(context, Account.class);
                                    i.putExtra("user_id",user_id_poster);
                                    view.getContext().startActivity(i);

                                }
                            });

                            //deleting
                            holder.mDelete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    new AlertDialog.Builder(context)
                                            .setCancelable(false)
                                            .setTitle("Deleting..")
                                            .setMessage("Are you sure you want to delete this posts?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    firebaseFirestore.collection("AllPosts").document(documentid)
                                                            .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Toast.makeText(context, "Deleted..", Toast.LENGTH_SHORT).show();
                                                                removeItem(position);
                                                            }
                                                        }
                                                    });
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            })
                                            .show();

                                }
                            });

                            //uploading our image to db
                            firebaseFirestore.collection("Users").document(user_id_poster).collection("Profile")
                                    .document(user_id_poster).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        if (task.getResult().exists()){
                                            String imageUrl = task.getResult().getString("imageUrl");
                                            //seeting values now
                                            holder.settingDetails(username,imageUrl,imageUrlPosted,meme,timeStamp,documentid);
                                            holder.mLike.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    firebaseFirestore.collection("AllPosts").document(documentid)
                                                            .collection("Likes").document(auth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                if (task.getResult().exists()) {

                                                                    firebaseFirestore.collection("AllPosts").document(documentid)
                                                                            .collection("Likes").document(auth.getCurrentUser().getUid()).delete();
                                                                } else {
                                                                    Map<String, Object> objectMap = new HashMap<>();
                                                                    objectMap.put("timeStamp", FieldValue.serverTimestamp());
                                                                    firebaseFirestore.collection("AllPosts").document(documentid)
                                                                            .collection("Likes").document(auth.getCurrentUser().getUid()).set(objectMap);

                                                                }
                                                            }
                                                        }
                                                    });

                                                }
                                            });
                                        }else {
                                            String imageUrl = "no_image";
                                            holder.settingDetails(username,imageUrl,imageUrlPosted,meme,timeStamp,documentid);
                                            holder.mLike.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    firebaseFirestore.collection("AllPosts").document(documentid)
                                                            .collection("Likes").document(auth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                if (task.getResult().exists()) {

                                                                    firebaseFirestore.collection("AllPosts").document(documentid)
                                                                            .collection("Likes").document(auth.getCurrentUser().getUid()).delete();
                                                                } else {
                                                                    Map<String, Object> objectMap = new HashMap<>();
                                                                    objectMap.put("timeStamp", FieldValue.serverTimestamp());
                                                                    firebaseFirestore.collection("AllPosts").document(documentid)
                                                                            .collection("Likes").document(auth.getCurrentUser().getUid()).set(objectMap);

                                                                }
                                                            }
                                                        }
                                                    });

                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }else {
            firebaseFirestore.collection("Users").document(user_id_poster).collection("Profile")
                    .document(user_id_poster).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if (task.getResult().exists()){
                            final String username = task.getResult().getString("username");
                            //getting imageUser

                            holder.mUserProfiles.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //sending data kwa uko
                                    Intent i =  new Intent(context, Account.class);
                                    i.putExtra("user_id",user_id_poster);
                                    i.putExtra("follow","adapter");
                                    view.getContext().startActivity(i);

                                }
                            });

                            //uploading our image to db
                            firebaseFirestore.collection("Users").document(user_id_poster).collection("Profile")
                                    .document(user_id_poster).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        if (task.getResult().exists()){
                                            String imageUrl = task.getResult().getString("imageUrl");
                                            //seeting values now
                                            holder.settingDetails(username,imageUrl,imageUrlPosted,meme,timeStamp,documentid);
                                            holder.mLike.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    firebaseFirestore.collection("AllPosts").document(documentid)
                                                            .collection("Likes").document(auth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                if (task.getResult().exists()) {

                                                                    firebaseFirestore.collection("AllPosts").document(documentid)
                                                                            .collection("Likes").document(auth.getCurrentUser().getUid()).delete();
                                                                } else {
                                                                    Map<String, Object> objectMap = new HashMap<>();
                                                                    objectMap.put("timeStamp", FieldValue.serverTimestamp());
                                                                    firebaseFirestore.collection("AllPosts").document(documentid)
                                                                            .collection("Likes").document(auth.getCurrentUser().getUid()).set(objectMap);

                                                                }
                                                            }
                                                        }
                                                    });

                                                }
                                            });
                                        }else {
                                            String imageUrl = "no_image";
                                            holder.settingDetails(username,imageUrl,imageUrlPosted,meme,timeStamp,documentid);
                                            holder.mLike.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    firebaseFirestore.collection("AllPosts").document(documentid)
                                                            .collection("Likes").document(auth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                if (task.getResult().exists()) {

                                                                    firebaseFirestore.collection("AllPosts").document(documentid)
                                                                            .collection("Likes").document(auth.getCurrentUser().getUid()).delete();
                                                                } else {
                                                                    Map<String, Object> objectMap = new HashMap<>();
                                                                    objectMap.put("timeStamp", FieldValue.serverTimestamp());
                                                                    firebaseFirestore.collection("AllPosts").document(documentid)
                                                                            .collection("Likes").document(auth.getCurrentUser().getUid()).set(objectMap);

                                                                }
                                                            }
                                                        }
                                                    });

                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }

        if (user_id_poster.equals(current_user_id)){
            holder.mDelete.setVisibility(View.VISIBLE);
        }

        //deleting
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setTitle("Deleting..")
                        .setMessage("Are you sure you want to delete this posts?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                firebaseFirestore.collection("AllPosts").document(documentid)
                                        .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(context, "Deleted..", Toast.LENGTH_SHORT).show();
                                            removeItem(position);
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();

            }
        });

        holder.mImagePosted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context,ZoomingImage.class);
                i.putExtra("imageUrl",imageUrlPosted);
                view.getContext().startActivity(i);
            }
        });


    }

    @Override
    public int getItemCount() {
        return postModelAdapters.size();
    }
    public void removeItem(int position) {
        postModelAdapters.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUsername,mLikeText,mTimeText;
        private AutofitTextView mTextPosted;
        private CircleImageView mUserProfile;
        private ImageView mImageLikes,mDelete;
        private ImageView mImagePosted;
        private LinearLayout mLike,mUserProfiles;
        private View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            view = itemView;
            mUserProfile = view.findViewById(R.id.image_profile_poster);
            mUserProfiles = view.findViewById(R.id.linearUserProfile);
            mImagePosted = view.findViewById(R.id.image_posted_postitem);
            mLike = view.findViewById(R.id.linear_likes);
            mUsername = view.findViewById(R.id.tv_username_poster);
            mTextPosted = view.findViewById(R.id.tv_thememe_poster);
            mLikeText = view.findViewById(R.id.tv_likes);
            mTimeText = view.findViewById(R.id.tv_time_post_item);
            mImageLikes = view.findViewById(R.id.image_likes);
            mDelete = view.findViewById(R.id.image_postitem_delete);

        }

        public void settingDetails(String username,String imageUrl,String imageUrlPosted,String memePosted,Date datePosted,String documentId){
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.centerCrop();
            requestOptions.fitCenter();

            if (imageUrl.equals("no_image")){
                requestOptions.placeholder(R.color.colorPrimaryDark);
            }
            Glide.with(applicationContext).applyDefaultRequestOptions(requestOptions).load(imageUrl).into(mUserProfile);
            Glide.with(applicationContext).applyDefaultRequestOptions(requestOptions).load(imageUrlPosted).into(mImagePosted);
            //setting tex values
            mUsername.setText(username);
            mTextPosted.setText(memePosted);

            if (imageUrlPosted.equals("NoImage")){
                mImagePosted.setVisibility(View.GONE);
                mTextPosted.setVisibility(View.VISIBLE);
            }

            if (memePosted.equals("NoText")){
                mImagePosted.setVisibility(View.VISIBLE);
                mTextPosted.setVisibility(View.GONE);
            }

            //setting dates

            Date c = Calendar.getInstance().getTime();
            //seting date to milisecs

            long currenttime = c.getTime();
            long diff = currenttime - datePosted.getTime();

            long seconds = diff / 1000;
            long min = seconds / 60;
            long hrs = min / 60;
            long day = hrs / 24;
            long week = day / 7;
            long month = week / 4;
            long year = month / 12;


            if (seconds < 60 && seconds >= 0) {
                mTimeText.setText(seconds + " sec ago");
            } else {

            }
            if (min== 1){

                mTimeText.setText(min + " min ago");
            }
            if (min > 1 && min < 60) {
                mTimeText.setText(min + " mins ago");
            }
            if (hrs > 1 && hrs < 24) {
                mTimeText.setText(hrs + " hrs ago");
            }
            if (hrs == 1) {
                mTimeText.setText(hrs + " hr ago");
            }
            if (day > 1 && day < 7) {
                mTimeText.setText(day + " days ago");
            }
            if (day == 1 ) {
                mTimeText.setText(day + " day ago");
            }
            if (week > 1 && week < 4) {
                mTimeText.setText(week + " weeks ago");
            }
            if (week == 1) {
                mTimeText.setText(week + " week ago");
            }
            if (month > 1 && month < 12) {
                mTimeText.setText(month + " months ago");
            }
            if (month == 1) {
                mTimeText.setText(month + " month ago");
            }
            if (year >= 1) {
                mTimeText.setText(year + " yrs ago");
            }
            if (year == 1) {
                mTimeText.setText(year + " yr ago");
            }

            //likes


            Query query1 = firebaseFirestore.collection("AllPosts").document(documentId)
                    .collection("Likes");
            query1.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {


                    int likes = queryDocumentSnapshots.size();

                    if (likes == 1) {
                        mLikeText.setText("" + likes );
                    } else {
                        mLikeText.setText("" + likes );
                    }
                }
            });




            firebaseFirestore.collection("AllPosts").document(documentId)
                    .collection("Likes").document(auth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot.exists()) {
                        mImageLikes.setImageDrawable(context.getDrawable(R.drawable.ic_thumb_up_black_24dp));

                    } else {
                        mImageLikes.setImageDrawable(context.getDrawable(R.drawable.ic_thumb_upbf_black_24dp));

                    }
                }
            });

        }
    }

}
