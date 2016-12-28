package com.inpen.shuffle.playerscreen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;

import com.inpen.shuffle.playback.MusicService;
import com.inpen.shuffle.utility.LogHelper;

import org.greenrobot.eventbus.EventBus;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 12/28/2016.
 */

public class PlayerActivityPresenter implements PlayerActivityContract.PlayerActivityListener {

    ///////////////////////////////////////////////////////////////////////////
    // Regular fields and methods
    ///////////////////////////////////////////////////////////////////////////

    private static final String LOG_TAG = LogHelper.makeLogTag(PlayerActivityPresenter.class);

    ///////////////////////////////////////////////////////////////////////////
    // Regular fields and methods
    ///////////////////////////////////////////////////////////////////////////
    public PlayerActivityContract.PlayerActivityView mPlayerActivityView;
    private boolean mBound;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.MusicServiceBinder binder = (MusicService.MusicServiceBinder) service;
            MusicService musicService = binder.getService();
            mBound = true;

            MediaSessionCompat.Token token = musicService.getSessionToken();

            try {
                mPlayerActivityView.connectToSession(token);

                EventBus.getDefault()
                        .post(new MusicServiceConnectedEvent(token));
            } catch (RemoteException e) {
                e.printStackTrace();
                LogHelper.e(LOG_TAG, "could not connectToSession: " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public PlayerActivityPresenter(@NonNull PlayerActivityContract.PlayerActivityView playerActivityView) {
        mPlayerActivityView = checkNotNull(playerActivityView);
    }

    @Override
    public void init(Context context) {
        connectToService(context);
    }

    @Override
    public void stop(Context context) {
        // Unbind from the service
        if (mBound) {
            EventBus.getDefault()
                    .post(new MusicServiceDisconnectedEvent());

            context.unbindService(mConnection);
            mBound = false;
        }
    }

    private void connectToService(Context context) {
        Intent serviceIntent = new Intent(context, MusicService.class);
        context.startService(serviceIntent);
        context.bindService(serviceIntent, mConnection, 0);
    }

    public class MusicServiceConnectedEvent {

        private final MediaSessionCompat.Token mediaSessionToken;

        public MusicServiceConnectedEvent(MediaSessionCompat.Token token) {
            mediaSessionToken = token;
        }
    }

    public class MusicServiceDisconnectedEvent {
    }
}
