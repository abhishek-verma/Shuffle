package com.inpen.shuffle.profile.loginscreen;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.inpen.shuffle.utility.LogHelper;

/**
 * Created by Abhishek on 7/26/2017.
 */

class LoginPresenter
        implements LoginContract.LoginListener {

    private static final String LOG_TAG = LogHelper.makeLogTag(LoginPresenter.class);
    private static final int RC_SIGN_IN = 1;

    private final LoginActivity mLoginView;
    private final AppCompatActivity mAppCompatActivity;
    private FirebaseAuth mAuth;

    public LoginPresenter(LoginActivity loginActivity) {
        mLoginView = loginActivity;
        mAppCompatActivity = loginActivity;
    }

    @Override
    public void init() {
        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mLoginView.showSignedInView(currentUser);
        } else {
            mLoginView.showSignedOutView();
        }
    }

    @Override
    public void signInResult(int requestCode, int resultCode, Intent data) {

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                signInFailed();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        LogHelper.d(LOG_TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(
                        mAppCompatActivity,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    LogHelper.d(LOG_TAG, "signInWithCredential:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    mLoginView.showSignedInView(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    LogHelper.w(LOG_TAG, "signInWithCredential:failure", task.getException());
                                    Toast.makeText(mAppCompatActivity, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    signInFailed();
                                }
                            }
                        });
    }

    @Override
    public void signedOut() {
        mLoginView.showSignedOutView();
    }

    @Override
    public void signInFailed() {

    }
}
