package com.inpen.shuffle.playerscreen.recommendation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inpen.shuffle.R;
import com.inpen.shuffle.playerscreen.recommendation.RecommendationContract.RecommendationViewListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abhishek on 6/17/2017.
 */

public class RecommendationFragment extends Fragment
        implements RecommendationContract.RecommendationView {

    @BindView(R.id.localRecomendationsRecV)
    RecyclerView mRecyclerView;
    private LocalRecommendationAdapter mLocalRecommendationsAdapter;
    private RecommendationViewListener mActionsListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recommendation, container, false);
        ButterKnife.bind(this, rootView);

        mActionsListener = new RecommendationPresenter();

        setupViewsAndAdapter();

        mActionsListener.init(this);

        return rootView;
    }

    private void setupViewsAndAdapter() {
        mLocalRecommendationsAdapter = new LocalRecommendationAdapter(
                new ArrayList<LocalRecommendationItem>(0),
                mActionsListener.getLocalRecommendationClickListener());

        mRecyclerView.setAdapter(mLocalRecommendationsAdapter);

        RecyclerView.LayoutManager lm = new GridLayoutManager(getContext(), 2, LinearLayoutManager.HORIZONTAL, false);

        mRecyclerView.setLayoutManager(lm);
    }

    @Override
    public void onStop() {
        mActionsListener.stop();
        super.onStop();
    }

    @Override
    public void showLocalRecommendation(List<LocalRecommendationItem> itemList) {
        mLocalRecommendationsAdapter.updateData(itemList);
    }

    @Override
    public AppCompatActivity getCompatActivity() {
        return (AppCompatActivity) getActivity();
    }


}
