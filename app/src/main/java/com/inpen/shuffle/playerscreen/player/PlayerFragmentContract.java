package com.inpen.shuffle.playerscreen.player;

import android.support.v4.media.MediaMetadataCompat;

/**
 * Created by Abhishek on 12/28/2016.
 */

public interface PlayerFragmentContract {

    interface PlayerFragmentView {

        void initView(MediaMetadataCompat metadata);

        void updateUIState(boolean playing);

    }

    interface PlayerFragmentListener {

        void init();

        void stop();

    }
}
