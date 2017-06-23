package com.inpen.shuffle.playerscreen.recommendation;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.abhi.bottomslidingdialog.BottomSlidingDialog;
import com.inpen.shuffle.R;
import com.inpen.shuffle.customviews.LocalRecommendationItemView;
import com.inpen.shuffle.model.MutableMediaMetadata;
import com.inpen.shuffle.model.repositories.QueueRepository;
import com.inpen.shuffle.model.repositories.SongsRepository;
import com.inpen.shuffle.playerscreen.recommendation.RecommendationContract.RecommendationView;
import com.inpen.shuffle.utility.LogHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Abhishek on 6/17/2017.
 */

public class RecommendationPresenter
        implements RecommendationContract.RecommendationViewListener {
    private static final String LOG_TAG = LogHelper.makeLogTag(RecommendationPresenter.class);

    private RecommendationView mView;

    private LoaderManager.LoaderCallbacks<Set<LocalRecommendationItem>>
            mLocalRecLoaderCallbacks = new LoaderManager.LoaderCallbacks<Set<LocalRecommendationItem>>() {

        @Override
        public Loader<Set<LocalRecommendationItem>> onCreateLoader(int i, Bundle bundle) {
            return new LocalRecomendationAsyncLoader(mView.getCompatActivity().getApplicationContext());
        }

        @Override
        public void onLoadFinished(Loader<Set<LocalRecommendationItem>> loader, Set<LocalRecommendationItem> localRecommendationItems) {
            dataUpdated(localRecommendationItems);
        }

        @Override
        public void onLoaderReset(Loader<Set<LocalRecommendationItem>> loader) {
            dataUpdated(null);
        }

    };

    @Override
    public void init(RecommendationContract.RecommendationView view) {
        mView = view;

        int LOCAL_REC_LOADER_ID = 0;
        mView.getCompatActivity()
                .getLoaderManager()
                .initLoader(LOCAL_REC_LOADER_ID, null, mLocalRecLoaderCallbacks);
    }

    @Override
    public void stop() {

    }

    @Override
    public View.OnClickListener getLocalRecommendationClickListener() {

        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalRecommendationItem item = ((LocalRecommendationItemView) view).getItem();

                final List<MutableMediaMetadata> metadataList = new ArrayList<>();

                metadataList
                        .add(new SongsRepository(
                                mView.getCompatActivity())
                                .getSongMetadataForId(item.id));

                new BottomSlidingDialog(mView.getCompatActivity())
                        .setDialogTitle(item.title)
                        .addAction(R.string.play_now,
                                R.drawable.ic_play_arrow_black_24dp,
                                0)
                        .addAction(R.string.play_next,
                                R.drawable.ic_playlist_add_black_24dp,
                                1)
                        .setActionListener(new BottomSlidingDialog.ActionListener() {
                            @Override
                            public void onActionSelected(int actionId) {
                                switch (actionId) {
                                    case 0:
                                        QueueRepository.getInstance().addNextSongs(metadataList, mView.getCompatActivity());
                                        mView.getCompatActivity().getMediaController().getTransportControls().skipToNext();
                                        break;
                                    case 1:
                                        QueueRepository.getInstance().addNextSongs(metadataList, mView.getCompatActivity());
                                        break;
                                }
                            }
                        })
                        .show();
            }
        };
    }

    private void dataUpdated(@Nullable Set<LocalRecommendationItem> localRecommendationItems) {
        if (localRecommendationItems == null) {
            mView.showLocalRecommendation(
                    new ArrayList<LocalRecommendationItem>());
        } else {
            mView.showLocalRecommendation(
                    new ArrayList<>(localRecommendationItems));
        }
    }
}
