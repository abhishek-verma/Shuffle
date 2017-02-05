package com.inpen.shuffle.utility;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CustomTypes {


    /**
     * Created by Abhishek on 12/10/2016.
     */

    public enum ItemType {
        ALBUM_KEY, ARTIST_KEY, PLAYLIST, GENRE, FOLDER;

        public static int toInt(ItemType it) {
            switch (it) {
                case ALBUM_KEY:
                    return 0;
                case ARTIST_KEY:
                    return 1;
                case PLAYLIST:
                    return 2;
                case GENRE:
                    return 3;
                case FOLDER:
                    return 4;
                default:
                    return -1;
            }
        }

        public static ItemType fromInt(int i) {
            switch (i) {
                case 0:
                    return ALBUM_KEY;
                case 1:
                    return ARTIST_KEY;
                case 2:
                    return PLAYLIST;
                case 3:
                    return GENRE;
                case 4:
                    return FOLDER;
                default:
                    return null;
            }
        }
    }

    @IntDef({RepositoryState.NON_INITIALIZED, RepositoryState.INITIALIZING, RepositoryState.INITIALIZED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RepositoryState {
        int NON_INITIALIZED = -1, INITIALIZING = 0, INITIALIZED = 1;
    }

    @IntDef({FabMode.SHUFFLE, FabMode.PLAYER, FabMode.PLAYER_WITH_ADD, FabMode.DISABLED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FabMode {
        int SHUFFLE = 100,
                PLAYER = 110,
                PLAYER_WITH_ADD = 120,
                DISABLED = 130;
    }

}
