package com.inpen.shuffle.model.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inpen.shuffle.model.MutableMediaMetadata;
import com.inpen.shuffle.model.QueueProvider;
import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.LogHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.inpen.shuffle.utility.CustomTypes.RepositoryState.INITIALIZED;

/**
 * Created by Abhishek on 12/10/2016.
 */

public class QueueRepository {

    ///////////////////////////////////////////////////////////////////////////
    // Static fields
    ///////////////////////////////////////////////////////////////////////////
    private static final String LOG_TAG = LogHelper.makeLogTag(QueueRepository.class);
    private static final String AUDIO_STORAGE = "com.inpen.shuffle.AUDIO_STORAGE";
    private static final String KEY_PLAYING_QUEUE = "playing_queue";
    private static final String KEY_CURRENT_TRACK_INDEX = "current_track_index";
    public static QueueRepository mQueueRepositoryInstance;

    ///////////////////////////////////////////////////////////////////////////
    // Regular fields and methods
    ///////////////////////////////////////////////////////////////////////////
    // cached playing queue
    public List<MutableMediaMetadata> mPlayingQueue;
    public int mCurrentTrackIndex = -1;
    private SharedPreferences mPreferences;
    private volatile CustomTypes.RepositoryState mCurrentState = CustomTypes.RepositoryState.NON_INITIALIZED;

    public static synchronized QueueRepository getInstance() {
        if (mQueueRepositoryInstance == null)
            mQueueRepositoryInstance = new QueueRepository();

        return mQueueRepositoryInstance;
    }

    public synchronized void initialize(@NonNull final Context context,
                                        @Nullable final SelectedItemsRepository selectedItemsRepository,
                                        @Nullable final QueueRepositoryCallback queueRepositoryCallback) {


        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                try {

                    if (mCurrentState == CustomTypes.RepositoryState.NON_INITIALIZED) {
                        mCurrentState = CustomTypes.RepositoryState.INITIALIZING;

                        if (selectedItemsRepository != null) {
                            retrieveQueue(context, selectedItemsRepository);
                            storeQueue(context);
                        } else if (!isCatchEmpty(context)) {
                            loadCachedQueue(context, queueRepositoryCallback);
                        }
                    }

                } finally {
                    if (mCurrentState != INITIALIZED) {
                        // Something bad happened, so we reset state to NON_INITIALIZED to allow
                        // retries (eg if the network connection is temporary unavailable)
                        mCurrentState = CustomTypes.RepositoryState.NON_INITIALIZED;
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if (queueRepositoryCallback != null) {
                    queueRepositoryCallback.onRepositoryInitialized(isInitialized());
                }
            }
        }.execute();

    }

    public
    @Nullable
    MutableMediaMetadata getCurrentSong() {
        if (!isInitialized())
            return null;

        if (mCurrentTrackIndex < 0 && isInitialized())
            mCurrentTrackIndex = 0;

        return mPlayingQueue.get(mCurrentTrackIndex);
    }

    public void skipQueuePosition(int amt) {

        int index = mCurrentTrackIndex + amt;
        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = 0;
        } else {
            // skip forwards when in last song will cycle back to play of the queue
            index %= mPlayingQueue.size();
        }
        mCurrentTrackIndex = index;
    }

    public void setCurrentQueueItem(MutableMediaMetadata mutableMediaMetadata) {
        if (mPlayingQueue != null)
            mCurrentTrackIndex = mPlayingQueue.indexOf(mutableMediaMetadata);

    }

    public boolean isInitialized() {
        return mCurrentState == INITIALIZED;
    }

    public void clearQueue(Context context) {

        mCurrentState = CustomTypes.RepositoryState.NON_INITIALIZED;
        mPlayingQueue.clear();
        mCurrentTrackIndex = -1;

        SharedPreferences.Editor editor = getmPreferences(context).edit();
        editor.clear();
        editor.apply();
    }

    public boolean isCatchEmpty(Context context) {
        return getmPreferences(context).contains(KEY_PLAYING_QUEUE);
    }


    public void setLiked(String musicId, boolean liked) {
        // TODO implement
    }


    public boolean isLiked(MutableMediaMetadata mutableMediaMetadata) {
// FIXME        return mutableMediaMetadata.metadata.getRating(MediaMetadataCompat.METADATA_KEY_USER_RATING).getRating().   ;
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////////////////////////////

    private void retrieveQueue(Context context, SelectedItemsRepository selectedItemsRepository) {

        QueueProvider queueProvider = new QueueProvider(context);

        mPlayingQueue = queueProvider
                .generateShuffledQuequeMetadata(
                        selectedItemsRepository.getSelectedItemIdList(),
                        selectedItemsRepository.getItemType());

        mCurrentTrackIndex = -1;

        mCurrentState = INITIALIZED;
    }

    private void loadCachedQueue(Context context, final QueueRepositoryCallback callback) {

        Gson gson = new Gson();
        String json = getmPreferences(context).getString(KEY_PLAYING_QUEUE, null);

        Type type = new TypeToken<ArrayList<MutableMediaMetadata>>() {
        }.getType();

        mPlayingQueue = gson.fromJson(json, type);
        mCurrentTrackIndex = getmPreferences(context).getInt(KEY_CURRENT_TRACK_INDEX, -1);

        if (mPlayingQueue != null && mPlayingQueue.size() > 0) {
            mCurrentState = CustomTypes.RepositoryState.INITIALIZED;
        }
    }

    private void storeQueue(Context context) {

        SharedPreferences.Editor editor = getmPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(mPlayingQueue);
        editor.putString(KEY_PLAYING_QUEUE, json);

        editor.putInt(KEY_CURRENT_TRACK_INDEX, mCurrentTrackIndex);
        editor.apply();
    }

    private SharedPreferences getmPreferences(Context context) {

        if (mPreferences == null)
            mPreferences = context.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE);

        return mPreferences;
    }

    public interface QueueRepositoryCallback {
        void onRepositoryInitialized(boolean success);
    }

}
