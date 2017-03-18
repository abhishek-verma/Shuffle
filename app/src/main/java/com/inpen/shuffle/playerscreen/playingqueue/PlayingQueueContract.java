package com.inpen.shuffle.playerscreen.playingqueue;

import android.support.v4.media.MediaMetadataCompat;

import java.util.List;

/**
 * Created by Abhishek on 3/8/2017.
 */

public interface PlayingQueueContract {

    interface PlayingQueueView {
        void updateView(List<MediaMetadataCompat> metadataCompatList);
    }

    interface PlayingQueueListener {
        void init();

        void onItemClicked(int position);

        void onItemRemoved(int position);
    }

}
