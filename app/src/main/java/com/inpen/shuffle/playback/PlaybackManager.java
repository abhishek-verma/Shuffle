package com.inpen.shuffle.playback;

import android.os.SystemClock;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.inpen.shuffle.model.MutableMediaMetadata;
import com.inpen.shuffle.model.repositories.QueueRepository;
import com.inpen.shuffle.utility.LogHelper;

/**
 * Created by Abhishek on 12/27/2016.
 */

public class PlaybackManager implements Playback.Callback {

    ///////////////////////////////////////////////////////////////////////////
    // Static variables and methods
    ///////////////////////////////////////////////////////////////////////////
    private static final String TAG = LogHelper.makeLogTag(PlaybackManager.class);

    ///////////////////////////////////////////////////////////////////////////
    // Regular fields and methods
    ///////////////////////////////////////////////////////////////////////////

    private final PlaybackServiceCallback mServiceCallback;
    private final Playback mPlayback;
    private QueueRepository mQueueRepository;

    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            super.onPlay();
            handlePlayRequest();
        }

        @Override
        public void onPause() {
            super.onPause();
            handlePauseRequest();
        }

        @Override
        public void onSkipToNext() {
            playNext();
        }

        @Override
        public void onSkipToPrevious() {
            playPrev();
        }

        @Override
        public void onStop() {
            LogHelper.d(TAG, "stop. current state=" + mPlayback.getState());
            handleStopRequest(null);
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }

        @Override
        public void onSetRating(RatingCompat rating) {
            setRating(rating);
        }
    };

    public PlaybackManager(PlaybackServiceCallback serviceCallback,
                           QueueRepository initializedQueueRepo,
                           Playback playback) {
        this.mServiceCallback = serviceCallback;

        mQueueRepository = initializedQueueRepo;

        mPlayback = playback;
        mPlayback.setCallback(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public methods to be called
    ///////////////////////////////////////////////////////////////////////////

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////////////////////////////

    public void handlePlayRequest() {
        MutableMediaMetadata mutableMetadata = mQueueRepository.getCurrentSong();

        if (mutableMetadata != null) {
            mPlayback.play(mutableMetadata.metadata);
            mServiceCallback.onPlaybackStart();
        }
    }

    private void handlePauseRequest() {
        LogHelper.d(TAG, "handlePauseRequest: mState=" + mPlayback.getState());
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
            mServiceCallback.onPlaybackStop();
        }
    }

    private void seekTo(int position) {
        mPlayback.seekTo(position);
    }

    private void playPrev() {
        mQueueRepository.skipQueuePosition(-1);
        handlePlayRequest();
    }

    private void playNext() {
        LogHelper.d(TAG, "skipToNext");
        mQueueRepository.skipQueuePosition(+1);
        handlePlayRequest();
    }

    private void setRating(RatingCompat rating) {
        // TODO implement rating
    }

    /**
     * Handle a request to stop music
     *
     * @param withError Error message in case the stop has an unexpected cause. The error
     *                  message will be set in the PlaybackState and will be visible to
     *                  MediaController clients.
     */
    public void handleStopRequest(String withError) {
        LogHelper.d(TAG, "handleStopRequest: mState=" + mPlayback.getState() + " error=", withError);
        mPlayback.stop(true);
        mServiceCallback.onPlaybackStop();
        updatePlaybackState(withError);
    }


    /**
     * Update the current media player state, optionally showing an error message.
     *
     * @param error if not null, error message to present to the user.
     */
    public void updatePlaybackState(String error) {
        LogHelper.d(TAG, "updatePlaybackState, playback state=" + mPlayback.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }

        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        int state = mPlayback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        // Set the activeQueueItemId if the current index is valid.
//        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
//        if (currentMusic != null) {
//            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
//        }

        mServiceCallback.onPlaybackStateUpdated(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            mServiceCallback.onNotificationRequired();
        }
    }

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SET_RATING;
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods from playback callback
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCompletion() {
        mQueueRepository.skipQueuePosition(1);
        handlePlayRequest();
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    public interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
}
