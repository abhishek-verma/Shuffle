package com.inpen.shuffle.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;

import com.inpen.shuffle.model.repositories.SelectedItemsRepository;
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

    public QueueProvider(Context context) {
        mContext = context;
    }

    /* @deprecated As
     * this method generates a metadata list (High on memory).
     * Use generateShuffledQueueIds instead
     */
    @Deprecated
    public List<MutableMediaMetadata> generateShuffledQueueMetadata(@Nullable SelectedItemsRepository selectedItemsRepository) {
        SongsRepository songsRepository = new SongsRepository(mContext);

        List<MutableMediaMetadata> songList;

        if(selectedItemsRepository != null) {
            // Get filtered song life from SongRepo
            songList = songsRepository.getSongsMetadataByFilter(
                    selectedItemsRepository.getSelectedItemList(CustomTypes.ItemType.SONG),
                    CustomTypes.ItemType.SONG);
            songList.addAll(songsRepository.getSongsMetadataByFilter(
                    selectedItemsRepository.getSelectedItemList(CustomTypes.ItemType.ALBUM_KEY),
                    CustomTypes.ItemType.ALBUM_KEY
            ));
            songList.addAll(songsRepository.getSongsMetadataByFilter(
                    selectedItemsRepository.getSelectedItemList(CustomTypes.ItemType.ARTIST_KEY),
                    CustomTypes.ItemType.ARTIST_KEY
            ));
            songList.addAll(songsRepository.getSongsMetadataByFilter(
                    selectedItemsRepository.getSelectedItemList(CustomTypes.ItemType.FOLDER),
                    CustomTypes.ItemType.FOLDER
            ));
            songList.addAll(songsRepository.getSongsMetadataByFilter(
                    selectedItemsRepository.getSelectedItemList(CustomTypes.ItemType.PLAYLIST),
                    CustomTypes.ItemType.PLAYLIST
            ));
        } else {
            songList = songsRepository.getAllSongs();
        }

        // get liked mSongs from songRepo
        List<String> playlistAsList = new ArrayList<>();
        playlistAsList.add(StaticStrings.PLAYLIST_NAME_LIKED);
        List<MutableMediaMetadata> likedSongs = songsRepository.getSongsMetadataByFilter(playlistAsList, CustomTypes.ItemType.PLAYLIST);

        // get disliked mSongs from songRepo
        playlistAsList.clear();
        playlistAsList.add(StaticStrings.PLAYLIST_NAME_DISLIKED);
        List<MutableMediaMetadata> dislikedSongs = songsRepository.getSongsMetadataByFilter(playlistAsList, CustomTypes.ItemType.PLAYLIST);

        setLikedAndDisliked(songList, likedSongs, dislikedSongs);

        return songList;
    }

    //    public List<String> generateShuffledQuequeId(List<String> selectorItemIdList, CustomTypes.ItemType itemType) {
//        SongsRepository songsRepository = new SongsRepository(mContext);
//
//        // Get filtered song life from SongRepo
//        mSongs = songsRepository.getSongsIdsByFilter(selectorItemIdList, itemType);
//
//        // get liked mSongs from songRepo
//        List<String> playlistAsList = new ArrayList<String>();
//        playlistAsList.add(StaticStrings.PLAYLIST_NAME_LIKED);
//        mLikedSongs = songsRepository.getSongsIdsByFilter(playlistAsList, CustomTypes.ItemType.PLAYLIST);
//
//        // get disliked mSongs from songRepo
//        playlistAsList.clear();
//        playlistAsList.add(StaticStrings.PLAYLIST_NAME_DISLIKED);
//        mDislikedSongs = songsRepository.getSongsIdsByFilter(playlistAsList, CustomTypes.ItemType.PLAYLIST);
//
//        shuffle();
//
//        return mSongs;
//    }

    private void setLikedAndDisliked(List<MutableMediaMetadata> songList, List<MutableMediaMetadata> likedSongs, List<MutableMediaMetadata> dislikedSongs) {
        for (MutableMediaMetadata song : songList) {
            if (likedSongs.contains(song)) {
                song.setLiked();
            }

            if (dislikedSongs.contains(song)) {
                song.setDisliked();
            }
        }
    }

    public void shuffle(List<MutableMediaMetadata> songList) {
        Collections.shuffle(songList);

        //apply biased shuffle algo here
        weightedShuffle(songList);
    }


    public void weightedShuffle(List<MutableMediaMetadata> songsList) {

        final double LIKED_PREF = 0.7/*very_often=0.8, often=0.7, normal= 0.6*/,
                NORMAL_PREF = 0.6,
                DISLIKED_PREF = 0.4/*normal=0.6, less_often=0.5, rarely=0.4, never=0*/;

        RatingCompat rating;
        int i;

        for (i = (int) (songsList.size() * 0.5); i < songsList.size(); i++) {
            rating = songsList.get(i).metadata.getRating(MediaMetadataCompat.METADATA_KEY_USER_RATING);

            double pref =
                    (rating != null && rating.isRated()) ?
                            rating.isThumbUp() ? LIKED_PREF : DISLIKED_PREF
                            : NORMAL_PREF;

            double rand = Math.random();

            if (pref > NORMAL_PREF) {
                if (rand * pref > 0.5) {
                    //swap to song to somewhere between
                    //0 to (int)songs.size()*(1-random*pref)
                    //ie random()*((int) songs.size()*(1-random*pref)

                    int dest = (int) (
                            Math.random()
                                    * (songsList.size() * (1 - rand * pref))
                    );

                    Collections.swap(songsList, i, dest);
                }
            }
        }

        for (i = 0; i < (int) (songsList.size() * 0.6); i++) {
            rating = songsList.get(i).metadata.getRating(MediaMetadataCompat.METADATA_KEY_USER_RATING);

            double pref =
                    (rating != null && rating.isRated()) ?
                            rating.isThumbUp() ? LIKED_PREF : DISLIKED_PREF
                            : NORMAL_PREF;

            double rand = Math.random();

            if (pref < NORMAL_PREF) {

                if (rand * pref < 0.37) {
                    //swap to song to somewhere between
                    //(int)songs.size()*(1-random*pref) to songs.size()
                    //ie (int)songs.size*(1-random*pref + random()*(random*pref))

                    int dest = (int) (
                            songsList.size()
                                    * (1 - rand * pref + Math.random() * (rand * pref))
                    );

                    Collections.rotate(songsList.subList(i, dest + 1), -1);
                    i--;//so that the counter does not skip the next song
                }
            }
        }
    }

}
