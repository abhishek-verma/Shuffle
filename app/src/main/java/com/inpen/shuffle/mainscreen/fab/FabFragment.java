package com.inpen.shuffle.mainscreen.fab;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inpen.shuffle.model.MutableMediaMetadata;
import com.inpen.shuffle.utility.LogHelper;

/**
 * Created by Abhishek on 12/23/2016.
 */

public class FabFragment extends Fragment implements FabContract.FabView {
    private static final int MAX_ART_WIDTH_ICON = 128;  // pixels
    private static final int MAX_ART_HEIGHT_ICON = 128;  // pixels
    String TAG = LogHelper.makeLogTag(FabFragment.class);

    private FabContract.InteractionsListener mFabInteractionListener;
    private FabViewManager mFabManager;
    private MediaMetadataCompat mMetadata;
    private PlaybackStateCompat mPlaybackState;

    public static FabFragment newInstance() {
        return new FabFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFabInteractionListener = new FabPresenter();
        mFabManager = new FabViewManager((FabViewManager.FabViewManagerListener) mFabInteractionListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFabManager.initViews(inflater, container, getContext());
        return mFabManager.getFabView();
    }

    @Override
    public void onStart() {
        super.onStart();
        mFabManager.resume();
        mFabInteractionListener.init(this, getContext());
    }

    @Override
    public void connectToMediaController() {
        MediaControllerCompat controller = getActivity()
                .getSupportMediaController();
        LogHelper.e(TAG, "onConnected, mediaController==null? ", controller == null);
        if (controller != null) {
//            mFabInteractionListener.metadataChanged(controller.getMetadata());
            mFabInteractionListener.playbackStateChanged(controller.getPlaybackState());
            controller.registerCallback(mFabInteractionListener.getControllerCallback());
            mFabInteractionListener.setTransportControls(controller.getTransportControls());
        }
    }

    @Override
    public void onStop() {
        LogHelper.d(TAG, "onStop called!");
        mFabInteractionListener.stop();
        mFabManager.stop();
        super.onStop();
    }

    @Override
    public FragmentActivity getFragmentActivity() {
        return getActivity();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implementation methods for FabView
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void updatePlayer(MediaMetadataCompat metadata, PlaybackStateCompat stateCompat) {

        if (metadata == null) { // todo check if required, since also checked in presenter
            mMetadata = null;
            mPlaybackState = stateCompat;
            return;
        }

        if (mFabManager.isPlayerMode()) {

            // noinspection ResourceType
            if (!mMetadata.getString(MutableMediaMetadata.CUSTOM_METADATA_KEY_TRACK_ID)
                    .equals(metadata.getString(MutableMediaMetadata.CUSTOM_METADATA_KEY_TRACK_ID))) {
                mMetadata = metadata;
                mFabManager.updatePlayerMetadata(mMetadata, getContext());
            }

            if (!(mPlaybackState == null && stateCompat == null) // not both null
                    && (mPlaybackState != stateCompat // for if one of them is null
                    || mPlaybackState.getState() != stateCompat.getState())) {
                mPlaybackState = stateCompat;

                if (mPlaybackState != null &&
                        mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mFabManager.showPauseButton();
                } else {
                    mFabManager.showPlayButton();
                }
            }
        } else {
            mMetadata = metadata;
            mFabManager.showPlayerView(mMetadata, getActivity());

            if (!(mPlaybackState == null && stateCompat == null) // not both null
                    && (mPlaybackState != stateCompat // for if one of them is null
                    || mPlaybackState.getState() != stateCompat.getState())) {
                mPlaybackState = stateCompat;

                if (mPlaybackState != null &&
                        mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mFabManager.showPauseButton();
                } else {
                    mFabManager.showPlayButton();
                }
            }
        }
    }


    @Override
    public void showShuffle() {
        mFabManager.showShuffleView(getActivity());
    }

    @Override
    public void disableFAB() {
        mFabManager.disableFab(getActivity());
    }

    @Override
    public void showPlus() {
        if (mFabManager.isPlayerMode()) {
            mFabManager.showPlusButton();
        }
    }

    @Override
    public void removePlus() {
        if (mFabManager.isPlayerMode()) {
            mFabManager.hidePlusButton();
        }
    }

    @Override
    public void updateSelectedItemCount(int count) {
        mFabManager.mSelectedItemCountTextView.setText(Integer.toString(count));
    }
}

