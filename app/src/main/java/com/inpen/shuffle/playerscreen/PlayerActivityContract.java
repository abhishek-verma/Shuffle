package com.inpen.shuffle.playerscreen;

import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;

/**
 * Created by Abhishek on 12/28/2016.
 */

public interface PlayerActivityContract {

    interface PlayerActivityView {

        void connectToSession(MediaSessionCompat.Token token) throws RemoteException;

        void updateMetadataViews(MediaMetadataCompat metadata);

        void updatePlaybackStateViews(PlaybackStateCompat playbackState);

        void togglePlaylistVisibility();
    }

    interface PlayerActivityListener {

        void init(Context context);

        void stop(Context context);

        void playbackStateChanged(PlaybackStateCompat playbackStateCompat);

        void metadataChanged(MediaMetadataCompat metadataCompat);

        void playPauseButtonClicked();

        void prevButtonClicked();

        void nextButtonClicked();

        void likeButtonClicked();

        void dislikeButtonClicked();

        MediaControllerCompat.Callback getControllerCallback();

        void setTransportControls(MediaControllerCompat.TransportControls transportControls);

        ViewPager.OnPageChangeListener getPageChangeListener();

        void showPlaylistClicked();
    }
}
