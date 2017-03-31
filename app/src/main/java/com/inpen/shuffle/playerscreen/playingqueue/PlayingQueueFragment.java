package com.inpen.shuffle.playerscreen.playingqueue;

import android.animation.Animator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.inpen.shuffle.R;
import com.inpen.shuffle.model.repositories.QueueRepository;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abhishek on 3/16/2017.
 */

public class PlayingQueueFragment extends Fragment implements PlayingQueueContract.PlayingQueueView {

    public PlayingQueueAdapter mPlayingQueueAdapter;
    PlayingQueueContract.PlayingQueueListener mActionsListener;
    @BindView(R.id.playingQueueParent)
    RelativeLayout mParentView;
    @BindView(R.id.playingQueueRecyclerView)
    RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionsListener = new PlayingQueuePresenter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playing_queue, container, false);

        ButterKnife.bind(this, view);

        mPlayingQueueAdapter = new PlayingQueueAdapter(new ArrayList<PlayingQueueItem>(0));

        mRecyclerView.setAdapter(mPlayingQueueAdapter);

        RecyclerView.LayoutManager lm = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(lm);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(mPlayingQueueAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        mActionsListener.init();

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            showReveal();
            mRecyclerView.smoothScrollToPosition(QueueRepository.getInstance().getCurrentIndex());
        } else {
            hideReveal();
        }
    }

    public void showReveal() {
        //doing reveal animation
        int cx = (int) (mParentView.getMeasuredWidth() * 0.90f);
        int cy = 0;

        double hypt = Math.hypot(mParentView.getMeasuredHeight(), mParentView.getMeasuredWidth());
        int finalRadius = (int) (hypt);

        mParentView.setVisibility(View.VISIBLE);

        try {
            final Animator animator = ViewAnimationUtils.createCircularReveal(mParentView, cx, cy, 0, finalRadius);
            animator.setDuration(400);
            animator.start();
        } catch (IllegalStateException e) {

        }
    }

    public void hideReveal() {
        //doing reveal animation
        int cx = (int) (mParentView.getMeasuredWidth() * 0.90f);
        int cy = 0;

        double hypt = Math.hypot(mParentView.getMeasuredHeight(), mParentView.getMeasuredWidth());
        int initialRadius = (int) (hypt);

        mParentView.setVisibility(View.INVISIBLE);

        try {
            final Animator animator = ViewAnimationUtils.createCircularReveal(mParentView, cx, cy, initialRadius, 0);
            animator.setDuration(400);
            animator.start();
        } catch (IllegalStateException e) {

        }
    }

    @Override
    public FragmentActivity getFragmentActivity() {
        return getActivity();
    }

    @Override
    public void refreshViews() {
        mPlayingQueueAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateView(List<PlayingQueueItem> itemList) {
        mPlayingQueueAdapter.updateData(itemList);
    }

}
