package com.inpen.shuffle.profile.loginscreen;

import android.content.Intent;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Abhishek on 7/26/2017.
 */

public interface LoginContract {

    interface LoginView {

        void signIn(int signInRequestCode);

        void signOut();

        void showSignedInView(FirebaseUser currentUser);

        void showSignedOutView();
    }

    interface LoginListener {

        void init();

        void signInResult(int requestCode, int resultCode, Intent result);

        void signedOut();

        void signInFailed();
    }
}
