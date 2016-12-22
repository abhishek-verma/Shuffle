package com.inpen.shuffle.playback;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaSessionCompat;

import com.inpen.shuffle.utility.LogHelper;

public class MusicService extends Service {

    private static final String TAG = LogHelper.makeLogTag(MusicService.class);

    ///////////////////////////////////////////////////////////////////////////
    // Regular fields and methods
    ///////////////////////////////////////////////////////////////////////////
    private IBinder mMusicBinder = new MusicServiceBinder();

    private boolean mIsBound;

    private PlaybackManager mPlaybackManager;
    private MediaNotificationManager mMediaNotificationManager;


    private MediaSessionCompat mSession;


    public static void startService(Context context) {
        context.startService(new Intent(context, MusicService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Start a new MediaSession
        mSession = new MediaSessionCompat(this, "MusicService");
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Create and initialize PlaybackManager
        mPlaybackManager = new PlaybackManager(this);
        mSession.setCallback(mPlaybackManager.getMediaSessionCallback());

        // Create Notification Manager
        try {
            mMediaNotificationManager = new MediaNotificationManager(this);
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMediaNotificationManager.startNotification();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogHelper.d(TAG, "onDestroy");

        mPlaybackManager.handleStopRequest();
        mMediaNotificationManager.stopNotification();

        mSession.release();
    }

    ///////////////////////////////////////////////////////////////////////////
    // public methods, to be called
    ///////////////////////////////////////////////////////////////////////////

    public MediaSessionCompat.Token getSessionToken() {
        return mSession.getSessionToken();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Binding related
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public IBinder onBind(Intent intent) {
        mIsBound = true;
        return mMusicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mIsBound = false;
        return true;
    }

    /*
     * Binding settings
     */
    public class MusicServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

}
