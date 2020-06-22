package com.pluslatestmemes.MainIssues;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

/**
 * Created by Mwarabu on 3/19/2018.
 */

public class PostId {

    @Exclude
    public String PostId;

    public <T extends PostId> T withId(@NonNull final String id) {
        this.PostId = id;
        return (T) this;
    }

}
