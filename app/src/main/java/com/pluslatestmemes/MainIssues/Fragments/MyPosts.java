package com.pluslatestmemes.MainIssues.Fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.pluslatestmemes.MainIssues.PostModelAdapter;
import com.pluslatestmemes.MainIssues.PostingAdapter;
import com.pluslatestmemes.R;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyPosts extends Fragment {
    private View view;
    private PostingAdapter postingAdapter;
    private List<PostModelAdapter> postModelAdapters;
    private DocumentSnapshot lastVisible;
    private boolean isFirstPageFirstLoad = true;
    private RecyclerView mRecyclerView;
    private FirebaseAuth auth;
    private String user_id;
    private FirebaseFirestore firebaseFirestore;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int counter;

    private FloatingActionButton floatingActionButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_my_posts, container, false);

        postModelAdapters = new ArrayList<>();
        postingAdapter = new PostingAdapter(postModelAdapters);

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_myposts);

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        user_id = auth.getCurrentUser().getUid();

        mRecyclerView = view.findViewById(R.id.recycler_myposts_settings);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(postingAdapter);

        //interstitialad
        /*mInterstitialAd = new PublisherInterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial));
        PublisherAdRequest adRequest1 = new PublisherAdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest1);*/

        /*new CountDownTimer(5000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Toast.makeText(MyPosts.this, "Ad loading in 3 Seconds: "+String.valueOf(counter), Toast.LENGTH_SHORT).show();
                counter++;
            }
            @Override
            public void onFinish() {
                if (mInterstitialAd.isLoaded()){
                    mInterstitialAd.show();
                }
            }
        }.start();*/

        loadPosts();



        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
                postingAdapter.notifyDataSetChanged();
                mRecyclerView.setAdapter(postingAdapter);
            }
        });


        return view;
    }


    private void loadPosts(){

        if (auth.getCurrentUser() != null) {

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);


                    Boolean aBoolean = !recyclerView.canScrollVertically(1);

                    if (aBoolean) {
                        loadMorePosts();
                    }
                }
            });

            Query firstQuery =firebaseFirestore.collection("AllPosts").whereEqualTo("user_id",user_id);
            firstQuery.addSnapshotListener( new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {


                    if (!queryDocumentSnapshots.isEmpty()) {

                        if (isFirstPageFirstLoad) {
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                        }


                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                String documentId = documentChange.getDocument().getId();
                                PostModelAdapter postModelAdapter = documentChange.getDocument().toObject(PostModelAdapter.class).withId(documentId);

                                if (isFirstPageFirstLoad) {
                                    postModelAdapters.add(postModelAdapter);
                                } else {
                                    postModelAdapters.add(0, postModelAdapter);
                                }
                                postingAdapter.notifyDataSetChanged();
                            }
                        }

                        isFirstPageFirstLoad = false;
                    }
                }
            });

        }
    }

    private void loadMorePosts() {
        Query secondQuery = firebaseFirestore.collection("AllPosts")
                .whereEqualTo("user_id",user_id)
                .startAfter(lastVisible)
                .limit(3);
        secondQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            String documentId = documentChange.getDocument().getId();
                            PostModelAdapter postModelAdapter = documentChange.getDocument().toObject(PostModelAdapter.class).withId(documentId);
                            postModelAdapters.add(postModelAdapter);
                            postingAdapter.notifyDataSetChanged();
                        }
                    }
                }


            }
        });
    }

}
