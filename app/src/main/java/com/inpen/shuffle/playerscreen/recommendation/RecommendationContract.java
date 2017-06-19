package com.inpen.shuffle.playerscreen.recommendation;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.inpen.shuffle.utility.BaseItem;

import java.util.List;

/**
 * Created by Abhishek on 6/17/2017.
 */

interface RecommendationContract {

    interface RecommendationView {

        void showLocalRecommendation(List<LocalRecommendationItem> itemList);

        AppCompatActivity getCompatActivity();
    }

    interface RecommendationViewListener {

        void init(RecommendationView view);

        void stop();

        View.OnClickListener getLocalRecommendationClickListener();

    }
}
