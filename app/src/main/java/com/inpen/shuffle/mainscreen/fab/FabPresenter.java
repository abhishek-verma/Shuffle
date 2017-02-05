package com.inpen.shuffle.mainscreen.fab;

import android.content.Context;
import android.content.Intent;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.inpen.shuffle.mainscreen.MainPresenter;
import com.inpen.shuffle.model.repositories.QueueRepository;
import com.inpen.shuffle.model.repositories.SelectedItemsRepository;
import com.inpen.shuffle.playerscreen.PlayerActivity;
import com.inpen.shuffle.utility.LogHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 12/23/2016.
 */

public class FabPresenter implements FabContract.InteractionsListener, FabViewManager.FabViewManagerListener {

    private static final String TAG = LogHelper.makeLogTag(FabPresenter.class);
    private final QueueRepository mQueueRepo;
    private final SelectedItemsRepository mSelectedItemsRepo;

    private FabContract.FabView mFabView;

    private MediaControllerCompat.TransportControls mTransportControls;

    private PlaybackStateCompat mPlaybackState;
    private MediaMetadataCompat mMediaMetadata;

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            LogHelper.d(TAG, "Received playback state change to state ", state.getState());
            playbackStateChanged(state);
        }

    };

    QueueRepository.QueueMetadataCallback mQueueMetadataChangedCallback = new QueueRepository.QueueMetadataCallback() {
        @Override
        public void onMetadataChanged() {

            MediaMetadataCompat metadata = QueueRepository.getInstance().getCurrentSong().metadata;

            LogHelper.d(TAG, "Received metadata state change to mediaId=",
                    metadata.getDescription().getMediaId(),
                    " song=", metadata.getDescription().getTitle());
            metadataChanged(metadata);
        }
    };

    public FabPresenter() {
        mQueueRepo = QueueRepository.getInstance();
        mSelectedItemsRepo = SelectedItemsRepository.getInstance();
    }

    @Override
    public void init(FabContract.FabView fabView, Context context) {
        mFabView = checkNotNull(fabView);

        mFabView.connectToMediaController(); // To register to controllerCallbacks

        EventBus.getDefault().register(this); // TO connect to selectedItemsRepo

        mQueueRepo.setmQueueMetadataCallbackObserver(mQueueMetadataChangedCallback); // To connect to queueRepo

        if (mQueueRepo.isInitialized()) {
            updateFABFromInit();
        } else if (!mQueueRepo.isCatchEmpty(context)) {
            mQueueRepo.initialize(context,
                    null,
                    new QueueRepository.RepositoryInitializedCallback() {
                        @Override
                        public void onRepositoryInitialized(boolean success) {
                            if (success)
                                updateFABFromInit();
                        }
                    });
        }
    }

    /**
     * this method DOES NOT take all the scenarios
     * this method is only to be called from {@link #init} and is used to shorten the method,
     * direct view methods should be called from everywhere else
     */
    private void updateFABFromInit() {
        mMediaMetadata = mQueueRepo.getCurrentSong().metadata;

        if (mQueueRepo.getCurrentSong() != null) {
            mFabView.updatePlayer(mMediaMetadata, mPlaybackState);
            if (!mSelectedItemsRepo.isEmpty()) {
                // show + sign
                mFabView.showPlus();
            }
        } else if (!mSelectedItemsRepo.isEmpty()) {
            // show shuffle fab
            mFabView.showShuffle();
        } else {
            // hide fab
            mFabView.disableFAB();
        }
    }

    @Override
    public void playbackStateChanged(PlaybackStateCompat state) {
        mPlaybackState = state;

        if (mMediaMetadata != null)
            mFabView.updatePlayer(mMediaMetadata, mPlaybackState);
    }

    @Override
    public void metadataChanged(MediaMetadataCompat metadata) {
        mMediaMetadata = metadata;

        if (mMediaMetadata == null) {
            // show shuffle if selected repo non empty
            if (!mSelectedItemsRepo.isEmpty()) {
                mFabView.showShuffle();
            } else {// hide player view if not
                mFabView.disableFAB();
            }
        } else {
            // update player
            mFabView.updatePlayer(mMediaMetadata, mPlaybackState);
        }
    }

    @Override
    public MediaControllerCompat.Callback getControllerCallback() {
        return mControllerCallback;
    }

    @Override
    public void stop() {
        // unregister listeners
        mQueueRepo.setmQueueMetadataCallbackObserver(null); // To unregister from queueRepo
        EventBus.getDefault().unregister(this);
        MediaControllerCompat controller = mFabView.getFragmentActivity()
                .getSupportMediaController();
        if (controller != null) {
            controller.unregisterCallback(getControllerCallback());
        }

        // TODO remove fab
    }

    @Override
    public void setTransportControls(MediaControllerCompat.TransportControls transportControls) {
        mTransportControls = transportControls;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRepositoryEmptyStateChanged(SelectedItemsRepository.RepositoryEmptyStateChangedEvent event) {
        if (!event.isEmpty) {
            if (mQueueRepo.isInitialized()) {
                mFabView.showPlus();
            } else {
                mFabView.showShuffle();
            }
        } else {
            if (mQueueRepo.isInitialized()) {
                mFabView.removePlus();
            } else {
                mFabView.disableFAB();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicPlayerConnected(MainPresenter.MusicServiceConnectedEvent event) {
        mFabView.connectToMediaController();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implementation methods for FabManagerListener
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void shuffleClicked() {

        QueueRepository mQueueRepository = QueueRepository.getInstance(); // hold reference to queue repository somehow
        mQueueRepository.initialize(mFabView.getFragmentActivity(),
                SelectedItemsRepository.getInstance(),
                new QueueRepository.RepositoryInitializedCallback() {
                    @Override
                    public void onRepositoryInitialized(boolean success) {
                        if (success) {
                            mTransportControls.play();

                            SelectedItemsRepository
                                    .getInstance()
                                    .clearItems(true); //TODO removed since it flickered the fab, do something else instead
                        }
                    }
                });
    }

    @Override
    public void playerIconClicked() {
        // launch player
        Context context = mFabView.getFragmentActivity();
        context.startActivity(new Intent(context, PlayerActivity.class));
    }

    @Override
    public void plusButtonClicked() {
        // TODO implement
    }

    @Override
    public void playPauseClicked() {
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            mTransportControls.pause();
        } else {
            mTransportControls.play();
        }
    }

    @Override
    public void closePlayerClicked() {

    }
}