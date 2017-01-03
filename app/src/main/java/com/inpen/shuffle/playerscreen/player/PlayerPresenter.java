package com.inpen.shuffle.playerscreen.player;

import android.os.Bundle;

import com.inpen.shuffle.model.repositories.QueueRepository;

/**
 * Created by Abhishek on 12/29/2016.
 */

public class PlayerPresenter implements PlayerFragmentContract.PlayerFragmentListener {

    int mIndex = -1;
    private PlayerFragmentContract.PlayerFragmentView mView;

    public PlayerPresenter(PlayerFragmentContract.PlayerFragmentView view, Bundle fragmentArgs) {
        mIndex = fragmentArgs.getInt(PlayerFragment.EXTRA_INT_INDEX);

        mView = view;
    }

    @Override
    public void init() {
        mView.initView(QueueRepository.getInstance().getSongForIndex(mIndex).metadata);
    }

    @Override
    public void stop() {

    }
}
