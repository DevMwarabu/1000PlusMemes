package com.pluslatestmemes.MainIssues.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class Memes extends Fragment {
    private View view;
    private Toolbar toolbar;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private RecyclerView mRecyclerView;
    private String user_id;
    private PostingAdapter postingAdapter;
    private List<PostModelAdapter> postModelAdapters;
    private DocumentSnapshot lastVisible;
    private boolean isFirstPageFirstLoad = true;
    public static Activity activity;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mAccountName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_memes, container, false);

        activity = this.getActivity();

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user_id = auth.getCurrentUser().getUid();

        postModelAdapters = new ArrayList<>();
        postingAdapter = new PostingAdapter(postModelAdapters);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_main);

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(postingAdapter);

        //getting post
        loadPosts();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
                postingAdapter.notifyDataSetChanged();

            }
        });

        return view;
    }

    private void loadPosts() {

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

            Query firstQuery = firebaseFirestore.collection("AllPosts")
                    .orderBy("timeStamp", Query.Direction.DESCENDING);
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
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);
        secondQuery.addSnapshotListener( new EventListener<QuerySnapshot>() {
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
