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

            if (mDislikedSongs.contains(song)) {
                song.setDisliked();
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(mSongs);

        //apply biased shuffle algo here
        weightedShuffle();
    }


    private void weightedShuffle() {

        final double LIKED_PREF = 0.7/*very_often=0.8, often=0.7, normal= 0.6*/,
                NORMAL_PREF = 0.6,
                DISLIKED_PREF = 0.4/*normal=0.6, less_often=0.5, rarely=0.4, never=0*/;

        int i;

        for (i = (int) (mSongs.size() * 0.5); i < mSongs.size(); i++) {
            double pref = mLikedSongs.contains(mSongs.get(i)) ?
                    LIKED_PREF :
                    mDislikedSongs.contains(mSongs.get(i)) ? DISLIKED_PREF : NORMAL_PREF;

            double rand = Math.random();

            if (pref > NORMAL_PREF) {
                if (rand * pref > 0.5) {
                    //swap to song to somewhere between
                    //0 to (int)songs.size()*(1-random*pref)
                    //ie random()*((int) songs.size()*(1-random*pref)

                    int dest = (int) (
                            Math.random()
                                    * (mSongs.size() * (1 - rand * pref))
                    );

                    Collections.swap(mSongs, i, dest);
                }
            }
        }

        for (i = 0; i < (int) (mSongs.size() * 0.6); i++) {
            double pref = mLikedSongs.contains(mSongs.get(i)) ?
                    LIKED_PREF :
                    mDislikedSongs.contains(mSongs.get(i)) ? DISLIKED_PREF : NORMAL_PREF;

            double rand = Math.random();

            if (pref < NORMAL_PREF) {

                if (rand * pref < 0.37) {
                    //swap to song to somewhere between
                    //(int)songs.size()*(1-random*pref) to songs.size()
                    //ie (int)songs.size*(1-random*pref + random()*(random*pref))

                    int dest = (int) (
                            mSongs.size()
                                    * (1 - rand * pref + Math.random() * (rand * pref))
                    );

                    Collections.rotate(mSongs.subList(i, dest + 1), -1);
                    i--;//so that the counter does not skip the next song
                }
            }
        }
    }

}
