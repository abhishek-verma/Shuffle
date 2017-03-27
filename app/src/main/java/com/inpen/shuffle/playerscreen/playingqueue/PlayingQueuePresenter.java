package com.inpen.shuffle.playerscreen.playingqueue;

import android.media.session.MediaController;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;

import com.inpen.shuffle.model.repositories.QueueRepository;
import com.inpen.shuffle.playback.PlaybackManager;
import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.LogHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek on 3/26/2017.
 */

public class PlayingQueuePresenter implements PlayingQueueContract.PlayingQueueListener {

    private static final String LOG_TAG = LogHelper.makeLogTag(PlayingQueuePresenter.class);

    private PlayingQueueContract.PlayingQueueView mplayingQueueView;

    public PlayingQueuePresenter(PlayingQueueContract.PlayingQueueView playingQueueView) {
        mplayingQueueView = playingQueueView;
    }

    @Override
    public void init() {
        EventBus.getDefault().register(this);

        updateView();
    }

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayingQueueEvent(PlayingQueueAdapter.PlayingQueueEvent event) {
        switch (event.eventType) {
            case CustomTypes.PlayingQueueEventType.CLICKED:
                onItemClicked(event.positions[0]);
                break;
            case CustomTypes.PlayingQueueEventType.SWIPED:
                onItemRemoved(event.positions[0]);
                break;
            case CustomTypes.PlayingQueueEventType.MOVED:
                onItemMoved(event.positions[0], event.positions[1]);
        }
    }

    public void onItemClicked(int position) {
        LogHelper.d(LOG_TAG, "item at position " + position + " clicked!");

        MediaController.TransportControls transportControls = mplayingQueueView.getFragmentActivity()
                .getMediaController().getTransportControls();

        Bundle b = new Bundle();
        b.putInt(PlaybackManager.SKIP_TO_ITEM_INDEX, position);

        transportControls.sendCustomAction(PlaybackManager.SKIP_TO_ITEM_ACTION, b);
    }

    public void onItemRemoved(int position) {
        LogHelper.d(LOG_TAG, "item at position " + position + " removed!");

        QueueRepository.getInstance().removeItemAtIndex(position);
    }

    public void onItemMoved(int positionInit, int positionFinal) {
        LogHelper.d(LOG_TAG, "item at position " + positionInit + " moved to " + positionFinal);
    }

    private void updateView() {

        new AsyncTask<Void, Void, List<PlayingQueueItem>>() {

            @Override
            protected List<PlayingQueueItem> doInBackground(Void... voids) {

                QueueRepository repo = QueueRepository.getInstance();
                int size = repo.getSize();

                ArrayList<PlayingQueueItem> playingQueueItemList = new ArrayList<>(size);

                for (int i = 0; i < size; i++) {
                    MediaMetadataCompat metadata = repo.getSongForIndex(i).metadata;

                    playingQueueItemList
                            .add(new PlayingQueueItem(metadata, i));
                }

                return playingQueueItemList;
            }

            @Override
            protected void onPostExecute(List<PlayingQueueItem> playingQueueItems) {
                super.onPostExecute(playingQueueItems);

                mplayingQueueView.updateView(playingQueueItems);
            }
        }.execute();
    }
}
