package com.inpen.shuffle.mainscreen;

import android.content.Context;
import android.support.annotation.NonNull;

import com.inpen.shuffle.syncmedia.SyncMediaIntentService;
import com.inpen.shuffle.utility.LogHelper;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 12/21/2016.
 */

public class MainPresenter implements MainScreenContract.ActivityActionsListener {

    private static final String LOG_TAG = LogHelper.makeLogTag(MainPresenter.class);
    private MainScreenContract.MainView mMainView;


    public MainPresenter(@NonNull MainScreenContract.MainView mainView) {
        mMainView = checkNotNull(mainView);
    }

    @Override
    public void init(Context context) {
        scanMedia(context);

    }

    @Override
    public void gotPermissionResult(Context context, boolean hasPermissionsResult) {
        scanMedia(context);
    }

    @Override
    public void shuffleClicked(Context context) {

    }

    public void scanMedia(Context context) {

        if (mMainView.hasPermissions())
            SyncMediaIntentService.syncMedia(context);
        else
            mMainView.getPermissions();
    }
}
