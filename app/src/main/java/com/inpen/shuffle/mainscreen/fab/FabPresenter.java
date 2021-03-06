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

    public FabPresenter() {
        mQueueRepo = QueueRepository.getInstance();
        mSelectedItemsRepo = SelectedItemsRepository.getInstance();
    }

    @Override
    public void init(FabContract.FabView fabView, Context context) {
        mFabView = checkNotNull(fabView);
        EventBus.getDefault().register(this); // TO connect to selectedItemsRepo

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
        if (mQueueRepo.getCurrentSong() != null) {
            mMediaMetadata = mQueueRepo.getCurrentSong().metadata;
        } else {
            mMediaMetadata = null;
        }

        if (mSelectedItemsRepo.isEmpty()) {
            if (mMediaMetadata != null) {
                mFabView.updatePlayer(mMediaMetadata, mPlaybackState);
            }
        } else if (!mSelectedItemsRepo.isEmpty()) {
            // show shuffle fab
            mFabView.showShuffle(mMediaMetadata != null);
        } else {
            // hide fab
            mFabView.disableFAB();
        }
    }

    @Override
    public void playbackStateChanged(PlaybackStateCompat state) {
        mPlaybackState = state;

        if (mMediaMetadata != null && mSelectedItemsRepo.isEmpty())
            mFabView.updatePlayer(mMediaMetadata, mPlaybackState);
    }

    @Override
    public void metadataChanged(MediaMetadataCompat metadata) {
        mMediaMetadata = metadata;

        if (mMediaMetadata == null) {
            // show shuffle if selected repo non empty
            if (!mSelectedItemsRepo.isEmpty()) {
                mFabView.removePlus();
            } else {// hide player view if not
                mFabView.disableFAB();
            }
        } else if (mSelectedItemsRepo.isEmpty()) {
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
        LogHelper.d(TAG, "stop method called");
        // unregister listeners
        EventBus.getDefault().unregister(this);
        MediaControllerCompat controller = mFabView.getFragmentActivity()
                .getSupportMediaController();
        if (controller != null) {
            LogHelper.d(TAG, "unregister controller callbacks");
            controller.unregisterCallback(getControllerCallback());
        }
        //TODO remove fab
    }

    @Override
    public void setTransportControls(MediaControllerCompat.TransportControls transportControls) {
        mTransportControls = transportControls;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRepositoryEmptyStateChanged(SelectedItemsRepository.RepositoryEmptyStateChangedEvent event) {
        if (!event.isEmpty) { // selected
            if (mQueueRepo.isInitialized()) { // playing
                mFabView.showShuffle(true);
            } else { // not playing
                mFabView.showShuffle(false);
            }
        } else { // deselected
            if (mQueueRepo.isInitialized()) { // playing
                mFabView.updatePlayer(mMediaMetadata, mPlaybackState);
            } else { // not playing
                mFabView.disableFAB();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSelectedItemCountChanged(SelectedItemsRepository.SelectedItemCountChanged event) {
        mFabView.updateSelectedItemCount(mSelectedItemsRepo.getSelectedItemCount());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicPlayerConnected(MainPresenter.MusicServiceConnectedEvent event) {
        mFabView.connectToMediaController();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueueIndexChanged(QueueRepository.QueueIndexChangedEvent event) {

        if (!QueueRepository.getInstance().isInitialized())
            return;

        MediaMetadataCompat metadata = QueueRepository.getInstance().getCurrentSong().metadata;

        LogHelper.d(TAG, "Received metadata state change to mediaId=",
                metadata.getDescription().getMediaId(),
                " song=", metadata.getDescription().getTitle());
        metadataChanged(metadata);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueueMetadataChanged(QueueRepository.QueueMetadataChangedEvent event) {

        MediaMetadataCompat metadata = QueueRepository.getInstance().getCurrentSong().metadata;

        LogHelper.d(TAG, "Received metadata state change to mediaId=",
                metadata.getDescription().getMediaId(),
                " song=", metadata.getDescription().getTitle());
        metadataChanged(metadata);
    }
    ///////////////////////////////////////////////////////////////////////////
    // Implementation methods for FabManagerListener
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void shuffleClicked() {
        mQueueRepo.initialize(mFabView.getFragmentActivity(),
                SelectedItemsRepository.getInstance(),
                new QueueRepository.RepositoryInitializedCallback() {
                    @Override
                    public void onRepositoryInitialized(boolean success) {
                        if (success) {
                            mTransportControls.play();

                            SelectedItemsRepository
                                    .getInstance()
                                    .clearItems(); //TODO removed since it flickered the fab, do something else instead
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
        // add selected items to the queue
        mQueueRepo.addItemsToQueue(mFabView.getFragmentActivity(),
                SelectedItemsRepository.getInstance(),
                new QueueRepository.RepositoryInitializedCallback() {
                    @Override
                    public void onRepositoryInitialized(boolean success) {
                        if (success) {

                            SelectedItemsRepository
                                    .getInstance()
                                    .clearItems(); //TODO removed since it flickered the fab, do something else instead
                        }
                    }
                });
    }

    @Override
    public void playPauseClicked() {
        if (mPlaybackState != null && mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            mTransportControls.pause();
        } else {
            mTransportControls.play();
        }
    }

    @Override
    public void closePlayerClicked() {

        if (!mSelectedItemsRepo.isEmpty()) {
            // show shuffle fab
            mFabView.showShuffle(false);
        } else {
            // hide fab
            mFabView.disableFAB();
        }

        mMediaMetadata = null;
        mPlaybackState = null;
        mTransportControls.stop();
    }

    @Override
    public void deselectButtonClicked() {
        mSelectedItemsRepo.clearItems();
    }
}