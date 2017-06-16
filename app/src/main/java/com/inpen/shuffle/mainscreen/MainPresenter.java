package com.inpen.shuffle.mainscreen;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.inpen.shuffle.R;
import com.inpen.shuffle.model.repositories.SearchRepositiory;
import com.inpen.shuffle.playback.MusicService;
import com.inpen.shuffle.syncmedia.SyncMediaIntentService;
import com.inpen.shuffle.utility.LogHelper;

import org.greenrobot.eventbus.EventBus;

import static android.content.Context.SEARCH_SERVICE;
import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 12/21/2016.
 */

public class MainPresenter implements MainScreenContract.ActivityActionsListener {

    private static final String LOG_TAG = LogHelper.makeLogTag(MainPresenter.class);
    private MainScreenContract.MainView mMainView;

    private boolean mBound;
    private SearchRepositiory mSearchRepo;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.MusicServiceBinder binder = (MusicService.MusicServiceBinder) service;
            MusicService musicService = binder.getService();
            mBound = true;

            MediaSessionCompat.Token token = musicService.getSessionToken();

            try {
                mMainView.connectToSession(token);

                EventBus.getDefault()
                        .post(new MusicServiceConnectedEvent(token));
            } catch (RemoteException e) {
                e.printStackTrace();
                LogHelper.e(LOG_TAG, "could not connectToSession: " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public MainPresenter(@NonNull MainScreenContract.MainView mainView) {
        mMainView = checkNotNull(mainView);
    }

    @Override
    public void init(Context context) {
        scanMedia(context);
        connectToService(context);
    }

    @Override
    public void stop(Context context) {
        // Unbind from the service
        if (mBound) {
            EventBus.getDefault()
                    .post(new MusicServiceDisconnectedEvent());

            context.unbindService(mConnection);
            mBound = false;
        }
    }

    private void connectToService(Context context) {
        Intent serviceIntent = new Intent(context, MusicService.class);
        context.startService(serviceIntent);
        context.bindService(serviceIntent, mConnection, 0);
    }

    @Override
    public void gotPermissionResult(Context context, boolean hasPermissionsResult) {
        if (hasPermissionsResult)
            scanMedia(context);
        else
            mMainView.getPermissions();
    }

    public void scanMedia(Context context) {

        if (mMainView.hasPermissions())
            SyncMediaIntentService.syncMedia(context);
        else
            mMainView.getPermissions();
    }

    @Override
    public void setupSearch(Menu menu) {

        SearchManager searchManager = (SearchManager) mMainView
                .getActivityContext()
                .getSystemService(SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(mMainView.getActivityContext().getComponentName()));

        if (mSearchRepo == null) mSearchRepo = SearchRepositiory.getInstance();

        mSearchRepo.setSearchView(searchView);

    }

    public class MusicServiceConnectedEvent {

        private final MediaSessionCompat.Token mediaSessionToken;

        public MusicServiceConnectedEvent(MediaSessionCompat.Token token) {
            mediaSessionToken = token;
        }
    }

    public class MusicServiceDisconnectedEvent {
    }
}
