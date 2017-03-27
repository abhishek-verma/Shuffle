/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inpen.shuffle.model;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.text.TextUtils;

/**
 * Holder class that encapsulates a MediaMetadata and allows the actual metadata to be modified
 * without requiring to rebuild the collections the metadata is in.
 */
public class MutableMediaMetadata {

    public static String CUSTOM_METADATA_KEY_TRACK_SOURCE = "__SOURCE__";
    public static String CUSTOM_METADATA_KEY_TRACK_ID = "__ID__";
    public static String CUSTOM_METADATA_KEY_FOLDER_PATH = "__FOLDER_PATH__";
    public static String CUSTOM_METADATA_KEY_ARTIST_KEY = "__ARTIST_KEY__";
    public static String CUSTOM_METADATA_KEY_ALBUM_KEY = "__ALBUM_KEY__";
    public final String trackId;
    public MediaMetadataCompat metadata;

    public MutableMediaMetadata(String trackId, MediaMetadataCompat metadata) {
        this.metadata = metadata;
        this.trackId = trackId;
    }

    public static String generateTrackID(String title, String artist, long duration) {
        // Math.min to prevent endIndex > length()
        // which throws IndexOutOfBondsException
        String durationString = String.valueOf(duration);

        return new StringBuffer(title.substring(0, Math.min(title.length(), 10)).replaceAll("[^\\w\\s\\-_]", ""))
                .append(artist.substring(0, Math.min(artist.length(), 5)).replaceAll("[^\\w\\s\\-_]", ""))
                .append(durationString
                        .substring(0, Math.min(durationString.length(), 5)).replaceAll("[^\\w\\s\\-_]", ""))
                .toString();
    }

    public void setLiked() {
        RatingCompat likedRating = RatingCompat.newThumbRating(true);

        metadata = new MediaMetadataCompat
                .Builder(metadata)
                .putRating(MediaMetadataCompat.METADATA_KEY_USER_RATING, likedRating)
                .build();

    }

    public void setDisliked() {
        RatingCompat dislikedRating = RatingCompat.newThumbRating(false);

        metadata = new MediaMetadataCompat
                .Builder(metadata)
                .putRating(MediaMetadataCompat.METADATA_KEY_USER_RATING, dislikedRating)
                .build();
    }

    public void setUnrated() {
        RatingCompat unratedRating = RatingCompat.newUnratedRating(RatingCompat.RATING_NONE);

        metadata = new MediaMetadataCompat
                .Builder(metadata)
                .putRating(MediaMetadataCompat.METADATA_KEY_USER_RATING, unratedRating)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != MutableMediaMetadata.class) {
            return false;
        }

        MutableMediaMetadata that = (MutableMediaMetadata) o;

        return TextUtils.equals(trackId, that.trackId);
    }

    @Override
    public int hashCode() {
        return trackId.hashCode();
    }
}
