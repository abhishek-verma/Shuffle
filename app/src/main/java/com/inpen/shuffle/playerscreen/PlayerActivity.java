package com.inpen.shuffle.playerscreen;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;

import com.inpen.shuffle.R;

public class PlayerActivity extends AppCompatActivity
        implements PlayerActivityContract.PlayerActivityView {

    private PlayerActivityPresenter mPlayerActivityPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPlayerActivityPresenter = new PlayerActivityPresenter(this);
        mPlayerActivityPresenter.init(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlayerActivityPresenter.stop(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods from PlayerActivityView interface
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        setSupportMediaController(mediaController);
    }
}
