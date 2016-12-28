package com.inpen.shuffle.mainscreen.fab;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;

import com.inpen.shuffle.mainscreen.MainActivity;

/**
 * Created by Abhishek on 12/23/2016.
 */

interface FabContract {

    interface FabView {

        void connectToMediaController();

        void showPlayer(MediaMetadataCompat metadata);

        void showShuffle();

        void showLoading();

        void disable(boolean animate);

        FragmentActivity getFragmentActivity();
    }

    interface InteractionsListener {

        void init(FabView fabView, Context context);

        void playbackStateChanged(PlaybackStateCompat state);

        void metadataChanged(MediaMetadataCompat metadata);

        MediaControllerCompat.Callback getControllerCallback();

        void shuffleClicked(MainActivity activity);

        void stop();

        void playerIconClicked(AppCompatActivity activity);

        void setTransportControls(MediaControllerCompat.TransportControls transportControls);
    }

}
