package com.inpen.shuffle.mainscreen;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Abhishek on 10/25/2016.
 */

public interface MainScreenContract {

    interface MainView {

        boolean hasPermissions();

        void getPermissions();

        AppCompatActivity getActivityContext();

    }

    interface ActivityActionsListener {

        void init(Context context);

        void gotPermissionResult(Context context, boolean hasPermissionsResult);

        void shuffleClicked(Context context);

    }

}
