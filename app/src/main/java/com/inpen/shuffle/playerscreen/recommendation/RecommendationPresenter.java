package com.inpen.shuffle.playerscreen.recommendation;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;

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

                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(mView.getCompatActivity());
                View sheetView
                        = mView
                        .getCompatActivity()
                        .getLayoutInflater()
                        .inflate(R.layout.recommend_botton_sheet_dialog, null);

                mBottomSheetDialog.setContentView(sheetView);
                mBottomSheetDialog.show();

                sheetView
                        .findViewById(R.id.optionPlayNow)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                QueueRepository.getInstance().addNextSongs(metadataList, mView.getCompatActivity());
                                mView.getCompatActivity().getMediaController().getTransportControls().skipToNext();
                                mBottomSheetDialog.dismiss();
                            }
                        });

                sheetView
                        .findViewById(R.id.optionPlayNext)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                QueueRepository.getInstance().addNextSongs(metadataList, mView.getCompatActivity());
                                mBottomSheetDialog.dismiss();
                            }
                        });
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
