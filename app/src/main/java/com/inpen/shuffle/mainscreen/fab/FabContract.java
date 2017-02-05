package com.inpen.shuffle.mainscreen.fab;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Created by Abhishek on 12/23/2016.
 */

interface FabContract {

    interface FabView {

        void connectToMediaController();

        FragmentActivity getFragmentActivity();

        void updatePlayer(MediaMetadataCompat metadata, PlaybackStateCompat stateCompat);

        void showShuffle();

        void disableFAB();

        void showPlus();

        void removePlus();

    }

    interface InteractionsListener {

        void init(FabView fabView, Context context);

        void playbackStateChanged(PlaybackStateCompat state);

        void metadataChanged(MediaMetadataCompat metadata);

        MediaControllerCompat.Callback getControllerCallback();

        void stop();

        void setTransportControls(MediaControllerCompat.TransportControls transportControls);

    }

}
