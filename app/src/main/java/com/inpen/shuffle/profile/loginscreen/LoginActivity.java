package com.inpen.shuffle.profile.loginscreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseUser;
import com.inpen.shuffle.R;

public class LoginActivity
        extends AppCompatActivity
        implements LoginContract.LoginView {

    private GoogleApiClient mGoogleApiClient;
    private LoginPresenter mLoginListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginListener = new LoginPresenter(this);
        mLoginListener.init();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(
                        this/*Activity*/,
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                mLoginListener.signInFailed();
                            }
                        })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLoginListener.signInResult(requestCode, resultCode, data);
    }

    @Override
    public void signIn(int signInRequestCode) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, signInRequestCode);
    }

    @Override
    public void signOut() {

    }

    @Override
    public void showSignedInView(FirebaseUser currentUser) {

    }

    @Override
    public void showSignedOutView() {

    }
}
