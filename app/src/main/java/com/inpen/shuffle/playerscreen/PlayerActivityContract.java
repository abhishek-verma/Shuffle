package com.inpen.shuffle.playerscreen;

import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.session.MediaSessionCompat;

/**
 * Created by Abhishek on 12/28/2016.
 */

public interface PlayerActivityContract {

    interface PlayerActivityView {

        void connectToSession(MediaSessionCompat.Token token) throws RemoteException;

    }

    interface PlayerActivityListener {

        void init(Context context);

        void stop(Context context);
    }
}
