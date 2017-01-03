package com.inpen.shuffle.playback;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.inpen.shuffle.model.MutableMediaMetadata;
import com.inpen.shuffle.model.repositories.QueueRepository;
import com.inpen.shuffle.utility.LogHelper;

/**
 * Created by Abhishek on 12/27/2016.
 */

public class MusicService extends Service
        implements PlaybackManager.PlaybackServiceCallback {

    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = "com.inpen.shuffle.ACTION_CMD";
    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";
    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should start with service (see {@link #onStartCommand})
    public static final String CMD_PLAY = "CMD_PLAY";
    ///////////////////////////////////////////////////////////////////////////
    // Static fields
    ///////////////////////////////////////////////////////////////////////////
    private static final String TAG = LogHelper.makeLogTag(MusicService.class);
    ///////////////////////////////////////////////////////////////////////////
    // Regular fields and methods
    ///////////////////////////////////////////////////////////////////////////
    private IBinder mMusicBinder = new MusicServiceBinder();
    private boolean mIsBound;

    private MediaSessionCompat mMediaSession;
    private PlaybackManager mPlaybackManager;
    private MediaNotificationManager mMediaNotificationManager;
    private QueueRepository mQueueRepository;


    @Override
    public void onCreate() {
        super.onCreate();

        mMediaSession = new MediaSessionCompat(this,
                MusicService.class.getSimpleName()); // TODO use other constructor for mediabuttonreciever {@Link "https://www.youtube.com/watch?v=FBC1FgWe5X4" }

        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mQueueRepository = QueueRepository.getInstance();

        // set metadatachangedobserver to to update session metadata
        // to that session and hence other components to be informed when metadata is changed from queueRepo
        // Such as when rating changed
        mQueueRepository.setmQueueMetadataCallbackObserver(new QueueRepository.QueueMetadataCallback() {
            @Override
            public void onMetadataChanged() {
                mMediaSession.setMetadata(mQueueRepository.getCurrentSong().metadata);
            }
        });

        // Create and initialize PlaybackManager
        mPlaybackManager = new PlaybackManager(this, mQueueRepository, new Playback(this), this);
        mMediaSession.setCallback(mPlaybackManager.getMediaSessionCallback());

        try {
            mMediaNotificationManager = new MediaNotificationManager(this);
        } catch (RemoteException e) {
            e.printStackTrace();
            LogHelper.e(TAG, "Cannot initialize notification manager! " + e);
        }
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action) && CMD_PLAY.equals(command)) {
                mPlaybackManager.handlePlayRequest();
            }
        } else {
            // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
            MediaButtonReceiver.handleIntent(mMediaSession, startIntent);
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogHelper.d(TAG, "onDestroy");

        // Service is being killed, so make sure we release our resources
        mPlaybackManager.handleStopRequest(null);
        mMediaNotificationManager.stopNotification();

        mMediaSession.release();
    }

    ///////////////////////////////////////////////////////////////////////////
    // private methods
    ///////////////////////////////////////////////////////////////////////////

    private void updateSessionMetadata() {
        MutableMediaMetadata mutableMediaMetadata = mQueueRepository.getCurrentSong();

        if (mutableMediaMetadata == null) {
            mMediaSession.setMetadata(null);
            return;
        }

        if (mMediaSession.getController().getMetadata() == null ||
                !mMediaSession.getController().getMetadata().equals(mutableMediaMetadata.metadata)) {
            mMediaSession.setMetadata(mutableMediaMetadata.metadata);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // PlaybackServiceCallbacks
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onPlaybackStart() {
        updateSessionMetadata();

        if (!mMediaSession.isActive()) {
            mMediaSession.setActive(true);

//      TODO notification should update itself automatically, however if doesn't work uncomment this
//            try {
//                mMediaMediaNotificationManager.startNotification();
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
        }
    }

    @Override
    public void onNotificationRequired() {
        try {
            mMediaNotificationManager.startNotification();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlaybackStop() {
        stopForeground(true);
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mMediaSession.setPlaybackState(newState);
    }


    ///////////////////////////////////////////////////////////////////////////
    // public methods, to be called
    ///////////////////////////////////////////////////////////////////////////

    public MediaSessionCompat.Token getSessionToken() {
        return mMediaSession.getSessionToken();
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
