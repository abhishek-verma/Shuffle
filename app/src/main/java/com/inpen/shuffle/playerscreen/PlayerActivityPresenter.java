package com.inpen.shuffle.playerscreen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;

import com.inpen.shuffle.mainscreen.MainActivity;
import com.inpen.shuffle.model.repositories.QueueRepository;
import com.inpen.shuffle.playback.MusicService;
import com.inpen.shuffle.utility.LogHelper;

import org.greenrobot.eventbus.EventBus;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 12/28/2016.
 */

public class PlayerActivityPresenter implements PlayerActivityContract.PlayerActivityListener {
    ///////////////////////////////////////////////////////////////////////////
    // Regular fields and methods
    ///////////////////////////////////////////////////////////////////////////

    private static final String LOG_TAG = LogHelper.makeLogTag(PlayerActivityPresenter.class);

    ///////////////////////////////////////////////////////////////////////////
    // Regular fields and methods
    ///////////////////////////////////////////////////////////////////////////
    public PlayerActivityContract.PlayerActivityView mPlayerActivityView;
    private boolean mBound;
    private MediaControllerCompat.TransportControls mTransportControls;

    private PlaybackStateCompat mPlaybackState;
    private MediaMetadataCompat mMediaMetadata;

    // if player viewPager is swiped, this saves the event
    // this variable is consumed in #playbackStateChanged() to know if it was due to the swipe
    private boolean mSwiped = false;

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            LogHelper.d(LOG_TAG, "Received playback state change to state ", state.getState());
            playbackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);

            LogHelper.d(LOG_TAG, "Received metadata state change to mediaId=",
                    metadata.getDescription().getMediaId(),
                    " song=", metadata.getDescription().getTitle());
            metadataChanged(metadata);
        }
    };
    private QueueRepository mQueueRepository;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.MusicServiceBinder binder = (MusicService.MusicServiceBinder) service;
            MusicService musicService = binder.getService();
            mBound = true;

            MediaSessionCompat.Token token = musicService.getSessionToken();

            try {
                mPlayerActivityView.connectToSession(token);

                EventBus.getDefault()
                        .post(new MusicServiceConnectedEvent(token));
            } catch (RemoteException e) {
                e.printStackTrace();
                LogHelper.e(LOG_TAG, "could not connectToSession: " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {

        int selectedPage = -1;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            selectedPage = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (ViewPager.SCROLL_STATE_IDLE == state && selectedPage != -1) { //Scrolling finished.
                skipSong(selectedPage);
            }
        }

        private void skipSong(final int position) {

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    int prevPosition = mQueueRepository.getCurrentIndex();

                    if (prevPosition != position) {
                        mSwiped = true;
                    }

                    if (prevPosition < position) {
                        mTransportControls.skipToNext();
                    } else if (prevPosition > position) {
                        mTransportControls.skipToPrevious();
                    }
                    return null;
                }
            }.execute();
        }
    };

    public PlayerActivityPresenter(@NonNull PlayerActivityContract.PlayerActivityView playerActivityView) {
        mPlayerActivityView = checkNotNull(playerActivityView);
        mQueueRepository = QueueRepository.getInstance();
    }

    @Override
    public void setTransportControls(MediaControllerCompat.TransportControls transportControls) {
        mTransportControls = transportControls;
    }

    @Override
    public MediaControllerCompat.Callback getControllerCallback() {
        return mControllerCallback;
    }

    @Override
    public ViewPager.OnPageChangeListener getPageChangeListener() {
        return mPageChangeListener;
    }

    @Override
    public void init(Context context) {
        if (!mQueueRepository.isInitialized()
                && mQueueRepository.isCatchEmpty(context)) {

            //launching main activity if queue empty
            Intent mainActivityIntent = new Intent(context, MainActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
            return;
        }

        connectToService(context);

    }

    @Override
    public void stop(Context context) {
        // Unbind from the service
        if (mBound) {
            EventBus.getDefault()
                    .post(new MusicServiceDisconnectedEvent());

            context.unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void playbackStateChanged(PlaybackStateCompat state) {
        boolean shouldChangeViews = true;


        if (mPlaybackState != null)
            LogHelper.e(LOG_TAG, "last Playback state: " + mPlaybackState.getState());
        LogHelper.e(LOG_TAG, "current Playback state: " + state.getState());

        //When pager is swiped, this condition is use to avoid updating the UI, if the last state was also PLAYING,
        // it also only sets mSwiped to false on the last call, that is PLAYING
        if (mSwiped) {
            shouldChangeViews = false;
            //since several playbackStateChanged are called, only save the state on the last call,
            //that is PLAYING
            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {

                // if last state was not PLAYING, then update views
                if (mPlaybackState == null || mPlaybackState.getState() != PlaybackStateCompat.STATE_PLAYING)
                    shouldChangeViews = true;

                mSwiped = false;
                mPlaybackState = state;
            }
        } else {
            mPlaybackState = state;
        }

        LogHelper.e(LOG_TAG, "should change views: " + shouldChangeViews);
        mPlayerActivityView.updatePlaybackStateViews(state, shouldChangeViews);
    }

    @Override
    public void metadataChanged(MediaMetadataCompat metadata) {
        mMediaMetadata = metadata;
        mPlayerActivityView.updateMetadataViews(metadata);
    }

    @Override
    public void playPauseButtonClicked() {
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            mTransportControls.pause();
        } else {
            mTransportControls.play();
        }
    }

    @Override
    public void likeButtonClicked() {
        RatingCompat prevRating = mMediaMetadata.getRating(MediaMetadataCompat.METADATA_KEY_USER_RATING);
        if (prevRating != null &&
                prevRating.isRated() &&
                prevRating.isThumbUp()) {
            RatingCompat unratedRating = RatingCompat.newUnratedRating(RatingCompat.RATING_THUMB_UP_DOWN);
            mTransportControls.setRating(unratedRating);
        } else {
            RatingCompat likedRating = RatingCompat.newThumbRating(true);
            mTransportControls.setRating(likedRating);
        }
    }

    @Override
    public void prevButtonClicked() {
        mTransportControls.skipToPrevious();
    }

    @Override
    public void nextButtonClicked() {
        mTransportControls.skipToNext();
    }

    @Override
    public void dislikeButtonClicked() {
        RatingCompat prevRating = mMediaMetadata.getRating(MediaMetadataCompat.METADATA_KEY_USER_RATING);
        if (prevRating != null &&
                prevRating.isRated() &&
                !prevRating.isThumbUp()) {
            RatingCompat unratedRating = RatingCompat.newUnratedRating(RatingCompat.RATING_THUMB_UP_DOWN);
            mTransportControls.setRating(unratedRating);
        } else {
            RatingCompat dislikedRating = RatingCompat.newThumbRating(false);
            mTransportControls.setRating(dislikedRating);
        }
    }

    @Override
    public void showPlaylistClicked() {
        mPlayerActivityView.togglePlaylistVisibility();
    }

    private void connectToService(Context context) {
        Intent serviceIntent = new Intent(context, MusicService.class);
        context.startService(serviceIntent);
        context.bindService(serviceIntent, mConnection, 0);
    }

    public class MusicServiceConnectedEvent {

        private final MediaSessionCompat.Token mediaSessionToken;

        public MusicServiceConnectedEvent(MediaSessionCompat.Token token) {
            mediaSessionToken = token;
        }
    }

    public class MusicServiceDisconnectedEvent {
    }
}
