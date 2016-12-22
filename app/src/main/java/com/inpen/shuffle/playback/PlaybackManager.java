package com.inpen.shuffle.playback;

import android.os.Bundle;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.inpen.shuffle.model.repositories.QueueRepository;

public class PlaybackManager {

    ///////////////////////////////////////////////////////////////////////////
    // Static variables and methods
    ///////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////
    // Regular fields and methods
    ///////////////////////////////////////////////////////////////////////////

    private final MusicService mMusicService;
    private final Playback mPlayback;
    MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            super.onPlay();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }

        @Override
        public void onSetRating(RatingCompat rating) {
            super.onSetRating(rating);
        }
    };
    private QueueRepository mQueueRepository;


    public PlaybackManager(MusicService mMusicService) {
        this.mMusicService = mMusicService;

        mQueueRepository = new QueueRepository();
        mQueueRepository.initialize(mMusicService, null, null);

        mPlayback = new Playback();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Pulbic methods to be called
    ///////////////////////////////////////////////////////////////////////////

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    public void handlePlayRequest() {

    }

    public void handlePauseRequest() {

    }

    public void handleStopRequest() {

    }


}
