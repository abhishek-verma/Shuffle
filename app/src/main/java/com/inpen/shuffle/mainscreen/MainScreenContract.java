package com.inpen.shuffle.mainscreen;

import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

/**
 * Created by Abhishek on 10/25/2016.
 */

public interface MainScreenContract {

    interface MainView {

        boolean hasPermissions();

        void getPermissions();

        AppCompatActivity getActivityContext();

        void connectToSession(MediaSessionCompat.Token token) throws RemoteException;
    }

    interface ActivityActionsListener {

        void init(Context context);

        void gotPermissionResult(Context context, boolean hasPermissionsResult);

        void stop(Context context);

        void setupSearch(Menu menu);
    }

}
