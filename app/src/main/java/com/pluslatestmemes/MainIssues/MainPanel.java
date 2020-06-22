package com.pluslatestmemes.MainIssues;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pluslatestmemes.MainActivity;
import com.pluslatestmemes.MainIssues.ActivitiesFromMenu.Account;
import com.pluslatestmemes.MainIssues.ActivitiesFromMenu.MemeGenerator;
import com.pluslatestmemes.MainIssues.Fragments.PagerAdapter;
import com.pluslatestmemes.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainPanel extends AppCompatActivity {
    private Toolbar toolbar;
    private CircleImageView mProfileImage;
    private TextView mUsername;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_panel);

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user  = auth.getCurrentUser();

        if (user == null){
            startActivity(new Intent(MainPanel.this, MainActivity.class));
        }else { user_id = auth.getCurrentUser().getUid();
            gettingUserDeatails(user_id);
            final TabLayout tabLayout = findViewById(R.id.tab_layout);
            mProfileImage = findViewById(R.id.image_profile_mainpanel);
            mUsername = findViewById(R.id.tv_username_mainpanel);
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            //setting tabs
            tabLayout.addTab(tabLayout.newTab().setText("Memes"));
            tabLayout.addTab(tabLayout.newTab().setText("My Memes"));
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

            final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
            final PagerAdapter adapter = new PagerAdapter
                    (getSupportFragmentManager(), tabLayout.getTabCount());
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });


            MobileAds.initialize(this, getString(R.string.appid));
            mAdView = findViewById(R.id.adView_mainpanel);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial));
            mInterstitialAd.loadAd(new AdRequest.Builder().build());

            if (mInterstitialAd.isLoaded()){
                mInterstitialAd.show();
            }

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mInterstitialAd.show();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when the ad is displayed.
                }

                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                @Override
                public void onAdClosed() {
                }
            });

            mProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialog = new Dialog(MainPanel.this);
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
                                                    Intent i = new Intent(MainPanel.this, ZoomingImage.class);
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
                            Toast.makeText(MainPanel.this, "Something went wrong: \n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    dialog.show();
                }
            });

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_account){
            Intent intent = new Intent(MainPanel.this, Account.class);
            intent.putExtra("user_id",user_id);
            startActivity(intent);
        }

        if (id == R.id.action_generate){
            Intent intent = new Intent(MainPanel.this, MemeGenerator.class);
            startActivity(intent);
        }

        if (id == R.id.action_addpost){

            startActivity(new Intent(MainPanel.this,Upload.class));
            finish();
        }
        if (id == R.id.action_share){

            String message = "Hi....!! You can now use 1000+ Memes App to upload or get latest and trending memes. For more info follow this link: https://play.google.com/store/apps/details?id=com.clemmarketplace";

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,message);
            sendIntent.setType("text/plain");
            Intent.createChooser(sendIntent,"Share Clem International app via");
            startActivity(sendIntent);
        }

        if (id == R.id.action_rateus){
            //going to play store
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.pluslatestmemes")));
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onStart() {
        super.onStart();
        if (user !=null){
            gettingUserDeatails(user_id);
        }else {
            finish();
            startActivity(new Intent(MainPanel.this, MainActivity.class));
        }
    }

}


