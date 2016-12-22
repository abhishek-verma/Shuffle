package com.inpen.shuffle.model.repositories;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import java.util.List;

/**
 * Created by Abhishek on 12/14/2016.
 */

public class PlaylistsRepository {

    ///////////////////////////////////////////////////////////////////////////
    // Static fields
    ///////////////////////////////////////////////////////////////////////////

    public static PlaylistsRepository mPlaylistsRepositoryInstance;

    ///////////////////////////////////////////////////////////////////////////
    // Regular fields and methods
    ///////////////////////////////////////////////////////////////////////////

    private final Context mContext;

    public PlaylistsRepository(Context context) {
        mContext = context;
    }

    public static synchronized PlaylistsRepository getInstance(Context context) {
        if (mPlaylistsRepositoryInstance == null)
            mPlaylistsRepositoryInstance = new PlaylistsRepository(context);

        return mPlaylistsRepositoryInstance;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public methods
    ///////////////////////////////////////////////////////////////////////////

    public void insertPlaylistEntry(String playlistaName, MediaMetadataCompat mediaMetadata) {

    }

    public void bulkInsertPlaylistEntries(String playlistName, List<MediaMetadataCompat> mediaMetadataList) {

    }

    public void removePlaylistEntry(String playlistName, MediaMetadataCompat mediaMetadata) {

    }

    public void removePlaylist(String playlistName) {

    }

    public void renamePLaylist(String oldPlaylistName, String newPlaylistName) {

    }

    public List<String> getPlaylists() {

        return null;
    }

}
