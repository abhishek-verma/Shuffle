package com.inpen.shuffle.playerscreen.recommendation;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.inpen.shuffle.model.MutableMediaMetadata;
import com.inpen.shuffle.model.repositories.QueueRepository;
import com.inpen.shuffle.model.repositories.SongsRepository;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Abhishek on 6/18/2017.
 */

public class LocalRecomendationAsyncLoader extends AsyncTaskLoader<Set<LocalRecommendationItem>> {

    private static final int MAX_RECOMMENDATIONS = 20;
    private Set<LocalRecommendationItem> mLocalRecommendationItemList;

    public LocalRecomendationAsyncLoader(Context context) {
        super(context);
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        // in cases such that device rotation,
        // loader is called again, so return the same data
        if (!takeContentChanged() && mLocalRecommendationItemList != null) {
            // if data already available and has not changed
            deliverResult(mLocalRecommendationItemList);
        } else {
            forceLoad();
        }

        if (!
                EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public Set<LocalRecommendationItem> loadInBackground() {

        String songId;


        songId = QueueRepository
                .getInstance()
                .getCurrentSong()
                .metadata
                .getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);

        //get genres, for each genre add songs from each genre
        SongsRepository songsRepository = new SongsRepository(getContext());
        List<String> genreList = songsRepository.getGenresForSong(songId);
        List<MutableMediaMetadata> songList = songsRepository.getSongsForGenres(genreList);

        for (MutableMediaMetadata mutableMediaMetadata : QueueRepository.getInstance().getQueue()) {
            //remove items already in playing queue
            if (songList.contains(mutableMediaMetadata)) {
                songList.remove(mutableMediaMetadata);
            }
        }

        Collections.shuffle(songList);

        Set<LocalRecommendationItem> itemList = new HashSet<>();
        if (songList.size() > 0) {
            for (MutableMediaMetadata mutableMetadata : songList.subList(0, Math.min(songList.size() - 1, MAX_RECOMMENDATIONS))) {
                itemList.add(new LocalRecommendationItem(mutableMetadata.metadata));
            }
        }

        return itemList;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(Set<LocalRecommendationItem> data) {
        // We’ll save the data for later retrieval
        mLocalRecommendationItemList = data;
        // We can do any pre-processing we want here
        // Just remember this is on the UI thread so nothing lengthy!
        super.deliverResult(data);
    }

    @Override
    protected void onReset() {
        super.onReset();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueueIndexChanged(QueueRepository.QueueIndexChangedEvent event) {
        onContentChanged();
    }

}

