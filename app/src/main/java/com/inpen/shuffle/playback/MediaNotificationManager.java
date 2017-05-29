package com.inpen.shuffle.playback;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.inpen.shuffle.R;
import com.inpen.shuffle.playerscreen.PlayerActivity;
import com.inpen.shuffle.utility.LogHelper;

/**
 * Created by Abhishek on 12/27/2016.
 */

public class MediaNotificationManager extends BroadcastReceiver {

    public static final String ACTION_PAUSE = "com.inpen.shuffle.pause";
    public static final String ACTION_PLAY = "com.inpen.shuffle.play";
    public static final String ACTION_PREV = "com.inpen.shuffle.prev";
    public static final String ACTION_NEXT = "com.inpen.shuffle.next";
    private static final String TAG = LogHelper.makeLogTag(MediaNotificationManager.class);
    private static final int MAX_ART_WIDTH_ICON = 128;  // pixels
    private static final int MAX_ART_HEIGHT_ICON = 128;  // pixels
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;
    private final NotificationManagerCompat mNotificationManager;
    private final int mNotificationColor;

    private final MusicService mMusicService;
    private final PendingIntent mPauseIntent;
    private final PendingIntent mPlayIntent;
    private final PendingIntent mPreviousIntent;
    private final PendingIntent mNextIntent;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mController;
    private MediaControllerCompat.TransportControls mTransportControls;

    //    private final PendingIntent mRatingIntent;
    private PlaybackStateCompat mPlaybackState;
    private MediaMetadataCompat mMetadata;

    private boolean mStarted = false;

    private final MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            // Set notification to paused or resumed

            mPlaybackState = state;
            LogHelper.d(TAG, "Received new playback state", state);
            if (state.getState() == PlaybackStateCompat.STATE_STOPPED ||
                    state.getState() == PlaybackStateCompat.STATE_NONE) {
                stopNotification();
            } else {
                Notification notification = createNotification();
                if (notification != null) {
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);

            updateNotificationMetadata(metadata);
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();

            LogHelper.d(TAG, "Session was destroyed, resetting to the new session token");
            try {
                updateSessionToken();
            } catch (RemoteException e) {
                LogHelper.e(TAG, e, "could not connect media controller");
            }
        }

    };

    public MediaNotificationManager(MusicService musicService) throws RemoteException {
        mMusicService = musicService;
        updateSessionToken();

        mNotificationColor = mMusicService.getResources().getColor(R.color.dr_grey);


        mNotificationManager = NotificationManagerCompat.from(mMusicService);


        String pkg = mMusicService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mMusicService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mMusicService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mMusicService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mMusicService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        LogHelper.d(TAG, "Received intent with action " + action);
        switch (action) {
            case ACTION_PAUSE:
                mTransportControls.pause();
                break;
            case ACTION_PLAY:
                mTransportControls.play();
                break;
            case ACTION_NEXT:
                mTransportControls.skipToNext();
                break;
            case ACTION_PREV:
                mTransportControls.skipToPrevious();
                break;
            default:
                LogHelper.w(TAG, "Unknown intent ignored. Action=", action);
        }
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before {@link #stopNotification} is called.
     */
    public void startNotification() throws RemoteException {

        if (!mStarted) {
            updateSessionToken();

            mMetadata = mController.getMetadata();
            mPlaybackState = mController.getPlaybackState();

            // The notification must be updated after setting started to true
            Notification notification = createNotification();
            if (notification != null) {
                mController.registerCallback(mControllerCallback);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);
                mMusicService.registerReceiver(this, filter);

                mMusicService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            mController.unregisterCallback(mControllerCallback);
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mMusicService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mMusicService.stopForeground(true);
        }
    }

    /**
     * Update the state based on a change on the session token. Called either when we are running
     * for the first time or when the media session owner has destroyed the session (see {@link
     * android.media.session.MediaController.Callback#onSessionDestroyed()})
     */
    private void updateSessionToken() throws RemoteException {
        MediaSessionCompat.Token freshToken = mMusicService.getSessionToken();
        if (mSessionToken == null && freshToken != null ||
                mSessionToken != null && !mSessionToken.equals(freshToken)) {
            if (mController != null) {
                mController.unregisterCallback(mControllerCallback);
            }
            mSessionToken = freshToken;
            if (mSessionToken != null) {
                mController = new MediaControllerCompat(mMusicService, mSessionToken);
                mTransportControls = mController.getTransportControls();
                if (mStarted) {
                    mController.registerCallback(mControllerCallback);
                }
            }
        }
    }

    private void updateNotificationMetadata(MediaMetadataCompat metadata) {
        mMetadata = metadata;
        LogHelper.d(TAG, "Received new metadata ", metadata);
        Notification notification = createNotification();
        if (notification != null) {
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private Notification createNotification() {
        LogHelper.d(TAG, "updateNotificationMetadata. mMetadata=" + mMetadata);
        if (mMetadata == null || mPlaybackState == null) {
            return null;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mMusicService);
        int skipToPreviousButtonoPosition = 0, playPauseButtonPosition = 0, skipToNextButtonPosition = 0;

        // If skip to previous action is enabled
        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            notificationBuilder.addAction(R.drawable.ic_skip_previous_white_24dp,
                    mMusicService.getString(R.string.label_previous), mPreviousIntent);

            skipToPreviousButtonoPosition = 0;
            // If there is a "skip to previous" button, the play/pause button will
            // be the second one. We need to keep track of it, because the MediaStyle notification
            // requires to specify the index of the buttons (actions) that should be visible
            // when in compact view.
            playPauseButtonPosition = 1;

            skipToNextButtonPosition = 2;
        }

        addPlayPauseAction(notificationBuilder);

        // If skip to next action is enabled
        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            notificationBuilder.addAction(R.drawable.ic_skip_next_white_24dp,
                    mMusicService.getString(R.string.label_next), mNextIntent);
        }

        MediaDescriptionCompat description = mMetadata.getDescription();

        String fetchArtUrl = null;
        Bitmap art = null;

        if (description.getIconUri() != null) {
            // This section assumes the iconUri will be a valid URL formatted String, but
            // it can actually be any valid Android Uri formatted String.
            // async fetch the album art icon
            fetchArtUrl = description.getIconUri().toString();
        }

        // use a placeholder art while the remote art is being downloaded
        art = BitmapFactory.decodeResource(mMusicService.getResources(),
                R.drawable.shuffle_bg);


        notificationBuilder
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(
                                new int[]{skipToPreviousButtonoPosition,
                                        playPauseButtonPosition,
                                        skipToNextButtonPosition})  // show only play/pause in compact view
                        .setMediaSession(mSessionToken))
                .setColor(mNotificationColor)
                .setSmallIcon(R.drawable.ic_shuffle_black_24dp)
                .setLargeIcon(art)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setUsesChronometer(false)
                .setContentIntent(createContentIntent(description))
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle());

        setNotificationPlaybackState(notificationBuilder);
        if (fetchArtUrl != null) {
            fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder);
        }

        return notificationBuilder.build();
    }

    private PendingIntent createContentIntent(MediaDescriptionCompat description) {
        Intent openUI = new Intent(mMusicService, PlayerActivity.class);
//        openUI.putExtra(MusicPlayerActivity.EXTRA_START_FULLSCREEN, true);
//        if (description != null) {
//            openUI.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
//        } ```````
        return PendingIntent.getActivity(mMusicService, REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void addPlayPauseAction(NotificationCompat.Builder builder) {
        LogHelper.d(TAG, "updatePlayPauseAction");
        String label;
        int icon;
        PendingIntent intent;
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            label = mMusicService.getString(R.string.label_pause);
            icon = R.drawable.ic_pause_white_24dp;
            intent = mPauseIntent;

            builder.setOngoing(true);
        } else {
            label = mMusicService.getString(R.string.label_play);
            icon = R.drawable.ic_play_arrow_white_24dp;
            intent = mPlayIntent;

            builder.setOngoing(false);
        }
        builder.addAction(new NotificationCompat.Action(icon, label, intent));
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        LogHelper.d(TAG, "updateNotificationPlaybackState. mPlaybackState=" + mPlaybackState);
        if (mPlaybackState == null || !mStarted) {
            LogHelper.d(TAG, "updateNotificationPlaybackState. cancelling notification!");
            mMusicService.stopForeground(true);
            return;
        }
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING
                && mPlaybackState.getPosition() >= 0) {
            LogHelper.d(TAG, "updateNotificationPlaybackState. updating playback position to ",
                    (System.currentTimeMillis() - mPlaybackState.getPosition()) / 1000, " seconds");
            builder
                    .setWhen(System.currentTimeMillis() - mPlaybackState.getPosition())
                    .setShowWhen(true)
                    .setUsesChronometer(true);
        } else {
            LogHelper.d(TAG, "updateNotificationPlaybackState. hiding playback position");
            builder
                    .setWhen(0)
                    .setShowWhen(false)
                    .setUsesChronometer(false);
        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    private void fetchBitmapFromURLAsync(final String artUrl,
                                         final NotificationCompat.Builder builder) {

        Glide
                .with(mMusicService)
                .load(artUrl)
                .asBitmap()
                .error(mMusicService.getDrawable(R.drawable.shuffle_bg))
                .into(new SimpleTarget<Bitmap>(MAX_ART_WIDTH_ICON, MAX_ART_HEIGHT_ICON) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
//                        image.setImageBitmap(resource); // Possibly runOnUiThread()

                        // If the media is still the same, update the notification:
                        LogHelper.d(TAG, "fetchBitmapFromURLAsync: set bitmap to ", artUrl);
                        builder.setLargeIcon(bitmap);

                        // TODO get a color from image and set it as notificaiton background
                        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
                    }
                });
    }


}
