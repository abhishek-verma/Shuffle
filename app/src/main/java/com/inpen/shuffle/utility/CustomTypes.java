package com.inpen.shuffle.utility;

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

    public enum RepositoryState {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    public enum FabMode {
        SHUFFLE, LOADING, PLAYER, DISABLED, ANIMATING
    }
}
