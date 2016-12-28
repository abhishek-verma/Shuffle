package com.inpen.shuffle.mainscreen.fab;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.inpen.shuffle.mainscreen.MainActivity;
import com.inpen.shuffle.mainscreen.MainPresenter;
import com.inpen.shuffle.model.repositories.QueueRepository;
import com.inpen.shuffle.model.repositories.SelectedItemsRepository;
import com.inpen.shuffle.utility.LogHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 12/23/2016.
 */

public class FabPresenter implements FabContract.InteractionsListener {

    private static final String TAG = LogHelper.makeLogTag(FabPresenter.class);

    private FabContract.FabView mFabView;

    private boolean mShouldShowLoading = false;
    private boolean mShouldShowPlayer = false;
    private boolean mShouldShowShuffle = false;
    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            LogHelper.d(TAG, "Received playback state change to state ", state.getState());
            playbackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);

            LogHelper.d(TAG, "Received metadata state change to mediaId=",
                    metadata.getDescription().getMediaId(),
                    " song=", metadata.getDescription().getTitle());
            metadataChanged(metadata);
        }
    };
    private MediaControllerCompat.TransportControls mTransportControls;

    @Override
    public void init(@NonNull FabContract.FabView fabView, Context context) {
        mFabView = checkNotNull(fabView);

        mFabView.connectToMediaController();

        EventBus.getDefault().register(this);

        if (!SelectedItemsRepository.getInstance().isEmpty()) {
            mShouldShowShuffle = true;
            updateView(null);
        }
    }

    @Override
    public void stop() {
        mFabView.disable(false);
    }

    @Override
    public void setTransportControls(MediaControllerCompat.TransportControls transportControls) {
        mTransportControls = transportControls;
    }

    @Override
    public void playbackStateChanged(PlaybackStateCompat state) {
        LogHelper.d(TAG, "onPlaybackStateChanged ", state);
        if (mFabView.getFragmentActivity() == null) {
            LogHelper.w(TAG, "onPlaybackStateChanged called when getActivity null," +
                    "this should not happen if the callback was properly unregistered. Ignoring.");
            return;
        }
        if (state == null) {
            mShouldShowPlayer = true;
            return;
        }

    }

    @Override
    public void metadataChanged(MediaMetadataCompat metadata) {

        LogHelper.d(TAG, "onMetadataChanged ", metadata);
        if (mFabView.getFragmentActivity() == null) {
            LogHelper.w(TAG, "onMetadataChanged called when getActivity null," +
                    "this should not happen if the callback was properly unregistered. Ignoring.");
            return;
        }
        if (metadata == null) {
            mShouldShowPlayer = false;
            return;
        }

        mShouldShowPlayer = true;
        updateView(metadata);
    }

    /**
     * @param metadata to pass to showPlayer method
     */
    private void updateView(@Nullable MediaMetadataCompat metadata) {
        LogHelper.d(TAG, "update View called");
        if (mShouldShowLoading) {
            mFabView.showLoading();
        } else if (!mShouldShowPlayer &&
                !mShouldShowShuffle) {
            mFabView.disable(true);
        } else if (mShouldShowShuffle) {
            mFabView.showShuffle();
        } else {
            mFabView.showPlayer(metadata);
        }
    }

    @Override
    public void shuffleClicked(MainActivity activity) {
        mShouldShowShuffle = false;
        mShouldShowLoading = true;

        updateView(null);

        QueueRepository mQueueRepository = QueueRepository.getInstance(); // hold reference to queue repository somehow
        mQueueRepository.initialize(activity,
                SelectedItemsRepository.getInstance(),
                new QueueRepository.RepositoryInitializedCallback() {
                    @Override
                    public void onRepositoryInitialized(boolean success) {
                        if (success) {
                            mTransportControls.play();
                            mShouldShowLoading = false;

                            SelectedItemsRepository
                                    .getInstance()
                                    .clearItems(true);
                        } else {
                            mShouldShowShuffle = true;
                            mShouldShowLoading = false;
                            updateView(null);

                            LogHelper.e(TAG, "Cannot initialize queue repo, SHIT IS SERIOUS DAMMIT!");
                        }
                    }
                });
    }

    @Override
    public void playerIconClicked(AppCompatActivity activity) {
        // TODO launch player
        Toast.makeText(activity, "Launching Player stub", Toast.LENGTH_SHORT);
    }

    @Override
    public MediaControllerCompat.Callback getControllerCallback() {
        return mControllerCallback;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRepositoryEmptyStateChanged(SelectedItemsRepository.RepositoryEmptyStateChangedEvent event) {
        // mShouldShowShuffle true when !isEmpty && false when isEmpty
        if (event.isEmpty == mShouldShowShuffle) { // that means mShouldShowShuffle state must be changed!
            mShouldShowShuffle = !event.isEmpty;
            updateView(null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicPlayerConnected(MainPresenter.MusicServiceConnectedEvent event) {
        mFabView.connectToMediaController();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicPlayerDisconnected(MainPresenter.MusicServiceDisconnectedEvent event) {

    }
}
