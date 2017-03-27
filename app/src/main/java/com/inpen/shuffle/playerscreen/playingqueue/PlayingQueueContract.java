package com.inpen.shuffle.playerscreen.playingqueue;

import android.support.v4.app.FragmentActivity;

import java.util.List;

/**
 * Created by Abhishek on 3/8/2017.
 */

public interface PlayingQueueContract {

    interface PlayingQueueView {

        void updateView(List<PlayingQueueItem> itemList);

        FragmentActivity getFragmentActivity();

        void refreshViews();
    }

    interface PlayingQueueListener {
        void init();

        void stop();
    }

}
