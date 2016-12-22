package com.inpen.shuffle.model;

import android.content.Context;

import com.inpen.shuffle.model.repositories.SongsRepository;
import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.StaticStrings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used to generate shuffled list of songs
 * Takes into account the liked songs and disliked songs
 */

public class QueueProvider {

    private final Context mContext;

    private List<MutableMediaMetadata> mSongs, mLikedSongs, mDislikedSongs;

    public QueueProvider(Context context) {
        mContext = context;
    }

    /* @deprecated As
     * this method generates a metadata list (High on memory).
     * Use generateShuffledQueueIds instead
     */
    @Deprecated
    public List<MutableMediaMetadata> generateShuffledQuequeMetadata(List<String> selectorItemIdList, CustomTypes.ItemType itemType) {
        SongsRepository songsRepository = new SongsRepository(mContext);

        // Get filtered song life from SongRepo
        mSongs = songsRepository.getSongsMetadataByFilter(selectorItemIdList, itemType);

        // get liked mSongs from songRepo
        List<String> playlistAsList = new ArrayList<>();
        playlistAsList.add(StaticStrings.PlAYLIST_NAME_LIKED);
        mLikedSongs = songsRepository.getSongsMetadataByFilter(playlistAsList, CustomTypes.ItemType.PLAYLIST);

        // get disliked mSongs from songRepo
        playlistAsList.clear();
        playlistAsList.add(StaticStrings.PlAYLIST_NAME_DISLIKED);
        mDislikedSongs = songsRepository.getSongsMetadataByFilter(playlistAsList, CustomTypes.ItemType.PLAYLIST);

        setLikedAndDisliked();

        shuffle();

        return mSongs;
    }

    //    public List<String> generateShuffledQuequeId(List<String> selectorItemIdList, CustomTypes.ItemType itemType) {
//        SongsRepository songsRepository = new SongsRepository(mContext);
//
//        // Get filtered song life from SongRepo
//        mSongs = songsRepository.getSongsIdsByFilter(selectorItemIdList, itemType);
//
//        // get liked mSongs from songRepo
//        List<String> playlistAsList = new ArrayList<String>();
//        playlistAsList.add(StaticStrings.PlAYLIST_NAME_LIKED);
//        mLikedSongs = songsRepository.getSongsIdsByFilter(playlistAsList, CustomTypes.ItemType.PLAYLIST);
//
//        // get disliked mSongs from songRepo
//        playlistAsList.clear();
//        playlistAsList.add(StaticStrings.PlAYLIST_NAME_DISLIKED);
//        mDislikedSongs = songsRepository.getSongsIdsByFilter(playlistAsList, CustomTypes.ItemType.PLAYLIST);
//
//        shuffle();
//
//        return mSongs;
//    }

    private void setLikedAndDisliked() {
        for (MutableMediaMetadata song : mSongs) {
            if (mLikedSongs.contains(song)) {
                song.setLiked();
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(mSongs);

        //TODO apply biased shuffle algo here
    }

}
